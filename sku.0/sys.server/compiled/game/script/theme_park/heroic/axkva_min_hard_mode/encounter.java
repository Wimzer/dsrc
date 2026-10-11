package script.theme_park.heroic.axkva_min_hard_mode;

import script.library.ai_lib;
import script.library.buff;
import script.library.create;
import script.library.trial;
import script.library.utils;
import script.location;
import script.obj_id;
import script.string_id;
import script.systems.combat.combat_actions;

public class encounter extends script.base_script
{
    public static final String VAR_PREFIX = "axkva_min_hard_mode";
    public static final String VAR_RUNNING = VAR_PREFIX + ".running";
    public static final String VAR_ACTORS = VAR_PREFIX + ".actors";
    public static final String VAR_DEAD = VAR_PREFIX + ".dead";
    public static final String VAR_ACTOR_SESSION = VAR_PREFIX + ".actor_session";
    public static final String[] SISTERS = { "nandina", "lelli_hi", "kimaru", "suin_chalo" };
    public static final String VAR_SISTER_DEATHS = VAR_PREFIX + ".sister_deaths";
    public static final String VAR_TOTAL_SISTERS_SPAWNED = VAR_PREFIX + ".total_sisters_spawned";
    public static final String VAR_SISTER_QUEUE = VAR_PREFIX + ".sister_queue";
    public static final String VAR_SISTER_QUEUE_INDEX = VAR_PREFIX + ".sister_queue_index";
    public static final String VAR_SISTER_PHASE_STARTED = VAR_PREFIX + ".sister_phase_started";
    public static final String VAR_AXKVA_SPAWNED = VAR_PREFIX + ".axkva_spawned";
    public static final String VAR_DEATH_REPORTED = VAR_PREFIX + ".death_reported";
    public static final String VAR_SPAWN_TRIGGERED_66 = VAR_PREFIX + ".spawn_triggered_66";
    public static final String VAR_SPAWN_TRIGGERED_33 = VAR_PREFIX + ".spawn_triggered_33";
    public static final String VAR_LAST_ADD_ASPECT = VAR_PREFIX + ".last_add_aspect";
    public static final String VAR_SUIN_OWNER = VAR_PREFIX + ".suin_owner";
    public static final String VAR_SISTER_REQUEST_PENDING = VAR_PREFIX + ".sister_request_pending";
    public static final String VAR_SISTER_REQUEST_SOURCE = VAR_PREFIX + ".sister_request_source";
    public static final String VAR_SISTER_REQUEST_REASON = VAR_PREFIX + ".sister_request_reason";
    public static final String VAR_SISTER_REQUEST_DEATHS = VAR_PREFIX + ".sister_request_deaths";
    public static final String VAR_PENDING_SISTER_SPAWN = VAR_PREFIX + ".pending_sister_spawn";
    public static final String VAR_PENDING_SISTER_INDEX = VAR_PREFIX + ".pending_sister_index";
    public static final String VAR_PENDING_SISTER_REASON = VAR_PREFIX + ".pending_sister_reason";
    public static final String VAR_PENDING_SISTER_SOURCE = VAR_PREFIX + ".pending_sister_source";
    public static final String VAR_PENDING_SISTER_DEATHS = VAR_PREFIX + ".pending_sister_deaths";
    public static final String VAR_AXKVA_REQUEST_PENDING = VAR_PREFIX + ".axkva_request_pending";
    public static final String VAR_PENDING_AXKVA_SPAWN = VAR_PREFIX + ".pending_axkva_spawn";
    public static final String VAR_AXKVA_RELEASE_PENDING = VAR_PREFIX + ".axkva_release_pending";
    public static final String VAR_AXKVA_RELEASED = VAR_PREFIX + ".axkva_released";
    public static final String VAR_COMPLETE = VAR_PREFIX + ".complete";
    public static final String AXKVA_CONTAGION = "axkva_hard_contagion";
    public static final String AXKVA_CONTAGION_RECOURSE = "axkva_hard_contagion_recourse";
    public static final String AXKVA_CONTAGION_BOMB_COMMAND = "axkva_hard_contagion_bomb";
    public static final String RAGE = "axkva_hard_grieving_rage_1";
    public static final String RAGE_2 = "axkva_hard_grieving_rage_2";
    public static final String RAGE_3 = "axkva_hard_grieving_rage_3";
    public static final String RAGE_DAMAGE_MOD = "axkva_hard_grieving_rage_damage";
    public static final String AXKVA_SISTER_PROTECTION = "axkva_hard_coven_protection";
    public static final int AXKVA_MAX_ACTIVE_ASPECT_ADDS = 3;
    public static final String STRENGTH = "axkva_hard_strength_from_the_grave";
    public static final String IMMUNE_RANGED = "axkva_hard_immune_ranged";
    public static final String IMMUNE_MELEE = "axkva_hard_immune_melee";
    public static final String[] ASPECTS = { "heat_aspect", "acid_aspect", "elec_aspect", "cold_aspect" };
    public static final String REASON_SISTER_66 = "sister66";
    public static final String REASON_SISTER_33 = "sister33";
    public static final int TOTAL_SISTERS = 4;

