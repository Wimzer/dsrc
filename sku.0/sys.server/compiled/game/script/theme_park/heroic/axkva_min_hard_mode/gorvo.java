package script.theme_park.heroic.axkva_min_hard_mode;

import script.dictionary;
import script.library.trial;
import script.obj_id;

public class gorvo extends script.theme_park.heroic.axkva_min.gorvo
{
    public int OnAttach(obj_id self) throws InterruptedException
    {
        setName(self, "Gorvo");
        encounter.prepareActorRegistration(self);
        trial.setHp(self, trial.HP_AXKVA_GORVO * 4);
        return SCRIPT_CONTINUE;
    }
    public int registerHardModeActor(obj_id self, dictionary params) throws InterruptedException
    {
        encounter.registerActor(self, params);
        return SCRIPT_CONTINUE;
    }
    public int OnEnteredCombat(obj_id self) throws InterruptedException
    {
        encounter.startWipeCheck(self);
        return SCRIPT_CONTINUE;
    }
}
