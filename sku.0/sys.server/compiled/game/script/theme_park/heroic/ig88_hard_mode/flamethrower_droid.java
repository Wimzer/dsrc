package script.theme_park.heroic.ig88_hard_mode;

import script.dictionary;
import script.library.ai_lib;
import script.library.hue;
import script.library.trial;
import script.library.utils;
import script.location;
import script.obj_id;

public class flamethrower_droid extends script.base_script
{
    public static final String VAR_RESPAWN_RUNNER = controller.VAR_PREFIX + ".respawn_runner";
    public static final String VAR_STATS_INITIALIZED = controller.VAR_PREFIX + ".stats_initialized";
    public static final String VAR_COMBAT_STARTED = controller.VAR_PREFIX + ".combat_started";
    public static final String VAR_TRANSIT_STARTED = controller.VAR_PREFIX + ".transit_started";
    public static final String VAR_ARRIVED = controller.VAR_PREFIX + ".arrived";

    public int OnAttach(obj_id self) throws InterruptedException
    {
        obj_id dungeon = getTopMostContainer(self);
        if (isIdValid(dungeon))
        {
            messageTo(dungeon, "flamePhaseStarted", controller.getControllerSessionDict(dungeon), 0.0f, false);
        }
        messageTo(self, "colorize", null, 0.25f, false);
        messageTo(self, "initializeSpawn", null, 0.5f, false);
        return SCRIPT_CONTINUE;
    }

