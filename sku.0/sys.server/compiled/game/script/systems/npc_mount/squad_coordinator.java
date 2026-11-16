package script.systems.npc_mount;

import script.*;
import script.library.ai_lib;
import script.library.chat;
import script.library.utils;

// Membership/state owner attached additively to the existing leader exterior
// spawn egg.  The stock POI scripts remain the only creation/respawn owner.
public class squad_coordinator extends script.base_script
{
    public static final String TABLE = "datatables/npc_mount/squads.iff";
    public static final String ROUTE_TABLE = "datatables/npc_mount/patrol_routes.iff";
    public static final String SECTION_TABLE = "datatables/npc_mount/formation_sections.iff";
    public static final String PREFIX = "npcMountSquad.coordinator.";

    public static final String OBJVAR_PROFILE = PREFIX + "profile";
    public static final String OBJVAR_LEADER_SPAWNER = PREFIX + "leaderSpawner";
    public static final String OBJVAR_ESCORT_1_SPAWNER = PREFIX + "escort1Spawner";
    public static final String OBJVAR_ESCORT_2_SPAWNER = PREFIX + "escort2Spawner";
    public static final String OBJVAR_ROUTE_NAME = PREFIX + "routeName";
    public static final String OBJVAR_ROUTE = PREFIX + "route";
    public static final String OBJVAR_ASSEMBLY = PREFIX + "assembly";
    public static final String OBJVAR_ASSEMBLY_RADIUS = PREFIX + "assemblyRadius";
    public static final String OBJVAR_REAR_DISTANCE = PREFIX + "rearDistance";
    public static final String OBJVAR_LATERAL_DISTANCE = PREFIX + "lateralDistance";
    public static final String OBJVAR_EPOCH = PREFIX + "epoch";
    public static final String OBJVAR_GENERATION = PREFIX + "generation";
    public static final String OBJVAR_STATE = PREFIX + "state";
    public static final String OBJVAR_MEMBERS = PREFIX + "members";
    public static final String OBJVAR_READY_MASK = PREFIX + "readyMask";
    public static final String OBJVAR_COMBAT_MASK = PREFIX + "combatMask";
    public static final String OBJVAR_RECOVERY_MASK = PREFIX + "recoveryMask";
    public static final String OBJVAR_ASSEMBLED_MASK = PREFIX + "assembledMask";
    public static final String OBJVAR_CASUALTY = PREFIX + "casualty";
    public static final String OBJVAR_ON_FOOT_LEADER = PREFIX + "onFootLeader";
    public static final String OBJVAR_INTERRUPTED_STATE = PREFIX + "interruptedState";
    public static final String OBJVAR_FAILURE = PREFIX + "failure";

    public static final String SCRIPTVAR_ACTIVE = PREFIX + "active";
    public static final String SCRIPTVAR_ROUTE = PREFIX + "route";
    public static final String SCRIPTVAR_SECTIONS = PREFIX + "sections";
    public static final String SCRIPTVAR_SECTION_FILE = PREFIX + "sectionFile";
    public static final String SCRIPTVAR_SECTION_REVISION = PREFIX + "sectionRevision";
    public static final String SCRIPTVAR_FORMATION_HOLD_COUNTER = PREFIX + "formationHoldCounter";
    public static final String SCRIPTVAR_FORMATION_HOLD_TOKEN = PREFIX + "formationHoldToken";
    public static final String SCRIPTVAR_FORMATION_HOLD_EPOCH = PREFIX + "formationHoldEpoch";
    public static final String SCRIPTVAR_FORMATION_HOLD_GENERATION = PREFIX + "formationHoldGeneration";
    public static final String SCRIPTVAR_FORMATION_HOLD_LEADER = PREFIX + "formationHoldLeader";
    public static final String SCRIPTVAR_FORMATION_HELD = PREFIX + "formationHeld";
    public static final String SCRIPTVAR_FORMATION_RESUMING = PREFIX + "formationResuming";

    public squad_coordinator()
    {
    }

