package script.systems.crafting.droid;

import script.*;
import script.library.*;

public class planetary_mining_controller extends script.base_script
{
    public static final String VAR_JOBS = "planetary_mining.jobs";
    public static final int STATE_RESERVED = 1;
    public static final int STATE_ACTIVE = 2;
    public static final int STATE_DELIVERING = 3;
    public static final int MAX_ACCOUNT_JOBS = 3;

    public int OnAttach(obj_id self) throws InterruptedException
    {
        setupController(self);
        return SCRIPT_CONTINUE;
    }

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        setupController(self);
        return SCRIPT_CONTINUE;
    }

    public int OnAboutToBeTransferred(obj_id self, obj_id destination, obj_id transferer) throws InterruptedException
    {
        return SCRIPT_OVERRIDE;
    }

    public void setupController(obj_id self) throws InterruptedException
    {
        setName(self, "Planetary Mining Droid Controller");
        persistObject(self);
        recoverJobs(self);
    }

    public void recoverJobs(obj_id self) throws InterruptedException
    {
        obj_var_list accounts = getObjVarList(self, VAR_JOBS);
        if (accounts == null)
        {
            return;
        }
        for (int accountIndex = 0; accountIndex < accounts.getNumItems(); ++accountIndex)
        {
            obj_var accountVar = accounts.getObjVar(accountIndex);
            if (accountVar == null)
            {
                continue;
            }
            int stationId;
            try
            {
                stationId = Integer.parseInt(accountVar.getName());
            }
            catch (NumberFormatException exception)
            {
                continue;
            }
            obj_var_list characters = accounts.getObjVarList(accountVar.getName());
            if (characters == null)
            {
                continue;
            }
            for (int characterIndex = 0; characterIndex < characters.getNumItems(); ++characterIndex)
            {
                obj_var characterVar = characters.getObjVar(characterIndex);
                if (characterVar == null)
                {
                    continue;
                }
                obj_id player;
                try
                {
                    player = obj_id.getObjId(Long.parseLong(characterVar.getName()));
                }
                catch (NumberFormatException exception)
                {
                    continue;
                }
                recoverJob(self, stationId, player);
            }
        }
    }

    public void recoverJob(obj_id self, int stationId, obj_id player) throws InterruptedException
    {
        String path = getJobPath(stationId, player);
        int sequence = getIntObjVar(self, path + ".sequence");
        int state = getIntObjVar(self, path + ".state");
        if (sequence < 1)
        {
            removeObjVar(self, path);
            return;
        }
        if (state == STATE_RESERVED)
        {
            sendReservationResponse(self, player, sequence, true, "");
        }
        else if (state == STATE_ACTIVE)
        {
            scheduleJob(self, stationId, player, sequence);
        }
        else if (state == STATE_DELIVERING)
        {
            sendDelivery(self, path, player, sequence);
        }
        else
        {
            removeObjVar(self, path);
        }
    }

    public int reservePlanetaryMiningDroidJob(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = getRequestPlayer(params);
        int sequence = params == null ? 0 : params.getInt("jobSequence");
        int stationId = getStationId(player);
        if (stationId < 1 || sequence < 1)
        {
            sendReservationResponse(self, player, sequence, false, "invalid_request");
            return SCRIPT_CONTINUE;
        }

        String path = getJobPath(stationId, player);
        if (hasObjVar(self, path))
        {
            int existingSequence = getIntObjVar(self, path + ".sequence");
            if (existingSequence == sequence)
            {
                int state = getIntObjVar(self, path + ".state");
                if (state == STATE_RESERVED)
                {
                    sendReservationResponse(self, player, sequence, true, "");
                }
                else
                {
                    sendJobState(self, path, player);
                }
            }
            else
            {
                sendReservationResponse(self, player, sequence, false, "character_active");
            }
            return SCRIPT_CONTINUE;
        }
        if (getAccountJobCount(self, stationId) >= MAX_ACCOUNT_JOBS)
        {
            sendReservationResponse(self, player, sequence, false, "account_limit");
            return SCRIPT_CONTINUE;
        }
        setObjVar(self, path + ".sequence", sequence);
        setObjVar(self, path + ".state", STATE_RESERVED);
        sendReservationResponse(self, player, sequence, true, "");
        return SCRIPT_CONTINUE;
    }

    public int commitPlanetaryMiningDroidJob(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = getRequestPlayer(params);
        int sequence = params == null ? 0 : params.getInt("jobSequence");
        int stationId = getStationId(player);
        String path = getJobPath(stationId, player);
        if (stationId < 1 || sequence < 1 || getIntObjVar(self, path + ".sequence") != sequence)
        {
            sendReservationResponse(self, player, sequence, false, "invalid_reservation");
            return SCRIPT_CONTINUE;
        }
        int state = getIntObjVar(self, path + ".state");
        if (state == STATE_RESERVED)
        {
            if (!isCommitValid(params))
            {
                removeObjVar(self, path);
                sendReservationResponse(self, player, sequence, false, "invalid_snapshot");
                return SCRIPT_CONTINUE;
            }
            int duration = params.getInt("duration");
            int surveying = params.getInt("surveying");
            float density = params.getFloat("density");
            int samplingInterval = params.getInt("samplingInterval");
            int samplingIncrease = params.getInt("samplingIncrease");
            int falleensFist = params.getInt("falleensFist");
            int amount = resource.getPlanetaryMiningAmount(density, surveying, duration, samplingInterval, samplingIncrease, falleensFist != 0);
            setObjVar(self, path + ".resource", params.getObjId("resource"));
            setObjVar(self, path + ".amount", amount);
            setObjVar(self, path + ".duration", duration);
            setObjVar(self, path + ".surveying", surveying);
            setObjVar(self, path + ".density", density);
            setObjVar(self, path + ".sampling_interval", samplingInterval);
            setObjVar(self, path + ".sampling_increase", samplingIncrease);
            setObjVar(self, path + ".falleens_fist", falleensFist);
            setObjVar(self, path + ".started_at", getCalendarTime());
            setObjVar(self, path + ".started_game_time", getGameTime());
            setObjVar(self, path + ".state", STATE_ACTIVE);
            scheduleJob(self, stationId, player, sequence);
        }
        sendJobState(self, path, player);
        return SCRIPT_CONTINUE;
    }

    public int cancelPlanetaryMiningDroidReservation(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = getRequestPlayer(params);
        int sequence = params == null ? 0 : params.getInt("jobSequence");
        int stationId = getStationId(player);
        String path = getJobPath(stationId, player);
        if (stationId > 0 && sequence > 0 && getIntObjVar(self, path + ".sequence") == sequence && getIntObjVar(self, path + ".state") == STATE_RESERVED)
        {
            removeObjVar(self, path);
        }
        return SCRIPT_CONTINUE;
    }

    public int syncPlanetaryMiningDroidJob(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = getRequestPlayer(params);
        int stationId = getStationId(player);
        String path = getJobPath(stationId, player);
        if (stationId < 1 || !hasObjVar(self, path))
        {
            sendNoJob(self, player, 0);
            return SCRIPT_CONTINUE;
        }
        int state = getIntObjVar(self, path + ".state");
        int sequence = getIntObjVar(self, path + ".sequence");
        if (state == STATE_RESERVED)
        {
            sendReservationResponse(self, player, sequence, true, "");
        }
        else if (state == STATE_ACTIVE && isJobDue(self, path))
        {
            beginDelivery(self, path, player, sequence);
        }
        else
        {
            sendJobState(self, path, player);
        }
        return SCRIPT_CONTINUE;
    }

    public int returnPlanetaryMiningDroidJob(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = getRequestPlayer(params);
        int sequence = params == null ? 0 : params.getInt("jobSequence");
        int stationId = getStationId(player);
        String path = getJobPath(stationId, player);
        if (stationId < 1 || sequence < 1 || getIntObjVar(self, path + ".sequence") != sequence)
        {
            if (stationId > 0 && hasObjVar(self, path))
            {
                sendJobState(self, path, player);
            }
            else
            {
                sendNoJob(self, player, sequence);
            }
            return SCRIPT_CONTINUE;
        }
        int state = getIntObjVar(self, path + ".state");
        if (state == STATE_ACTIVE)
        {
            beginDelivery(self, path, player, sequence);
        }
        else if (state == STATE_DELIVERING)
        {
            sendDelivery(self, path, player, sequence);
        }
        return SCRIPT_CONTINUE;
    }

    public int acknowledgePlanetaryMiningDroidDelivery(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = getRequestPlayer(params);
        int sequence = params == null ? 0 : params.getInt("jobSequence");
        int stationId = getStationId(player);
        String path = getJobPath(stationId, player);
        if (stationId > 0 && sequence > 0 && getIntObjVar(self, path + ".sequence") == sequence && getIntObjVar(self, path + ".state") == STATE_DELIVERING)
        {
            removeObjVar(self, path);
        }
        return SCRIPT_CONTINUE;
    }

    public int handlePlanetaryMiningDroidDue(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null)
        {
            return SCRIPT_CONTINUE;
        }
        int stationId = params.getInt("stationId");
        obj_id player = params.getObjId("player");
        int sequence = params.getInt("jobSequence");
        String path = getJobPath(stationId, player);
        if (sequence > 0 && getIntObjVar(self, path + ".sequence") == sequence && getIntObjVar(self, path + ".state") == STATE_ACTIVE)
        {
            if (isJobDue(self, path))
            {
                beginDelivery(self, path, player, sequence);
            }
            else
            {
                scheduleJob(self, stationId, player, sequence);
            }
        }
        return SCRIPT_CONTINUE;
    }

    public boolean isCommitValid(dictionary params) throws InterruptedException
    {
        if (params == null)
        {
            return false;
        }
        location launchLocation = params.getLocation("launchLocation");
        if (launchLocation == null || launchLocation.area == null || isIdValid(launchLocation.cell) || !resource.isPlanetaryMiningSceneSupported(launchLocation.area))
        {
            return false;
        }
        obj_id resourceType = params.getObjId("resource");
        String resourceClass = params.getString("resourceClass");
        int duration = params.getInt("duration");
        int surveying = params.getInt("surveying");
        float density = params.getFloat("density");
        int samplingInterval = params.getInt("samplingInterval");
        int samplingIncrease = params.getInt("samplingIncrease");
        int falleensFist = params.getInt("falleensFist");
        if (!isIdValid(resourceType) || resourceClass == null || resourceClass.equals("") || duration < 72000 || duration > 86400 || surveying < 0 || surveying > 1000 || density <= 0.0f || density > 1.0f || samplingInterval < 1 || samplingInterval > 3600 || samplingIncrease < 0 || samplingIncrease > 1000 || (falleensFist != 0 && falleensFist != 1))
        {
            return false;
        }
        String typeClass = getResourceClass(resourceType);
        String requiredSuffix = launchLocation.area.startsWith("kashyyyk_") ? "_kashyyyk" : "_mustafar";
        if (typeClass == null || !typeClass.contains(requiredSuffix))
        {
            return false;
        }
        resource_density[] available = requestResourceList(launchLocation, 0.0f, 1.0f, resourceClass);
        boolean found = false;
        if (available != null)
        {
            for (resource_density availableResource : available)
            {
                obj_id availableType = availableResource.getResourceType();
                if (resourceType.equals(availableType))
                {
                    found = true;
                    break;
                }
            }
        }
        return found && resource.getPlanetaryMiningAmount(density, surveying, duration, samplingInterval, samplingIncrease, falleensFist != 0) > 0;
    }

    public void scheduleJob(obj_id self, int stationId, obj_id player, int sequence) throws InterruptedException
    {
        String path = getJobPath(stationId, player);
        int remaining = Math.max(0, getIntObjVar(self, path + ".started_at") + getIntObjVar(self, path + ".duration") - getCalendarTime());
        dictionary due = new dictionary();
        due.put("stationId", stationId);
        due.put("player", player);
        due.put("jobSequence", sequence);
        messageTo(self, "handlePlanetaryMiningDroidDue", due, remaining, true);
    }

    public boolean isJobDue(obj_id self, String path) throws InterruptedException
    {
        return getIntObjVar(self, path + ".started_at") + getIntObjVar(self, path + ".duration") <= getCalendarTime();
    }

    public void beginDelivery(obj_id self, String path, obj_id player, int sequence) throws InterruptedException
    {
        int amount = getProjectedAmount(self, path);
        if (amount < 0)
        {
            amount = 0;
        }
        setObjVar(self, path + ".completion_amount", amount);
        setObjVar(self, path + ".state", STATE_DELIVERING);
        sendDelivery(self, path, player, sequence);
    }

    public int getProjectedAmount(obj_id self, String path) throws InterruptedException
    {
        int startedAt = getIntObjVar(self, path + ".started_at");
        int duration = getIntObjVar(self, path + ".duration");
        obj_id resourceType = getObjIdObjVar(self, path + ".resource");
        if (startedAt < 1 || duration < 1 || !isIdValid(resourceType))
        {
            return -1;
        }
        int elapsed = Math.max(0, Math.min(duration, getCalendarTime() - startedAt));
        int startedGameTime = getIntObjVar(self, path + ".started_game_time");
        int depletionTime = getResourceDepletionTime(resourceType);
        if (startedGameTime > 0 && depletionTime >= 0)
        {
            elapsed = Math.min(elapsed, Math.max(0, depletionTime - startedGameTime));
        }
        return resource.getPlanetaryMiningAmount(getFloatObjVar(self, path + ".density"), getIntObjVar(self, path + ".surveying"), elapsed, getIntObjVar(self, path + ".sampling_interval"), getIntObjVar(self, path + ".sampling_increase"), getIntObjVar(self, path + ".falleens_fist") != 0);
    }

    public void sendReservationResponse(obj_id self, obj_id player, int sequence, boolean success, String reason) throws InterruptedException
    {
        if (!isIdValid(player))
        {
            return;
        }
        dictionary response = new dictionary();
        response.put("jobSequence", sequence);
        response.put("success", success);
        response.put("reason", reason);
        messageTo(player, "handlePlanetaryMiningDroidReservationResponse", response, 0.0f, true);
    }

    public void sendJobState(obj_id self, String path, obj_id player) throws InterruptedException
    {
        int state = getIntObjVar(self, path + ".state");
        if (state == STATE_DELIVERING)
        {
            sendDelivery(self, path, player, getIntObjVar(self, path + ".sequence"));
            return;
        }
        dictionary response = new dictionary();
        response.put("state", state);
        response.put("jobSequence", getIntObjVar(self, path + ".sequence"));
        response.put("resource", getObjIdObjVar(self, path + ".resource"));
        response.put("amount", getIntObjVar(self, path + ".amount"));
        response.put("startedAt", getIntObjVar(self, path + ".started_at"));
        response.put("startedGameTime", getIntObjVar(self, path + ".started_game_time"));
        response.put("duration", getIntObjVar(self, path + ".duration"));
        response.put("surveying", getIntObjVar(self, path + ".surveying"));
        response.put("density", getFloatObjVar(self, path + ".density"));
        response.put("samplingInterval", getIntObjVar(self, path + ".sampling_interval"));
        response.put("samplingIncrease", getIntObjVar(self, path + ".sampling_increase"));
        response.put("falleensFist", getIntObjVar(self, path + ".falleens_fist"));
        messageTo(player, "handlePlanetaryMiningDroidSync", response, 0.0f, true);
    }

    public void sendDelivery(obj_id self, String path, obj_id player, int sequence) throws InterruptedException
    {
        dictionary delivery = new dictionary();
        delivery.put("jobSequence", sequence);
        delivery.put("resource", getObjIdObjVar(self, path + ".resource"));
        delivery.put("amount", getIntObjVar(self, path + ".completion_amount"));
        messageTo(player, "handlePlanetaryMiningDroidReturn", delivery, 0.0f, true);
    }

    public void sendNoJob(obj_id self, obj_id player, int sequence) throws InterruptedException
    {
        if (!isIdValid(player))
        {
            return;
        }
        dictionary response = new dictionary();
        response.put("state", 0);
        response.put("jobSequence", sequence);
        messageTo(player, "handlePlanetaryMiningDroidSync", response, 0.0f, true);
    }

    public obj_id getRequestPlayer(dictionary params) throws InterruptedException
    {
        return params == null ? obj_id.NULL_ID : params.getObjId("player");
    }

    public int getStationId(obj_id player) throws InterruptedException
    {
        return isIdValid(player) ? getPlayerStationId(player) : 0;
    }

    public int getAccountJobCount(obj_id self, int stationId) throws InterruptedException
    {
        obj_var_list jobs = getObjVarList(self, VAR_JOBS + "." + stationId);
        return jobs == null ? 0 : jobs.getNumItems();
    }

    public String getJobPath(int stationId, obj_id player) throws InterruptedException
    {
        return VAR_JOBS + "." + stationId + "." + (isIdValid(player) ? Long.toString(player.getValue()) : "0");
    }

}
