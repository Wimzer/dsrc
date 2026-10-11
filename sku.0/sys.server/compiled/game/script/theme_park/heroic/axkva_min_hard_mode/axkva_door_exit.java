package script.theme_park.heroic.axkva_min_hard_mode;

import script.library.instance;
import script.menu_info;
import script.menu_info_types;
import script.obj_id;
import script.string_id;

public class axkva_door_exit extends script.base_script
{
    public static final String INSTANCE_NAME = "axkva_min_hard_mode";
    public static final string_id HARD_MODE_EXIT = new string_id("dummy_string_table", "Exit Axkva Min Hard Mode");

    public int OnObjectMenuRequest(obj_id self, obj_id player, menu_info item) throws InterruptedException
    {
        if (getDistance(player, self) <= 6.0f)
        {
            item.addRootMenu(menu_info_types.ITEM_USE, HARD_MODE_EXIT);
        }
        return SCRIPT_CONTINUE;
    }

    public int OnObjectMenuSelect(obj_id self, obj_id player, int item) throws InterruptedException
    {
        if (item == menu_info_types.ITEM_USE)
        {
            instance.requestExitPlayer(INSTANCE_NAME, player);
        }
        return SCRIPT_CONTINUE;
    }
}
