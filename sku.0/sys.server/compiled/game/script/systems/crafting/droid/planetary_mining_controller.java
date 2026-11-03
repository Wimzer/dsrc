package script.systems.crafting.droid;

import script.*;
import script.library.*;

public class planetary_mining_controller extends script.base_script
{
    public static final String VAR_JOBS = "planetary_mining.jobs";
    public static final int STATE_RESERVED = 1;
    public static final int STATE_ACTIVE = 2;
    public static final int STATE_DELIVERING = 3;
    public static final int DEFAULT_SHARED_ACTIVITY_CAPACITY = 3;
    public static final int SAMPLE_AUTHORIZATION_START = 1;
    public static final int SAMPLING_CLAIM_ACTIVE = 1;
    public static final int STOP_KIND_NATURAL = 1;
    public static final int STOP_KIND_EARLY = 2;
    public static final String CONFIG_SHARED_ACTIVITY_CAPACITY = "planetaryMiningSharedActivityCapacity";
    public static final String VAR_CAPACITY = "planetary_mining.capacity";
    public static final String VAR_SAMPLING_CLAIMS = "planetary_mining.sampling_claims";
    public static final String VAR_SAMPLING_FENCE = "planetary_mining.sampling_fence";
    public static final String SCRIPTVAR_ACCOUNT_SAMPLING_SUBSCRIBERS = "planetary_mining.account_sampling_subscribers";

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
        recoverSamplingFences(self);
        recoverSamplingClaims(self);
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
            reconcileOverdueStops(self, stationId);
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
            capacityChanged(self, stationId);
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
            int state = getIntObjVar(self, path + ".state");
            if (existingSequence == sequence && state == STATE_RESERVED)
            {
                sendReservationResponse(self, player, sequence, true, "");
                return SCRIPT_CONTINUE;
            }
            if (state == STATE_ACTIVE || state == STATE_DELIVERING)
            {
                if (existingSequence == sequence)
                {
                    sendJobState(self, path, player);
                }
                else
                {
                    sendReservationResponse(self, player, sequence, false, "character_active");
                }
                return SCRIPT_CONTINUE;
            }
            // Finish recovery of an interrupted/incomplete reservation write.
            removeObjVar(self, path);
        }
        reconcileOverdueStops(self, stationId);
        if (getSharedActivityOccupancy(self, stationId) >= getSharedActivityCapacity())
        {
            sendReservationResponse(self, player, sequence, false, "shared_capacity_full");
            return SCRIPT_CONTINUE;
        }
        setObjVar(self, path + ".sequence", sequence);
        setObjVar(self, path + ".state", STATE_RESERVED);
        capacityChanged(self, stationId);
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
                capacityChanged(self, stationId);
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
            reconcileOverdueStops(self, stationId);
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
        }
        // Replay must repair scheduling and notification after an interrupted
        // ACTIVE or capacity-revision write without restarting the expedition.
        if (getIntObjVar(self, path + ".state") == STATE_ACTIVE)
        {
            scheduleJob(self, stationId, player, sequence);
            capacityChanged(self, stationId);
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
            capacityChanged(self, stationId);
        }
        return SCRIPT_CONTINUE;
    }

    public int syncPlanetaryMiningDroidJob(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = getRequestPlayer(params);
        int requestedSequence = params == null ? 0 : params.getInt("jobSequence");
        int stationId = getStationId(player);
        String path = getJobPath(stationId, player);
        if (stationId < 1 || !hasObjVar(self, path))
        {
            sendNoJob(self, player, requestedSequence);
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
            beginDelivery(self, stationId, path, player, sequence, STOP_KIND_NATURAL);
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
            beginDelivery(self, stationId, path, player, sequence, STOP_KIND_EARLY);
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
                beginDelivery(self, stationId, path, player, sequence, STOP_KIND_NATURAL);
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

    public void beginDelivery(obj_id self, int stationId, String path, obj_id player, int sequence, int stopKind) throws InterruptedException
    {
        if (getIntObjVar(self, path + ".state") != STATE_ACTIVE)
        {
            return;
        }
        int amount = getProjectedAmount(self, path);
        if (amount < 0)
        {
            amount = 0;
        }
        int stopAt = stopKind == STOP_KIND_NATURAL ? getJobDueAt(self, path) : getCalendarTime();
        setObjVar(self, path + ".stop_at", stopAt);
        setObjVar(self, path + ".stop_kind", stopKind);
        setObjVar(self, path + ".completion_amount", amount);
        setObjVar(self, path + ".state", STATE_DELIVERING);
        capacityChanged(self, stationId);
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
        int stationId = getStationId(player);
        response.put("capacity", getSharedActivityCapacity());
        int occupied = stationId > 0 ? getSharedActivityOccupancy(self, stationId) : 0;
        response.put("occupied", occupied);
        response.put("full", occupied >= response.getInt("capacity"));
        response.put("capacityRevision", stationId > 0 ? getCapacityRevision(self, stationId) : 0);
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

    public void registerAccountSamplingSubscriber(obj_id self, int stationId, obj_id player) throws InterruptedException
    {
        if (stationId < 1 || !isIdValid(player))
        {
            return;
        }
        String path = getAccountSamplingSubscriberPath(stationId);
        obj_id[] subscribers = utils.hasScriptVar(self, path) ? utils.getObjIdArrayScriptVar(self, path) : null;
        if (subscribers == null)
        {
            utils.setScriptVar(self, path, new obj_id[] { player });
            return;
        }
        for (obj_id subscriber : subscribers)
        {
            if (player.equals(subscriber))
            {
                return;
            }
        }
        obj_id[] updated = new obj_id[subscribers.length + 1];
        for (int index = 0; index < subscribers.length; ++index)
        {
            updated[index] = subscribers[index];
        }
        updated[subscribers.length] = player;
        utils.setScriptVar(self, path, updated);
    }

    public void notifyAccountSamplingSubscribers(obj_id self, int stationId) throws InterruptedException
    {
        String path = getAccountSamplingSubscriberPath(stationId);
        if (!utils.hasScriptVar(self, path))
        {
            return;
        }
        obj_id[] subscribers = utils.getObjIdArrayScriptVar(self, path);
        if (subscribers == null)
        {
            return;
        }
        for (obj_id subscriber : subscribers)
        {
            sendCapacityState(self, subscriber, stationId);
        }
    }

    public void reconcileOverdueStops(obj_id self, int stationId) throws InterruptedException
    {
        obj_var_list characters = getObjVarList(self, VAR_JOBS + "." + stationId);
        if (characters == null)
        {
            return;
        }
        int now = getCalendarTime();
        for (int index = 0; index < characters.getNumItems(); ++index)
        {
            obj_var characterVar = characters.getObjVar(index);
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
            String jobPath = getJobPath(stationId, player);
            int sequence = getIntObjVar(self, jobPath + ".sequence");
            if (sequence > 0 && getIntObjVar(self, jobPath + ".state") == STATE_ACTIVE && getJobDueAt(self, jobPath) <= now)
            {
                beginDelivery(self, stationId, jobPath, player, sequence, STOP_KIND_NATURAL);
            }
            // Delivering jobs intentionally consume no shared-capacity slot.
        }
    }

    public int getJobDueAt(obj_id self, String path) throws InterruptedException
    {
        return getIntObjVar(self, path + ".started_at") + getIntObjVar(self, path + ".duration");
    }

    public String getAccountSamplingSubscriberPath(int stationId) throws InterruptedException
    {
        return SCRIPTVAR_ACCOUNT_SAMPLING_SUBSCRIBERS + "." + stationId;
    }

    public obj_id getRequestPlayer(dictionary params) throws InterruptedException
    {
        return params == null ? obj_id.NULL_ID : params.getObjId("player");
    }

    public int getStationId(obj_id player) throws InterruptedException
    {
        return isIdValid(player) ? getPlayerStationId(player) : 0;
    }

    public String getJobPath(int stationId, obj_id player) throws InterruptedException
    {
        return VAR_JOBS + "." + stationId + "." + (isIdValid(player) ? Long.toString(player.getValue()) : "0");
    }

    public int getSharedActivityCapacity() throws InterruptedException
    {
        int configured = utils.getIntConfigSetting("GameServer", CONFIG_SHARED_ACTIVITY_CAPACITY);
        return configured > 0 ? configured : DEFAULT_SHARED_ACTIVITY_CAPACITY;
    }

    public int getSharedActivityOccupancy(obj_id self, int stationId) throws InterruptedException
    {
        int occupied = 0;
        obj_var_list jobs = getObjVarList(self, VAR_JOBS + "." + stationId);
        if (jobs != null)
        {
            for (int index = 0; index < jobs.getNumItems(); ++index)
            {
                obj_var entry = jobs.getObjVar(index);
                if (entry == null)
                {
                    continue;
                }
                int state = getIntObjVar(self, VAR_JOBS + "." + stationId + "." + entry.getName() + ".state");
                if (state == STATE_RESERVED || state == STATE_ACTIVE)
                {
                    ++occupied;
                }
            }
        }
        obj_var_list claims = getObjVarList(self, VAR_SAMPLING_CLAIMS + "." + stationId);
        if (claims != null)
        {
            for (int index = 0; index < claims.getNumItems(); ++index)
            {
                obj_var entry = claims.getObjVar(index);
                if (entry != null && getIntObjVar(self, VAR_SAMPLING_CLAIMS + "." + stationId + "." + entry.getName() + ".state") == SAMPLING_CLAIM_ACTIVE)
                {
                    ++occupied;
                }
            }
        }
        return occupied;
    }

    public int getCapacityRevision(obj_id self, int stationId) throws InterruptedException
    {
        return stationId > 0 ? getIntObjVar(self, VAR_CAPACITY + "." + stationId + ".revision") : 0;
    }

    public int getNextPmdCompletionAt(obj_id self, int stationId) throws InterruptedException
    {
        obj_var_list jobs = stationId > 0 ? getObjVarList(self, VAR_JOBS + "." + stationId) : null;
        int firstDue = 0;
        if (jobs != null)
        {
            for (int index = 0; index < jobs.getNumItems(); ++index)
            {
                obj_var entry = jobs.getObjVar(index);
                if (entry == null)
                {
                    continue;
                }
                String path = VAR_JOBS + "." + stationId + "." + entry.getName();
                if (getIntObjVar(self, path + ".state") != STATE_ACTIVE || getIntObjVar(self, path + ".started_at") < 1 || getIntObjVar(self, path + ".duration") < 1)
                {
                    continue;
                }
                int dueAt = getJobDueAt(self, path);
                if (dueAt > 0 && (firstDue == 0 || dueAt < firstDue))
                {
                    firstDue = dueAt;
                }
            }
        }
        return firstDue;
    }

    public void reconcileCapacityConfiguration(obj_id self, int stationId) throws InterruptedException
    {
        if (stationId < 1)
        {
            return;
        }
        String path = VAR_CAPACITY + "." + stationId;
        int capacity = getSharedActivityCapacity();
        if (!hasObjVar(self, path + ".configured") || getIntObjVar(self, path + ".configured") != capacity)
        {
            setObjVar(self, path + ".configured", capacity);
            int revision = getIntObjVar(self, path + ".revision");
            setObjVar(self, path + ".revision", revision < 1 || revision == Integer.MAX_VALUE ? 1 : revision + 1);
        }
    }

    public void capacityChanged(obj_id self, int stationId) throws InterruptedException
    {
        if (stationId < 1)
        {
            return;
        }
        String path = VAR_CAPACITY + "." + stationId + ".revision";
        int revision = getIntObjVar(self, path);
        setObjVar(self, path, revision < 1 || revision == Integer.MAX_VALUE ? 1 : revision + 1);
        notifyAccountSamplingSubscribers(self, stationId);
    }

    public void sendCapacityState(obj_id self, obj_id player, int stationId) throws InterruptedException
    {
        if (!isIdValid(player) || stationId < 1)
        {
            return;
        }
        reconcileCapacityConfiguration(self, stationId);
        int capacity = getSharedActivityCapacity();
        int occupied = getSharedActivityOccupancy(self, stationId);
        dictionary state = new dictionary();
        state.put("capacity", capacity);
        state.put("occupied", occupied);
        state.put("full", occupied >= capacity);
        state.put("capacityRevision", getCapacityRevision(self, stationId));
        state.put("nextPmdCompletionAt", getNextPmdCompletionAt(self, stationId));
        messageTo(player, "handlePlanetaryMiningAccountSamplingState", state, 0.0f, false);
    }

    public void recoverSamplingClaims(obj_id self) throws InterruptedException
    {
        obj_var_list accounts = getObjVarList(self, VAR_SAMPLING_CLAIMS);
        if (accounts == null)
        {
            return;
        }
        for (int accountIndex = 0; accountIndex < accounts.getNumItems(); ++accountIndex)
        {
            obj_var account = accounts.getObjVar(accountIndex);
            if (account == null)
            {
                continue;
            }
            int stationId;
            try
            {
                stationId = Integer.parseInt(account.getName());
            }
            catch (NumberFormatException exception)
            {
                continue;
            }
            obj_var_list claims = accounts.getObjVarList(account.getName());
            if (claims == null)
            {
                continue;
            }
            for (int claimIndex = 0; claimIndex < claims.getNumItems(); ++claimIndex)
            {
                obj_var claim = claims.getObjVar(claimIndex);
                if (claim == null)
                {
                    continue;
                }
                obj_id player;
                try
                {
                    player = obj_id.getObjId(Long.parseLong(claim.getName()));
                }
                catch (NumberFormatException exception)
                {
                    continue;
                }
                String path = getSamplingClaimPath(stationId, player);
                if (getIntObjVar(self, path + ".state") != SAMPLING_CLAIM_ACTIVE || !isCompleteSamplingClaim(self, path) || isClaimAtOrBelowFence(self, stationId, player, path))
                {
                    removeObjVar(self, path);
                    continue;
                }
            }
            capacityChanged(self, stationId);
        }
    }

    public boolean isCompleteSamplingClaim(obj_id self, String path) throws InterruptedException
    {
        return isIdValid(getObjIdObjVar(self, path + ".player")) && isIdValid(getObjIdObjVar(self, path + ".tool")) && getIntObjVar(self, path + ".sessionEpoch") > 0 && getIntObjVar(self, path + ".sessionSequence") > 0 && getIntObjVar(self, path + ".claimSequence") > 0;
    }

    public void recoverSamplingFences(obj_id self) throws InterruptedException
    {
        obj_var_list accounts = getObjVarList(self, VAR_SAMPLING_FENCE);
        if (accounts == null)
        {
            return;
        }
        for (int accountIndex = 0; accountIndex < accounts.getNumItems(); ++accountIndex)
        {
            obj_var account = accounts.getObjVar(accountIndex);
            if (account == null)
            {
                continue;
            }
            int stationId;
            try
            {
                stationId = Integer.parseInt(account.getName());
            }
            catch (NumberFormatException exception)
            {
                continue;
            }
            obj_var_list fences = accounts.getObjVarList(account.getName());
            boolean committedFence = false;
            if (fences != null)
            {
                for (int fenceIndex = 0; fenceIndex < fences.getNumItems(); ++fenceIndex)
                {
                    obj_var fence = fences.getObjVar(fenceIndex);
                    if (fence == null)
                    {
                        continue;
                    }
                    String fencePath = VAR_SAMPLING_FENCE + "." + stationId + "." + fence.getName();
                    recoverPendingSamplingFence(self, fencePath);
                    if (getIntObjVar(self, fencePath + ".state") != 1)
                    {
                        continue;
                    }
                    committedFence = true;
                    obj_id player;
                    try
                    {
                        player = obj_id.getObjId(Long.parseLong(fence.getName()));
                    }
                    catch (NumberFormatException exception)
                    {
                        continue;
                    }
                    String claimPath = getSamplingClaimPath(stationId, player);
                    if (getIntObjVar(self, claimPath + ".state") == SAMPLING_CLAIM_ACTIVE && isClaimAtOrBelowFence(self, stationId, player, claimPath))
                    {
                        removeObjVar(self, claimPath);
                    }
                }
            }
            if (committedFence)
            {
                capacityChanged(self, stationId);
            }
        }
    }

    public void recoverPendingSamplingFence(obj_id self, String fencePath) throws InterruptedException
    {
        String pending = getStringObjVar(self, fencePath + ".pending");
        if (pending == null || pending.equals(""))
        {
            return;
        }
        java.util.StringTokenizer tokens = new java.util.StringTokenizer(pending, ":");
        if (tokens.countTokens() != 3)
        {
            return;
        }
        int sessionEpoch;
        int sessionSequence;
        int claimSequence;
        try
        {
            sessionEpoch = Integer.parseInt(tokens.nextToken());
            sessionSequence = Integer.parseInt(tokens.nextToken());
            claimSequence = Integer.parseInt(tokens.nextToken());
        }
        catch (NumberFormatException exception)
        {
            return;
        }
        if (sessionEpoch < 1 || sessionSequence < 1 || claimSequence < 1)
        {
            return;
        }
        setObjVar(self, fencePath + ".sessionEpoch", sessionEpoch);
        setObjVar(self, fencePath + ".sessionSequence", sessionSequence);
        setObjVar(self, fencePath + ".claimSequence", claimSequence);
        setObjVar(self, fencePath + ".state", 1);
        removeObjVar(self, fencePath + ".pending");
    }

    public int syncPlanetaryMiningSharedCapacity(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = getRequestPlayer(params);
        int stationId = getStationId(player);
        if (stationId > 0)
        {
            registerAccountSamplingSubscriber(self, stationId, player);
            reconcileOverdueStops(self, stationId);
            sendCapacityState(self, player, stationId);
        }
        return SCRIPT_CONTINUE;
    }

    public int requestPlanetaryMiningSamplingClaim(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = getRequestPlayer(params);
        obj_id tool = params == null ? obj_id.NULL_ID : params.getObjId("tool");
        int sessionEpoch = params == null ? 0 : params.getInt("sessionEpoch");
        int sessionSequence = params == null ? 0 : params.getInt("sessionSequence");
        int claimSequence = params == null ? 0 : params.getInt("claimSequence");
        int requestSequence = params == null ? 0 : params.getInt("requestSequence");
        int kind = params == null ? 0 : params.getInt("kind");
        // Protocol v2 is event driven.  Old per-iteration/event/heartbeat
        // requests may still be queued across a script reload, but they must
        // not renew, close, or recreate a claim.
        if (kind != SAMPLE_AUTHORIZATION_START)
        {
            return SCRIPT_CONTINUE;
        }
        int stationId = getStationId(player);
        boolean valid = isIdValid(player) && isIdValid(tool) && stationId > 0 && sessionEpoch > 0 && sessionSequence > 0 && claimSequence > 0 && requestSequence > 0;
        boolean allowed = false;
        String reason = "invalid_request";
        if (valid)
        {
            registerAccountSamplingSubscriber(self, stationId, player);
            reconcileOverdueStops(self, stationId);
            String path = getSamplingClaimPath(stationId, player);
            boolean active = getIntObjVar(self, path + ".state") == SAMPLING_CLAIM_ACTIVE;
            boolean exact = active && isExactSamplingClaim(self, path, player, tool, sessionEpoch, sessionSequence, claimSequence);
            if (kind == SAMPLE_AUTHORIZATION_START)
            {
                if (exact)
                {
                    allowed = true;
                    reason = "";
                }
                else if (isIdentityAtOrBelowFence(self, stationId, player, sessionEpoch, sessionSequence, claimSequence))
                {
                    reason = "closed_claim";
                }
                else if (active)
                {
                    // A newer identity is not evidence that the old live claim
                    // ended.  Only its exact END may make this slot reusable.
                    reason = "claim_active";
                }
                else if (getSharedActivityOccupancy(self, stationId) >= getSharedActivityCapacity())
                {
                    reason = "shared_capacity_full";
                }
                else
                {
                    commitSamplingClaim(self, stationId, player, tool, sessionEpoch, sessionSequence, claimSequence);
                    allowed = true;
                    reason = "";
                }
            }
        }
        sendSamplingClaimResponse(self, player, tool, sessionEpoch, sessionSequence, claimSequence, requestSequence, kind, stationId, allowed, reason, params);
        return SCRIPT_CONTINUE;
    }

    public void commitSamplingClaim(obj_id self, int stationId, obj_id player, obj_id tool, int sessionEpoch, int sessionSequence, int claimSequence) throws InterruptedException
    {
        String path = getSamplingClaimPath(stationId, player);
        removeObjVar(self, path);
        setObjVar(self, path + ".player", player);
        setObjVar(self, path + ".tool", tool);
        setObjVar(self, path + ".sessionEpoch", sessionEpoch);
        setObjVar(self, path + ".sessionSequence", sessionSequence);
        setObjVar(self, path + ".claimSequence", claimSequence);
        setObjVar(self, path + ".acquiredAt", getCalendarTime());
        setObjVar(self, path + ".state", SAMPLING_CLAIM_ACTIVE);
        capacityChanged(self, stationId);
    }

    public void sendSamplingClaimResponse(obj_id self, obj_id player, obj_id tool, int sessionEpoch, int sessionSequence, int claimSequence, int requestSequence, int kind, int stationId, boolean allowed, String reason, dictionary request) throws InterruptedException
    {
        if (!isIdValid(player))
        {
            return;
        }
        reconcileCapacityConfiguration(self, stationId);
        int capacity = getSharedActivityCapacity();
        int occupied = stationId > 0 ? getSharedActivityOccupancy(self, stationId) : 0;
        dictionary response = new dictionary();
        response.put("player", player);
        response.put("tool", tool);
        response.put("sessionEpoch", sessionEpoch);
        response.put("sessionSequence", sessionSequence);
        response.put("claimSequence", claimSequence);
        response.put("requestSequence", requestSequence);
        response.put("kind", kind);
        response.put("eventSequence", request == null ? 0 : request.getInt("eventSequence"));
        response.put("pageId", request == null ? 0 : request.getInt("pageId"));
        response.put("eventChoice", request == null ? 0 : request.getInt("eventChoice"));
        response.put("allowed", allowed);
        response.put("reason", reason);
        response.put("capacity", capacity);
        response.put("occupied", occupied);
        response.put("full", occupied >= capacity);
        response.put("capacityRevision", stationId > 0 ? getCapacityRevision(self, stationId) : 0);
        response.put("nextPmdCompletionAt", getNextPmdCompletionAt(self, stationId));
        response.put("controllerIssuedAt", getCalendarTime());
        messageTo(player, "handlePlanetaryMiningSamplingAuthorization", response, 0.0f, false);
    }

    public int releasePlanetaryMiningSamplingClaim(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = getRequestPlayer(params);
        obj_id tool = params == null ? obj_id.NULL_ID : params.getObjId("tool");
        int sessionEpoch = params == null ? 0 : params.getInt("sessionEpoch");
        int sessionSequence = params == null ? 0 : params.getInt("sessionSequence");
        int claimSequence = params == null ? 0 : params.getInt("claimSequence");
        int stationId = getStationId(player);
        if (stationId < 1 || sessionEpoch < 1 || sessionSequence < 1 || claimSequence < 1)
        {
            return SCRIPT_CONTINUE;
        }
        String path = getSamplingClaimPath(stationId, player);
        if (getIntObjVar(self, path + ".state") == SAMPLING_CLAIM_ACTIVE && isExactSamplingClaim(self, path, player, tool, sessionEpoch, sessionSequence, claimSequence))
        {
            closeSamplingClaim(self, stationId, player, path);
        }
        else if (getIntObjVar(self, path + ".state") != SAMPLING_CLAIM_ACTIVE)
        {
            // A guaranteed release may overtake its non-guaranteed START request.
            // Fence the unseen exact identity so the delayed START cannot resurrect it.
            commitSamplingFence(self, stationId, player, sessionEpoch, sessionSequence, claimSequence);
        }
        sendSamplingReleaseAcknowledgement(player, tool, sessionEpoch, sessionSequence, claimSequence);
        return SCRIPT_CONTINUE;
    }

    public void sendSamplingReleaseAcknowledgement(obj_id player, obj_id tool, int sessionEpoch, int sessionSequence, int claimSequence) throws InterruptedException
    {
        if (!isIdValid(player))
        {
            return;
        }
        dictionary acknowledgement = new dictionary();
        acknowledgement.put("player", player);
        acknowledgement.put("tool", tool);
        acknowledgement.put("sessionEpoch", sessionEpoch);
        acknowledgement.put("sessionSequence", sessionSequence);
        acknowledgement.put("claimSequence", claimSequence);
        messageTo(player, "handlePlanetaryMiningSamplingReleaseAcknowledged", acknowledgement, 0.0f, false);
    }

    public void closeSamplingClaim(obj_id self, int stationId, obj_id player, String path) throws InterruptedException
    {
        closeSamplingClaim(self, stationId, player, path, true);
    }

    public void closeSamplingClaim(obj_id self, int stationId, obj_id player, String path, boolean publish) throws InterruptedException
    {
        if (getIntObjVar(self, path + ".state") != SAMPLING_CLAIM_ACTIVE)
        {
            return;
        }
        commitSamplingFence(self, stationId, player, getIntObjVar(self, path + ".sessionEpoch"), getIntObjVar(self, path + ".sessionSequence"), getIntObjVar(self, path + ".claimSequence"));
        removeObjVar(self, path);
        if (publish)
        {
            capacityChanged(self, stationId);
        }
    }

    public void commitSamplingFence(obj_id self, int stationId, obj_id player, int sessionEpoch, int sessionSequence, int claimSequence) throws InterruptedException
    {
        if (stationId < 1 || !isIdValid(player) || sessionEpoch < 1 || sessionSequence < 1 || claimSequence < 1 || isIdentityAtOrBelowFence(self, stationId, player, sessionEpoch, sessionSequence, claimSequence))
        {
            return;
        }
        String fence = getSamplingFencePath(stationId, player);
        // One atomic pending value lets restart recovery finish any interrupted
        // multi-objvar fence write before accepting a delayed START.
        setObjVar(self, fence + ".pending", sessionEpoch + ":" + sessionSequence + ":" + claimSequence);
        setObjVar(self, fence + ".sessionEpoch", sessionEpoch);
        setObjVar(self, fence + ".sessionSequence", sessionSequence);
        setObjVar(self, fence + ".claimSequence", claimSequence);
        // This marker commits the tombstone before an active marker is removed.
        setObjVar(self, fence + ".state", 1);
        removeObjVar(self, fence + ".pending");
    }

    public int handlePlanetaryMiningSamplingProbeDue(obj_id self, dictionary params) throws InterruptedException
    {
        return SCRIPT_CONTINUE;
    }

    public int handlePlanetaryMiningSamplingProbeUndelivered(obj_id self, dictionary params) throws InterruptedException
    {
        return SCRIPT_CONTINUE;
    }

    public int handlePlanetaryMiningSamplingProbeRetry(obj_id self, dictionary params) throws InterruptedException
    {
        return SCRIPT_CONTINUE;
    }

    public boolean isExactSamplingClaim(obj_id self, String path, obj_id player, obj_id tool, int sessionEpoch, int sessionSequence, int claimSequence) throws InterruptedException
    {
        return player.equals(getObjIdObjVar(self, path + ".player")) && tool.equals(getObjIdObjVar(self, path + ".tool")) && sessionEpoch == getIntObjVar(self, path + ".sessionEpoch") && sessionSequence == getIntObjVar(self, path + ".sessionSequence") && claimSequence == getIntObjVar(self, path + ".claimSequence");
    }

    public boolean isClaimAtOrBelowFence(obj_id self, int stationId, obj_id player, String path) throws InterruptedException
    {
        return isIdentityAtOrBelowFence(self, stationId, player, getIntObjVar(self, path + ".sessionEpoch"), getIntObjVar(self, path + ".sessionSequence"), getIntObjVar(self, path + ".claimSequence"));
    }

    public boolean isIdentityAtOrBelowFence(obj_id self, int stationId, obj_id player, int sessionEpoch, int sessionSequence, int claimSequence) throws InterruptedException
    {
        String fence = getSamplingFencePath(stationId, player);
        return getIntObjVar(self, fence + ".state") == 1 && compareSamplingIdentity(sessionEpoch, sessionSequence, claimSequence, getIntObjVar(self, fence + ".sessionEpoch"), getIntObjVar(self, fence + ".sessionSequence"), getIntObjVar(self, fence + ".claimSequence")) <= 0;
    }

    public int compareSamplingIdentity(int leftEpoch, int leftSession, int leftClaim, int rightEpoch, int rightSession, int rightClaim) throws InterruptedException
    {
        if (leftEpoch != rightEpoch)
        {
            return leftEpoch < rightEpoch ? -1 : 1;
        }
        if (leftSession != rightSession)
        {
            return leftSession < rightSession ? -1 : 1;
        }
        return leftClaim == rightClaim ? 0 : leftClaim < rightClaim ? -1 : 1;
    }

    public String getSamplingClaimPath(int stationId, obj_id player) throws InterruptedException
    {
        return VAR_SAMPLING_CLAIMS + "." + stationId + "." + (isIdValid(player) ? Long.toString(player.getValue()) : "0");
    }

    public String getSamplingFencePath(int stationId, obj_id player) throws InterruptedException
    {
        return VAR_SAMPLING_FENCE + "." + stationId + "." + (isIdValid(player) ? Long.toString(player.getValue()) : "0");
    }

}
