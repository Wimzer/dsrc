package script.theme_park.heroic.axkva_min_hard_mode;

import script.*;
import script.library.ai_lib;
import script.library.create;
import script.library.trial;
import script.library.utils;

public class illusions extends script.base_script
{
    public static final String OWNER = "amhm_illusion_owner";
    public static final String ATTEMPT = "amhm_illusion_attempt";
    public static final String POOL = "amhm_illusion_pool";

    public static void cleanupBrokenPools(obj_id top) throws InterruptedException
    {
        obj_id[] objects = trial.getAllObjectsInDungeon(top);
        if (objects == null)
        {
            return;
        }
        for (obj_id object : objects)
        {
            if (isIdValid(object) && exists(object)
                    && "object/static/item/item_kimaru_flame_core.iff".equals(getTemplateName(object))
                    && hasObjVar(object, OWNER) && hasObjVar(object, ATTEMPT)
                    && !hasScript(object, "theme_park.heroic.axkva_min_hard_mode.false_hazard"))
            {
                // Only cores left half-initialized by the prior setMaster exception.
                // Real fire trackers do not carry either of these illusion markers.
                destroyObject(object);
            }
        }
    }

    public static boolean isKimaru(obj_id actor) throws InterruptedException
    {
        obj_id top = trial.getTop(actor);
        return encounter.isRunning(top) && "kimaru".equals(getStringObjVar(actor, "spawn_id"))
                && hasScript(actor, "theme_park.heroic.axkva_min_hard_mode.kimaru")
                && getIntObjVar(actor, encounter.VAR_ACTOR_SESSION) == trial.getSession(top, encounter.VAR_PREFIX);
    }

    public static void configure(obj_id hazard, obj_id owner) throws InterruptedException
    {
        obj_id top = trial.getTop(owner);
        setObjVar(hazard, OWNER, owner);
        setObjVar(hazard, ATTEMPT, trial.getSession(top, encounter.VAR_PREFIX));
        trial.markAsTempObject(hazard, true);
        trial.setParent(top, hazard, false);
    }

    public static void spawnStorms(obj_id owner, location[] points, obj_id[] hateList) throws InterruptedException
    {
        if (!isKimaru(owner) || hateList == null || hateList.length == 0)
        {
            return;
        }
        int first = rand(0, points.length - 1);
        for (int i = 0; i < 2; i++)
        {
            location loc = points[(first + i * 2) % points.length];
            obj_id storm = create.object("kimaru_false_storm_hard_mode", loc, false);
            if (isIdValid(storm))
            {
                configure(storm, owner);
                // setMaster is creature-only; static fire cores use the owner objvar.
                setMaster(storm, owner);
                // Both rows retain the shared force_storm template's display name.
                // There is no mob/creature_names:kimaru_force_storm localization key.
                utils.setScriptVar(storm, trial.KIMARU_HATE_LIST, hateList);
                playClientEffectLoc(hateList[0], "appearance/pt_kimaru_force_storm_birth.prt", loc, 0.0f);
            }
        }
    }

    public static location poolLocation(obj_id owner, location real, obj_id[] targets) throws InterruptedException
    {
        obj_id top = trial.getTop(owner);
        obj_id room = getCellId(top, "r2");
        if (targets != null && targets.length > 0)
        {
            int first = rand(0, targets.length - 1);
            for (int i = 0; i < targets.length; i++)
            {
                obj_id target = targets[(first + i) % targets.length];
                if (!isIdValid(target) || !exists(target) || ai_lib.isDead(target) || trial.getTop(target) != top)
                {
                    continue;
                }
                location loc = getLocation(target);
                if (loc.cell == room && real.cell == room && getDistance(loc, real) >= 3.0f)
                {
                    return loc;
                }
            }
        }
        // Use the interior floor query. isValidLocation() is a native no-op here.
        for (int i = 0; i < 32; i++)
        {
            location loc = getGoodLocation(top, "r2");
            if (loc != null && loc.cell == room && real.cell == room)
            {
                float distance = getDistance(loc, real);
                if (distance >= 3.0f && distance <= 12.0f)
                {
                    return loc;
                }
            }
        }
        return null;
    }

    public static void spawnPool(obj_id owner, location real, obj_id[] targets) throws InterruptedException
    {
        if (!isKimaru(owner))
        {
            return;
        }
        location loc = poolLocation(owner, real, targets);
        if (loc == null)
        {
            CustomerServiceLog("instance-axkva_min_hard_mode", "No separated floor location for Kimaru false pool in " + trial.getTop(owner));
            return;
        }
        combat_engine.combat_data data = combat_engine.getCombatData("kimaru_flame_patch");
        obj_id pool = createObject(data.delayAttackEggTemplate, loc);
        if (!isIdValid(pool))
        {
            return;
        }
        configure(pool, owner);
        setObjVar(pool, POOL, 1);
        setObjVar(pool, "illusion_effect", data.delayAttackParticle);
        setObjVar(pool, "illusion_interval", data.delayAttackInterval);
        setObjVar(pool, "illusion_initial", data.initialDelayAttackInterval >= 0 ? data.initialDelayAttackInterval : data.delayAttackInterval);
        setObjVar(pool, "illusion_loops", data.delayAttackLoops);
        // Deliberately never attach combat_delayed_tracker: this object only presents particles.
        attachScript(pool, "theme_park.heroic.axkva_min_hard_mode.false_hazard");
    }
}
