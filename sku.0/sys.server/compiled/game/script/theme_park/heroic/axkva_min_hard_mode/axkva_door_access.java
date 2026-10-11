package script.theme_park.heroic.axkva_min_hard_mode;

import script.library.instance;
import script.menu_info;
import script.menu_info_types;
import script.obj_id;
import script.string_id;

public class axkva_door_access extends script.base_script
{
    public static final String INSTANCE_NAME = "axkva_min_hard_mode";
    public static final String PLAYER_INSTANCE_SCRIPT = "player.player_instance";
    public static final String REQUIRED_COLLECTION_SLOT = "heroic_axkva_min_01";
    public static final int REQUIRED_COLLECTION_COUNT = 10;
    public static final string_id HARD_MODE_ENTER = new string_id("Enter Axkva Min's Prison (Hard Mode)");

    public int OnObjectMenuRequest(obj_id self, obj_id player, menu_info item) throws InterruptedException
    {
        if (getDistance(player, self) <= 6.0f)
        {
            item.addRootMenu(menu_info_types.SERVER_MENU1, HARD_MODE_ENTER);
        }
        return SCRIPT_CONTINUE;
    }

    public int OnObjectMenuSelect(obj_id self, obj_id player, int item) throws InterruptedException
    {
        if (item != menu_info_types.SERVER_MENU1)
        {
            return SCRIPT_CONTINUE;
        }
        if (getDistance(player, self) > 6.0f)
        {
            sendSystemMessage(player, "You are too far from the Axkva Min doorway to enter hard mode.", null);
            return SCRIPT_CONTINUE;
        }
        if (getCollectionSlotValue(player, REQUIRED_COLLECTION_SLOT) < REQUIRED_COLLECTION_COUNT)
        {
            sendSystemMessage(player, "You must complete the Axkva Min defeated collection before entering hard mode.", null);
            return SCRIPT_CONTINUE;
        }
        if (!hasScript(player, PLAYER_INSTANCE_SCRIPT))
        {
            attachScript(player, PLAYER_INSTANCE_SCRIPT);
        }
        if (!instance.isFlaggedForInstance(player, INSTANCE_NAME))
        {
            instance.flagPlayerForInstance(player, INSTANCE_NAME);
        }
        instance.requestInstanceMovement(player, INSTANCE_NAME);
        return SCRIPT_CONTINUE;
    }
}
