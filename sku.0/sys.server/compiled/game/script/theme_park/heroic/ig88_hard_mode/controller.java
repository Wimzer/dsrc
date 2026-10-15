package script.theme_park.heroic.ig88_hard_mode;

import script.dictionary;
import script.library.ai_lib;
import script.library.buff;
import script.library.trial;
import script.library.utils;
import script.obj_id;

public class controller extends script.base_script
{
    public static final String VAR_PREFIX = "ig88_hard_mode";
    public static final String VAR_ROLE = VAR_PREFIX + ".role";
    public static final String VAR_OBJECT = VAR_PREFIX + ".object";
    public static final String VAR_STARTED = VAR_PREFIX + ".started";
    public static final String VAR_COMPLETE = VAR_PREFIX + ".complete";
    public static final String VAR_BOMBS_UNLOCKED = VAR_PREFIX + ".bombs_unlocked";
    public static final String VAR_DROIDEKAS_UNLOCKED = VAR_PREFIX + ".droidekas_unlocked";
    public static final String VAR_FLAMES_UNLOCKED = VAR_PREFIX + ".flames_unlocked";
    public static final String VAR_FLAMES_DEFEATED_ONCE = VAR_PREFIX + ".flames_defeated_once";
    public static final String VAR_WIPE_PENDING = VAR_PREFIX + ".wipe_pending";
    public static final String VAR_RESET_PENDING = VAR_PREFIX + ".reset_pending";
    public static final String VAR_FLAME_DEAD_PREFIX = VAR_PREFIX + ".flame_dead.";
    public static final String VAR_DROIDEKA_DEAD_PREFIX = VAR_PREFIX + ".droideka_dead.";
    public static final String VAR_DROIDEKA_RESPAWN_PENDING = VAR_PREFIX + ".droideka_respawn_pending";
    public static final String VAR_PENDING_SPAWN_PREFIX = VAR_PREFIX + ".pending_spawn.";
    public static final String VAR_SPAWN_ATTEMPT_PREFIX = VAR_PREFIX + ".spawn_attempt.";
    public static final String ROLE_BOMB = "bomb";
    public static final String ROLE_DROIDEKA = "droideka";
    public static final String ROLE_FLAME = "flame";
    public static final String ROLE_MOUSE = "mouse";
    public static final String ROLE_HMIG = "hmig";
    public static final String SPAWN_HMIG = "ig88";
    public static final String FLAME_A = "flame_a";
    public static final String FLAME_B = "flame_b";
    public static final String MOUSE_A = "mouse_droid1";
    public static final String MOUSE_B = "mouse_droid2";
    public static final String START_TRIGGER = "message_mouse_droid_run_away";
    public static final String BOSS_TEMPLATE = "heroic_ig88_ig88_hard_mode";
    public static final String BOMB_TEMPLATE = "heroic_ig88_bomb_droid_hard_mode";
    public static final String DROIDEKA_TEMPLATE = "heroic_ig88_droideka_hard_mode";
    public static final String FLAME_TEMPLATE = "heroic_ig88_flamethrower_droid_hard_mode";
    public static final String MOUSE_TEMPLATE = "heroic_ig88_mouse_droid";
    public static final String MOUSE_SCRIPT = "theme_park.heroic.ig88.ig88_mouse_droid";
    public static final String REFLECTIVE_HIDE = "ig88_hard_deflector_plating";
    public static final String SYMBIOTIC_WARNING = "ig88_hard_symbiotic_warning";
    public static final String VAR_RESPAWN_RUNNER = VAR_PREFIX + ".respawn_runner";
    public static final int HMIG_HEALTH = 1794000;
    public static final int SYMBIOTIC_DAMAGE = 10000;
    public static final int MAX_FINALIZE_ATTEMPTS = 5;
    public static final int MAX_RECOVERY_ATTEMPTS = 1;

