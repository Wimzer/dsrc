package script.theme_park.heroic.ig88_hard_mode;

import script.library.instance;
import script.menu_info;
import script.menu_info_data;
import script.menu_info_types;
import script.obj_id;
import script.string_id;

public class exit extends script.base_script
{
    public static final String INSTANCE_NAME = "ig88_hard_mode";

    public int OnObjectMenuRequest(obj_id self, obj_id player, menu_info mi) throws InterruptedException
    {
        if (isDead(player) || isIncapacitated(player))
        {
            return SCRIPT_CONTINUE;
        }
        menu_info_data mid = mi.getMenuItemByType(menu_info_types.ITEM_USE);
        if (mid != null)
        {
            mid.setServerNotify(true);
        }
        else
        {
            mi.addRootMenu(menu_info_types.ITEM_USE, new string_id("ui_radial", "item_use"));
        }
        return SCRIPT_CONTINUE;
    }

    public int OnObjectMenuSelect(obj_id self, obj_id player, int item) throws InterruptedException
    {
        if (isDead(player) || isIncapacitated(player))
        {
            return SCRIPT_CONTINUE;
        }
        if (item == menu_info_types.ITEM_USE)
        {
            instance.requestExitPlayer(INSTANCE_NAME, player);
        }
        return SCRIPT_CONTINUE;
    }
}
