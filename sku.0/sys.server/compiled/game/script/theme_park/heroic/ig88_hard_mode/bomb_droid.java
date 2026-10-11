package script.theme_park.heroic.ig88_hard_mode;

import script.dictionary;
import script.library.ai_lib;
import script.library.hue;
import script.library.stealth;
import script.library.trial;
import script.library.utils;
import script.obj_id;

public class bomb_droid extends script.base_script
{
    public int OnAttach(obj_id self) throws InterruptedException
    {
        messageTo(self, "colorize", null, 0.25f, true);
        messageTo(self, "setBombDroidMaxHealth", null, 0.25f, false);
        return SCRIPT_CONTINUE;
    }

    public int colorize(obj_id self, dictionary params) throws InterruptedException
    {
        hue.setColor(self, hue.INDEX_1, 9);
        hue.setColor(self, hue.INDEX_2, 15);
        hue.setTexture(self, 1, 7);
        return SCRIPT_CONTINUE;
    }

    public int setBombDroidMaxHealth(obj_id self, dictionary params) throws InterruptedException
    {
        if (!hasObjVar(self, "bombDroidMaxHealthSet"))
        {
            setMaxAttrib(self, HEALTH, 8000);
            setAttrib(self, HEALTH, 8000);
            setObjVar(self, "bombDroidMaxHealthSet", 1);
        }
        return SCRIPT_CONTINUE;
    }

    public int findTarget(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || ai_lib.isDead(self))
        {
            return SCRIPT_CONTINUE;
        }
        if (getMovementPercent(self) >= 1.0f)
        {
            setMovementPercent(self, 0.5f + rand(0.0f, 0.1f));
        }
        if (isInvulnerable(self))
        {
            setInvulnerable(self, false);
        }
        obj_id dungeon = getTopMostContainer(self);
        obj_id target = utils.getObjIdScriptVar(self, "target");
        if (!isIdValid(dungeon))
        {
            return SCRIPT_CONTINUE;
        }
        setObjVar(self, "ai.combat.oneShotAction", "ig88_hard_bomb_explode");
        dictionary sessionDict = new dictionary();
        if (!isIdValid(target) || isDead(target) || stealth.hasInvisibleBuff(target))
        {
            obj_id[] targets = trial.getNonStealthedTargetsInCell(dungeon, "r1");
            if (targets == null || targets.length <= 0)
            {
                messageTo(dungeon, "ig88_failed", controller.getControllerSessionDict(dungeon), 0, false);
                return SCRIPT_CONTINUE;
            }
            obj_id closest = trial.getClosest(self, targets);
            obj_id secondClosest = trial.getSecondClosest(self, targets);
            target = isIdValid(closest) && isIdValid(secondClosest) && rand(0, 1) == 1 ? secondClosest : closest;
        }
        if (!isIdValid(target))
        {
            messageTo(self, "findTarget", sessionDict, 1.0f, false);
            return SCRIPT_CONTINUE;
        }
        if (utils.getDistance2D(getLocation(target), getLocation(self)) < 5)
        {
            if (!utils.hasScriptVar(self, "exploded"))
            {
                queueCommand(self, getStringCrc("ig88_hard_bomb_explode"), target, "", COMMAND_PRIORITY_DEFAULT);
            }
            else
            {
                dictionary messageParams = new dictionary();
                messageParams.put("npc", self);
                messageTo(dungeon, "killNPC", messageParams, 0, false);
                return SCRIPT_CONTINUE;
            }
            messageTo(self, "findTarget", sessionDict, 1.0f, false);
            return SCRIPT_CONTINUE;
        }
        setHate(self, target, 5000);
        utils.setScriptVar(self, "target", target);
        messageTo(self, "findTarget", sessionDict, 1.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int OnExitedCombat(obj_id self) throws InterruptedException
    {
        messageTo(self, "findTarget", new dictionary(), 0.25f, true);
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
            messageTo(dungeon, "bombDroidDied", dict, 0.0f, false);
        }
    }
}