    public static boolean isRunning(obj_id top) throws InterruptedException
    {
        return isIdValid(top) && hasObjVar(top, VAR_RUNNING) && !hasObjVar(top, VAR_COMPLETE);
    }

    public static void prepareActorRegistration(obj_id self) throws InterruptedException
    {
        obj_id top = trial.getTop(self);
        setObjVar(self, VAR_ACTOR_SESSION, trial.getSession(top, VAR_PREFIX));
        // The sequencer assigns spawn_id after OnAttach returns.
        messageTo(self, "registerHardModeActor", trial.getSessionDict(top, VAR_PREFIX), 1.0f, false);
    }

    public static void registerActor(obj_id self, script.dictionary params) throws InterruptedException
    {
        obj_id top = trial.getTop(self);
        if (!isRunning(top) || params == null || !params.containsKey(trial.MESSAGE_SESSION + "." + VAR_PREFIX) || !trial.verifySession(top, params, VAR_PREFIX) || !hasObjVar(self, "spawn_id"))
        {
            return;
        }
        params.put("actor", self);
        params.put("spawnId", getStringObjVar(self, "spawn_id"));
        messageTo(top, "actorReady", params, 0.0f, false);
    }

    public static void applyCurrentRage(obj_id self) throws InterruptedException
    {
        obj_id top = trial.getTop(self);
        if (!isRunning(top) || !hasObjVar(self, VAR_ACTOR_SESSION) || getIntObjVar(self, VAR_ACTOR_SESSION) != trial.getSession(top, VAR_PREFIX))
        {
            return;
        }
        buff.applyBuff(self, STRENGTH, -1.0f);
        applyRage(self, getSisterDeaths(self), false);
    }

    public static void startWipeCheck(obj_id self) throws InterruptedException
    {
        obj_id top = trial.getTop(self);
        if (isRunning(top) && hasObjVar(self, VAR_ACTOR_SESSION) && getIntObjVar(self, VAR_ACTOR_SESSION) == trial.getSession(top, VAR_PREFIX))
        {
            messageTo(top, "startWipeCheck", trial.getSessionDict(top, VAR_PREFIX), 0.0f, false);
        }
    }

    public static void handleSisterDamaged(obj_id self) throws InterruptedException
    {
        float healthRatio = (float)getHealth(self) / (float)getMaxHealth(self);
        obj_id top = trial.getTop(self);
        if (!isRunning(top) || !hasObjVar(self, VAR_ACTOR_SESSION) || getIntObjVar(self, VAR_ACTOR_SESSION) != trial.getSession(top, VAR_PREFIX) || getObjIdObjVar(top, VAR_ACTORS + "." + getStringObjVar(self, "spawn_id")) != self)
        {
            return;
        }
        String thresholdReason = getSisterThresholdReason(self, healthRatio);
        if (thresholdReason == null)
        {
            return;
        }
        requestNextSister(top, self, thresholdReason);
    }

