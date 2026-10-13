package script.theme_park.heroic.axkva_min_hard_mode;

import script.dictionary;
import script.library.ai_lib;
import script.library.trial;
import script.library.utils;
import script.obj_id;
import script.systems.combat.combat_actions;

public class suin_chalo extends script.theme_park.heroic.axkva_min.suin_chalo
{
    public int OnAttach(obj_id self) throws InterruptedException
    {
        setName(self, "Suin Chalo");
        encounter.prepareActorRegistration(self);
        trial.setHp(self, trial.HP_AXKVA_SUIN * 4);
        messageTo(self, "applyCurrentHardModeRage", null, 3.0f, false);
        return SCRIPT_CONTINUE;
    }
    public int applyCurrentHardModeRage(obj_id self, dictionary params) throws InterruptedException
    {
        encounter.applyCurrentRage(self);
        return SCRIPT_CONTINUE;
    }
    public int registerHardModeActor(obj_id self, dictionary params) throws InterruptedException
    {
        encounter.registerActor(self, params);
        return SCRIPT_CONTINUE;
    }
    public int OnIncapacitated(obj_id self, obj_id killer) throws InterruptedException
    {
        encounter.handleSisterDeath(self);
        return super.OnIncapacitated(self, killer);
    }
    public int OnEnteredCombat(obj_id self) throws InterruptedException
    {
        encounter.startWipeCheck(self);
        return super.OnEnteredCombat(self);
    }
    public int OnExitedCombat(obj_id self) throws InterruptedException
    {
        if (combat_actions.isHardModeAllyCloaked(self))
        {
            return SCRIPT_CONTINUE;
        }
        if (!ai_lib.isDead(self))
        {
            int toHeal = getMaxHealth(self) - getHealth(self);
            addToHealth(self, toHeal);
            String[] markers = { "spawn90", "spawn75", "spawn60", "spawn45", "spawn30" };
            utils.removeObjVarList(self, markers);
        }
        return SCRIPT_CONTINUE;
    }
    public int OnCreatureDamaged(obj_id self, obj_id attacker, obj_id weapon, int[] damage) throws InterruptedException
    {
        encounter.handleSisterDamaged(self);
        float ratio = (float)getHealth(self) / (float)getMaxHealth(self);
        if (ratio <= 0.35f)
        {
            spawnAspect(self, 30);
        }
        else if (ratio <= 0.45f)
        {
            spawnAspect(self, 45);
        }
        else if (ratio <= 0.60f)
        {
            spawnAspect(self, 60);
        }
        else if (ratio <= 0.75f)
        {
            spawnAspect(self, 75);
        }
        else if (ratio <= 0.90f)
        {
            spawnAspect(self, 90);
        }
        return SCRIPT_CONTINUE;
    }
    public void spawnAspect(obj_id self, int value) throws InterruptedException
    {
        if (hasObjVar(self, "spawn" + value))
        {
            return;
        }
        obj_id add = encounter.spawnAspectAdd(self, getLocation(self));
        if (isIdValid(add))
        {
            setObjVar(self, "spawn" + value, 1);
        }
    }
}
