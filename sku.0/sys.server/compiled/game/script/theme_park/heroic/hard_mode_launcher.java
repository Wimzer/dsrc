package script.theme_park.heroic;

import script.library.ai_lib;
import script.library.chat;
import script.library.instance;
import script.library.utils;
import script.location;
import script.menu_info;
import script.menu_info_data;
import script.menu_info_types;
import script.obj_id;
import script.string_id;

public class hard_mode_launcher extends script.base_script
{
    public static final String AXKVA_HARD_MODE = "axkva_min_hard_mode";
    public static final String IG88_HARD_MODE = "ig88_hard_mode";
    public static final String CONVO = "hard_mode_launcher";
    public static final String BRANCH_ID = "conversation.hard_mode_launcher.branchId";
    public static final String PLAYER_INSTANCE_SCRIPT = "player.player_instance";
    public static final string_id GREETING = new string_id("dummy_string_table", "Select a hard-mode test destination.");
    public static final string_id RESPONSE_AXKVA = new string_id("dummy_string_table", "Send me to Axkva Min Hard Mode.");
    public static final string_id RESPONSE_IG88 = new string_id("dummy_string_table", "Send me to IG-88 Hard Mode.");
    public static final string_id RADIAL_AXKVA = new string_id("Enter Axkva Min's Prison (Hard Mode)");
    public static final string_id RADIAL_IG88 = new string_id("dummy_string_table", "Travel to IG-88 Hard Mode");
    public static final string_id TRANSFER = new string_id("dummy_string_table", "Routing request accepted. Stand by for instance transfer.");
    public static final string_id ADMIN_ONLY = new string_id("dummy_string_table", "This test launcher is restricted to GOD-mode administrators.");
    public static final string_id TOO_FAR = new string_id("dummy_string_table", "Please stand closer; my vocabulator is not rated for shouting across a starport.");
    public static final float SAFE_X = 0.0f;
    public static final float SAFE_Y = 0.6f;
    public static final float SAFE_Z = 53.5f;
    public static final float SAFE_YAW = 180.0f;

    public int OnAttach(obj_id self) throws InterruptedException
    {
        configureLauncher(self);
        return SCRIPT_CONTINUE;
    }

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        configureLauncher(self);
        return SCRIPT_CONTINUE;
    }

    public int OnObjectMenuRequest(obj_id self, obj_id player, menu_info mi) throws InterruptedException
    {
        if (!isGod(player))
        {
            return SCRIPT_CONTINUE;
        }
        int converse = mi.addRootMenu(menu_info_types.CONVERSE_START, null);
        menu_info_data converseData = mi.getMenuItemById(converse);
        converseData.setServerNotify(false);
        int axkva = mi.addRootMenu(menu_info_types.SERVER_MENU1, RADIAL_AXKVA);
        menu_info_data axkvaData = mi.getMenuItemById(axkva);
        axkvaData.setServerNotify(true);
        int ig88 = mi.addRootMenu(menu_info_types.SERVER_MENU2, RADIAL_IG88);
        menu_info_data ig88Data = mi.getMenuItemById(ig88);
        ig88Data.setServerNotify(true);
        return SCRIPT_CONTINUE;
    }

    public int OnObjectMenuSelect(obj_id self, obj_id player, int item) throws InterruptedException
    {
        if (!isGod(player))
        {
            sendSystemMessage(player, ADMIN_ONLY);
            return SCRIPT_CONTINUE;
        }
        if (item == menu_info_types.SERVER_MENU1)
        {
            requestTransfer(player, AXKVA_HARD_MODE);
        }
        else if (item == menu_info_types.SERVER_MENU2)
        {
            requestTransfer(player, IG88_HARD_MODE);
        }
        return SCRIPT_CONTINUE;
    }

    public int OnStartNpcConversation(obj_id self, obj_id speaker) throws InterruptedException
    {
        if (!isGod(speaker))
        {
            sendSystemMessage(speaker, ADMIN_ONLY);
            return SCRIPT_CONTINUE;
        }
        if (getDistance(speaker, self) > 8.0f)
        {
            sendSystemMessage(speaker, TOO_FAR);
            return SCRIPT_CONTINUE;
        }
        chat.setGoodMood(self);
        faceToBehavior(self, speaker);
        string_id[] responses = new string_id[] { RESPONSE_AXKVA, RESPONSE_IG88 };
        utils.setScriptVar(speaker, BRANCH_ID, 1);
        npcStartConversation(speaker, self, CONVO, GREETING, responses);
        return SCRIPT_CONTINUE;
    }

    public int OnNpcConversationResponse(obj_id self, String convo, obj_id player, string_id response) throws InterruptedException
    {
        if (!isGod(player) || !convo.equals(CONVO))
        {
            utils.removeScriptVar(player, BRANCH_ID);
            return SCRIPT_CONTINUE;
        }
        int branchId = utils.getIntScriptVar(player, BRANCH_ID);
        if (branchId == 1 && response.equals(RESPONSE_AXKVA.getAsciiId()))
        {
            requestTransfer(player, AXKVA_HARD_MODE);
            utils.removeScriptVar(player, BRANCH_ID);
            npcEndConversationWithMessage(player, TRANSFER);
            return SCRIPT_CONTINUE;
        }
        if (branchId == 1 && response.equals(RESPONSE_IG88.getAsciiId()))
        {
            requestTransfer(player, IG88_HARD_MODE);
            utils.removeScriptVar(player, BRANCH_ID);
            npcEndConversationWithMessage(player, TRANSFER);
            return SCRIPT_CONTINUE;
        }
        utils.removeScriptVar(player, BRANCH_ID);
        return SCRIPT_CONTINUE;
    }

    public void configureLauncher(obj_id self) throws InterruptedException
    {
        setName(self, "Hard Mode Protocol Droid");
        setInvulnerable(self, true);
        setCondition(self, CONDITION_CONVERSABLE);
        ai_lib.setDefaultCalmBehavior(self, ai_lib.BEHAVIOR_SENTINEL);
        setObjVar(self, "ai.diction", "droid_prissy");
        location loc = getLocation(self);
        loc.x = SAFE_X;
        loc.y = SAFE_Y;
        loc.z = SAFE_Z;
        setLocation(self, loc);
        setYaw(self, SAFE_YAW);
    }

    public void requestTransfer(obj_id player, String instanceName) throws InterruptedException
    {
        if (!isGod(player))
        {
            sendSystemMessage(player, ADMIN_ONLY);
            return;
        }
        if (!hasScript(player, PLAYER_INSTANCE_SCRIPT))
        {
            attachScript(player, PLAYER_INSTANCE_SCRIPT);
        }
        if (instanceName.equals(IG88_HARD_MODE))
        {
            removeObjVar(player, instance.PLAYER_INSTANCE + "." + instanceName);
        }
        if (!instance.isFlaggedForInstance(player, instanceName))
        {
            instance.flagPlayerForInstance(player, instanceName);
        }
        instance.requestInstanceMovement(player, instanceName);
    }
}