    public int OnAttach(obj_id self) throws InterruptedException
    {
        activateSquad(self);
        return SCRIPT_CONTINUE;
    }

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        activateSquad(self);
        return SCRIPT_CONTINUE;
    }

    public void activateSquad(obj_id self) throws InterruptedException
    {
        if (!isIdValid(self) || !exists(self) || !loadProfile(self))
        {
            return;
        }
        if (!utils.hasScriptVar(self, SCRIPTVAR_ACTIVE))
        {
            int epoch = hasObjVar(self, OBJVAR_EPOCH) ? getIntObjVar(self, OBJVAR_EPOCH) + 1 : 1;
            if (epoch <= 0)
            {
                epoch = 1;
            }
            setObjVar(self, OBJVAR_EPOCH, epoch);
            if (!hasObjVar(self, OBJVAR_GENERATION) || getIntObjVar(self, OBJVAR_GENERATION) <= 0)
            {
                setObjVar(self, OBJVAR_GENERATION, 1);
            }
            if (!hasObjVar(self, OBJVAR_STATE))
            {
                setState(self, squad_state.ASSEMBLING);
            }
            utils.setScriptVar(self, SCRIPTVAR_ACTIVE, 1);
            squad_monitor.event(self, "active", "epoch=" + epoch);
            squad_monitor.start(self);
            squad_monitor.startCensus(self);
        }
        reconcile(self);
        if (getBooleanObjVar(self, OBJVAR_CASUALTY)
                && squad_state.recoveryComplete(mask(self, OBJVAR_COMBAT_MASK), mask(self, OBJVAR_RECOVERY_MASK)))
        {
            finishRecovery(self);
        }
        else
        {
            recoverMovement(self);
        }
        tryDeparture(self);
    }

    public boolean loadProfile(obj_id self) throws InterruptedException
    {
        // A failed reload must not leave an older route available to commands.
        utils.removeScriptVar(self, SCRIPTVAR_ROUTE);
        if (!dataTableOpen(TABLE))
        {
            fail(self, "configurationTable");
            return false;
        }
        int selected = -1;
        int rows = dataTableGetNumRows(TABLE);
        for (int row = 0; row < rows; row++)
        {
            String configured = squad_binding.spawner(self, TABLE, row, "coordinatorSpawner");
            if (configured != null && configured.equals(self.toString()))
            {
                if (selected != -1)
                {
                    fail(self, "configurationAmbiguous");
                    return false;
                }
                selected = row;
            }
        }
        if (selected < 0 || !validProfileRow(self, selected))
        {
            fail(self, selected < 0 ? "configurationMissing" : "configurationInvalid");
            return false;
        }
        String profile = dataTableGetString(TABLE, selected, "name");
        String leader = squad_binding.spawner(self, TABLE, selected, "leaderSpawner");
        String escort1 = squad_binding.spawner(self, TABLE, selected, "escort1Spawner");
        String escort2 = squad_binding.spawner(self, TABLE, selected, "escort2Spawner");
        String routeName = dataTableGetString(TABLE, selected, "patrolRoute");
        location assembly = hasObjVar(self, OBJVAR_ASSEMBLY) ? getLocationObjVar(self, OBJVAR_ASSEMBLY) : getLocation(self);
        if (assembly == null || isIdValid(assembly.cell))
        {
            fail(self, "assemblyLocation");
            return false;
        }
        location[] route = loadRoute(routeName, assembly);
        if (route == null)
        {
            fail(self, "patrolRoute");
            return false;
        }
        float[] sections = loadSections(routeName);
        if (sections == null)
        {
            fail(self, "formationSections");
            return false;
        }
        setObjVar(self, OBJVAR_PROFILE, profile);
        setObjVar(self, OBJVAR_LEADER_SPAWNER, leader);
        setObjVar(self, OBJVAR_ESCORT_1_SPAWNER, escort1);
        setObjVar(self, OBJVAR_ESCORT_2_SPAWNER, escort2);
        setObjVar(self, OBJVAR_ROUTE_NAME, routeName);
        setObjVar(self, OBJVAR_ASSEMBLY, assembly);
        setObjVar(self, OBJVAR_ASSEMBLY_RADIUS, dataTableGetFloat(TABLE, selected, "assemblyRadius"));
        setObjVar(self, OBJVAR_REAR_DISTANCE, dataTableGetFloat(TABLE, selected, "rearDistance"));
        setObjVar(self, OBJVAR_LATERAL_DISTANCE, dataTableGetFloat(TABLE, selected, "lateralDistance"));
        utils.setScriptVar(self, SCRIPTVAR_SECTIONS, sections);
        // Keep only identity/origin durable. A full location array can exceed the
        // native 1000-byte objvar value limit; initialize rebuilds this cache from
        // the current route table, as loadProfile has always done.
        utils.setScriptVar(self, SCRIPTVAR_ROUTE, route);
        if (patrolRoute(self) == null)
        {
            fail(self, "patrolRouteCache");
            return false;
        }
        removeObjVar(self, OBJVAR_ROUTE); // discard a legacy shorter route only after successful reconstruction
        removeObjVar(self, OBJVAR_FAILURE);
        return true;
    }

    public location[] patrolRoute(obj_id self) throws InterruptedException
    {
        location[] route = utils.getLocationArrayScriptVar(self, SCRIPTVAR_ROUTE);
        return route != null && route.length >= 3 && route.length <= 64 ? route : null;
    }

    public boolean validProfileRow(obj_id self, int row) throws InterruptedException
    {
        String name = dataTableGetString(TABLE, row, "name");
        String coordinator = squad_binding.spawner(self, TABLE, row, "coordinatorSpawner");
        String leader = squad_binding.spawner(self, TABLE, row, "leaderSpawner");
        String escort1 = squad_binding.spawner(self, TABLE, row, "escort1Spawner");
        String escort2 = squad_binding.spawner(self, TABLE, row, "escort2Spawner");
        String route = dataTableGetString(TABLE, row, "patrolRoute");
        float radius = dataTableGetFloat(TABLE, row, "assemblyRadius");
        float rear = dataTableGetFloat(TABLE, row, "rearDistance");
        float lateral = dataTableGetFloat(TABLE, row, "lateralDistance");
        return nonempty(name) && nonempty(coordinator) && nonempty(leader) && nonempty(escort1)
                && nonempty(escort2) && nonempty(route) && coordinator.equals(leader)
                && !leader.equals(escort1) && !leader.equals(escort2) && !escort1.equals(escort2)
                && isIdValid(squad_member.parseSpawner(coordinator)) && isIdValid(squad_member.parseSpawner(leader))
                && isIdValid(squad_member.parseSpawner(escort1)) && isIdValid(squad_member.parseSpawner(escort2))
                && radius > 0.0f && radius <= 64.0f && rear > 0.0f && rear <= 64.0f
                && lateral >= 0.0f && lateral <= 64.0f;
    }

    public boolean nonempty(String value)
    {
        return value != null && value.length() > 0;
    }

    public location[] loadRoute(String routeName, location assembly) throws InterruptedException
    {
        if (!dataTableOpen(ROUTE_TABLE))
        {
            return null;
        }
        int count = 0;
        int rows = dataTableGetNumRows(ROUTE_TABLE);
        for (int row = 0; row < rows; row++)
        {
            if (routeName.equals(dataTableGetString(ROUTE_TABLE, row, "route")))
            {
                count++;
            }
        }
        if (count < 3 || count > 64)
        {
            return null;
        }
        location[] route = new location[count];
        int index = 0;
        for (int row = 0; row < rows; row++)
        {
            if (routeName.equals(dataTableGetString(ROUTE_TABLE, row, "route")))
            {
                float x = dataTableGetFloat(ROUTE_TABLE, row, "x");
                float z = dataTableGetFloat(ROUTE_TABLE, row, "z");
                if (x != x || z != z || x < -8192.0f || x > 8192.0f || z < -8192.0f || z > 8192.0f)
                {
                    return null;
                }
                location point = new location(assembly);
                point.x += x;
                point.z += z;
                point.y = getHeightAtLocation(point.x, point.z);
                route[index++] = point;
            }
        }
        return route;
    }

    public void setState(obj_id self, int state) throws InterruptedException
    {
        int previous = hasObjVar(self, OBJVAR_STATE) ? getIntObjVar(self, OBJVAR_STATE) : -1;
        setObjVar(self, OBJVAR_STATE, state);
        if (previous != state)
        {
            squad_monitor.event(self, "state", "from=" + squad_monitor.stateName(previous) + " to=" + squad_monitor.stateName(state));
        }
    }

    public int squadMonitorCensus(obj_id self, dictionary params) throws InterruptedException
    {
        squad_monitor.censusRow(self, params);
        return SCRIPT_CONTINUE;
    }

    public int squadMonitorHeartbeat(obj_id self, dictionary params) throws InterruptedException
    {
        if (!squad_monitor.current(self, params))
        {
            return SCRIPT_CONTINUE;
        }
        int state = getIntObjVar(self, OBJVAR_STATE);
        if (squad_monitor.heartbeatState(state) && hasObjVar(self, OBJVAR_MEMBERS))
        {
            obj_id[] current = members(self);
            String detail = "state=" + squad_monitor.stateName(state)
                    + " mode=" + (utils.getIntScriptVar(self, SCRIPTVAR_SECTION_FILE) != 0 ? "file" : "open")
                    + " hold=" + (formationHoldActive(self) ? 1 : 0)
                    + " " + squad_monitor.where("leader", current[squad_state.ROLE_LEADER])
                    + " " + squad_monitor.where("e1", current[squad_state.ROLE_ESCORT_1])
                    + " " + squad_monitor.where("e2", current[squad_state.ROLE_ESCORT_2]);
            for (int role = squad_state.ROLE_ESCORT_1; role <= squad_state.ROLE_ESCORT_2; role++)
            {
                if (living(current[role]))
                {
                    detail += " gap" + role + "=" + squad_monitor.tenth(escortSlotDistance(self, current[role], role));
                }
            }
            squad_monitor.event(self, "heartbeat", detail);
        }
        messageTo(self, squad_monitor.HEARTBEAT, params, squad_monitor.HEARTBEAT_SECONDS, false);
        return SCRIPT_CONTINUE;
    }

    public int squadEvent(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || !hasObjVar(self, OBJVAR_PROFILE))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id actor = params.getObjId("actor");
        String event = params.getString("event");
        if (!isIdValid(actor) || event == null)
        {
            return SCRIPT_CONTINUE;
        }
        if ("register".equals(event))
        {
            registerMember(self, actor);
            return SCRIPT_CONTINUE;
        }
        int role = roleOfMember(self, actor);
        if (!squad_state.validRole(role))
        {
            // A first callback can race its registration message.  Register only
            // through actual configured provenance, then resolve the slot again.
            registerMember(self, actor);
            role = roleOfMember(self, actor);
            if (!squad_state.validRole(role))
            {
                return SCRIPT_CONTINUE;
            }
        }
        int eventEpoch = params.getInt("epoch");
        int eventGeneration = params.getInt("generation");
        boolean formationEvent = isFormationEvent(event);
        if (formationEvent && !validFormationEnvelope(self, actor, role, event, params, eventEpoch, eventGeneration))
        {
            return SCRIPT_CONTINUE;
        }
        if ((eventEpoch != 0 && eventEpoch != getIntObjVar(self, OBJVAR_EPOCH))
                || (eventGeneration != 0 && eventGeneration != getIntObjVar(self, OBJVAR_GENERATION)))
        {
            return SCRIPT_CONTINUE;
        }

        if (!formationEvent && !"formationSectionCheck".equals(event))
        {
            squad_monitor.event(self, "member", "role=" + role + " actor=" + actor + " what=" + event);
        }
        if ("formationSectionCheck".equals(event))
        {
            refreshSectionFormation(self);
            return SCRIPT_CONTINUE;
        }
        if ("formationTooFar".equals(event))
        {
            beginFormationHold(self, actor, role);
            return SCRIPT_CONTINUE;
        }
        if ("formationRecovered".equals(event))
        {
            tryFormationResume(self);
            return SCRIPT_CONTINUE;
        }
        if ("formationHeld".equals(event))
        {
            utils.setScriptVar(self, SCRIPTVAR_FORMATION_HELD, 1);
            tryFormationResume(self);
            return SCRIPT_CONTINUE;
        }
        if ("formationResumed".equals(event))
        {
            clearFormationHold(self);
            return SCRIPT_CONTINUE;
        }
        if ("formationResumeDeferred".equals(event))
        {
            // A member can move after the coordinator's sample but before the
            // queued resume reaches the leader. Fresh escort checks retry it.
            utils.removeScriptVar(self, SCRIPTVAR_FORMATION_RESUMING);
            return SCRIPT_CONTINUE;
        }
        if ("formationHoldFailed".equals(event))
        {
            fail(self, "formationHoldFailed");
            clearFormationHold(self);
            return SCRIPT_CONTINUE;
        }

        if ("mountedReady".equals(event))
        {
            if (role == squad_state.ROLE_LEADER && validMountedLeader(actor))
            {
                setMask(self, OBJVAR_READY_MASK, squad_state.add(mask(self, OBJVAR_READY_MASK), role));
                tryDeparture(self);
            }
            return SCRIPT_CONTINUE;
        }
        if ("combatEntered".equals(event))
        {
            clearFormationHold(self);
            int state = getIntObjVar(self, OBJVAR_STATE);
            setMask(self, OBJVAR_COMBAT_MASK, squad_state.add(mask(self, OBJVAR_COMBAT_MASK), role));
            setMask(self, OBJVAR_RECOVERY_MASK, squad_state.add(mask(self, OBJVAR_RECOVERY_MASK), role));
            if (state != squad_state.COMBAT)
            {
                advanceSectionRevision(self);
                setObjVar(self, OBJVAR_INTERRUPTED_STATE, state);
                setState(self, squad_state.COMBAT);
                sendAll(self, "combat");
            }
            return SCRIPT_CONTINUE;
        }
        if ("combatExited".equals(event))
        {
            setMask(self, OBJVAR_COMBAT_MASK, squad_state.remove(mask(self, OBJVAR_COMBAT_MASK), role));
            finishRecovery(self);
            return SCRIPT_CONTINUE;
        }
        if ("tetherComplete".equals(event))
        {
            setMask(self, OBJVAR_COMBAT_MASK, squad_state.remove(mask(self, OBJVAR_COMBAT_MASK), role));
            setMask(self, OBJVAR_RECOVERY_MASK, squad_state.remove(mask(self, OBJVAR_RECOVERY_MASK), role));
            finishRecovery(self);
            return SCRIPT_CONTINUE;
        }
        if ("terminalRetire".equals(event) || "lostPair".equals(event))
        {
            boolean interruptedFormationHold = formationHoldActive(self);
            clearFormationHold(self);
            if (role == squad_state.ROLE_LEADER && departed(self) && living(actor))
            {
                // A genuine forced dismount remains terminal for the mount pair,
                // but not for this living squad generation.
                setObjVar(self, OBJVAR_ON_FOOT_LEADER, 1);
                setMask(self, OBJVAR_READY_MASK, squad_state.add(mask(self, OBJVAR_READY_MASK), role));
                finishRecovery(self);
                if (interruptedFormationHold && getIntObjVar(self, OBJVAR_STATE) == squad_state.PATROLLING)
                {
                    recoverMovement(self);
                }
            }
            else if (role == squad_state.ROLE_LEADER)
            {
                setMask(self, OBJVAR_READY_MASK, squad_state.remove(mask(self, OBJVAR_READY_MASK), role));
            }
            return SCRIPT_CONTINUE;
        }
        if ("memberLost".equals(event))
        {
            loseMember(self, role, actor);
            return SCRIPT_CONTINUE;
        }
        if ("unloaded".equals(event))
        {
            // An unloaded object cannot remain an actionable member reference.
            // Its normal initialize handshake can reclaim the configured slot.
            loseMember(self, role, actor);
            return SCRIPT_CONTINUE;
        }
        if ("assemblyArrived".equals(event))
        {
            if (isAssembled(self, actor))
            {
                setMask(self, OBJVAR_ASSEMBLED_MASK, squad_state.add(mask(self, OBJVAR_ASSEMBLED_MASK), role));
                if (getIntObjVar(self, OBJVAR_STATE) == squad_state.REGROUPING
                        && (mask(self, OBJVAR_ASSEMBLED_MASK) & occupiedMask(self)) == occupiedMask(self))
                {
                    setState(self, squad_state.ASSEMBLING);
                    tryDeparture(self);
                }
            }
            return SCRIPT_CONTINUE;
        }
        if ("departed".equals(event))
        {
            if (role == squad_state.ROLE_LEADER && getIntObjVar(self, OBJVAR_STATE) == squad_state.DEPARTING)
            {
                setState(self, squad_state.PATROLLING);
                sendPatrolFormation(self, false);
            }
            return SCRIPT_CONTINUE;
        }
        if ("calmReady".equals(event))
        {
            if (formationHoldActive(self) && role == squad_state.ROLE_LEADER)
            {
                return SCRIPT_CONTINUE;
            }
            if (squad_state.mayRestoreMovement(getIntObjVar(self, OBJVAR_STATE), mask(self, OBJVAR_COMBAT_MASK),
                    mask(self, OBJVAR_RECOVERY_MASK), getBooleanObjVar(self, OBJVAR_CASUALTY)))
            {
                refreshSectionFormation(self);
                sendMovementForRole(self, role, true);
            }
            else if (getIntObjVar(self, OBJVAR_STATE) == squad_state.REGROUPING
                    && !ai_lib.isInCombat(actor) && !aiIsTethered(actor))
            {
                sendCommand(self, actor, role, isAssembled(self, actor) ? "hold" : "return");
            }
            else if (getIntObjVar(self, OBJVAR_STATE) == squad_state.ASSEMBLING)
            {
                tryDeparture(self);
            }
            return SCRIPT_CONTINUE;
        }
        if ("commandFailed".equals(event))
        {
            fail(self, "memberCommandFailed:" + role);
        }
        return SCRIPT_CONTINUE;
    }

    public void registerMember(obj_id self, obj_id actor) throws InterruptedException
    {
        int role = roleForActor(self, actor);
        if (!squad_state.validRole(role) || !living(actor))
        {
            return;
        }
        obj_id[] members = members(self);
        obj_id current = members[role];
        if (isIdValid(current) && current != actor && memberMatchesRole(self, current, role))
        {
            // Never replace a living foreign generation merely because another
            // object claims the same role.
            return;
        }
        members[role] = actor;
        setObjVar(self, OBJVAR_MEMBERS, members);
        int ready = mask(self, OBJVAR_READY_MASK);
        if (role != squad_state.ROLE_LEADER || (getBooleanObjVar(self, OBJVAR_ON_FOOT_LEADER) && departed(self)))
        {
            ready = squad_state.add(ready, role);
        }
        if (role == squad_state.ROLE_LEADER && validMountedLeader(actor))
        {
            ready = squad_state.add(ready, role);
        }
        setMask(self, OBJVAR_READY_MASK, ready);
        if (isAssembled(self, actor))
        {
            setMask(self, OBJVAR_ASSEMBLED_MASK, squad_state.add(mask(self, OBJVAR_ASSEMBLED_MASK), role));
        }
        sendCommand(self, actor, role, "bound");
        int state = getIntObjVar(self, OBJVAR_STATE);
        if (state == squad_state.PATROLLING)
        {
            if (formationHoldActive(self) && role == squad_state.ROLE_LEADER)
            {
                sendCommand(self, actor, role, "formationHold");
            }
            else
            {
                sendMovementForRole(self, role, true);
            }
        }
        else if (state == squad_state.DEPARTING)
        {
            sendCommand(self, actor, role, role == squad_state.ROLE_LEADER ? "depart" : "follow");
        }
        else if (state == squad_state.REGROUPING)
        {
            sendCommand(self, actor, role, isAssembled(self, actor) ? "hold" : "return");
        }
        tryDeparture(self);
    }

    public void loseMember(obj_id self, int role, obj_id actor) throws InterruptedException
    {
        obj_id[] members = members(self);
        if (!squad_state.validRole(role) || members[role] != actor)
        {
            return;
        }
        clearFormationHold(self);
        members[role] = obj_id.NULL_ID;
        setObjVar(self, OBJVAR_MEMBERS, members);
        setMask(self, OBJVAR_READY_MASK, squad_state.remove(mask(self, OBJVAR_READY_MASK), role));
        setMask(self, OBJVAR_ASSEMBLED_MASK, squad_state.remove(mask(self, OBJVAR_ASSEMBLED_MASK), role));
        setMask(self, OBJVAR_COMBAT_MASK, squad_state.remove(mask(self, OBJVAR_COMBAT_MASK), role));
        setMask(self, OBJVAR_RECOVERY_MASK, squad_state.remove(mask(self, OBJVAR_RECOVERY_MASK), role));
        if (role == squad_state.ROLE_LEADER)
        {
            removeObjVar(self, OBJVAR_ON_FOOT_LEADER);
        }
        if (departed(self))
        {
            setObjVar(self, OBJVAR_CASUALTY, 1);
        }
        finishRecovery(self);
    }

    public void finishRecovery(obj_id self) throws InterruptedException
    {
        if (!squad_state.recoveryComplete(mask(self, OBJVAR_COMBAT_MASK), mask(self, OBJVAR_RECOVERY_MASK)))
        {
            return;
        }
        int state = getIntObjVar(self, OBJVAR_STATE);
        int interrupted = state == squad_state.COMBAT && hasObjVar(self, OBJVAR_INTERRUPTED_STATE)
                ? getIntObjVar(self, OBJVAR_INTERRUPTED_STATE) : state;
        if (getBooleanObjVar(self, OBJVAR_CASUALTY))
        {
            if (interrupted == squad_state.REGROUPING)
            {
                setState(self, squad_state.REGROUPING);
                removeObjVar(self, OBJVAR_INTERRUPTED_STATE);
                recoverMovement(self);
            }
            else
            {
                beginRegroup(self);
            }
        }
        else if (state == squad_state.COMBAT)
        {
            setState(self, interrupted);
            removeObjVar(self, OBJVAR_INTERRUPTED_STATE);
            if (interrupted == squad_state.ASSEMBLING)
            {
                tryDeparture(self);
            }
            else
            {
                recoverMovement(self);
            }
        }
        else if (state == squad_state.ASSEMBLING)
        {
            tryDeparture(self);
        }
    }

    public void beginRegroup(obj_id self) throws InterruptedException
    {
        if (getIntObjVar(self, OBJVAR_STATE) == squad_state.REGROUPING)
        {
            return;
        }
        clearFormationHold(self);
        int generation = getIntObjVar(self, OBJVAR_GENERATION) + 1;
        if (generation <= 0)
        {
            generation = 1;
        }
        setObjVar(self, OBJVAR_GENERATION, generation);
        setState(self, squad_state.REGROUPING);
        removeObjVar(self, OBJVAR_INTERRUPTED_STATE);
        setMask(self, OBJVAR_ASSEMBLED_MASK, currentAssembledMask(self));
        obj_id[] members = members(self);
        for (int role = 0; role < squad_state.ROLE_COUNT; role++)
        {
            if (memberMatchesRole(self, members[role], role))
            {
                sendCommand(self, members[role], role, "bound");
                if (!squad_state.contains(mask(self, OBJVAR_ASSEMBLED_MASK), role))
                {
                    sendCommand(self, members[role], role, "return");
                }
                else
                {
                    sendCommand(self, members[role], role, "hold");
                }
            }
        }
        if ((mask(self, OBJVAR_ASSEMBLED_MASK) & occupiedMask(self)) == occupiedMask(self))
        {
            setState(self, squad_state.ASSEMBLING);
            tryDeparture(self);
        }
    }

    public void tryDeparture(obj_id self) throws InterruptedException
    {
        if (getIntObjVar(self, OBJVAR_STATE) != squad_state.ASSEMBLING)
        {
            return;
        }
        if (patrolRoute(self) == null)
        {
            fail(self, "departureRoute");
            return;
        }
        reconcile(self);
        int occupied = occupiedMask(self);
        int assembled = currentAssembledMask(self);
        setMask(self, OBJVAR_ASSEMBLED_MASK, assembled);
        obj_id leader = members(self)[squad_state.ROLE_LEADER];
        boolean onFoot = getBooleanObjVar(self, OBJVAR_ON_FOOT_LEADER) && living(leader);
        boolean mounted = validMountedLeader(leader);
        if (!mounted && !onFoot)
        {
            setMask(self, OBJVAR_READY_MASK, squad_state.remove(mask(self, OBJVAR_READY_MASK), squad_state.ROLE_LEADER));
        }
        if (!squad_state.canDepart(getIntObjVar(self, OBJVAR_STATE), occupied, mask(self, OBJVAR_READY_MASK), assembled,
                mounted, onFoot) || !allMembersCalm(self))
        {
            return;
        }
        setState(self, squad_state.DEPARTING);
        clearFormationHold(self);
        removeObjVar(self, OBJVAR_CASUALTY);
        setMask(self, OBJVAR_COMBAT_MASK, 0);
        setMask(self, OBJVAR_RECOVERY_MASK, 0);
        sendCommand(self, leader, squad_state.ROLE_LEADER, "depart");
        sendMovementForRole(self, squad_state.ROLE_ESCORT_1, false);
        sendMovementForRole(self, squad_state.ROLE_ESCORT_2, false);
    }

    public void sendPatrolFormation(obj_id self, boolean resume) throws InterruptedException
    {
        refreshSectionFormation(self);
        sendMovementForRole(self, squad_state.ROLE_LEADER, resume);
        sendMovementForRole(self, squad_state.ROLE_ESCORT_1, resume);
        sendMovementForRole(self, squad_state.ROLE_ESCORT_2, resume);
    }

    public void sendMovementForRole(obj_id self, int role, boolean resume) throws InterruptedException
    {
        obj_id[] current = members(self);
        if (!squad_state.validRole(role) || !memberMatchesRole(self, current[role], role))
        {
            return;
        }
        sendCommand(self, current[role], role, role == squad_state.ROLE_LEADER ? (resume ? "resume" : "patrol") : "follow");
    }

    public void sendAll(obj_id self, String command) throws InterruptedException
    {
        obj_id[] current = members(self);
        for (int role = 0; role < squad_state.ROLE_COUNT; role++)
        {
            if (memberMatchesRole(self, current[role], role))
            {
                sendCommand(self, current[role], role, command);
            }
        }
    }

    public void sendCommand(obj_id self, obj_id actor, int role, String command) throws InterruptedException
    {
        if (!isIdValid(actor) || !exists(actor))
        {
            return;
        }
        location[] route = patrolRoute(self);
        if (route == null)
        {
            fail(self, "commandRoute");
            return;
        }
        dictionary order = new dictionary();
        order.put("coordinator", self);
        order.put("command", command);
        order.put("epoch", getIntObjVar(self, OBJVAR_EPOCH));
        order.put("generation", getIntObjVar(self, OBJVAR_GENERATION));
        order.put("leader", members(self)[squad_state.ROLE_LEADER]);
        order.put("target", "depart".equals(command) ? route[0] : getLocationObjVar(self, OBJVAR_ASSEMBLY));
        order.put("route", route);
        location offset = formationOffset(self, role);
        order.put("offsetX", offset.x);
        order.put("offsetZ", offset.z);
        order.put("sectionRevision", utils.getIntScriptVar(self, SCRIPTVAR_SECTION_REVISION));
        order.put("monitorFormation", "follow".equals(command) && getIntObjVar(self, OBJVAR_STATE) == squad_state.PATROLLING ? 1 : 0);
        order.put("formationHoldToken", formationHoldActive(self) ? utils.getIntScriptVar(self, SCRIPTVAR_FORMATION_HOLD_TOKEN) : 0);
        messageTo(actor, "squadCommand", order, 0.0f, false);
    }

    public void reconcile(obj_id self) throws InterruptedException
    {
        obj_id[] current = members(self);
        int ready = mask(self, OBJVAR_READY_MASK);
        int combat = mask(self, OBJVAR_COMBAT_MASK);
        int recovery = mask(self, OBJVAR_RECOVERY_MASK);
        boolean missingAfterDeparture = false;
        for (int role = 0; role < squad_state.ROLE_COUNT; role++)
        {
            if (!memberMatchesRole(self, current[role], role))
            {
                if (departed(self))
                {
                    missingAfterDeparture = true;
                }
                current[role] = obj_id.NULL_ID;
                ready = squad_state.remove(ready, role);
                combat = squad_state.remove(combat, role);
                recovery = squad_state.remove(recovery, role);
                if (role == squad_state.ROLE_LEADER)
                {
                    removeObjVar(self, OBJVAR_ON_FOOT_LEADER);
                }
            }
            else
            {
                if (role != squad_state.ROLE_LEADER || getBooleanObjVar(self, OBJVAR_ON_FOOT_LEADER))
                {
                    ready = squad_state.add(ready, role);
                }
                if (role == squad_state.ROLE_LEADER && validMountedLeader(current[role]))
                {
                    ready = squad_state.add(ready, role);
                }
                sendCommand(self, current[role], role, "bound");
            }
        }
        setObjVar(self, OBJVAR_MEMBERS, current);
        setMask(self, OBJVAR_READY_MASK, ready);
        setMask(self, OBJVAR_COMBAT_MASK, combat);
        setMask(self, OBJVAR_RECOVERY_MASK, recovery);
        if (missingAfterDeparture)
        {
            clearFormationHold(self);
            setObjVar(self, OBJVAR_CASUALTY, 1);
        }
    }

    public void recoverMovement(obj_id self) throws InterruptedException
    {
        int state = getIntObjVar(self, OBJVAR_STATE);
        if (state == squad_state.PATROLLING)
        {
            refreshSectionFormation(self);
            if (formationHoldActive(self))
            {
                sendCommand(self, members(self)[squad_state.ROLE_LEADER], squad_state.ROLE_LEADER, "formationHold");
                sendMovementForRole(self, squad_state.ROLE_ESCORT_1, true);
                sendMovementForRole(self, squad_state.ROLE_ESCORT_2, true);
            }
            else
            {
                sendPatrolFormation(self, true);
            }
        }
        else if (state == squad_state.DEPARTING)
        {
            sendCommand(self, members(self)[squad_state.ROLE_LEADER], squad_state.ROLE_LEADER, "depart");
            sendMovementForRole(self, squad_state.ROLE_ESCORT_1, false);
            sendMovementForRole(self, squad_state.ROLE_ESCORT_2, false);
        }
        else if (state == squad_state.REGROUPING)
        {
            obj_id[] current = members(self);
            for (int role = 0; role < squad_state.ROLE_COUNT; role++)
            {
                if (memberMatchesRole(self, current[role], role))
                {
                    sendCommand(self, current[role], role, isAssembled(self, current[role]) ? "hold" : "return");
                }
            }
        }
    }

    public boolean validMountedLeader(obj_id leader) throws InterruptedException
    {
        if (!living(leader) || !hasObjVar(leader, mounted_sandtrooper.OBJVAR_SETUP_COMPLETE)
                || hasObjVar(leader, mounted_sandtrooper.OBJVAR_RETIRED))
        {
            return false;
        }
        obj_id mount = getMountId(leader);
        if (!isIdValid(mount) || !exists(mount) || getRiderId(mount) != leader
                || getObjIdObjVar(leader, mounted_sandtrooper.OBJVAR_MOUNT) != mount)
        {
            return false;
        }
        obj_id controlDevice = getObjIdObjVar(leader, mounted_sandtrooper.OBJVAR_CONTROL_DEVICE);
        return mounted_sandtrooper.isPreparedAuxiliaryPair(leader, mount, controlDevice);
    }

    public boolean allMembersCalm(obj_id self) throws InterruptedException
    {
        obj_id[] current = members(self);
        for (int role = 0; role < squad_state.ROLE_COUNT; role++)
        {
            if (!memberMatchesRole(self, current[role], role) || ai_lib.isInCombat(current[role]) || aiIsTethered(current[role]))
            {
                return false;
            }
        }
        return true;
    }

    public boolean isFormationEvent(String event)
    {
        return "formationSectionCheck".equals(event) || "formationTooFar".equals(event) || "formationRecovered".equals(event)
                || "formationHeld".equals(event) || "formationResumed".equals(event)
                || "formationHoldFailed".equals(event) || "formationResumeDeferred".equals(event);
    }

    public boolean validFormationEnvelope(obj_id self, obj_id actor, int role, String event, dictionary params,
            int epoch, int generation) throws InterruptedException
    {
        if (epoch <= 0 || generation <= 0 || epoch != getIntObjVar(self, OBJVAR_EPOCH)
                || generation != getIntObjVar(self, OBJVAR_GENERATION) || params.getInt("role") != role
                || params.getInt("sectionRevision") != utils.getIntScriptVar(self, SCRIPTVAR_SECTION_REVISION)
                || !memberMatchesRole(self, actor, role))
        {
            return false;
        }
        if ("formationSectionCheck".equals(event) || "formationTooFar".equals(event) || "formationRecovered".equals(event))
        {
            int sequence = params.getInt("sequence");
            int episode = params.getInt("episode");
            return (role == squad_state.ROLE_ESCORT_1 || role == squad_state.ROLE_ESCORT_2)
                    && sequence > 0
                    && sequence == utils.getIntScriptVar(actor, squad_member.SCRIPTVAR_FORMATION_ACTIVE_SEQUENCE)
                    && episode == utils.getIntScriptVar(actor, squad_member.SCRIPTVAR_FORMATION_EPISODE)
                    && (!"formationTooFar".equals(event) || episode > 0);
        }
        return role == squad_state.ROLE_LEADER && formationHoldActive(self)
                && params.getInt("formationHoldToken") == utils.getIntScriptVar(self, SCRIPTVAR_FORMATION_HOLD_TOKEN)
                && actor == utils.getObjIdScriptVar(self, SCRIPTVAR_FORMATION_HOLD_LEADER);
    }

    public void beginFormationHold(obj_id self, obj_id actor, int role) throws InterruptedException
    {
        if (formationHoldActive(self))
        {
            return;
        }
        if (getIntObjVar(self, OBJVAR_STATE) != squad_state.PATROLLING || getBooleanObjVar(self, OBJVAR_CASUALTY)
                || occupiedMask(self) != squad_state.ALL_MEMBERS || !allMembersCalm(self)
                || escortSlotDistance(self, actor, role) <= squad_member.FORMATION_ALERT_DISTANCE)
        {
            return;
        }
        obj_id leader = members(self)[squad_state.ROLE_LEADER];
        if (!memberMatchesRole(self, leader, squad_state.ROLE_LEADER))
        {
            return;
        }
        int token = utils.getIntScriptVar(self, SCRIPTVAR_FORMATION_HOLD_COUNTER) + 1;
        if (token <= 0)
        {
            token = 1;
        }
        utils.setScriptVar(self, SCRIPTVAR_FORMATION_HOLD_COUNTER, token);
        utils.setScriptVar(self, SCRIPTVAR_FORMATION_HOLD_TOKEN, token);
        utils.setScriptVar(self, SCRIPTVAR_FORMATION_HOLD_EPOCH, getIntObjVar(self, OBJVAR_EPOCH));
        utils.setScriptVar(self, SCRIPTVAR_FORMATION_HOLD_GENERATION, getIntObjVar(self, OBJVAR_GENERATION));
        utils.setScriptVar(self, SCRIPTVAR_FORMATION_HOLD_LEADER, leader);
        utils.removeScriptVar(self, SCRIPTVAR_FORMATION_HELD);
        utils.removeScriptVar(self, SCRIPTVAR_FORMATION_RESUMING);
        // The first valid escort report owns the single squad-wide callout.
        // Later reports still participate in recovery, but do not repeat speech.
        chat.chat(actor, squad_member.FORMATION_SPEECH);
        squad_monitor.event(self, "formationHold", "role=" + role + " gap=" + squad_monitor.tenth(escortSlotDistance(self, actor, role)));
        sendCommand(self, leader, squad_state.ROLE_LEADER, "formationHold");
    }

    public void tryFormationResume(obj_id self) throws InterruptedException
    {
        if (!formationHoldActive(self) || !utils.hasScriptVar(self, SCRIPTVAR_FORMATION_HELD)
                || utils.hasScriptVar(self, SCRIPTVAR_FORMATION_RESUMING)
                || getIntObjVar(self, OBJVAR_STATE) != squad_state.PATROLLING
                || getBooleanObjVar(self, OBJVAR_CASUALTY) || occupiedMask(self) != squad_state.ALL_MEMBERS
                || !allMembersCalm(self) || !allEscortsInFormation(self))
        {
            return;
        }
        obj_id leader = utils.getObjIdScriptVar(self, SCRIPTVAR_FORMATION_HOLD_LEADER);
        if (!memberMatchesRole(self, leader, squad_state.ROLE_LEADER))
        {
            clearFormationHold(self);
            return;
        }
        utils.setScriptVar(self, SCRIPTVAR_FORMATION_RESUMING, 1);
        squad_monitor.event(self, "formationResume", "");
        sendCommand(self, leader, squad_state.ROLE_LEADER, "formationResume");
    }

    public boolean allEscortsInFormation(obj_id self) throws InterruptedException
    {
        return squad_member.escortsWithinFormation(self, members(self)[squad_state.ROLE_LEADER]);
    }

    public static boolean currentFormationOrder(obj_id coordinator, obj_id leader, int token, int epoch, int generation) throws InterruptedException
    {
        return isIdValid(coordinator) && exists(coordinator) && token > 0 && epoch > 0 && generation > 0
                && getIntObjVar(coordinator, OBJVAR_STATE) == squad_state.PATROLLING
                && !getBooleanObjVar(coordinator, OBJVAR_CASUALTY)
                && getIntObjVar(coordinator, OBJVAR_EPOCH) == epoch && getIntObjVar(coordinator, OBJVAR_GENERATION) == generation
                && utils.getIntScriptVar(coordinator, SCRIPTVAR_FORMATION_HOLD_EPOCH) == epoch
                && utils.getIntScriptVar(coordinator, SCRIPTVAR_FORMATION_HOLD_GENERATION) == generation
                && utils.getIntScriptVar(coordinator, SCRIPTVAR_FORMATION_HOLD_TOKEN) == token
                && utils.getObjIdScriptVar(coordinator, SCRIPTVAR_FORMATION_HOLD_LEADER) == leader;
    }

    public float escortSlotDistance(obj_id self, obj_id escort, int role) throws InterruptedException
    {
        obj_id leader = members(self)[squad_state.ROLE_LEADER];
        if (!memberMatchesRole(self, leader, squad_state.ROLE_LEADER) || !memberMatchesRole(self, escort, role))
        {
            return Float.MAX_VALUE;
        }
        location offset = formationOffset(self, role);
        return squad_member.formationGap(leader, escort, offset.x, offset.z);
    }

    public boolean formationHoldActive(obj_id self) throws InterruptedException
    {
        return utils.getIntScriptVar(self, SCRIPTVAR_FORMATION_HOLD_TOKEN) > 0
                && utils.getIntScriptVar(self, SCRIPTVAR_FORMATION_HOLD_EPOCH) == getIntObjVar(self, OBJVAR_EPOCH)
                && utils.getIntScriptVar(self, SCRIPTVAR_FORMATION_HOLD_GENERATION) == getIntObjVar(self, OBJVAR_GENERATION)
                && isIdValid(utils.getObjIdScriptVar(self, SCRIPTVAR_FORMATION_HOLD_LEADER));
    }

    public void clearFormationHold(obj_id self) throws InterruptedException
    {
        utils.removeScriptVar(self, SCRIPTVAR_FORMATION_HOLD_TOKEN);
        utils.removeScriptVar(self, SCRIPTVAR_FORMATION_HOLD_EPOCH);
        utils.removeScriptVar(self, SCRIPTVAR_FORMATION_HOLD_GENERATION);
        utils.removeScriptVar(self, SCRIPTVAR_FORMATION_HOLD_LEADER);
        utils.removeScriptVar(self, SCRIPTVAR_FORMATION_HELD);
        utils.removeScriptVar(self, SCRIPTVAR_FORMATION_RESUMING);
    }

    public boolean living(obj_id actor) throws InterruptedException
    {
        return isIdValid(actor) && exists(actor) && !isPlayer(actor) && !isDead(actor) && !ai_lib.isIncapacitated(actor);
    }

    public float[] loadSections(String routeName) throws InterruptedException
    {
        if (!dataTableOpen(SECTION_TABLE))
        {
            return null;
        }
        int count = 0;
        int rows = dataTableGetNumRows(SECTION_TABLE);
        for (int row = 0; row < rows; row++)
        {
            if (routeName.equals(dataTableGetString(SECTION_TABLE, row, "route")))
            {
                count++;
            }
        }
        if (count < 1 || count > 64)
        {
            return null;
        }
        float[] sections = new float[count * 6];
        String[] columns = {"x1", "z1", "x2", "z2", "enterRadius", "exitRadius"};
        int index = 0;
        for (int row = 0; row < rows; row++)
        {
            if (!routeName.equals(dataTableGetString(SECTION_TABLE, row, "route")))
            {
                continue;
            }
            for (int column = 0; column < columns.length; column++)
            {
                float value = dataTableGetFloat(SECTION_TABLE, row, columns[column]);
                if (value != value || value < -8192.0f || value > 8192.0f)
                {
                    return null;
                }
                sections[index + column] = value;
            }
            if (sections[index + 4] <= 0.0f || sections[index + 5] <= sections[index + 4]
                    || sections[index + 5] > 64.0f)
            {
                return null;
            }
            index += 6;
        }
        return sections;
    }

    // Authored spatial capsules select formation, NOT the route or native cursor.
    // Exit uses a larger radius and all three actors so the tail cannot spread early.
    public boolean insideFormationSection(obj_id self, obj_id actor, boolean exiting) throws InterruptedException
    {
        location origin = getLocationObjVar(self, OBJVAR_ASSEMBLY);
        location position = living(actor) ? getLocation(actor) : null;
        if (!squad_member.sameSpace(origin, position))
        {
            return true; // Unknown/foreign-cell tail is not evidence it is clear.
        }
        float[] sections = utils.getFloatArrayScriptVar(self, SCRIPTVAR_SECTIONS);
        if (sections == null || sections.length == 0 || sections.length % 6 != 0)
        {
            return true;
        }
        float x = position.x - origin.x;
        float z = position.z - origin.z;
        for (int index = 0; index < sections.length; index += 6)
        {
            float dx = sections[index + 2] - sections[index];
            float dz = sections[index + 3] - sections[index + 1];
            float lengthSquared = dx * dx + dz * dz;
            float fraction = lengthSquared > 0.0f
                    ? ((x - sections[index]) * dx + (z - sections[index + 1]) * dz) / lengthSquared : 0.0f;
            fraction = Math.max(0.0f, Math.min(1.0f, fraction));
            float errorX = x - sections[index] - fraction * dx;
            float errorZ = z - sections[index + 1] - fraction * dz;
            float radius = sections[index + (exiting ? 5 : 4)];
            if (errorX * errorX + errorZ * errorZ <= radius * radius)
            {
                return true;
            }
        }
        return false;
    }

    public void advanceSectionRevision(obj_id self) throws InterruptedException
    {
        int revision = utils.getIntScriptVar(self, SCRIPTVAR_SECTION_REVISION) + 1;
        utils.setScriptVar(self, SCRIPTVAR_SECTION_REVISION, revision > 0 ? revision : 1);
    }

    public void refreshSectionFormation(obj_id self) throws InterruptedException
    {
        if (getIntObjVar(self, OBJVAR_STATE) != squad_state.PATROLLING
                || getBooleanObjVar(self, OBJVAR_CASUALTY) || occupiedMask(self) != squad_state.ALL_MEMBERS
                || !allMembersCalm(self) || utils.hasScriptVar(self, SCRIPTVAR_FORMATION_RESUMING))
        {
            return;
        }
        obj_id[] current = members(self);
        boolean wasFile = utils.getIntScriptVar(self, SCRIPTVAR_SECTION_FILE) != 0;
        boolean file = insideFormationSection(self, current[squad_state.ROLE_LEADER], wasFile);
        if (wasFile)
        {
            file = file || insideFormationSection(self, current[squad_state.ROLE_ESCORT_1], true)
                    || insideFormationSection(self, current[squad_state.ROLE_ESCORT_2], true);
        }
        if (file == wasFile)
        {
            return;
        }
        utils.setScriptVar(self, SCRIPTVAR_SECTION_FILE, file ? 1 : 0);
        squad_monitor.event(self, "formationMode", "mode=" + (file ? "file" : "open"));
        advanceSectionRevision(self);
        sendMovementForRole(self, squad_state.ROLE_ESCORT_1, false);
        sendMovementForRole(self, squad_state.ROLE_ESCORT_2, false);
        if (formationHoldActive(self) && !utils.hasScriptVar(self, SCRIPTVAR_FORMATION_HELD))
        {
            // Refresh an in-flight hold acknowledgement after the mode revision.
            sendCommand(self, current[squad_state.ROLE_LEADER], squad_state.ROLE_LEADER, "formationHold");
        }
    }

    public static location formationOffset(obj_id coordinator, int role) throws InterruptedException
    {
        location offset = new location();
        float rear = getFloatObjVar(coordinator, OBJVAR_REAR_DISTANCE);
        float lateral = getFloatObjVar(coordinator, OBJVAR_LATERAL_DISTANCE);
        if (utils.getIntScriptVar(coordinator, SCRIPTVAR_SECTION_FILE) != 0)
        {
            offset.x = 0.0f;
            offset.z = -rear - (role == squad_state.ROLE_ESCORT_2 ? 2.0f * lateral : 0.0f);
        }
        else
        {
            offset.x = role == squad_state.ROLE_ESCORT_1 ? -lateral : lateral;
            offset.z = -rear;
        }
        return offset;
    }

    public int roleForActor(obj_id self, obj_id actor) throws InterruptedException
    {
        int found = squad_state.ROLE_INVALID;
        for (int role = 0; role < squad_state.ROLE_COUNT; role++)
        {
            if (mounted_sandtrooper.isFromSpawner(actor, spawnerForRole(self, role)))
            {
                if (squad_state.validRole(found))
                {
                    return squad_state.ROLE_INVALID;
                }
                found = role;
            }
        }
        return found;
    }

    public int roleOfMember(obj_id self, obj_id actor) throws InterruptedException
    {
        obj_id[] current = members(self);
        for (int role = 0; role < squad_state.ROLE_COUNT; role++)
        {
            if (current[role] == actor)
            {
                return role;
            }
        }
        return squad_state.ROLE_INVALID;
    }

    public boolean memberMatchesRole(obj_id self, obj_id actor, int role) throws InterruptedException
    {
        return living(actor) && mounted_sandtrooper.isFromSpawner(actor, spawnerForRole(self, role));
    }

    public String spawnerForRole(obj_id self, int role) throws InterruptedException
    {
        if (role == squad_state.ROLE_LEADER)
        {
            return getStringObjVar(self, OBJVAR_LEADER_SPAWNER);
        }
        if (role == squad_state.ROLE_ESCORT_1)
        {
            return getStringObjVar(self, OBJVAR_ESCORT_1_SPAWNER);
        }
        if (role == squad_state.ROLE_ESCORT_2)
        {
            return getStringObjVar(self, OBJVAR_ESCORT_2_SPAWNER);
        }
        return "";
    }

    public obj_id[] members(obj_id self) throws InterruptedException
    {
        obj_id[] current = hasObjVar(self, OBJVAR_MEMBERS) ? getObjIdArrayObjVar(self, OBJVAR_MEMBERS) : null;
        if (current == null || current.length != squad_state.ROLE_COUNT)
        {
            current = new obj_id[squad_state.ROLE_COUNT];
            for (int role = 0; role < squad_state.ROLE_COUNT; role++)
            {
                current[role] = obj_id.NULL_ID;
            }
        }
        else
        {
            for (int role = 0; role < squad_state.ROLE_COUNT; role++)
            {
                if (current[role] == null)
                {
                    current[role] = obj_id.NULL_ID;
                }
            }
        }
        return current;
    }

    public int occupiedMask(obj_id self) throws InterruptedException
    {
        int occupied = 0;
        obj_id[] current = members(self);
        for (int role = 0; role < squad_state.ROLE_COUNT; role++)
        {
            if (memberMatchesRole(self, current[role], role))
            {
                occupied = squad_state.add(occupied, role);
            }
        }
        return occupied;
    }

    public int currentAssembledMask(obj_id self) throws InterruptedException
    {
        int assembled = 0;
        obj_id[] current = members(self);
        for (int role = 0; role < squad_state.ROLE_COUNT; role++)
        {
            if (memberMatchesRole(self, current[role], role) && isAssembled(self, current[role]))
            {
                assembled = squad_state.add(assembled, role);
            }
        }
        return assembled;
    }

    public boolean isAssembled(obj_id self, obj_id actor) throws InterruptedException
    {
        location assembly = getLocationObjVar(self, OBJVAR_ASSEMBLY);
        location current = living(actor) ? getLocation(actor) : null;
        return assembly != null && current != null && assembly.area.equals(current.area)
                && assembly.cell == current.cell && getDistance(current, assembly) <= getFloatObjVar(self, OBJVAR_ASSEMBLY_RADIUS);
    }

    public int mask(obj_id self, String key) throws InterruptedException
    {
        return hasObjVar(self, key) ? getIntObjVar(self, key) : 0;
    }

    public void setMask(obj_id self, String key, int value) throws InterruptedException
    {
        setObjVar(self, key, value & squad_state.ALL_MEMBERS);
    }

    public boolean departed(obj_id self) throws InterruptedException
    {
        int state = getIntObjVar(self, OBJVAR_STATE);
        if (state == squad_state.COMBAT && hasObjVar(self, OBJVAR_INTERRUPTED_STATE))
        {
            state = getIntObjVar(self, OBJVAR_INTERRUPTED_STATE);
        }
        return state == squad_state.DEPARTING || state == squad_state.PATROLLING
                || state == squad_state.COMBAT || state == squad_state.REGROUPING;
    }

    public void fail(obj_id self, String stage) throws InterruptedException
    {
        setObjVar(self, OBJVAR_FAILURE, stage);
    }

    public int OnUnloadedFromMemory(obj_id self) throws InterruptedException
    {
        clearFormationHold(self);
        utils.removeScriptVar(self, SCRIPTVAR_ROUTE);
        utils.removeScriptVar(self, SCRIPTVAR_SECTIONS);
        utils.removeScriptVar(self, SCRIPTVAR_SECTION_FILE);
        utils.removeScriptVar(self, SCRIPTVAR_SECTION_REVISION);
        utils.removeScriptVar(self, SCRIPTVAR_ACTIVE);
        return SCRIPT_CONTINUE;
    }
}