    public int initializeSpawn(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || ai_lib.isDead(self))
        {
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(self, VAR_RESPAWN_RUNNER))
        {
            initializeStats(self);
            stop(self);
            setMovementPercent(self, 0.0f);
            setObjVar(self, "forceNoMovement", 1);
            messageTo(self, "startRunnerTransit", null, 2.0f, false);
            return SCRIPT_CONTINUE;
        }
        messageTo(self, "findTarget", null, 0.25f, false);
        return SCRIPT_CONTINUE;
    }

    public int colorize(obj_id self, dictionary params) throws InterruptedException
    {
        hue.setColor(self, hue.INDEX_1, 9);
        hue.setColor(self, hue.INDEX_2, 14);
        hue.setTexture(self, 1, 9);
        return SCRIPT_CONTINUE;
    }

    public obj_id getRocketTarget(obj_id self) throws InterruptedException
    {
        obj_id dungeon = getTopMostContainer(self);
        if (!isIdValid(self) || !isIdValid(dungeon))
        {
            return obj_id.NULL_ID;
        }
        obj_id[] targets = trial.getNonStealthedTargetsInCell(dungeon, "r1");
        if (targets == null || targets.length <= 0)
        {
            return obj_id.NULL_ID;
        }
        obj_id target = targets[rand(0, targets.length - 1)];
        return isIdValid(target) ? target : obj_id.NULL_ID;
    }

    public void findNextTarget(obj_id self) throws InterruptedException
    {
        if (!isIdValid(self) || ai_lib.isDead(self) || utils.hasScriptVar(self, "flameThrowerUnequipped"))
        {
            return;
        }
        obj_id dungeon = getTopMostContainer(self);
        if (!isIdValid(dungeon))
        {
            return;
        }
        String spawnId = getStringObjVar(self, "spawn_id");
        boolean firstFlamer = controller.FLAME_A.equals(spawnId);
        location firingPoint = getFiringPoint(self, firstFlamer);
        if (utils.getDistance2D(firingPoint, getLocation(self)) > 2.0f)
        {
            setHomeLocation(self, firingPoint);
            pathTo(self, firingPoint);
            return;
        }
        arriveAtFiringPoint(self, firstFlamer);
    }

    public int startRunnerTransit(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || ai_lib.isDead(self) || !hasObjVar(self, VAR_RESPAWN_RUNNER) || hasObjVar(self, VAR_ARRIVED))
        {
            return SCRIPT_CONTINUE;
        }
        boolean firstFlamer = controller.FLAME_A.equals(getStringObjVar(self, "spawn_id"));
        location firingPoint = getFiringPoint(self, firstFlamer);
        removeObjVar(self, "forceNoMovement");
        setObjVar(self, VAR_TRANSIT_STARTED, 1);
        setMovementPercent(self, 1.5f);
        setMovementRun(self);
        setHomeLocation(self, firingPoint);
        pathTo(self, firingPoint);
        messageTo(self, "runnerTransitCheck", null, 4.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int runnerTransitCheck(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || ai_lib.isDead(self) || !hasObjVar(self, VAR_RESPAWN_RUNNER) || hasObjVar(self, VAR_ARRIVED))
        {
            return SCRIPT_CONTINUE;
        }
        boolean firstFlamer = controller.FLAME_A.equals(getStringObjVar(self, "spawn_id"));
        location firingPoint = getFiringPoint(self, firstFlamer);
        if (utils.getDistance2D(firingPoint, getLocation(self)) <= 2.0f)
        {
            arriveAtFiringPoint(self, firstFlamer);
            return SCRIPT_CONTINUE;
        }
        setMovementPercent(self, 1.5f);
        setMovementRun(self);
        pathTo(self, firingPoint);
        messageTo(self, "runnerTransitCheck", null, 4.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int OnMovePathComplete(obj_id self) throws InterruptedException
    {
        if (hasObjVar(self, VAR_RESPAWN_RUNNER) && hasObjVar(self, VAR_TRANSIT_STARTED) && !hasObjVar(self, VAR_ARRIVED))
        {
            boolean firstFlamer = controller.FLAME_A.equals(getStringObjVar(self, "spawn_id"));
            arriveAtFiringPoint(self, firstFlamer);
        }
        return SCRIPT_CONTINUE;
    }

    public int OnMovePathNotFound(obj_id self) throws InterruptedException
    {
        if (hasObjVar(self, VAR_RESPAWN_RUNNER) && hasObjVar(self, VAR_TRANSIT_STARTED) && !hasObjVar(self, VAR_ARRIVED))
        {
            messageTo(self, "startRunnerTransit", null, 1.0f, false);
        }
        return SCRIPT_CONTINUE;
    }

    private void arriveAtFiringPoint(obj_id self, boolean firstFlamer) throws InterruptedException
    {
        if (hasObjVar(self, VAR_ARRIVED))
        {
            return;
        }
        stop(self);
        removeObjVar(self, "run");
        removeObjVar(self, VAR_TRANSIT_STARTED);
        setMovementPercent(self, 0.0f);
        setObjVar(self, "forceNoMovement", 1);
        setObjVar(self, VAR_ARRIVED, 1);
        initializeStats(self);
        beginCombatRoutine(self);
        acquireMouseTarget(self, firstFlamer);
    }

    private void acquireMouseTarget(obj_id self, boolean firstFlamer) throws InterruptedException
    {
        obj_id dungeon = getTopMostContainer(self);
        if (!isIdValid(dungeon))
        {
            return;
        }
        setMovementPercent(self, 0.0f);
        obj_id target = controller.getObjectBySpawnId(dungeon, firstFlamer ? controller.MOUSE_A : controller.MOUSE_B);
        if (!isIdValid(target) || ai_lib.isDead(target))
        {
            messageTo(dungeon, "ensureFlameMouseDroids", controller.getControllerSessionDict(dungeon), 0.0f, false);
            return;
        }
        clearHateList(self);
        setHate(self, target, 1000000.0f);
        forceHateTarget(self, target);
    }

    private location getFiringPoint(obj_id self, boolean firstFlamer) throws InterruptedException
    {
        return new location(firstFlamer ? 5.0f : -5.0f, 0.0f, 10.0f, getLocation(self).area, getLocation(self).cell);
    }

    public int findTarget(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(self, VAR_RESPAWN_RUNNER))
        {
            if (hasObjVar(self, VAR_ARRIVED))
            {
                acquireMouseTarget(self, controller.FLAME_A.equals(getStringObjVar(self, "spawn_id")));
            }
            return SCRIPT_CONTINUE;
        }
        findNextTarget(self);
        if (!hasObjVar(self, VAR_ARRIVED))
        {
            messageTo(self, "findTarget", new dictionary(), 1.0f, false);
        }
        return SCRIPT_CONTINUE;
    }

    public int findRocketTarget(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || ai_lib.isDead(self))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id target = getRocketTarget(self);
        if (hasObjVar(self, "ai.combat.oneShotAction") && hasObjVar(self, "oneShotActionComplete"))
        {
            removeObjVar(self, "ai.combat.oneShotAction");
            removeObjVar(self, "oneShotActionComplete");
        }
        if (isIdValid(target) && !hasObjVar(self, "ai.combat.oneShotAction") && (float)getAttrib(self, ACTION) / getMaxAttrib(self, ACTION) >= 0.1f)
        {
            setObjVar(self, "ai.combat.oneShotAction", "ig88_hard_rocket_launch");
        }
        messageTo(self, "findRocketTarget", new dictionary(), 1 + rand(1, 2), false);
        return SCRIPT_CONTINUE;
    }

    public void destroyMouseDroids(obj_id dungeon) throws InterruptedException
    {
        obj_id mouseA = controller.getObjectBySpawnId(dungeon, controller.MOUSE_A);
        obj_id mouseB = controller.getObjectBySpawnId(dungeon, controller.MOUSE_B);
        if (isIdValid(mouseA))
        {
            destroyObject(mouseA);
        }
        if (isIdValid(mouseB))
        {
            destroyObject(mouseB);
        }
    }

    public int OnExitedCombat(obj_id self) throws InterruptedException
    {
        if (hasObjVar(self, VAR_ARRIVED))
        {
            acquireMouseTarget(self, controller.FLAME_A.equals(getStringObjVar(self, "spawn_id")));
        }
        else if (!hasObjVar(self, VAR_RESPAWN_RUNNER))
        {
            messageTo(self, "findTarget", null, 0.25f, false);
        }
        return SCRIPT_CONTINUE;
    }

    public int OnIncapacitated(obj_id self, obj_id killer) throws InterruptedException
    {
        reportDeath(self);
        return SCRIPT_CONTINUE;
    }

    public int OnDeath(obj_id self, obj_id killer, obj_id corpse) throws InterruptedException
    {
        reportDeath(self);
        return SCRIPT_CONTINUE;
    }

    private void initializeStats(obj_id self) throws InterruptedException
    {
        if (hasObjVar(self, VAR_STATS_INITIALIZED))
        {
            return;
        }
        if (isInvulnerable(self))
        {
            setInvulnerable(self, false);
        }
        setMaxAttrib(self, HEALTH, getMaxAttrib(self, HEALTH) * 2);
        setAttrib(self, HEALTH, getMaxAttrib(self, HEALTH));
        setObjVar(self, VAR_STATS_INITIALIZED, 1);
    }

    private void beginCombatRoutine(obj_id self) throws InterruptedException
    {
        if (hasObjVar(self, VAR_COMBAT_STARTED))
        {
            return;
        }
        setObjVar(self, VAR_COMBAT_STARTED, 1);
        messageTo(self, "findRocketTarget", new dictionary(), rand(1, 3), false);
    }

    private void reportDeath(obj_id self) throws InterruptedException
    {
        if (hasObjVar(self, controller.VAR_PREFIX + ".death_reported"))
        {
            return;
        }
        setObjVar(self, controller.VAR_PREFIX + ".death_reported", 1);
        obj_id dungeon = getTopMostContainer(self);
        if (isIdValid(dungeon) && hasObjVar(self, "spawn_id"))
        {
            dictionary dict = controller.getControllerSessionDict(dungeon);
            dict.put("spawnId", getStringObjVar(self, "spawn_id"));
            messageTo(dungeon, "flameDied", dict, 0.0f, false);
        }
    }
}
