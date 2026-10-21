package script.npe;

import script.library.npe;
import script.library.holocron_introduction;
import script.library.utils;
import script.dictionary;
import script.obj_id;
import script.string_id;

public class npe_notify_map extends script.base_script
{
    public npe_notify_map()
    {
    }
    public static final String HOLOCRON_OFFERED = "npe.holocronHangarExit";
    public static final float HOLOCRON_DELAY = 8.0f;
    public int OnReceivedItem(obj_id self, obj_id srcContainer, obj_id transferer, obj_id item) throws InterruptedException
    {
        if (isPlayer(item))
        {
            if (!utils.hasScriptVar(item, "npe.map"))
            {
                obj_id building = getTopMostContainer(item);
                obj_id droid = utils.getObjIdScriptVar(building, "objDroidInvis");
                string_id popMap = new string_id("npe", "pop_map");
                npe.commTutorialPlayer(droid, item, 8, popMap, "sound/c3po_46.snd", "object/mobile/c_3po.iff");
                utils.setScriptVar(item, "npe.map", 1);
            }
            // Han can already have set npe.map. First corridor entry is a
            // separate opportunity, paced by the stock comm's configured interval.
            if (holocron_introduction.canRequestAutomatic(item) && getContainedBy(item) == self &&
                !utils.hasScriptVar(item, HOLOCRON_OFFERED))
            {
                obj_id station = getTopMostContainer(item);
                if (isIdValid(station) && getTopMostContainer(self) == station &&
                    holocron_introduction.STATION.equals(getTemplateName(station)))
                {
                    dictionary params = new dictionary();
                    params.put("player", item);
                    params.put("station", station);
                    utils.setScriptVar(item, HOLOCRON_OFFERED, 1);
                    if (!messageTo(self, "requestHolocron", params, HOLOCRON_DELAY, false))
                    {
                        // No callback was accepted. A later entry may offer again.
                        utils.removeScriptVar(item, HOLOCRON_OFFERED);
                    }
                }
            }
        }
        return SCRIPT_CONTINUE;
    }
    public int requestHolocron(obj_id self, dictionary params) throws InterruptedException
    {
        if (params != null && isIdValid(self) && exists(self))
        {
            obj_id station = params.getObjId("station");
            if (isIdValid(station) && getTopMostContainer(self) == station)
            {
                holocron_introduction.requestAutomatic(params.getObjId("player"), station);
            }
        }
        return SCRIPT_CONTINUE;
    }
}
