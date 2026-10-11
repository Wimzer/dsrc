package script.quest.township;

import script.*;
import script.library.groundquests;
import script.library.instance;
import script.library.stealth;
import script.library.sui;
import script.library.utils;

public class ig88_scanner extends script.base_script
{
    public ig88_scanner()
    {
    }
    public static final int COUNTDOWN_TIME = 5;
    public static final String QUEST_NAME = "found_ig88_encounter";
    public static final String QUEST_TASK_NAME = "ig88_enter_compound";
    public static final String QUEST_SIGNAL = "ig88_compound_enter_signal";
    public static final String INSTANCE_NAME = "heroic_ig88";
    public static final String HARD_MODE_INSTANCE_NAME = "ig88_hard_mode";
    public static final String HARD_MODE_COLLECTION_SLOT = "heroic_ig88_01";
    public static final String TARGET_INSTANCE_VAR = "ig88_scanner.target_instance";
    public static final string_id SCANNER_TEXT = new string_id("spam", "ig88_use_scanner");
    public static final string_id HARD_MODE_TEXT = new string_id("Travel to IG-88 Hard Mode");
    public static final string_id HARD_MODE_REQUIREMENT_TEXT = new string_id("You must complete Heroic: IG-88 before entering IG-88 Hard Mode.");
    public int OnObjectMenuRequest(obj_id self, obj_id player, menu_info menuInfo) throws InterruptedException
    {
        if (isDead(player) || isIncapacitated(player))
        {
            return SCRIPT_CONTINUE;
        }
        int menu = menu = menuInfo.addRootMenu(menu_info_types.ITEM_USE, SCANNER_TEXT);
        menu_info_data menuInfoData = menuInfo.getMenuItemById(menu);
        if (menuInfoData != null)
        {
            menuInfoData.setServerNotify(true);
        }
        int hardMode = menuInfo.addRootMenu(menu_info_types.SERVER_MENU1, HARD_MODE_TEXT);
        menu_info_data hardModeData = menuInfo.getMenuItemById(hardMode);
        if (hardModeData != null)
        {
            hardModeData.setServerNotify(true);
        }
        return SCRIPT_CONTINUE;
    }
    public int OnObjectMenuSelect(obj_id self, obj_id player, int item) throws InterruptedException
    {
        if (isDead(player) || isIncapacitated(player))
        {
            return SCRIPT_CONTINUE;
        }
        if (item == menu_info_types.ITEM_USE && (groundquests.isQuestActiveOrComplete(player, QUEST_NAME) || instance.isFlaggedForInstance(player, "heroic_ig88")))
        {
            utils.setScriptVar(player, TARGET_INSTANCE_VAR, INSTANCE_NAME);
            string_id menuStringId = new string_id("spam", "ig88_use_scanner");
            String handler = "handleCountdownTimer";
            int startTime = 0;
            int range = 3;
            int flags = 0;
            flags |= sui.CD_EVENT_INCAPACITATE;
            flags |= sui.CD_EVENT_DAMAGED;
            stealth.testInvisNonCombatAction(player, self);
            int countdownSui = sui.smartCountdownTimerSUI(self, player, "quest_countdown_timer", menuStringId, startTime, COUNTDOWN_TIME, handler, range, flags);
        }
        else if (item == menu_info_types.SERVER_MENU1)
        {
            if (getCollectionSlotValue(player, HARD_MODE_COLLECTION_SLOT) <= 0)
            {
                sendSystemMessage(player, HARD_MODE_REQUIREMENT_TEXT);
                return SCRIPT_CONTINUE;
            }
            utils.setScriptVar(player, TARGET_INSTANCE_VAR, HARD_MODE_INSTANCE_NAME);
            int flags = sui.CD_EVENT_INCAPACITATE | sui.CD_EVENT_DAMAGED;
            stealth.testInvisNonCombatAction(player, self);
            int countdownSui = sui.smartCountdownTimerSUI(self, player, "quest_countdown_timer", HARD_MODE_TEXT, 0, COUNTDOWN_TIME, "handleCountdownTimer", 3, flags);
        }
        else 
        {
            sendSystemMessage(player, new string_id("nexus", "retina_not_recognized"));
        }
        return SCRIPT_CONTINUE;
    }
    public int handleCountdownTimer(obj_id self, dictionary params) throws InterruptedException
    {
        int pid = params.getInt("id");
        obj_id player = params.getObjId("player");
        if (!isIdValid(player))
        {
            return SCRIPT_CONTINUE;
        }
        int bp = sui.getIntButtonPressed(params);
        if (bp == sui.BP_CANCEL)
        {
            detachScript(player, sui.COUNTDOWNTIMER_PLAYER_SCRIPT);
            utils.removeScriptVar(player, TARGET_INSTANCE_VAR);
            return SCRIPT_CONTINUE;
        }
        else if (bp == sui.BP_REVERT)
        {
            int event = params.getInt("event");
            if (event == sui.CD_EVENT_LOCOMOTION)
            {
                sendSystemMessage(player, new string_id("quest/groundquests", "countdown_interrupted_locomotion"));
            }
            else if (event == sui.CD_EVENT_INCAPACITATE)
            {
                sendSystemMessage(player, new string_id("quest/groundquests", "countdown_interrupted_incapacitated"));
            }
            else if (event == sui.CD_EVENT_DAMAGED)
            {
                sendSystemMessage(player, new string_id("quest/groundquests", "countdown_interrupted_damaged"));
            }
            utils.removeScriptVar(player, TARGET_INSTANCE_VAR);
            return SCRIPT_CONTINUE;
        }
        if (!hasObjVar(player, sui.COUNTDOWNTIMER_SUI_VAR))
        {
            return SCRIPT_CONTINUE;
        }
        int test_pid = getIntObjVar(player, sui.COUNTDOWNTIMER_SUI_VAR);
        if (pid != test_pid)
        {
            return SCRIPT_CONTINUE;
        }
        forceCloseSUIPage(pid);
        detachScript(player, sui.COUNTDOWNTIMER_PLAYER_SCRIPT);
        String targetInstance = utils.hasScriptVar(player, TARGET_INSTANCE_VAR) ? utils.getStringScriptVar(player, TARGET_INSTANCE_VAR) : INSTANCE_NAME;
        utils.removeScriptVar(player, TARGET_INSTANCE_VAR);
        if (targetInstance.equals(HARD_MODE_INSTANCE_NAME))
        {
            if (getCollectionSlotValue(player, HARD_MODE_COLLECTION_SLOT) <= 0)
            {
                sendSystemMessage(player, HARD_MODE_REQUIREMENT_TEXT);
                return SCRIPT_CONTINUE;
            }
            if (!hasScript(player, "player.player_instance"))
            {
                attachScript(player, "player.player_instance");
            }
            if (!instance.isFlaggedForInstance(player, HARD_MODE_INSTANCE_NAME))
            {
                instance.flagPlayerForInstance(player, HARD_MODE_INSTANCE_NAME);
            }
            instance.requestInstanceMovement(player, HARD_MODE_INSTANCE_NAME);
            return SCRIPT_CONTINUE;
        }
        groundquests.sendSignal(player, QUEST_SIGNAL);
        instance.requestInstanceMovement(player, INSTANCE_NAME);
        return SCRIPT_CONTINUE;
    }
}
