package script.library;

import script.*;

import java.util.Vector;

public class resource extends script.base_script
{
    public resource()
    {
    }
    public static final int SURVEY_RESOLUTION_MIN = 3;
    public static final int SURVEY_RESOLUTION_MAX = 6;
    public static final int SURVEY_RESOLUTION_DEFAULT = 3;
    public static final int SURVEY_RANGE_MIN = 16;
    public static final int SURVEY_RANGE_MAX = 384;
    public static final int SURVEY_RANGE_DEFAULT = 64;
    public static final String VAR_SURVEY_BASE = "survey";
    public static final String VAR_SURVEY_CLASS = VAR_SURVEY_BASE + ".resource_class";
    public static final String VAR_SURVEY_RESOLUTION_MIN = VAR_SURVEY_BASE + ".resolution.min";
    public static final String VAR_SURVEY_RESOLUTION_MAX = VAR_SURVEY_BASE + ".resolution.max";
    public static final String VAR_SURVEY_RESOLUTION_VALUE = VAR_SURVEY_BASE + ".resolution.value";
    public static final String VAR_SURVEY_RANGE_MIN = VAR_SURVEY_BASE + ".range.min";
    public static final String VAR_SURVEY_RANGE_MAX = VAR_SURVEY_BASE + ".range.max";
    public static final String VAR_SURVEY_RANGE_VALUE = VAR_SURVEY_BASE + ".range.value";
    public static final String VAR_SAMPLE_STAMP = VAR_SURVEY_BASE + ".sample.stamp";
    public static final String VAR_PLANETARY_MINING_SURVEY_LICENSE = "planetary_mining.survey_license_occupied";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB = "planetary_mining.active_job";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB_SEQUENCE = "planetary_mining.active_job_sequence";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB_RESOURCE = VAR_PLANETARY_MINING_ACTIVE_JOB + ".resource";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB_AMOUNT = VAR_PLANETARY_MINING_ACTIVE_JOB + ".amount";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB_STARTED_AT = VAR_PLANETARY_MINING_ACTIVE_JOB + ".started_at";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB_DURATION = VAR_PLANETARY_MINING_ACTIVE_JOB + ".duration";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB_STARTED_GAME_TIME = VAR_PLANETARY_MINING_ACTIVE_JOB + ".started_game_time";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB_SURVEYING = VAR_PLANETARY_MINING_ACTIVE_JOB + ".surveying";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB_DENSITY = VAR_PLANETARY_MINING_ACTIVE_JOB + ".density";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB_SAMPLING_INTERVAL = VAR_PLANETARY_MINING_ACTIVE_JOB + ".sampling_interval";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB_SAMPLING_INCREASE = VAR_PLANETARY_MINING_ACTIVE_JOB + ".sampling_increase";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB_FALLEENS_FIST = VAR_PLANETARY_MINING_ACTIVE_JOB + ".falleens_fist";
    public static final String VAR_PLANETARY_MINING_ACTIVE_JOB_COMPLETION_AMOUNT = VAR_PLANETARY_MINING_ACTIVE_JOB + ".completion_amount";
    public static final String VAR_PLANETARY_MINING_DELIVERY_JOB = "planetary_mining.delivery_job";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH = "planetary_mining.pending_launch";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_ITEM = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".item";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_SEQUENCE = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".sequence";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_PHASE = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".phase";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_ITEM_COUNT = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".item_count";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_RESOURCE = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".resource";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_DURATION = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".duration";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_SURVEYING = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".surveying";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_DENSITY = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".density";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_SAMPLING_INTERVAL = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".sampling_interval";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_SAMPLING_INCREASE = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".sampling_increase";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_FALLEENS_FIST = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".falleens_fist";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_RESOURCE_CLASS = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".resource_class";
    public static final String VAR_PLANETARY_MINING_PENDING_LAUNCH_LOCATION = VAR_PLANETARY_MINING_PENDING_LAUNCH + ".location";
    public static final int PLANETARY_MINING_PENDING_PHASE_RESERVE = 1;
    public static final int PLANETARY_MINING_PENDING_PHASE_COMMIT = 2;
    public static final obj_id PLANETARY_MINING_CONTROLLER = obj_id.getObjId(60525);
    public static final float PLANETARY_MINING_MAX_DEPLOYMENT_MOVEMENT = 8.0f;
    public static final int PLANETARY_MINING_BASE_SAMPLING_DELAY = 25;
    public static final int PLANETARY_MINING_MIN_SAMPLING_DELAY = 10;
    public static final int PLANETARY_MINING_SAMPLE_LOOP_OVERHEAD = 3;
    public static final String BUFF_PLANETARY_MINING_SURVEY_LICENSE = "pmd_survey_license_occupied";
    public static final String BUFF_PLANETARY_MINING_ACCOUNT_SAMPLING_RESTRICTED = "pmd_account_hand_sampling_restricted";
    public static final String VAR_PLANETARY_MINING_ACCOUNT_SAMPLING = "planetary_mining.account_sampling";
    public static final String VAR_PLANETARY_MINING_ACCOUNT_SAMPLING_FULL = VAR_PLANETARY_MINING_ACCOUNT_SAMPLING + ".full";
    public static final String VAR_PLANETARY_MINING_ACCOUNT_SAMPLING_CAPACITY = VAR_PLANETARY_MINING_ACCOUNT_SAMPLING + ".capacity";
    public static final String VAR_PLANETARY_MINING_ACCOUNT_SAMPLING_OCCUPIED = VAR_PLANETARY_MINING_ACCOUNT_SAMPLING + ".occupied";
    public static final String VAR_PLANETARY_MINING_ACCOUNT_SAMPLING_REVISION = VAR_PLANETARY_MINING_ACCOUNT_SAMPLING + ".revision";
    public static final String VAR_PLANETARY_MINING_SAMPLING_IDENTITY = "planetary_mining.sampling_identity";
    public static final String VAR_PLANETARY_MINING_SAMPLING_IDENTITY_TOOL = VAR_PLANETARY_MINING_SAMPLING_IDENTITY + ".tool";
    public static final String VAR_PLANETARY_MINING_SAMPLING_IDENTITY_EPOCH = VAR_PLANETARY_MINING_SAMPLING_IDENTITY + ".session_epoch";
    public static final String VAR_PLANETARY_MINING_SAMPLING_IDENTITY_SESSION = VAR_PLANETARY_MINING_SAMPLING_IDENTITY + ".session_sequence";
    public static final String VAR_PLANETARY_MINING_SAMPLING_IDENTITY_CLAIM = VAR_PLANETARY_MINING_SAMPLING_IDENTITY + ".claim_sequence";
    public static final String VAR_PLANETARY_MINING_SAMPLING_IDENTITY_RESOURCE = VAR_PLANETARY_MINING_SAMPLING_IDENTITY + ".resource";
    public static final String VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING = "planetary_mining.sampling_release_pending";
    public static final String VAR_PLANETARY_MINING_SAMPLING_SESSION_EPOCH = "planetary_mining.sampling_session_epoch";
    public static final String VAR_PLANETARY_MINING_SAMPLING_SESSION_SEQUENCE = "planetary_mining.sampling_session_sequence";
    public static final String VAR_PLANETARY_MINING_SAMPLING_CLAIM_SEQUENCE = "planetary_mining.sampling_claim_sequence";
    public static final String VAR_PLANETARY_MINING_SAMPLING_REQUEST_SEQUENCE = "planetary_mining.sampling_request_sequence";
    public static final String VAR_PLANETARY_MINING_SAMPLING_EVENT_SEQUENCE = "planetary_mining.sampling_event_sequence";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING = "planetary_mining.sampling";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_SESSION = SCRIPTVAR_PLANETARY_MINING_SAMPLING + ".session";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_EPOCH = SCRIPTVAR_PLANETARY_MINING_SAMPLING + ".epoch";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_CLAIM = SCRIPTVAR_PLANETARY_MINING_SAMPLING + ".claim";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_TOOL = SCRIPTVAR_PLANETARY_MINING_SAMPLING + ".tool";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_RESOURCE = SCRIPTVAR_PLANETARY_MINING_SAMPLING + ".resource";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT = SCRIPTVAR_PLANETARY_MINING_SAMPLING + ".event";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_SESSION = SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT + ".session";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_SEQUENCE = SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT + ".sequence";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_PAGE = SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT + ".page";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_KIND = SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT + ".kind";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING = SCRIPTVAR_PLANETARY_MINING_SAMPLING + ".pending";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_TOOL = SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING + ".tool";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_SESSION = SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING + ".session";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_EPOCH = SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING + ".epoch";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_CLAIM = SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING + ".claim";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_REQUEST = SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING + ".request";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_KIND = SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING + ".kind";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_OPERATION_DEPTH = "planetary_mining.sampling_operation_depth";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_DEFERRED_RELEASE = "planetary_mining.sampling_deferred_release";
    public static final String SCRIPTVAR_PLANETARY_MINING_SAMPLING_RELEASE_RETRY = "planetary_mining.sampling_release_retry";
    public static final int PLANETARY_MINING_SAMPLING_AUTHORIZATION_START = 1;
    public static final int PLANETARY_MINING_SAMPLING_EVENT_NODE = 1;
    public static final int PLANETARY_MINING_SAMPLING_EVENT_GAMBLE = 2;
    public static final float PLANETARY_MINING_SAMPLING_AUTHORIZATION_TIMEOUT = 10.0f;
    public static final float PLANETARY_MINING_SAMPLING_RELEASE_RETRY_DELAY = 10.0f;
    public static final String PLANETARY_MINING_MOVED_TOO_FAR = "You moved too far from your original launch coordinates.";
    public static final string_id SID_PLANETARY_MINING_SURVEY_LICENSE_OCCUPIED = new string_id("planetary_mining_droid", "survey_license_occupied");
    public static final string_id SID_PLANETARY_MINING_ACCOUNT_SAMPLING_RESTRICTED = new string_id("planetary_mining_droid", "account_sampling_restricted");
    public static final string_id SID_PLANETARY_MINING_ACCOUNT_SAMPLING_UNAVAILABLE = new string_id("planetary_mining_droid", "account_sampling_unavailable");

