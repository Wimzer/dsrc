package script.systems.npc_mount;

import script.*;
import script.ai.ai_combat_movement;
import script.combat_engine.combat_data;
import script.library.*;

public class mounted_sandtrooper extends script.ai.creature_combat
{
    public mounted_sandtrooper()
    {
    }

    public static final String SCRIPT_BASE_COMBAT = "ai.creature_combat";
    public static final String SCRIPT_AUXILIARY = "systems.npc_mount.auxiliary_support";

    public static final String SCRIPT_MOUNT_CANDIDATE = "systems.npc_mount.mount_candidate";

    public static final String COMMAND_NPC_MOUNTED_SHOT = "npcMountedShot";
    public static final String ACTION_RANGED_SHOT = "rangedShot";

    public static final String OBJVAR_FEATURE = "npcMount.prototype";
    public static final String OBJVAR_RETIRED = "npcMount.retired";
    public static final String OBJVAR_SETUP_COMPLETE = "npcMount.setupComplete";
    public static final String OBJVAR_MOUNT = "npcMount.mount";
    public static final String OBJVAR_CONTROL_DEVICE = "npcMount.controlDevice";
    public static final String OBJVAR_AUXILIARY_OWNER = "npcMount.auxiliaryOwner";
    public static final String OBJVAR_AUXILIARY_ROLE = "npcMount.auxiliaryRole";
    public static final String OBJVAR_MOUNTABLE_PREPARED = "npcMount.mountablePrepared";
    public static final String OBJVAR_FAILURE_STAGE = "npcMount.failureStage";
    public static final String OBJVAR_MOUNTABILITY_STATUS = "npcMount.mountabilityStatus";
    public static final String OBJVAR_SPAWNER = "npcMount.spawner";
    public static final String OBJVAR_PATROL_ROUTE = "npcMount.patrolRoute";
    public static final String OBJVAR_PATROL_FAILURE = "npcMount.patrolFailure";
    public static final String PATROL_TABLE = "datatables/npc_mount/patrol_routes.iff";

    public static final String ROLE_MOUNT = "mount";
    public static final String ROLE_CONTROL_DEVICE = "controlDevice";

    public static final String SCRIPTVAR_SETUP_ATTEMPTS = "npcMount.setupAttempts";
    public static final String SCRIPTVAR_SETUP_WAITING = "npcMount.setupWaiting";
    public static final String SCRIPTVAR_CLEANUP = "npcMount.cleanup";
    public static final String SCRIPTVAR_CLEANUP_FINISHED = "npcMount.cleanupFinished";
    public static final String SCRIPTVAR_MOUNT_TRANSFER = "npcMount.mountTransfer";
    public static final String SCRIPTVAR_PATROL_CONFIGURED = "npcMount.patrolConfigured";

    // Temporary, event-only POI13 probe. No timer, behavioral gate or persisted objvar.
    public static final String TRACE_PREFIX = "npcMount.trace14";
    public static final String TRACE_CHANNEL = "npc_mount_trace";
    public static final int TRACE_EVENT_LIMIT = 32;

    public static boolean traceEnabled(obj_id rider) throws InterruptedException
    {
        return isIdValid(rider) && exists(rider) && !isPlayer(rider)
                && "1095642".equals(getRiderSpawner(rider))
                && "1095643".equals(getMountSpawner(rider))
                && isFromSpawner(rider, "1095642");
    }

    public static String traceLocation(location where)
    {
        return where == null ? "unknown" : where.area + ":" + where.x + "," + where.y + "," + where.z + ":cell=" + where.cell;
    }

    public static void traceLeashExit(obj_id rider) throws InterruptedException
    {
        try
        {
            if (traceEnabled(rider) && !utils.hasScriptVar(rider, TRACE_PREFIX + ".outsideLeash"))
            {
                utils.setScriptVar(rider, TRACE_PREFIX + ".outsideLeash", true);
                traceEvent(rider, "leash.outside", "");
            }
        }
        catch (Exception ignored)
        {
            // A probe failure cannot interfere with the normal leash response.
        }
    }

    public static void traceEvent(obj_id rider, String event, String detail) throws InterruptedException
    {
        // A logging/API failure must never prevent normal dismount or cleanup.
        try
        {
            if (!traceEnabled(rider))
            {
                return;
            }
            boolean terminal = "retire.beforeCleanup".equals(event);
            if (terminal)
            {
                if (utils.hasScriptVar(rider, TRACE_PREFIX + ".retired"))
                {
                    return;
                }
                utils.setScriptVar(rider, TRACE_PREFIX + ".retired", true);
            }
            else
            {
                int count = utils.getIntScriptVar(rider, TRACE_PREFIX + ".count");
                if (count >= TRACE_EVENT_LIMIT)
                {
                    return;
                }
                utils.setScriptVar(rider, TRACE_PREFIX + ".count", count + 1);
            }
            obj_id mount = getObjIdObjVar(rider, OBJVAR_MOUNT);
            obj_id device = getObjIdObjVar(rider, OBJVAR_CONTROL_DEVICE);
            boolean mountExists = isIdValid(mount) && exists(mount);
            boolean deviceExists = isIdValid(device) && exists(device);
            location here = getLocation(rider);
            location home = aiGetHomeLocation(rider);
            location anchor = aiGetLeashAnchorLocation(rider);
            String snapshot = "[DEBUG-npc-mount14] t=" + getGameTime() + " rider=" + rider
                    + " event=" + event + " " + detail
                    + " setupComplete=" + hasObjVar(rider, OBJVAR_SETUP_COMPLETE)
                    + " retired=" + hasObjVar(rider, OBJVAR_RETIRED)
                    + " mount=" + mount + " device=" + device
                    + " actualMount=" + getMountId(rider)
                    + " actualRider=" + (mountExists ? getRiderId(mount) : null)
                    + " mountExists=" + mountExists + " deviceExists=" + deviceExists
                    + " mountOwner=" + (mountExists ? getObjIdObjVar(mount, OBJVAR_AUXILIARY_OWNER) : null)
                    + " deviceOwner=" + (deviceExists ? getObjIdObjVar(device, OBJVAR_AUXILIARY_OWNER) : null)
                    + " mountRole=" + (mountExists ? getStringObjVar(mount, OBJVAR_AUXILIARY_ROLE) : "missing")
                    + " deviceRole=" + (deviceExists ? getStringObjVar(device, OBJVAR_AUXILIARY_ROLE) : "missing")
                    + " deviceNested=" + (deviceExists && utils.isNestedWithin(device, rider))
                    + " callableDevice=" + (mountExists ? callable.getCallableCD(mount) : null)
                    + " deviceCallable=" + (deviceExists ? callable.getCDCallable(device) : null)
                    + " posture=" + getPosture(rider)
                    + " mountPosture=" + (mountExists ? getPosture(mount) : -1)
                    + " combat=" + ai_lib.isInCombat(rider) + " tethered=" + aiIsTethered(rider)
                    + " location=" + traceLocation(here) + " home=" + traceLocation(home)
                    + " anchor=" + traceLocation(anchor)
                    + " homeDistance=" + (here != null && home != null ? getDistance(here, home) : -1.0f)
                    + " anchorDistance=" + (here != null && anchor != null ? getDistance(here, anchor) : -1.0f)
                    + " leashRadius=" + aiGetLeashRadius()
                    + " leashRadiusSource=globalConfig";
            LIVE_LOG(TRACE_CHANNEL, snapshot);
        }
        catch (Exception ignored)
        {
            // Diagnostics only: retain the original gameplay callback outcome.
        }
    }

