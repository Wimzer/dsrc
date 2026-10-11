package script.theme_park.heroic.axkva_min_hard_mode;

import script.dictionary;
import script.library.ai_lib;
import script.library.buff;
import script.library.trial;
import script.obj_id;

public class nandina extends script.theme_park.heroic.axkva_min.nandina
{
    public int OnAttach(obj_id self) throws InterruptedException
    {
        setName(self, "Nandina");
        encounter.prepareActorRegistration(self);
        messageTo(self, "setGorvoAsPet", null, 1.0f, false);
        trial.setHp(self, trial.HP_AKXVA_NANDINA * 4);
        encounter.applyCurrentRage(self);
        return SCRIPT_CONTINUE;
    }
    public int registerHardModeActor(obj_id self, dictionary params) throws InterruptedException
    {
        encounter.registerActor(self, params);
        return SCRIPT_CONTINUE;
    }
    public int setGorvoAsPet(obj_id self, dictionary params) throws InterruptedException
    {
        int result = super.setGorvoAsPet(self, params);
        obj_id gorvo = encounter.getObjectBySpawnId(trial.getTop(self), "gorvo");
        if (isIdValid(gorvo) && exists(gorvo))
        {
            obj_id[] allies = { gorvo, self };
            ai_lib.establishAgroLink(gorvo, allies);
            buff.applyBuff(gorvo, "bodyguard");
        }
        return result;
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
