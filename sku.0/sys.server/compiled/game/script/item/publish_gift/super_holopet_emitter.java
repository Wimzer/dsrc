package script.item.publish_gift;

import script.*;
import script.library.incubator;
import script.library.player_structure;
import script.library.sui;
import script.library.utils;

/** Separate item, nine asset-only colors. Stock emitter and native client stay unchanged. */
public class super_holopet_emitter extends holopet_emitter
{
    public static final String PREFIX = "super_holopet";
    public static final String TABLE = "datatables/item/super_holopet_appearances.iff";
    public static final String[] COLORS = {"yellow", "orange", "red", "violet", "purple", "dark_blue", "light_blue", "teal", "green"};
    public static final String[] NAMES = {"Yellow", "Orange", "Red", "Violet", "Purple", "Dark Blue", "Light Blue", "Teal", "Green"};
    public static final int CAP = 100 * 3600;
    public static final int MANAGE = menu_info_types.SERVER_MENU4;

    public int selected(obj_id self) throws InterruptedException
    {
        int color = getIntObjVar(self, PREFIX + ".color");
        return color >= 0 && color < COLORS.length ? color : 0;
    }
    public int reserve(obj_id self, int color) throws InterruptedException
    {
        return color >= 0 && color < COLORS.length ? Math.max(0, Math.min(CAP, getIntObjVar(self, PREFIX + ".reserve." + color))) : 0;
    }
    public void setReserve(obj_id self, int color, int seconds) throws InterruptedException
    {
        if (color >= 0 && color < COLORS.length)
        {
            setObjVar(self, PREFIX + ".reserve." + color, Math.max(0, Math.min(CAP, seconds)));
        }
    }
    public int nextToken(obj_id self, String key) throws InterruptedException
    {
        int token = getIntObjVar(self, PREFIX + "." + key);
        token = token == Integer.MAX_VALUE ? 1 : token + 1;
        setObjVar(self, PREFIX + "." + key, token);
        return token;
    }
    public boolean authorized(obj_id self, obj_id player) throws InterruptedException
    {
        obj_id structure = getTopMostContainer(self);
        obj_id owner = getOwner(self);
        return isIdValid(player) && exists(player) && isPlayer(player) && isIdValid(owner) && isIdValid(structure) && exists(structure) && (player_structure.isBuilding(structure) || isPlayer(structure)) && (owner == player || player_structure.isAdmin(structure, player)) && getTopMostContainer(player) == structure && getDistance(player, self) <= 7.0f;
    }
    public int OnInitialize(obj_id self) throws InterruptedException
    {
        // Do not bill process downtime. Reserves include committed partial hours;
        // only the last uncommitted <=60-second active interval can be lost.
        utils.removeScriptVar(self, PREFIX + ".last");
        nextToken(self, "fuel_token");
        nextToken(self, "random_token");
        int result = super.OnInitialize(self);
        if (isTurnedOn(self))
        {
            if (reserve(self, selected(self)) <= 0 || !applyAppearance(self))
            {
                shutdown(self);
            }
            else
            {
                utils.setScriptVar(self, PREFIX + ".last", getCalendarTime());
                scheduleFuel(self);
                scheduleRandom(self);
            }
        }
        return result;
    }
    public int OnObjectMenuRequest(obj_id self, obj_id player, menu_info menu) throws InterruptedException
    {
        int result = super.OnObjectMenuRequest(self, player, menu);
        if (authorized(self, player))
        {
            menu.addRootMenu(MANAGE, new string_id("super_holopet", "menu_manage"));
        }
        return result;
    }
    public int OnObjectMenuSelect(obj_id self, obj_id player, int item) throws InterruptedException
    {
        if (item != MANAGE)
        {
            return super.OnObjectMenuSelect(self, player, item);
        }
        if (authorized(self, player))
        {
            showPage(self, player, 0);
        }
        return SCRIPT_CONTINUE;
    }
    public String appearance(obj_id self, int color) throws InterruptedException
    {
        String disk = getStringObjVar(self, "holopet_loaded");
        if (disk == null || color < 0 || color >= COLORS.length)
        {
            return "";
        }
        String key = disk + "_" + COLORS[color];
        int row = dataTableSearchColumnForString(key, "key", TABLE);
        return row < 0 ? "" : dataTableGetString(TABLE, row, "shared_template");
    }
    public boolean applyAppearance(obj_id self) throws InterruptedException
    {
        obj_id pet = getObjIdObjVar(self, "holopet_active");
        String template = appearance(self, selected(self));
        if (!isIdValid(pet) || !exists(pet) || template == null || template.equals(""))
        {
            return false;
        }
        // Existing replicated shared-creature alternate-appearance API. NHOL in
        // the private shaders avoids global fixed hologram shader replacement.
        setObjectAppearance(pet, template);
        return true;
    }
    public void turnOn(obj_id self, obj_id player, String type) throws InterruptedException
    {
        if (isTurnedOn(self))
        {
            return;
        }
        if (reserve(self, selected(self)) <= 0)
        {
            if (isIdValid(player))
            {
                sendSystemMessage(player, new string_id("super_holopet", "needs_fuel"));
            }
            return;
        }
        super.turnOn(self, player, type);
        if (isTurnedOn(self))
        {
            if (!applyAppearance(self))
            {
                shutdown(self);
                return;
            }
            utils.setScriptVar(self, PREFIX + ".last", getCalendarTime());
            scheduleFuel(self);
            scheduleRandom(self);
        }
    }
    public void settle(obj_id self) throws InterruptedException
    {
        if (isTurnedOn(self) && utils.hasScriptVar(self, PREFIX + ".last"))
        {
            int now = getCalendarTime();
            int last = utils.getIntScriptVar(self, PREFIX + ".last");
            long elapsed = Math.max(0L, (long)now - last);
            int remaining = reserve(self, selected(self));
            setReserve(self, selected(self), (int)Math.max(0L, remaining - elapsed));
            utils.setScriptVar(self, PREFIX + ".last", now);
        }
    }
    public void turnOff(obj_id self, obj_id player) throws InterruptedException
    {
        settle(self);
        nextToken(self, "fuel_token");
        nextToken(self, "random_token");
        utils.removeScriptVar(self, PREFIX + ".last");
        super.turnOff(self, player);
    }
    public void shutdown(obj_id self) throws InterruptedException
    {
        obj_id owner = getOwner(self);
        turnOff(self, isIdValid(owner) ? owner : self);
        if (isIdValid(owner) && exists(owner) && isPlayer(owner))
        {
            sendSystemMessage(owner, new string_id("super_holopet", "fuel_empty"));
        }
    }
    public void scheduleFuel(obj_id self) throws InterruptedException
    {
        dictionary params = new dictionary();
        params.put("token", nextToken(self, "fuel_token"));
        messageTo(self, "fuelHeartbeat", params, Math.max(1, Math.min(60, reserve(self, selected(self)))), false);
    }
    public int fuelHeartbeat(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || params.getInt("token") != getIntObjVar(self, PREFIX + ".fuel_token") || !isTurnedOn(self))
        {
            return SCRIPT_CONTINUE;
        }
        settle(self);
        if (utils.isNestedWithinAPlayer(self) || reserve(self, selected(self)) <= 0)
        {
            shutdown(self);
        }
        else
        {
            scheduleFuel(self);
        }
        return SCRIPT_CONTINUE;
    }
    public int randomInterval(obj_id self) throws InterruptedException
    {
        int value = getIntObjVar(self, PREFIX + ".random_interval");
        return value == 60 || value == 300 || value == 900 ? value : 0;
    }
    public void scheduleRandom(obj_id self) throws InterruptedException
    {
        int token = nextToken(self, "random_token");
        if (isTurnedOn(self) && randomInterval(self) > 0)
        {
            dictionary params = new dictionary();
            params.put("token", token);
            messageTo(self, "randomHeartbeat", params, randomInterval(self), false);
        }
    }
    public int randomHeartbeat(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || params.getInt("token") != getIntObjVar(self, PREFIX + ".random_token") || !isTurnedOn(self) || randomInterval(self) == 0)
        {
            return SCRIPT_CONTINUE;
        }
        settle(self);
        if (reserve(self, selected(self)) <= 0 || utils.isNestedWithinAPlayer(self))
        {
            shutdown(self);
            return SCRIPT_CONTINUE;
        }
        int[] choices = new int[COLORS.length];
        int count = 0;
        for (int i = 0; i < COLORS.length; ++i)
        {
            if (i != selected(self) && reserve(self, i) > 0)
            {
                choices[count++] = i;
            }
        }
        if (count > 0)
        {
            setObjVar(self, PREFIX + ".color", choices[rand(0, count - 1)]);
            if (!applyAppearance(self))
            {
                shutdown(self);
                return SCRIPT_CONTINUE;
            }
            scheduleFuel(self);
        }
        scheduleRandom(self);
        return SCRIPT_CONTINUE;
    }
    public int fuelForQuality(obj_id enzyme) throws InterruptedException
    {
        if (!hasObjVar(enzyme, incubator.ENZYME_QUALITY_OBJVAR))
        {
            return 0;
        }
        float quality = getFloatObjVar(enzyme, incubator.ENZYME_QUALITY_OBJVAR);
        if (Float.isNaN(quality) || Float.isInfinite(quality) || quality < 0 || quality > 100)
        {
            return 0;
        }
        return quality < 31 ? 1 : quality < 51 ? 2 : quality < 71 ? 3 : quality < 90 ? 5 : 100;
    }
    public void refuel(obj_id self, obj_id player) throws InterruptedException
    {
        if (!authorized(self, player))
        {
            return;
        }
        settle(self);
        obj_id inventory = utils.getInventoryContainer(player);
        obj_id[] enzymes = utils.getContainedObjectsWithObjVar(inventory, incubator.ENZYME_COLOR_OBJVAR, true);
        int added = 0;
        if (enzymes != null)
        {
            for (obj_id enzyme : enzymes)
            {
                if (!isIdValid(enzyme) || !exists(enzyme) || !utils.isNestedWithin(enzyme, inventory) || getGameObjectType(enzyme) != GOT_misc_enzyme_isomerase || !hasObjVar(enzyme, incubator.ENZYME_COLOR_OBJVAR))
                {
                    continue;
                }
                int color = getIntObjVar(enzyme, incubator.ENZYME_COLOR_OBJVAR);
                long total = (long)fuelForQuality(enzyme) * Math.max(1, getCount(enzyme)) * 3600L;
                if (color < 0 || color >= COLORS.length || total <= 0 || total > CAP - reserve(self, color))
                {
                    continue;
                }
                int amount = (int)total;
                // No partial consumption or lost overflow. Failure to destroy
                // grants no credit; deletion and objvar credit aren't DB-atomic.
                if (destroyObject(enzyme))
                {
                    setReserve(self, color, reserve(self, color) + amount);
                    added += amount / 3600;
                }
            }
        }
        if (added == 0)
        {
            sendSystemMessage(player, new string_id("super_holopet", "no_fuel_added"));
        }
        showPage(self, player, 0);
    }
    public String status(obj_id self) throws InterruptedException
    {
        String value = "Current color: " + NAMES[selected(self)] + "\nRandom interval: " + randomInterval(self) + " seconds (0 = Disabled)\n";
        for (int i = 0; i < COLORS.length; ++i)
        {
            value += NAMES[i] + ": " + (reserve(self, i) / 36 / 100.0f) + "/100 units\n";
        }
        return value + "Only the selected reserve drains: one unit per active hour. No Lime fuel. Fixed anchors only; no HSV editor or live preview.";
    }
    public void showPage(obj_id self, obj_id player, int page) throws InterruptedException
    {
        if (!authorized(self, player))
        {
            return;
        }
        settle(self);
        if (isTurnedOn(self) && reserve(self, selected(self)) <= 0)
        {
            shutdown(self);
        }
        String[] options = {"Select projected-pet color", "Refuel (consumes matching inventory Isomerases; confirm next)", "Random cycling", "Fuel status"};
        String prompt = status(self);
        if (page == 1)
        {
            options = NAMES;
            prompt += "\nA color requires its own funded reserve.";
        }
        else if (page == 2)
        {
            options = new String[]{"Disabled", "60 seconds", "300 seconds", "900 seconds"};
        }
        else if (page == 3)
        {
            options = new String[]{"Confirm: consume ALL eligible non-Lime Isomerases in inventory/backpacks that fully fit their matching reserves"};
            prompt += "\nWhole items/stacks only, multiplying yield by stack count. Quality yields: 0-30=1,31-50=2,51-70=3,71-89=5,90-100=100 units each. Overflow items/stacks are left untouched.";
        }
        else if (page == 4)
        {
            options = new String[]{"Back"};
        }
        int pid = sui.listbox(self, player, prompt, sui.OK_CANCEL, "@super_holopet:editor_title", options, "handleEditor", true, false);
        String key = PREFIX + ".sui." + player;
        utils.setScriptVar(self, key + ".pid", pid);
        utils.setScriptVar(self, key + ".page", page);
    }
    public int handleEditor(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null)
        {
            return SCRIPT_CONTINUE;
        }
        obj_id player = sui.getPlayerId(params);
        String key = PREFIX + ".sui." + player;
        if (!params.containsKey("pageId") || !utils.hasScriptVar(self, key + ".pid") || params.getInt("pageId") != utils.getIntScriptVar(self, key + ".pid"))
        {
            return SCRIPT_CONTINUE;
        }
        int page = utils.getIntScriptVar(self, key + ".page");
        utils.removeScriptVarTree(self, key);
        if (sui.getIntButtonPressed(params) == sui.BP_CANCEL || !authorized(self, player))
        {
            return SCRIPT_CONTINUE;
        }
        int row = sui.getListboxSelectedRow(params);
        if (page == 0 && row >= 0 && row <= 3)
        {
            showPage(self, player, row == 0 ? 1 : row == 1 ? 3 : row == 2 ? 2 : 4);
            return SCRIPT_CONTINUE;
        }
        if (page == 3 && row == 0)
        {
            refuel(self, player);
            return SCRIPT_CONTINUE;
        }
        if (page == 1 && row >= 0 && row < COLORS.length)
        {
            settle(self);
            if (isTurnedOn(self) && reserve(self, selected(self)) <= 0)
            {
                shutdown(self);
            }
            if (reserve(self, row) <= 0)
            {
                sendSystemMessage(player, new string_id("super_holopet", "needs_fuel"));
            }
            else
            {
                setObjVar(self, PREFIX + ".color", row);
                if (isTurnedOn(self))
                {
                    if (!applyAppearance(self))
                    {
                        shutdown(self);
                    }
                    else
                    {
                        scheduleFuel(self);
                    }
                }
            }
        }
        else if (page == 2 && row >= 0 && row < 4)
        {
            int[] intervals = {0, 60, 300, 900};
            setObjVar(self, PREFIX + ".random_interval", intervals[row]);
            scheduleRandom(self);
        }
        showPage(self, player, 0);
        return SCRIPT_CONTINUE;
    }
}