    public static boolean isPlanetaryMiningSceneSupported(String scene)
    {
        return scene != null && (scene.equals("mustafar") || scene.equals("kashyyyk_main") || scene.equals("kashyyyk_dead_forest") || scene.equals("kashyyyk_hunting") || scene.equals("kashyyyk_rryatt_trail"));
    }

    public static final String HANDLER_SET_RANGE = "handleSetRange";
    public static final String HANDLER_SET_RESOLUTION = "handleSetResolution";
    public static final String VAR_SETTINGS_BASE = VAR_SURVEY_BASE + ".settings";
    public static final String VAR_SETTINGS_PLAYER = VAR_SETTINGS_BASE + ".player";
    public static final String VAR_SETTINGS_INDEX = VAR_SETTINGS_BASE + ".index";
    public static final String VAR_SETTINGS_RANGES = VAR_SETTINGS_BASE + ".ranges";
    public static final String VAR_SETTINGS_RESOLUTIONS = VAR_SETTINGS_BASE + ".resolutions";
    public static final String PID_NAME = "resourceSpecialEvent";
    public static final String PLANETARY_MINING_SAMPLING_EVENT_PID = "planetaryMiningSamplingEvent";
    public static final int CLASS_RESOURCE = 0;
    public static final int CLASS_INORGANIC = 1;
    public static final int CLASS_MINERAL = 2;
    public static final int CLASS_WATER = 3;
    public static final int CLASS_CHEMICAL = 4;
    public static final int CLASS_GAS = 5;
    public static final int CLASS_ENERGY = 6;
    public static final int CLASS_ORGANIC = 7;
    public static final int CLASS_FOOD = 8;
    public static final int CLASS_CREATURE_STRUCTURAL = 9;
    public static final int CLASS_FLORA_RESOURCE = 10;
    public static final int CLASS_FLORA_STRUCTURAL = 11;
    public static final int CLASS_ENERGY_WIND = 12;
    public static final int CLASS_ENERGY_SOLAR = 13;
    public static final int CLASS_MAX = 14;
    public static final String[] CLASS_NAME = 
    {
        "resource",
        "inorganic",
        "mineral",
        "water",
        "chemical",
        "gas",
        "energy",
        "organic",
        "creature_food",
        "creature_structural",
        "flora_resources",
        "flora_structural",
        "energy_renewable_unlimited_wind",
        "energy_renewable_unlimited_solar"
    };
    public static final String RT_ORGANIC = "organic";
    public static final String RT_INORGANIC = "inorganic";
    public static final String RT_MINERAL = "mineral";
    public static final String RT_GAS = "gas";
    public static final String RT_LIQUID_VAPORS = "liquid_vapors";
    public static final String RT_LIQUID_WATER = "water";
    public static final String RT_LIQUID_CHEMICAL = "chemical";
    public static final String RT_CREATURE_RESOURCES = "creature_resources";
    public static final String RT_FLORA_RESOURCES = "flora_resources";
    public static final String RT_FLORA_FOOD = "flora_food";
    public static final String RT_FLORA_STRUCTURAL = "flora_structural";
    public static final String RT_ENERGY_WIND = "energy_renewable_unlimited_wind";
    public static final String RT_ENERGY_SOLAR = "energy_renewable_unlimited_solar";
    public static final String RT_ENERGY_GEO = "energy_renewable_site_limited_geothermal";
    public static final String MOD_SURVEY_ORGANIC = "survey_organic";
    public static final String MOD_SURVEY_ORGANIC_RANGE = "survey_organic_range";
    public static final String MOD_SURVEY_ORGANIC_RESOLUTION = "survey_organic_resolution";
    public static final String MOD_SURVEY_INORGANIC = "survey_inorganic";
    public static final String MOD_SURVEY_INORGANIC_RANGE = "survey_inorganic_range";
    public static final String MOD_SURVEY_INORGANIC_RESOLUTION = "survey_inorganic_resolution";
    public static final String MOD_SURVEY_SOLID = "survey_solid";
    public static final String MOD_SURVEY_SOLID_RANGE = "survey_solid_range";
    public static final String MOD_SURVEY_SOLID_RESOLUTION = "survey_solid_resolution";
    public static final String MOD_SURVEY_GAS = "survey_gas";
    public static final String MOD_SURVEY_GAS_RANGE = "survey_gas_range";
    public static final String MOD_SURVEY_GAS_RESOLUTION = "survey_gas_resolution";
    public static final String MOD_SURVEY_LIQUID = "survey_liquid";
    public static final String MOD_SURVEY_LIQUID_RANGE = "survey_liquid_range";
    public static final String MOD_SURVEY_LIQUID_RESOLUTION = "survey_liquid_resolution";
    public static final String MOD_SURVEY_CREATURE = "survey_creature";
    public static final String MOD_SURVEY_CREATURE_RANGE = "survey_creature_range";
    public static final String MOD_SURVEY_CREATURE_RESOLUTION = "survey_creature_resolution";
    public static final String MOD_SURVEY_FOOD = "survey_food";
    public static final String MOD_SURVEY_FOOD_RANGE = "survey_food_range";
    public static final String MOD_SURVEY_FOOD_RESOLUTION = "survey_food_resolution";
    public static final String MOD_SURVEY_FLORA_STRUCTURAL = "survey_flora_structural";
    public static final String MOD_SURVEY_FLORA_STRUCTURAL_RANGE = "survey_flora_structural_range";
    public static final String MOD_SURVEY_FLORA_STRUCTURAL_RESOLUTION = "survey_flora_structural_resolution";
    public static final String MOD_SURVEY_ENERGY_WIND = "survey_energy_wind";
    public static final String MOD_SURVEY_ENERGY_WIND_RANGE = "survey_energy_wind_range";
    public static final String MOD_SURVEY_ENERGY_WIND_RESOLUTION = "survey_energy_wind_resolution";
    public static final String MOD_SURVEY_ENERGY_SOLAR = "survey_energy_solar";
    public static final String MOD_SURVEY_ENERGY_SOLAR_RANGE = "survey_energy_solar_range";
    public static final String MOD_SURVEY_ENERGY_SOLAR_RESOLUTION = "survey_energy_solar_resolution";
    public static final String MOD_SURVEY_ENERGY_GEO = "survey_energy_geo_thermal";
    public static final String MOD_SURVEY_ENERGY_GEO_RANGE = "survey_energy_geo_thermal_range";
    public static final String MOD_SURVEY_ENERGY_GEO_RESOLUTION = "survey_energy_geo_thermal_resolution";
    public static final String MOD_SURVEY_ALL = "survey_resources_all";
    public static final float SAMPLE_DENSITY_THRESHOLD = 0.3f;
    public static final int BASE_HARVEST_XP = 40;
    public static final int TIME_SAMPLE = 18;
    public static final int TIME_FAIL_BONUS = 180;
    public static final int BASE_SAMPLE_AMOUNT = 22;
    public static final String STF_SURVEY = "survey";
    public static final string_id SID_WRONG_TOOL = new string_id(STF_SURVEY, "wrong_tool");
    public static final string_id SID_NSF_SKILL = new string_id("error_message", "prose_nsf_skill");
    public static final string_id SID_NSF_SKILL_PLURAL = new string_id("error_message", "prose_nsf_skill_plural");
    public static final string_id SID_SAMPLE_LOCATED = new string_id(STF_SURVEY, "sample_located");
    public static final string_id SID_SAMPLE_FAILED = new string_id(STF_SURVEY, "sample_failed");
    public static final string_id SID_DENSITY_BELOW_THESHOLD = new string_id(STF_SURVEY, "density_below_threshold");
    public static final string_id SID_TRACE_AMOUNT = new string_id(STF_SURVEY, "trace_amount");
    public static final string_id SID_EFFICIENCY_TOO_LOW = new string_id(STF_SURVEY, "efficiency_too_low");
    public static final string_id SID_TOOL_RECHARGE_TIME = new string_id(STF_SURVEY, "tool_recharge_time");
    public static final string_id SID_NO_INV_SPACE = new string_id(STF_SURVEY, "no_inv_space");
    public static final string_id SID_CRITICAL_SUCCESS = new string_id(STF_SURVEY, "critical_success");
    public static final string_id SID_NODE_NOT_CLOSE = new string_id(STF_SURVEY, "node_not_close");
    public static final string_id SID_NODE_RECOVERY = new string_id(STF_SURVEY, "node_recovery");
    public static final string_id SID_GAMBLE_SUCCESS = new string_id(STF_SURVEY, "gamble_success");
    public static final string_id SID_GAMBLE_FAIL = new string_id(STF_SURVEY, "gamble_fail");
    public static final string_id SID_PET_SEARCH_SUCCESS = new string_id(STF_SURVEY, "pet_search_success");
    public static final string_id SID_PET_SEARCH_FAIL = new string_id(STF_SURVEY, "pet_search_fail");
    public static final string_id SID_GAMBLE_RARE = new string_id(STF_SURVEY, "gamble_rare");
    public static final string_id SID_EFFECTS_OF_RADIATION_SICKNESS = new string_id(STF_SURVEY, "effects_of_radiation_sickness");
    public static final string_id SID_SUI_SURVEY_TOOL_PROPERTIES_TITLE = new string_id(STF_SURVEY, "sui_survey_tool_properties_title");
    public static final string_id SID_SUI_SURVEY_TOOL_PROPERTIES_HEADER = new string_id(STF_SURVEY, "sui_survey_tool_properties_header");
    public static final string_id SID_SUI_SURVEY_TOOL_PROPERTIES_RESOURCE_HEADER = new string_id(STF_SURVEY, "sui_survey_tool_properties_resource_header");
    public static final string_id SID_SUI_SURVEY_TOOL_PROPERTIES_RESOURCE = new string_id(STF_SURVEY, "sui_survey_tool_properties_resource");
    public static final string_id SID_SUI_SURVEY_TOOL_PROPERTIES_RESOLUTION = new string_id(STF_SURVEY, "sui_survey_tool_properties_resolution");
    public static final string_id SID_SUI_SURVEY_TOOL_PROPERTIES_RESOLUTION_MIN = new string_id(STF_SURVEY, "sui_survey_tool_properties_resolution_min");
    public static final string_id SID_SUI_SURVEY_TOOL_PROPERTIES_RESOLUTION_MAX = new string_id(STF_SURVEY, "sui_survey_tool_properties_resolution_max");
    public static final string_id SID_SUI_SURVEY_TOOL_PROPERTIES_RESOLUTION_VAL = new string_id(STF_SURVEY, "sui_survey_tool_properties_resolution_val");
    public static final string_id SID_SUI_SURVEY_TOOL_PROPERTIES_RANGE = new string_id(STF_SURVEY, "sui_survey_tool_properties_range");
    public static final string_id SID_SUI_SURVEY_TOOL_PROPERTIES_RANGE_MIN = new string_id(STF_SURVEY, "sui_survey_tool_properties_range_min");
    public static final string_id SID_SUI_SURVEY_TOOL_PROPERTIES_RANGE_MAX = new string_id(STF_SURVEY, "sui_survey_tool_properties_range_max");
    public static final string_id SID_SUI_SURVEY_TOOL_PROPERTIES_RANGE_VAL = new string_id(STF_SURVEY, "sui_survey_tool_properties_range_val");
    public static final string_id SID_SURVEY_ERROR = new string_id("error_message", "survey_error");
    public static final String DATATABLE_RESOURCES = "datatables/resource/resource_tree.iff";
    public static final String DATATABLE_COL_ENUM = "Enum";
    public static final String DATATABLE_COL_RESOURCE_CRATE_TYPE = "Resource Container Type";