    public static String getSisterThresholdReason(obj_id self, float healthRatio) throws InterruptedException
    {
        if (healthRatio <= 0.66f && !hasObjVar(self, VAR_SPAWN_TRIGGERED_66))
        {
            return REASON_SISTER_66;
        }
        if (healthRatio <= 0.33f && !hasObjVar(self, VAR_SPAWN_TRIGGERED_33))
        {
            return REASON_SISTER_33;
        }
        return null;
    }

    public static boolean canSpawnNextSister(obj_id top) throws InterruptedException
    {
        return isRunning(top) && !hasObjVar(top, VAR_SISTER_REQUEST_PENDING) && !hasObjVar(top, VAR_PENDING_SISTER_SPAWN) && getIntObjVar(top, VAR_TOTAL_SISTERS_SPAWNED) < TOTAL_SISTERS && countActiveSisters(top, obj_id.NULL_ID) < 3;
    }

    public static boolean requestNextSister(obj_id top, obj_id source, String reason) throws InterruptedException
    {
        if (!isIdValid(top) || !canSpawnNextSister(top))
        {
            return false;
        }
        setObjVar(top, VAR_SISTER_REQUEST_PENDING, 1);
        setObjVar(top, VAR_SISTER_REQUEST_SOURCE, source);
        setObjVar(top, VAR_SISTER_REQUEST_REASON, reason == null ? "" : reason);
        setObjVar(top, VAR_SISTER_REQUEST_DEATHS, getSisterDeathsFromTop(top));
        messageTo(top, "spawnNextSister", trial.getSessionDict(top, VAR_PREFIX), 0.0f, false);
        return true;
    }

    public static void handleSisterDeath(obj_id self) throws InterruptedException
    {
        obj_id top = trial.getTop(self);
        if (!isRunning(top) || !hasObjVar(self, VAR_ACTOR_SESSION) || getIntObjVar(self, VAR_ACTOR_SESSION) != trial.getSession(top, VAR_PREFIX) || hasObjVar(self, VAR_DEATH_REPORTED))
        {
            return;
        }
        setObjVar(self, VAR_DEATH_REPORTED, 1);
        script.dictionary death = trial.getSessionDict(top, VAR_PREFIX);
        death.put("actor", self);
        death.put("spawnId", getStringObjVar(self, "spawn_id"));
        messageTo(top, "sisterDied", death, 0.0f, false);
    }

    public static void updateActiveSisterCount(obj_id top, obj_id deadSister) throws InterruptedException
    {
        if (!hasObjVar(top, VAR_SISTER_PHASE_STARTED))
        {
            return;
        }
        int active = countActiveSisters(top, deadSister);
        obj_id axkva = getObjIdObjVar(top, VAR_ACTORS + ".axkva_min");
        if (isIdValid(axkva))
        {
            updateAxkvaSisterProtection(axkva, active > 0);
        }
        tryActivateAxkvaMin(top, deadSister);
    }

    public static int getSisterDeaths(obj_id self) throws InterruptedException
    {
        return getSisterDeathsFromTop(trial.getTop(self));
    }

    public static int getSisterDeathsFromTop(obj_id top) throws InterruptedException
    {
        if (!isIdValid(top) || !hasObjVar(top, VAR_SISTER_DEATHS))
        {
            return 0;
        }
        return getIntObjVar(top, VAR_SISTER_DEATHS);
    }

    public static void updateAxkvaSisterProtection(obj_id axkva) throws InterruptedException
    {
        if (!isIdValid(axkva) || !exists(axkva) || ai_lib.isDead(axkva))
        {
            return;
        }
        updateAxkvaSisterProtection(axkva, countActiveSisters(trial.getTop(axkva), obj_id.NULL_ID) > 0);
    }

