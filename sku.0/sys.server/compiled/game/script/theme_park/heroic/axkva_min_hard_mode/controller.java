package script.theme_park.heroic.axkva_min_hard_mode;

import script.dictionary;
import script.library.ai_lib;
import script.library.skill;
import script.library.trial;
import script.library.utils;
import script.obj_id;

public class controller extends script.theme_park.heroic.axkva_min.axkva_controller
{
    public static final String VAR_PREFIX = encounter.VAR_PREFIX;

    public int startNewInstance(obj_id self, dictionary params) throws InterruptedException
    {
        if (params != null && !params.isEmpty())
        {
            clearEncounterState(self);
            setObjVar(self, encounter.VAR_RUNNING, 1);
        }
        return SCRIPT_CONTINUE;
    }

    public int endInstanceSession(obj_id self, dictionary params) throws InterruptedException
    {
        clearEncounterState(self);
        return SCRIPT_CONTINUE;
    }

    public int startWipeCheck(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isCurrentRun(self, params))
        {
            return SCRIPT_CONTINUE;
        }
        initializeSisterPhase(self);
        if (!hasObjVar(self, VAR_PREFIX + ".wipe_check_started"))
        {
            setObjVar(self, VAR_PREFIX + ".wipe_check_started", 1);
            setObjVar(self, VAR_PREFIX + ".engaged", 1);
            scheduleRegistrationTimeout(self, "nandina");
            scheduleRegistrationTimeout(self, "gorvo");
            messageTo(self, "wipeCheck", trial.getSessionDict(self, VAR_PREFIX), 5.0f, false);
        }
        return SCRIPT_CONTINUE;
    }

    public int startSisterPhase(obj_id self, dictionary params) throws InterruptedException
    {
        if (encounter.isRunning(self) && params != null && params.containsKey(trial.MESSAGE_SESSION) && trial.verifySession(self, params))
        {
            initializeSisterPhase(self);
        }
        return SCRIPT_CONTINUE;
    }

    public int wipeCheck(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isCurrentRun(self, params))
        {
            return SCRIPT_CONTINUE;
        }
        boolean noLivingPlayers = !hasLivingPlayer(self);
        boolean fullyDisengaged = hasObjVar(self, VAR_PREFIX + ".engaged") && encounter.hasActivePrimaryEnemy(self) && !encounter.hasPrimaryEnemyInCombat(self);
        if (noLivingPlayers || fullyDisengaged)
        {
            String reason = noLivingPlayers ? "group wipe" : "all active primary enemies left combat";
            if (hasObjVar(self, VAR_PREFIX + ".reset_pending"))
            {
                resetEncounter(self, reason);
                return SCRIPT_CONTINUE;
            }
            setObjVar(self, VAR_PREFIX + ".reset_pending", 1);
        }
        else if (hasObjVar(self, VAR_PREFIX + ".reset_pending"))
        {
            removeObjVar(self, VAR_PREFIX + ".reset_pending");
        }
        messageTo(self, "wipeCheck", trial.getSessionDict(self, VAR_PREFIX), 5.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int axkvaDied(obj_id self, dictionary params) throws InterruptedException
    {
        if (!encounter.isRunning(self) || params == null || !params.containsKey(trial.MESSAGE_SESSION) || !trial.verifySession(self, params) || !hasObjVar(self, encounter.VAR_AXKVA_RELEASED))
        {
            return SCRIPT_CONTINUE;
        }
        setObjVar(self, encounter.VAR_COMPLETE, 1);
        removeObjVar(self, encounter.VAR_RUNNING);
        trial.bumpSession(self, VAR_PREFIX);
        removeObjVar(self, VAR_PREFIX + ".reset_pending");
        trial.setDungeonCleanOutTimer(self, 15);

        obj_id[] players = trial.getPlayersInDungeon(self);
        CustomerServiceLog("instance-axkva_min_hard_mode", "Axkva defeated in instance " + self + " at " + getCalendarTimeStringLocal(getCalendarTime()) + ".");
        if (players != null && players.length > 0)
        {
            dictionary reward = new dictionary();
            reward.put("tokenIndex", 0);
            utils.messageTo(players, "handleAwardtoken", reward, 0, false);
            for (obj_id player : players)
            {
                CustomerServiceLog("instance-axkva_min_hard_mode", "Player " + player + " (" + getFirstName(player) + "), group " + getGroupObject(player) + ", profession " + skill.getProfessionName(getSkillTemplate(player)) + ".");
            }
        }
        return SCRIPT_CONTINUE;
    }

    public int spawnNextSister(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isCurrentRun(self, params))
        {
            return SCRIPT_CONTINUE;
        }
        initializeSisterPhase(self);
        if (hasObjVar(self, encounter.VAR_PENDING_SISTER_SPAWN))
        {
            return SCRIPT_CONTINUE;
        }
        if (encounter.countActiveSisters(self, obj_id.NULL_ID) >= 3 || getIntObjVar(self, encounter.VAR_TOTAL_SISTERS_SPAWNED) >= encounter.TOTAL_SISTERS)
        {
            clearSisterRequest(self);
            return SCRIPT_CONTINUE;
        }
        String[] queue = getStringArrayObjVar(self, encounter.VAR_SISTER_QUEUE);
        int index = hasObjVar(self, encounter.VAR_SISTER_QUEUE_INDEX) ? getIntObjVar(self, encounter.VAR_SISTER_QUEUE_INDEX) : 0;
        if (queue == null || queue.length == 0 || index >= queue.length)
        {
            clearSisterRequest(self);
            return SCRIPT_CONTINUE;
        }

        String spawnId = queue[index];
        setObjVar(self, encounter.VAR_PENDING_SISTER_SPAWN, spawnId);
        setObjVar(self, encounter.VAR_PENDING_SISTER_INDEX, index);
        setObjVar(self, encounter.VAR_PENDING_SISTER_REASON, getStringObjVar(self, encounter.VAR_SISTER_REQUEST_REASON));
        setObjVar(self, encounter.VAR_PENDING_SISTER_SOURCE, getObjIdObjVar(self, encounter.VAR_SISTER_REQUEST_SOURCE));
        setObjVar(self, encounter.VAR_PENDING_SISTER_DEATHS, getIntObjVar(self, encounter.VAR_SISTER_REQUEST_DEATHS));
        clearSisterRequest(self);
        fireSequencerTrigger(self, "hm_release_" + spawnId);

        dictionary timeout = trial.getSessionDict(self, VAR_PREFIX);
        timeout.put("spawnId", spawnId);
        messageTo(self, "actorRegistrationTimeout", timeout, 6.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int spawnAxkvaMin(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isCurrentRun(self, params) || hasObjVar(self, encounter.VAR_AXKVA_SPAWNED) || hasObjVar(self, encounter.VAR_PENDING_AXKVA_SPAWN))
        {
            return SCRIPT_CONTINUE;
        }
        setObjVar(self, encounter.VAR_PENDING_AXKVA_SPAWN, 1);
        removeObjVar(self, encounter.VAR_AXKVA_REQUEST_PENDING);
        fireSequencerTrigger(self, "hm_spawn_axkva_min");

        dictionary timeout = trial.getSessionDict(self, VAR_PREFIX);
        timeout.put("spawnId", "axkva_min");
        messageTo(self, "actorRegistrationTimeout", timeout, 6.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int actorReady(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isCurrentRun(self, params))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id actor = params.getObjId("actor");
        String spawnId = params.getString("spawnId");
        if (!isIdValid(actor) || !exists(actor) || trial.getTop(actor) != self || (!encounter.isSister(spawnId) && !"gorvo".equals(spawnId) && !"axkva_min".equals(spawnId)))
        {
            return SCRIPT_CONTINUE;
        }
        String scriptName = "theme_park.heroic.axkva_min_hard_mode." + ("axkva_min".equals(spawnId) ? "axkva" : spawnId);
        if (!spawnId.equals(getStringObjVar(actor, "spawn_id")) || !hasScript(actor, scriptName) || getIntObjVar(actor, encounter.VAR_ACTOR_SESSION) != trial.getSession(self, VAR_PREFIX))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id registered = getObjIdObjVar(self, encounter.VAR_ACTORS + "." + spawnId);
        if (isIdValid(registered))
        {
            if (registered != actor)
            {
                resetEncounter(self, "duplicate actor " + spawnId);
            }
            return SCRIPT_CONTINUE;
        }

        boolean opening = "nandina".equals(spawnId) || "gorvo".equals(spawnId);
        boolean axkva = "axkva_min".equals(spawnId);
        if (!opening && !(axkva ? hasObjVar(self, encounter.VAR_PENDING_AXKVA_SPAWN) : spawnId.equals(getStringObjVar(self, encounter.VAR_PENDING_SISTER_SPAWN))))
        {
            resetEncounter(self, "unexpected actor " + spawnId);
            return SCRIPT_CONTINUE;
        }
        setObjVar(self, encounter.VAR_ACTORS + "." + spawnId, actor);

        if (encounter.isSister(spawnId))
        {
            setObjVar(self, encounter.VAR_TOTAL_SISTERS_SPAWNED, getIntObjVar(self, encounter.VAR_TOTAL_SISTERS_SPAWNED) + 1);
            if (!opening)
            {
                int queueIndex = getIntObjVar(self, encounter.VAR_PENDING_SISTER_INDEX);
                obj_id source = getObjIdObjVar(self, encounter.VAR_PENDING_SISTER_SOURCE);
                String reason = getStringObjVar(self, encounter.VAR_PENDING_SISTER_REASON);
                int requestDeaths = getIntObjVar(self, encounter.VAR_PENDING_SISTER_DEATHS);
                setObjVar(self, encounter.VAR_SISTER_QUEUE_INDEX, queueIndex + 1);
                if (requestDeaths == encounter.getSisterDeathsFromTop(self) && isIdValid(source) && exists(source) && !ai_lib.isDead(source))
                {
                    if (encounter.REASON_SISTER_66.equals(reason))
                    {
                        setObjVar(source, encounter.VAR_SPAWN_TRIGGERED_66, 1);
                    }
                    else if (encounter.REASON_SISTER_33.equals(reason))
                    {
                        setObjVar(source, encounter.VAR_SPAWN_TRIGGERED_33, 1);
                    }
                }
                clearPendingSister(self);
                setInvulnerable(actor, false);
                addPlayersToHateList(self, actor);
            }
        }
        if (axkva)
        {
            removeObjVar(self, encounter.VAR_PENDING_AXKVA_SPAWN);
            setObjVar(self, encounter.VAR_AXKVA_SPAWNED, 1);
        }
        encounter.tryActivateAxkvaMin(self);
        return SCRIPT_CONTINUE;
    }

    public int sisterDied(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isCurrentRun(self, params))
        {
            return SCRIPT_CONTINUE;
        }
        actorReady(self, params);
        obj_id actor = params.getObjId("actor");
        String spawnId = params.getString("spawnId");
        if (!isCurrentRun(self, params) || !encounter.isSister(spawnId) || !isIdValid(actor) || actor != getObjIdObjVar(self, encounter.VAR_ACTORS + "." + spawnId) || hasObjVar(self, encounter.VAR_DEAD + "." + spawnId))
        {
            return SCRIPT_CONTINUE;
        }
        setObjVar(self, encounter.VAR_DEAD + "." + spawnId, 1);
        int deaths = encounter.getSisterDeathsFromTop(self) + 1;
        setObjVar(self, encounter.VAR_SISTER_DEATHS, deaths);
        encounter.updateActiveSisterCount(self, actor);
        encounter.applyRageToActiveBosses(self, deaths, actor);
        return SCRIPT_CONTINUE;
    }

    public int actorRegistrationTimeout(obj_id self, dictionary params) throws InterruptedException
    {
        if (isCurrentRun(self, params) && !hasObjVar(self, encounter.VAR_ACTORS + "." + params.getString("spawnId")))
        {
            resetEncounter(self, "missing actor registration: " + params.getString("spawnId"));
        }
        return SCRIPT_CONTINUE;
    }

    public int releaseAxkvaMin(obj_id self, dictionary params) throws InterruptedException
    {
        if (isCurrentRun(self, params) && hasObjVar(self, encounter.VAR_AXKVA_RELEASE_PENDING) && !hasObjVar(self, encounter.VAR_AXKVA_RELEASED))
        {
            fireSequencerTrigger(self, "hm_release_axkva_min");
            messageTo(self, "finalizeAxkvaMinRelease", trial.getSessionDict(self, VAR_PREFIX), 0.5f, false);
        }
        return SCRIPT_CONTINUE;
    }

    public int finalizeAxkvaMinRelease(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isCurrentRun(self, params) || !hasObjVar(self, encounter.VAR_AXKVA_RELEASE_PENDING) || hasObjVar(self, encounter.VAR_AXKVA_RELEASED))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id axkva = getObjIdObjVar(self, encounter.VAR_ACTORS + ".axkva_min");
        if (!isIdValid(axkva) || !exists(axkva) || ai_lib.isDead(axkva))
        {
            resetEncounter(self, "Axkva disappeared before release");
            return SCRIPT_CONTINUE;
        }
        removeObjVar(self, encounter.VAR_AXKVA_RELEASE_PENDING);
        setObjVar(self, encounter.VAR_AXKVA_RELEASED, 1);
        setInvulnerable(axkva, false);
        encounter.updateAxkvaSisterProtection(axkva);
        addPlayersToHateList(self, axkva);
        return SCRIPT_CONTINUE;
    }

    public int OnReceivedItem(obj_id self, obj_id srcContainer, obj_id transferer, obj_id item) throws InterruptedException
    {
        return SCRIPT_CONTINUE;
    }

    private void initializeSisterPhase(obj_id self) throws InterruptedException
    {
        if (hasObjVar(self, encounter.VAR_SISTER_PHASE_STARTED))
        {
            return;
        }
        setObjVar(self, encounter.VAR_SISTER_PHASE_STARTED, 1);
        String[] sisters = { "lelli_hi", "kimaru", "suin_chalo" };
        for (int i = sisters.length - 1; i > 0; i--)
        {
            int swap = rand(0, i);
            String tmp = sisters[i];
            sisters[i] = sisters[swap];
            sisters[swap] = tmp;
        }
        setObjVar(self, encounter.VAR_SISTER_QUEUE, sisters);
    }

    private void clearEncounterState(obj_id self) throws InterruptedException
    {
        trial.bumpSession(self, VAR_PREFIX);
        removeObjVar(self, VAR_PREFIX);
    }

    private boolean isCurrentRun(obj_id self, dictionary params) throws InterruptedException
    {
        return encounter.isRunning(self) && params != null && params.containsKey(trial.MESSAGE_SESSION + "." + VAR_PREFIX) && trial.verifySession(self, params, VAR_PREFIX);
    }

    private void resetEncounter(obj_id self, String reason) throws InterruptedException
    {
        if (!encounter.isRunning(self))
        {
            return;
        }
        clearEncounterState(self);
        setObjVar(self, encounter.VAR_RUNNING, 1);
        CustomerServiceLog("instance-axkva_min_hard_mode", "Resetting encounter in " + self + ": " + reason + ".");
        messageTo(self, "restartSpawn", trial.getSessionDict(self), 0.0f, false);
    }

    private void fireSequencerTrigger(obj_id self, String triggerName) throws InterruptedException
    {
        dictionary trigger = trial.getSessionDict(self);
        trigger.put("triggerName", triggerName);
        trigger.put("triggerType", "triggerId");
        messageTo(self, "triggerFired", trigger, 0.0f, false);
    }

    private void clearSisterRequest(obj_id self) throws InterruptedException
    {
        removeObjVar(self, encounter.VAR_SISTER_REQUEST_PENDING);
        removeObjVar(self, encounter.VAR_SISTER_REQUEST_SOURCE);
        removeObjVar(self, encounter.VAR_SISTER_REQUEST_REASON);
        removeObjVar(self, encounter.VAR_SISTER_REQUEST_DEATHS);
    }

    private void clearPendingSister(obj_id self) throws InterruptedException
    {
        removeObjVar(self, encounter.VAR_PENDING_SISTER_SPAWN);
        removeObjVar(self, encounter.VAR_PENDING_SISTER_INDEX);
        removeObjVar(self, encounter.VAR_PENDING_SISTER_REASON);
        removeObjVar(self, encounter.VAR_PENDING_SISTER_SOURCE);
        removeObjVar(self, encounter.VAR_PENDING_SISTER_DEATHS);
    }

    private void scheduleRegistrationTimeout(obj_id self, String spawnId) throws InterruptedException
    {
        dictionary timeout = trial.getSessionDict(self, VAR_PREFIX);
        timeout.put("spawnId", spawnId);
        messageTo(self, "actorRegistrationTimeout", timeout, 6.0f, false);
    }

    private void addPlayersToHateList(obj_id self, obj_id boss) throws InterruptedException
    {
        obj_id[] players = trial.getPlayersInDungeon(self);
        if (players == null || players.length == 0)
        {
            return;
        }
        for (obj_id player : players)
        {
            if (isIdValid(player) && exists(player) && !ai_lib.isDead(player))
            {
                addHate(boss, player, 1.0f);
            }
        }
    }

    private boolean hasLivingPlayer(obj_id self) throws InterruptedException
    {
        obj_id[] players = trial.getPlayersInDungeon(self);
        if (players == null || players.length == 0)
        {
            return false;
        }
        for (obj_id player : players)
        {
            if (isIdValid(player) && exists(player) && !isIncapacitated(player) && !isDead(player))
            {
                return true;
            }
        }
        return false;
    }
}