    public static int getPlanetaryMiningAmount(float density, int surveying, int duration, int samplingInterval, int samplingIncrease, boolean falleensFist) throws InterruptedException
    {
        if (density <= 0 || duration < 1 || samplingInterval < 1)
        {
            return 0;
        }
        float threshold = SAMPLE_DENSITY_THRESHOLD * ((100.0f - surveying) / 100.0f);
        float potentialAmount = BASE_SAMPLE_AMOUNT * (density - threshold);
        if (potentialAmount < 1)
        {
            return 0;
        }
        float chance = 50.0f + 20.0f * ((surveying - 15.0f) / 85.0f);
        if (chance > 70.0f)
        {
            chance = 70.0f;
        }
        float expectedAmount = 0.0f;
        for (int roll = 1; roll <= 100; ++roll)
        {
            if (roll <= chance)
            {
                float resultModifier = ((2 * chance) - roll) / (2 * chance);
                int successfulAmount = (int)(potentialAmount * resultModifier);
                successfulAmount = Math.max(1, successfulAmount);
                if (samplingIncrease > 0)
                {
                    successfulAmount += (int)(successfulAmount * samplingIncrease / 100.0f);
                }
                if (falleensFist)
                {
                    successfulAmount = (int)(successfulAmount * 1.5f);
                }
                expectedAmount += successfulAmount / 100.0f;
            }
        }
        int sampleCount = duration / samplingInterval;
        return (int)(expectedAmount * sampleCount);
    }
    public static int getPlanetaryMiningDuration(obj_id item) throws InterruptedException
    {
        float duration = getFloatObjVar(item, craftinglib.COMPONENT_ATTRIBUTE_OBJVAR_NAME + ".duration");
        if (duration <= 0)
        {
            duration = 20.0f;
        }
        return Math.round(Math.max(20.0f, Math.min(24.0f, duration)) * 60 * 60);
    }
    public static int getPlanetaryMiningSamplingInterval(obj_id player) throws InterruptedException
    {
        int samplingDelay = PLANETARY_MINING_BASE_SAMPLING_DELAY - getSkillStatisticModifier(player, "expertise_resource_sampling_time_decrease");
        return Math.max(PLANETARY_MINING_MIN_SAMPLING_DELAY, samplingDelay) + PLANETARY_MINING_SAMPLE_LOOP_OVERHEAD;
    }
    public static boolean sendPlanetaryMiningControllerMessage(String handler, dictionary params) throws InterruptedException
    {
        return messageTo(PLANETARY_MINING_CONTROLLER, handler, params, 0.0f, true);
    }
    public static int nextPlanetaryMiningSamplingSequence(obj_id player, String variable) throws InterruptedException
    {
        int current = getIntObjVar(player, variable);
        int next = current < 1 || current >= 2147483647 ? 1 : current + 1;
        setObjVar(player, variable, next);
        return next;
    }
    public static boolean beginPlanetaryMiningSamplingAuthorization(obj_id player, obj_id tool, String resourceType) throws InterruptedException
    {
        if (!isIdValid(player) || !isIdValid(tool) || resourceType == null || resourceType.equals("") || hasObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY) || hasObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING) || utils.hasScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_SESSION) || hasPendingPlanetaryMiningSamplingAuthorization(player))
        {
            return false;
        }
        retirePlanetaryMiningSamplingEvent(player);
        int currentSession = getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_SESSION_SEQUENCE);
        int sessionEpoch = getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_SESSION_EPOCH);
        if (sessionEpoch < 1)
        {
            sessionEpoch = 1;
        }
        if ((currentSession >= 2147483647 || getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_CLAIM_SEQUENCE) >= 2147483647) && sessionEpoch >= 2147483647)
        {
            return false;
        }
        int sessionSequence = nextPlanetaryMiningSamplingSequence(player, VAR_PLANETARY_MINING_SAMPLING_SESSION_SEQUENCE);
        int currentClaim = getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_CLAIM_SEQUENCE);
        int claimSequence = nextPlanetaryMiningSamplingSequence(player, VAR_PLANETARY_MINING_SAMPLING_CLAIM_SEQUENCE);
        if (currentSession >= 2147483647 || currentClaim >= 2147483647)
        {
            sessionEpoch = sessionEpoch + 1;
        }
        setObjVar(player, VAR_PLANETARY_MINING_SAMPLING_SESSION_EPOCH, sessionEpoch);
        setObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_TOOL, tool);
        setObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_EPOCH, sessionEpoch);
        setObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_SESSION, sessionSequence);
        setObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_CLAIM, claimSequence);
        setObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_RESOURCE, resourceType);
        // Durable candidate identity supports release-before-START fencing.  The
        // runnable local grant is installed only by the correlated START reply.
        utils.setScriptVar(tool, "surveying.player", player);
        if (!requestPlanetaryMiningSamplingAuthorization(player, tool, sessionSequence, PLANETARY_MINING_SAMPLING_AUTHORIZATION_START))
        {
            cleanupTool(player, tool);
            return false;
        }
        return true;
    }
    public static boolean requestPlanetaryMiningSamplingAuthorization(obj_id player, obj_id tool, int sessionSequence, int kind) throws InterruptedException
    {
        if (!isIdValid(player) || !isIdValid(tool) || sessionSequence < 1 || kind != PLANETARY_MINING_SAMPLING_AUTHORIZATION_START || hasPendingPlanetaryMiningSamplingAuthorization(player))
        {
            return false;
        }
        int sessionEpoch = getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_EPOCH);
        int claimSequence = getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_CLAIM);
        if (!isStagedPlanetaryMiningSamplingIdentity(player, tool, sessionEpoch, sessionSequence, claimSequence))
        {
            return false;
        }
        int requestSequence = nextPlanetaryMiningSamplingSequence(player, VAR_PLANETARY_MINING_SAMPLING_REQUEST_SEQUENCE);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_TOOL, tool);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_EPOCH, sessionEpoch);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_SESSION, sessionSequence);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_CLAIM, claimSequence);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_REQUEST, requestSequence);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_KIND, kind);
        dictionary request = new dictionary();
        request.put("player", player);
        request.put("tool", tool);
        request.put("sessionEpoch", sessionEpoch);
        request.put("sessionSequence", sessionSequence);
        request.put("claimSequence", claimSequence);
        request.put("requestSequence", requestSequence);
        request.put("kind", kind);
        if (!messageTo(PLANETARY_MINING_CONTROLLER, "requestPlanetaryMiningSamplingClaim", request, 0.0f, false))
        {
            clearPlanetaryMiningSamplingAuthorization(player, tool, sessionEpoch, sessionSequence, claimSequence, requestSequence, kind);
            return false;
        }
        dictionary timeout = new dictionary();
        timeout.put("player", player);
        timeout.put("tool", tool);
        timeout.put("sessionEpoch", sessionEpoch);
        timeout.put("sessionSequence", sessionSequence);
        timeout.put("claimSequence", claimSequence);
        timeout.put("requestSequence", requestSequence);
        timeout.put("kind", kind);
        if (!messageTo(player, "handlePlanetaryMiningSamplingAuthorizationTimeout", timeout, PLANETARY_MINING_SAMPLING_AUTHORIZATION_TIMEOUT, false))
        {
            clearPlanetaryMiningSamplingAuthorization(player, tool, sessionEpoch, sessionSequence, claimSequence, requestSequence, kind);
            return false;
        }
        return true;
    }
    public static boolean hasPendingPlanetaryMiningSamplingAuthorization(obj_id player) throws InterruptedException
    {
        return isIdValid(player) && utils.hasScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_REQUEST);
    }
    public static boolean isPlanetaryMiningSamplingAuthorizationPending(obj_id player, obj_id tool, int sessionEpoch, int sessionSequence, int claimSequence, int requestSequence, int kind) throws InterruptedException
    {
        return kind == PLANETARY_MINING_SAMPLING_AUTHORIZATION_START && isStagedPlanetaryMiningSamplingIdentity(player, tool, sessionEpoch, sessionSequence, claimSequence) && requestSequence > 0 && utils.hasScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_REQUEST) && tool.equals(utils.getObjIdScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_TOOL)) && utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_EPOCH) == sessionEpoch && utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_SESSION) == sessionSequence && utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_CLAIM) == claimSequence && utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_REQUEST) == requestSequence && utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING_KIND) == kind;
    }
    public static boolean clearPlanetaryMiningSamplingAuthorization(obj_id player, obj_id tool, int sessionEpoch, int sessionSequence, int claimSequence, int requestSequence, int kind) throws InterruptedException
    {
        if (!isPlanetaryMiningSamplingAuthorizationPending(player, tool, sessionEpoch, sessionSequence, claimSequence, requestSequence, kind))
        {
            return false;
        }
        utils.removeScriptVarTree(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING);
        return true;
    }
    public static boolean consumePlanetaryMiningSamplingAuthorization(obj_id player, obj_id tool, dictionary params, int expectedKind) throws InterruptedException
    {
        if (expectedKind != PLANETARY_MINING_SAMPLING_AUTHORIZATION_START || params == null || !player.equals(params.getObjId("player")) || !tool.equals(params.getObjId("tool")) || params.getInt("kind") != expectedKind || !params.getBoolean("allowed"))
        {
            return false;
        }
        int sessionEpoch = params.getInt("sessionEpoch");
        int sessionSequence = params.getInt("sessionSequence");
        int claimSequence = params.getInt("claimSequence");
        int requestSequence = params.getInt("requestSequence");
        if (!clearPlanetaryMiningSamplingAuthorization(player, tool, sessionEpoch, sessionSequence, claimSequence, requestSequence, expectedKind))
        {
            return false;
        }
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EPOCH, sessionEpoch);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_SESSION, sessionSequence);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_CLAIM, claimSequence);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_TOOL, tool);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_RESOURCE, getStringObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_RESOURCE));
        return true;
    }
    public static boolean isStagedPlanetaryMiningSamplingIdentity(obj_id player, obj_id tool, int sessionEpoch, int sessionSequence, int claimSequence) throws InterruptedException
    {
        return isIdValid(player) && isIdValid(tool) && sessionEpoch > 0 && sessionSequence > 0 && claimSequence > 0 && hasObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY) && tool.equals(getObjIdObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_TOOL)) && sessionEpoch == getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_EPOCH) && sessionSequence == getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_SESSION) && claimSequence == getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_CLAIM);
    }
    public static boolean beginPlanetaryMiningSampleOperation(obj_id player, obj_id tool, dictionary params) throws InterruptedException
    {
        if (!isCurrentPlanetaryMiningSamplingIdentity(player, tool, params))
        {
            return false;
        }
        int depth = utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_OPERATION_DEPTH);
        if (depth < 0 || depth == Integer.MAX_VALUE)
        {
            return false;
        }
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_OPERATION_DEPTH, depth + 1);
        return true;
    }
    public static void endPlanetaryMiningSampleOperation(obj_id player) throws InterruptedException
    {
        if (!isIdValid(player))
        {
            return;
        }
        int depth = utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_OPERATION_DEPTH);
        if (depth > 1)
        {
            utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_OPERATION_DEPTH, depth - 1);
            return;
        }
        utils.removeScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_OPERATION_DEPTH);
        if (utils.hasScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_DEFERRED_RELEASE))
        {
            utils.removeScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_DEFERRED_RELEASE);
            retryPlanetaryMiningSamplingRelease(player);
        }
    }
    public static boolean isCurrentPlanetaryMiningSamplingSession(obj_id player, obj_id tool, int sessionSequence) throws InterruptedException
    {
        return isCurrentPlanetaryMiningSamplingIdentity(player, tool, getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_EPOCH), sessionSequence, getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_CLAIM));
    }
    public static boolean isCurrentPlanetaryMiningSamplingIdentity(obj_id player, obj_id tool, int sessionEpoch, int sessionSequence, int claimSequence) throws InterruptedException
    {
        return isIdValid(player) && isIdValid(tool) && sessionEpoch > 0 && sessionSequence > 0 && claimSequence > 0 && hasObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY) && tool.equals(getObjIdObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_TOOL)) && sessionEpoch == getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_EPOCH) && sessionSequence == getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_SESSION) && claimSequence == getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_CLAIM) && utils.hasScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_SESSION) && tool.equals(utils.getObjIdScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_TOOL)) && sessionEpoch == utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EPOCH) && sessionSequence == utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_SESSION) && claimSequence == utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_CLAIM);
    }
    public static boolean isCurrentPlanetaryMiningSamplingIdentity(obj_id player, obj_id tool, dictionary params) throws InterruptedException
    {
        return params != null && player.equals(params.getObjId("player")) && isCurrentPlanetaryMiningSamplingIdentity(player, tool, params.getInt("sessionEpoch"), params.getInt("sessionSequence"), params.getInt("claimSequence"));
    }
    public static int getCurrentPlanetaryMiningSamplingSession(obj_id player, obj_id tool) throws InterruptedException
    {
        if (!isIdValid(player) || !isIdValid(tool) || !utils.hasScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_SESSION) || !tool.equals(utils.getObjIdScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_TOOL)))
        {
            return 0;
        }
        return utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_SESSION);
    }
    public static boolean stopPlanetaryMiningSampling(obj_id player, String reason) throws InterruptedException
    {
        if (!isIdValid(player))
        {
            return false;
        }
        boolean hadSampling = hasObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY) || utils.hasScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_SESSION) || hasPendingPlanetaryMiningSamplingAuthorization(player) || utils.hasScriptVar(player, "surveying.takingSamples");
        if (!hadSampling)
        {
            return false;
        }
        obj_id tool = utils.hasScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_TOOL) ? utils.getObjIdScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_TOOL) : utils.getObjIdScriptVar(player, "surveying.tool");
        cleanupTool(player, tool, reason);
        queueCommand(player, (-1465754503), player, "No params.", COMMAND_PRIORITY_FRONT);
        return true;
    }
    public static void retirePlanetaryMiningSamplingEvent(obj_id player) throws InterruptedException
    {
        if (!isIdValid(player))
        {
            return;
        }
        if (sui.hasPid(player, PLANETARY_MINING_SAMPLING_EVENT_PID))
        {
            int pid = sui.getPid(player, PLANETARY_MINING_SAMPLING_EVENT_PID);
            if (pid > 0)
            {
                forceCloseSUIPage(pid);
            }
            sui.removePid(player, PLANETARY_MINING_SAMPLING_EVENT_PID);
        }
        if (sui.hasPid(player, PID_NAME))
        {
            int legacyPid = sui.getPid(player, PID_NAME);
            if (legacyPid > 0)
            {
                forceCloseSUIPage(legacyPid);
            }
            sui.removePid(player, PID_NAME);
        }
        utils.removeScriptVarTree(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT);
    }
    public static boolean recordPlanetaryMiningSamplingEvent(obj_id player, obj_id tool, int pid, int eventKind) throws InterruptedException
    {
        if (pid < 1 || !isCurrentPlanetaryMiningSamplingSession(player, tool, getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_SESSION)))
        {
            return false;
        }
        int eventSequence = nextPlanetaryMiningSamplingSequence(player, VAR_PLANETARY_MINING_SAMPLING_EVENT_SEQUENCE);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT + ".tool", tool);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT + ".epoch", getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_EPOCH));
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_SESSION, getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_SESSION));
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT + ".claim", getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_CLAIM));
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_SEQUENCE, eventSequence);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_PAGE, pid);
        utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_KIND, eventKind);
        sui.setPid(player, pid, PID_NAME);
        return true;
    }
    public static boolean isCurrentPlanetaryMiningSamplingEvent(obj_id player, obj_id tool, int pageId, int eventKind) throws InterruptedException
    {
        return isIdValid(player) && player.isAuthoritative() && isIdValid(tool) && utils.isNestedWithin(tool, player) && sui.hasPid(player, PID_NAME) && sui.getPid(player, PID_NAME) == pageId && utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_PAGE) == pageId && utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_KIND) == eventKind && isCurrentPlanetaryMiningSamplingIdentity(player, tool, utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT + ".epoch"), utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_SESSION), utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT + ".claim"));
    }
    public static boolean clearCurrentPlanetaryMiningSamplingEvent(obj_id player, obj_id tool, int pageId, int eventKind) throws InterruptedException
    {
        if (!isCurrentPlanetaryMiningSamplingEvent(player, tool, pageId, eventKind))
        {
            return false;
        }
        sui.removePid(player, PID_NAME);
        return true;
    }
    public static dictionary consumePlanetaryMiningSamplingEventPage(obj_id player, obj_id tool, int pageId, int eventKind) throws InterruptedException
    {
        if (!clearCurrentPlanetaryMiningSamplingEvent(player, tool, pageId, eventKind))
        {
            return null;
        }
        dictionary identity = new dictionary();
        identity.put("player", player);
        identity.put("tool", tool);
        identity.put("sessionEpoch", utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT + ".epoch"));
        identity.put("sessionSequence", utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_SESSION));
        identity.put("claimSequence", utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT + ".claim"));
        identity.put("eventSequence", utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_SEQUENCE));
        identity.put("pageId", pageId);
        identity.put("eventKind", eventKind);
        return identity;
    }
    public static boolean consumePlanetaryMiningSamplingContinuation(obj_id player, obj_id tool, dictionary params, int eventKind) throws InterruptedException
    {
        if (params == null || !isCurrentPlanetaryMiningSamplingIdentity(player, tool, params) || params.getInt("eventSequence") < 1 || params.getInt("eventSequence") != utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_SEQUENCE) || params.getInt("pageId") != utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_PAGE) || eventKind != utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT_KIND))
        {
            return false;
        }
        utils.removeScriptVarTree(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_EVENT);
        return true;
    }
    public static boolean stagePlanetaryMiningSamplingRelease(obj_id player, String reason) throws InterruptedException
    {
        if (!isIdValid(player) || !hasObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY))
        {
            return false;
        }
        if (hasObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING))
        {
            return false;
        }
        obj_id tool = getObjIdObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_TOOL);
        int sessionEpoch = getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_EPOCH);
        int sessionSequence = getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_SESSION);
        int claimSequence = getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY_CLAIM);
        if (!isIdValid(tool) || sessionEpoch < 1 || sessionSequence < 1 || claimSequence < 1)
        {
            return false;
        }
        // One durable value commits the exact END identity before local grant
        // fields are invalidated.  The controller ACK, not enqueue success,
        // retires this outbox entry.
        setObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING + ".identity", tool.getValue() + ":" + sessionEpoch + ":" + sessionSequence + ":" + claimSequence);
        setObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING + ".reason", reason);
        return true;
    }
    public static dictionary getPendingPlanetaryMiningSamplingRelease(obj_id player) throws InterruptedException
    {
        if (!isIdValid(player) || !hasObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING))
        {
            return null;
        }
        obj_id tool = obj_id.NULL_ID;
        int sessionEpoch = 0;
        int sessionSequence = 0;
        int claimSequence = 0;
        String encoded = getStringObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING + ".identity");
        if (encoded != null && !encoded.equals(""))
        {
            java.util.StringTokenizer tokens = new java.util.StringTokenizer(encoded, ":");
            if (tokens.countTokens() == 4)
            {
                try
                {
                    tool = obj_id.getObjId(Long.parseLong(tokens.nextToken()));
                    sessionEpoch = Integer.parseInt(tokens.nextToken());
                    sessionSequence = Integer.parseInt(tokens.nextToken());
                    claimSequence = Integer.parseInt(tokens.nextToken());
                }
                catch (NumberFormatException exception)
                {
                    return null;
                }
            }
        }
        else
        {
            // Bounded migration for the previous multi-field pending shape.
            tool = getObjIdObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING + ".tool");
            sessionEpoch = getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING + ".session_epoch");
            sessionSequence = getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING + ".session_sequence");
            claimSequence = getIntObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING + ".claim_sequence");
            if (isIdValid(tool) && sessionEpoch > 0 && sessionSequence > 0 && claimSequence > 0)
            {
                setObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING + ".identity", tool.getValue() + ":" + sessionEpoch + ":" + sessionSequence + ":" + claimSequence);
            }
        }
        if (!isIdValid(tool) || sessionEpoch < 1 || sessionSequence < 1 || claimSequence < 1)
        {
            return null;
        }
        dictionary release = new dictionary();
        release.put("player", player);
        release.put("tool", tool);
        release.put("sessionEpoch", sessionEpoch);
        release.put("sessionSequence", sessionSequence);
        release.put("claimSequence", claimSequence);
        release.put("reason", getStringObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING + ".reason"));
        return release;
    }
    public static boolean isPendingPlanetaryMiningSamplingRelease(obj_id player, dictionary params) throws InterruptedException
    {
        dictionary pending = getPendingPlanetaryMiningSamplingRelease(player);
        return pending != null && params != null && player.equals(params.getObjId("player")) && pending.getObjId("tool").equals(params.getObjId("tool")) && pending.getInt("sessionEpoch") == params.getInt("sessionEpoch") && pending.getInt("sessionSequence") == params.getInt("sessionSequence") && pending.getInt("claimSequence") == params.getInt("claimSequence");
    }
    public static boolean retryPlanetaryMiningSamplingRelease(obj_id player) throws InterruptedException
    {
        if (!isIdValid(player) || !hasObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING))
        {
            return true;
        }
        if (utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_OPERATION_DEPTH) > 0)
        {
            utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_DEFERRED_RELEASE, 1);
            return false;
        }
        dictionary release = getPendingPlanetaryMiningSamplingRelease(player);
        if (release == null)
        {
            return false;
        }
        boolean sent = sendPlanetaryMiningControllerMessage("releasePlanetaryMiningSamplingClaim", release);
        if (hasObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING) && !utils.hasScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_RELEASE_RETRY))
        {
            utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_RELEASE_RETRY, 1);
            if (!messageTo(player, "retryPlanetaryMiningSamplingRelease", release, PLANETARY_MINING_SAMPLING_RELEASE_RETRY_DELAY, false))
            {
                utils.removeScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_RELEASE_RETRY);
            }
        }
        return sent;
    }
    public static boolean acknowledgePlanetaryMiningSamplingRelease(obj_id player, dictionary params) throws InterruptedException
    {
        if (!isPendingPlanetaryMiningSamplingRelease(player, params))
        {
            return false;
        }
        removeObjVar(player, VAR_PLANETARY_MINING_SAMPLING_RELEASE_PENDING);
        utils.removeScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_RELEASE_RETRY);
        return true;
    }
    public static final String DEFAULT_CONTAINER = "object/resource_container/simple.iff";
    public static final int CONTAINER_VOLUME_MAX = 100000;
    public static final String MSG_SELECT_RANGE = "@" + STF_SURVEY + ":select_range";
    public static final String MSG_SELECT_CLASS = "@" + STF_SURVEY + ":select_class";
    public static final int SAMPLE_STOP_LOOP = 0;
    public static final int SAMPLE_CONTINUE_LOOP = 1;
    public static final int SAMPLE_CONTINUE_LOOP_NOSAMPLE = 2;
    public static final int SAMPLE_PAUSE_LOOP_EVENT = 3;
    public static boolean isResourceDerivedFrom(String resource_type, String parent_class) throws InterruptedException
    {
        if ((resource_type.equals("")) || (parent_class.equals("")))
        {
            return false;
        }
        if (resource_type.equals(parent_class))
        {
            return true;
        }
        obj_id resourceId = getResourceTypeByName(resource_type);
        if ((resourceId == null) || (resourceId == obj_id.NULL_ID))
        {
            return false;
        }
        if (isResourceDerivedFrom(resourceId, parent_class))
        {
            return true;
        }
        return false;
    }
    public static obj_id[] create(obj_id resourceId, int amt, obj_id targetContainer, obj_id playerId) throws InterruptedException
    {
        if ((resourceId == null) || (resourceId == obj_id.NULL_ID))
        {
            return null;
        }
        if ((targetContainer == null) || (targetContainer == obj_id.NULL_ID))
        {
            return null;
        }
        if (amt < 1)
        {
            return null;
        }
        int total = amt;
        Vector ret = new Vector();
        ret.setSize(0);
        String containerTemplate = getResourceContainerTemplate(resourceId);
        if ((containerTemplate == null) || (containerTemplate.equals("")))
        {
            containerTemplate = DEFAULT_CONTAINER;
        }
        int chunks = amt / CONTAINER_VOLUME_MAX;
        if (amt % CONTAINER_VOLUME_MAX > 0)
        {
            chunks++;
        }
        for (int i = 0; i < chunks; i++)
        {
            if (total > 0)
            {
                obj_id crate = createObject(containerTemplate, targetContainer, "");
                if ((crate != null) && (crate != obj_id.NULL_ID)) {
                    int containerAmount = CONTAINER_VOLUME_MAX;
                    if (total < CONTAINER_VOLUME_MAX)
                    {
                        containerAmount = total;
                    }
                    addResourceToContainer(crate, resourceId, containerAmount, playerId);
                    total -= containerAmount;
                    ret = utils.addElement(ret, crate);
                }
            }
        }
        if ((ret == null) || (ret.size() == 0))
        {
            return null;
        }
        obj_id[] _ret = new obj_id[ret.size()];
        ret.toArray(_ret);
        return _ret;
    }
    public static obj_id[] create(obj_id resourceId, int amt, obj_id targetContainer) throws InterruptedException
    {
        return create(resourceId, amt, targetContainer, obj_id.NULL_ID);
    }
    public static obj_id[] createRandom(String resourceClass, int amt, location loc, obj_id targetContainer, obj_id playerId, int min) throws InterruptedException
    {
        if ((resourceClass == null) || (resourceClass.equals("")))
        {
            return null;
        }
        if (loc == null)
        {
            return null;
        }
        if (min < 1)
        {
            min = 1;
        }
        if (!isIdValid(targetContainer))
        {
            return null;
        }
        if (getVolumeFree(targetContainer) <= 0)
        {
            return null;
        }
        obj_id resourceId = null;
        if ((loc.compareTo(new location()) == 0 || (loc.cell != null && loc.cell != obj_id.NULL_ID)) && isIdValid(targetContainer))
        {
            obj_id topmost = getTopMostContainer(targetContainer);
            if (topmost != targetContainer)
            {
                loc = getLocation(topmost);
            }
            resourceId = pickRandomNonDepeletedResource(resourceClass);
            if ((resourceId == null) || (resourceId == obj_id.NULL_ID))
            {
                return null;
            }
        }
        else 
        {
            resource_density[] availableResources = requestResourceList(loc, 0.0f, 1.0f, resourceClass);
            if (availableResources == null || availableResources.length == 0)
            {
                return null;
            }
            int idx = rand(0, availableResources.length - 1);
            resourceId = availableResources[idx].getResourceType();
            if ((resourceId == null) || (resourceId == obj_id.NULL_ID))
            {
                return null;
            }
        }
        float eff = getResourceEfficiency(resourceId, loc);
        int finalAmount = oneTimeHarvest(resourceId, amt, loc);
        if (finalAmount < min)
        {
            finalAmount = min;
        }
        utils.setScriptVar(playerId, "resource.lastEff", eff);
        utils.setScriptVar(playerId, "resource.lastAmt", finalAmount);
        utils.setScriptVar(playerId, "resource.lastId", resourceId);
        return create(resourceId, finalAmount, targetContainer, playerId);
    }
    public static obj_id[] createRandom(String resourceClass, int amt, location loc, obj_id targetContainer, obj_id playerId) throws InterruptedException
    {
        return createRandom(resourceClass, amt, loc, targetContainer, playerId, 1);
    }
    public static obj_id[] createRandom(String resourceClass, int amt, location loc, obj_id targetContainer) throws InterruptedException
    {
        return createRandom(resourceClass, amt, loc, targetContainer, obj_id.NULL_ID, 1);
    }
    public static boolean setToolClass(obj_id tool, int resource_class) throws InterruptedException
    {
        if (tool == null)
        {
            return false;
        }
        if ((resource_class < CLASS_RESOURCE) || (resource_class >= CLASS_MAX))
        {
            return false;
        }
        return setObjVar(tool, VAR_SURVEY_CLASS, CLASS_NAME[resource_class]);
    }
    public static boolean requestSetToolRatio(obj_id user, obj_id tool) throws InterruptedException
    {
        if ((user == null) || (tool == null))
        {
            return false;
        }
        String resource_class = getStringObjVar(tool, resource.VAR_SURVEY_CLASS);
        if (resource_class.equals(""))
        {
            sendSystemMessageTestingOnly(user, "ERROR: survey tool broken -> no assigned resource class");
            return false;
        }
        String baseMod = getSkillModForClass(resource_class);
        if (baseMod.equals(""))
        {
            sendSystemMessageTestingOnly(user, "ERROR: survey tool broken -> unknown resource class");
            return false;
        }
        int rangeVal = getSkillStatMod(user, "surveying") / 20;
        if (rangeVal < 1)
        {
            rangeVal = 1;
        }
        int range_min = getIntObjVar(tool, VAR_SURVEY_RANGE_MIN);
        int range_max = getIntObjVar(tool, VAR_SURVEY_RANGE_MAX);
        int res_min = getIntObjVar(tool, VAR_SURVEY_RESOLUTION_MIN);
        int res_max = getIntObjVar(tool, VAR_SURVEY_RESOLUTION_MAX);
        Vector options = new Vector();
        options.setSize(0);
        Vector ranges = new Vector();
        ranges.setSize(0);
        Vector resolutions = new Vector();
        resolutions.setSize(0);
        float res = SURVEY_RESOLUTION_MIN;
        int count = 1;
        for (int range = range_min; range <= range_max; range += ((range_max - range_min) / 5))
        {
            if (count > rangeVal)
            {
                break;
            }
            res += (res_max - res_min) / 5.0;
            ranges = utils.addElement(ranges, range);
            resolutions = utils.addElement(resolutions, (int)res);
            options = utils.addElement(options, "" + range + "m x " + ((int)res) + "pts");
            count++;
        }
        if (ranges.size() == 0 || resolutions.size() == 0)
        {
            return false;
        }
        removeObjVar(tool, VAR_SETTINGS_BASE);
        setObjVar(tool, VAR_SETTINGS_PLAYER, user);
        setObjVar(tool, VAR_SETTINGS_RANGES, ranges);
        setObjVar(tool, VAR_SETTINGS_RESOLUTIONS, resolutions);
        sui.listbox(tool, user, MSG_SELECT_RANGE, options, HANDLER_SET_RANGE);
        return true;
    }
    public static int getSample(obj_id user, obj_id tool, String type) throws InterruptedException
    {
        if ((user == null) || (tool == null) || (type.equals("")))
        {
            return SAMPLE_STOP_LOOP;
        }
        obj_id typeId = getResourceTypeByName(type);
        if (!isIdValid(typeId))
        {
            debugSpeakMsg(user, "resource::getSample: unable to retrieve obj_id for " + type);
            sendSystemMessage(user, SID_SURVEY_ERROR);
            return SAMPLE_STOP_LOOP;
        }
        String resource_class = getStringObjVar(tool, resource.VAR_SURVEY_CLASS);
        if (resource_class == null || resource_class.equals(""))
        {
            sendSystemMessageTestingOnly(user, "ERROR: survey tool broken -> no assigned resource class");
            return SAMPLE_STOP_LOOP;
        }
        if (!isResourceDerivedFrom(typeId, resource_class))
        {
            sendSystemMessageProse(user, prose.getPackage(SID_WRONG_TOOL, type));
            return SAMPLE_STOP_LOOP;
        }
        float density = getResourceEfficiency(typeId, getLocation(user));
        if (density < 0)
        {
            return SAMPLE_STOP_LOOP;
        }
        else if (density < 0.1f)
        {
            sendSystemMessageProse(user, prose.getPackage(SID_EFFICIENCY_TOO_LOW, type));
            return SAMPLE_STOP_LOOP;
        }
        int modVal = getSkillStatMod(user, "surveying");
        float threshold = SAMPLE_DENSITY_THRESHOLD * ((100.0f - modVal) / 100.0f);
        if (density > threshold)
        {
            float deltaDensity = density - threshold;
            float famt = BASE_SAMPLE_AMOUNT * deltaDensity;
            if (famt < 1)
            {
                sendSystemMessageProse(user, prose.getPackage(SID_TRACE_AMOUNT, type));
                return SAMPLE_STOP_LOOP;
            }
            location nodecritloc = null;
            int gamble = 0;
            if (utils.hasScriptVar(user, "survey_event.tool"))
            {
                if (utils.hasScriptVar(user, "survey_event.gamble"))
                {
                    gamble = utils.getIntScriptVar(user, "survey_event.gamble");
                    
                    utils.removeScriptVar(user, "survey_event.gamble");
                    utils.removeScriptVar(user, "survey_event.tool");
                }
                else 
                {
                    obj_id crittool = utils.getObjIdScriptVar(user, "survey_event.tool");
                    if (crittool != tool)
                    {
                        utils.removeScriptVar(user, "survey_event.tool");
                    }
                    else 
                    {
                        nodecritloc = utils.getLocationScriptVar(user, "survey_event.location");
                        location ploc = getLocation(user);
                        if (utils.getDistance2D(ploc, nodecritloc) > 5)
                        {
                            utils.removeScriptVar(user, "survey_event.tool");
                            utils.removeScriptVar(user, "survey_event.location");
                            nodecritloc = null;
                            sendSystemMessage(user, SID_NODE_NOT_CLOSE);
                        }
                    }
                }
            }
            int city_id = city.checkCity(user, false);
            float chance = 50.0f + 20.0f * ((modVal - 15.0f) / 85.0f);
            if (chance > 70)
            {
                chance = 70;
            }
            int roll = rand(1, 100);
            if (city_id > 0 && city.cityHasSpec(city_id, city.SF_SPEC_SAMPLE_RICH))
            {
                chance += 10;
            }
            if ((roll <= chance) || (gamble > 0) || (nodecritloc != null))
            {
                float resultModifier = ((2 * chance) - roll) / (2 * chance);
                int amt = (int)(famt * resultModifier);
                if (amt == 0)
                {
                    amt = 1;
                }
                if (city_id > 0 && city.cityHasSpec(city_id, city.SF_SPEC_SAMPLE_RICH))
                {
                    amt *= 1.2;
                }
                if (gamble == 1)
                {
                    sendSystemMessage(user, SID_GAMBLE_SUCCESS);
                    amt *= 4;
                }
                else if (gamble == 2)
                {
                    sendSystemMessage(user, SID_GAMBLE_FAIL);
                }
                else if (gamble == 3)
                {
                    sendSystemMessage(user, SID_PET_SEARCH_SUCCESS);
                    amt *= 3;
                }
                else if (gamble == 4)
                {
                    sendSystemMessage(user, SID_PET_SEARCH_FAIL);
                }
                else if (gamble == 5)
                {
                    sendSystemMessage(user, SID_GAMBLE_RARE);
                }
                else if (nodecritloc != null)
                {
                    sendSystemMessage(user, SID_NODE_RECOVERY);
                    amt *= 3;
                    utils.removeScriptVar(user, "survey_event.location");
                    utils.removeScriptVar(user, "survey_event.tool");
                }
                else 
                {
                    int rollResult = 10;
                    if (isGod(user))
                    {
                        rollResult = 100;
                    }
                    if (roll <= rollResult)
                    {
                        int critRoll = rand(1, 100);
                        if (critRoll <= 50)
                        {
                            sendSystemMessage(user, SID_CRITICAL_SUCCESS);
                            amt *= 2;
                        }
                        else if (critRoll <= 75)
                        {
                            utils.setScriptVar(user, "survey_event.tool", tool);
                            obj_id beastCheck = (beast_lib.getBeastOnPlayer(user));
                            if (beastCheck != null)
                            {
                                String[] nodeOptions = new String[3];
                                nodeOptions[0] = "@survey:cnode_1";
                                nodeOptions[1] = "@survey:cnode_2";
                                nodeOptions[2] = "@survey:cnode_collection";
                                if (!sui.hasPid(user, PID_NAME))
                                {
                                    int pid = sui.listbox(user, user, "@survey:cnode_d", sui.OK_CANCEL, "@survey:cnode_t", nodeOptions, "handleSurveyNodeChoice", true);
                                    if (!recordPlanetaryMiningSamplingEvent(user, tool, pid, PLANETARY_MINING_SAMPLING_EVENT_NODE))
                                    {
                                        utils.removeScriptVarTree(user, "survey_event");
                                        return SAMPLE_STOP_LOOP;
                                    }
                                }
                            }
                            else 
                            {
                                String[] nodeOptions = new String[2];
                                nodeOptions[0] = "@survey:cnode_1";
                                nodeOptions[1] = "@survey:cnode_2";
                                if (!sui.hasPid(user, PID_NAME))
                                {
                                    int pid = sui.listbox(user, user, "@survey:cnode_d", sui.OK_CANCEL, "@survey:cnode_t", nodeOptions, "handleSurveyNodeChoice", true);
                                    if (!recordPlanetaryMiningSamplingEvent(user, tool, pid, PLANETARY_MINING_SAMPLING_EVENT_NODE))
                                    {
                                        utils.removeScriptVarTree(user, "survey_event");
                                        return SAMPLE_STOP_LOOP;
                                    }
                                }
                            }
                            return SAMPLE_PAUSE_LOOP_EVENT;
                        }
                        else 
                        {
                            utils.setScriptVar(user, "survey_event.tool", tool);
                            String collectionName = "col_resource_" + resource_class + "_01";
                            if (!hasCompletedCollectionSlot(user, collectionName))
                            {
                                String[] nodeOptions = new String[3];
                                nodeOptions[0] = "@survey:gnode_1";
                                nodeOptions[1] = "@survey:gnode_2";
                                nodeOptions[2] = "@survey:gnode_collection";
                                if (!sui.hasPid(user, PID_NAME))
                                {
                                    int pid = sui.listbox(user, user, "@survey:gnode_d", sui.OK_CANCEL, "@survey:gnode_t", nodeOptions, "handleSurveyGambleChoice", true);
                                    if (!recordPlanetaryMiningSamplingEvent(user, tool, pid, PLANETARY_MINING_SAMPLING_EVENT_GAMBLE))
                                    {
                                        utils.removeScriptVarTree(user, "survey_event");
                                        return SAMPLE_STOP_LOOP;
                                    }
                                }
                            }
                            else 
                            {
                                String[] nodeOptions = new String[2];
                                nodeOptions[0] = "@survey:gnode_1";
                                nodeOptions[1] = "@survey:gnode_2";
                                if (!sui.hasPid(user, PID_NAME))
                                {
                                    int pid = sui.listbox(user, user, "@survey:gnode_d", sui.OK_CANCEL, "@survey:gnode_t", nodeOptions, "handleSurveyGambleChoice", true);
                                    if (!recordPlanetaryMiningSamplingEvent(user, tool, pid, PLANETARY_MINING_SAMPLING_EVENT_GAMBLE))
                                    {
                                        utils.removeScriptVarTree(user, "survey_event");
                                        return SAMPLE_STOP_LOOP;
                                    }
                                }
                            }
                            return SAMPLE_PAUSE_LOOP_EVENT;
                        }
                    }
                }
                int expertiseResourceIncrease = getSkillStatisticModifier(user, "expertise_resource_sampling_increase");
                if (expertiseResourceIncrease > 0)
                {
                    amt += (int)(amt * expertiseResourceIncrease / 100.0f);
                }
                if (buff.hasBuff(user, "tcg_series4_falleens_fist"))
                {
                    amt = (int)(amt * 1.5f);
                    LOG("sissynoid", "Granting 50% Increase due to Falleen's Fist Buff");
                }
                String crateTemplate = getResourceContainerTemplate(typeId);
                if (!crateTemplate.equals(""))
                {
                    obj_id pInv = utils.getInventoryContainer(user);
                    if (pInv != null)
                    {
                        obj_id crate = createObject(crateTemplate, pInv, "");
                        if (addResourceToContainer(crate, typeId, amt, user))
                        {
                            location curloc = getLocation(user);
                            setLocation(crate, curloc);
                            putIn(crate, pInv, user);
                            if (hasScript(user, "theme_park.new_player.new_player"))
                            {
                                dictionary webster = new dictionary();
                                webster.put("sampling", 1);
                                messageTo(user, "handleNewPlayerArtisanAction", webster, 1, false);
                            }
                            if (hasScript(user, "toad.survey_quest"))
                            {
                                dictionary merriam = new dictionary();
                                merriam.put("resource", typeId);
                                messageTo(user, "forceSensitiveQuestInfo", merriam, 1, false);
                            }
                            sendSystemMessageProse(user, prose.getPackage(SID_SAMPLE_LOCATED, type, amt));
                            if (isResourceDerivedFrom(typeId, "radioactive"))
                        {
                            int pe = resource.getResourceAttribute(typeId, "res_potential_energy");
                            if (pe > 500)
                            {
                                int damage = (pe - 500) / 2;
                                if (damage < 1)
                                {
                                    damage = 1;
                                }
                                int current = getAttrib(user, HEALTH);
                                if (damage > current)
                                {
                                    damage = (current - 1);
                                }
                                addAttribModifier(user, HEALTH, (damage * -1), 0, 0, MOD_POOL);
                                int fatigue = damage / 4;
                                addShockWound(user, fatigue);
                                sendSystemMessage(user, SID_EFFECTS_OF_RADIATION_SICKNESS);
                            }
                        }
                            return SAMPLE_CONTINUE_LOOP;
                        }
                        else 
                        {
                            setObjVar(user, "sampleCrateGenFailed", 1);
                            sendSystemMessage(user, SID_NO_INV_SPACE);
                            setObjVar(user, VAR_SAMPLE_STAMP, getGameTime() - TIME_FAIL_BONUS);
                            return SAMPLE_STOP_LOOP;
                        }
                    }
                }
            }
        }
        else 
        {
            sendSystemMessageProse(user, prose.getPackage(SID_DENSITY_BELOW_THESHOLD, type));
            return SAMPLE_STOP_LOOP;
        }
        setObjVar(user, VAR_SAMPLE_STAMP, getGameTime() - TIME_FAIL_BONUS);
        sendSystemMessageProse(user, prose.getPackage(SID_SAMPLE_FAILED, type));
        return SAMPLE_CONTINUE_LOOP_NOSAMPLE;
    }
    public static String getResourceContainerTemplate(obj_id typeId) throws InterruptedException
    {
        if (typeId == null)
        {
            return "";
        }
        String tpf = getResourceContainerForType(typeId);
        if (tpf.equals(""))
        {
            return DEFAULT_CONTAINER;
        }
        return tpf;
    }
    public static boolean showToolProperties(obj_id target, obj_id tool) throws InterruptedException
    {
        if ((target == null) || (tool == null))
        {
            return false;
        }
        Vector dsrc = new Vector();
        dsrc.setSize(0);

        dsrc = utils.addElement(dsrc, utils.packStringId(SID_SUI_SURVEY_TOOL_PROPERTIES_RESOURCE_HEADER));

        prose_package ppResource = prose.getPackage(SID_SUI_SURVEY_TOOL_PROPERTIES_RESOURCE);
        prose.setTO(ppResource, getStringObjVar(tool, resource.VAR_SURVEY_CLASS));
        dsrc = utils.addElement(dsrc, " \0" + packOutOfBandProsePackage(null, ppResource));
        dsrc = utils.addElement(dsrc, " ");

        int res_min = getIntObjVar(tool, resource.VAR_SURVEY_RESOLUTION_MIN);
        int res_max = getIntObjVar(tool, resource.VAR_SURVEY_RESOLUTION_MAX);
        int res_val = getIntObjVar(tool, resource.VAR_SURVEY_RESOLUTION_VALUE);
        dsrc = utils.addElement(dsrc, utils.packStringId(SID_SUI_SURVEY_TOOL_PROPERTIES_RESOLUTION));

        prose_package ppResMin = prose.getPackage(SID_SUI_SURVEY_TOOL_PROPERTIES_RESOLUTION_MIN);
        prose.setDI(ppResMin, res_min);
        dsrc = utils.addElement(dsrc, " \0" + packOutOfBandProsePackage(null, ppResMin));

        prose_package ppResMax = prose.getPackage(SID_SUI_SURVEY_TOOL_PROPERTIES_RESOLUTION_MAX);
        prose.setDI(ppResMax, res_max);
        dsrc = utils.addElement(dsrc, " \0" + packOutOfBandProsePackage(null, ppResMax));

        prose_package ppResVal = prose.getPackage(SID_SUI_SURVEY_TOOL_PROPERTIES_RESOLUTION_VAL);
        prose.setDI(ppResVal, res_val);
        dsrc = utils.addElement(dsrc, " \0" + packOutOfBandProsePackage(null, ppResVal));
        dsrc = utils.addElement(dsrc, " ");

        int range_min = getIntObjVar(tool, resource.VAR_SURVEY_RANGE_MIN);
        int range_max = getIntObjVar(tool, resource.VAR_SURVEY_RANGE_MAX);
        int range_val = getIntObjVar(tool, resource.VAR_SURVEY_RANGE_VALUE);

        dsrc = utils.addElement(dsrc, utils.packStringId(SID_SUI_SURVEY_TOOL_PROPERTIES_RANGE));
        prose_package ppRangeMin = prose.getPackage(SID_SUI_SURVEY_TOOL_PROPERTIES_RANGE_MIN);
        prose.setDI(ppRangeMin, range_min);

        dsrc = utils.addElement(dsrc, " \0" + packOutOfBandProsePackage(null, ppRangeMin));
        prose_package ppRangeMax = prose.getPackage(SID_SUI_SURVEY_TOOL_PROPERTIES_RANGE_MAX);
        prose.setDI(ppRangeMax, range_max);

        dsrc = utils.addElement(dsrc, " \0" + packOutOfBandProsePackage(null, ppRangeMax));
        prose_package ppRangeVal = prose.getPackage(SID_SUI_SURVEY_TOOL_PROPERTIES_RANGE_VAL);
        prose.setDI(ppRangeVal, range_val);

        dsrc = utils.addElement(dsrc, " \0" + packOutOfBandProsePackage(null, ppRangeVal));
        if ((dsrc != null) && (dsrc.size() > 0))
        {
            String title = utils.packStringId(SID_SUI_SURVEY_TOOL_PROPERTIES_TITLE);
            String header = utils.packStringId(SID_SUI_SURVEY_TOOL_PROPERTIES_HEADER);
            sui.listbox(target, title, header, dsrc);
            return true;
        }
        return false;
    }
    public static String getSkillModForClass(String resource_class) throws InterruptedException
    {
        if (resource_class.equals("resource"))
        {
            return MOD_SURVEY_ALL;
        }
        if (resource_class.equals(""))
        {
            return "";
        }
        if (isFloraResource(resource_class))
        {
            return MOD_SURVEY_FLORA_STRUCTURAL;
        }
        if (isCreatureResource(resource_class))
        {
            return MOD_SURVEY_CREATURE;
        }
        if (isLiquid(resource_class))
        {
            return MOD_SURVEY_LIQUID;
        }
        if (isGas(resource_class))
        {
            return MOD_SURVEY_GAS;
        }
        if (isSolid(resource_class))
        {
            return MOD_SURVEY_SOLID;
        }
        if (isEnergyWindResource(resource_class))
        {
            return MOD_SURVEY_ENERGY_WIND;
        }
        if (isEnergySolarResource(resource_class))
        {
            return MOD_SURVEY_ENERGY_SOLAR;
        }
        if (isEnergyGeoThermal(resource_class))
        {
            return MOD_SURVEY_ENERGY_GEO;
        }
        if (isInorganic(resource_class))
        {
            return MOD_SURVEY_INORGANIC;
        }
        if (isOrganic(resource_class))
        {
            return MOD_SURVEY_ORGANIC;
        }
        return "";
    }
    public static boolean isEnergyGeoThermal(String resource_class) throws InterruptedException
    {
        if (resource_class.equals(""))
        {
            return false;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_ENERGY_GEO))
        {
            return true;
        }
        return false;
    }
    public static boolean isOrganic(String resource_class) throws InterruptedException
    {
        if (resource_class.equals(""))
        {
            return false;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_ORGANIC))
        {
            return true;
        }
        return false;
    }
    public static boolean isInorganic(String resource_class) throws InterruptedException
    {
        if (resource_class.equals(""))
        {
            return false;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_INORGANIC))
        {
            return true;
        }
        return false;
    }
    public static boolean isSolid(String resource_class) throws InterruptedException
    {
        if (resource_class.equals(""))
        {
            return false;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_MINERAL))
        {
            return true;
        }
        return false;
    }
    public static boolean isGas(String resource_class) throws InterruptedException
    {
        if (resource_class.equals(""))
        {
            return false;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_GAS))
        {
            return true;
        }
        return false;
    }
    public static boolean isLiquid(String resource_class) throws InterruptedException
    {
        if (resource_class.equals(""))
        {
            return false;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_LIQUID_WATER))
        {
            return true;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_LIQUID_CHEMICAL))
        {
            return true;
        }
        return false;
    }
    public static boolean isCreatureResource(String resource_class) throws InterruptedException
    {
        if (resource_class.equals(""))
        {
            return false;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_CREATURE_RESOURCES))
        {
            return true;
        }
        return false;
    }
    public static boolean isFloraResource(String resource_class) throws InterruptedException
    {
        if (resource_class.equals(""))
        {
            return false;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_FLORA_RESOURCES))
        {
            return true;
        }
        return false;
    }
    public static boolean isFloraFoodResource(String resource_class) throws InterruptedException
    {
        if (resource_class.equals(""))
        {
            return false;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_FLORA_FOOD))
        {
            return true;
        }
        return false;
    }
    public static boolean isFloraStructuralResource(String resource_class) throws InterruptedException
    {
        if (resource_class.equals(""))
        {
            return false;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_FLORA_STRUCTURAL))
        {
            return true;
        }
        return false;
    }
    public static boolean isEnergyWindResource(String resource_class) throws InterruptedException
    {
        if (resource_class.equals(""))
        {
            return false;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_ENERGY_WIND))
        {
            return true;
        }
        return false;
    }
    public static boolean isEnergySolarResource(String resource_class) throws InterruptedException
    {
        if (resource_class.equals(""))
        {
            return false;
        }
        if (isResourceClassDerivedFrom(resource_class, RT_ENERGY_SOLAR))
        {
            return true;
        }
        return false;
    }
    public static String getResourceName(String resource_class) throws InterruptedException
    {
        int rowNum = dataTableSearchColumnForString(resource_class, 1, DATATABLE_RESOURCES);
        if (rowNum < 0)
        {
            return "";
        }
        int i = 0;
        String res = null;
        int numRows = dataTableGetNumRows(DATATABLE_RESOURCES);
        while ((res == null) || (res.equals("")) || i > numRows)
        {
            res = dataTableGetRow(DATATABLE_RESOURCES, rowNum).getString("CLASS " + i++);
        }
        return res;
    }
    public static int getEnergyTotalOnTarget(obj_id target) throws InterruptedException
    {
        if (!isIdValid(target))
        {
            return -1;
        }
        int cnt = 0;
        obj_id[] items = getInventoryAndEquipment(target);
        if ((items != null) && (items.length > 0))
        {
            obj_id rId;
            for (obj_id item : items) {
                if (isGameObjectTypeOf(getGameObjectType(item), GOT_resource_container)) {
                    rId = getResourceContainerResourceType(item);
                    if (isIdValid(rId)) {
                        if (isResourceDerivedFrom(rId, "energy") || isResourceDerivedFrom(rId, "radioactive")) {
                            cnt += getPotentialEnergyValue(item);
                        }
                    }
                }
            }
        }
        return cnt;
    }
    public static int getPotentialEnergyValue(obj_id crate) throws InterruptedException
    {
        if (!isIdValid(crate))
        {
            return 0;
        }
        int amt = getResourceContainerQuantity(crate);
        if (amt < 1)
        {
            return 0;
        }
        obj_id rType = getResourceContainerResourceType(crate);
        if (!isIdValid(rType))
        {
            return 0;
        }
        return Math.round(getEnergyPowerRatio(rType) * amt);
    }
    public static float getEnergyPowerRatio(obj_id rType) throws InterruptedException
    {
        int potential = getResourceAttribute(rType, "res_potential_energy");
        if (potential < 1)
        {
            return 1.0f;
        }
        if (potential <= 500)
        {
            return 1.0f;
        }
        else 
        {
            return 1.0f + (potential - 500.0f) / 500.0f;
        }
    }
    public static int getResourceAttribute(String resourceName, String attribName) throws InterruptedException
    {
        if (resourceName == null || resourceName.equals(""))
        {
            return -1;
        }
        return getResourceAttribute(getResourceTypeByName(resourceName), attribName);
    }
    public static dictionary getGeoThermalEnergyInformationOnTarget(obj_id target) throws InterruptedException
    {
        if (!isIdValid(target))
        {
            return null;
        }
        int cnt = 0;
        String resourceName = "";
        int resourceQuality = 0;
        obj_id[] items = getInventoryAndEquipment(target);
        Vector tempListUniqueList = new Vector();
        tempListUniqueList.setSize(0);
        dictionary resourceInfo = new dictionary();
        if ((items != null) && (items.length > 0))
        {
            obj_id resourceType;
            for (obj_id item : items) {
                if (isGameObjectTypeOf(getGameObjectType(item), GOT_resource_container)) {
                    resourceType = getResourceContainerResourceType(item);
                    if (isIdValid(resourceType)) {
                        if (isResourceDerivedFrom(resourceType, "energy_renewable_site_limited_geothermal")) {
                            if (tempListUniqueList.size() == 0) {
                                tempListUniqueList.addElement(resourceType);
                                resourceName = getResourceName(resourceType);
                                resourceQuality = getResourceAttribute(resourceType, "res_quality");
                            }
                            if (tempListUniqueList.contains(resourceType)) {
                                cnt += getResourceContainerQuantity(item);
                            }
                        }
                    }
                }
            }
        }
        resourceInfo.put("resourceName", resourceName);
        resourceInfo.put("cnt", cnt);
        resourceInfo.put("resourceQuality", resourceQuality);
        return resourceInfo;
    }
    public static void cleanupTool(obj_id player, obj_id tool) throws InterruptedException
    {
        cleanupTool(player, tool, "tool_cleanup");
    }
    public static void cleanupTool(obj_id player, obj_id tool, String reason) throws InterruptedException
    {
        if (isIdValid(player))
        {
            stagePlanetaryMiningSamplingRelease(player, reason);
            retirePlanetaryMiningSamplingEvent(player);
            utils.removeScriptVarTree(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_PENDING);
            utils.removeScriptVarTree(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING);
            removeObjVar(player, VAR_PLANETARY_MINING_SAMPLING_IDENTITY);
            removeObjVar(player, "surveying.sampleLocation");
            utils.removeScriptVar(player, "surveying.takingSamples");
            utils.removeScriptVar(player, "surveying.tool");
            utils.removeScriptVar(player, "surveying.surveying");
            if (utils.getIntScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_OPERATION_DEPTH) > 0)
            {
                utils.setScriptVar(player, SCRIPTVAR_PLANETARY_MINING_SAMPLING_DEFERRED_RELEASE, 1);
            }
            else
            {
                retryPlanetaryMiningSamplingRelease(player);
            }
        }
        if (isIdValid(tool))
        {
            utils.removeScriptVar(tool, "surveying.takingSamples");
            utils.removeScriptVar(tool, "surveying.player");
            utils.removeScriptVar(tool, "surveying.surveying");
        }
    }
    public static boolean consumeResource(obj_id player, String resource, int quantity) throws InterruptedException
    {
        obj_id[] contents = getInventoryAndEquipment(player);
        if (contents == null || contents.length < 1)
        {
            return false;
        }
        obj_id container;
        String parent;

        for (obj_id content : contents) {
            if (!isIdValid(content) || !exists(content)) {
                continue;
            }
            if (!isResourceContainer(content)) {
                continue;
            }
            container = getResourceContainerResourceType(content);
            if (!isIdValid(container)) {
                continue;
            }
            parent = getResourceParentClass(getResourceClass(container));
            parent = getResourceParentClass(parent);
            parent = getResourceParentClass(parent);
            if (resource.equals(parent) && getResourceContainerQuantity(content) >= quantity) {
                removeResourceFromContainer(content, getResourceContainerResourceType(content), quantity);
                return true;
            }
        }
        return false;
    }
    public static boolean hasResource(obj_id player, String resource, int quantity) throws InterruptedException
    {
        obj_id[] contents = getInventoryAndEquipment(player);
        if (contents == null || contents.length < 1)
        {
            return false;
        }
        obj_id container;
        String parent;
        for (obj_id content : contents) {
            if (!isIdValid(content) || !exists(content)) {
                continue;
            }
            if (!isResourceContainer(content)) {
                continue;
            }
            container = getResourceContainerResourceType(content);
            if (!isIdValid(container)) {
                continue;
            }
            parent = getResourceParentClass(getResourceClass(container));
            parent = getResourceParentClass(parent);
            parent = getResourceParentClass(parent);
            if (resource.equals(parent) && getResourceContainerQuantity(content) >= quantity) {
                return true;
            }
        }
        return false;
    }
}