    public static void updateAxkvaSisterProtection(obj_id axkva, boolean sisterAlive) throws InterruptedException
    {
        if (!isIdValid(axkva) || !exists(axkva) || ai_lib.isDead(axkva))
        {
            return;
        }
        if (sisterAlive)
        {
            if (!buff.hasBuff(axkva, AXKVA_SISTER_PROTECTION))
            {
                buff.applyBuff(axkva, AXKVA_SISTER_PROTECTION, -1.0f);
            }
        }
        else
        {
            buff.removeBuff(axkva, AXKVA_SISTER_PROTECTION);
        }
    }

    public static void applyRageToActiveBosses(obj_id top, int deaths, obj_id deadSister) throws InterruptedException
    {
        String[] bosses = { "nandina", "lelli_hi", "kimaru", "suin_chalo", "axkva_min" };
        for (String spawnId : bosses)
        {
            obj_id object = getObjIdObjVar(top, VAR_ACTORS + "." + spawnId);
            if (!isIdValid(object) || !exists(object) || object == deadSister || hasObjVar(top, VAR_DEAD + "." + spawnId) || ai_lib.isDead(object))
            {
                continue;
            }
            if (isSister(spawnId))
            {
                applyRage(object, deaths, true);
            }
            else if ("axkva_min".equals(spawnId))
            {
                applyRage(object, deaths, false);
            }
        }
    }

    public static void applyRage(obj_id target, int deaths, boolean healToFull) throws InterruptedException
    {
        int stacks = Math.min(3, Math.max(0, deaths));
        buff.removeBuff(target, RAGE);
        buff.removeBuff(target, RAGE_2);
        buff.removeBuff(target, RAGE_3);
        removeAttribOrSkillModModifier(target, RAGE_DAMAGE_MOD);
        if (stacks > 0)
        {
            int damageBonus = (stacks - 1) * 50;
            String rageBuff = RAGE;
            if (stacks == 2)
            {
                rageBuff = RAGE_2;
            }
            else if (stacks >= 3)
            {
                rageBuff = RAGE_3;
            }
            buff.applyBuff(target, null, getStringCrc(rageBuff.toLowerCase()), -1.0f, 0.0f, stacks);
            int visibleBuffContribution = damageBonus * stacks;
            int damageOffset = damageBonus - visibleBuffContribution;
            if (damageOffset != 0)
            {
                addSkillModModifier(target, RAGE_DAMAGE_MOD, "combat_multiply_damage_dealt", damageOffset, -1.0f, false, false);
            }
        }
        if (healToFull)
        {
            int toHeal = getMaxHealth(target) - getHealth(target);
            if (toHeal > 0)
            {
                addToHealth(target, toHeal);
            }
            removeObjVar(target, VAR_SPAWN_TRIGGERED_66);
            removeObjVar(target, VAR_SPAWN_TRIGGERED_33);
        }
    }

    public static boolean isSister(String spawnId) throws InterruptedException
    {
        return "nandina".equals(spawnId) || "lelli_hi".equals(spawnId) || "kimaru".equals(spawnId) || "suin_chalo".equals(spawnId);
    }

    public static void tryActivateAxkvaMin(obj_id top) throws InterruptedException
    {
        tryActivateAxkvaMin(top, obj_id.NULL_ID);
    }

    public static void tryActivateAxkvaMin(obj_id top, obj_id ignoreSister) throws InterruptedException
    {
        if (!isRunning(top) || getIntObjVar(top, VAR_TOTAL_SISTERS_SPAWNED) < TOTAL_SISTERS)
        {
            return;
        }
        int active = countActiveSisters(top, ignoreSister);
        if (!hasObjVar(top, VAR_AXKVA_SPAWNED))
        {
            if (active == 1)
            {
                spawnAxkvaMin(top);
            }
            return;
        }
        if (active <= 1)
        {
            releaseAxkvaMin(top);
        }
    }

