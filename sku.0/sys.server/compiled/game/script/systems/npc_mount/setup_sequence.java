package script.systems.npc_mount;

import script.*;
import script.library.utils;

// Shared callback sequencing only. The rider's adapter owns eligibility,
// spawn bindings, native mounting, validation, movement and cleanup.
public class setup_sequence extends script.base_script
{
    public static final int MOUNT = 1;
    public static final int VALIDATE = 2;
    public static final String PREFIX = "npcMount.setupSequence";

    public static boolean pending(obj_id rider) throws InterruptedException
    {
        return utils.hasScriptVar(rider, PREFIX + ".phase");
    }

    public static dictionary begin(obj_id rider, obj_id mount, obj_id device, int phase) throws InterruptedException
    {
        int generation = utils.getIntScriptVar(rider, PREFIX + ".generation") + 1;
        utils.setScriptVar(rider, PREFIX + ".generation", generation);
        utils.setScriptVar(rider, PREFIX + ".phase", phase);
        utils.setScriptVar(rider, PREFIX + ".mount", mount);
        utils.setScriptVar(rider, PREFIX + ".device", device);
        dictionary token = new dictionary();
        token.put("generation", generation);
        token.put("mount", mount);
        token.put("device", device);
        return token;
    }

    public static boolean accepts(obj_id rider, dictionary token, int phase) throws InterruptedException
    {
        return token != null && pending(rider)
                && utils.getIntScriptVar(rider, PREFIX + ".phase") == phase
                && utils.getIntScriptVar(rider, PREFIX + ".generation") == token.getInt("generation")
                && utils.getObjIdScriptVar(rider, PREFIX + ".mount") == token.getObjId("mount")
                && utils.getObjIdScriptVar(rider, PREFIX + ".device") == token.getObjId("device");
    }

    public static void clear(obj_id rider) throws InterruptedException
    {
        utils.setScriptVar(rider, PREFIX + ".generation", utils.getIntScriptVar(rider, PREFIX + ".generation") + 1);
        utils.removeScriptVar(rider, PREFIX + ".phase");
        utils.removeScriptVar(rider, PREFIX + ".mount");
        utils.removeScriptVar(rider, PREFIX + ".device");
    }
}
