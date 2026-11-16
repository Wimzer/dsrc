package script.systems.npc_mount;

import script.*;
import script.library.utils;

// One log channel for watching the squad without being in game.  Lines go through the
// native SharedLog (base_class.LOG); on swg-test the game servers forward this channel to
// the LogServer, which writes it to one file.  Every line is "event=NAME squad=ID key=value".
public class squad_monitor extends script.base_script
{
    public static final String CHANNEL = "npc_mount_squad";
    public static final String HEARTBEAT = "squadMonitorHeartbeat";
    public static final String SCRIPTVAR_TOKEN = "npcMountSquad.monitor.token";
    public static final float HEARTBEAT_SECONDS = 5.0f;
    // Unobserved AI hibernates and stops moving.  Keep squad actors active for this long
    // after they were last observed, as npc/cb_deflarian_courier.java does for its route.
    public static final float HIBERNATION_DELAY = 5.0f * 24.0f * 60.0f * 60.0f;

    // Census of what the live server actually has around Mos Eisley, so maps and route
    // planning include objects the captured buildout lacks (script-spawned content, events,
    // player structures).  One grid row per message keeps each script call small.
    public static final String CENSUS = "squadMonitorCensus";
    public static final float CENSUS_DELAY = 60.0f;
    public static final float CENSUS_X0 = 3150.0f, CENSUS_X1 = 3830.0f;
    public static final float CENSUS_Z0 = -5150.0f, CENSUS_Z1 = -4430.0f;
    public static final float CENSUS_STEP = 40.0f;
    public static final float CENSUS_RANGE = 30.0f;
    public static final float CENSUS_MIN_RADIUS = 0.25f;

    public squad_monitor()
    {
    }

    public static void startCensus(obj_id coordinator) throws InterruptedException
    {
        dictionary params = new dictionary();
        params.put("token", utils.getIntScriptVar(coordinator, SCRIPTVAR_TOKEN));
        params.put("row", 0);
        messageTo(coordinator, CENSUS, params, CENSUS_DELAY, false);
    }

    // Logs every solid exterior non-creature object within range of one row of grid points.
    // Objects near two grid points are logged twice; the viewer removes duplicates.
    public static void censusRow(obj_id coordinator, dictionary params) throws InterruptedException
    {
        if (!current(coordinator, params))
        {
            return;
        }
        int row = params.getInt("row");
        float z = CENSUS_Z0 + row * CENSUS_STEP;
        if (z > CENSUS_Z1)
        {
            event(coordinator, "censusDone", "rows=" + row);
            return;
        }
        String area = getLocation(coordinator).area;
        for (float x = CENSUS_X0; x <= CENSUS_X1; x += CENSUS_STEP)
        {
            obj_id[] found = getNonCreaturesInRange(new location(x, getHeightAtLocation(x, z), z, area), CENSUS_RANGE);
            if (found == null)
            {
                continue;
            }
            for (obj_id item : found)
            {
                if (!isIdValid(item) || !exists(item) || isPlayer(item))
                {
                    continue;
                }
                location at = getLocation(item);
                float radius = getObjectCollisionRadius(item);
                if (at == null || isIdValid(at.cell) || !(radius >= CENSUS_MIN_RADIUS))
                {
                    continue;
                }
                event(coordinator, "census", "obj=" + item + " template=" + getTemplateName(item)
                        + " x=" + tenth(at.x) + " z=" + tenth(at.z) + " yaw=" + tenth(getYaw(item)) + " r=" + tenth(radius));
            }
        }
        params.put("row", row + 1);
        messageTo(coordinator, CENSUS, params, 1.0f, false);
    }

    public static void event(obj_id coordinator, String name, String detail) throws InterruptedException
    {
        String line = "event=" + name + " squad=" + coordinator;
        if (detail != null && detail.length() > 0)
        {
            line += " " + detail;
        }
        LOG(CHANNEL, line);
    }

    public static String stateName(int state)
    {
        switch (state)
        {
            case squad_state.ASSEMBLING: return "assembling";
            case squad_state.DEPARTING: return "departing";
            case squad_state.PATROLLING: return "patrolling";
            case squad_state.COMBAT: return "combat";
            case squad_state.REGROUPING: return "regrouping";
            default: return "unknown" + state;
        }
    }

    // Heartbeat positions only while the squad is moving as a squad.
    public static boolean heartbeatState(int state)
    {
        return state == squad_state.DEPARTING || state == squad_state.PATROLLING || state == squad_state.REGROUPING;
    }

    public static float tenth(float value)
    {
        return Math.round(value * 10.0f) / 10.0f;
    }

    public static String where(String key, obj_id actor) throws InterruptedException
    {
        if (!isIdValid(actor) || !exists(actor))
        {
            return key + "=none";
        }
        location at = getLocation(actor);
        if (at == null)
        {
            return key + "=none";
        }
        return key + "=" + actor + "@" + tenth(at.x) + "," + tenth(at.z)
                + (hasCondition(actor, CONDITION_HIBERNATING) ? "~hibernating" : "");
    }

    // Starts a new heartbeat chain; an older chain stops at its next tick.
    public static void start(obj_id coordinator) throws InterruptedException
    {
        int token = utils.getIntScriptVar(coordinator, SCRIPTVAR_TOKEN) + 1;
        if (token <= 0)
        {
            token = 1;
        }
        utils.setScriptVar(coordinator, SCRIPTVAR_TOKEN, token);
        dictionary params = new dictionary();
        params.put("token", token);
        messageTo(coordinator, HEARTBEAT, params, HEARTBEAT_SECONDS, false);
    }

    public static boolean current(obj_id coordinator, dictionary params) throws InterruptedException
    {
        return params != null && params.getInt("token") == utils.getIntScriptVar(coordinator, SCRIPTVAR_TOKEN);
    }
}
