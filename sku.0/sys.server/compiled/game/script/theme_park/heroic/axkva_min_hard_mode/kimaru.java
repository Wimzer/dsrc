package script.theme_park.heroic.axkva_min_hard_mode;

import script.dictionary;
import script.library.trial;
import script.obj_id;

public class kimaru extends script.theme_park.heroic.axkva_min.kimaru
{
    public int OnAttach(obj_id self) throws InterruptedException
    {
        setName(self, "Kimaru");
        encounter.prepareActorRegistration(self);
        trial.setHp(self, trial.HP_AXKVA_KIMARU * 4);
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
    public int OnCreatureDamaged(obj_id self, obj_id attacker, obj_id weapon, int[] damage) throws InterruptedException
    {
        encounter.handleSisterDamaged(self);
        return SCRIPT_CONTINUE;
    }
}
