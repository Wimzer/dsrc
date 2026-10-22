package script.theme_park.heroic.axkva_min_hard_mode;

import script.dictionary;
import script.obj_id;
import script.library.trial;

public class essence extends script.theme_park.heroic.axkva_min.axkva_shade
{
    public int OnAttach(obj_id self) throws InterruptedException
    {
        super.OnAttach(self);
        obj_id top = trial.getTop(self);
        dictionary ready = trial.getSessionDict(top, encounter.VAR_PREFIX);
        ready.put("essence", self);
        messageTo(top, "essenceReady", ready, 0.0f, false);
        return SCRIPT_CONTINUE;
    }
}
