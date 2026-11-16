package script.systems.npc_mount;

import script.*;
import script.library.ai_lib;
import script.library.utils;

// Actor-local adapter only.  It validates real spawn provenance, registers with
// the egg-hosted coordinator, and is the squad's sole patrol/follow/path command
// recipient.  It does not replace ai.creature_combat or own actor spawning.
public class squad_member extends script.base_script
{
    public static final String TABLE = "datatables/npc_mount/squads.iff";

    public static final String PREFIX = "npcMountSquad.";
    public static final String OBJVAR_PROFILE = PREFIX + "profile";
    public static final String OBJVAR_COORDINATOR = PREFIX + "coordinator";
    public static final String OBJVAR_SPAWNER = PREFIX + "spawner";
    public static final String OBJVAR_ROLE = PREFIX + "role";
    public static final String OBJVAR_EPOCH = PREFIX + "epoch";
    public static final String OBJVAR_GENERATION = PREFIX + "generation";
    public static final String OBJVAR_PERMISSION = PREFIX + "patrolPermission";
    public static final String OBJVAR_MOUNT_READY = PREFIX + "mountedReady";
    public static final String OBJVAR_PATROL_EPOCH = PREFIX + "patrolEpoch";
    public static final String OBJVAR_PATROL_GENERATION = PREFIX + "patrolGeneration";
    public static final String OBJVAR_FOLLOW_TARGET = PREFIX + "followTarget";
    public static final String OBJVAR_FOLLOW_X = PREFIX + "followX";
    public static final String OBJVAR_FOLLOW_Z = PREFIX + "followZ";
    public static final String OBJVAR_FAILURE = PREFIX + "memberFailure";
    public static final String OBJVAR_DISPLAY_NAME = PREFIX + "displayName";

    public static final String SCRIPTVAR_HANDSHAKE = PREFIX + "handshake";
    public static final String SCRIPTVAR_ATTEMPTS = PREFIX + "handshakeAttempts";
    public static final String SCRIPTVAR_ACK = PREFIX + "handshakeAck";
    public static final String SCRIPTVAR_MOVE_KIND = PREFIX + "moveKind";
    public static final String SCRIPTVAR_MOVE_EPOCH = PREFIX + "moveEpoch";
    public static final String SCRIPTVAR_MOVE_GENERATION = PREFIX + "moveGeneration";
    public static final String SCRIPTVAR_MOVE_TARGET = PREFIX + "moveTarget";
    public static final String SCRIPTVAR_FORMATION_SEQUENCE = PREFIX + "formationSequence";
    public static final String SCRIPTVAR_FORMATION_ACTIVE_SEQUENCE = PREFIX + "formationActiveSequence";
    public static final String SCRIPTVAR_FORMATION_EPOCH = PREFIX + "formationEpoch";
    public static final String SCRIPTVAR_FORMATION_GENERATION = PREFIX + "formationGeneration";
    public static final String SCRIPTVAR_FORMATION_LEADER = PREFIX + "formationLeader";
    public static final String SCRIPTVAR_FORMATION_FAR_SINCE = PREFIX + "formationFarSince";
    public static final String SCRIPTVAR_FORMATION_ALERTED = PREFIX + "formationAlerted";
    public static final String SCRIPTVAR_FORMATION_IN_SLOT = PREFIX + "formationInSlot";
    public static final String SCRIPTVAR_FORMATION_EPISODE = PREFIX + "formationEpisode";
    public static final String SCRIPTVAR_FORMATION_HOLD_EPOCH = PREFIX + "formationHoldEpoch";
    public static final String SCRIPTVAR_FORMATION_HOLD_GENERATION = PREFIX + "formationHoldGeneration";
    public static final String SCRIPTVAR_FORMATION_HOLD_TOKEN = PREFIX + "formationHoldToken";
    public static final String SCRIPTVAR_FORMATION_LAST_HOLD_TOKEN = PREFIX + "formationLastHoldToken";
    public static final String SCRIPTVAR_SECTION_REVISION = PREFIX + "sectionRevision";

    public static final int MAX_HANDSHAKE_ATTEMPTS = 8;
    public static final float HANDSHAKE_DELAY = 1.0f;
    public static final float FORMATION_CHECK_DELAY = 1.0f;
    public static final float FORMATION_ALERT_DISTANCE = 3.0f;
    // Single file walks the leader's actual trail (user, 2026-11-16) instead of a slot fixed
    // to the leader's current heading, which swings through walls on every turn.
    public static final String SCRIPTVAR_TRAIL = PREFIX + "trail";
    public static final String SCRIPTVAR_TRAIL_OFFSET = PREFIX + "trailOffset";
    public static final int TRAIL_POINTS = 48;
    public static final float TRAIL_SPACING = 1.0f;
    public static final float TRAIL_RESET_JUMP = 15.0f;
    public static final float TRAIL_RESTEER = 0.75f;
    public static final float FORMATION_RECOVER_DISTANCE = 1.5f;
    public static final int FORMATION_ALERT_SECONDS = 2;
    public static final String FORMATION_SPEECH = "Out of formation, regroup";

    public squad_member()
    {
    }

