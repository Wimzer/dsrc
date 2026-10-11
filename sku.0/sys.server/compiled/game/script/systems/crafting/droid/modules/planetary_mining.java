package script.systems.crafting.droid.modules;

import script.*;
import script.library.*;

import java.util.Vector;

public class planetary_mining extends script.base_script
{
    public static final String STF = "planetary_mining_droid";
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
    public static final String VAR_RESOURCE_TYPE = "planetary_mining.resource_type";
    public static final String VAR_SURVEY_SELECTED = "planetary_mining.survey_selected";
    public static final String VAR_FLOW_PLAYER = "planetary_mining.flow.player";
    public static final String VAR_FLOW_ITEM = "planetary_mining.flow.item";
    public static final String VAR_FLOW_PID = "planetary_mining.flow.pid";
    public static final String VAR_FLOW_SEQUENCE = "planetaryMiningFlowSequence";
    public static final String VAR_LAUNCH_COUNTDOWN = "planetary_mining.launch_countdown";
    public static final String VAR_JOB_SEQUENCE = "planetary_mining.job_sequence";
    public static final String VAR_LAUNCH_JOB_SEQUENCE = "planetary_mining.launch_job_sequence";
    public static final String VAR_LAUNCH_AMOUNT = "planetary_mining.launch_amount";
    public static final String VAR_LAUNCH_SURVEYING = "planetary_mining.launch_surveying";
    public static final String VAR_LAUNCH_DENSITY = "planetary_mining.launch_density";
    public static final String VAR_LAUNCH_SAMPLING_INTERVAL = "planetary_mining.launch_sampling_interval";
    public static final String VAR_LAUNCH_SAMPLING_INCREASE = "planetary_mining.launch_sampling_increase";
    public static final String VAR_LAUNCH_FALLEENS_FIST = "planetary_mining.launch_falleens_fist";
    public static final String PID_NAME = "planetaryMiningDroid";
    public static final String DISPLAY_NAME = "Interplanetary Mining Droid";
    public static final float MIN_ACTIVE_DENSITY = 0.0001f;
    public static final float RRYATT_TRAIL_WORLD_OFFSET_X = 1294.0f;
    public static final float RRYATT_TRAIL_WORLD_OFFSET_Z = 3880.0f;
    public static final float MOVEMENT_CHECK_INTERVAL = 1.0f;

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
        obj_id player = utils.getContainingPlayer(self);
        boolean pendingLaunch = isIdValid(player) && self.equals(getObjIdObjVar(player, resource.VAR_PLANETARY_MINING_PENDING_LAUNCH_ITEM));
        return utils.hasScriptVar(self, VAR_FLOW_PLAYER) || pendingLaunch ? SCRIPT_OVERRIDE : SCRIPT_CONTINUE;
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
        location surveyLocation = getLocation(player);
        if (surveyLocation == null || surveyLocation.area == null || isIdValid(surveyLocation.cell) || !resource.isPlanetaryMiningSceneSupported(surveyLocation.area))
        {
            sendSystemMessage(player, new string_id(STF, "no_resources_available"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_PLANET, surveyLocation.area);
        utils.setScriptVar(self, VAR_SURVEY_LOCATION, surveyLocation);
        String[] resourceClasses = getPmdResourceClasses(surveyLocation);
        if (resourceClasses.length == 0)
        {
            sendSystemMessage(player, new string_id(STF, "no_resources_available"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        dictionary movementCheck = new dictionary();
        movementCheck.put("flowSequence", utils.getIntScriptVar(self, VAR_FLOW_SEQUENCE));
        if (!messageTo(self, "checkPlanetaryMiningDroidMovement", movementCheck, MOVEMENT_CHECK_INTERVAL, false))
        {
            sendSystemMessage(player, new string_id(STF, "expedition_schedule_failed"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
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
        if (rejectFlowIfMovedTooFar(self, player))
        {
            return SCRIPT_CONTINUE;
        }
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
        if (surveyLocation == null || !resource.isPlanetaryMiningSceneSupported(planet))
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
        if (rejectFlowIfMovedTooFar(self, player))
        {
            return SCRIPT_CONTINUE;
        }
        if (params == null || params.isEmpty())
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (sui.getIntButtonPressed(params) == sui.BP_CANCEL)
        {
            utils.removeScriptVar(self, VAR_RESOURCE_TYPE);
            utils.removeScriptVar(self, VAR_SURVEY_BEST_DENSITY);
            utils.removeScriptVar(self, VAR_SURVEY_SELECTED);
            showPmdSurveyResults(self, player);
            return SCRIPT_CONTINUE;
        }
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        if (!utils.hasScriptVar(self, VAR_SURVEY_SELECTED) || !isEligibleMiningDroidUser(self, player))
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
        if (utils.hasScriptVar(self, VAR_LAUNCH_COUNTDOWN))
        {
            return SCRIPT_CONTINUE;
        }
        // Freeze the yield inputs before the launch countdown begins.
        int amount = getMiningAmount(self, player);
        if (amount < 1)
        {
            sendSystemMessage(player, new string_id(STF, "yield_too_low"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int jobSequence = getIntObjVar(player, VAR_JOB_SEQUENCE) + 1;
        setObjVar(player, VAR_JOB_SEQUENCE, jobSequence);
        utils.setScriptVar(self, VAR_LAUNCH_JOB_SEQUENCE, jobSequence);
        utils.setScriptVar(self, VAR_LAUNCH_AMOUNT, amount);
        utils.setScriptVar(self, VAR_LAUNCH_COUNTDOWN, 3);
        sendSystemMessage(player, new string_id(STF, "launch_preparing"));
        sendSystemMessageProse(player, prose.getPackage(new string_id(STF, "launch_countdown"), 3));
        dictionary countdown = new dictionary();
        countdown.put("step", 2);
        if (!messageTo(self, "handlePlanetaryMiningDroidLaunchCountdown", countdown, 1.0f, false))
        {
            sendSystemMessage(player, new string_id(STF, "expedition_schedule_failed"));
            cleanScriptVars(self);
        }
        return SCRIPT_CONTINUE;
    }

    public int handlePlanetaryMiningDroidLaunchCountdown(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = utils.getObjIdScriptVar(self, VAR_FLOW_PLAYER);
        if (params == null || !utils.hasScriptVar(self, VAR_LAUNCH_COUNTDOWN))
        {
            return SCRIPT_CONTINUE;
        }
        if (!isIdValid(player))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (rejectFlowIfMovedTooFar(self, player))
        {
            return SCRIPT_CONTINUE;
        }
        int step = params.getInt("step");
        if (step > 0)
        {
            utils.setScriptVar(self, VAR_LAUNCH_COUNTDOWN, step);
            sendSystemMessageProse(player, prose.getPackage(new string_id(STF, "launch_countdown"), step));
            dictionary countdown = new dictionary();
            countdown.put("step", step - 1);
            if (!messageTo(self, "handlePlanetaryMiningDroidLaunchCountdown", countdown, 1.0f, false))
            {
                sendSystemMessage(player, new string_id(STF, "expedition_schedule_failed"));
                cleanScriptVars(self);
            }
            return SCRIPT_CONTINUE;
        }

        utils.removeScriptVar(self, VAR_LAUNCH_COUNTDOWN);
        requestPlanetaryMiningDroidReservation(self, player);
        return SCRIPT_CONTINUE;
    }

    public void requestPlanetaryMiningDroidReservation(obj_id self, obj_id player) throws InterruptedException
    {
        if (rejectFlowIfMovedTooFar(self, player))
        {
            return;
        }
        int jobSequence = utils.getIntScriptVar(self, VAR_LAUNCH_JOB_SEQUENCE);
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        int amount = utils.getIntScriptVar(self, VAR_LAUNCH_AMOUNT);
        if (!isEligibleMiningDroidUser(self, player) || !isSelectedResourceValid(self, resourceType) || jobSequence < 1 || amount < 1 || resourceClass == null || resourceClass.equals("") || surveyLocation == null)
        {
            sendSystemMessage(player, new string_id(STF, "resource_lost"));
            cleanScriptVars(self);
            return;
        }
        if (hasObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE) || buff.hasBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE))
        {
            sendSystemMessage(player, resource.SID_PLANETARY_MINING_SURVEY_LICENSE_OCCUPIED);
            cleanScriptVars(self);
            return;
        }
        if (!utils.hasScriptVar(self, VAR_LAUNCH_SURVEYING) || !utils.hasScriptVar(self, VAR_LAUNCH_DENSITY) || !utils.hasScriptVar(self, VAR_LAUNCH_SAMPLING_INTERVAL) || !utils.hasScriptVar(self, VAR_LAUNCH_SAMPLING_INCREASE) || !utils.hasScriptVar(self, VAR_LAUNCH_FALLEENS_FIST))
        {
            cleanScriptVars(self);
            return;
        }
        setObjVar(player, resource.VAR_PLANETARY_MINING_PENDING_LAUNCH_ITEM, self);
        setObjVar(player, resource.VAR_PLANETARY_MINING_PENDING_LAUNCH_SEQUENCE, jobSequence);
        setObjVar(player, resource.VAR_PLANETARY_MINING_PENDING_LAUNCH_PHASE, resource.PLANETARY_MINING_PENDING_PHASE_RESERVE);
        setObjVar(player, resource.VAR_PLANETARY_MINING_PENDING_LAUNCH_ITEM_COUNT, getCount(self));
        setObjVar(player, resource.VAR_PLANETARY_MINING_PENDING_LAUNCH_RESOURCE, resourceType);
        setObjVar(player, resource.VAR_PLANETARY_MINING_PENDING_LAUNCH_DENSITY, utils.getFloatScriptVar(self, VAR_LAUNCH_DENSITY));
        setObjVar(player, resource.VAR_PLANETARY_MINING_PENDING_LAUNCH_RESOURCE_CLASS, resourceClass);
        setObjVar(player, resource.VAR_PLANETARY_MINING_PENDING_LAUNCH_LOCATION, surveyLocation);

        dictionary request = new dictionary();
        request.put("player", player);
        request.put("jobSequence", jobSequence);
        if (!resource.sendPlanetaryMiningControllerMessage("reservePlanetaryMiningDroidJob", request))
        {
            removeObjVar(player, resource.VAR_PLANETARY_MINING_PENDING_LAUNCH);
            sendSystemMessage(player, new string_id(STF, "account_slot_reservation_failed"));
        }
        cleanScriptVars(self);
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
        obj_id[] candidateTypes = getPmdResourceTypes(surveyLocation, resourceClass);
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
        requestNextPmdSurvey(self, player, resourceClass, planet, surveyLocation);
    }

    public void requestNextPmdSurvey(obj_id self, obj_id player, String resourceClass, String planet, location surveyLocation) throws InterruptedException
    {
        obj_id[] candidates = utils.getObjIdArrayScriptVar(self, VAR_SURVEY_CANDIDATES);
        int surveyIndex = utils.getIntScriptVar(self, VAR_SURVEY_INDEX);
        if (rejectFlowIfMovedTooFar(self, player))
        {
            return;
        }
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
        if (rejectFlowIfMovedTooFar(self, player))
        {
            return SCRIPT_CONTINUE;
        }
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
        if (rejectFlowIfMovedTooFar(self, player))
        {
            return SCRIPT_CONTINUE;
        }
        if (params == null || params.isEmpty())
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (sui.getIntButtonPressed(params) == sui.BP_CANCEL)
        {
            if (getCount(self) <= 1)
            {
                String prompt = "You have burned out the survey module within this droid. If you cancel out of this section, the droid will be destroyed. Click Yes to confirm.";
                trackSui(self, player, sui.msgbox(self, player, prompt, sui.YES_NO, "Destroy Interplanetary Mining Droid", sui.MSG_QUESTION, "handleMiningSurveyCancellationConfirmation"));
                return SCRIPT_CONTINUE;
            }
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
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        if (!isIdValid(resourceType) || bestLocation == null || bestLocation.area == null || !bestLocation.area.equals(planet))
        {
            sendSystemMessage(player, new string_id(STF, "resource_inactive"));
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_RESOURCE_TYPE, resourceType);
        utils.setScriptVar(self, VAR_SURVEY_BEST_DENSITY, resultDensities[index]);
        utils.setScriptVar(self, VAR_SURVEY_SELECTED, 1);
        showMiningResourceConfirmation(self, player, new resource_density(resourceType, resultDensities[index]), bestLocation);
        return SCRIPT_CONTINUE;
    }

    public int handleMiningSurveyCancellationConfirmation(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
        if (rejectFlowIfMovedTooFar(self, player))
        {
            return SCRIPT_CONTINUE;
        }
        if (sui.getIntButtonPressed(params) != sui.BP_OK)
        {
            showPmdSurveyResults(self, player);
            return SCRIPT_CONTINUE;
        }
        consumeCharge(self);
        cleanScriptVars(self);
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
        if (!isIdValid(player) || !utils.isNestedWithin(self, player) || !utils.isProfession(player, utils.TRADER) || getTopMostContainer(player) != player)
        {
            return false;
        }
        if (!utils.hasScriptVar(self, VAR_SURVEY_LOCATION))
        {
            return true;
        }
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        location currentLocation = getLocation(player);
        return surveyLocation != null && currentLocation != null && surveyLocation.area != null && surveyLocation.area.equals(currentLocation.area) && resource.isPlanetaryMiningSceneSupported(currentLocation.area) && !isIdValid(currentLocation.cell) && getDistance(surveyLocation, currentLocation) <= resource.PLANETARY_MINING_MAX_DEPLOYMENT_MOVEMENT;
    }

    public int checkPlanetaryMiningDroidMovement(obj_id self, dictionary params) throws InterruptedException
    {
        int flowSequence = params == null ? 0 : params.getInt("flowSequence");
        if (flowSequence < 1 || flowSequence != utils.getIntScriptVar(self, VAR_FLOW_SEQUENCE) || !utils.hasScriptVar(self, VAR_FLOW_PLAYER))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id player = utils.getObjIdScriptVar(self, VAR_FLOW_PLAYER);
        if (!isIdValid(player))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (rejectFlowIfMovedTooFar(self, player))
        {
            return SCRIPT_CONTINUE;
        }
        if (!messageTo(self, "checkPlanetaryMiningDroidMovement", params, MOVEMENT_CHECK_INTERVAL, false))
        {
            cleanScriptVars(self);
            sendSystemMessage(player, new string_id(STF, "expedition_schedule_failed"));
        }
        return SCRIPT_CONTINUE;
    }

    public boolean rejectFlowIfMovedTooFar(obj_id self, obj_id player) throws InterruptedException
    {
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        location currentLocation = isIdValid(player) ? getLocation(player) : null;
        if (surveyLocation == null || currentLocation == null || surveyLocation.area == null || !surveyLocation.area.equals(currentLocation.area) || getDistance(surveyLocation, currentLocation) <= resource.PLANETARY_MINING_MAX_DEPLOYMENT_MOVEMENT)
        {
            return false;
        }
        cleanScriptVars(self);
        sendSystemMessage(player, resource.PLANETARY_MINING_MOVED_TOO_FAR, null);
        return true;
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
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (surveyLocation == null || resourceClass == null || resourceClass.equals(""))
        {
            return false;
        }
        obj_id[] resourceTypes = getPmdResourceTypes(surveyLocation, resourceClass);
        if (resourceTypes != null)
        {
            for (obj_id availableResourceType : resourceTypes)
            {
                if (resourceType.equals(availableResourceType))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public String[] getPmdResourceClasses(location surveyLocation) throws InterruptedException
    {
        if (surveyLocation == null || surveyLocation.area == null)
        {
            return new String[0];
        }
        String[] configuredClasses = surveyLocation.area.startsWith("kashyyyk_") ? KASHYYYK_RESOURCE_CLASSES : MUSTAFAR_RESOURCE_CLASSES;
        Vector availableClasses = new Vector();
        for (String resourceClass : configuredClasses)
        {
            obj_id[] resourceTypes = getPmdResourceTypes(surveyLocation, resourceClass);
            if (resourceTypes != null && resourceTypes.length > 0)
            {
                availableClasses.add(resourceClass);
            }
        }
        String[] resourceClasses = new String[availableClasses.size()];
        availableClasses.toArray(resourceClasses);
        return resourceClasses;
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

    public obj_id[] getPmdResourceTypes(location surveyLocation, String resourceClass) throws InterruptedException
    {
        if (surveyLocation == null || surveyLocation.area == null || resourceClass == null || resourceClass.equals(""))
        {
            return null;
        }
        boolean kashyyyk = surveyLocation.area.startsWith("kashyyyk_");
        if (!kashyyyk && !surveyLocation.area.equals("mustafar"))
        {
            return null;
        }
        resource_density[] availableResources = requestResourceList(surveyLocation, 0.0f, 1.0f, resourceClass);
        if (availableResources == null)
        {
            return null;
        }
        String sceneResourceClass = kashyyyk ? "_kashyyyk" : "_mustafar";
        Vector resourceTypes = new Vector();
        for (resource_density availableResource : availableResources)
        {
            obj_id resourceType = availableResource.getResourceType();
            String typeClass = getResourceClass(resourceType);
            if (typeClass != null && typeClass.contains(sceneResourceClass))
            {
                resourceTypes.add(resourceType);
            }
        }
        obj_id[] types = new obj_id[resourceTypes.size()];
        resourceTypes.toArray(types);
        return types;
    }

    public int getMiningTime(obj_id self) throws InterruptedException
    {
        return resource.getPlanetaryMiningDuration(self);
    }

    public int getMiningAmount(obj_id self, obj_id player) throws InterruptedException
    {
        resource_density selectedResource = getResourceDensity(self, utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE));
        if (selectedResource == null || selectedResource.getDensity() <= 0)
        {
            return 0;
        }
        int samplingInterval = resource.getPlanetaryMiningSamplingInterval(player);
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
        if (utils.hasScriptVar(self, VAR_FLOW_PLAYER) || hasObjVar(player, resource.VAR_PLANETARY_MINING_PENDING_LAUNCH))
        {
            return false;
        }
        obj_id activeItem = utils.getObjIdScriptVar(player, VAR_FLOW_ITEM);
        if (isIdValid(activeItem))
        {
            return false;
        }
        utils.removeScriptVar(player, VAR_FLOW_ITEM);
        int flowSequence = utils.getIntScriptVar(self, VAR_FLOW_SEQUENCE) + 1;
        utils.setScriptVar(self, VAR_FLOW_SEQUENCE, flowSequence);
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
        int itemPid = -1;
        if (utils.hasScriptVar(self, VAR_FLOW_PID))
        {
            itemPid = utils.getIntScriptVar(self, VAR_FLOW_PID);
            if (itemPid >= 0)
            {
                forceCloseSUIPage(itemPid);
            }
        }
        if (isIdValid(player))
        {
            if (sui.hasPid(player, PID_NAME))
            {
                int playerPid = sui.getPid(player, PID_NAME);
                if (playerPid >= 0 && playerPid != itemPid)
                {
                    forceCloseSUIPage(playerPid);
                }
                sui.removePid(player, PID_NAME);
            }
            obj_id flowItem = utils.getObjIdScriptVar(player, VAR_FLOW_ITEM);
            if (self.equals(flowItem))
            {
                utils.removeScriptVar(player, VAR_FLOW_ITEM);
            }
        }
        utils.removeScriptVarTree(self, "planetary_mining");
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
