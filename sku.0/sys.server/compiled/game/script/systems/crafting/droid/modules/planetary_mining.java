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
    public static final String[] KASHYYYK_SCENE_NAMES =
    {
        "Kashyyyk Main",
        "Dead Forest",
        "Hunting Grounds"
    };
    public static final String[] KASHYYYK_SCENES =
    {
        "kashyyyk_main",
        "kashyyyk_dead_forest",
        "kashyyyk_hunting"
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
    public static final String VAR_SURVEY_LOCATION = "planetary_mining.survey_location";
    public static final String VAR_RESOURCE_CLASS = "planetary_mining.resource_class";
    public static final String VAR_SELECTED_RESOURCE_CLASS = "planetary_mining.selected_resource_class";
    public static final String VAR_RESOURCE_TYPE = "planetary_mining.resource_type";
    public static final String VAR_ACCOUNT_RESERVATION_PENDING = "planetary_mining.account_reservation_pending";
    public static final String ATTRIBUTE_BASE = craftinglib.COMPONENT_ATTRIBUTE_OBJVAR_NAME + ".";
    public static final String LEGACY_STATIC_ATTRIBUTE_BASE = "crafting.component_attribute.";
    public static final String ATTRIBUTE_EXTRACTION_RATE = ATTRIBUTE_BASE + "extractRate";
    public static final float MIN_ACTIVE_DENSITY = 0.0001f;
    public static final float MUSTAFAR_MIN_X = -3523.0f;
    public static final float MUSTAFAR_MAX_X = 3564.0f;
    public static final float MUSTAFAR_MIN_Z = -3363.0f;
    public static final float MUSTAFAR_MAX_Z = 3600.0f;

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
        if (hasObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE) || buff.hasBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE))
        {
            sendSystemMessage(player, "Your Survey License is occupied by an active Planetary Mining Droid.", null);
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
        if (index == 0)
        {
            sui.listbox(self, player, "Select the Kashyyyk scene to survey.", sui.OK_CANCEL, "Planetary Mining Droid", KASHYYYK_SCENE_NAMES, "handleMiningKashyyykSceneSelection");
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_PLANET, PLANET_INTERNAL[index]);
        promptForMiningCoordinates(self, player);
        return SCRIPT_CONTINUE;
    }

    public int handleMiningKashyyykSceneSelection(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isListSelectionValid(params))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int index = sui.getListboxSelectedRow(params);
        if (index < 0 || index >= KASHYYYK_SCENES.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id player = sui.getPlayerId(params);
        utils.setScriptVar(self, VAR_PLANET, KASHYYYK_SCENES[index]);
        promptForMiningCoordinates(self, player);
        return SCRIPT_CONTINUE;
    }

    public void promptForMiningCoordinates(obj_id self, obj_id player) throws InterruptedException
    {
        sui.inputbox(self, player, "Enter X and Z coordinates, separated by a comma.", sui.OK_CANCEL, "Planetary Mining Droid", sui.INPUT_NORMAL, null, "handleMiningCoordinates", null);
    }

    public int handleMiningCoordinates(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || params.isEmpty() || sui.getIntButtonPressed(params) == sui.BP_CANCEL)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id player = sui.getPlayerId(params);
        String[] coordinates = sui.getInputBoxText(params).trim().split("[,\\s]+");
        if (coordinates.length != 2)
        {
            sendSystemMessage(player, "Enter exactly two coordinates, for example: -500, 1200.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        float x;
        float z;
        try
        {
            x = Float.parseFloat(coordinates[0]);
            z = Float.parseFloat(coordinates[1]);
        }
        catch (NumberFormatException exception)
        {
            sendSystemMessage(player, "Coordinates must be numbers.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        if (Float.isNaN(x) || Float.isInfinite(x) || Float.isNaN(z) || Float.isInfinite(z))
        {
            sendSystemMessage(player, "Coordinates must be finite numbers.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        if (planet.equals("mustafar") && (x < MUSTAFAR_MIN_X || x > MUSTAFAR_MAX_X || z < MUSTAFAR_MIN_Z || z > MUSTAFAR_MAX_Z))
        {
            sendSystemMessage(player, "Those coordinates are outside Mustafar's surveyable area.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        location surveyLocation = new location(x, 0, z, planet);
        String[] availableClasses = getAvailableResourceClasses(planet, surveyLocation);
        if (availableClasses.length == 0)
        {
            sendSystemMessage(player, "No supported resources are currently available at those coordinates.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_SURVEY_LOCATION, surveyLocation);
        utils.setScriptVar(self, VAR_RESOURCE_CLASS, availableClasses);
        sui.listbox(self, player, "Select a survey type.", sui.OK_CANCEL, "Planetary Mining Droid", getMiningResourceClassNames(availableClasses), "handleMiningClassSelection");
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
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        String resourceClass = availableClasses[index];
        resource_density selectedResource = getHighestResourceDensity(planet, surveyLocation, resourceClass);
        if (selectedResource == null)
        {
            sendSystemMessage(player, "No active resources of that type are currently available at those coordinates.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_SELECTED_RESOURCE_CLASS, resourceClass);
        utils.setScriptVar(self, VAR_RESOURCE_TYPE, selectedResource.getResourceType());
        showMiningResourceConfirmation(self, player, selectedResource, surveyLocation, selectedResource.getDensity());
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
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (hasObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE) || buff.hasBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE))
        {
            sendSystemMessage(player, "Your Survey License is occupied by an active Planetary Mining Droid.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!utils.isNestedWithin(self, player) || !isSelectedResourceAvailable(planet, surveyLocation, resourceClass, resourceType))
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
        if (params.getInt("newValue") > 3)
        {
            planetaryMiningDroidAdjustAccountFeatureId(player, player, -1);
            sendSystemMessage(player, "This account already has three active Planetary Mining Droid jobs.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (!isSelectedResourceAvailable(planet, surveyLocation, resourceClass, resourceType))
        {
            planetaryMiningDroidAdjustAccountFeatureId(player, player, -1);
            sendSystemMessage(player, "That resource is no longer active on the selected planet.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!buff.applyBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE, getMiningTime(self)))
        {
            planetaryMiningDroidAdjustAccountFeatureId(player, player, -1);
            sendSystemMessage(player, "The Planetary Mining Droid could not occupy your Survey License.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        setObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE, 1);
        dictionary data = new dictionary();
        int surveying = getSkillStatMod(player, "surveying");
        int resourceSamplingIncrease = getSkillStatisticModifier(player, "expertise_resource_sampling_increase");
        boolean falleensFist = buff.hasBuff(player, "tcg_series4_falleens_fist");
        data.put("resourceType", resourceType);
        data.put("amount", getMiningAmount(self));
        data.put("planet", planet);
        data.put("surveying", surveying);
        data.put("resourceSamplingIncrease", resourceSamplingIncrease);
        data.put("falleensFist", falleensFist);
        sendSystemMessage(player, "PMD snapshot: surveying=" + surveying + ", sampling bonus=" + resourceSamplingIncrease + "%, Falleen's Fist=" + falleensFist, null);
        messageTo(player, "handlePlanetaryMiningDroidReturn", data, getMiningTime(self), true);
        consumeCharge(self);
        sendSystemMessage(player, "The Planetary Mining Droid has been launched.", null);
        cleanScriptVars(self);
        return SCRIPT_CONTINUE;
    }

    public void showMiningResourceConfirmation(obj_id self, obj_id player, resource_density activeResource, location selectedSite, float selectedDensity) throws InterruptedException
    {
        obj_id resourceType = activeResource.getResourceType();
        String resourceClass = getResourceClass(resourceType);
        String details = "Resource: " + getLocalizedResourceName(resourceType) + "\n";
        details += "Type: " + getLocalizedResourceClassName(resourceClass) + "\n";
        details += "Selected concentration: " + Math.round(selectedDensity * 100) + "% at (" + Math.round(selectedSite.x) + ", " + Math.round(selectedSite.z) + ")\n";
        details += "Highest concentration detected at these coordinates.\n\nAttributes:\n";
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

    public String[] getAvailableResourceClasses(String planet, location surveyLocation) throws InterruptedException
    {
        Vector classes = new Vector();
        for (String resourceClass : RESOURCE_CLASSES) {
            resource_density[] resources = getAvailablePlanetResourceDensities(planet, surveyLocation, resourceClass);
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

    public resource_density[] getAvailablePlanetResourceDensities(String planet, location surveyLocation, String resourceClass) throws InterruptedException
    {
        return requestResourceList(surveyLocation, MIN_ACTIVE_DENSITY, 1.0f, resourceClass);
    }

    public resource_density getHighestResourceDensity(String planet, location surveyLocation, String resourceClass) throws InterruptedException
    {
        resource_density[] resources = getAvailablePlanetResourceDensities(planet, surveyLocation, resourceClass);
        if (resources == null)
        {
            return null;
        }
        resource_density highest = null;
        for (resource_density resource : resources) {
            if (highest == null || resource.getDensity() > highest.getDensity())
            {
                highest = resource;
            }
        }
        return highest;
    }

    public boolean isSelectedResourceAvailable(String planet, location surveyLocation, String resourceClass, obj_id resourceType) throws InterruptedException
    {
        if (surveyLocation == null || resourceClass == null || resourceClass.equals(""))
        {
            return false;
        }
        resource_density[] activeResources = getAvailablePlanetResourceDensities(planet, surveyLocation, resourceClass);
        if (activeResources == null)
        {
            return false;
        }
        for (resource_density activeResource : activeResources) {
            if (activeResource.getResourceType() == resourceType)
            {
                return true;
            }
        }
        return false;
    }

    public int getMiningTime(obj_id self) throws InterruptedException
    {
        return 30;
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
        utils.removeScriptVar(self, VAR_SURVEY_LOCATION);
        utils.removeScriptVar(self, VAR_RESOURCE_CLASS);
        utils.removeScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
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