    public int OnAttach(obj_id self) throws InterruptedException
    {
        return SCRIPT_CONTINUE;
    }

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        return SCRIPT_CONTINUE;
    }

    public int startNewInstance(obj_id self, dictionary params) throws InterruptedException
    {
        clearEncounterState(self, false);
        return SCRIPT_CONTINUE;
    }

    public int endInstanceSession(obj_id self, dictionary params) throws InterruptedException
    {
        clearEncounterState(self, false);
        return SCRIPT_CONTINUE;
    }

    public int triggerFired(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || params.isEmpty())
        {
            return SCRIPT_CONTINUE;
        }
        if (!params.containsKey(trial.MESSAGE_SESSION) || !trial.verifySession(self, params))
        {
            return SCRIPT_CONTINUE;
        }
        String triggerName = params.getString("triggerName");
        if (START_TRIGGER.equals(triggerName))
        {
            if (hasObjVar(self, VAR_STARTED) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING))
            {
                return SCRIPT_CONTINUE;
            }
            trial.bumpSession(self, VAR_PREFIX);
            setObjVar(self, VAR_STARTED, 1);
            removeObjVar(self, VAR_RESET_PENDING);
        }
        else if ("message_ig88_droidekas".equals(triggerName) && !hasObjVar(self, VAR_BOMBS_UNLOCKED))
        {
            unlockPersistentBombs(self);
        }
        else if ("message_ig88_droidekas_defeated".equals(triggerName) && !hasObjVar(self, VAR_DROIDEKAS_UNLOCKED))
        {
            setObjVar(self, VAR_DROIDEKAS_UNLOCKED, 1);
            schedulePersistentDroidekas(self, 30.0f);
        }
        else if ("message_ig88_spawn_super_droids".equals(triggerName) && !hasObjVar(self, VAR_FLAMES_UNLOCKED))
        {
            unlockFlames(self);
        }
        return SCRIPT_CONTINUE;
    }

    public int droidekaPhaseStarted(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED))
        {
            return SCRIPT_CONTINUE;
        }
        unlockPersistentBombs(self);
        return SCRIPT_CONTINUE;
    }

    public int flamePhaseStarted(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED))
        {
            return SCRIPT_CONTINUE;
        }
        unlockFlames(self);
        synchronizeReflectiveHide(self);
        return SCRIPT_CONTINUE;
    }

    public int bombDroidDied(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED))
        {
            return SCRIPT_CONTINUE;
        }
        String spawnId = params.getString("spawnId");
        if (spawnId != null && spawnId.startsWith("bomb_persist_"))
        {
            removeObjVar(self, VAR_PENDING_SPAWN_PREFIX + spawnId);
            scheduleBombSlot(self, spawnId, 30.0f);
        }
        return SCRIPT_CONTINUE;
    }

    public int killNPC(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || params.isEmpty())
        {
            return SCRIPT_CONTINUE;
        }
        obj_id npc = params.getObjId("npc");
        if (!isIdValid(npc))
        {
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(npc, "spawn_id") && !hasObjVar(self, VAR_COMPLETE) && !hasObjVar(self, VAR_RESET_PENDING) && hasObjVar(self, VAR_STARTED))
        {
            String spawnId = getStringObjVar(npc, "spawn_id");
            if (spawnId != null && spawnId.startsWith("bomb_persist_"))
            {
                setObjVar(npc, VAR_PREFIX + ".death_reported", 1);
                removeObjVar(self, VAR_PENDING_SPAWN_PREFIX + spawnId);
                scheduleBombSlot(self, spawnId, 30.0f);
            }
        }
        playClientEffectLoc(npc, "clienteffect/ig88_bomb_droid_explode.cef", getLocation(npc), 0.0f);
        trial.cleanupNpc(npc);
        return SCRIPT_CONTINUE;
    }

    public int droidekaDied(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED))
        {
            return SCRIPT_CONTINUE;
        }
        String spawnId = params.getString("spawnId");
        if (getPersistentDroidekaTrigger(spawnId) != null)
        {
            setObjVar(self, VAR_DROIDEKA_DEAD_PREFIX + spawnId, 1);
            if (allPersistentDroidekasDead(self) && !hasObjVar(self, VAR_DROIDEKA_RESPAWN_PENDING))
            {
                setObjVar(self, VAR_DROIDEKA_RESPAWN_PENDING, 1);
                messageTo(self, "respawnPersistentDroidekas", getControllerSessionDict(self), 30.0f, false);
            }
        }
        return SCRIPT_CONTINUE;
    }

    public int respawnBombSlot(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED) || !hasObjVar(self, VAR_BOMBS_UNLOCKED))
        {
            return SCRIPT_CONTINUE;
        }
        String spawnId = params.getString("spawnId");
        spawnBombSlot(self, spawnId);
        return SCRIPT_CONTINUE;
    }

    public int respawnDroidekaSlot(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED) || !hasObjVar(self, VAR_DROIDEKAS_UNLOCKED))
        {
            return SCRIPT_CONTINUE;
        }
        String spawnId = params.getString("spawnId");
        spawnDroidekaSlot(self, spawnId);
        return SCRIPT_CONTINUE;
    }

    public int respawnPersistentDroidekas(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED) || !hasObjVar(self, VAR_DROIDEKAS_UNLOCKED))
        {
            return SCRIPT_CONTINUE;
        }
        clearPersistentDroidekaDeaths(self);
        schedulePersistentDroidekas(self, 0.0f);
        return SCRIPT_CONTINUE;
    }

    public int finalizeSequencerSpawn(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED))
        {
            return SCRIPT_CONTINUE;
        }
        String spawnId = params.getString("spawnId");
        String triggerName = params.getString("triggerName");
        if (spawnId == null || spawnId.length() == 0 || triggerName == null || triggerName.length() == 0)
        {
            return SCRIPT_CONTINUE;
        }
        int attempt = params.getInt("attempt");
        String pendingVar = VAR_PENDING_SPAWN_PREFIX + spawnId;
        if (!hasObjVar(self, pendingVar) || getIntObjVar(self, pendingVar) != attempt)
        {
            return SCRIPT_CONTINUE;
        }
        obj_id spawned = getVerifiedSpawn(self, spawnId, getExpectedScript(spawnId));
        if (!isIdValid(spawned))
        {
            int retries = params.getInt("retries") + 1;
            if (retries < MAX_FINALIZE_ATTEMPTS)
            {
                params.put("retries", retries);
                messageTo(self, "finalizeSequencerSpawn", params, 1.0f, false);
                return SCRIPT_CONTINUE;
            }
            removeObjVar(self, pendingVar);
            deleteSpawn(self, spawnId);
            int recoveries = params.getInt("recoveries");
            if (recoveries >= MAX_RECOVERY_ATTEMPTS)
            {
                CustomerServiceLog("instance-ig88_hard_mode", "Failed to finalize sequencer spawn " + spawnId + " in instance " + self + " after bounded recovery. The slot was left empty for reset.");
                return SCRIPT_CONTINUE;
            }
            CustomerServiceLog("instance-ig88_hard_mode", "Failed to finalize sequencer spawn " + spawnId + " in instance " + self + ". Scheduling one recovery attempt.");
            dictionary retry = getControllerSessionDict(self);
            retry.put("spawnId", spawnId);
            retry.put("triggerName", triggerName);
            retry.put("recoveries", recoveries + 1);
            messageTo(self, "retrySequencerSpawn", retry, 5.0f, false);
            return SCRIPT_CONTINUE;
        }
        removeObjVar(self, pendingVar);
        if (FLAME_A.equals(spawnId) || FLAME_B.equals(spawnId))
        {
            synchronizeReflectiveHide(self);
        }
        return SCRIPT_CONTINUE;
    }

    public int retrySequencerSpawn(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || !canSpawnSlot(self, params.getString("spawnId")))
        {
            return SCRIPT_CONTINUE;
        }
        requestSequencerSpawn(self, params.getString("spawnId"), params.getString("triggerName"), params.getInt("recoveries"));
        return SCRIPT_CONTINUE;
    }

    public int reflectiveHideLoop(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED) || !hasObjVar(self, VAR_FLAMES_UNLOCKED))
        {
            return SCRIPT_CONTINUE;
        }
        synchronizeReflectiveHide(self);
        messageTo(self, "reflectiveHideLoop", getControllerSessionDict(self), 11.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int flameDied(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED) || !hasObjVar(self, VAR_FLAMES_UNLOCKED))
        {
            return SCRIPT_CONTINUE;
        }
        String deadId = params.getString("spawnId");
        if (deadId == null || deadId.length() == 0)
        {
            return SCRIPT_CONTINUE;
        }
        setObjVar(self, VAR_FLAME_DEAD_PREFIX + deadId, 1);
        synchronizeReflectiveHide(self);
        if (bothFlamesDead(self))
        {
            deleteSpawn(self, MOUSE_A);
            deleteSpawn(self, MOUSE_B);
            if (!hasObjVar(self, VAR_FLAMES_DEFEATED_ONCE))
            {
                setObjVar(self, VAR_FLAMES_DEFEATED_ONCE, 1);
                dictionary trigger = trial.getSessionDict(self);
                trigger.put("triggerName", "message_ig88_super_droids_defeated");
                trigger.put("triggerType", "triggerId");
                messageTo(self, "triggerFired", trigger, 0.0f, false);
            }
            messageTo(self, "flameRespawn", getControllerSessionDict(self), 30.0f, false);
            return SCRIPT_CONTINUE;
        }
        String survivorId = FLAME_A.equals(deadId) ? FLAME_B : FLAME_A;
        obj_id survivor = getObjectBySpawnId(self, survivorId);
        if (isIdValid(survivor) && !ai_lib.isDead(survivor))
        {
            buff.applyBuff(survivor, SYMBIOTIC_WARNING, 7.0f);
            dictionary dict = getControllerSessionDict(self);
            dict.put("deadId", deadId);
            dict.put("survivorId", survivorId);
            messageTo(self, "symbioticCheck", dict, 7.0f, false);
        }
        return SCRIPT_CONTINUE;
    }

    public int symbioticCheck(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED) || !hasObjVar(self, VAR_FLAMES_UNLOCKED))
        {
            return SCRIPT_CONTINUE;
        }
        String deadId = params.getString("deadId");
        String survivorId = params.getString("survivorId");
        if (bothFlamesDead(self) || !hasObjVar(self, VAR_FLAME_DEAD_PREFIX + deadId))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id survivor = getObjectBySpawnId(self, survivorId);
        if (isIdValid(survivor) && !ai_lib.isDead(survivor))
        {
            setAttrib(survivor, HEALTH, getMaxAttrib(survivor, HEALTH));
        }
        spawnFlame(self, deadId, true);
        spawnFlameMouseDroids(self);
        removeObjVar(self, VAR_FLAME_DEAD_PREFIX + deadId);
        damageGroupTrue(self, SYMBIOTIC_DAMAGE);
        return SCRIPT_CONTINUE;
    }

    public int flameRespawn(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED) || !hasObjVar(self, VAR_FLAMES_UNLOCKED))
        {
            return SCRIPT_CONTINUE;
        }
        removeObjVar(self, VAR_FLAME_DEAD_PREFIX + FLAME_A);
        removeObjVar(self, VAR_FLAME_DEAD_PREFIX + FLAME_B);
        spawnFlamePair(self);
        return SCRIPT_CONTINUE;
    }

    public int ensureFlameMouseDroids(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED) || !hasObjVar(self, VAR_FLAMES_UNLOCKED))
        {
            return SCRIPT_CONTINUE;
        }
        spawnFlameMouseDroids(self);
        return SCRIPT_CONTINUE;
    }

    public int hmigDied(obj_id self, dictionary params) throws InterruptedException
    {
        if (!verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !hasObjVar(self, VAR_STARTED))
        {
            return SCRIPT_CONTINUE;
        }
        setObjVar(self, VAR_COMPLETE, 1);
        trial.bumpSession(self, VAR_PREFIX);
        CustomerServiceLog("instance-ig88_hard_mode", "HMIG-88 defeated in test instance with rewards disabled.");
        trial.setDungeonCleanOutTimer(self, 15);
        return SCRIPT_CONTINUE;
    }

    public int ig88_failed(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || !verifyControllerSession(self, params) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING))
        {
            return SCRIPT_CONTINUE;
        }
        if (utils.hasScriptVar(self, "ig88.failure_time"))
        {
            int lastFailure = utils.getIntScriptVar(self, "ig88.failure_time");
            int messageTime = params.getInt("failure_time");
            if (messageTime != 0 && messageTime != lastFailure)
            {
                return SCRIPT_CONTINUE;
            }
        }
        obj_id[] spawnIds = trial.getObjectsInDungeonWithObjVar(self, "spawn_id");
        if (spawnIds != null)
        {
            for (obj_id spawnId : spawnIds)
            {
                if (isIdValid(spawnId))
                {
                    messageTo(spawnId, "shoutFailed", new dictionary(), 1.0f, false);
                }
            }
        }
        setObjVar(self, VAR_RESET_PENDING, 1);
        resetEncounter(self);
        messageTo(self, "restartSpawn", trial.getSessionDict(self), 5.0f, false);
        messageTo(self, "releaseWipeReset", trial.getSessionDict(self, VAR_PREFIX), 6.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int releaseWipeReset(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || params.isEmpty() || !trial.verifySession(self, params, VAR_PREFIX))
        {
            return SCRIPT_CONTINUE;
        }
        removeObjVar(self, VAR_RESET_PENDING);
        return SCRIPT_CONTINUE;
    }

    public int ig88_failure_check(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || hasObjVar(self, VAR_COMPLETE) || hasObjVar(self, VAR_RESET_PENDING) || !verifyControllerSession(self, params))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id[] targets = trial.getValidTargetsInCell(self, "r1");
        if (targets == null || targets.length <= 0)
        {
            int gameTime = getGameTime();
            utils.setScriptVar(self, "ig88.failure_time", gameTime);
            dictionary sessionDict = getControllerSessionDict(self);
            sessionDict.put("failure_time", gameTime);
            int lastFailureTime = utils.getIntScriptVar(self, "ig88.last_failure_time");
            if (gameTime - lastFailureTime > 1)
            {
                utils.setScriptVar(self, "ig88.last_failure_time", gameTime);
                messageTo(self, "ig88_failed", sessionDict, 1.0f, false);
            }
            return SCRIPT_CONTINUE;
        }
        messageTo(self, "ig88_failure_check", getControllerSessionDict(self), 5.0f, false);
        return SCRIPT_CONTINUE;
    }

    private void resetEncounter(obj_id self) throws InterruptedException
    {
        clearEncounterState(self, true);
    }

    private void clearEncounterState(obj_id self, boolean preserveResetPending) throws InterruptedException
    {
        trial.bumpSession(self, VAR_PREFIX);
        cleanupHardModeObjects(self);
        removeObjVar(self, VAR_STARTED);
        removeObjVar(self, VAR_COMPLETE);
        removeObjVar(self, VAR_BOMBS_UNLOCKED);
        removeObjVar(self, VAR_DROIDEKAS_UNLOCKED);
        removeObjVar(self, VAR_FLAMES_UNLOCKED);
        removeObjVar(self, VAR_FLAMES_DEFEATED_ONCE);
        removeObjVar(self, VAR_WIPE_PENDING);
        if (!preserveResetPending)
        {
            removeObjVar(self, VAR_RESET_PENDING);
        }
        removeObjVar(self, VAR_FLAME_DEAD_PREFIX + FLAME_A);
        removeObjVar(self, VAR_FLAME_DEAD_PREFIX + FLAME_B);
        clearSequencerSpawnState(self);
        clearPersistentDroidekaDeaths(self);
    }

    private void schedulePersistentBombs(obj_id top, float delay) throws InterruptedException
    {
        scheduleBombSlot(top, "bomb_persist_1", delay);
        scheduleBombSlot(top, "bomb_persist_2", delay);
        scheduleBombSlot(top, "bomb_persist_3", delay);
        scheduleBombSlot(top, "bomb_persist_4", delay);
    }

    private void unlockPersistentBombs(obj_id top) throws InterruptedException
    {
        if (!hasObjVar(top, VAR_BOMBS_UNLOCKED))
        {
            setObjVar(top, VAR_BOMBS_UNLOCKED, 1);
            schedulePersistentBombs(top, 30.0f);
        }
    }

    private void unlockFlames(obj_id top) throws InterruptedException
    {
        if (!hasObjVar(top, VAR_FLAMES_UNLOCKED))
        {
            setObjVar(top, VAR_FLAMES_UNLOCKED, 1);
            messageTo(top, "reflectiveHideLoop", getControllerSessionDict(top), 1.0f, false);
        }
    }

    private void schedulePersistentDroidekas(obj_id top, float delay) throws InterruptedException
    {
        scheduleDroidekaSlot(top, "droideka_persist_1", delay);
        scheduleDroidekaSlot(top, "droideka_persist_2", delay);
        scheduleDroidekaSlot(top, "droideka_persist_3", delay);
        scheduleDroidekaSlot(top, "droideka_persist_4", delay);
    }

    private void scheduleBombSlot(obj_id top, String spawnId, float delay) throws InterruptedException
    {
        dictionary dict = getControllerSessionDict(top);
        dict.put("spawnId", spawnId);
        messageTo(top, "respawnBombSlot", dict, delay, false);
    }

    private void scheduleDroidekaSlot(obj_id top, String spawnId, float delay) throws InterruptedException
    {
        dictionary dict = getControllerSessionDict(top);
        dict.put("spawnId", spawnId);
        messageTo(top, "respawnDroidekaSlot", dict, delay, false);
    }

    private void spawnBombSlot(obj_id top, String spawnId) throws InterruptedException
    {
        if (getPersistentBombTrigger(spawnId) == null)
        {
            return;
        }
        obj_id existing = getVerifiedSpawn(top, spawnId, getExpectedScript(spawnId));
        if (isIdValid(existing))
        {
            messageTo(existing, "findTarget", getControllerSessionDict(top), 0.25f, false);
            return;
        }
        deleteSpawn(top, spawnId);
        requestSequencerSpawn(top, spawnId, getPersistentBombTrigger(spawnId));
    }

    private void spawnDroidekaSlot(obj_id top, String spawnId) throws InterruptedException
    {
        if (getPersistentDroidekaTrigger(spawnId) == null)
        {
            return;
        }
        obj_id existing = getVerifiedSpawn(top, spawnId, getExpectedScript(spawnId));
        if (isIdValid(existing))
        {
            return;
        }
        deleteSpawn(top, spawnId);
        requestSequencerSpawn(top, spawnId, getPersistentDroidekaTrigger(spawnId));
    }

    private void spawnFlamePair(obj_id top) throws InterruptedException
    {
        spawnFlameMouseDroids(top);
        obj_id flameA = getVerifiedSpawn(top, FLAME_A, getExpectedScript(FLAME_A));
        obj_id flameB = getVerifiedSpawn(top, FLAME_B, getExpectedScript(FLAME_B));
        if (!isIdValid(flameA))
        {
            spawnFlame(top, FLAME_A, hasObjVar(top, VAR_FLAMES_DEFEATED_ONCE));
        }
        if (!isIdValid(flameB))
        {
            spawnFlame(top, FLAME_B, hasObjVar(top, VAR_FLAMES_DEFEATED_ONCE));
        }
    }

    private void spawnFlame(obj_id top, String spawnId) throws InterruptedException
    {
        spawnFlame(top, spawnId, false);
    }

    private void spawnFlame(obj_id top, String spawnId, boolean activeRunner) throws InterruptedException
    {
        deleteSpawn(top, spawnId);
        if (FLAME_A.equals(spawnId))
        {
            requestSequencerSpawn(top, spawnId, activeRunner ? "hm_spawn_flame_active_a" : "hm_spawn_flame_a");
        }
        else if (FLAME_B.equals(spawnId))
        {
            requestSequencerSpawn(top, spawnId, activeRunner ? "hm_spawn_flame_active_b" : "hm_spawn_flame_b");
        }
    }

    private void spawnFlameMouseDroids(obj_id top) throws InterruptedException
    {
        spawnFlameMouseDroid(top, MOUSE_A, 1.0f, 0.0f, -41.0f, 0.0f);
        spawnFlameMouseDroid(top, MOUSE_B, 1.0f, 0.0f, 41.0f, 0.0f);
    }

    private void spawnFlameMouseDroid(obj_id top, String spawnId, float x, float y, float z, float yaw) throws InterruptedException
    {
        obj_id existing = getVerifiedSpawn(top, spawnId, getExpectedScript(spawnId));
        if (isIdValid(existing))
        {
            return;
        }
        deleteSpawn(top, spawnId);
        if (MOUSE_A.equals(spawnId))
        {
            requestSequencerSpawn(top, spawnId, "hm_spawn_flame_mouse_a");
            return;
        }
        if (MOUSE_B.equals(spawnId))
        {
            requestSequencerSpawn(top, spawnId, "hm_spawn_flame_mouse_b");
        }
    }

    private void requestSequencerSpawn(obj_id top, String spawnId, String triggerName) throws InterruptedException
    {
        requestSequencerSpawn(top, spawnId, triggerName, 0);
    }

    private void requestSequencerSpawn(obj_id top, String spawnId, String triggerName, int recoveries) throws InterruptedException
    {
        if (triggerName == null || triggerName.length() == 0 || !canSpawnSlot(top, spawnId))
        {
            return;
        }
        String pendingVar = VAR_PENDING_SPAWN_PREFIX + spawnId;
        if (hasObjVar(top, pendingVar))
        {
            return;
        }
        int attempt = hasObjVar(top, VAR_SPAWN_ATTEMPT_PREFIX + spawnId) ? getIntObjVar(top, VAR_SPAWN_ATTEMPT_PREFIX + spawnId) + 1 : 1;
        setObjVar(top, VAR_SPAWN_ATTEMPT_PREFIX + spawnId, attempt);
        setObjVar(top, pendingVar, attempt);
        fireTrigger(top, triggerName);
        dictionary finalize = getControllerSessionDict(top);
        finalize.put("spawnId", spawnId);
        finalize.put("triggerName", triggerName);
        finalize.put("attempt", attempt);
        finalize.put("retries", 0);
        finalize.put("recoveries", recoveries);
        messageTo(top, "finalizeSequencerSpawn", finalize, 1.0f, false);
    }

    private boolean canSpawnSlot(obj_id top, String spawnId) throws InterruptedException
    {
        if (spawnId == null || spawnId.length() == 0 || hasObjVar(top, VAR_COMPLETE) || hasObjVar(top, VAR_RESET_PENDING) || !hasObjVar(top, VAR_STARTED))
        {
            return false;
        }
        if (spawnId.startsWith("bomb_persist_"))
        {
            return hasObjVar(top, VAR_BOMBS_UNLOCKED);
        }
        if (spawnId.startsWith("droideka_persist_"))
        {
            return hasObjVar(top, VAR_DROIDEKAS_UNLOCKED);
        }
        return (FLAME_A.equals(spawnId) || FLAME_B.equals(spawnId) || MOUSE_A.equals(spawnId) || MOUSE_B.equals(spawnId)) && hasObjVar(top, VAR_FLAMES_UNLOCKED);
    }

    public static dictionary getControllerSessionDict(obj_id top) throws InterruptedException
    {
        dictionary dict = trial.getSessionDict(top, VAR_PREFIX);
        dict.put(trial.MESSAGE_SESSION, trial.getSession(top));
        return dict;
    }

    public static boolean verifyControllerSession(obj_id top, dictionary params) throws InterruptedException
    {
        return params != null && !params.isEmpty() && trial.verifySession(top, params, VAR_PREFIX) && trial.verifySession(top, params);
    }

    private String getExpectedScript(String spawnId) throws InterruptedException
    {
        if (spawnId == null || spawnId.length() == 0)
        {
            return "";
        }
        if (spawnId.startsWith("bomb_persist_"))
        {
            return "theme_park.heroic.ig88_hard_mode.bomb_droid";
        }
        if (spawnId.startsWith("droideka_persist_"))
        {
            return "theme_park.heroic.ig88_hard_mode.droideka";
        }
        if (FLAME_A.equals(spawnId) || FLAME_B.equals(spawnId))
        {
            return "theme_park.heroic.ig88_hard_mode.flamethrower_droid";
        }
        if (MOUSE_A.equals(spawnId) || MOUSE_B.equals(spawnId))
        {
            return MOUSE_SCRIPT;
        }
        return "";
    }

    private obj_id getVerifiedSpawn(obj_id top, String spawnId, String scriptName) throws InterruptedException
    {
        obj_id[] objects = trial.getObjectsInDungeonWithObjVar(top, "spawn_id");
        if (objects == null || objects.length == 0 || spawnId == null || spawnId.length() == 0 || scriptName == null || scriptName.length() == 0)
        {
            return obj_id.NULL_ID;
        }
        obj_id match = obj_id.NULL_ID;
        int count = 0;
        for (obj_id object : objects)
        {
            if (!isIdValid(object) || !hasObjVar(object, "spawn_id") || !spawnId.equals(getStringObjVar(object, "spawn_id")))
            {
                continue;
            }
            count++;
            if (!ai_lib.isDead(object) && hasScript(object, scriptName))
            {
                match = object;
            }
        }
        return count == 1 && isIdValid(match) ? match : obj_id.NULL_ID;
    }

    private void fireTrigger(obj_id top, String triggerName) throws InterruptedException
    {
        if (triggerName == null || triggerName.length() == 0)
        {
            return;
        }
        dictionary trigger = trial.getSessionDict(top);
        trigger.put("triggerName", triggerName);
        trigger.put("triggerType", "triggerId");
        messageTo(top, "triggerFired", trigger, 0.0f, false);
    }

    private void clearSequencerSpawnState(obj_id top) throws InterruptedException
    {
        String[] spawnIds =
        {
            "bomb_persist_1",
            "bomb_persist_2",
            "bomb_persist_3",
            "bomb_persist_4",
            "droideka_persist_1",
            "droideka_persist_2",
            "droideka_persist_3",
            "droideka_persist_4",
            FLAME_A,
            FLAME_B,
            MOUSE_A,
            MOUSE_B
        };
        for (String spawnId : spawnIds)
        {
            removeObjVar(top, VAR_PENDING_SPAWN_PREFIX + spawnId);
            removeObjVar(top, VAR_SPAWN_ATTEMPT_PREFIX + spawnId);
        }
    }

    private String getPersistentBombTrigger(String spawnId)
    {
        if ("bomb_persist_1".equals(spawnId))
        {
            return "hm_spawn_persistent_bomb_1";
        }
        if ("bomb_persist_2".equals(spawnId))
        {
            return "hm_spawn_persistent_bomb_2";
        }
        if ("bomb_persist_3".equals(spawnId))
        {
            return "hm_spawn_persistent_bomb_3";
        }
        if ("bomb_persist_4".equals(spawnId))
        {
            return "hm_spawn_persistent_bomb_4";
        }
        return null;
    }

    private String getPersistentDroidekaTrigger(String spawnId)
    {
        if ("droideka_persist_1".equals(spawnId))
        {
            return "hm_spawn_persistent_droideka_1";
        }
        if ("droideka_persist_2".equals(spawnId))
        {
            return "hm_spawn_persistent_droideka_2";
        }
        if ("droideka_persist_3".equals(spawnId))
        {
            return "hm_spawn_persistent_droideka_3";
        }
        if ("droideka_persist_4".equals(spawnId))
        {
            return "hm_spawn_persistent_droideka_4";
        }
        return null;
    }

    private boolean hasLivingFlame(obj_id top) throws InterruptedException
    {
        obj_id flameA = getObjectBySpawnId(top, FLAME_A);
        obj_id flameB = getObjectBySpawnId(top, FLAME_B);
        return (!hasObjVar(top, VAR_FLAME_DEAD_PREFIX + FLAME_A) && isIdValid(flameA) && !ai_lib.isDead(flameA)) || (!hasObjVar(top, VAR_FLAME_DEAD_PREFIX + FLAME_B) && isIdValid(flameB) && !ai_lib.isDead(flameB));
    }

    private void synchronizeReflectiveHide(obj_id top) throws InterruptedException
    {
        obj_id boss = getObjectBySpawnId(top, SPAWN_HMIG);
        if (!isIdValid(boss))
        {
            return;
        }
        if (hasLivingFlame(top))
        {
            buff.applyBuff(boss, REFLECTIVE_HIDE, 12.0f);
        }
        else if (buff.hasBuff(boss, REFLECTIVE_HIDE))
        {
            buff.removeBuff(boss, REFLECTIVE_HIDE);
        }
    }

    private boolean bothFlamesDead(obj_id top) throws InterruptedException
    {
        return hasObjVar(top, VAR_FLAME_DEAD_PREFIX + FLAME_A) && hasObjVar(top, VAR_FLAME_DEAD_PREFIX + FLAME_B);
    }

    private boolean allPersistentDroidekasDead(obj_id top) throws InterruptedException
    {
        return hasObjVar(top, VAR_DROIDEKA_DEAD_PREFIX + "droideka_persist_1") && hasObjVar(top, VAR_DROIDEKA_DEAD_PREFIX + "droideka_persist_2") && hasObjVar(top, VAR_DROIDEKA_DEAD_PREFIX + "droideka_persist_3") && hasObjVar(top, VAR_DROIDEKA_DEAD_PREFIX + "droideka_persist_4");
    }

    private void clearPersistentDroidekaDeaths(obj_id top) throws InterruptedException
    {
        removeObjVar(top, VAR_DROIDEKA_RESPAWN_PENDING);
        removeObjVar(top, VAR_DROIDEKA_DEAD_PREFIX + "droideka_persist_1");
        removeObjVar(top, VAR_DROIDEKA_DEAD_PREFIX + "droideka_persist_2");
        removeObjVar(top, VAR_DROIDEKA_DEAD_PREFIX + "droideka_persist_3");
        removeObjVar(top, VAR_DROIDEKA_DEAD_PREFIX + "droideka_persist_4");
    }

    public static obj_id getObjectBySpawnId(obj_id top, String spawnId) throws InterruptedException
    {
        obj_id[] objects = trial.getObjectsInDungeonWithObjVar(top, "spawn_id");
        if (objects == null || objects.length == 0 || spawnId == null || spawnId.length() == 0)
        {
            return obj_id.NULL_ID;
        }
        for (obj_id object : objects)
        {
            if (isIdValid(object) && hasObjVar(object, "spawn_id") && spawnId.equals(getStringObjVar(object, "spawn_id")) && !ai_lib.isDead(object))
            {
                return object;
            }
        }
        return obj_id.NULL_ID;
    }

    private void deleteSpawn(obj_id top, String spawnId) throws InterruptedException
    {
        obj_id[] objects = trial.getObjectsInDungeonWithObjVar(top, "spawn_id");
        if (objects == null || objects.length == 0 || spawnId == null || spawnId.length() == 0)
        {
            return;
        }
        for (obj_id object : objects)
        {
            if (isIdValid(object) && hasObjVar(object, "spawn_id") && spawnId.equals(getStringObjVar(object, "spawn_id")))
            {
                trial.cleanupObject(object);
            }
        }
    }

    private void damageGroupTrue(obj_id top, int amount) throws InterruptedException
    {
        obj_id[] players = trial.getValidTargetsInCell(top, "r1");
        if (players == null || players.length == 0)
        {
            return;
        }
        for (obj_id player : players)
        {
            if (isIdValid(player) && !ai_lib.isDead(player))
            {
                int current = getAttrib(player, HEALTH);
                setAttrib(player, HEALTH, Math.max(1, current - amount));
            }
        }
    }

    private void cleanupHardModeObjects(obj_id top) throws InterruptedException
    {
        obj_id[] objects = trial.getObjectsInDungeonWithObjVar(top, VAR_OBJECT);
        if (objects == null || objects.length == 0)
        {
            return;
        }
        for (obj_id object : objects)
        {
            if (isIdValid(object))
            {
                trial.cleanupObject(object);
            }
        }
    }
}