    public static final int SETUP_ABORT = -1;
    public static final int SETUP_RETRY = 0;
    public static final int SETUP_PENDING = 2;
    public static final int MAX_SETUP_ATTEMPTS = 8;
    public static final float INITIAL_SETUP_DELAY = 1.0f;
    public static final float SETUP_RETRY_DELAY = 1.0f;
    public static final float DISMOUNT_VERIFY_DELAY = 0.25f;
    public static final float PAIR_SEARCH_RADIUS = 12.0f;

    public static String getRiderSpawner(obj_id actor) throws InterruptedException
    {
        if (!isIdValid(actor) || !exists(actor))
        {
            return "";
        }
        return hasObjVar(actor, "npcMount.riderSpawner") ? getStringObjVar(actor, "npcMount.riderSpawner") : "";
    }

    public static String getMountSpawner(obj_id actor) throws InterruptedException
    {
        if (!isIdValid(actor) || !exists(actor))
        {
            return "";
        }
        return hasObjVar(actor, "npcMount.mountSpawner") ? getStringObjVar(actor, "npcMount.mountSpawner") : "";
    }

    public int OnAttach(obj_id self) throws InterruptedException
    {
        rememberSpawner(self);
        setObjVar(self, OBJVAR_FEATURE, true);
        replaceBaseCombatScript(self);
        messageTo(self, "setupMountedPair", null, INITIAL_SETUP_DELAY, false);
        return SCRIPT_CONTINUE;
    }

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        rememberSpawner(self);
        setObjVar(self, OBJVAR_FEATURE, true);
        replaceBaseCombatScript(self);
        if (!hasObjVar(self, OBJVAR_RETIRED))
        {
            setup_sequence.clear(self);
            utils.removeScriptVar(self, SCRIPTVAR_CLEANUP_FINISHED);
            utils.removeScriptVar(self, SCRIPTVAR_SETUP_WAITING);
            utils.removeScriptVar(self, SCRIPTVAR_SETUP_ATTEMPTS);
            messageTo(self, "setupMountedPair", null, INITIAL_SETUP_DELAY, false);
        }
        return SCRIPT_CONTINUE;
    }

    public void replaceBaseCombatScript(obj_id self) throws InterruptedException
    {
        if (hasScript(self, SCRIPT_BASE_COMBAT))
        {
            detachScript(self, SCRIPT_BASE_COMBAT);
        }
    }

    public int setupMountedPair(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || !exists(self) || isPlayer(self) || hasObjVar(self, OBJVAR_RETIRED) || isDead(self) || ai_lib.isIncapacitated(self)
                || utils.hasScriptVar(self, SCRIPTVAR_CLEANUP) || utils.hasScriptVar(self, SCRIPTVAR_CLEANUP_FINISHED)
                || utils.hasScriptVar(self, SCRIPTVAR_SETUP_WAITING))
        {
            return SCRIPT_CONTINUE;
        }
        if (setup_sequence.pending(self))
        {
            return SCRIPT_CONTINUE;
        }
        if (isValidMountedPair(self) && !hasObjVar(self, OBJVAR_SETUP_COMPLETE))
        {
            dictionary validation = setup_sequence.begin(self, getRecordedMount(self), getRecordedControlDevice(self), setup_sequence.VALIDATE);
            messageTo(self, "validateMountedPair", validation, SETUP_RETRY_DELAY, false);
            return SCRIPT_CONTINUE;
        }
        if (isValidMountedPair(self))
        {
            configureMountedMovement(self, getRecordedMount(self));
            setObjVar(self, OBJVAR_SETUP_COMPLETE, true);
            removeObjVar(self, OBJVAR_FAILURE_STAGE);
            utils.removeScriptVar(self, SCRIPTVAR_SETUP_ATTEMPTS);
            squad_member.signal(self, "mountedReady");
            return SCRIPT_CONTINUE;
        }
        // A stale setup message must not remount a rider after a forced dismount.
        if (hasObjVar(self, OBJVAR_SETUP_COMPLETE))
        {
            retireMountedPair(self, true, "setup.completedPairInvalid");
            return SCRIPT_CONTINUE;
        }

        int attempts = utils.getIntScriptVar(self, SCRIPTVAR_SETUP_ATTEMPTS) + 1;
        utils.setScriptVar(self, SCRIPTVAR_SETUP_ATTEMPTS, attempts);

        int result = trySetupMountedPair(self);
        if (result == SETUP_PENDING)
        {
            return SCRIPT_CONTINUE;
        }
        if (result == SETUP_ABORT || attempts >= MAX_SETUP_ATTEMPTS)
        {
            retireInitialSetup(self);
            return SCRIPT_CONTINUE;
        }

        messageTo(self, "setupMountedPair", null, SETUP_RETRY_DELAY, false);
        return SCRIPT_CONTINUE;
    }

    public int trySetupMountedPair(obj_id self) throws InterruptedException
    {
        if (!getMountsEnabled())
        {
            return failSetup(self, "mountsDisabled", SETUP_ABORT);
        }

        if (isPlayer(self) || !isFromSpawner(self, getRiderSpawner(self)))
        {
            return failSetup(self, "riderSpawn", SETUP_ABORT);
        }
        if (!prepareSpawnedPair(self))
        {
            return SETUP_RETRY;
        }

        obj_id mount = getRecordedMount(self);
        obj_id controlDevice = getRecordedControlDevice(self);
        if (!isPreparedAuxiliaryPair(self, mount, controlDevice))
        {
            return failSetup(self, "pairLinks", SETUP_RETRY);
        }

        if (!hasObjVar(mount, OBJVAR_MOUNTABLE_PREPARED))
        {
            int status = couldPetBeMadeMountable(mount);
            setObjVar(self, OBJVAR_MOUNTABILITY_STATUS, status);
            if (status != MSC_CREATURE_MOUNTABLE)
            {
                return failSetup(self, "mountability", SETUP_ABORT);
            }
            if (!makePetMountable(mount))
            {
                return failSetup(self, "makeMountable", SETUP_RETRY);
            }
            setObjVar(mount, OBJVAR_MOUNTABLE_PREPARED, true);
        }

        // Do not convert a visible creature and transfer its rider in the same
        // callback. This is a publication opportunity, not a client ACK.
        dictionary prepared = setup_sequence.begin(self, mount, controlDevice, setup_sequence.MOUNT);
        messageTo(self, "mountPreparedPair", prepared, SETUP_RETRY_DELAY, false);
        return SETUP_PENDING;
    }

    public boolean acceptsSetupPhase(obj_id self, dictionary token, int phase) throws InterruptedException
    {
        return isIdValid(self) && exists(self) && !isPlayer(self) && !isDead(self) && !ai_lib.isIncapacitated(self)
                && !hasObjVar(self, OBJVAR_RETIRED) && !hasObjVar(self, OBJVAR_SETUP_COMPLETE)
                && !utils.hasScriptVar(self, SCRIPTVAR_SETUP_WAITING)
                && !utils.hasScriptVar(self, SCRIPTVAR_CLEANUP) && !utils.hasScriptVar(self, SCRIPTVAR_CLEANUP_FINISHED)
                && setup_sequence.accepts(self, token, phase);
    }

    public int mountPreparedPair(obj_id self, dictionary token) throws InterruptedException
    {
        if (!acceptsSetupPhase(self, token, setup_sequence.MOUNT))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id mount = getRecordedMount(self);
        obj_id controlDevice = getRecordedControlDevice(self);
        if (mount != token.getObjId("mount") || controlDevice != token.getObjId("device")
                || !isPreparedAuxiliaryPair(self, mount, controlDevice) || !isMountCandidate(mount)
                || !isFromSpawner(mount, getMountSpawner(self)))
        {
            failSetup(self, "preparedPairChanged", SETUP_ABORT);
            retireInitialSetup(self);
            return SCRIPT_CONTINUE;
        }
        if (!getMountsEnabled())
        {
            failSetup(self, "mountsDisabled", SETUP_ABORT);
            retireInitialSetup(self);
            return SCRIPT_CONTINUE;
        }
        if (!isFromSpawner(self, getRiderSpawner(self)))
        {
            failSetup(self, "riderSpawn", SETUP_ABORT);
            retireInitialSetup(self);
            return SCRIPT_CONTINUE;
        }
        if (!doesMountHaveRoom(mount))
        {
            failSetup(self, "saddleCapacity", SETUP_ABORT);
            retireInitialSetup(self);
            return SCRIPT_CONTINUE;
        }
        boolean mounted;
        utils.setScriptVar(self, SCRIPTVAR_MOUNT_TRANSFER, mount);
        try
        {
            mounted = mountCreature(self, mount);
        }
        catch (RuntimeException ex)
        {
            utils.removeScriptVar(self, SCRIPTVAR_MOUNT_TRANSFER);
            failSetup(self, "mountCreatureException", SETUP_ABORT);
            retireInitialSetup(self);
            throw ex;
        }
        finally
        {
            // Never leave a reusable transfer permission after this native call.
            utils.removeScriptVar(self, SCRIPTVAR_MOUNT_TRANSFER);
        }
        if (!mounted)
        {
            setup_sequence.clear(self);
            failSetup(self, "mountCreature", SETUP_RETRY);
            if (utils.getIntScriptVar(self, SCRIPTVAR_SETUP_ATTEMPTS) >= MAX_SETUP_ATTEMPTS)
            {
                retireInitialSetup(self);
            }
            else
            {
                messageTo(self, "setupMountedPair", null, SETUP_RETRY_DELAY, false);
            }
            return SCRIPT_CONTINUE;
        }
        dictionary validation = setup_sequence.begin(self, mount, controlDevice, setup_sequence.VALIDATE);
        messageTo(self, "validateMountedPair", validation, SETUP_RETRY_DELAY, false);
        return SCRIPT_CONTINUE;
    }

    public int validateMountedPair(obj_id self, dictionary token) throws InterruptedException
    {
        if (!acceptsSetupPhase(self, token, setup_sequence.VALIDATE))
        {
            return SCRIPT_CONTINUE;
        }
        if (getRecordedMount(self) != token.getObjId("mount") || getRecordedControlDevice(self) != token.getObjId("device")
                || !isValidMountedPair(self))
        {
            failSetup(self, "mountedPairValidation", SETUP_ABORT);
            // Native mount succeeded earlier. A subsequent forced detach must
            // remain terminal, not become an automatic remount retry.
            retireMountedPair(self, true, "validation.invalidPair");
            return SCRIPT_CONTINUE;
        }
        setup_sequence.clear(self);
        setObjVar(self, OBJVAR_SETUP_COMPLETE, true);
        removeObjVar(self, OBJVAR_FAILURE_STAGE);
        utils.removeScriptVar(self, SCRIPTVAR_SETUP_ATTEMPTS);
        configureMountedMovement(self, getRecordedMount(self));
        traceEvent(self, "setup.validated", "");
        squad_member.signal(self, "mountedReady");
        return SCRIPT_CONTINUE;
    }

    public void configureMountedMovement(obj_id rider, obj_id mount) throws InterruptedException
    {
        // Native mount movement follows the driving rider. Do not run a second
        // autonomous mover on the dewback or freeze the rider on duplicate setup.
        if (!hasSuspendedMovement(mount))
        {
            stop(mount);
            suspendMovement(mount);
        }
        configureMountedPatrol(rider);
    }

    public boolean canDriveMountedPair(obj_id self) throws InterruptedException
    {
        return !isPlayer(self) && !isDead(self) && !ai_lib.isIncapacitated(self)
                && !hasObjVar(self, OBJVAR_RETIRED) && hasObjVar(self, OBJVAR_FEATURE)
                && !setup_sequence.pending(self)
                && isFromSpawner(self, getRiderSpawner(self)) && isValidMountedPair(self);
    }

    public void configureMountedPatrol(obj_id self) throws InterruptedException
    {
        // Managed squads have one route/formation owner. Initial mounting must
        // not release a partial squad or restart its route during recovery.
        if (squad_member.managed(self))
        {
            return;
        }
        if (!canDriveMountedPair(self) || ai_lib.isInCombat(self) || aiIsTethered(self)
                || ai_combat_movement.isAiImmobile(self) || !posture.isStanding(self)
                || utils.hasScriptVar(self, SCRIPTVAR_PATROL_CONFIGURED))
        {
            return;
        }
        String route = hasObjVar(self, OBJVAR_PATROL_ROUTE) ? getStringObjVar(self, OBJVAR_PATROL_ROUTE) : null;
        if (route != null && route.length() == 0)
        {
            // An explicitly configured stationary encounter still mounts and
            // fights normally; it must not inherit another content profile.
            stop(self);
            utils.setScriptVar(self, SCRIPTVAR_PATROL_CONFIGURED, true);
            removeObjVar(self, OBJVAR_PATROL_FAILURE);
            return;
        }
        if (route == null || !dataTableOpen(PATROL_TABLE))
        {
            setObjVar(self, OBJVAR_PATROL_FAILURE, "missingRoute");
            stop(self);
            return;
        }
        int rows = dataTableGetNumRows(PATROL_TABLE);
        int count = 0;
        for (int row = 0; row < rows; row++)
        {
            if (route.equals(dataTableGetString(PATROL_TABLE, row, "route")))
            {
                count++;
            }
        }
        location home = aiGetHomeLocation(self);
        if (count < 3 || count > 64 || home == null || isIdValid(home.cell))
        {
            setObjVar(self, OBJVAR_PATROL_FAILURE, "invalidRoute");
            stop(self);
            return;
        }
        location[] points = new location[count];
        int index = 0;
        for (int row = 0; row < rows; row++)
        {
            if (route.equals(dataTableGetString(PATROL_TABLE, row, "route")))
            {
                location point = new location(home);
                point.x += dataTableGetFloat(PATROL_TABLE, row, "x");
                point.z += dataTableGetFloat(PATROL_TABLE, row, "z");
                point.y = getHeightAtLocation(point.x, point.z);
                points[index++] = point;
            }
        }
        if (hasSuspendedMovement(self))
        {
            resumeMovement(self);
        }
        stop(self);
        setMovementWalk(self);
        ai_lib.setPatrolPath(self, points, 0);
        utils.setScriptVar(self, SCRIPTVAR_PATROL_CONFIGURED, true);
        setObjVar(self, OBJVAR_PATROL_ROUTE, route);
        removeObjVar(self, OBJVAR_PATROL_FAILURE);
    }

    public void clearMountedPatrol(obj_id self) throws InterruptedException
    {
        if (utils.hasScriptVar(self, SCRIPTVAR_PATROL_CONFIGURED))
        {
            // Discard the saved patrol as well as the active combat movement.
            if (hasSuspendedMovement(self))
            {
                resumeMovement(self);
            }
            stop(self);
            ai_lib.clearPatrolPath(self);
            ai_combat_movement.aiClearMoveMode();
            utils.removeScriptVar(self, SCRIPTVAR_PATROL_CONFIGURED);
        }
    }

    public int OnAiTetherStart(obj_id self) throws InterruptedException
    {
        traceEvent(self, "tether.start.before", "");
        int result = super.OnAiTetherStart(self);
        traceEvent(self, "tether.start.after", "");
        return result;
    }

    public int OnAiTetherComplete(obj_id self) throws InterruptedException
    {
        traceEvent(self, "tether.complete.before", "");
        int result = super.OnAiTetherComplete(self);
        traceEvent(self, "tether.complete.after", "");
        if (squad_member.managed(self))
        {
            squad_member.signal(self, "tetherComplete");
            return result;
        }
        // Match normal AI reset/calm recovery, without a separate patrol loop.
        messageTo(self, "resumeMountedPatrol", null, 1.1f, false);
        return result;
    }

    public int resumeMountedPatrol(obj_id self, dictionary params) throws InterruptedException
    {
        if (squad_member.managed(self))
        {
            squad_member.signal(self, "tetherComplete");
            return SCRIPT_CONTINUE;
        }
        if (canDriveMountedPair(self) && !ai_lib.isInCombat(self) && !aiIsTethered(self)
                && !ai_combat_movement.isAiImmobile(self) && posture.isStanding(self))
        {
            if (utils.hasScriptVar(self, SCRIPTVAR_PATROL_CONFIGURED))
            {
                ai_lib.resumePatrol(self);
            }
            else
            {
                configureMountedPatrol(self);
            }
        }
        return SCRIPT_CONTINUE;
    }

    public int failSetup(obj_id self, String stage, int result) throws InterruptedException
    {
        setObjVar(self, OBJVAR_FAILURE_STAGE, stage);
        return result;
    }

    public void retireInitialSetup(obj_id self) throws InterruptedException
    {
        setup_sequence.clear(self);
        // Exhaust only this bounded setup burst, not the fresh spawn's eligibility.
        // A later candidate announcement (or initialize) may restart it. Genuine
        // post-setup dismount still uses terminal retireMountedPair instead.
        utils.setScriptVar(self, SCRIPTVAR_SETUP_WAITING, true);
        utils.removeScriptVar(self, SCRIPTVAR_SETUP_ATTEMPTS);
        releaseInitialClaim(self);
        resumeMovement(self);
    }

    public void releaseInitialClaim(obj_id self) throws InterruptedException
    {
        obj_id mount = getRecordedMount(self);
        obj_id controlDevice = getRecordedControlDevice(self);
        obj_id actualMount = getMountId(self);
        if (isOwnedAuxiliary(self, actualMount, ROLE_MOUNT))
        {
            queueClear(self);
            dismountCreature(self);
        }
        if (isOwnedAuxiliary(self, mount, ROLE_MOUNT) && !isIdValid(getRiderId(mount)))
        {
            obj_id linkedDevice = callable.getCallableCD(mount);
            if (!isIdValid(linkedDevice) || isOwnedAuxiliary(self, linkedDevice, ROLE_CONTROL_DEVICE))
            {
                removeObjVar(mount, callable.OBJVAR_CALLABLE_CONTROL_DEVICE);
                removeObjVar(mount, OBJVAR_AUXILIARY_OWNER);
                removeObjVar(mount, OBJVAR_AUXILIARY_ROLE);
            }
        }
        // Clear recorded IDs before destruction notifications can be delivered.
        removeObjVar(self, OBJVAR_MOUNT);
        removeObjVar(self, OBJVAR_CONTROL_DEVICE);
        if (isOwnedAuxiliary(self, controlDevice, ROLE_CONTROL_DEVICE))
        {
            callable.setCDCallable(controlDevice, null);
            destroyObject(controlDevice);
        }
    }

    public void rememberSpawner(obj_id self) throws InterruptedException
    {
        if (isFromSpawner(self, getRiderSpawner(self)))
        {
            setObjVar(self, OBJVAR_SPAWNER, getRiderSpawner(self));
        }
    }

    public static boolean isFromSpawner(obj_id actor, String spawner) throws InterruptedException
    {
        if (!isIdValid(actor) || !exists(actor) || spawner == null || spawner.length() == 0)
        {
            return false;
        }
        // object.destroy_message removes its own list before sending the death
        // notification. Retain the already-validated identity for early cleanup.
        if (spawner.equals(getStringObjVar(actor, OBJVAR_SPAWNER)))
        {
            return true;
        }
        obj_id[] recipients = getObjIdArrayObjVar(actor, "destroyMessageList.recipients");
        if (recipients != null)
        {
            for (obj_id recipient : recipients)
            {
                if (isIdValid(recipient) && spawner.equals(recipient.toString()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isMountCandidate(obj_id mount) throws InterruptedException
    {
        return isFromSpawner(mount, getMountSpawner(mount)) && !isPlayer(mount) && !isDead(mount)
                && !ai_lib.isIncapacitated(mount) && hasScript(mount, SCRIPT_MOUNT_CANDIDATE);
    }

    public boolean claimMount(obj_id rider, obj_id mount) throws InterruptedException
    {
        if (!isMountCandidate(mount) || !isFromSpawner(mount, getMountSpawner(rider)) || getDistance(rider, mount) > PAIR_SEARCH_RADIUS)
        {
            return false;
        }
        obj_id owner = getObjIdObjVar(mount, OBJVAR_AUXILIARY_OWNER);
        obj_id occupant = getRiderId(mount);
        obj_id device = callable.getCallableCD(mount);
        if (isIdValid(owner) && owner != rider && !exists(owner)
                && ROLE_MOUNT.equals(getStringObjVar(mount, OBJVAR_AUXILIARY_ROLE))
                && !isIdValid(occupant))
        {
            // Recover only this marker's abandoned claim, never a live owner,
            // occupied saddle, foreign device, or device linked to another mount.
            if (isIdValid(device) && (!isOwnedAuxiliary(owner, device, ROLE_CONTROL_DEVICE)
                    || (isIdValid(callable.getCDCallable(device)) && callable.getCDCallable(device) != mount)))
            {
                return false;
            }
            removeObjVar(mount, callable.OBJVAR_CALLABLE_CONTROL_DEVICE);
            removeObjVar(mount, OBJVAR_AUXILIARY_OWNER);
            removeObjVar(mount, OBJVAR_AUXILIARY_ROLE);
            if (isIdValid(device))
            {
                callable.setCDCallable(device, null);
                destroyObject(device);
            }
            owner = null;
            device = null;
        }
        // Do not steal another actor's claim, device or occupied saddle.
        if ((isIdValid(owner) && owner != rider)
                || (isIdValid(occupant) && occupant != rider)
                || (isIdValid(device) && !isOwnedAuxiliary(rider, device, ROLE_CONTROL_DEVICE)))
        {
            return false;
        }
        setObjVar(mount, OBJVAR_AUXILIARY_OWNER, rider);
        setObjVar(mount, OBJVAR_AUXILIARY_ROLE, ROLE_MOUNT);
        setObjVar(rider, OBJVAR_MOUNT, mount);
        return true;
    }

    public boolean claimSpawnedMount(obj_id rider) throws InterruptedException
    {
        if (isPlayer(rider) || !isFromSpawner(rider, getRiderSpawner(rider)))
        {
            return false;
        }
        obj_id mount = getRecordedMount(rider);
        if (isOwnedAuxiliary(rider, mount, ROLE_MOUNT))
        {
            return isMountCandidate(mount);
        }
        obj_id[] candidates = getAllObjectsWithScript(getLocation(rider), PAIR_SEARCH_RADIUS, SCRIPT_MOUNT_CANDIDATE);
        if (candidates != null)
        {
            for (obj_id candidate : candidates)
            {
                if (claimMount(rider, candidate))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public int mountCandidateReady(obj_id self, dictionary params) throws InterruptedException
    {
        // Readiness may wake an exhausted initial burst, never a completed or
        // retired pair. Duplicate readiness cannot create parallel retry chains.
        if (params != null && isIdValid(self) && exists(self) && !isPlayer(self) && !isDead(self) && !ai_lib.isIncapacitated(self)
                && !hasObjVar(self, OBJVAR_RETIRED) && !hasObjVar(self, OBJVAR_SETUP_COMPLETE)
                && !utils.hasScriptVar(self, SCRIPTVAR_CLEANUP) && !utils.hasScriptVar(self, SCRIPTVAR_CLEANUP_FINISHED)
                && (!isIdValid(getRecordedMount(self)) || !exists(getRecordedMount(self)))
                && isFromSpawner(self, getRiderSpawner(self)))
        {
            if (claimMount(self, params.getObjId("mount")) && utils.hasScriptVar(self, SCRIPTVAR_SETUP_WAITING))
            {
                utils.removeScriptVar(self, SCRIPTVAR_SETUP_WAITING);
                utils.removeScriptVar(self, SCRIPTVAR_SETUP_ATTEMPTS);
                messageTo(self, "setupMountedPair", null, 0.0f, false);
            }
        }
        return SCRIPT_CONTINUE;
    }

    public boolean prepareSpawnedPair(obj_id self) throws InterruptedException
    {
        if (!claimSpawnedMount(self))
        {
            failSetup(self, "waitingForMount", SETUP_RETRY);
            return false;
        }
        obj_id mount = getRecordedMount(self);
        obj_id inventory = utils.getInventoryContainer(self);
        if (!isIdValid(inventory) || !exists(inventory))
        {
            failSetup(self, "riderInventory", SETUP_RETRY);
            return false;
        }

        obj_id controlDevice = getRecordedControlDevice(self);
        if (!isIdValid(controlDevice) || !exists(controlDevice))
        {
            String deviceTemplate = hasObjVar(self, "npcMount.controlDeviceTemplate")
                    ? getStringObjVar(self, "npcMount.controlDeviceTemplate") : "";
            if (deviceTemplate == null || deviceTemplate.length() == 0)
            {
                failSetup(self, "controlDeviceTemplate", SETUP_ABORT);
                return false;
            }
            controlDevice = createObject(deviceTemplate, inventory, "");
            if (!isIdValid(controlDevice) || !exists(controlDevice))
            {
                failSetup(self, "controlDeviceCreation", SETUP_RETRY);
                return false;
            }
            setObjVar(controlDevice, OBJVAR_AUXILIARY_OWNER, self);
            setObjVar(controlDevice, OBJVAR_AUXILIARY_ROLE, ROLE_CONTROL_DEVICE);
            setObjVar(self, OBJVAR_CONTROL_DEVICE, controlDevice);
            attachScript(controlDevice, SCRIPT_AUXILIARY);
        }
        if (!isOwnedAuxiliary(self, controlDevice, ROLE_CONTROL_DEVICE) || !utils.isNestedWithin(controlDevice, self))
        {
            failSetup(self, "controlDeviceContainment", SETUP_RETRY);
            return false;
        }

        obj_id existingDevice = callable.getCallableCD(mount);
        if (isIdValid(existingDevice) && existingDevice != controlDevice)
        {
            failSetup(self, "mountDeviceConflict", SETUP_RETRY);
            return false;
        }
        if (!callable.setCallableCD(mount, controlDevice) || !callable.setCDCallable(controlDevice, mount))
        {
            failSetup(self, "callableLinks", SETUP_RETRY);
            return false;
        }
        return true;
    }

    public static boolean isPrototypeMountTransfer(obj_id rider, obj_id destination, obj_id transferer) throws InterruptedException
    {
        // Called only as an exception to ai.ai's building/cell entry check.
        // Native mounting uses a null transferer; ordinary/player moves do not
        // gain permission merely because the NPC has a mount or feature marker.
        if (isIdValid(transferer) || !isIdValid(rider) || !exists(rider) || isPlayer(rider)
                || isDead(rider) || ai_lib.isIncapacitated(rider)
                || !hasScript(rider, "systems.npc_mount.mounted_sandtrooper")
                || !hasObjVar(rider, OBJVAR_FEATURE) || hasObjVar(rider, OBJVAR_RETIRED)
                || hasObjVar(rider, OBJVAR_SETUP_COMPLETE)
                || utils.hasScriptVar(rider, SCRIPTVAR_CLEANUP)
                || utils.hasScriptVar(rider, SCRIPTVAR_CLEANUP_FINISHED)
                || !utils.hasScriptVar(rider, SCRIPTVAR_MOUNT_TRANSFER))
        {
            return false;
        }
        if (!isFromSpawner(rider, getRiderSpawner(rider)) || !isMountCandidate(destination)
                || !isFromSpawner(destination, getMountSpawner(rider))
                || !hasObjVar(destination, OBJVAR_MOUNTABLE_PREPARED)
                || getObjIdObjVar(rider, OBJVAR_MOUNT) != destination
                || utils.getObjIdScriptVar(rider, SCRIPTVAR_MOUNT_TRANSFER) != destination)
        {
            return false;
        }
        return isPreparedAuxiliaryPair(rider, destination, getObjIdObjVar(rider, OBJVAR_CONTROL_DEVICE));
    }

    public static boolean isValidMountedContainment(obj_id rider, obj_id container) throws InterruptedException
    {
        // ai.ai normally destroys outdoor AI found inside another object when a
        // radial is requested. Native mounting legitimately puts this rider in
        // its mount, so exempt only a complete pair whose recorded, native and
        // actual containment links all still agree.
        if (!isIdValid(rider) || !exists(rider) || isPlayer(rider) || isDead(rider)
                || ai_lib.isIncapacitated(rider) || !isIdValid(container) || !exists(container)
                || !hasScript(rider, "systems.npc_mount.mounted_sandtrooper")
                || !hasObjVar(rider, OBJVAR_FEATURE) || !hasObjVar(rider, OBJVAR_SETUP_COMPLETE)
                || hasObjVar(rider, OBJVAR_RETIRED) || setup_sequence.pending(rider)
                || utils.hasScriptVar(rider, SCRIPTVAR_CLEANUP)
                || utils.hasScriptVar(rider, SCRIPTVAR_CLEANUP_FINISHED)
                || getContainedBy(rider) != container)
        {
            return false;
        }
        obj_id mount = getObjIdObjVar(rider, OBJVAR_MOUNT);
        obj_id controlDevice = getObjIdObjVar(rider, OBJVAR_CONTROL_DEVICE);
        if (mount != container || !isMountCandidate(mount)
                || !isFromSpawner(rider, getRiderSpawner(rider))
                || !isFromSpawner(mount, getMountSpawner(rider))
                || !hasObjVar(mount, OBJVAR_MOUNTABLE_PREPARED)
                || !isPreparedAuxiliaryPair(rider, mount, controlDevice))
        {
            return false;
        }
        return getMountId(rider) == mount && getRiderId(mount) == rider;
    }

    public static boolean isPreparedAuxiliaryPair(obj_id rider, obj_id mount, obj_id controlDevice) throws InterruptedException
    {
        if (!isIdValid(rider) || !exists(rider) || !isIdValid(mount) || !exists(mount) || !isIdValid(controlDevice) || !exists(controlDevice))
        {
            return false;
        }
        if (getObjIdObjVar(mount, OBJVAR_AUXILIARY_OWNER) != rider || getObjIdObjVar(controlDevice, OBJVAR_AUXILIARY_OWNER) != rider)
        {
            return false;
        }
        if (!ROLE_MOUNT.equals(getStringObjVar(mount, OBJVAR_AUXILIARY_ROLE)) || !ROLE_CONTROL_DEVICE.equals(getStringObjVar(controlDevice, OBJVAR_AUXILIARY_ROLE)))
        {
            return false;
        }
        if (!utils.isNestedWithin(controlDevice, rider))
        {
            return false;
        }
        return callable.getCallableCD(mount) == controlDevice && callable.getCDCallable(controlDevice) == mount;
    }

    public boolean isValidMountedPair(obj_id rider) throws InterruptedException
    {
        obj_id mount = getRecordedMount(rider);
        obj_id controlDevice = getRecordedControlDevice(rider);
        if (!isPreparedAuxiliaryPair(rider, mount, controlDevice))
        {
            return false;
        }
        return getMountId(rider) == mount && getRiderId(mount) == rider;
    }

    public obj_id getRecordedMount(obj_id rider) throws InterruptedException
    {
        if (!isIdValid(rider) || !hasObjVar(rider, OBJVAR_MOUNT))
        {
            return null;
        }
        return getObjIdObjVar(rider, OBJVAR_MOUNT);
    }

    public obj_id getRecordedControlDevice(obj_id rider) throws InterruptedException
    {
        if (!isIdValid(rider) || !hasObjVar(rider, OBJVAR_CONTROL_DEVICE))
        {
            return null;
        }
        return getObjIdObjVar(rider, OBJVAR_CONTROL_DEVICE);
    }

    public void attack(obj_id target) throws InterruptedException
    {
        obj_id self = getSelf();
        if (setup_sequence.pending(self))
        {
            return;
        }
        if (isValidMountedPair(self))
        {
            queueMountedShot(self, target);
            return;
        }
        if (hasObjVar(self, OBJVAR_SETUP_COMPLETE))
        {
            retireMountedPair(self, true, "attack.invalidPair");
        }
        super.attack(target);
    }

    public void queueMountedShot(obj_id self, obj_id target) throws InterruptedException
    {
        if (!isIdValid(target) || !exists(target) || isDead(target))
        {
            return;
        }
        int eligibility = combat.canPerformAction(ACTION_RANGED_SHOT, self);
        if (eligibility != combat.ACTION_SUCCESS)
        {
            return;
        }

        combat_data actionData = combat_engine.getCombatData(ACTION_RANGED_SHOT);
        if (actionData == null)
        {
            return;
        }
        int cooldownGroup = getStringCrc(actionData.cooldownGroup);
        if (getCooldownTimeLeft(self, cooldownGroup) > 0.0f)
        {
            return;
        }

        obj_id weapon = getCurrentWeapon(self);
        if (!isIdValid(weapon) || !exists(weapon))
        {
            return;
        }
        float distanceToTarget = getDistance(self, target);
        range_info weaponRange = aiGetWeaponRangeInfo(weapon);
        // Match combat_base.isInAttackRange: a positive action range overrides
        // the weapon; zero (ordinary rangedShot) leaves weapon range in force.
        float maxRange = actionData.maxRange > 0.0f ? actionData.maxRange : weaponRange.maxRange;
        if (!(distanceToTarget < maxRange))
        {
            return;
        }
        if (!canSee(self, target))
        {
            return;
        }

        faceTo(self, target);
        queueCommand(self, getStringCrc(COMMAND_NPC_MOUNTED_SHOT.toLowerCase()), target, "", COMMAND_PRIORITY_DEFAULT);
    }

    public void move(obj_id target) throws InterruptedException
    {
        obj_id self = getSelf();
        if (setup_sequence.pending(self))
        {
            return;
        }
        if (isValidMountedPair(self))
        {
            if (aiIsTethered(self))
            {
                return;
            }
            if (!canDriveMountedPair(self) || ai_combat_movement.isAiImmobile(self) || !posture.isStanding(self))
            {
                ai_combat_movement.aiIdle();
                return;
            }
            if (!ai_lib.isWithinLeash(self))
            {
                traceLeashExit(self);
                ai_combat_movement.aiIdle();
                stopCombat(self);
                return;
            }
            if (!isIdValid(target) || !exists(target) || isDead(target))
            {
                ai_combat_movement.aiIdle();
                return;
            }
            setLookAtTarget(self, target);
            obj_id weapon = getCurrentWeapon(self);
            combat_data actionData = combat_engine.getCombatData(ACTION_RANGED_SHOT);
            if (!isIdValid(weapon) || !exists(weapon) || actionData == null)
            {
                ai_combat_movement.aiIdle();
                return;
            }
            range_info weaponRange = aiGetWeaponRangeInfo(weapon);
            float maxRange = actionData.maxRange > 0.0f ? actionData.maxRange : weaponRange.maxRange;
            if (maxRange <= weaponRange.minRange)
            {
                ai_combat_movement.aiIdle();
                return;
            }
            if (!canSee(self, target))
            {
                ai_combat_movement.aiMoveToSee(target);
                return;
            }
            // Same upper follow-band calculation as ordinary ranged NPC AI,
            // using the resolved shot range. No minimum-distance retreat.
            float midRange = (maxRange - weaponRange.minRange) / 2.0f;
            float stopDistance = weaponRange.minRange + midRange + midRange * 0.2f;
            if (getDistance(self, target) > stopDistance)
            {
                pair_movement.approach(self, target, stopDistance);
            }
            else
            {
                ai_combat_movement.aiIdle();
            }
            faceTo(self, target);
            return;
        }
        if (hasObjVar(self, OBJVAR_SETUP_COMPLETE))
        {
            retireMountedPair(self, true, "move.invalidPair");
        }
        super.move(target);
    }

    public int npcMountedShot(obj_id self, obj_id target, String params, float defaultTime) throws InterruptedException
    {
        if (!isIdValid(self) || !exists(self) || isPlayer(self) || !hasObjVar(self, OBJVAR_FEATURE) || hasObjVar(self, OBJVAR_RETIRED)
                || setup_sequence.pending(self))
        {
            return SCRIPT_OVERRIDE;
        }
        if (!isValidMountedPair(self))
        {
            retireMountedPair(self, true, "shot.invalidPair");
            return SCRIPT_OVERRIDE;
        }
        if (!isIdValid(target) || !exists(target) || isDead(target))
        {
            return SCRIPT_OVERRIDE;
        }
        boolean success = combatStandardAction(ACTION_RANGED_SHOT, self, target, params, "", "");
        if (!success)
        {
            return SCRIPT_OVERRIDE;
        }
        return SCRIPT_CONTINUE;
    }

    public int OnChangedPosture(obj_id self, int oldPosture, int newPosture) throws InterruptedException
    {
        traceEvent(self, "posture.before", "old=" + oldPosture + " new=" + newPosture);
        int result = super.OnChangedPosture(self, oldPosture, newPosture);
        if (newPosture != POSTURE_UPRIGHT && hasObjVar(self, OBJVAR_SETUP_COMPLETE))
        {
            messageTo(self, "verifyMountedPairAfterPostureChange", null, DISMOUNT_VERIFY_DELAY, false);
        }
        return result;
    }

    public int verifyMountedPairAfterPostureChange(obj_id self, dictionary params) throws InterruptedException
    {
        if (hasObjVar(self, OBJVAR_SETUP_COMPLETE) && !isValidMountedPair(self))
        {
            retireMountedPair(self, true, "postureOrLostItem.invalidPair");
        }
        return SCRIPT_CONTINUE;
    }

    public int auxiliaryUnavailable(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || utils.hasScriptVar(self, SCRIPTVAR_CLEANUP))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id auxiliary = params.getObjId("auxiliary");
        if (isIdValid(auxiliary) && (auxiliary == getRecordedMount(self) || auxiliary == getRecordedControlDevice(self)))
        {
            if (!hasObjVar(self, OBJVAR_SETUP_COMPLETE))
            {
                failSetup(self, auxiliary == getRecordedMount(self) ? "mountUnavailable" : "controlDeviceUnavailable", SETUP_ABORT);
                retireInitialSetup(self);
            }
            else
            {
                retireMountedPair(self, true, "auxiliary." + params.getString("traceEvent"));
            }
        }
        return SCRIPT_CONTINUE;
    }

    public void retireMountedPair(obj_id self, boolean resumeOnFoot) throws InterruptedException
    {
        retireMountedPair(self, resumeOnFoot, "unspecified");
    }

    public void retireMountedPair(obj_id self, boolean resumeOnFoot, String reason) throws InterruptedException
    {
        traceEvent(self, "retire.beforeCleanup", "reason=" + reason);
        if (isIdValid(self) && exists(self))
        {
            setObjVar(self, OBJVAR_RETIRED, true);
        }
        cleanupAuxiliaries(self, true, resumeOnFoot);
        squad_member.signal(self, "terminalRetire");
    }

    public void cleanupAuxiliaries(obj_id self, boolean dismountRider, boolean resumeOnFoot) throws InterruptedException
    {
        if (!isIdValid(self) || utils.hasScriptVar(self, SCRIPTVAR_CLEANUP)
                || utils.hasScriptVar(self, SCRIPTVAR_CLEANUP_FINISHED))
        {
            return;
        }
        setup_sequence.clear(self);
        clearMountedPatrol(self);
        // Cleanup never acquires a nearby candidate: independent respawn timers
        // mean it may already belong to the next generation.
        utils.setScriptVar(self, SCRIPTVAR_CLEANUP_FINISHED, true);
        obj_id mount = getRecordedMount(self);
        obj_id controlDevice = getRecordedControlDevice(self);
        obj_id actualMount = null;
        if (exists(self) && !isPlayer(self))
        {
            actualMount = getMountId(self);
        }
        boolean hadAuxiliaries = hasObjVar(self, OBJVAR_SETUP_COMPLETE) || isIdValid(mount) || isIdValid(controlDevice);
        if (!hadAuxiliaries && (!dismountRider || !isIdValid(actualMount)))
        {
            return;
        }
        utils.setScriptVar(self, SCRIPTVAR_CLEANUP, true);
        removeObjVar(self, OBJVAR_SETUP_COMPLETE);
        removeObjVar(self, OBJVAR_MOUNT);
        removeObjVar(self, OBJVAR_CONTROL_DEVICE);
        utils.removeScriptVar(self, SCRIPTVAR_SETUP_ATTEMPTS);
        utils.removeScriptVar(self, SCRIPTVAR_SETUP_WAITING);

        if (dismountRider && isIdValid(actualMount))
        {
            queueClear(self);
            dismountCreature(self);
        }
        if (actualMount != mount && isOwnedAuxiliary(self, actualMount, ROLE_MOUNT))
        {
            destroyObject(actualMount);
        }
        if (isOwnedAuxiliary(self, mount, ROLE_MOUNT))
        {
            destroyObject(mount);
        }
        if (isOwnedAuxiliary(self, controlDevice, ROLE_CONTROL_DEVICE))
        {
            callable.setCDCallable(controlDevice, null);
            destroyObject(controlDevice);
        }
        if (resumeOnFoot && (hadAuxiliaries || isIdValid(actualMount)) && exists(self) && !isPlayer(self) && !isDead(self) && !ai_lib.isIncapacitated(self))
        {
            resumeMovement(self);
        }

        utils.removeScriptVar(self, SCRIPTVAR_CLEANUP);
    }

    public boolean isOwnedAuxiliary(obj_id rider, obj_id auxiliary, String role) throws InterruptedException
    {
        return isIdValid(auxiliary) && exists(auxiliary)
                && getObjIdObjVar(auxiliary, OBJVAR_AUXILIARY_OWNER) == rider
                && role.equals(getStringObjVar(auxiliary, OBJVAR_AUXILIARY_ROLE));
    }

    public int OnIncapacitated(obj_id self, obj_id killer) throws InterruptedException
    {
        retireMountedPair(self, false, "rider.incapacitated");
        return super.OnIncapacitated(self, killer);
    }

    public int OnDeath(obj_id self, obj_id killer, obj_id corpseId) throws InterruptedException
    {
        retireMountedPair(self, false, "rider.death");
        return SCRIPT_CONTINUE;
    }

    public int OnDestroy(obj_id self) throws InterruptedException
    {
        retireMountedPair(self, false, "rider.destroy");
        return super.OnDestroy(self);
    }

    public int OnUnloadedFromMemory(obj_id self) throws InterruptedException
    {
        traceEvent(self, "retire.beforeCleanup", "reason=rider.unload");
        cleanupAuxiliaries(self, true, false);
        return SCRIPT_CONTINUE;
    }

    public int OnDetach(obj_id self) throws InterruptedException
    {
        retireMountedPair(self, true, "rider.detach");
        return SCRIPT_CONTINUE;
    }
}