    public static void applyRandomCloneImmunity(obj_id clone) throws InterruptedException
    {
        if (!isIdValid(clone))
        {
            return;
        }
        if (rand(0, 1) == 0)
        {
            buff.applyBuff(clone, IMMUNE_RANGED, -1.0f);
        }
        else
        {
            buff.applyBuff(clone, IMMUNE_MELEE, -1.0f);
        }
    }

    public static void applyCurrentAddVulnerability(obj_id target) throws InterruptedException
    {
        obj_id top = trial.getTop(target);
        if (isIdValid(top) && hasObjVar(top, VAR_LAST_ADD_ASPECT))
        {
            applyAspectVulnerability(target, getStringObjVar(top, VAR_LAST_ADD_ASPECT));
        }
    }

    public static int countActiveAspectAdds(obj_id owner) throws InterruptedException
    {
        if (!isIdValid(owner) || !exists(owner))
        {
            return 0;
        }
        obj_id top = trial.getTop(owner);
        if (!isIdValid(top))
        {
            return 0;
        }
        obj_id[] children = trial.getAllChildrenInDungeon(top, top);
        if (children == null || children.length == 0)
        {
            return 0;
        }
        int active = 0;
        for (obj_id child : children)
        {
            if (!isIdValid(child) || !exists(child) || ai_lib.isDead(child) || !utils.hasScriptVar(child, VAR_SUIN_OWNER))
            {
                continue;
            }
            if (utils.getObjIdScriptVar(child, VAR_SUIN_OWNER) == owner)
            {
                active++;
            }
        }
        return active;
    }

    public static obj_id spawnAspectAdd(obj_id owner, location loc) throws InterruptedException
    {
        String aspect = ASPECTS[rand(0, ASPECTS.length - 1)];
        obj_id creature = create.object("heroic_axkva_" + aspect, loc);
        if (!isIdValid(creature))
        {
            return obj_id.NULL_ID;
        }

        setName(creature, "@" + new string_id("mob/creature_names", aspect));
        applyAspectVulnerability(creature, aspect);
        rememberLatestAddAspect(trial.getTop(owner), aspect);
        applyAspectVulnerability(owner, getOppositeAspect(aspect));
        applyRandomCloneImmunity(creature);
        utils.setScriptVar(creature, "immunity.dot.all", true);
        utils.setScriptVar(creature, "aspect", aspect);
        utils.setScriptVar(creature, VAR_SUIN_OWNER, owner);
        trial.markAsTempObject(creature, true);
        attachScript(creature, "theme_park.heroic.axkva_min_hard_mode.suin_guard");
        trial.setParent(trial.getTop(owner), creature, false);
        addOwnerHateList(owner, creature);
        return creature;
    }

    public static void rememberLatestAddAspect(obj_id top, String aspect) throws InterruptedException
    {
        if (!isIdValid(top) || aspect == null || aspect.length() == 0)
        {
            return;
        }
        setObjVar(top, VAR_LAST_ADD_ASPECT, aspect);
    }

    public static void applyAspectVulnerability(obj_id target, String aspect) throws InterruptedException
    {
        if (!isIdValid(target) || aspect == null || aspect.length() == 0)
        {
            return;
        }
        for (String existing : ASPECTS)
        {
            buff.removeBuff(target, existing);
        }
        buff.applyBuff(target, aspect, -1.0f);
    }

    public static String getOppositeAspect(String aspect) throws InterruptedException
    {
        if ("heat_aspect".equals(aspect))
        {
            return "cold_aspect";
        }
        if ("cold_aspect".equals(aspect))
        {
            return "heat_aspect";
        }
        if ("acid_aspect".equals(aspect))
        {
            return "elec_aspect";
        }
        if ("elec_aspect".equals(aspect))
        {
            return "acid_aspect";
        }
        return aspect;
    }

    public static void addOwnerHateList(obj_id owner, obj_id add) throws InterruptedException
    {
        obj_id[] hateList = getHateList(owner);
        if (hateList == null || hateList.length == 0)
        {
            return;
        }
        for (obj_id target : hateList)
        {
            if (isIdValid(target) && !ai_lib.isDead(target))
            {
                setHate(add, target, 1.0f);
            }
        }
    }

