package script.systems.npc_mount;

import script.*;

// Resolves spawner columns of the squad and encounter profile tables.  A world-placed
// squad writes fixed spawn-egg IDs in those columns.  A script-spawned test squad has
// runtime marker IDs instead, so its row holds BOUND and each object created by
// squad_test_spawner carries the real IDs in objvars before any squad script attaches.
public class squad_binding extends script.base_script
{
    public static final String BOUND = "bound";
    public static final String PREFIX = "npcMount.binding.";
    public static final String OBJVAR_PROFILE = PREFIX + "profile";

    public squad_binding()
    {
    }

    public static String spawner(obj_id subject, String table, int row, String column) throws InterruptedException
    {
        String value = dataTableGetString(table, row, column);
        if (!BOUND.equals(value))
        {
            return value;
        }
        String profile = dataTableGetString(table, row, "name");
        if (!isIdValid(subject) || !exists(subject) || profile == null || profile.length() == 0
                || !hasObjVar(subject, OBJVAR_PROFILE) || !profile.equals(getStringObjVar(subject, OBJVAR_PROFILE))
                || !hasObjVar(subject, PREFIX + column))
        {
            return "";
        }
        String bound = getStringObjVar(subject, PREFIX + column);
        return bound == null ? "" : bound;
    }
}
