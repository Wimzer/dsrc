package script.test;

import script.*;

public class test_holocron extends script.base_script
{
    public int OnAttach(obj_id self) throws InterruptedException
    {
        debugSpeakMsg(self, "Holocron test: calling openHolocronToPage...");
        boolean result = openHolocronToPage(self, "");
        debugSpeakMsg(self, "Holocron test: returned " + result);
        detachScript(self, "test.test_holocron");
        return SCRIPT_CONTINUE;
    }
}