    public int OnAttach(obj_id self) throws InterruptedException
    {
        configureAndRegister(self);
        return SCRIPT_CONTINUE;
    }

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        configureAndRegister(self);
        return SCRIPT_CONTINUE;
    }

    public static boolean managed(obj_id actor) throws InterruptedException
    {
        if (!isIdValid(actor) || !exists(actor) || isPlayer(actor)
                || !hasObjVar(actor, OBJVAR_PROFILE) || !hasObjVar(actor, OBJVAR_COORDINATOR)
                || !hasObjVar(actor, OBJVAR_SPAWNER) || !hasObjVar(actor, OBJVAR_ROLE))
        {
            return false;
        }
        int role = getIntObjVar(actor, OBJVAR_ROLE);
        String spawner = getStringObjVar(actor, OBJVAR_SPAWNER);
        return squad_state.validRole(role) && mounted_sandtrooper.isFromSpawner(actor, spawner);
    }

    public static boolean patrolAllowed(obj_id actor) throws InterruptedException
    {
        if (!managed(actor))
        {
            return true;
        }
        int generation = getIntObjVar(actor, OBJVAR_GENERATION);
        return generation > 0 && getIntObjVar(actor, OBJVAR_PERMISSION) == generation;
    }

    public static void signal(obj_id actor, String event) throws InterruptedException
    {
        if (event == null || event.length() == 0 || !managed(actor))
        {
            return;
        }
        if ("mountedReady".equals(event))
        {
            setObjVar(actor, OBJVAR_MOUNT_READY, 1);
        }
        else if ("terminalRetire".equals(event) || "lostPair".equals(event))
        {
            removeObjVar(actor, OBJVAR_MOUNT_READY);
        }
        notifyCoordinator(actor, event);
    }

    public void configureAndRegister(obj_id self) throws InterruptedException
    {
        if (!eligible(self) || !dataTableOpen(TABLE))
        {
            return;
        }
        int selected = -1;
        int selectedRole = squad_state.ROLE_INVALID;
        int rows = dataTableGetNumRows(TABLE);
        for (int row = 0; row < rows; row++)
        {
            int role = roleFromRow(self, row);
            if (squad_state.validRole(role))
            {
                if (selected != -1)
                {
                    setObjVar(self, OBJVAR_FAILURE, "configurationAmbiguous");
                    return;
                }
                selected = row;
                selectedRole = role;
            }
        }
        if (selected < 0 || !validProfileRow(self, selected))
        {
            setObjVar(self, OBJVAR_FAILURE, selected < 0 ? "configurationMissing" : "configurationInvalid");
            return;
        }

        String profile = dataTableGetString(TABLE, selected, "name");
        String coordinatorSpawner = squad_binding.spawner(self, TABLE, selected, "coordinatorSpawner");
        String actorSpawner = spawnerForRole(self, selected, selectedRole);
        obj_id coordinator = parseSpawner(coordinatorSpawner);
        if (!isIdValid(coordinator) || !mounted_sandtrooper.isFromSpawner(self, actorSpawner)
                || !matches(self, OBJVAR_PROFILE, profile) || !matches(self, OBJVAR_COORDINATOR, coordinator)
                || !matches(self, OBJVAR_SPAWNER, actorSpawner)
                || (hasObjVar(self, OBJVAR_ROLE) && getIntObjVar(self, OBJVAR_ROLE) != selectedRole))
        {
            setObjVar(self, OBJVAR_FAILURE, "configurationConflict");
            return;
        }
        setObjVar(self, OBJVAR_PROFILE, profile);
        setObjVar(self, OBJVAR_COORDINATOR, coordinator);
        setObjVar(self, OBJVAR_SPAWNER, actorSpawner);
        setObjVar(self, OBJVAR_ROLE, selectedRole);
        if (selectedRole == squad_state.ROLE_LEADER && hasObjVar(self, OBJVAR_DISPLAY_NAME))
        {
            String displayName = getStringObjVar(self, OBJVAR_DISPLAY_NAME);
            if (nonempty(displayName))
            {
                setName(self, displayName);
            }
        }
        removeObjVar(self, OBJVAR_FAILURE);
        setHibernationDelay(self, squad_monitor.HIBERNATION_DELAY);
        startHandshake(self);
    }

    public boolean eligible(obj_id actor) throws InterruptedException
    {
        return isIdValid(actor) && exists(actor) && !isPlayer(actor) && !isDead(actor)
                && !ai_lib.isIncapacitated(actor);
    }

    public boolean matches(obj_id actor, String key, String value) throws InterruptedException
    {
        return !hasObjVar(actor, key) || value.equals(getStringObjVar(actor, key));
    }

    public boolean matches(obj_id actor, String key, obj_id value) throws InterruptedException
    {
        return !hasObjVar(actor, key) || value == getObjIdObjVar(actor, key);
    }

    public int roleFromRow(obj_id actor, int row) throws InterruptedException
    {
        int role = squad_state.ROLE_INVALID;
        for (int candidate = 0; candidate < squad_state.ROLE_COUNT; candidate++)
        {
            if (mounted_sandtrooper.isFromSpawner(actor, spawnerForRole(actor, row, candidate)))
            {
                if (squad_state.validRole(role))
                {
                    return squad_state.ROLE_INVALID;
                }
                role = candidate;
            }
        }
        return role;
    }

    public String spawnerForRole(obj_id actor, int row, int role) throws InterruptedException
    {
        if (role == squad_state.ROLE_LEADER)
        {
            return squad_binding.spawner(actor, TABLE, row, "leaderSpawner");
        }
        if (role == squad_state.ROLE_ESCORT_1)
        {
            return squad_binding.spawner(actor, TABLE, row, "escort1Spawner");
        }
        if (role == squad_state.ROLE_ESCORT_2)
        {
            return squad_binding.spawner(actor, TABLE, row, "escort2Spawner");
        }
        return "";
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
                && nonempty(escort2) && nonempty(route) && leader.equals(coordinator)
                && !leader.equals(escort1) && !leader.equals(escort2) && !escort1.equals(escort2)
                && isIdValid(parseSpawner(coordinator)) && isIdValid(parseSpawner(leader))
                && isIdValid(parseSpawner(escort1)) && isIdValid(parseSpawner(escort2))
                && radius > 0.0f && radius <= 64.0f && rear > 0.0f && rear <= 64.0f
                && lateral >= 0.0f && lateral <= 64.0f;
    }

    public boolean nonempty(String value)
    {
        return value != null && value.length() > 0;
    }

    public static obj_id parseSpawner(String value)
    {
        if (value == null || value.length() == 0)
        {
            return null;
        }
        try
        {
            return obj_id.getObjId(Long.parseLong(value));
        }
        catch (NumberFormatException ignored)
        {
            return null;
        }
    }

    public void startHandshake(obj_id actor) throws InterruptedException
    {
        int sequence = utils.getIntScriptVar(actor, SCRIPTVAR_HANDSHAKE) + 1;
        utils.setScriptVar(actor, SCRIPTVAR_HANDSHAKE, sequence);
        utils.setScriptVar(actor, SCRIPTVAR_ATTEMPTS, 1);
        utils.removeScriptVar(actor, SCRIPTVAR_ACK);
        notifyCoordinator(actor, "register");
        dictionary retry = new dictionary();
        retry.put("sequence", sequence);
        messageTo(actor, "squadRegistrationRetry", retry, HANDSHAKE_DELAY, false);
    }

    public int squadRegistrationRetry(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || !managed(self) || utils.hasScriptVar(self, SCRIPTVAR_ACK)
                || params.getInt("sequence") != utils.getIntScriptVar(self, SCRIPTVAR_HANDSHAKE))
        {
            return SCRIPT_CONTINUE;
        }
        int attempts = utils.getIntScriptVar(self, SCRIPTVAR_ATTEMPTS);
        if (attempts >= MAX_HANDSHAKE_ATTEMPTS)
        {
            setObjVar(self, OBJVAR_FAILURE, "registrationTimeout");
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, SCRIPTVAR_ATTEMPTS, attempts + 1);
        notifyCoordinator(self, "register");
        messageTo(self, "squadRegistrationRetry", params, HANDSHAKE_DELAY, false);
        return SCRIPT_CONTINUE;
    }

    public static void notifyCoordinator(obj_id actor, String event) throws InterruptedException
    {
        if (!isIdValid(actor) || !hasObjVar(actor, OBJVAR_COORDINATOR))
        {
            return;
        }
        obj_id coordinator = getObjIdObjVar(actor, OBJVAR_COORDINATOR);
        if (!isIdValid(coordinator))
        {
            return;
        }
        dictionary notice = new dictionary();
        notice.put("actor", actor);
        notice.put("event", event);
        notice.put("role", hasObjVar(actor, OBJVAR_ROLE) ? getIntObjVar(actor, OBJVAR_ROLE) : squad_state.ROLE_INVALID);
        notice.put("epoch", hasObjVar(actor, OBJVAR_EPOCH) ? getIntObjVar(actor, OBJVAR_EPOCH) : 0);
        notice.put("generation", hasObjVar(actor, OBJVAR_GENERATION) ? getIntObjVar(actor, OBJVAR_GENERATION) : 0);
        messageTo(coordinator, "squadEvent", notice, 0.0f, false);
    }

    public int squadCommand(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || !managed(self) || params.getObjId("coordinator") != getObjIdObjVar(self, OBJVAR_COORDINATOR))
        {
            return SCRIPT_CONTINUE;
        }
        String command = params.getString("command");
        int epoch = params.getInt("epoch");
        int generation = params.getInt("generation");
        if ("bound".equals(command))
        {
            if (epoch <= 0 || generation <= 0)
            {
                return SCRIPT_CONTINUE;
            }
            int currentEpoch = getIntObjVar(self, OBJVAR_EPOCH);
            int currentGeneration = getIntObjVar(self, OBJVAR_GENERATION);
            if (epoch < currentEpoch || (epoch == currentEpoch && generation < currentGeneration))
            {
                return SCRIPT_CONTINUE;
            }
            boolean changed = epoch != currentEpoch || generation != currentGeneration;
            boolean unacknowledged = !utils.hasScriptVar(self, SCRIPTVAR_ACK);
            setObjVar(self, OBJVAR_EPOCH, epoch);
            setObjVar(self, OBJVAR_GENERATION, generation);
            if (changed)
            {
                removeObjVar(self, OBJVAR_PERMISSION);
                clearMoveCommand(self);
                stopFormationChecks(self);
                clearFormationHold(self);
                utils.removeScriptVar(self, SCRIPTVAR_FORMATION_LAST_HOLD_TOKEN);
                utils.removeScriptVar(self, SCRIPTVAR_SECTION_REVISION);
            }
            utils.setScriptVar(self, SCRIPTVAR_ACK, 1);
            removeObjVar(self, OBJVAR_FAILURE);
            if (hasObjVar(self, OBJVAR_MOUNT_READY) && (changed || unacknowledged))
            {
                notifyCoordinator(self, "mountedReady");
            }
            return SCRIPT_CONTINUE;
        }
        if (epoch != getIntObjVar(self, OBJVAR_EPOCH) || generation != getIntObjVar(self, OBJVAR_GENERATION))
        {
            return SCRIPT_CONTINUE;
        }
        if ("combat".equals(command))
        {
            removeObjVar(self, OBJVAR_PERMISSION);
            stopFormationChecks(self);
            clearFormationHold(self);
            return SCRIPT_CONTINUE;
        }
        if ("formationHold".equals(command))
        {
            holdFormationPatrol(self, params, epoch, generation);
            return SCRIPT_CONTINUE;
        }
        if ("formationResume".equals(command))
        {
            resumeFormationPatrol(self, params, epoch, generation);
            return SCRIPT_CONTINUE;
        }
        if ("hold".equals(command))
        {
            removeObjVar(self, OBJVAR_PERMISSION);
            stopFormationChecks(self);
            clearFormationHold(self);
            clearMoveCommand(self);
            if (!ai_lib.isInCombat(self) && !aiIsTethered(self))
            {
                stop(self);
            }
            return SCRIPT_CONTINUE;
        }
        if ("depart".equals(command))
        {
            stopFormationChecks(self);
            clearFormationHold(self);
            beginPath(self, params.getLocation("target"), "depart", epoch, generation);
            return SCRIPT_CONTINUE;
        }
        if ("return".equals(command))
        {
            stopFormationChecks(self);
            clearFormationHold(self);
            beginPath(self, params.getLocation("target"), "return", epoch, generation);
            return SCRIPT_CONTINUE;
        }
        if ("patrol".equals(command) || "resume".equals(command))
        {
            if (getIntObjVar(self, OBJVAR_ROLE) == squad_state.ROLE_LEADER)
            {
                clearFormationHold(self);
                restorePatrol(self, params.getLocationArray("route"), epoch, generation);
            }
            return SCRIPT_CONTINUE;
        }
        if ("follow".equals(command))
        {
            obj_id coordinator = getObjIdObjVar(self, OBJVAR_COORDINATOR);
            int state = getIntObjVar(coordinator, squad_coordinator.OBJVAR_STATE);
            int revision = params.getInt("sectionRevision");
            if ((state != squad_state.DEPARTING && state != squad_state.PATROLLING)
                    || getBooleanObjVar(coordinator, squad_coordinator.OBJVAR_CASUALTY)
                    || revision != utils.getIntScriptVar(coordinator, squad_coordinator.SCRIPTVAR_SECTION_REVISION))
            {
                return SCRIPT_CONTINUE;
            }
            if (revision != utils.getIntScriptVar(self, SCRIPTVAR_SECTION_REVISION))
            {
                // Keep the one scheduled check chain. Mode revisions reject old
                // reports; only lag/readiness sampled for the old slot is reset.
                utils.removeScriptVar(self, SCRIPTVAR_FORMATION_FAR_SINCE);
                utils.removeScriptVar(self, SCRIPTVAR_FORMATION_ALERTED);
                utils.removeScriptVar(self, SCRIPTVAR_FORMATION_IN_SLOT);
                utils.setScriptVar(self, SCRIPTVAR_SECTION_REVISION, revision);
            }
            restoreFollow(self, params.getObjId("leader"), params.getFloat("offsetX"), params.getFloat("offsetZ"), epoch, generation);
            if (params.getInt("monitorFormation") != 0)
            {
                startFormationChecks(self, params.getObjId("leader"), epoch, generation);
            }
            else
            {
                stopFormationChecks(self);
            }
        }
        return SCRIPT_CONTINUE;
    }

    public void holdFormationPatrol(obj_id actor, dictionary params, int epoch, int generation) throws InterruptedException
    {
        int token = params.getInt("formationHoldToken");
        if (getIntObjVar(actor, OBJVAR_ROLE) != squad_state.ROLE_LEADER || token <= 0
                || ai_lib.isInCombat(actor) || aiIsTethered(actor)
                || !squad_coordinator.currentFormationOrder(getObjIdObjVar(actor, OBJVAR_COORDINATOR), actor, token, epoch, generation))
        {
            return;
        }
        if (formationHoldMatches(actor, epoch, generation, token))
        {
            if (hasSuspendedMovement(actor))
            {
                notifyFormationCoordinator(actor, "formationHeld", 0, 0, token);
            }
            else
            {
                clearFormationHold(actor);
                notifyFormationCoordinator(actor, "formationHoldFailed", 0, 0, token);
            }
            return;
        }
        if (token <= utils.getIntScriptVar(actor, SCRIPTVAR_FORMATION_LAST_HOLD_TOKEN))
        {
            return;
        }
        utils.setScriptVar(actor, SCRIPTVAR_FORMATION_LAST_HOLD_TOKEN, token);
        if (hasSuspendedMovement(actor) || aiGetMovementState(actor) != MOVEMENT_PATROL
                || !suspendMovement(actor))
        {
            notifyFormationCoordinator(actor, "formationHoldFailed", 0, 0, token);
            return;
        }
        utils.setScriptVar(actor, SCRIPTVAR_FORMATION_HOLD_EPOCH, epoch);
        utils.setScriptVar(actor, SCRIPTVAR_FORMATION_HOLD_GENERATION, generation);
        utils.setScriptVar(actor, SCRIPTVAR_FORMATION_HOLD_TOKEN, token);
        notifyFormationCoordinator(actor, "formationHeld", 0, 0, token);
    }

    public void resumeFormationPatrol(obj_id actor, dictionary params, int epoch, int generation) throws InterruptedException
    {
        int token = params.getInt("formationHoldToken");
        if (getIntObjVar(actor, OBJVAR_ROLE) != squad_state.ROLE_LEADER
                || params.getInt("sectionRevision") != utils.getIntScriptVar(getObjIdObjVar(actor, OBJVAR_COORDINATOR), squad_coordinator.SCRIPTVAR_SECTION_REVISION)
                || !formationHoldMatches(actor, epoch, generation, token)
                || ai_lib.isInCombat(actor) || aiIsTethered(actor) || !hasSuspendedMovement(actor)
                || !squad_coordinator.currentFormationOrder(getObjIdObjVar(actor, OBJVAR_COORDINATOR), actor, token, epoch, generation))
        {
            return;
        }
        if (!escortsWithinFormation(getObjIdObjVar(actor, OBJVAR_COORDINATOR), actor))
        {
            notifyFormationCoordinator(actor, "formationResumeDeferred", 0, 0, token);
            return;
        }
        if (resumeMovement(actor))
        {
            clearFormationHold(actor);
            notifyFormationCoordinator(actor, "formationResumed", 0, 0, token);
        }
    }

    public boolean formationHoldMatches(obj_id actor, int epoch, int generation, int token) throws InterruptedException
    {
        return token > 0 && utils.getIntScriptVar(actor, SCRIPTVAR_FORMATION_HOLD_EPOCH) == epoch
                && utils.getIntScriptVar(actor, SCRIPTVAR_FORMATION_HOLD_GENERATION) == generation
                && utils.getIntScriptVar(actor, SCRIPTVAR_FORMATION_HOLD_TOKEN) == token;
    }

    public void clearFormationHold(obj_id actor) throws InterruptedException
    {
        utils.removeScriptVar(actor, SCRIPTVAR_FORMATION_HOLD_EPOCH);
        utils.removeScriptVar(actor, SCRIPTVAR_FORMATION_HOLD_GENERATION);
        utils.removeScriptVar(actor, SCRIPTVAR_FORMATION_HOLD_TOKEN);
    }

    public void beginPath(obj_id actor, location target, String kind, int epoch, int generation) throws InterruptedException
    {
        if (target == null || ai_lib.isInCombat(actor) || aiIsTethered(actor))
        {
            return;
        }
        boolean same = moveCommandMatches(actor, kind, epoch, generation, target);
        setObjVar(actor, OBJVAR_PERMISSION, generation);
        setMovementWalk(actor);
        if (same)
        {
            if (getDistance(actor, target) <= 1.5f)
            {
                clearMoveCommand(actor);
                removeObjVar(actor, OBJVAR_PERMISSION);
                notifyCoordinator(actor, "depart".equals(kind) ? "departed" : "assemblyArrived");
                return;
            }
            if (hasSuspendedMovement(actor))
            {
                resumeMovement(actor);
                return;
            }
            if (aiGetMovementState(actor) != MOVEMENT_IDLE)
            {
                return;
            }
        }
        if (!same && hasSuspendedMovement(actor))
        {
            resumeMovement(actor);
        }
        if ("return".equals(kind))
        {
            if (!same)
            {
                stop(actor);
                ai_lib.clearPatrolPath(actor);
            }
            if (getDistance(actor, target) <= 1.5f)
            {
                clearMoveCommand(actor);
                removeObjVar(actor, OBJVAR_PERMISSION);
                notifyCoordinator(actor, "assemblyArrived");
                return;
            }
        }
        else
        {
            stop(actor);
        }
        utils.setScriptVar(actor, SCRIPTVAR_MOVE_KIND, kind);
        utils.setScriptVar(actor, SCRIPTVAR_MOVE_EPOCH, epoch);
        utils.setScriptVar(actor, SCRIPTVAR_MOVE_GENERATION, generation);
        utils.setScriptVar(actor, SCRIPTVAR_MOVE_TARGET, target);
        pathTo(actor, target);
    }

    public void restorePatrol(obj_id actor, location[] route, int epoch, int generation) throws InterruptedException
    {
        if (route == null || route.length < 3 || ai_lib.isInCombat(actor) || aiIsTethered(actor))
        {
            return;
        }
        setObjVar(actor, OBJVAR_PERMISSION, generation);
        setMovementWalk(actor);
        if (aiGetMovementState(actor) == MOVEMENT_PATROL)
        {
            return;
        }
        if (hasSuspendedMovement(actor))
        {
            ai_lib.resumePatrol(actor);
            return;
        }
        if (getIntObjVar(actor, OBJVAR_PATROL_EPOCH) == epoch
                && getIntObjVar(actor, OBJVAR_PATROL_GENERATION) == generation)
        {
            // There is no script API for a native current-waypoint index.  Never
            // reset a same-load route to point zero merely because another calm
            // callback stopped movement.
            commandFailure(actor, "patrolInterrupted");
            return;
        }
        stop(actor);
        ai_lib.setPatrolPath(actor, route, 0);
        setObjVar(actor, OBJVAR_PATROL_EPOCH, epoch);
        setObjVar(actor, OBJVAR_PATROL_GENERATION, generation);
    }

    public void restoreFollow(obj_id actor, obj_id leader, float offsetX, float offsetZ, int epoch, int generation) throws InterruptedException
    {
        if (!isIdValid(leader) || !exists(leader) || isDead(leader) || ai_lib.isIncapacitated(leader)
                || ai_lib.isInCombat(actor) || aiIsTethered(actor) || ai_lib.isInCombat(leader) || aiIsTethered(leader))
        {
            return;
        }
        boolean same = getObjIdObjVar(actor, OBJVAR_FOLLOW_TARGET) == leader
                && getIntObjVar(actor, OBJVAR_EPOCH) == epoch
                && getIntObjVar(actor, OBJVAR_GENERATION) == generation
                && getFloatObjVar(actor, OBJVAR_FOLLOW_X) == offsetX
                && getFloatObjVar(actor, OBJVAR_FOLLOW_Z) == offsetZ;
        setObjVar(actor, OBJVAR_PERMISSION, generation);
        setMovementWalk(actor);
        if (same && hasSuspendedMovement(actor))
        {
            resumeMovement(actor);
            return;
        }
        if (same && aiGetMovementState(actor) == MOVEMENT_FOLLOW)
        {
            return;
        }
        location offset = new location();
        offset.x = offsetX;
        offset.z = offsetZ;
        if (!follow(actor, leader, offset))
        {
            removeObjVar(actor, OBJVAR_PERMISSION);
            commandFailure(actor, "followRejected");
            return;
        }
        setObjVar(actor, OBJVAR_FOLLOW_TARGET, leader);
        setObjVar(actor, OBJVAR_FOLLOW_X, offsetX);
        setObjVar(actor, OBJVAR_FOLLOW_Z, offsetZ);
        utils.removeScriptVar(actor, SCRIPTVAR_TRAIL_OFFSET); // a fresh nominal follow; re-aim from it
    }

    public void startFormationChecks(obj_id actor, obj_id leader, int epoch, int generation) throws InterruptedException
    {
        int role = getIntObjVar(actor, OBJVAR_ROLE);
        if ((role != squad_state.ROLE_ESCORT_1 && role != squad_state.ROLE_ESCORT_2)
                || epoch <= 0 || generation <= 0 || !isIdValid(leader) || !exists(leader)
                || getIntObjVar(actor, OBJVAR_PERMISSION) != generation)
        {
            stopFormationChecks(actor);
            return;
        }
        if (utils.hasScriptVar(actor, SCRIPTVAR_FORMATION_ACTIVE_SEQUENCE)
                && utils.getIntScriptVar(actor, SCRIPTVAR_FORMATION_EPOCH) == epoch
                && utils.getIntScriptVar(actor, SCRIPTVAR_FORMATION_GENERATION) == generation
                && utils.getObjIdScriptVar(actor, SCRIPTVAR_FORMATION_LEADER) == leader)
        {
            return;
        }
        stopFormationChecks(actor);
        int sequence = utils.getIntScriptVar(actor, SCRIPTVAR_FORMATION_SEQUENCE) + 1;
        if (sequence <= 0)
        {
            sequence = 1;
        }
        utils.setScriptVar(actor, SCRIPTVAR_FORMATION_SEQUENCE, sequence);
        utils.setScriptVar(actor, SCRIPTVAR_FORMATION_ACTIVE_SEQUENCE, sequence);
        utils.setScriptVar(actor, SCRIPTVAR_FORMATION_EPOCH, epoch);
        utils.setScriptVar(actor, SCRIPTVAR_FORMATION_GENERATION, generation);
        utils.setScriptVar(actor, SCRIPTVAR_FORMATION_LEADER, leader);
        scheduleFormationCheck(actor, sequence, epoch, generation);
    }

    public void scheduleFormationCheck(obj_id actor, int sequence, int epoch, int generation) throws InterruptedException
    {
        dictionary check = new dictionary();
        check.put("sequence", sequence);
        check.put("epoch", epoch);
        check.put("generation", generation);
        messageTo(actor, "squadFormationCheck", check, FORMATION_CHECK_DELAY, false);
    }

    public int squadFormationCheck(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || !currentFormationCheck(self, params))
        {
            return SCRIPT_CONTINUE;
        }
        int sequence = params.getInt("sequence");
        int epoch = params.getInt("epoch");
        int generation = params.getInt("generation");
        obj_id leader = utils.getObjIdScriptVar(self, SCRIPTVAR_FORMATION_LEADER);
        if (!formationCheckEligible(self, leader, generation))
        {
            stopFormationChecks(self);
            return SCRIPT_CONTINUE;
        }
        // Reuse this bounded check chain; the coordinator alone selects the mode.
        notifyFormationCoordinator(self, "formationSectionCheck", sequence,
                utils.getIntScriptVar(self, SCRIPTVAR_FORMATION_EPISODE), 0);
        float offsetX = getFloatObjVar(self, OBJVAR_FOLLOW_X);
        float offsetZ = getFloatObjVar(self, OBJVAR_FOLLOW_Z);
        if (offsetX == 0.0f)
        {
            // Single file: steer along the leader's trail; the gap rule below is shared
            // with the coordinator so neither side waits on a point the other ignores.
            location along = trailTarget(self, leader, -offsetZ);
            if (along != null)
            {
                steerToTrail(self, leader, along);
            }
        }
        else
        {
            utils.removeScriptVar(self, SCRIPTVAR_TRAIL);
            utils.removeScriptVar(self, SCRIPTVAR_TRAIL_OFFSET);
        }
        float distance = formationGap(leader, self, offsetX, offsetZ);
        boolean inSlot = distance <= FORMATION_RECOVER_DISTANCE;
        boolean wasInSlot = utils.getIntScriptVar(self, SCRIPTVAR_FORMATION_IN_SLOT) != 0;
        boolean wasAlerted = utils.hasScriptVar(self, SCRIPTVAR_FORMATION_ALERTED);
        utils.setScriptVar(self, SCRIPTVAR_FORMATION_IN_SLOT, inSlot ? 1 : 0);
        if (inSlot)
        {
            int episode = utils.getIntScriptVar(self, SCRIPTVAR_FORMATION_EPISODE);
            if (wasAlerted)
            {
                episode++;
                if (episode <= 0)
                {
                    episode = 1;
                }
                utils.setScriptVar(self, SCRIPTVAR_FORMATION_EPISODE, episode);
            }
            utils.removeScriptVar(self, SCRIPTVAR_FORMATION_FAR_SINCE);
            utils.removeScriptVar(self, SCRIPTVAR_FORMATION_ALERTED);
            if (!wasInSlot || utils.hasScriptVar(leader, SCRIPTVAR_FORMATION_HOLD_TOKEN))
            {
                notifyFormationCoordinator(self, "formationRecovered", sequence, episode, 0);
            }
        }
        else if (distance > FORMATION_ALERT_DISTANCE)
        {
            int now = getGameTime();
            if (!utils.hasScriptVar(self, SCRIPTVAR_FORMATION_FAR_SINCE))
            {
                utils.setScriptVar(self, SCRIPTVAR_FORMATION_FAR_SINCE, now);
            }
            else if (!utils.hasScriptVar(self, SCRIPTVAR_FORMATION_ALERTED)
                    && now - utils.getIntScriptVar(self, SCRIPTVAR_FORMATION_FAR_SINCE) >= FORMATION_ALERT_SECONDS)
            {
                int episode = utils.getIntScriptVar(self, SCRIPTVAR_FORMATION_EPISODE) + 1;
                if (episode <= 0)
                {
                    episode = 1;
                }
                utils.setScriptVar(self, SCRIPTVAR_FORMATION_EPISODE, episode);
                utils.setScriptVar(self, SCRIPTVAR_FORMATION_ALERTED, 1);
                notifyFormationCoordinator(self, "formationTooFar", sequence, episode, 0);
            }
        }
        else if (!utils.hasScriptVar(self, SCRIPTVAR_FORMATION_ALERTED))
        {
            utils.removeScriptVar(self, SCRIPTVAR_FORMATION_FAR_SINCE);
        }
        if (currentFormationCheck(self, params))
        {
            scheduleFormationCheck(self, sequence, epoch, generation);
        }
        return SCRIPT_CONTINUE;
    }

    public boolean currentFormationCheck(obj_id actor, dictionary params) throws InterruptedException
    {
        int epoch = params.getInt("epoch");
        int generation = params.getInt("generation");
        return epoch > 0 && generation > 0 && params.getInt("sequence") > 0
                && params.getInt("sequence") == utils.getIntScriptVar(actor, SCRIPTVAR_FORMATION_ACTIVE_SEQUENCE)
                && epoch == getIntObjVar(actor, OBJVAR_EPOCH) && generation == getIntObjVar(actor, OBJVAR_GENERATION)
                && epoch == utils.getIntScriptVar(actor, SCRIPTVAR_FORMATION_EPOCH)
                && generation == utils.getIntScriptVar(actor, SCRIPTVAR_FORMATION_GENERATION);
    }

    public boolean formationCheckEligible(obj_id actor, obj_id leader, int generation) throws InterruptedException
    {
        int role = getIntObjVar(actor, OBJVAR_ROLE);
        return managed(actor) && (role == squad_state.ROLE_ESCORT_1 || role == squad_state.ROLE_ESCORT_2)
                && livingForFormation(actor) && livingForFormation(leader) && !ai_lib.isInCombat(actor)
                && !aiIsTethered(actor) && getIntObjVar(actor, OBJVAR_PERMISSION) == generation
                && getObjIdObjVar(actor, OBJVAR_FOLLOW_TARGET) == leader;
    }

    public boolean livingForFormation(obj_id actor) throws InterruptedException
    {
        return isIdValid(actor) && exists(actor) && !isDead(actor) && !ai_lib.isIncapacitated(actor);
    }

    // Records where the leader has been (oldest first, one point per TRAIL_SPACING metres) and
    // returns the point `back` metres behind the leader along that trail, or null while the
    // trail is still shorter than that.  A leader jump (teleport) restarts the trail.
    public location trailTarget(obj_id self, obj_id leader, float back) throws InterruptedException
    {
        location here = getLocation(leader);
        if (here == null || isIdValid(here.cell) || back <= 0.0f)
        {
            return null;
        }
        location[] trail = utils.getLocationArrayScriptVar(self, SCRIPTVAR_TRAIL);
        location last = trail != null && trail.length > 0 ? trail[trail.length - 1] : null;
        if (last == null || !sameSpace(last, here) || getDistance(last, here) > TRAIL_RESET_JUMP)
        {
            trail = new location[]{new location(here)};
        }
        else if (getDistance(last, here) >= TRAIL_SPACING)
        {
            int keep = Math.min(trail.length, TRAIL_POINTS - 1);
            location[] grown = new location[keep + 1];
            System.arraycopy(trail, trail.length - keep, grown, 0, keep);
            grown[keep] = new location(here);
            trail = grown;
        }
        utils.setScriptVar(self, SCRIPTVAR_TRAIL, trail);
        float remaining = back;
        location previous = here;
        for (int index = trail.length - 1; index >= 0; index--)
        {
            float step = getDistance(previous, trail[index]);
            if (step >= remaining && step > 0.0f)
            {
                float fraction = remaining / step;
                location point = new location(previous);
                point.x += (trail[index].x - previous.x) * fraction;
                point.z += (trail[index].z - previous.z) * fraction;
                return point;
            }
            remaining -= step;
            previous = trail[index];
        }
        return null;
    }

    // Re-aims the native follow at a trail point by converting it into the leader-relative
    // offset formationSlot uses.  The nominal offset objvars stay untouched.
    public void steerToTrail(obj_id self, obj_id leader, location target) throws InterruptedException
    {
        location origin = getLocation(leader);
        if (origin == null || aiGetMovementState(self) != MOVEMENT_FOLLOW)
        {
            return;
        }
        double radians = Math.toRadians(getYaw(leader));
        float cosine = (float)StrictMath.cos(radians);
        float sine = (float)StrictMath.sin(radians);
        float dx = target.x - origin.x;
        float dz = target.z - origin.z;
        location offset = new location();
        offset.x = cosine * dx - sine * dz;
        offset.z = sine * dx + cosine * dz;
        location previous = utils.getLocationScriptVar(self, SCRIPTVAR_TRAIL_OFFSET);
        if (previous != null && Math.abs(previous.x - offset.x) < TRAIL_RESTEER && Math.abs(previous.z - offset.z) < TRAIL_RESTEER)
        {
            return;
        }
        if (follow(self, leader, offset))
        {
            utils.setScriptVar(self, SCRIPTVAR_TRAIL_OFFSET, offset);
        }
    }

    // How far an escort is from where it should be; the coordinator and the escort's own
    // check both use this.  Fanned out: distance to its slot beside and behind the leader.
    // Single file: the escort walks the leader's trail, so it is in place whenever it is no
    // further from the leader in a straight line than its trail distance (-offsetZ).
    public static float formationGap(obj_id leader, obj_id escort, float offsetX, float offsetZ) throws InterruptedException
    {
        location position = getLocation(escort);
        if (offsetX == 0.0f && offsetZ < 0.0f)
        {
            location lead = getLocation(leader);
            return sameSpace(position, lead) ? Math.max(0.0f, getDistance(position, lead) + offsetZ) : Float.MAX_VALUE;
        }
        location desired = formationSlot(leader, offsetX, offsetZ);
        return sameSpace(position, desired) ? getDistance(position, desired) : Float.MAX_VALUE;
    }

    public static location formationSlot(obj_id leader, float offsetX, float offsetZ) throws InterruptedException
    {
        location origin = getLocation(leader);
        if (origin == null)
        {
            return null;
        }
        double radians = Math.toRadians(getYaw(leader));
        float cosine = (float)StrictMath.cos(radians);
        float sine = (float)StrictMath.sin(radians);
        location slot = new location(origin);
        slot.x += cosine * offsetX + sine * offsetZ;
        slot.z += -sine * offsetX + cosine * offsetZ;
        return slot;
    }

    public static boolean sameSpace(location first, location second)
    {
        return first != null && second != null && first.area.equals(second.area) && first.cell == second.cell;
    }

    public static boolean escortsWithinFormation(obj_id coordinator, obj_id leader) throws InterruptedException
    {
        if (!isIdValid(coordinator) || !exists(coordinator) || !isIdValid(leader) || !exists(leader))
        {
            return false;
        }
        obj_id[] current = getObjIdArrayObjVar(coordinator, squad_coordinator.OBJVAR_MEMBERS);
        if (current == null || current.length != squad_state.ROLE_COUNT || current[squad_state.ROLE_LEADER] != leader)
        {
            return false;
        }
        for (int role = squad_state.ROLE_ESCORT_1; role <= squad_state.ROLE_ESCORT_2; role++)
        {
            obj_id escort = current[role];
            if (!isIdValid(escort) || !exists(escort) || isDead(escort) || ai_lib.isIncapacitated(escort)
                    || !managed(escort) || ai_lib.isInCombat(escort) || aiIsTethered(escort)
                    || getObjIdObjVar(escort, OBJVAR_COORDINATOR) != coordinator || getIntObjVar(escort, OBJVAR_ROLE) != role
                    || getIntObjVar(escort, OBJVAR_EPOCH) != getIntObjVar(coordinator, squad_coordinator.OBJVAR_EPOCH)
                    || getIntObjVar(escort, OBJVAR_GENERATION) != getIntObjVar(coordinator, squad_coordinator.OBJVAR_GENERATION))
            {
                return false;
            }
            location offset = squad_coordinator.formationOffset(coordinator, role);
            if (utils.getIntScriptVar(escort, SCRIPTVAR_SECTION_REVISION)
                    != utils.getIntScriptVar(coordinator, squad_coordinator.SCRIPTVAR_SECTION_REVISION)
                    || getFloatObjVar(escort, OBJVAR_FOLLOW_X) != offset.x
                    || getFloatObjVar(escort, OBJVAR_FOLLOW_Z) != offset.z)
            {
                return false;
            }
            if (formationGap(leader, escort, offset.x, offset.z) > FORMATION_RECOVER_DISTANCE)
            {
                return false;
            }
        }
        return true;
    }

    public void notifyFormationCoordinator(obj_id actor, String event, int sequence, int episode, int holdToken) throws InterruptedException
    {
        if (!isIdValid(actor) || !hasObjVar(actor, OBJVAR_COORDINATOR))
        {
            return;
        }
        obj_id coordinator = getObjIdObjVar(actor, OBJVAR_COORDINATOR);
        if (!isIdValid(coordinator))
        {
            return;
        }
        dictionary notice = new dictionary();
        notice.put("actor", actor);
        notice.put("event", event);
        notice.put("role", getIntObjVar(actor, OBJVAR_ROLE));
        notice.put("epoch", getIntObjVar(actor, OBJVAR_EPOCH));
        notice.put("generation", getIntObjVar(actor, OBJVAR_GENERATION));
        notice.put("sequence", sequence);
        notice.put("episode", episode);
        notice.put("formationHoldToken", holdToken);
        notice.put("sectionRevision", getIntObjVar(actor, OBJVAR_ROLE) == squad_state.ROLE_LEADER
                ? utils.getIntScriptVar(coordinator, squad_coordinator.SCRIPTVAR_SECTION_REVISION)
                : utils.getIntScriptVar(actor, SCRIPTVAR_SECTION_REVISION));
        messageTo(coordinator, "squadEvent", notice, 0.0f, false);
    }

    public void stopFormationChecks(obj_id actor) throws InterruptedException
    {
        utils.removeScriptVar(actor, SCRIPTVAR_FORMATION_ACTIVE_SEQUENCE);
        utils.removeScriptVar(actor, SCRIPTVAR_FORMATION_EPOCH);
        utils.removeScriptVar(actor, SCRIPTVAR_FORMATION_GENERATION);
        utils.removeScriptVar(actor, SCRIPTVAR_FORMATION_LEADER);
        utils.removeScriptVar(actor, SCRIPTVAR_FORMATION_FAR_SINCE);
        utils.removeScriptVar(actor, SCRIPTVAR_FORMATION_ALERTED);
        utils.removeScriptVar(actor, SCRIPTVAR_FORMATION_IN_SLOT);
    }

    public void commandFailure(obj_id actor, String stage) throws InterruptedException
    {
        setObjVar(actor, OBJVAR_FAILURE, stage);
        notifyCoordinator(actor, "commandFailed");
    }

    public int OnMovePathComplete(obj_id self) throws InterruptedException
    {
        if (!currentMoveCommand(self))
        {
            return SCRIPT_CONTINUE;
        }
        String kind = utils.getStringScriptVar(self, SCRIPTVAR_MOVE_KIND);
        location target = utils.getLocationScriptVar(self, SCRIPTVAR_MOVE_TARGET);
        if (target == null || getDistance(self, target) > 1.5f)
        {
            // Native path callbacks carry no squad token.  Keep the current
            // command intact when a completion cannot be reconciled physically.
            return SCRIPT_CONTINUE;
        }
        clearMoveCommand(self);
        removeObjVar(self, OBJVAR_PERMISSION);
        notifyCoordinator(self, "depart".equals(kind) ? "departed" : "assemblyArrived");
        return SCRIPT_CONTINUE;
    }

    public int OnMovePathNotFound(obj_id self) throws InterruptedException
    {
        if (currentMoveCommand(self))
        {
            String kind = utils.getStringScriptVar(self, SCRIPTVAR_MOVE_KIND);
            clearMoveCommand(self);
            removeObjVar(self, OBJVAR_PERMISSION);
            commandFailure(self, kind + "PathNotFound");
        }
        return SCRIPT_CONTINUE;
    }

    public boolean moveCommandMatches(obj_id actor, String kind, int epoch, int generation, location target) throws InterruptedException
    {
        if (!utils.hasScriptVar(actor, SCRIPTVAR_MOVE_KIND)
                || !kind.equals(utils.getStringScriptVar(actor, SCRIPTVAR_MOVE_KIND))
                || utils.getIntScriptVar(actor, SCRIPTVAR_MOVE_EPOCH) != epoch
                || utils.getIntScriptVar(actor, SCRIPTVAR_MOVE_GENERATION) != generation)
        {
            return false;
        }
        location oldTarget = utils.getLocationScriptVar(actor, SCRIPTVAR_MOVE_TARGET);
        return oldTarget != null && target.area.equals(oldTarget.area) && target.cell == oldTarget.cell
                && getDistance(target, oldTarget) <= 0.1f;
    }

    public boolean currentMoveCommand(obj_id actor) throws InterruptedException
    {
        return utils.hasScriptVar(actor, SCRIPTVAR_MOVE_KIND)
                && utils.getIntScriptVar(actor, SCRIPTVAR_MOVE_EPOCH) == getIntObjVar(actor, OBJVAR_EPOCH)
                && utils.getIntScriptVar(actor, SCRIPTVAR_MOVE_GENERATION) == getIntObjVar(actor, OBJVAR_GENERATION);
    }

    public void clearMoveCommand(obj_id actor) throws InterruptedException
    {
        utils.removeScriptVar(actor, SCRIPTVAR_MOVE_KIND);
        utils.removeScriptVar(actor, SCRIPTVAR_MOVE_EPOCH);
        utils.removeScriptVar(actor, SCRIPTVAR_MOVE_GENERATION);
        utils.removeScriptVar(actor, SCRIPTVAR_MOVE_TARGET);
    }

    // ai.ai's calm callback can stop a sentinel or replace a direct native
    // follow.  Notify once per genuine calm transition; the coordinator restores
    // only movement appropriate to its current state.
    public int resumeDefaultCalmBehavior(obj_id self, dictionary params) throws InterruptedException
    {
        signal(self, "calmReady");
        return SCRIPT_CONTINUE;
    }

    public int OnEnteredCombat(obj_id self) throws InterruptedException
    {
        signal(self, "combatEntered");
        return SCRIPT_CONTINUE;
    }

    public int OnExitedCombat(obj_id self) throws InterruptedException
    {
        signal(self, "combatExited");
        return SCRIPT_CONTINUE;
    }

    public int OnAiTetherComplete(obj_id self) throws InterruptedException
    {
        signal(self, "tetherComplete");
        return SCRIPT_CONTINUE;
    }

    public int OnIncapacitated(obj_id self, obj_id killer) throws InterruptedException
    {
        notifyCoordinator(self, "memberLost");
        return SCRIPT_CONTINUE;
    }

    public int OnDeath(obj_id self, obj_id killer, obj_id corpseId) throws InterruptedException
    {
        notifyCoordinator(self, "memberLost");
        return SCRIPT_CONTINUE;
    }

    public int OnDestroy(obj_id self) throws InterruptedException
    {
        notifyCoordinator(self, "memberLost");
        return SCRIPT_CONTINUE;
    }

    public int OnUnloadedFromMemory(obj_id self) throws InterruptedException
    {
        notifyCoordinator(self, "unloaded");
        utils.removeScriptVar(self, SCRIPTVAR_ACK);
        stopFormationChecks(self);
        utils.removeScriptVar(self, SCRIPTVAR_SECTION_REVISION);
        clearFormationHold(self);
        clearMoveCommand(self);
        removeObjVar(self, OBJVAR_PERMISSION);
        return SCRIPT_CONTINUE;
    }
}
