package script.npe;

import script.library.prose;
import script.library.utils;
import script.library.corpse;
import script.library.pet_lib;
import script.library.beast_lib;
import script.dictionary;
import script.obj_id;
import script.string_id;

public class npe_notify_audio extends script.base_script
{
    public npe_notify_audio()
    {
    }
    public static final String STATION = "object/building/general/npe_space_station.iff";
    // Retain the existing topic record: previously introduced characters are not
    // silently replayed just because presentation changed from a hint to a comm.
    public static final String HARVEST_SENT = "npe.audioHints.sent.harvest";
    public static final String HARVEST_PORTRAIT = "object/mobile/dressed_commoner_naboo_human_female_07.iff";
    public static final String HARVEST_SOUND = "sound/tutorial_npe2_harvest.snd";
    public static final string_id HARVEST_TEXT = new string_id("npe_atrium_tutorial", "harvesting");
    public static final float HARVEST_DISPLAY_SECONDS = 10.0f;
    public static final String TARGET_SENT = "npe.audioHints.sent.target";
    public static final String TARGET_SOUND = "sound/tutorial_npe2_target.snd";
    public static final string_id TARGET_TEXT = new string_id("npe_atrium_tutorial", "targeting");
    public static final String SENT = "npe.audioHints.sent.";
    public int OnReceivedItem(obj_id self, obj_id srcContainer, obj_id transferer, obj_id item) throws InterruptedException
    {
        if (!isIdValid(item) || !isPlayer(item))
        {
            return SCRIPT_CONTINUE;
        }
        if (STATION.equals(getTemplateName(self)))
        {
            return SCRIPT_CONTINUE;
        }
        if ("greenhouse".equals(getCellName(self)))
        {
            obj_id station = getTopMostContainer(item);
            if (!isIdValid(station) || !STATION.equals(getTemplateName(station)) ||
                getContainedBy(item) != self || hasObjVar(item, HARVEST_SENT))
            {
                return SCRIPT_CONTINUE;
            }
            // Stock comm carries portrait, voice and readable localized text to
            // this player only. No custom hint exchange or forced Holocron open.
            commPlayers(item, HARVEST_PORTRAIT, HARVEST_SOUND, HARVEST_DISPLAY_SECONDS,
                item, prose.getPackage(HARVEST_TEXT));
            setObjVar(item, HARVEST_SENT, 1);
        }
        else if (("armory".equals(getCellName(self)) || "cloninglab".equals(getCellName(self))) &&
            getContainedBy(item) == self)
        {
            sendTargetingComm(item, getTopMostContainer(self));
        }
        return SCRIPT_CONTINUE;
    }
    private boolean canSendLesson(obj_id player, obj_id station, String topic) throws InterruptedException
    {
        return isIdValid(player) && exists(player) && isPlayer(player) && isPlayerConnected(player) &&
            isIdValid(station) && exists(station) && STATION.equals(getTemplateName(station)) &&
            getTopMostContainer(player) == station && getState(player, STATE_COMBAT) == 0 &&
            !isIncapacitated(player) && !hasObjVar(player, SENT + topic);
    }
    private boolean sendLesson(obj_id player, obj_id station, String topic, String key) throws InterruptedException
    {
        if (!canSendLesson(player, station, topic))
        {
            return false;
        }
        commPlayers(player, HARVEST_PORTRAIT, "sound/tutorial_npe2_" + key + ".snd", HARVEST_DISPLAY_SECONDS,
            player, prose.getPackage(new string_id("npe_atrium_tutorial", key)));
        setObjVar(player, SENT + topic, 1);
        return true;
    }
    public int notifyLoot(obj_id self, dictionary params) throws InterruptedException
    {
        if (params != null && self == params.getObjId("station") && STATION.equals(getTemplateName(self)))
        {
            obj_id player = params.getObjId("player");
            if (canSendLesson(player, self, "loot") && hasAccessibleLoot(player, params.getObjId("target")))
            {
                sendLesson(player, self, "loot", "loot");
            }
        }
        return SCRIPT_CONTINUE;
    }
    private boolean hasAccessibleLoot(obj_id player, obj_id body) throws InterruptedException
    {
        if (!isIdValid(body) || !exists(body) || !isMob(body) || isPlayer(body) ||
            pet_lib.isPet(body) || beast_lib.isBeast(body) || !isIncapacitated(body) ||
            getTopMostContainer(body) != getTopMostContainer(player) || getContainedBy(body) != getContainedBy(player) ||
            !hasObjVar(body, "readyToLoot") || utils.getIntScriptVar(body, "timeLootable") >= getGameTime() ||
            utils.getDistance2D(player, body) > 10 || !corpse.hasLootPermissions(body, player))
        {
            return false;
        }
        if (getCashBalance(body) > 0)
        {
            return true;
        }
        obj_id inventory = utils.getInventoryContainer(body);
        obj_id[] contents = isIdValid(inventory) ? getContents(inventory) : null;
        if (contents != null)
        {
            for (obj_id item : contents)
            {
                // Inspect only; the normal loot filtering helper deletes creature weapons.
                if (isIdValid(item) && exists(item) && !utils.hasScriptVar(item, "isCreatureWeapon"))
                {
                    return true;
                }
            }
        }
        return false;
    }
    public int notifyTargeting(obj_id self, dictionary params) throws InterruptedException
    {
        if (params != null && "elevator".equals(getCellName(self)))
        {
            obj_id station = params.getObjId("station");
            if (isIdValid(station) && getTopMostContainer(self) == station)
            {
                sendTargetingComm(params.getObjId("player"), station);
            }
        }
        return SCRIPT_CONTINUE;
    }
    private void sendTargetingComm(obj_id player, obj_id station) throws InterruptedException
    {
        if (!isIdValid(player) || !exists(player) || !isPlayer(player) || !isPlayerConnected(player) ||
            !isIdValid(station) || !exists(station) || !STATION.equals(getTemplateName(station)) ||
            getTopMostContainer(player) != station || getState(player, STATE_COMBAT) != 0 ||
            hasObjVar(player, TARGET_SENT))
        {
            return;
        }
        commPlayers(player, HARVEST_PORTRAIT, TARGET_SOUND, HARVEST_DISPLAY_SECONDS,
            player, prose.getPackage(TARGET_TEXT));
        setObjVar(player, TARGET_SENT, 1);
    }
}
