package script.systems.crafting.droid.modules;

import script.*;
import script.library.*;

import java.util.Vector;

public class planetary_mining extends script.base_script
{
    public static final String STF = "planetary_mining_droid";
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
        "Kachirho",
        "Dead Forest",
        "Etyyy",
        "Rryatt Trail"
    };
    public static final String[] KASHYYYK_SCENES =
    {
        "kashyyyk_main",
        "kashyyyk_dead_forest",
        "kashyyyk_hunting",
        "kashyyyk_rryatt_trail"
    };
    public static final String[] MUSTAFAR_RESOURCE_CLASSES =
    {
        "mineral",
        "chemical",
        "gas",
        "flora_resources",
        "water"
    };
    public static final String[] KASHYYYK_RESOURCE_CLASSES =
    {
        "chemical",
        "flora_resources",
        "water",
        "energy"
    };
    public static final String[] MUSTAFAR_MINERAL_RESOURCE_TYPES =
    {
        "petrochem_fuel_solid_mustafar", "radioactive_mustafar", "steel_mustafar", "iron_mustafar",
        "aluminum_mustafar", "copper_mustafar", "ore_extrusive_mustafar", "ore_intrusive_mustafar",
        "armophous_mustafar_1", "armophous_mustafar_2", "crystalline_mustafar_1", "crystalline_mustafar_2"
    };
    public static final String[] MUSTAFAR_CHEMICAL_RESOURCE_TYPES =
    {
        "fiberplast_mustafar", "petrochem_fuel_liquid_mustafar"
    };
    public static final String[] MUSTAFAR_GAS_RESOURCE_TYPES =
    {
        "gas_reactive_mustafar"
    };
    public static final String[] MUSTAFAR_FLORA_RESOURCE_TYPES =
    {
        "corn_domesticated_mustafar", "corn_wild_mustafar", "rice_domesticated_mustafar", "rice_wild_mustafar",
        "oats_domesticated_mustafar", "oats_wild_mustafar", "wheat_domesticated_mustafar", "wheat_wild_mustafar",
        "vegetable_greens_mustafar", "vegetable_beans_mustafar", "vegetable_tubers_mustafar", "vegetable_fungi_mustafar",
        "fruit_fruits_mustafar", "fruit_berries_mustafar", "fruit_flowers_mustafar", "wood_deciduous_mustafar",
        "softwood_conifer_mustafar", "softwood_evergreen_mustafar"
    };
    public static final String[] MUSTAFAR_WATER_RESOURCE_TYPES =
    {
        "water_vapor_mustafar"
    };
    public static final String[] KASHYYYK_CHEMICAL_RESOURCE_TYPES =
    {
        "fiberplast_kashyyyk"
    };
    public static final String[] KASHYYYK_FLORA_RESOURCE_TYPES =
    {
        "corn_domesticated_kashyyyk", "corn_wild_kashyyyk", "rice_domesticated_kashyyyk", "rice_wild_kashyyyk",
        "oats_domesticated_kashyyyk", "oats_wild_kashyyyk", "wheat_domesticated_kashyyyk", "wheat_wild_kashyyyk",
        "vegetable_greens_kashyyyk", "vegetable_beans_kashyyyk", "vegetable_tubers_kashyyyk", "vegetable_fungi_kashyyyk",
        "fruit_fruits_kashyyyk", "fruit_berries_kashyyyk", "fruit_flowers_kashyyyk", "wood_deciduous_kashyyyk",
        "softwood_conifer_kashyyyk", "softwood_evergreen_kashyyyk"
    };
    public static final String[] KASHYYYK_WATER_RESOURCE_TYPES =
    {
        "water_vapor_kashyyyk"
    };
    public static final String[] KASHYYYK_ENERGY_RESOURCE_TYPES =
    {
        "energy_renewable_site_limited_tidal_kashyyyk", "energy_renewable_site_limited_hydron3_kashyyyk",
        "energy_renewable_site_limited_geothermal_kashyyyk", "energy_renewable_unlimited_wind_kashyyyk",
        "energy_renewable_unlimited_solar_kashyyyk"
    };
    public static final String VAR_PLANET = "planetary_mining.planet";
    public static final String VAR_SURVEY_LOCATION = "planetary_mining.survey_location";
    public static final String VAR_RESOURCE_CLASS = "planetary_mining.resource_class";
    public static final String VAR_SELECTED_RESOURCE_CLASS = "planetary_mining.selected_resource_class";
    public static final String VAR_SURVEY_CANDIDATES = "planetary_mining.survey_candidates";
    public static final String VAR_SURVEY_INDEX = "planetary_mining.survey_index";
    public static final String VAR_SURVEY_RESULT_TYPES = "planetary_mining.survey_result_types";
    public static final String VAR_SURVEY_RESULT_DENSITIES = "planetary_mining.survey_result_densities";
    public static final String VAR_SURVEY_RESULT_LOCATIONS = "planetary_mining.survey_result_locations";
    public static final String VAR_SURVEY_BEST_DENSITY = "planetary_mining.survey_best_density";
    public static final String VAR_SURVEY_BEST_LOCATION = "planetary_mining.survey_best_location";
    public static final String VAR_RESOURCE_TYPE = "planetary_mining.resource_type";
    public static final String VAR_SURVEY_SELECTED = "planetary_mining.survey_selected";
    public static final String VAR_ACCOUNT_RESERVATION_PENDING = "planetary_mining.account_reservation_pending";
    public static final String VAR_FLOW_PLAYER = "planetary_mining.flow.player";
    public static final String VAR_FLOW_ITEM = "planetary_mining.flow.item";
    public static final String VAR_FLOW_PID = "planetary_mining.flow.pid";
    public static final String VAR_LAUNCH_COUNTDOWN = "planetary_mining.launch_countdown";
    public static final String VAR_PENDING_LAUNCH_ITEM = "planetary_mining.pending_launch_item";
    public static final String VAR_PENDING_LAUNCH_OPERATION = "planetary_mining.pending_launch_operation";
    public static final String VAR_JOB_SEQUENCE = "planetary_mining.job_sequence";
    public static final String VAR_ACTIVE_JOB_SEQUENCE = resource.VAR_PLANETARY_MINING_ACTIVE_JOB_SEQUENCE;
    public static final String VAR_PENDING_RELEASE_SEQUENCES = "planetary_mining.pending_release_sequences";
    public static final String VAR_RELEASE_IN_FLIGHT = "planetary_mining.release_in_flight";
    public static final String VAR_LAUNCH_JOB_SEQUENCE = "planetary_mining.launch_job_sequence";
    public static final String VAR_LAUNCH_SURVEYING = "planetary_mining.launch_surveying";
    public static final String VAR_LAUNCH_DENSITY = "planetary_mining.launch_density";
    public static final String VAR_LAUNCH_SAMPLING_INTERVAL = "planetary_mining.launch_sampling_interval";
    public static final String VAR_LAUNCH_SAMPLING_INCREASE = "planetary_mining.launch_sampling_increase";
    public static final String VAR_LAUNCH_FALLEENS_FIST = "planetary_mining.launch_falleens_fist";
    public static final String PID_NAME = "planetaryMiningDroid";
    public static final String DISPLAY_NAME = "Interplanetary Mining Droid";
    public static final String ATTRIBUTE_BASE = craftinglib.COMPONENT_ATTRIBUTE_OBJVAR_NAME + ".";
    public static final String ATTRIBUTE_DURATION = ATTRIBUTE_BASE + "duration";
    public static final float MIN_ACTIVE_DENSITY = 0.0001f;
    public static final int BASE_SAMPLING_DELAY = 25;
    public static final int MIN_SAMPLING_DELAY = 10;
    public static final int SAMPLE_LOOP_OVERHEAD = 3;
    public static final float RRYATT_TRAIL_WORLD_OFFSET_X = 1294.0f;
    public static final float RRYATT_TRAIL_WORLD_OFFSET_Z = 3880.0f;
    public static final int MAX_COORDINATE_INPUT_LENGTH = 32;

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        setName(self, DISPLAY_NAME);
        return SCRIPT_CONTINUE;
    }

    public int OnDestroy(obj_id self) throws InterruptedException
    {
        cleanScriptVars(self);
        return SCRIPT_CONTINUE;
    }

    public int OnAboutToBeTransferred(obj_id self, obj_id destination, obj_id transferer) throws InterruptedException
    {
        return utils.hasScriptVar(self, VAR_FLOW_PLAYER) ? SCRIPT_OVERRIDE : SCRIPT_CONTINUE;
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
            sendSystemMessage(player, new string_id(STF, "interface_too_complex"));
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE) || buff.hasBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE))
        {
            sendSystemMessage(player, resource.SID_PLANETARY_MINING_SURVEY_LICENSE_OCCUPIED);
            return SCRIPT_CONTINUE;
        }
        if (getTopMostContainer(player) != player)
        {
            sendSystemMessage(player, new string_id(STF, "must_be_outdoors"));
            return SCRIPT_OVERRIDE;
        }
        if (!beginFlow(self, player))
        {
            sendSystemMessage(player, new string_id(STF, "interface_already_active"));
            return SCRIPT_CONTINUE;
        }
        trackSui(self, player, sui.listbox(self, player, "Select the planet to mine.", sui.OK_CANCEL, DISPLAY_NAME, PLANET_NAMES, "handleMiningPlanetSelection"));
        return SCRIPT_CONTINUE;
    }

    public int handleMiningPlanetSelection(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
        if (!isListSelectionValid(params))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int index = sui.getListboxSelectedRow(params);
        if (!isEligibleMiningDroidUser(self, player) || index < 0 || index >= PLANET_INTERNAL.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (index == 0)
        {
            trackSui(self, player, sui.listbox(self, player, "Select the Kashyyyk scene to survey.", sui.OK_CANCEL, DISPLAY_NAME, KASHYYYK_SCENE_NAMES, "handleMiningKashyyykSceneSelection"));
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_PLANET, PLANET_INTERNAL[index]);
        promptForMiningCoordinates(self, player);
        return SCRIPT_CONTINUE;
    }

    public int handleMiningKashyyykSceneSelection(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
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
        trackSui(self, player, sui.inputbox(self, player, "Enter X, Z or X, Y, Z. You may paste /waypoint coordinates.", sui.OK_CANCEL, DISPLAY_NAME, sui.INPUT_NORMAL, null, "handleMiningCoordinates", null));
    }

    public int handleMiningCoordinates(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
        if (params == null || params.isEmpty() || sui.getIntButtonPressed(params) == sui.BP_CANCEL)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!isEligibleMiningDroidUser(self, player))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        String coordinateInput = sui.getInputBoxText(params);
        if (coordinateInput == null)
        {
            sendSystemMessage(player, new string_id(STF, "enter_coordinates"));
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        coordinateInput = coordinateInput.trim().replaceFirst("(?i)^/waypoint(?:[,\\s]+)", "");
        if (coordinateInput.length() > MAX_COORDINATE_INPUT_LENGTH)
        {
            sendSystemMessage(player, new string_id(STF, "coordinate_input_too_long"));
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        String[] coordinates = coordinateInput.split("[,\\s]+");
        if (coordinates.length != 2 && coordinates.length != 3)
        {
            sendSystemMessage(player, new string_id(STF, "coordinate_format"));
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        float[] values = new float[coordinates.length];
        try
        {
            for (int i = 0; i < coordinates.length; ++i)
            {
                values[i] = Float.parseFloat(coordinates[i]);
            }
        }
        catch (NumberFormatException exception)
        {
            sendSystemMessage(player, new string_id(STF, "coordinate_numbers"));
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        for (float value : values)
        {
            if (Float.isNaN(value) || Float.isInfinite(value))
            {
                sendSystemMessage(player, new string_id(STF, "coordinate_finite"));
                promptForMiningCoordinates(self, player);
                return SCRIPT_CONTINUE;
            }
        }
        float x = values[0];
        float z = values[values.length - 1];
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        if (planet.equals("mustafar") && (x < planetary_mining_regions.MUSTAFAR_DISPLAY_MIN_COORDINATE + planetary_mining_regions.SURVEY_GRID_RADIUS || x > planetary_mining_regions.MUSTAFAR_DISPLAY_MAX_COORDINATE - planetary_mining_regions.SURVEY_GRID_RADIUS || z < planetary_mining_regions.MUSTAFAR_DISPLAY_MIN_COORDINATE + planetary_mining_regions.SURVEY_GRID_RADIUS || z > planetary_mining_regions.MUSTAFAR_DISPLAY_MAX_COORDINATE - planetary_mining_regions.SURVEY_GRID_RADIUS))
        {
            sendSystemMessage(player, new string_id(STF, "outside_survey_area"));
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
        else if (planet.equals("kashyyyk_rryatt_trail"))
        {
            worldX = x + RRYATT_TRAIL_WORLD_OFFSET_X;
            worldZ = z + RRYATT_TRAIL_WORLD_OFFSET_Z;
        }
        location surveyLocation = new location(worldX, 0, worldZ, planet);
        if (!planetary_mining_regions.isAllowedSurveyGrid(planet, surveyLocation))
        {
            if (planet.equals("kashyyyk_rryatt_trail"))
            {
                sendSystemMessage(player, new string_id(STF, "rryatt_too_dangerous"));
            }
            else if (planet.equals("kashyyyk_dead_forest"))
            {
                sendSystemMessage(player, new string_id(STF, "dead_forest_prevents_survey"));
            }
            else
            {
                sendSystemMessage(player, new string_id(STF, "outside_survey_area"));
            }
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_SURVEY_LOCATION, surveyLocation);
        String[] resourceClasses = getPmdResourceClasses(planet);
        String[] resourceClassNames = getPmdResourceClassNames(resourceClasses);
        utils.setScriptVar(self, VAR_RESOURCE_CLASS, resourceClasses);
        trackSui(self, player, sui.listbox(self, player, "Select a survey type.", sui.OK_CANCEL, DISPLAY_NAME, resourceClassNames, "handleMiningClassSelection"));
        return SCRIPT_CONTINUE;
    }

    public int handleMiningClassSelection(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
        if (!isListSelectionValid(params))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
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
        if (!planetary_mining_regions.isAllowedSurveyGrid(planet, surveyLocation))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_SELECTED_RESOURCE_CLASS, resourceClass);
        startPmdSurveys(self, player, resourceClass, planet, surveyLocation);
        return SCRIPT_CONTINUE;
    }

    public int handleMiningResourceConfirm(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
        if (params == null || params.isEmpty())
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (sui.getIntButtonPressed(params) == sui.BP_CANCEL)
        {
            utils.removeScriptVar(self, VAR_RESOURCE_TYPE);
            utils.removeScriptVar(self, VAR_SURVEY_BEST_DENSITY);
            utils.removeScriptVar(self, VAR_SURVEY_BEST_LOCATION);
            utils.removeScriptVar(self, VAR_SURVEY_SELECTED);
            showPmdSurveyResults(self, player);
            return SCRIPT_CONTINUE;
        }
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (!utils.hasScriptVar(self, VAR_SURVEY_SELECTED) || !isEligibleMiningDroidUser(self, player) || !planetary_mining_regions.isAllowedSurveyGrid(planet, surveyLocation))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE) || buff.hasBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE))
        {
            sendSystemMessage(player, resource.SID_PLANETARY_MINING_SURVEY_LICENSE_OCCUPIED);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!utils.isNestedWithin(self, player) || !isSelectedResourceValid(self, resourceType))
        {
            sendSystemMessage(player, new string_id(STF, "resource_inactive"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (utils.hasScriptVar(self, VAR_LAUNCH_COUNTDOWN) || utils.hasScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING))
        {
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_LAUNCH_COUNTDOWN, 3);
        sendSystemMessage(player, new string_id(STF, "launch_preparing"));
        sendSystemMessageProse(player, prose.getPackage(new string_id(STF, "launch_countdown"), 3));
        dictionary countdown = new dictionary();
        countdown.put("step", 2);
        messageTo(self, "handlePlanetaryMiningDroidLaunchCountdown", countdown, 1.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int handlePlanetaryMiningDroidLaunchCountdown(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = utils.getObjIdScriptVar(self, VAR_FLOW_PLAYER);
        if (!isEligibleMiningDroidUser(self, player) || params == null || !utils.hasScriptVar(self, VAR_LAUNCH_COUNTDOWN))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int step = params.getInt("step");
        if (step > 0)
        {
            utils.setScriptVar(self, VAR_LAUNCH_COUNTDOWN, step);
            sendSystemMessageProse(player, prose.getPackage(new string_id(STF, "launch_countdown"), step));
            dictionary countdown = new dictionary();
            countdown.put("step", step - 1);
            messageTo(self, "handlePlanetaryMiningDroidLaunchCountdown", countdown, 1.0f, false);
            return SCRIPT_CONTINUE;
        }

        utils.removeScriptVar(self, VAR_LAUNCH_COUNTDOWN);
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (!planetary_mining_regions.isAllowedSurveyGrid(planet, surveyLocation) || !isSelectedResourceValid(self, resourceType))
        {
            sendSystemMessage(player, new string_id(STF, "resource_lost"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int amount = getMiningAmount(self, player);
        if (amount < 1)
        {
            sendSystemMessage(player, new string_id(STF, "yield_too_low"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE) || buff.hasBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE))
        {
            sendSystemMessage(player, resource.SID_PLANETARY_MINING_SURVEY_LICENSE_OCCUPIED);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int jobSequence = getIntObjVar(player, VAR_JOB_SEQUENCE) + 1;
        setObjVar(player, VAR_JOB_SEQUENCE, jobSequence);
        String operationId = "reserve:" + jobSequence;
        setObjVar(player, VAR_PENDING_LAUNCH_ITEM, self);
        setObjVar(player, VAR_PENDING_LAUNCH_OPERATION, operationId);
        utils.setScriptVar(self, VAR_LAUNCH_JOB_SEQUENCE, jobSequence);
        if (!planetaryMiningDroidUpdateAccountJob(player, player, jobSequence, true))
        {
            clearPendingLaunch(player, operationId);
            sendSystemMessage(player, new string_id(STF, "account_slot_reservation_failed"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING, 1);
        return SCRIPT_CONTINUE;
    }

    public int handlePlanetaryMiningDroidAccountJobResponse(obj_id self, dictionary params) throws InterruptedException
    {
        if (!utils.hasScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id player = utils.getObjIdScriptVar(self, VAR_FLOW_PLAYER);
        int jobSequence = utils.getIntScriptVar(self, VAR_LAUNCH_JOB_SEQUENCE);
        String operationId = "reserve:" + jobSequence;
        if (params == null || !operationId.equals(params.getString("operationId")))
        {
            return SCRIPT_CONTINUE;
        }
        utils.removeScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING);
        if (params == null || !params.getBoolean("success") || !isIdValid(player))
        {
            if (isIdValid(player))
            {
                clearPendingLaunch(player, operationId);
                sendSystemMessage(player, new string_id(STF, "account_job_limit"));
            }
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (params.getInt("newValue") > 3)
        {
            clearPendingLaunch(player, operationId);
            queuePlanetaryMiningDroidRelease(player, jobSequence);
            sendSystemMessage(player, new string_id(STF, "account_job_limit"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (!isEligibleMiningDroidUser(self, player) || !planetary_mining_regions.isAllowedSurveyGrid(planet, surveyLocation) || !isSelectedResourceValid(self, resourceType))
        {
            clearPendingLaunch(player, operationId);
            queuePlanetaryMiningDroidRelease(player, jobSequence);
            sendSystemMessage(player, new string_id(STF, "resource_lost"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int amount = getMiningAmount(self, player);
        if (amount < 1)
        {
            clearPendingLaunch(player, operationId);
            queuePlanetaryMiningDroidRelease(player, jobSequence);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!buff.applyBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE, getMiningTime(self)))
        {
            clearPendingLaunch(player, operationId);
            queuePlanetaryMiningDroidRelease(player, jobSequence);
            sendSystemMessage(player, new string_id(STF, "survey_license_reservation_failed"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int miningDuration = getMiningTime(self);
        setObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE, 1);
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_RESOURCE, resourceType);
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_AMOUNT, amount);
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_STARTED_AT, getCalendarTime());
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_STARTED_GAME_TIME, getGameTime());
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_DURATION, miningDuration);
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_SCHEDULED, 0);
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_SURVEYING, utils.getIntScriptVar(self, VAR_LAUNCH_SURVEYING));
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_DENSITY, utils.getFloatScriptVar(self, VAR_LAUNCH_DENSITY));
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_SAMPLING_INTERVAL, utils.getIntScriptVar(self, VAR_LAUNCH_SAMPLING_INTERVAL));
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_SAMPLING_INCREASE, utils.getIntScriptVar(self, VAR_LAUNCH_SAMPLING_INCREASE));
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_FALLEENS_FIST, utils.getIntScriptVar(self, VAR_LAUNCH_FALLEENS_FIST));
        setObjVar(player, VAR_ACTIVE_JOB_SEQUENCE, jobSequence);
        dictionary data = new dictionary();
        data.put("jobSequence", jobSequence);
        clearPendingLaunch(player, operationId);
        if (!messageTo(player, "handlePlanetaryMiningDroidReturn", data, miningDuration, true))
        {
            queuePlanetaryMiningDroidRelease(player, jobSequence);
            removeObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB);
            removeObjVar(player, VAR_ACTIVE_JOB_SEQUENCE);
            removeObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE);
            buff.removeBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE);
            cleanScriptVars(self);
            sendSystemMessage(player, new string_id(STF, "expedition_schedule_failed"));
            return SCRIPT_CONTINUE;
        }
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_SCHEDULED, 1);
        cleanScriptVars(self);
        consumeCharge(self);
        sendSystemMessage(player, new string_id(STF, "expedition_departed"));
        return SCRIPT_CONTINUE;
    }

    public void clearPendingLaunch(obj_id player, String operationId) throws InterruptedException
    {
        if (operationId.equals(getStringObjVar(player, VAR_PENDING_LAUNCH_OPERATION)))
        {
            removeObjVar(player, VAR_PENDING_LAUNCH_ITEM);
            removeObjVar(player, VAR_PENDING_LAUNCH_OPERATION);
        }
    }

    public void queuePlanetaryMiningDroidRelease(obj_id player, int jobSequence) throws InterruptedException
    {
        if (jobSequence < 1)
        {
            return;
        }
        int[] pendingReleases = hasObjVar(player, VAR_PENDING_RELEASE_SEQUENCES) ? getIntArrayObjVar(player, VAR_PENDING_RELEASE_SEQUENCES) : new int[0];
        for (int pendingRelease : pendingReleases)
        {
            if (pendingRelease == jobSequence)
            {
                messageTo(player, "retryPlanetaryMiningDroidRelease", null, 0.0f, true);
                return;
            }
        }
        int[] updatedReleases = new int[pendingReleases.length + 1];
        for (int i = 0; i < pendingReleases.length; ++i)
        {
            updatedReleases[i] = pendingReleases[i];
        }
        updatedReleases[pendingReleases.length] = jobSequence;
        setObjVar(player, VAR_PENDING_RELEASE_SEQUENCES, updatedReleases);
        messageTo(player, "retryPlanetaryMiningDroidRelease", null, 0.0f, true);
    }

    public void showMiningResourceConfirmation(obj_id self, obj_id player, resource_density activeResource, location selectedSite) throws InterruptedException
    {
        obj_id resourceType = activeResource.getResourceType();
        float displayX = selectedSite.x;
        float displayZ = selectedSite.z;
        if (selectedSite.area.equals("mustafar"))
        {
            displayX += 2880.0f;
            displayZ -= 2976.0f;
        }
        else if (selectedSite.area.equals("kashyyyk_rryatt_trail"))
        {
            displayX -= RRYATT_TRAIL_WORLD_OFFSET_X;
            displayZ -= RRYATT_TRAIL_WORLD_OFFSET_Z;
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("Resource Name: ").append(getLocalizedResourceName(resourceType));
        prompt.append("\nType: ").append(getLocalizedResourceTypeName(resourceType));
        prompt.append("\nHighest Concentration: ").append(Math.round(activeResource.getDensity() * 100)).append("%");
        prompt.append("\nLocation: (").append(displayX).append(", ").append(displayZ).append(")");
        prompt.append("\n\nResource Stats:");
        resource_attribute[] resourceAttributes = getResourceAttributes(resourceType);
        if (resourceAttributes != null)
        {
            for (resource_attribute resourceAttribute : resourceAttributes)
            {
                prompt.append("\n").append(localize(new string_id("obj_attr_n", resourceAttribute.getName()))).append(": ").append(resourceAttribute.getValue());
            }
        }
        prompt.append("\n\nLaunch the Interplanetary Mining Droid?");
        int pid = sui.createSUIPage(sui.SUI_MSGBOX, self, player, "handleMiningResourceConfirm");
        if (pid >= 0)
        {
            sui.setSUIProperty(pid, sui.MSGBOX_TITLE, sui.PROP_TEXT, DISPLAY_NAME);
            sui.setSUIProperty(pid, sui.MSGBOX_PROMPT, sui.PROP_TEXT, prompt.toString());
            sui.msgboxButtonSetup(pid, sui.OK_CANCEL);
            sui.setSUIProperty(pid, sui.MSGBOX_BTN_CANCEL, sui.PROP_TEXT, "@back");
            sui.showSUIPage(pid);
            trackSui(self, player, pid);
        }
        else
        {
            cleanScriptVars(self);
        }
    }

    public boolean isListSelectionValid(dictionary params) throws InterruptedException
    {
        return params != null && !params.isEmpty() && sui.getIntButtonPressed(params) != sui.BP_CANCEL && sui.getListboxSelectedRow(params) >= 0;
    }

    public void startPmdSurveys(obj_id self, obj_id player, String resourceClass, String planet, location surveyLocation) throws InterruptedException
    {
        obj_id[] candidateTypes = getPmdResourceTypes(planet, resourceClass);
        if (candidateTypes == null || candidateTypes.length == 0)
        {
            sendSystemMessage(player, new string_id(STF, "no_resources_available"));
            cleanScriptVars(self);
            return;
        }
        utils.setScriptVar(self, VAR_SURVEY_CANDIDATES, candidateTypes);
        utils.setScriptVar(self, VAR_SURVEY_INDEX, 0);
        utils.setScriptVar(self, VAR_SURVEY_RESULT_TYPES, new obj_id[0]);
        utils.setScriptVar(self, VAR_SURVEY_RESULT_DENSITIES, new float[0]);
        utils.setScriptVar(self, VAR_SURVEY_RESULT_LOCATIONS, new location[0]);
        utils.removeScriptVar(self, VAR_RESOURCE_TYPE);
        utils.removeScriptVar(self, VAR_SURVEY_BEST_DENSITY);
        utils.removeScriptVar(self, VAR_SURVEY_BEST_LOCATION);
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
        if (surveyLocation == null || resourceClass == null || resourceClass.equals(""))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id[] candidates = utils.getObjIdArrayScriptVar(self, VAR_SURVEY_CANDIDATES);
        int surveyIndex = utils.getIntScriptVar(self, VAR_SURVEY_INDEX);
        if (!isEligibleMiningDroidUser(self, player) || candidates == null || surveyIndex <= 0 || surveyIndex > candidates.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (xVals != null && zVals != null && efficiencies != null && xVals.length == 9 && zVals.length == 9 && efficiencies.length == 9)
        {
            float bestDensity = 0.0f;
            int bestIndex = -1;
            for (int i = 0; i < efficiencies.length; ++i)
            {
                float efficiency = efficiencies[i];
                if (efficiency > bestDensity)
                {
                    bestDensity = efficiency;
                    bestIndex = i;
                }
            }
            if (bestIndex >= 0 && bestDensity >= MIN_ACTIVE_DENSITY)
            {
                location bestLocation = new location(xVals[bestIndex], 0, zVals[bestIndex], planet);
                if (planetary_mining_regions.isAllowedSurveySample(planet, bestLocation))
                {
                    obj_id[] resultTypes = utils.getObjIdArrayScriptVar(self, VAR_SURVEY_RESULT_TYPES);
                    float[] resultDensities = utils.getFloatArrayScriptVar(self, VAR_SURVEY_RESULT_DENSITIES);
                    location[] resultLocations = utils.getLocationArrayScriptVar(self, VAR_SURVEY_RESULT_LOCATIONS);
                    int resultCount = resultTypes.length;
                    obj_id[] updatedTypes = new obj_id[resultCount + 1];
                    float[] updatedDensities = new float[resultCount + 1];
                    location[] updatedLocations = new location[resultCount + 1];
                    System.arraycopy(resultTypes, 0, updatedTypes, 0, resultCount);
                    System.arraycopy(resultDensities, 0, updatedDensities, 0, resultCount);
                    System.arraycopy(resultLocations, 0, updatedLocations, 0, resultCount);
                    updatedTypes[resultCount] = candidates[surveyIndex - 1];
                    updatedDensities[resultCount] = bestDensity;
                    updatedLocations[resultCount] = bestLocation;
                    utils.setScriptVar(self, VAR_SURVEY_RESULT_TYPES, updatedTypes);
                    utils.setScriptVar(self, VAR_SURVEY_RESULT_DENSITIES, updatedDensities);
                    utils.setScriptVar(self, VAR_SURVEY_RESULT_LOCATIONS, updatedLocations);
                }
            }
        }
        requestNextPmdSurvey(self, player, resourceClass, planet, surveyLocation);
        return SCRIPT_CONTINUE;
    }

    public void showPmdSurveyResults(obj_id self, obj_id player) throws InterruptedException
    {
        obj_id[] resultTypes = utils.getObjIdArrayScriptVar(self, VAR_SURVEY_RESULT_TYPES);
        float[] resultDensities = utils.getFloatArrayScriptVar(self, VAR_SURVEY_RESULT_DENSITIES);
        location[] resultLocations = utils.getLocationArrayScriptVar(self, VAR_SURVEY_RESULT_LOCATIONS);
        if (resultTypes == null || resultDensities == null || resultLocations == null || resultTypes.length == 0 || resultTypes.length != resultDensities.length || resultTypes.length != resultLocations.length)
        {
            sendSystemMessage(player, new string_id(STF, "no_resources_available"));
            cleanScriptVars(self);
            return;
        }
        String[] results = new String[resultTypes.length];
        for (int i = 0; i < resultTypes.length; ++i)
        {
            results[i] = getLocalizedResourceName(resultTypes[i]) + ": " + getLocalizedResourceTypeName(resultTypes[i]) + " - " + Math.round(resultDensities[i] * 100) + "%";
        }
        trackSui(self, player, sui.listbox(self, player, "Survey results. Select a resource to launch the droid.", sui.OK_CANCEL, DISPLAY_NAME, results, "handleMiningResourceSelection"));
    }

    public int handleMiningResourceSelection(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
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
        if (sui.getListboxSelectedRow(params) < 0)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id[] resultTypes = utils.getObjIdArrayScriptVar(self, VAR_SURVEY_RESULT_TYPES);
        float[] resultDensities = utils.getFloatArrayScriptVar(self, VAR_SURVEY_RESULT_DENSITIES);
        location[] resultLocations = utils.getLocationArrayScriptVar(self, VAR_SURVEY_RESULT_LOCATIONS);
        int index = sui.getListboxSelectedRow(params);
        if (!isEligibleMiningDroidUser(self, player) || resultTypes == null || resultDensities == null || resultLocations == null || index < 0 || index >= resultTypes.length || resultTypes.length != resultDensities.length || resultTypes.length != resultLocations.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id resourceType = resultTypes[index];
        location bestLocation = resultLocations[index];
        if (!planetary_mining_regions.isAllowedSurveyGrid(utils.getStringScriptVar(self, VAR_PLANET), utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION)) || !isIdValid(resourceType) || !planetary_mining_regions.isAllowedSurveySample(utils.getStringScriptVar(self, VAR_PLANET), bestLocation))
        {
            sendSystemMessage(player, new string_id(STF, "resource_inactive"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_RESOURCE_TYPE, resourceType);
        utils.setScriptVar(self, VAR_SURVEY_BEST_DENSITY, resultDensities[index]);
        utils.setScriptVar(self, VAR_SURVEY_BEST_LOCATION, bestLocation);
        utils.setScriptVar(self, VAR_SURVEY_SELECTED, 1);
        showMiningResourceConfirmation(self, player, new resource_density(resourceType, resultDensities[index]), bestLocation);
        return SCRIPT_CONTINUE;
    }

    public String getLocalizedResourceName(obj_id resourceType) throws InterruptedException
    {
        return utils.localizeSIDString(getResourceName(resourceType));
    }

    public String getLocalizedResourceTypeName(obj_id resourceType) throws InterruptedException
    {
        String resourceClass = getResourceClass(resourceType);
        return utils.localizeSIDString(getResourceClassName(resourceClass));
    }

    public boolean isEligibleMiningDroidUser(obj_id self, obj_id player) throws InterruptedException
    {
        return isIdValid(player) && utils.isNestedWithin(self, player) && utils.isProfession(player, utils.TRADER) && getTopMostContainer(player) == player;
    }

    public resource_density getResourceDensity(obj_id self, obj_id resourceType) throws InterruptedException
    {
        obj_id selectedResource = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        if (isIdValid(resourceType) && resourceType.equals(selectedResource) && utils.hasScriptVar(self, VAR_SURVEY_BEST_DENSITY))
        {
            return new resource_density(resourceType, utils.getFloatScriptVar(self, VAR_SURVEY_BEST_DENSITY));
        }
        return null;
    }

    public boolean isSelectedResourceValid(obj_id self, obj_id resourceType) throws InterruptedException
    {
        if (getResourceDensity(self, resourceType) == null)
        {
            return false;
        }
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        if (planet == null || resourceClass == null || resourceClass.equals(""))
        {
            return false;
        }
        String[] resourceTypeClasses = getPmdResourceTypeClasses(planet, resourceClass);
        if (resourceTypeClasses != null)
        {
            for (String resourceTypeClass : resourceTypeClasses)
            {
                if (isResourceDerivedFrom(resourceType, resourceTypeClass))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public String[] getPmdResourceClasses(String planet)
    {
        return planet != null && planet.startsWith("kashyyyk_") ? KASHYYYK_RESOURCE_CLASSES : MUSTAFAR_RESOURCE_CLASSES;
    }

    public String[] getPmdResourceClassNames(String[] resourceClasses)
    {
        String[] resourceClassNames = new String[resourceClasses.length];
        for (int i = 0; i < resourceClasses.length; ++i)
        {
            String resourceClass = resourceClasses[i];
            if (resourceClass.equals("mineral"))
            {
                resourceClassNames[i] = "Minerals";
            }
            else if (resourceClass.equals("chemical"))
            {
                resourceClassNames[i] = "Chemicals";
            }
            else if (resourceClass.equals("gas"))
            {
                resourceClassNames[i] = "Gases";
            }
            else if (resourceClass.equals("flora_resources"))
            {
                resourceClassNames[i] = "Flora";
            }
            else if (resourceClass.equals("water"))
            {
                resourceClassNames[i] = "Water";
            }
            else
            {
                resourceClassNames[i] = "Energy";
            }
        }
        return resourceClassNames;
    }

    public String[] getPmdResourceTypeClasses(String planet, String resourceClass)
    {
        if (planet != null && planet.startsWith("kashyyyk_"))
        {
            if (resourceClass.equals("chemical"))
            {
                return KASHYYYK_CHEMICAL_RESOURCE_TYPES;
            }
            if (resourceClass.equals("flora_resources"))
            {
                return KASHYYYK_FLORA_RESOURCE_TYPES;
            }
            if (resourceClass.equals("water"))
            {
                return KASHYYYK_WATER_RESOURCE_TYPES;
            }
            if (resourceClass.equals("energy"))
            {
                return KASHYYYK_ENERGY_RESOURCE_TYPES;
            }
            return null;
        }
        if (resourceClass.equals("mineral"))
        {
            return MUSTAFAR_MINERAL_RESOURCE_TYPES;
        }
        if (resourceClass.equals("chemical"))
        {
            return MUSTAFAR_CHEMICAL_RESOURCE_TYPES;
        }
        if (resourceClass.equals("gas"))
        {
            return MUSTAFAR_GAS_RESOURCE_TYPES;
        }
        if (resourceClass.equals("flora_resources"))
        {
            return MUSTAFAR_FLORA_RESOURCE_TYPES;
        }
        if (resourceClass.equals("water"))
        {
            return MUSTAFAR_WATER_RESOURCE_TYPES;
        }
        return null;
    }

    public obj_id[] getPmdResourceTypes(String planet, String resourceClass) throws InterruptedException
    {
        String[] resourceTypeClasses = getPmdResourceTypeClasses(planet, resourceClass);
        if (resourceTypeClasses == null)
        {
            return null;
        }
        Vector resourceTypes = new Vector();
        for (String resourceTypeClass : resourceTypeClasses)
        {
            obj_id[] types = getResourceTypes(resourceTypeClass);
            if (types != null)
            {
                for (obj_id type : types)
                {
                    resourceTypes.add(type);
                }
            }
        }
        obj_id[] types = new obj_id[resourceTypes.size()];
        resourceTypes.toArray(types);
        return types;
    }

    public int getMiningTime(obj_id self) throws InterruptedException
    {
        float duration = getFloatObjVar(self, ATTRIBUTE_DURATION);
        if (duration <= 0)
        {
            duration = 20.0f;
        }
        duration = Math.max(20.0f, Math.min(24.0f, duration));
        return Math.round(duration * 60 * 60);
    }

    public int getMiningAmount(obj_id self, obj_id player) throws InterruptedException
    {
        resource_density selectedResource = getResourceDensity(self, utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE));
        if (selectedResource == null || selectedResource.getDensity() <= 0)
        {
            return 0;
        }
        int samplingTimeDecrease = getSkillStatisticModifier(player, "expertise_resource_sampling_time_decrease");
        int samplingDelay = BASE_SAMPLING_DELAY - samplingTimeDecrease;
        if (samplingDelay <= MIN_SAMPLING_DELAY)
        {
            samplingDelay = MIN_SAMPLING_DELAY;
        }
        int samplingInterval = samplingDelay + SAMPLE_LOOP_OVERHEAD;
        int expertiseResourceIncrease = getSkillStatisticModifier(player, "expertise_resource_sampling_increase");
        int surveying = getSkillStatMod(player, "surveying");
        boolean falleensFist = buff.hasBuff(player, "tcg_series4_falleens_fist");
        utils.setScriptVar(self, VAR_LAUNCH_SURVEYING, surveying);
        utils.setScriptVar(self, VAR_LAUNCH_DENSITY, selectedResource.getDensity());
        utils.setScriptVar(self, VAR_LAUNCH_SAMPLING_INTERVAL, samplingInterval);
        utils.setScriptVar(self, VAR_LAUNCH_SAMPLING_INCREASE, expertiseResourceIncrease);
        utils.setScriptVar(self, VAR_LAUNCH_FALLEENS_FIST, falleensFist ? 1 : 0);
        return resource.getPlanetaryMiningAmount(selectedResource.getDensity(), surveying, getMiningTime(self), samplingInterval, expertiseResourceIncrease, falleensFist);
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

    public boolean beginFlow(obj_id self, obj_id player) throws InterruptedException
    {
        if (utils.hasScriptVar(self, VAR_FLOW_PLAYER) || hasObjVar(player, VAR_PENDING_LAUNCH_ITEM) || hasObjVar(player, VAR_PENDING_RELEASE_SEQUENCES) || utils.hasScriptVar(player, VAR_RELEASE_IN_FLIGHT))
        {
            return false;
        }
        obj_id activeItem = utils.getObjIdScriptVar(player, VAR_FLOW_ITEM);
        if (isIdValid(activeItem))
        {
            return false;
        }
        utils.removeScriptVar(player, VAR_FLOW_ITEM);
        utils.setScriptVar(self, VAR_FLOW_PLAYER, player);
        utils.setScriptVar(player, VAR_FLOW_ITEM, self);
        return true;
    }

    public void trackSui(obj_id self, obj_id player, int pid) throws InterruptedException
    {
        if (pid < 0)
        {
            cleanScriptVars(self);
            return;
        }
        utils.setScriptVar(self, VAR_FLOW_PID, pid);
        sui.setPid(player, pid, PID_NAME);
    }

    public boolean isCurrentSui(obj_id self, obj_id player, dictionary params) throws InterruptedException
    {
        if (params == null || params.isEmpty() || !isIdValid(player))
        {
            return false;
        }
        obj_id flowPlayer = utils.getObjIdScriptVar(self, VAR_FLOW_PLAYER);
        obj_id flowItem = utils.getObjIdScriptVar(player, VAR_FLOW_ITEM);
        if (!player.equals(flowPlayer) || !self.equals(flowItem) || !utils.hasScriptVar(self, VAR_FLOW_PID) || !sui.hasPid(player, PID_NAME))
        {
            return false;
        }
        int pageId = params.getInt("pageId");
        int itemPid = utils.getIntScriptVar(self, VAR_FLOW_PID);
        int playerPid = sui.getPid(player, PID_NAME);
        if (pageId != itemPid || pageId != playerPid)
        {
            if (pageId >= 0)
            {
                forceCloseSUIPage(pageId);
            }
            return false;
        }
        if (!utils.isNestedWithin(self, player))
        {
            cleanScriptVars(self);
            return false;
        }
        return true;
    }

    public void clearTrackedSui(obj_id self, obj_id player) throws InterruptedException
    {
        int itemPid = utils.getIntScriptVar(self, VAR_FLOW_PID);
        if (isIdValid(player) && sui.hasPid(player, PID_NAME) && sui.getPid(player, PID_NAME) == itemPid)
        {
            sui.removePid(player, PID_NAME);
        }
        utils.removeScriptVar(self, VAR_FLOW_PID);
    }

    public void cleanScriptVars(obj_id self) throws InterruptedException
    {
        obj_id player = utils.getObjIdScriptVar(self, VAR_FLOW_PLAYER);
        if (utils.hasScriptVar(self, VAR_FLOW_PID))
        {
            int pid = utils.getIntScriptVar(self, VAR_FLOW_PID);
            if (pid >= 0)
            {
                forceCloseSUIPage(pid);
            }
        }
        if (isIdValid(player))
        {
            if (sui.hasPid(player, PID_NAME))
            {
                sui.removePid(player, PID_NAME);
            }
            obj_id flowItem = utils.getObjIdScriptVar(player, VAR_FLOW_ITEM);
            if (self.equals(flowItem))
            {
                utils.removeScriptVar(player, VAR_FLOW_ITEM);
            }
        }
        utils.removeScriptVar(self, VAR_FLOW_PLAYER);
        utils.removeScriptVar(self, VAR_FLOW_PID);
        utils.removeScriptVar(self, VAR_LAUNCH_COUNTDOWN);
        utils.removeScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING);
        utils.removeScriptVar(self, VAR_LAUNCH_JOB_SEQUENCE);
        utils.removeScriptVar(self, VAR_LAUNCH_SURVEYING);
        utils.removeScriptVar(self, VAR_LAUNCH_DENSITY);
        utils.removeScriptVar(self, VAR_LAUNCH_SAMPLING_INTERVAL);
        utils.removeScriptVar(self, VAR_LAUNCH_SAMPLING_INCREASE);
        utils.removeScriptVar(self, VAR_LAUNCH_FALLEENS_FIST);
        utils.removeScriptVar(self, VAR_PLANET);
        utils.removeScriptVar(self, VAR_SURVEY_LOCATION);
        utils.removeScriptVar(self, VAR_RESOURCE_CLASS);
        utils.removeScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        utils.removeScriptVar(self, VAR_SURVEY_CANDIDATES);
        utils.removeScriptVar(self, VAR_SURVEY_INDEX);
        utils.removeScriptVar(self, VAR_SURVEY_RESULT_TYPES);
        utils.removeScriptVar(self, VAR_SURVEY_RESULT_DENSITIES);
        utils.removeScriptVar(self, VAR_SURVEY_RESULT_LOCATIONS);
        utils.removeScriptVar(self, VAR_SURVEY_BEST_DENSITY);
        utils.removeScriptVar(self, VAR_SURVEY_BEST_LOCATION);
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
            names[index] = "charges";
            attribs[index++] = Integer.toString(charges);
        }
        if (index < names.length)
        {
            names[index] = "duration";
            attribs[index] = Integer.toString(getMiningTime(self) / (60 * 60)) + " hours";
        }
        return SCRIPT_CONTINUE;
    }
}
