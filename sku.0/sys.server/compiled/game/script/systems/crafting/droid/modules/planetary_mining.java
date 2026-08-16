package script.systems.crafting.droid.modules;

import script.*;
import script.library.*;

import java.util.Vector;

public class planetary_mining extends script.base_script
{
    public static final String[] PLANET_NAMES =
    {
        "Kashyyyk",
        "Mustafar"
    };
    public static final String[] PLANET_INTERNAL =
    {
        "kashyyyk_main",
        "mustafar"
    };
    public static final String[] RESOURCE_CLASSES =
    {
        "mineral",
        "chemical",
        "gas",
        "flora_resources",
        "water",
        "energy"
    };
    public static final String[] RESOURCE_CLASS_NAMES =
    {
        "Minerals",
        "Chemicals",
        "Gases",
        "Flora",
        "Water",
        "Energy"
    };
    public static final String VAR_PLANET = "planetary_mining.planet";
    public static final String VAR_RESOURCE_CLASS = "planetary_mining.resource_class";
    public static final String VAR_RESOURCE_SUBCLASSES = "planetary_mining.resource_subclasses";
    public static final String VAR_SELECTED_RESOURCE_CLASS = "planetary_mining.selected_resource_class";
    public static final String VAR_RESOURCE_TYPES = "planetary_mining.resource_types";
    public static final String VAR_RESOURCE_CONCENTRATIONS = "planetary_mining.resource_concentrations";
    public static final String VAR_RESOURCE_TYPE = "planetary_mining.resource_type";
    public static final String VAR_ACCOUNT_RESERVATION_PENDING = "planetary_mining.account_reservation_pending";
    public static final String ATTRIBUTE_BASE = craftinglib.COMPONENT_ATTRIBUTE_OBJVAR_NAME + ".";
    public static final String LEGACY_STATIC_ATTRIBUTE_BASE = "crafting.component_attribute.";
    public static final String ATTRIBUTE_EXTRACTION_RATE = ATTRIBUTE_BASE + "extractRate";
    public static final float MIN_ACTIVE_DENSITY = 0.0001f;
    public static final int MAX_RESOURCE_MENU_ENTRIES = 50;
    public static final float CONCENTRATION_MULTIPLIER = 0.85f;

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        setName(self, "Planetary Mining Droid");
        return SCRIPT_CONTINUE;
    }

    public int OnObjectMenuRequest(obj_id self, obj_id player, menu_info mi) throws InterruptedException
    {
        menu_info_data mid = mi.getMenuItemByType(menu_info_types.ITEM_USE);
        if (mid == null)
        {
            int menu = mi.addRootMenu(menu_info_types.ITEM_USE, new string_id("", ""));
            mid = mi.getMenuItemById(menu);
        }
        mid.setServerNotify(true);
        return SCRIPT_CONTINUE;
    }

    public int OnObjectMenuSelect(obj_id self, obj_id player, int item) throws InterruptedException
    {
        if (item != menu_info_types.ITEM_USE)
        {
            return SCRIPT_CONTINUE;
        }
        if (!utils.isProfession(player, utils.TRADER))
        {
            sendSystemMessage(player, "The interface for this droid is too complex for you to interact with.", null);
            return SCRIPT_CONTINUE;
        }
        if (getTopMostContainer(player) != player)
        {
            sendSystemMessage(player, "You must be outdoors to launch this droid.", null);
            return SCRIPT_OVERRIDE;
        }
        sui.listbox(self, player, "Select the planet to mine.", sui.OK_CANCEL, "Planetary Mining Droid", PLANET_NAMES, "handleMiningPlanetSelection");
        return SCRIPT_CONTINUE;
    }

    public int handleMiningPlanetSelection(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isListSelectionValid(params))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int index = sui.getListboxSelectedRow(params);
        obj_id player = sui.getPlayerId(params);
        utils.setScriptVar(self, VAR_PLANET, PLANET_INTERNAL[index]);

        String[] availableClasses = getAvailableResourceClasses(PLANET_INTERNAL[index]);
        if (availableClasses.length == 0)
        {
            sendSystemMessage(player, "No supported resources are currently available on that planet.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_RESOURCE_CLASS, availableClasses);
        sui.listbox(self, player, "Select a resource type.", sui.OK_CANCEL, "Planetary Mining Droid", getMiningResourceClassNames(availableClasses), "handleMiningClassSelection");
        return SCRIPT_CONTINUE;
    }

    public int handleMiningClassSelection(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isListSelectionValid(params))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id player = sui.getPlayerId(params);
        String[] availableClasses = utils.getStringArrayScriptVar(self, VAR_RESOURCE_CLASS);
        int index = sui.getListboxSelectedRow(params);
        if (availableClasses == null || index < 0 || index >= availableClasses.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        String resourceClass = availableClasses[index];
        utils.setScriptVar(self, VAR_SELECTED_RESOURCE_CLASS, resourceClass);
        showActiveResourceSelection(self, player, planet, resourceClass);
        return SCRIPT_CONTINUE;
    }

    public int handleMiningSubclassSelection(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isListSelectionValid(params))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id player = sui.getPlayerId(params);
        String[] subclasses = utils.getStringArrayScriptVar(self, VAR_RESOURCE_SUBCLASSES);
        int index = sui.getListboxSelectedRow(params);
        if (subclasses == null || index < 0 || index >= subclasses.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        String resourceClass = subclasses[index];
        utils.setScriptVar(self, VAR_SELECTED_RESOURCE_CLASS, resourceClass);
        showActiveResourceSelection(self, player, utils.getStringScriptVar(self, VAR_PLANET), resourceClass);
        return SCRIPT_CONTINUE;
    }

    public void showActiveResourceSelection(obj_id self, obj_id player, String planet, String resourceClass) throws InterruptedException
    {
        resource_density[] resources = getAvailablePlanetResourceDensities(planet, resourceClass);
        if (resources == null || resources.length == 0)
        {
            sendSystemMessage(player, "No active resources of that type are currently available on that planet.", null);
            cleanScriptVars(self);
            return;
        }
        if (resources.length > MAX_RESOURCE_MENU_ENTRIES)
        {
            String[] childClasses = getImmediateResourceChildClasses(resourceClass);
            Vector activeChildren = new Vector();
            if (childClasses != null)
            {
                for (String childClass : childClasses) {
                    resource_density[] childResources = getAvailablePlanetResourceDensities(planet, childClass);
                    if (childResources != null && childResources.length > 0)
                    {
                        activeChildren.add(childClass);
                    }
                }
            }
            if (activeChildren.size() == 0)
            {
                sendSystemMessage(player, "Too many active resources are available in this category. Select a more specific resource type.", null);
                cleanScriptVars(self);
                return;
            }
            String[] subclasses = new String[activeChildren.size()];
            activeChildren.toArray(subclasses);
            String[] names = new String[subclasses.length];
            for (int i = 0; i < subclasses.length; ++i)
            {
                names[i] = getLocalizedResourceClassName(subclasses[i]);
            }
            utils.setScriptVar(self, VAR_RESOURCE_SUBCLASSES, subclasses);
            sui.listbox(self, player, "Select an active resource subtype.", sui.OK_CANCEL, "Planetary Mining Droid", names, "handleMiningSubclassSelection");
            return;
        }

        obj_id[] resourceTypes = new obj_id[resources.length];
        float[] concentrations = new float[resources.length];
        for (int i = 0; i < resources.length; ++i)
        {
            resourceTypes[i] = resources[i].getResourceType();
            concentrations[i] = resources[i].getDensity() * CONCENTRATION_MULTIPLIER;
        }
        utils.setScriptVar(self, VAR_RESOURCE_CONCENTRATIONS, concentrations);
        String[] names = new String[resources.length];
        for (int i = 0; i < resources.length; ++i)
        {
            names[i] = getLocalizedResourceName(resourceTypes[i]) + " [" + getLocalizedResourceClassName(getResourceClass(resourceTypes[i])) + "] " + Math.round(concentrations[i] * 100) + "%";
        }
        utils.setScriptVar(self, VAR_RESOURCE_TYPES, resourceTypes);
        sui.listbox(self, player, "Select an active resource.", sui.OK_CANCEL, "Planetary Mining Droid", names, "handleMiningResourceSelection");
    }

    public int handleMiningResourceSelection(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isListSelectionValid(params))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id player = sui.getPlayerId(params);
        if (!utils.isNestedWithin(self, player))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id[] resources = utils.getObjIdArrayScriptVar(self, VAR_RESOURCE_TYPES);
        int index = sui.getListboxSelectedRow(params);
        if (resources == null || index < 0 || index >= resources.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id resourceType = resources[index];
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        float[] concentrations = utils.getFloatArrayScriptVar(self, VAR_RESOURCE_CONCENTRATIONS);
        if (concentrations == null || index >= concentrations.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!isSelectedResourceAvailable(planet, resourceClass, resourceType))
        {
            sendSystemMessage(player, "That resource is no longer active on the selected planet.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }

        resource_density activeResource = getActiveResourceDensity(planet, resourceClass, resourceType);
        if (activeResource == null)
        {
            sendSystemMessage(player, "That resource is no longer active on the selected planet.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_RESOURCE_TYPE, resourceType);
        showMiningResourceConfirmation(self, player, activeResource, concentrations[index]);
        return SCRIPT_CONTINUE;
    }

    public int handleMiningResourceConfirm(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || params.isEmpty() || sui.getIntButtonPressed(params) == sui.BP_CANCEL)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id player = sui.getPlayerId(params);
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        if (!utils.isNestedWithin(self, player) || !isSelectedResourceAvailable(planet, resourceClass, resourceType))
        {
            sendSystemMessage(player, "That resource is no longer active on the selected planet.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int amount = getMiningAmount(self);
        if (amount < 1)
        {
            sendSystemMessage(player, "This droid's base extraction rate is too low to return a resource unit at the 90% cap.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }

        if (utils.hasScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING))
        {
            return SCRIPT_CONTINUE;
        }
        if (!planetaryMiningDroidAdjustAccountFeatureId(player, self, 1))
        {
            sendSystemMessage(player, "The Planetary Mining Droid could not reserve an account mining slot.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING, 1);
        sendSystemMessage(player, "Reserving a planetary mining slot...", null);
        return SCRIPT_CONTINUE;
    }

    public int handlePlanetaryMiningDroidAccountFeatureResponse(obj_id self, dictionary params) throws InterruptedException
    {
        if (!utils.hasScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING))
        {
            return SCRIPT_CONTINUE;
        }
        utils.removeScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING);
        obj_id player = utils.getContainingPlayer(self);
        if (params == null || !params.getBoolean("success") || !isIdValid(player))
        {
            if (isIdValid(player))
            {
                sendSystemMessage(player, "This account already has three active Planetary Mining Droid jobs.", null);
            }
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        if (!isSelectedResourceAvailable(planet, resourceClass, resourceType))
        {
            planetaryMiningDroidAdjustAccountFeatureId(player, player, -1);
            sendSystemMessage(player, "That resource is no longer active on the selected planet.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        dictionary data = new dictionary();
        data.put("resourceType", resourceType);
        data.put("amount", getMiningAmount(self));
        data.put("planet", planet);
        messageTo(player, "handlePlanetaryMiningDroidReturn", data, getMiningTime(self), true);
        consumeCharge(self);
        sendSystemMessage(player, "The Planetary Mining Droid has been launched.", null);
        cleanScriptVars(self);
        return SCRIPT_CONTINUE;
    }

    public void showMiningResourceConfirmation(obj_id self, obj_id player, resource_density activeResource, float selectedDensity) throws InterruptedException
    {
        obj_id resourceType = activeResource.getResourceType();
        String resourceClass = getResourceClass(resourceType);
        String details = "Resource: " + getLocalizedResourceName(resourceType) + "\n";
        details += "Type: " + getLocalizedResourceClassName(resourceClass) + "\n";
        details += "Estimated concentration: " + Math.round(selectedDensity * 100) + "%\n\nAttributes:\n";
        resource_attribute[] attributes = getResourceAttributes(resourceType);
        if (attributes != null)
        {
            for (resource_attribute attribute : attributes) {
                details += utils.localizeSIDString(utils.packStringId(new string_id("obj_attr_n", attribute.getName()))) + ": " + attribute.getValue() + "\n";
            }
        }
        int pid = sui.createSUIPage(sui.SUI_MSGBOX, self, player, "handleMiningResourceConfirm");
        setSUIProperty(pid, sui.MSGBOX_TITLE, sui.PROP_TEXT, "Confirm Planetary Mining");
        setSUIProperty(pid, sui.MSGBOX_PROMPT, sui.PROP_TEXT, details);
        sui.msgboxButtonSetup(pid, sui.OK_CANCEL);
        setSUIProperty(pid, sui.MSGBOX_BTN_OK, sui.PROP_TEXT, "Launch");
        setSUIProperty(pid, sui.MSGBOX_BTN_CANCEL, sui.PROP_TEXT, "@back");
        sui.showSUIPage(pid);
    }

    public boolean isListSelectionValid(dictionary params) throws InterruptedException
    {
        return params != null && !params.isEmpty() && sui.getIntButtonPressed(params) != sui.BP_CANCEL && sui.getListboxSelectedRow(params) >= 0;
    }

    public String[] getAvailableResourceClasses(String planet) throws InterruptedException
    {
        Vector classes = new Vector();
        for (String resourceClass : RESOURCE_CLASSES) {
            obj_id[] resources = getAvailablePlanetResources(planet, resourceClass);
            if (resources != null && resources.length > 0)
            {
                classes.add(resourceClass);
            }
        }
        String[] result = new String[classes.size()];
        classes.toArray(result);
        return result;
    }

    public String[] getMiningResourceClassNames(String[] resourceClasses) throws InterruptedException
    {
        String[] names = new String[resourceClasses.length];
        for (int i = 0; i < resourceClasses.length; ++i)
        {
            for (int j = 0; j < RESOURCE_CLASSES.length; ++j)
            {
                if (RESOURCE_CLASSES[j].equals(resourceClasses[i]))
                {
                    names[i] = RESOURCE_CLASS_NAMES[j];
                    break;
                }
            }
        }
        return names;
    }

    public String getLocalizedResourceClassName(String resourceClass) throws InterruptedException
    {
        return utils.localizeSIDString(getResourceClassName(resourceClass));
    }

    public String getLocalizedResourceName(obj_id resourceType) throws InterruptedException
    {
        return utils.localizeSIDString(getResourceName(resourceType));
    }

    public obj_id[] getAvailablePlanetResources(String planet, String resourceClass) throws InterruptedException
    {
        resource_density[] resources = getAvailablePlanetResourceDensities(planet, resourceClass);
        if (resources == null || resources.length == 0)
        {
            return new obj_id[0];
        }
        obj_id[] result = new obj_id[resources.length];
        for (int i = 0; i < resources.length; ++i)
        {
            result[i] = resources[i].getResourceType();
        }
        return result;
    }

    public resource_density[] getAvailablePlanetResourceDensities(String planet, String resourceClass) throws InterruptedException
    {
        return requestResourceList(new location(0, 0, 0, planet), MIN_ACTIVE_DENSITY, 1.0f, resourceClass);
    }

    public resource_density getActiveResourceDensity(String planet, String resourceClass, obj_id resourceType) throws InterruptedException
    {
        resource_density[] resources = getAvailablePlanetResourceDensities(planet, resourceClass);
        if (resources == null)
        {
            return null;
        }
        for (resource_density resource : resources) {
            if (resource.getResourceType() == resourceType)
            {
                return resource;
            }
        }
        return null;
    }

    public String getSelectedResourceClass(obj_id self, obj_id resourceType) throws InterruptedException
    {
        String[] availableClasses = utils.getStringArrayScriptVar(self, VAR_RESOURCE_CLASS);
        if (availableClasses == null)
        {
            return "";
        }
        for (String resourceClass : availableClasses) {
            if (isResourceDerivedFrom(resourceType, resourceClass))
            {
                return resourceClass;
            }
        }
        return "";
    }

    public boolean isSelectedResourceAvailable(String planet, String resourceClass, obj_id resourceType) throws InterruptedException
    {
        if (resourceClass == null || resourceClass.equals(""))
        {
            return false;
        }
        obj_id[] activeResources = getAvailablePlanetResources(planet, resourceClass);
        for (obj_id activeResource : activeResources) {
            if (activeResource == resourceType)
            {
                return true;
            }
        }
        return false;
    }

    public int getMiningTime(obj_id self) throws InterruptedException
    {
        final int minTime = 15 * 60;
        final int maxTime = 60 * 60;
        float quality = getFloatObjVar(self, ATTRIBUTE_BASE + "mechanism_quality");
        if (quality <= 0)
        {
            quality = getFloatObjVar(self, LEGACY_STATIC_ATTRIBUTE_BASE + "mechanism_quality");
        }
        if (quality > 100)
        {
            quality = 100;
        }
        return minTime + (int)((maxTime - minTime) * ((100 - quality) / 100));
    }

    public int getMiningAmount(obj_id self) throws InterruptedException
    {
        float baseExtractionRate = getFloatObjVar(self, ATTRIBUTE_EXTRACTION_RATE);
        if (baseExtractionRate <= 0)
        {
            baseExtractionRate = getFloatObjVar(self, LEGACY_STATIC_ATTRIBUTE_BASE + "extractRate");
        }
        return (int)(baseExtractionRate * 0.9f);
    }

    public void consumeCharge(obj_id self) throws InterruptedException
    {
        int charges = getCount(self);
        if (charges > 1)
        {
            incrementCount(self, -1);
        }
        else
        {
            destroyObject(self);
        }
    }

    public void cleanScriptVars(obj_id self) throws InterruptedException
    {
        utils.removeScriptVar(self, VAR_PLANET);
        utils.removeScriptVar(self, VAR_RESOURCE_CLASS);
        utils.removeScriptVar(self, VAR_RESOURCE_SUBCLASSES);
        utils.removeScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        utils.removeScriptVar(self, VAR_RESOURCE_TYPES);
        utils.removeScriptVar(self, VAR_RESOURCE_CONCENTRATIONS);
        utils.removeScriptVar(self, VAR_RESOURCE_TYPE);
    }

    public int OnGetAttributes(obj_id self, obj_id player, String[] names, String[] attribs) throws InterruptedException
    {
        int index = utils.getValidAttributeIndex(names);
        if (index == -1)
        {
            return SCRIPT_CONTINUE;
        }
        int charges = getCount(self);
        if (charges > 0)
        {
            names[index] = "quantity";
            attribs[index++] = Integer.toString(charges);
        }
        if (index < names.length && hasObjVar(self, ATTRIBUTE_EXTRACTION_RATE))
        {
            names[index] = "extractRate";
            attribs[index++] = Integer.toString((int)getFloatObjVar(self, ATTRIBUTE_EXTRACTION_RATE));
        }
        if (index < names.length && hasObjVar(self, ATTRIBUTE_BASE + "mechanism_quality"))
        {
            names[index] = "mechanism_quality";
            attribs[index] = Integer.toString((int)getFloatObjVar(self, ATTRIBUTE_BASE + "mechanism_quality"));
        }
        return SCRIPT_CONTINUE;
    }
}
