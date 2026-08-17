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
    public static final String VAR_RESOURCE_TYPES = "planetary_mining.resource_types";
    public static final String VAR_SURVEY_CANDIDATES = "planetary_mining.survey_candidates";
    public static final String VAR_SURVEY_DENSITIES = "planetary_mining.survey_densities";
    public static final String VAR_SURVEY_INDEX = "planetary_mining.survey_index";
    public static final String VAR_RESOURCE_TYPE = "planetary_mining.resource_type";
    public static final String VAR_SURVEY_SELECTED = "planetary_mining.survey_selected";
    public static final String VAR_ACCOUNT_RESERVATION_PENDING = "planetary_mining.account_reservation_pending";
    public static final String ATTRIBUTE_BASE = craftinglib.COMPONENT_ATTRIBUTE_OBJVAR_NAME + ".";
    public static final String LEGACY_STATIC_ATTRIBUTE_BASE = "crafting.component_attribute.";
    public static final String ATTRIBUTE_EXTRACTION_RATE = ATTRIBUTE_BASE + "extractRate";
    public static final float MIN_ACTIVE_DENSITY = 0.0001f;
    public static final float MUSTAFAR_DISPLAY_MIN_COORDINATE = -4000.0f;
    public static final float MUSTAFAR_DISPLAY_MAX_COORDINATE = 4000.0f;

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
        if (!utils.isNestedWithin(self, player))
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
        if (!isEligibleMiningDroidUser(self, player) || index < 0 || index >= PLANET_INTERNAL.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
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
        if (!isEligibleMiningDroidUser(self, player))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
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
        if (!isEligibleMiningDroidUser(self, player))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
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
        if (planet.equals("mustafar") && (x < MUSTAFAR_DISPLAY_MIN_COORDINATE || x > MUSTAFAR_DISPLAY_MAX_COORDINATE || z < MUSTAFAR_DISPLAY_MIN_COORDINATE || z > MUSTAFAR_DISPLAY_MAX_COORDINATE))
        {
            sendSystemMessage(player, "Those coordinates are outside the client-display surveyable area.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        float worldX = x;
        float worldZ = z;
        if (planet.equals("mustafar"))
        {
            worldX = x - 2880.0f;
            worldZ = z + 2976.0f;
        }
        location surveyLocation = new location(worldX, 0, worldZ, planet);
        if (!isAllowedSurveyLocation(planet, surveyLocation))
        {
            sendSystemMessage(player, "Those coordinates are outside the client-display surveyable area.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_SURVEY_LOCATION, surveyLocation);
        utils.setScriptVar(self, VAR_RESOURCE_CLASS, RESOURCE_CLASSES);
        sui.listbox(self, player, "Select a survey type.", sui.OK_CANCEL, "Planetary Mining Droid", RESOURCE_CLASS_NAMES, "handleMiningClassSelection");
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
        if (!isEligibleMiningDroidUser(self, player))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
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
        if (!isAllowedSurveyLocation(planet, surveyLocation))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_SELECTED_RESOURCE_CLASS, resourceClass);
        startPmdSurveys(self, player, resourceClass, planet, surveyLocation);
        return SCRIPT_CONTINUE;
    }

    public int handleMiningResourceSelection(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || params.isEmpty())
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (sui.getIntButtonPressed(params) == sui.BP_CANCEL)
        {
            consumeCharge(self);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id player = sui.getPlayerId(params);
        if (!isEligibleMiningDroidUser(self, player))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        obj_id[] resourceTypes = utils.getObjIdArrayScriptVar(self, VAR_RESOURCE_TYPES);
        int index = sui.getListboxSelectedRow(params);
        if (resourceTypes == null || index < 0 || index >= resourceTypes.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id resourceType = resourceTypes[index];
        if (!isAllowedSurveyLocation(planet, surveyLocation) || resourceClass == null || resourceClass.equals("") || !isIdValid(resourceType) || !isSelectedResourceAvailable(self, resourceType))
        {
            sendSystemMessage(player, "That resource is no longer active at the selected coordinates.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_RESOURCE_TYPE, resourceType);
        utils.setScriptVar(self, VAR_SURVEY_SELECTED, 1);
        resource_density selectedResource = getResourceDensity(self, resourceType);
        if (selectedResource == null)
        {
            sendSystemMessage(player, "That resource is no longer active at the selected coordinates.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        showMiningResourceConfirmation(self, player, selectedResource, surveyLocation);
        return SCRIPT_CONTINUE;
    }

    public int handleMiningResourceConfirm(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || params.isEmpty())
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (sui.getIntButtonPressed(params) == sui.BP_CANCEL)
        {
            consumeCharge(self);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id player = sui.getPlayerId(params);
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (!utils.hasScriptVar(self, VAR_SURVEY_SELECTED) || !isEligibleMiningDroidUser(self, player) || !isAllowedSurveyLocation(planet, surveyLocation))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE) || buff.hasBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE))
        {
            sendSystemMessage(player, "Your Survey License is occupied by an active Planetary Mining Droid.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!utils.isNestedWithin(self, player) || !isSelectedResourceAvailable(self, resourceType))
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
        if (!isEligibleMiningDroidUser(self, player) || !isAllowedSurveyLocation(planet, surveyLocation) || !isSelectedResourceAvailable(self, resourceType))
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

    public void showMiningResourceConfirmation(obj_id self, obj_id player, resource_density activeResource, location selectedSite) throws InterruptedException
    {
        obj_id resourceType = activeResource.getResourceType();
        String prompt = "Launch the Planetary Mining Droid for " + getLocalizedResourceName(resourceType) + " at " + Math.round(activeResource.getDensity() * 100) + "% concentration at (" + Math.round(selectedSite.x) + ", " + Math.round(selectedSite.z) + ")?";
        sui.msgbox(self, player, prompt, sui.OK_CANCEL, "Planetary Mining Droid", "handleMiningResourceConfirm");
    }

    public boolean isListSelectionValid(dictionary params) throws InterruptedException
    {
        return params != null && !params.isEmpty() && sui.getIntButtonPressed(params) != sui.BP_CANCEL && sui.getListboxSelectedRow(params) >= 0;
    }

    public void startPmdSurveys(obj_id self, obj_id player, String resourceClass, String planet, location surveyLocation) throws InterruptedException
    {
        String[] leafResourceClasses = getLeafResourceChildClasses(resourceClass);
        if (leafResourceClasses == null)
        {
            cleanScriptVars(self);
            return;
        }
        if (leafResourceClasses.length == 0)
        {
            leafResourceClasses = new String[] { resourceClass };
        }
        Vector candidates = new Vector();
        for (String leafResourceClass : leafResourceClasses)
        {
            obj_id[] resourceTypes = getResourceTypes(leafResourceClass);
            if (resourceTypes != null)
            {
                for (obj_id resourceType : resourceTypes)
                {
                    candidates.add(resourceType);
                }
            }
        }
        obj_id[] candidateTypes = new obj_id[candidates.size()];
        candidates.toArray(candidateTypes);
        utils.setScriptVar(self, VAR_SURVEY_CANDIDATES, candidateTypes);
        utils.setScriptVar(self, VAR_RESOURCE_TYPES, new obj_id[0]);
        utils.setScriptVar(self, VAR_SURVEY_DENSITIES, new float[0]);
        utils.setScriptVar(self, VAR_SURVEY_INDEX, 0);
        requestNextPmdSurvey(self, player, resourceClass, planet, surveyLocation);
    }

    public void requestNextPmdSurvey(obj_id self, obj_id player, String resourceClass, String planet, location surveyLocation) throws InterruptedException
    {
        obj_id[] candidates = utils.getObjIdArrayScriptVar(self, VAR_SURVEY_CANDIDATES);
        int surveyIndex = utils.getIntScriptVar(self, VAR_SURVEY_INDEX);
        if (!isEligibleMiningDroidUser(self, player) || candidates == null || surveyIndex < 0)
        {
            cleanScriptVars(self);
            return;
        }
        if (surveyIndex >= candidates.length)
        {
            showPmdSurveyResults(self, player);
            return;
        }
        obj_id resourceType = candidates[surveyIndex];
        utils.setScriptVar(self, VAR_SURVEY_INDEX, surveyIndex + 1);
        String resourceName = getResourceName(resourceType);
        if (!requestPmdSurvey(player, self, resourceClass, resourceName, planet, surveyLocation.x, surveyLocation.z))
        {
            cleanScriptVars(self);
        }
    }

    public int OnSurveyDataReceived(obj_id self, float[] xVals, float[] zVals, float[] efficiencies) throws InterruptedException
    {
        obj_id player = utils.getContainingPlayer(self);
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        obj_id[] candidates = utils.getObjIdArrayScriptVar(self, VAR_SURVEY_CANDIDATES);
        int surveyIndex = utils.getIntScriptVar(self, VAR_SURVEY_INDEX);
        if (!isEligibleMiningDroidUser(self, player) || candidates == null || surveyIndex <= 0 || surveyIndex > candidates.length)
        {
            return SCRIPT_CONTINUE;
        }
        if (efficiencies != null && efficiencies.length == 9)
        {
            float bestDensity = 0.0f;
            for (float efficiency : efficiencies)
            {
                if (efficiency > bestDensity)
                {
                    bestDensity = efficiency;
                }
            }
            if (bestDensity >= MIN_ACTIVE_DENSITY)
            {
                obj_id[] resourceTypes = utils.getObjIdArrayScriptVar(self, VAR_RESOURCE_TYPES);
                float[] densities = utils.getFloatArrayScriptVar(self, VAR_SURVEY_DENSITIES);
                int resultCount = resourceTypes.length;
                obj_id[] updatedTypes = new obj_id[resultCount + 1];
                float[] updatedDensities = new float[resultCount + 1];
                System.arraycopy(resourceTypes, 0, updatedTypes, 0, resultCount);
                System.arraycopy(densities, 0, updatedDensities, 0, resultCount);
                updatedTypes[resultCount] = candidates[surveyIndex - 1];
                updatedDensities[resultCount] = bestDensity;
                utils.setScriptVar(self, VAR_RESOURCE_TYPES, updatedTypes);
                utils.setScriptVar(self, VAR_SURVEY_DENSITIES, updatedDensities);
            }
        }
        requestNextPmdSurvey(self, player, resourceClass, planet, surveyLocation);
        return SCRIPT_CONTINUE;
    }

    public void showPmdSurveyResults(obj_id self, obj_id player) throws InterruptedException
    {
        obj_id[] resourceTypes = utils.getObjIdArrayScriptVar(self, VAR_RESOURCE_TYPES);
        float[] densities = utils.getFloatArrayScriptVar(self, VAR_SURVEY_DENSITIES);
        if (resourceTypes == null || densities == null || resourceTypes.length == 0 || resourceTypes.length != densities.length)
        {
            sendSystemMessage(player, "No resources of that type are currently available at those coordinates.", null);
            cleanScriptVars(self);
            return;
        }
        String[] resourceNames = new String[resourceTypes.length];
        for (int i = 0; i < resourceTypes.length; ++i)
        {
            resourceNames[i] = getLocalizedResourceName(resourceTypes[i]) + ": " + Math.round(densities[i] * 100) + "%";
        }
        sui.listbox(self, player, "Survey results. Select a resource to launch the droid.", sui.OK_CANCEL, "Planetary Mining Droid", resourceNames, "handleMiningResourceSelection");
    }

    public String getLocalizedResourceName(obj_id resourceType) throws InterruptedException
    {
        return utils.localizeSIDString(getResourceName(resourceType));
    }

    public boolean isEligibleMiningDroidUser(obj_id self, obj_id player) throws InterruptedException
    {
        return isIdValid(player) && utils.isNestedWithin(self, player) && utils.isProfession(player, utils.TRADER) && getTopMostContainer(player) == player;
    }

    public boolean isAllowedSurveyLocation(String planet, location surveyLocation) throws InterruptedException
    {
        if (surveyLocation == null || planet == null || !planet.equals(surveyLocation.area))
        {
            return false;
        }
        if (planet.equals("mustafar"))
        {
            return true;
        }
        for (String kashyyykScene : KASHYYYK_SCENES)
        {
            if (planet.equals(kashyyykScene))
            {
                return true;
            }
        }
        return false;
    }

    public resource_density getResourceDensity(obj_id self, obj_id resourceType) throws InterruptedException
    {
        obj_id[] resourceTypes = utils.getObjIdArrayScriptVar(self, VAR_RESOURCE_TYPES);
        float[] densities = utils.getFloatArrayScriptVar(self, VAR_SURVEY_DENSITIES);
        if (resourceTypes != null && densities != null && resourceTypes.length == densities.length)
        {
            for (int i = 0; i < resourceTypes.length; ++i)
            {
                if (resourceTypes[i] == resourceType)
                {
                    return new resource_density(resourceType, densities[i]);
                }
            }
        }
        return null;
    }

    public boolean isSelectedResourceAvailable(obj_id self, obj_id resourceType) throws InterruptedException
    {
        return getResourceDensity(self, resourceType) != null;
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
        utils.removeScriptVar(self, VAR_RESOURCE_TYPES);
        utils.removeScriptVar(self, VAR_SURVEY_CANDIDATES);
        utils.removeScriptVar(self, VAR_SURVEY_DENSITIES);
        utils.removeScriptVar(self, VAR_SURVEY_INDEX);
        utils.removeScriptVar(self, VAR_RESOURCE_TYPE);
        utils.removeScriptVar(self, VAR_SURVEY_SELECTED);
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
