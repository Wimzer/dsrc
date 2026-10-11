package script.theme_park.heroic.axkva_min_hard_mode;

import script.dictionary;
import script.library.trial;
import script.obj_id;
import script.systems.combat.combat_actions;

public class lelli_hi extends script.theme_park.heroic.axkva_min.lelli_hi
{
    public int OnAttach(obj_id self) throws InterruptedException
    {
        setName(self, "Lelli Hi");
        encounter.prepareActorRegistration(self);
        trial.setHp(self, trial.HP_AXKVA_LILLI_HI * 4);
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
        return SCRIPT_CONTINUE;
    }
    public int OnEnteredCombat(obj_id self) throws InterruptedException
    {
        encounter.startWipeCheck(self);
        return super.OnEnteredCombat(self);
    }
    public int OnExitedCombat(obj_id self) throws InterruptedException
    {
        if (combat_actions.isHardModeLelliAmbushing(self))
        {
            return SCRIPT_CONTINUE;
        }
        return super.OnExitedCombat(self);
    }
    public int OnCreatureDamaged(obj_id self, obj_id attacker, obj_id weapon, int[] damage) throws InterruptedException
    {
        encounter.handleSisterDamaged(self);
        return SCRIPT_CONTINUE;
    }
}
