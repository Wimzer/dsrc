package script.theme_park.heroic.ig88_hard_mode;

import script.dictionary;
import script.library.ai_lib;
import script.library.buff;
import script.library.hue;
import script.library.trial;
import script.library.utils;
import script.location;
import script.obj_id;

public class droideka extends script.base_script
{
    public static final String HOME = controller.VAR_PREFIX + ".droideka_home";
    public static final String DUG_IN_BUFF = "ig88_hard_dug_in";

    public int OnAttach(obj_id self) throws InterruptedException
    {
        messageTo(self, "initializeDroid", null, 0.25f, false);
        return SCRIPT_CONTINUE;
    }

    public int initializeDroid(obj_id self, dictionary params) throws InterruptedException
    {
        hue.setColor(self, hue.INDEX_1, 7);
        obj_id dungeon = getTopMostContainer(self);
        if (isIdValid(dungeon))
        {
            messageTo(dungeon, "droidekaPhaseStarted", controller.getControllerSessionDict(dungeon), 0.0f, false);
        }
        if (!hasObjVar(self, HOME))
        {
            setObjVar(self, HOME, getLocation(self));
        }
        messageTo(self, "secureDroidekaPosition", null, 1.0f, false);
        messageTo(self, "findTarget", null, 1.25f, false);
        return SCRIPT_CONTINUE;
    }

    public int secureDroidekaPosition(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || ai_lib.isDead(self))
        {
            return SCRIPT_CONTINUE;
        }
        setMovementPercent(self, 0.0f);
        setObjVar(self, "forceNoMovement", 1);
        if (buff.hasBuff(self, "co_position_secured"))
        {
            buff.removeBuff(self, "co_position_secured");
        }
        buff.applyBuff(self, DUG_IN_BUFF, -1.0f);
        return SCRIPT_CONTINUE;
    }

    public int findTarget(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || ai_lib.isDead(self))
        {
            return SCRIPT_CONTINUE;
        }
        holdPosition(self);
        obj_id dungeon = getTopMostContainer(self);
        if (!isIdValid(dungeon))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id[] targets = trial.getNonStealthedTargetsInCell(dungeon, "r1");
        if (targets == null || targets.length <= 0)
        {
            return SCRIPT_CONTINUE;
        }
        obj_id target = trial.getClosest(self, targets);
        if (!isIdValid(target))
        {
            messageTo(self, "findTarget", null, 1.0f, false);
            return SCRIPT_CONTINUE;
        }
        int lastShieldTime = utils.getIntScriptVar(self, "lastShieldTime");
        if (getGameTime() - lastShieldTime > 120)
        {
            utils.setScriptVar(self, "lastShieldTime", getGameTime());
            queueCommand(self, (-2086643447), self, "", COMMAND_PRIORITY_DEFAULT);
        }
        if (utils.getDistance2D(getLocation(self), getLocation(target)) <= 100.0f)
        {
            setObjVar(self, "ai.combat.oneShotAction", "ig88_hard_droideka_electrify");
        }
        setHate(self, target, 5000.0f);
        messageTo(self, "findTarget", null, 1.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int OnExitedCombat(obj_id self) throws InterruptedException
    {
        messageTo(self, "findTarget", null, 0.25f, false);
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

    private void holdPosition(obj_id self) throws InterruptedException
    {
        setMovementPercent(self, 0.0f);
        setObjVar(self, "forceNoMovement", 1);
        if (!hasObjVar(self, HOME))
        {
            setObjVar(self, HOME, getLocation(self));
            return;
        }
        location home = getLocationObjVar(self, HOME);
        if (home != null && utils.getDistance2D(getLocation(self), home) > 1.0f)
        {
            setLocation(self, home);
        }
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
            messageTo(dungeon, "droidekaDied", dict, 0.0f, false);
        }
    }
}