    public static int countActiveSisters(obj_id top, obj_id ignoreSister) throws InterruptedException
    {
        int active = 0;
        for (String spawnId : SISTERS)
        {
            obj_id object = getObjIdObjVar(top, VAR_ACTORS + "." + spawnId);
            if (!isIdValid(object) || !exists(object) || object == ignoreSister || hasObjVar(top, VAR_DEAD + "." + spawnId) || ai_lib.isDead(object))
            {
                continue;
            }
            active++;
        }
        return active;
    }

    public static boolean hasActivePrimaryEnemy(obj_id top) throws InterruptedException
    {
        String[] actors = { "nandina", "gorvo", "lelli_hi", "kimaru", "suin_chalo", "axkva_min" };
        for (String spawnId : actors)
        {
            obj_id actor = getObjIdObjVar(top, VAR_ACTORS + "." + spawnId);
            if (isIdValid(actor) && exists(actor) && !hasObjVar(top, VAR_DEAD + "." + spawnId) && !ai_lib.isDead(actor))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPrimaryEnemyInCombat(obj_id top) throws InterruptedException
    {
        String[] actors = { "nandina", "gorvo", "lelli_hi", "kimaru", "suin_chalo", "axkva_min" };
        for (String spawnId : actors)
        {
            obj_id actor = getObjIdObjVar(top, VAR_ACTORS + "." + spawnId);
            if (isIdValid(actor) && exists(actor) && !hasObjVar(top, VAR_DEAD + "." + spawnId) && !ai_lib.isDead(actor) && (ai_lib.isInCombat(actor) || combat_actions.isHardModeAllyCloaked(actor) || combat_actions.isHardModeLelliAmbushing(actor)))
            {
                return true;
            }
        }
        return false;
    }

    public static void spawnAxkvaMin(obj_id top) throws InterruptedException
    {
        if (!isRunning(top) || hasObjVar(top, VAR_AXKVA_SPAWNED) || hasObjVar(top, VAR_AXKVA_REQUEST_PENDING) || hasObjVar(top, VAR_PENDING_AXKVA_SPAWN))
        {
            return;
        }
        setObjVar(top, VAR_AXKVA_REQUEST_PENDING, 1);
        messageTo(top, "spawnAxkvaMin", trial.getSessionDict(top, VAR_PREFIX), 0.0f, false);
    }

    public static void releaseAxkvaMin(obj_id top) throws InterruptedException
    {
        if (!isRunning(top) || hasObjVar(top, VAR_AXKVA_RELEASE_PENDING) || hasObjVar(top, VAR_AXKVA_RELEASED))
        {
            return;
        }
        setObjVar(top, VAR_AXKVA_RELEASE_PENDING, 1);
        messageTo(top, "releaseAxkvaMin", trial.getSessionDict(top, VAR_PREFIX), 0.0f, false);
    }

    public static obj_id getObjectBySpawnId(obj_id top, String spawnId) throws InterruptedException
    {
        obj_id[] objects = trial.getObjectsInDungeonWithObjVar(top, "spawn_id");
        if (objects == null || objects.length == 0)
        {
            return obj_id.NULL_ID;
        }
        for (obj_id object : objects)
        {
            if (isIdValid(object) && spawnId.equals(getStringObjVar(object, "spawn_id")))
            {
                return object;
            }
        }
        return obj_id.NULL_ID;
    }

    public static boolean isCurrentAxkva(obj_id axkva) throws InterruptedException
    {
        if (!isIdValid(axkva) || !exists(axkva) || ai_lib.isDead(axkva) || !hasScript(axkva, "theme_park.heroic.axkva_min_hard_mode.axkva"))
        {
            return false;
        }
        obj_id top = trial.getTop(axkva);
        return isRunning(top) && getObjIdObjVar(top, VAR_ACTORS + ".axkva_min") == axkva;
    }

}
