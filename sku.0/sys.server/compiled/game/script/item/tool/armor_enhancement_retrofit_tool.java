package script.item.tool;

import script.*;
import script.library.armor;
import script.library.armor_enhancement;
import script.library.sui;
import script.library.utils;


public class armor_enhancement_retrofit_tool extends script.base_script
{
    public static final String SESSION_ROOT = "armor_enhancement_retrofit_session";
    public static final String SESSION_PLAYER = SESSION_ROOT + ".player";
    public static final String SESSION_TARGET = SESSION_ROOT + ".target";
    public static final String SESSION_PID = SESSION_ROOT + ".pid";

    public static final String OPERATION_ROOT = "armor_enhancement_retrofit_operation";
    public static final String OPERATION_ID = OPERATION_ROOT + ".id";
    public static final String OPERATION_PLAYER = OPERATION_ROOT + ".player";
    public static final String OPERATION_TARGET = OPERATION_ROOT + ".target";
    public static final String OPERATION_FEATHER = OPERATION_ROOT + ".feather";
    public static final String OPERATION_CARTRIDGE = OPERATION_ROOT + ".cartridge";
    public static final String OPERATION_FEATHER_COUNT = OPERATION_ROOT + ".feather_count";
    public static final String OPERATION_CARTRIDGE_COUNT = OPERATION_ROOT + ".cartridge_count";
    public static final String OPERATION_HEALTH = OPERATION_ROOT + ".health";
    public static final String OPERATION_SKILL_MOD = OPERATION_ROOT + ".skill_mod";
    public static final String OPERATION_SKILL_VALUE = OPERATION_ROOT + ".skill_value";
    public static final String OPERATION_TARGET_APPLIED = OPERATION_ROOT + ".target_applied";
    public static final String OPERATION_FEATHER_CONSUMED = OPERATION_ROOT + ".feather_consumed";
    public static final String OPERATION_CARTRIDGE_CONSUMED = OPERATION_ROOT + ".cartridge_consumed";
    // Either-or retrofit (user, 2026-11-21). Absent on records written before it: both loaded.
    // An unloaded component is recorded as already consumed, with the tool as a placeholder id.
    public static final String OPERATION_FEATHER_LOADED = OPERATION_ROOT + ".feather_loaded";
    public static final String OPERATION_CARTRIDGE_LOADED = OPERATION_ROOT + ".cartridge_loaded";

