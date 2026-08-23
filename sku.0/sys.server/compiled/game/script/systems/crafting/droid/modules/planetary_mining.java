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
    public static final String VAR_LAUNCH_AMOUNT = "planetary_mining.launch_amount";
    public static final String VAR_LAUNCH_JOB_SEQUENCE = "planetary_mining.launch_job_sequence";
    public static final String VAR_LAUNCH_QUALITY = "planetary_mining.launch_quality";
    public static final String VAR_LAUNCH_SURVEYING = "planetary_mining.launch_surveying";
    public static final String VAR_LAUNCH_DENSITY = "planetary_mining.launch_density";
    public static final String VAR_LAUNCH_SAMPLING_INTERVAL = "planetary_mining.launch_sampling_interval";
    public static final String VAR_LAUNCH_SAMPLING_INCREASE = "planetary_mining.launch_sampling_increase";
    public static final String VAR_LAUNCH_FALLEENS_FIST = "planetary_mining.launch_falleens_fist";
    public static final String PID_NAME = "planetaryMiningDroid";
    public static final String DISPLAY_NAME = "Interplanetary Mining Droid";
    public static final String ATTRIBUTE_BASE = craftinglib.COMPONENT_ATTRIBUTE_OBJVAR_NAME + ".";
    public static final String ATTRIBUTE_QUALITY = ATTRIBUTE_BASE + "quality";
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
            sendSystemMessage(player, "The interface for this droid is too complex for you to interact with.", null);
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE) || buff.hasBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE))
        {
            sendSystemMessage(player, "Your Survey License is occupied by an active Interplanetary Mining Droid.", null);
            return SCRIPT_CONTINUE;
        }
        if (getTopMostContainer(player) != player)
        {
            sendSystemMessage(player, "You must be outdoors to launch this droid.", null);
            return SCRIPT_OVERRIDE;
        }
        if (!beginFlow(self, player))
        {
            sendSystemMessage(player, "An Interplanetary Mining Droid interface is already active.", null);
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
            sendSystemMessage(player, "Enter coordinates to survey.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        coordinateInput = coordinateInput.trim().replaceFirst("(?i)^/waypoint(?:[,\\s]+)", "");
        if (coordinateInput.length() > MAX_COORDINATE_INPUT_LENGTH)
        {
            sendSystemMessage(player, "Coordinate input is too long.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        String[] coordinates = coordinateInput.split("[,\\s]+");
        if (coordinates.length != 2 && coordinates.length != 3)
        {
            sendSystemMessage(player, "Enter X, Z or X, Y, Z coordinates.", null);
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
            sendSystemMessage(player, "Coordinates must be numbers.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        for (float value : values)
        {
            if (Float.isNaN(value) || Float.isInfinite(value))
            {
                sendSystemMessage(player, "Coordinates must be finite numbers.", null);
                promptForMiningCoordinates(self, player);
                return SCRIPT_CONTINUE;
            }
        }
        float x = values[0];
        float z = values[values.length - 1];
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        if (planet.equals("mustafar") && (x < planetary_mining_regions.MUSTAFAR_DISPLAY_MIN_COORDINATE + planetary_mining_regions.SURVEY_GRID_RADIUS || x > planetary_mining_regions.MUSTAFAR_DISPLAY_MAX_COORDINATE - planetary_mining_regions.SURVEY_GRID_RADIUS || z < planetary_mining_regions.MUSTAFAR_DISPLAY_MIN_COORDINATE + planetary_mining_regions.SURVEY_GRID_RADIUS || z > planetary_mining_regions.MUSTAFAR_DISPLAY_MAX_COORDINATE - planetary_mining_regions.SURVEY_GRID_RADIUS))
        {
            sendSystemMessage(player, "Those coordinates are outside the surveyable area.", null);
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
                sendSystemMessage(player, "That area of the trail is too dangerous for your droid.", null);
            }
            else if (planet.equals("kashyyyk_dead_forest"))
            {
                sendSystemMessage(player, "The Sayormi's dark magic prevents your droid from surveying there.", null);
            }
            else
            {
                sendSystemMessage(player, "Those coordinates are outside the surveyable area.", null);
            }
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_SURVEY_LOCATION, surveyLocation);
        Vector availableClasses = new Vector();
        Vector availableClassNames = new Vector();
        for (int i = 0; i < RESOURCE_CLASSES.length; ++i)
        {
            obj_id[] availableResourceTypes = getAvailablePmdResourceTypes(surveyLocation, RESOURCE_CLASSES[i]);
            if (availableResourceTypes != null && availableResourceTypes.length > 0)
            {
                availableClasses.add(RESOURCE_CLASSES[i]);
                availableClassNames.add(RESOURCE_CLASS_NAMES[i]);
            }
        }
        if (availableClasses.size() == 0)
        {
            sendSystemMessage(player, "No resources are currently available in the selected area.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        String[] resourceClasses = new String[availableClasses.size()];
        String[] resourceClassNames = new String[availableClassNames.size()];
        availableClasses.toArray(resourceClasses);
        availableClassNames.toArray(resourceClassNames);
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
            sendSystemMessage(player, "Your Survey License is occupied by an active Interplanetary Mining Droid.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!utils.isNestedWithin(self, player) || !isSelectedResourceAvailable(self, resourceType))
        {
            sendSystemMessage(player, "That resource is no longer active on the selected planet.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (utils.hasScriptVar(self, VAR_LAUNCH_COUNTDOWN) || utils.hasScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING))
        {
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_LAUNCH_COUNTDOWN, 3);
        sendSystemMessage(player, "3", null);
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
            sendSystemMessage(player, Integer.toString(step), null);
            dictionary countdown = new dictionary();
            countdown.put("step", step - 1);
            messageTo(self, "handlePlanetaryMiningDroidLaunchCountdown", countdown, 1.0f, false);
            return SCRIPT_CONTINUE;
        }

        utils.removeScriptVar(self, VAR_LAUNCH_COUNTDOWN);
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (!planetary_mining_regions.isAllowedSurveyGrid(planet, surveyLocation) || !isSelectedResourceAvailable(self, resourceType))
        {
            sendSystemMessage(player, "Your droid can no longer find the resource.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int amount = getMiningAmount(self, player);
        if (amount < 1)
        {
            sendSystemMessage(player, "This droid's expected yield is too low to return a resource unit at the selected concentration and duration.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_LAUNCH_AMOUNT, amount);
        if (hasObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE) || buff.hasBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE))
        {
            sendSystemMessage(player, "Your Survey License is occupied by an active Interplanetary Mining Droid.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int jobSequence = getIntObjVar(player, VAR_JOB_SEQUENCE) + 1;
        setObjVar(player, VAR_JOB_SEQUENCE, jobSequence);
        String operationId = "reserve:" + jobSequence;
        setObjVar(player, VAR_PENDING_LAUNCH_ITEM, self);
        setObjVar(player, VAR_PENDING_LAUNCH_OPERATION, operationId);
        utils.setScriptVar(self, VAR_LAUNCH_JOB_SEQUENCE, jobSequence);
        if (!planetaryMiningDroidAdjustAccountFeatureId(player, player, 1, operationId))
        {
            clearPendingLaunch(player, operationId);
            sendSystemMessage(player, "The Interplanetary Mining Droid could not reserve an account mining slot.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING, 1);
        sendSystemMessage(player, "Reserving an interplanetary mining slot...", null);
        return SCRIPT_CONTINUE;
    }

    public int handlePlanetaryMiningDroidAccountFeatureResponse(obj_id self, dictionary params) throws InterruptedException
    {
        if (!utils.hasScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING))
        {
            return SCRIPT_CONTINUE;
        }
        utils.removeScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING);
        obj_id player = utils.getObjIdScriptVar(self, VAR_FLOW_PLAYER);
        int jobSequence = utils.getIntScriptVar(self, VAR_LAUNCH_JOB_SEQUENCE);
        String operationId = "reserve:" + jobSequence;
        if (params == null || !operationId.equals(params.getString("operationId")))
        {
            return SCRIPT_CONTINUE;
        }
        if (params == null || !params.getBoolean("success") || !isIdValid(player))
        {
            if (isIdValid(player))
            {
                clearPendingLaunch(player, operationId);
                sendSystemMessage(player, "This account already has three active Interplanetary Mining Droid jobs.", null);
            }
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (params.getInt("newValue") > 3)
        {
            clearPendingLaunch(player, operationId);
            queuePlanetaryMiningDroidRelease(player, jobSequence);
            sendSystemMessage(player, "This account already has three active Interplanetary Mining Droid jobs.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (!isEligibleMiningDroidUser(self, player) || !planetary_mining_regions.isAllowedSurveyGrid(planet, surveyLocation) || !isSelectedResourceAvailable(self, resourceType))
        {
            clearPendingLaunch(player, operationId);
            queuePlanetaryMiningDroidRelease(player, jobSequence);
            sendSystemMessage(player, "Your droid can no longer find the resource.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int amount = utils.getIntScriptVar(self, VAR_LAUNCH_AMOUNT);
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
            sendSystemMessage(player, "The Interplanetary Mining Droid could not occupy your Survey License.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int miningDuration = getMiningTime(self);
        setObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE, 1);
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_RESOURCE, resourceType);
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_AMOUNT, amount);
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_STARTED_AT, getCalendarTime());
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_DURATION, miningDuration);
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_SCHEDULED, 0);
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_QUALITY, utils.getFloatScriptVar(self, VAR_LAUNCH_QUALITY));
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
            sendSystemMessage(player, "The Interplanetary Mining Droid could not schedule its expedition.", null);
            return SCRIPT_CONTINUE;
        }
        setObjVar(player, resource.VAR_PLANETARY_MINING_ACTIVE_JOB_SCHEDULED, 1);
        cleanScriptVars(self);
        consumeCharge(self);
        sendSystemMessage(player, "Your droid has departed on its expedition.", null);
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
        obj_id[] candidateTypes = getAvailablePmdResourceTypes(surveyLocation, resourceClass);
        if (candidateTypes == null || candidateTypes.length == 0)
        {
            sendSystemMessage(player, "No resources of that type are currently available at those coordinates.", null);
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
            sendSystemMessage(player, "No resources of that type are currently available at those coordinates.", null);
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
            sendSystemMessage(player, "That resource is no longer active on the selected planet.", null);
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

    public boolean isSelectedResourceAvailable(obj_id self, obj_id resourceType) throws InterruptedException
    {
        if (getResourceDensity(self, resourceType) == null)
        {
            return false;
        }
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        if (surveyLocation == null || resourceClass == null || resourceClass.equals(""))
        {
            return false;
        }
        obj_id[] availableResources = getAvailablePmdResourceTypes(surveyLocation, resourceClass);
        if (availableResources != null)
        {
            for (obj_id availableResource : availableResources)
            {
                if (resourceType.equals(availableResource))
                {
                    return true;
                }
            }
        }
        return false;
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

    public float getMiningQuality(obj_id self) throws InterruptedException
    {
        return 100.0f;
    }

    public int getMiningAmount(obj_id self, obj_id player) throws InterruptedException
    {
        float quality = getMiningQuality(self);
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
        utils.setScriptVar(self, VAR_LAUNCH_QUALITY, quality);
        utils.setScriptVar(self, VAR_LAUNCH_SURVEYING, surveying);
        utils.setScriptVar(self, VAR_LAUNCH_DENSITY, selectedResource.getDensity());
        utils.setScriptVar(self, VAR_LAUNCH_SAMPLING_INTERVAL, samplingInterval);
        utils.setScriptVar(self, VAR_LAUNCH_SAMPLING_INCREASE, expertiseResourceIncrease);
        utils.setScriptVar(self, VAR_LAUNCH_FALLEENS_FIST, falleensFist ? 1 : 0);
        return resource.getPlanetaryMiningAmount(quality, selectedResource.getDensity(), surveying, getMiningTime(self), samplingInterval, expertiseResourceIncrease, falleensFist);
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
        utils.removeScriptVar(self, VAR_LAUNCH_AMOUNT);
        utils.removeScriptVar(self, VAR_LAUNCH_JOB_SEQUENCE);
        utils.removeScriptVar(self, VAR_LAUNCH_QUALITY);
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
            names[index] = "quality";
            attribs[index++] = Integer.toString((int)getMiningQuality(self));
        }
        if (index < names.length)
        {
            names[index] = "duration";
            attribs[index] = Integer.toString(getMiningTime(self) / (60 * 60)) + " hours";
        }
        return SCRIPT_CONTINUE;
    }
}