    public static final String TITLE = "Armor Enhancement Retrofit";
    public static final String LOG_CATEGORY = "armor_enhancement";

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        if (hasObjVar(self, OPERATION_ROOT) && !hasObjVar(self, OPERATION_ID))
        {
            removeObjVar(self, OPERATION_ROOT);
        }
        if (hasObjVar(self, OPERATION_ID))
        {
            messageTo(self, "recoverRetrofitOperation", null, 1.0f, false);
        }
        return SCRIPT_CONTINUE;
    }

    public int OnAboutToReceiveItem(obj_id self, obj_id srcContainer, obj_id transferer, obj_id item) throws InterruptedException
    {
        if (!isInMainInventory(self, transferer) || hasObjVar(self, OPERATION_ROOT) || !isIdValid(item) || !exists(item))
        {
            sendMessage(transferer, "The retrofit tool must be in your inventory and idle before components can be loaded.");
            return SCRIPT_OVERRIDE;
        }
        obj_id[] contents = getContents(self);
        if (contents == null || contents.length >= 3)
        {
            sendMessage(transferer, "The retrofit tool holds one armor piece, one feather attachment and one Biological Mechanics Cartridge.");
            return SCRIPT_OVERRIDE;
        }
        // Like the socket retrofit tool, the piece to change is loaded into the tool.
        boolean incomingTarget = isRetrofitTarget(item);
        boolean incomingFeather = armor_enhancement.getFinishedFeatherHealth(item) >= 0;
        boolean incomingCartridge = armor_enhancement.getFinishedCartridgeSkillModIndex(item) >= 0;
        if ((incomingTarget ? 1 : 0) + (incomingFeather ? 1 : 0) + (incomingCartridge ? 1 : 0) != 1)
        {
            sendMessage(transferer, "Only an unequipped crafted armor piece or core-bearing cybernetic, a finished feather attachment, or a finished Biological Mechanics Cartridge can be loaded.");
            return SCRIPT_OVERRIDE;
        }
        for (obj_id content : contents)
        {
            if (incomingTarget && isRetrofitTarget(content) || incomingFeather && armor_enhancement.getFinishedFeatherHealth(content) >= 0 || incomingCartridge && armor_enhancement.getFinishedCartridgeSkillModIndex(content) >= 0)
            {
                sendMessage(transferer, "The retrofit tool already contains one of those.");
                return SCRIPT_OVERRIDE;
            }
        }
        return SCRIPT_CONTINUE;
    }

    public int OnAboutToLoseItem(obj_id self, obj_id destContainer, obj_id transferer, obj_id item) throws InterruptedException
    {
        if (hasObjVar(self, OPERATION_ROOT))
        {
            sendMessage(transferer, "Components cannot be removed while a retrofit operation is being recovered.");
            return SCRIPT_OVERRIDE;
        }
        return SCRIPT_CONTINUE;
    }

    public int OnObjectMenuRequest(obj_id self, obj_id player, menu_info mi) throws InterruptedException
    {
        // A container's generic Use opens it on the client and never reaches this script, so
        // the action uses SERVER_MENU3 with the stock "Retrofit" label, as socket_retrofit_tool does.
        if (isInMainInventory(self, player))
        {
            mi.addRootMenu(menu_info_types.SERVER_MENU3, new string_id("spam", "retrofit_tool_retrofit"));
        }
        return SCRIPT_CONTINUE;
    }

    public int OnObjectMenuSelect(obj_id self, obj_id player, int item) throws InterruptedException
    {
        if (item != menu_info_types.SERVER_MENU3 || !isInMainInventory(self, player))
        {
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(self, OPERATION_ID))
        {
            if (!recoverOperation(self, player, true))
            {
                sendMessage(player, "This tool has an interrupted retrofit operation that could not yet be recovered. Keep its components loaded and unequip the target, then try again.");
            }
            return SCRIPT_CONTINUE;
        }
        if (!isIdValid(getLoadedTarget(self)))
        {
            sendMessage(player, "Load the armor piece or core-bearing cybernetic to change into the tool, with a feather attachment, a Biological Mechanics Cartridge, or both.");
            return SCRIPT_CONTINUE;
        }
        if (!validateLoadedComponents(self))
        {
            sendMessage(player, "Load a finished feather attachment, a finished Biological Mechanics Cartridge, or one of each, with the armor piece.");
            return SCRIPT_CONTINUE;
        }
        beginConfirmation(self, player);
        return SCRIPT_CONTINUE;
    }

    public void beginConfirmation(obj_id self, obj_id player) throws InterruptedException
    {
        cleanupSession(self, player);
        obj_id target = getLoadedTarget(self);
        if (!validateCommitObjects(self, player, target))
        {
            sendMessage(player, "The loaded armor piece or components cannot be retrofitted.");
            return;
        }
        obj_id feather = getLoadedFeather(self);
        obj_id cartridge = getLoadedCartridge(self);
        int[] payload = getRetrofitPayload(target, feather, cartridge);
        if (payload == null)
        {
            sendMessage(player, "The loaded armor piece or components cannot be retrofitted.");
            return;
        }
        String prompt = "Retrofit " + getEncodedName(target) + "? " + describeChange(isIdValid(feather), isIdValid(cartridge), payload) +
            " Only bonuses from this enhancement system change. The loaded " + (isIdValid(feather) && isIdValid(cartridge) ? "components" : "component") + " will be consumed.";
        utils.setScriptVar(self, SESSION_PLAYER, player);
        utils.setScriptVar(self, SESSION_TARGET, target);
        int pid = sui.msgbox(self, player, prompt, sui.YES_NO, TITLE, "handleRetrofitConfirmation");
        if (pid < 0)
        {
            cleanupSession(self, player);
            return;
        }
        sui.setPid(player, pid, SESSION_PID);
    }

    public int handleRetrofitConfirmation(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = getValidSessionPlayer(self, params);
        if (!isIdValid(player))
        {
            return SCRIPT_CONTINUE;
        }
        if (sui.getIntButtonPressed(params) != sui.BP_OK)
        {
            cleanupSession(self, player);
            return SCRIPT_CONTINUE;
        }
        obj_id target = utils.getObjIdScriptVar(self, SESSION_TARGET);
        if (!isIdValid(target) || !target.equals(getLoadedTarget(self)) || !validateCommitObjects(self, player, target))
        {
            cleanupSession(self, player);
            sendMessage(player, "The retrofit was cancelled because the target or components changed.");
            return SCRIPT_CONTINUE;
        }

        obj_id feather = getLoadedFeather(self);
        obj_id cartridge = getLoadedCartridge(self);
        int[] payload = getRetrofitPayload(target, feather, cartridge);
        if (payload == null)
        {
            cleanupSession(self, player);
            sendMessage(player, "The retrofit was cancelled because the target or components changed.");
            return SCRIPT_CONTINUE;
        }
        boolean featherLoaded = isIdValid(feather);
        boolean cartridgeLoaded = isIdValid(cartridge);
        String operationId = player + ":" + target + ":" + getGameTime() + ":" + rand(1, 2147483647);

        cleanupSession(self, player);
        setObjVar(self, OPERATION_PLAYER, player);
        setObjVar(self, OPERATION_TARGET, target);
        setObjVar(self, OPERATION_FEATHER, featherLoaded ? feather : self);
        setObjVar(self, OPERATION_CARTRIDGE, cartridgeLoaded ? cartridge : self);
        setObjVar(self, OPERATION_FEATHER_COUNT, featherLoaded ? getCount(feather) : 1);
        setObjVar(self, OPERATION_CARTRIDGE_COUNT, cartridgeLoaded ? getCount(cartridge) : 1);
        setObjVar(self, OPERATION_FEATHER_LOADED, featherLoaded ? 1 : 0);
        setObjVar(self, OPERATION_CARTRIDGE_LOADED, cartridgeLoaded ? 1 : 0);
        setObjVar(self, OPERATION_HEALTH, payload[0]);
        setObjVar(self, OPERATION_SKILL_MOD, armor_enhancement.getRetrofitSkillModName(payload[1]));
        setObjVar(self, OPERATION_SKILL_VALUE, payload[2]);
        setObjVar(self, OPERATION_TARGET_APPLIED, 0);
        setObjVar(self, OPERATION_FEATHER_CONSUMED, featherLoaded ? 0 : 1);
        setObjVar(self, OPERATION_CARTRIDGE_CONSUMED, cartridgeLoaded ? 0 : 1);
        setObjVar(self, OPERATION_ID, operationId);

        if (!recoverOperation(self, player, true))
        {
            sendMessage(player, "The retrofit operation was interrupted. The tool retained recovery data; keep the components loaded and try using it again.");
        }
        return SCRIPT_CONTINUE;
    }

    public int recoverRetrofitOperation(obj_id self, dictionary params) throws InterruptedException
    {
        if (hasObjVar(self, OPERATION_ID) && hasObjVar(self, OPERATION_PLAYER))
        {
            recoverOperation(self, getObjIdObjVar(self, OPERATION_PLAYER), false);
        }
        return SCRIPT_CONTINUE;
    }

    public boolean recoverOperation(obj_id self, obj_id player, boolean notifyPlayer) throws InterruptedException
    {
        if (!validateOperationRecord(self))
        {
            CustomerServiceLog(LOG_CATEGORY, "Invalid retrofit recovery record on tool " + self + ".");
            return false;
        }
        String operationId = getStringObjVar(self, OPERATION_ID);
        obj_id recordedPlayer = getObjIdObjVar(self, OPERATION_PLAYER);
        obj_id target = getObjIdObjVar(self, OPERATION_TARGET);
        obj_id feather = getObjIdObjVar(self, OPERATION_FEATHER);
        obj_id cartridge = getObjIdObjVar(self, OPERATION_CARTRIDGE);
        int health = getIntObjVar(self, OPERATION_HEALTH);
        int skillModIndex = armor_enhancement.getRetrofitSkillModIndex(getStringObjVar(self, OPERATION_SKILL_MOD));
        int skillValue = getIntObjVar(self, OPERATION_SKILL_VALUE);
        if (!isIdValid(recordedPlayer) || !recordedPlayer.equals(player) || !isInMainInventory(self, recordedPlayer) || !isInMainInventory(target, recordedPlayer) || utils.isEquipped(target) || !armor_enhancement.isEligibleRetrofitTarget(target))
        {
            return false;
        }
        boolean targetApplied = getIntObjVar(self, OPERATION_TARGET_APPLIED) == 1 || armor_enhancement.isRetrofitOperationApplied(target, operationId);
        int featherState = getConsumptionState(self, feather, OPERATION_FEATHER_COUNT, OPERATION_FEATHER_CONSUMED, targetApplied);
        int cartridgeState = getConsumptionState(self, cartridge, OPERATION_CARTRIDGE_COUNT, OPERATION_CARTRIDGE_CONSUMED, targetApplied);
        if (featherState < 0 || cartridgeState < 0 || health < 0 || health > armor_enhancement.MAX_HEALTH || !armor_enhancement.isValidRetrofitSkill(skillModIndex, skillValue) || health == 0 && skillModIndex < 0 ||
            !isOperationLoaded(self, OPERATION_FEATHER_LOADED) && !isOperationLoaded(self, OPERATION_CARTRIDGE_LOADED))
        {
            CustomerServiceLog(LOG_CATEGORY, "Retrofit operation " + operationId + " has invalid recovery state on tool " + self + ".");
            return false;
        }
        if (!isIdValid(target) || !exists(target))
        {
            CustomerServiceLog(LOG_CATEGORY, "Retrofit operation " + operationId + " cannot find target " + target + ".");
            return false;
        }

        if (!armor_enhancement.hasMatchingRetrofitOperation(target, operationId))
        {
            if (armor_enhancement.hasRetrofitOperation(target))
            {
                CustomerServiceLog(LOG_CATEGORY, "Retrofit operation " + operationId + " found a different operation on target " + target + ".");
                return false;
            }
            if (featherState == 1 && cartridgeState == 1 && armor_enhancement.matchesAppliedPayload(target, health, skillModIndex, skillValue))
            {
                removeObjVar(self, OPERATION_ROOT);
                return true;
            }
            if (utils.isEquipped(target) || !isInMainInventory(target, player) || featherState != 0 || cartridgeState != 0 || !validateOperationComponents(self, feather, cartridge, health, skillModIndex, skillValue))
            {
                return false;
            }
            if (!armor_enhancement.prepareRetrofitTarget(target, operationId, health, skillModIndex, skillValue))
            {
                return false;
            }
        }
        if (utils.isEquipped(target) || !armor_enhancement.applyPreparedRetrofit(target, operationId))
        {
            return false;
        }
        setObjVar(self, OPERATION_TARGET_APPLIED, 1);
        if (!consumeOperationComponent(self, feather, OPERATION_FEATHER_COUNT, OPERATION_FEATHER_CONSUMED) || !consumeOperationComponent(self, cartridge, OPERATION_CARTRIDGE_COUNT, OPERATION_CARTRIDGE_CONSUMED))
        {
            return false;
        }
        if (!armor_enhancement.finishRetrofitTarget(target, operationId))
        {
            return false;
        }
        removeObjVar(self, OPERATION_ROOT);
        CustomerServiceLog(LOG_CATEGORY, "Completed retrofit operation " + operationId + " for player " + player + " on target " + target + ".");
        // Return the piece to the inventory, as the socket retrofit tool does.
        boolean returned = getContainedBy(target) == self && putIn(target, utils.getInventoryContainer(player), player);
        if (notifyPlayer && isIdValid(player) && exists(player))
        {
            sendMessage(player, returned ? "The armor enhancement retrofit completed successfully. The piece is back in your inventory and the tool remains reusable." :
                "The armor enhancement retrofit completed successfully. Take the piece out of the tool; the tool remains reusable.");
        }
        return true;
    }

    public boolean validateCommitObjects(obj_id self, obj_id player, obj_id target) throws InterruptedException
    {
        return isInMainInventory(self, player) && isInMainInventory(target, player) && !utils.isEquipped(target) && armor_enhancement.isEligibleRetrofitTarget(target) && !armor_enhancement.hasRetrofitOperation(target) && validateLoadedComponents(self);
    }

    // One feather, one cartridge, or one of each; nothing else.
    public boolean validateLoadedComponents(obj_id self) throws InterruptedException
    {
        obj_id[] contents = getContents(self);
        if (contents == null || contents.length < 1 || contents.length > 3)
        {
            return false;
        }
        obj_id feather = getLoadedFeather(self);
        obj_id cartridge = getLoadedCartridge(self);
        int loaded = (isIdValid(feather) ? 1 : 0) + (isIdValid(cartridge) ? 1 : 0) + (isIdValid(getLoadedTarget(self)) ? 1 : 0);
        if (loaded != contents.length || loaded < 2 || isIdValid(feather) && isIdValid(cartridge) && feather.equals(cartridge))
        {
            return false;
        }
        if (isIdValid(feather) && (getContainedBy(feather) != self || getCount(feather) < 1 || armor_enhancement.getFinishedFeatherHealth(feather) < 0))
        {
            return false;
        }
        if (isIdValid(cartridge))
        {
            int skillModIndex = armor_enhancement.getFinishedCartridgeSkillModIndex(cartridge);
            if (getContainedBy(cartridge) != self || getCount(cartridge) < 1 || skillModIndex < 0 ||
                !armor_enhancement.isValidSkillMod(armor_enhancement.SKILL_MODS[skillModIndex], armor_enhancement.getFinishedCartridgeSkillValue(cartridge)))
            {
                return false;
            }
        }
        return true;
    }

    // {health, skill mod index (-1 none), skill value}: loaded components replace their part,
    // the other part keeps the target's current feature-owned value.
    public int[] getRetrofitPayload(obj_id target, obj_id feather, obj_id cartridge) throws InterruptedException
    {
        if (!armor_enhancement.validateAppliedPayload(target) || !isIdValid(feather) && !isIdValid(cartridge))
        {
            return null;
        }
        int health = isIdValid(feather) ? armor_enhancement.getFinishedFeatherHealth(feather) : armor_enhancement.getAppliedHealth(target);
        int skillModIndex = isIdValid(cartridge) ? armor_enhancement.getFinishedCartridgeSkillModIndex(cartridge) : armor_enhancement.getAppliedSkillModIndex(target);
        int skillValue = isIdValid(cartridge) ? armor_enhancement.getFinishedCartridgeSkillValue(cartridge) : armor_enhancement.getAppliedSkillValue(target);
        if (skillModIndex < 0)
        {
            skillValue = 0;
        }
        if (health < 0 || health > armor_enhancement.MAX_HEALTH || !armor_enhancement.isValidRetrofitSkill(skillModIndex, skillValue) || health == 0 && skillModIndex < 0)
        {
            return null;
        }
        return new int[]{health, skillModIndex, skillValue};
    }

    public String describeChange(boolean featherLoaded, boolean cartridgeLoaded, int[] payload) throws InterruptedException
    {
        String skill = payload[1] >= 0 ? "+" + payload[2] + " " + localize(new string_id("stat_n", armor_enhancement.SKILL_MODS[payload[1]])) : "no cartridge bonus";
        String health = payload[0] > 0 ? "+" + payload[0] + " Health" : "no feather Health";
        if (featherLoaded && cartridgeLoaded)
        {
            return "Apply " + health + " and " + skill + ".";
        }
        if (featherLoaded)
        {
            return "Apply " + health + "; the current cartridge bonus (" + skill + ") is kept.";
        }
        return "Apply " + skill + "; the current feather Health (" + health + ") is kept.";
    }

    public boolean isOperationLoaded(obj_id self, String loadedObjVar) throws InterruptedException
    {
        return !hasObjVar(self, loadedObjVar) || getIntObjVar(self, loadedObjVar) == 1;
    }

    public boolean isRetrofitTarget(obj_id item) throws InterruptedException
    {
        return armor_enhancement.isEligibleRetrofitTarget(item) && !utils.isEquipped(item);
    }

    public obj_id getLoadedTarget(obj_id self) throws InterruptedException
    {
        obj_id[] contents = getContents(self);
        if (contents == null)
        {
            return obj_id.NULL_ID;
        }
        for (obj_id content : contents)
        {
            if (isIdValid(content) && exists(content) && armor_enhancement.isEligibleRetrofitTarget(content))
            {
                return content;
            }
        }
        return obj_id.NULL_ID;
    }

    public obj_id getLoadedFeather(obj_id self) throws InterruptedException
    {
        obj_id[] contents = getContents(self);
        for (obj_id content : contents)
        {
            if (isIdValid(content) && exists(content) && armor_enhancement.getFinishedFeatherHealth(content) >= 0)
            {
                return content;
            }
        }
        return obj_id.NULL_ID;
    }

    public obj_id getLoadedCartridge(obj_id self) throws InterruptedException
    {
        obj_id[] contents = getContents(self);
        for (obj_id content : contents)
        {
            if (isIdValid(content) && exists(content) && armor_enhancement.getFinishedCartridgeSkillModIndex(content) >= 0)
            {
                return content;
            }
        }
        return obj_id.NULL_ID;
    }

    public obj_id getValidSessionPlayer(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || params.isEmpty() || !utils.hasScriptVar(self, SESSION_PLAYER))
        {
            return obj_id.NULL_ID;
        }
        obj_id player = sui.getPlayerId(params);
        obj_id sessionPlayer = utils.getObjIdScriptVar(self, SESSION_PLAYER);
        if (!isIdValid(player) || !exists(player) || !player.equals(sessionPlayer) || !sui.hasPid(player, SESSION_PID))
        {
            return obj_id.NULL_ID;
        }
        int pageId = params.getInt("pageId");
        int pid = sui.getPid(player, SESSION_PID);
        if (pageId != pid)
        {
            if (pageId > -1)
            {
                forceCloseSUIPage(pageId);
            }
            return obj_id.NULL_ID;
        }
        if (!isInMainInventory(self, player))
        {
            cleanupSession(self, player);
            return obj_id.NULL_ID;
        }
        return player;
    }

    public void cleanupSession(obj_id self, obj_id player) throws InterruptedException
    {
        if (isIdValid(player) && sui.hasPid(player, SESSION_PID))
        {
            int pid = sui.getPid(player, SESSION_PID);
            if (pid > -1)
            {
                forceCloseSUIPage(pid);
            }
            sui.removePid(player, SESSION_PID);
        }
        utils.removeScriptVarTree(self, SESSION_ROOT);
    }

    public boolean validateOperationRecord(obj_id self) throws InterruptedException
    {
        return hasObjVar(self, OPERATION_ID) && hasObjVar(self, OPERATION_PLAYER) && hasObjVar(self, OPERATION_TARGET) &&
            hasObjVar(self, OPERATION_FEATHER) && hasObjVar(self, OPERATION_CARTRIDGE) && hasObjVar(self, OPERATION_FEATHER_COUNT) &&
            hasObjVar(self, OPERATION_CARTRIDGE_COUNT) && hasObjVar(self, OPERATION_HEALTH) && hasObjVar(self, OPERATION_SKILL_MOD) &&
            hasObjVar(self, OPERATION_SKILL_VALUE) && hasObjVar(self, OPERATION_TARGET_APPLIED) && hasObjVar(self, OPERATION_FEATHER_CONSUMED) && hasObjVar(self, OPERATION_CARTRIDGE_CONSUMED);
    }

    public boolean validateOperationComponents(obj_id self, obj_id feather, obj_id cartridge, int health, int skillModIndex, int skillValue) throws InterruptedException
    {
        boolean featherOk = !isOperationLoaded(self, OPERATION_FEATHER_LOADED) ||
            isIdValid(feather) && exists(feather) && getContainedBy(feather) == self && getCount(feather) == getIntObjVar(self, OPERATION_FEATHER_COUNT) &&
            armor_enhancement.getFinishedFeatherHealth(feather) == health;
        boolean cartridgeOk = !isOperationLoaded(self, OPERATION_CARTRIDGE_LOADED) ||
            isIdValid(cartridge) && exists(cartridge) && getContainedBy(cartridge) == self &&
            getCount(cartridge) == getIntObjVar(self, OPERATION_CARTRIDGE_COUNT) && armor_enhancement.getFinishedCartridgeSkillModIndex(cartridge) == skillModIndex &&
            armor_enhancement.getFinishedCartridgeSkillValue(cartridge) == skillValue;
        return featherOk && cartridgeOk;
    }

    public int getConsumptionState(obj_id self, obj_id component, String countObjVar, String consumedObjVar, boolean targetApplied) throws InterruptedException
    {
        if (getIntObjVar(self, consumedObjVar) == 1)
        {
            return 1;
        }
        int initialCount = getIntObjVar(self, countObjVar);
        if (initialCount < 1)
        {
            return -1;
        }
        if (!isIdValid(component) || !exists(component))
        {
            if (!targetApplied)
            {
                return -1;
            }
            setObjVar(self, consumedObjVar, 1);
            return 1;
        }
        int currentCount = getCount(component);
        if (currentCount == initialCount)
        {
            return 0;
        }
        if (currentCount == initialCount - 1)
        {
            setObjVar(self, consumedObjVar, 1);
            return 1;
        }
        return -1;
    }

    public boolean consumeOperationComponent(obj_id self, obj_id component, String countObjVar, String consumedObjVar) throws InterruptedException
    {
        int state = getConsumptionState(self, component, countObjVar, consumedObjVar, true);
        if (state == 1)
        {
            return true;
        }
        if (state != 0 || getContainedBy(component) != self || !decrementCount(component))
        {
            return false;
        }
        setObjVar(self, consumedObjVar, 1);
        return true;
    }

    public boolean isInMainInventory(obj_id object, obj_id player) throws InterruptedException
    {
        if (!isIdValid(object) || !exists(object) || !isIdValid(player) || !exists(player))
        {
            return false;
        }
        obj_id inventory = utils.getInventoryContainer(player);
        return isIdValid(inventory) && utils.isNestedWithin(object, inventory);
    }

    public void sendMessage(obj_id player, String message) throws InterruptedException
    {
        if (isIdValid(player) && exists(player))
        {
            sendSystemMessage(player, message, null);
        }
    }
}
