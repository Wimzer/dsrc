package script.systems.npc_mount;

import script.*;
import script.library.ai_lib;
import script.library.create;
import script.library.utils;

// Explicit test spawn of the Mos Eisley watch squad: a mounted sergeant on a dewback
// with two walking troopers.  Attach to a god-mode player with
//   /script attach systems.npc_mount.squad_test_spawner
// then say "squad spawn" or "squad clear".  Chat handling follows the stock test
// scripts (test/qa_cube.java, test/ai_test.java).  Each actor is created the way the
// stock spawner creates one (poi/interior_spawner/interior_spawner.java spawnMob), with a
// script-created, unpersisted marker standing in for its spawn egg.  Nothing respawns.
public class squad_test_spawner extends script.base_script
{
    public static final String SCRIPT_NAME = "systems.npc_mount.squad_test_spawner";
    public static final String PROFILE = "mos_eisley_watch_test";
    public static final String MARKER_TEMPLATE = "object/tangible/poi/base/poi_base.iff";
    public static final String OBJVAR_OBJECTS = "npcMount.testSpawn.objects";
    public static final String SCENE = "tatooine";
    public static final float YAW = 90.0f;

    // Start point of the mos_eisley_perimeter route; its waypoints are offsets from the
    // leader marker.  Positions, creatures and scripts are those of the retired
    // always-on spawner rows 1095642-1095645.
    public static final float[][] POSITIONS = {
        {3420.0f, -5120.0f},
        {3422.0f, -5120.0f},
        {3414.0f, -5123.0f},
        {3414.0f, -5117.0f}
    };
    public static final String[] CREATURES = {
        "gcw_city_mos_eisley_mounted_sergeant",
        "dewback",
        "gcw_city_mos_eisley_trooper",
        "gcw_city_mos_eisley_trooper"
    };
    public static final String[] SCRIPTS = {
        "systems.npc_mount.squad_member,systems.npc_mount.encounter_setup",
        "systems.npc_mount.encounter_setup",
        "systems.npc_mount.squad_member",
        "systems.npc_mount.squad_member"
    };
    public static final int LEADER = 0;
    public static final int MOUNT = 1;
    public static final int ESCORT_1 = 2;
    public static final int ESCORT_2 = 3;

    public squad_test_spawner()
    {
    }

    public int OnAttach(obj_id self) throws InterruptedException
    {
        if (!isPlayer(self) || !isGod(self))
        {
            detachScript(self, SCRIPT_NAME);
            return SCRIPT_CONTINUE;
        }
        sendSystemMessageTestingOnly(self, "Mounted squad test: say 'squad spawn' or 'squad clear'.");
        return SCRIPT_CONTINUE;
    }

    public int OnSpeaking(obj_id self, String text) throws InterruptedException
    {
        if (!isGod(self) || text == null)
        {
            return SCRIPT_CONTINUE;
        }
        String command = toLower(text).trim();
        if (command.equals("squad spawn"))
        {
            spawnSquad(self);
            return SCRIPT_OVERRIDE;
        }
        if (command.equals("squad clear"))
        {
            int removed = clearSquad(self);
            sendSystemMessageTestingOnly(self, "Mounted squad test: removed " + removed + " objects.");
            return SCRIPT_OVERRIDE;
        }
        return SCRIPT_CONTINUE;
    }

    public void spawnSquad(obj_id self) throws InterruptedException
    {
        if (liveCount(self) > 0)
        {
            sendSystemMessageTestingOnly(self, "Mounted squad test: a squad already exists. Say 'squad clear' first.");
            return;
        }
        if (!SCENE.equals(getCurrentSceneName()))
        {
            sendSystemMessageTestingOnly(self, "Mounted squad test: go to Tatooine first.");
            return;
        }
        obj_id[] markers = new obj_id[POSITIONS.length];
        obj_id[] actors = new obj_id[POSITIONS.length];
        for (int i = 0; i < POSITIONS.length; i++)
        {
            float x = POSITIONS[i][0];
            float z = POSITIONS[i][1];
            markers[i] = createObject(MARKER_TEMPLATE, new location(x, getHeightAtLocation(x, z), z, SCENE));
            if (!isIdValid(markers[i]))
            {
                record(self, markers, actors);
                clearSquad(self);
                sendSystemMessageTestingOnly(self, "Mounted squad test: could not create a marker.");
                return;
            }
            setYaw(markers[i], YAW);
        }
        bind(markers[LEADER], markers);
        attachScript(markers[LEADER], "systems.npc_mount.squad_coordinator");
        for (int i = 0; i < POSITIONS.length; i++)
        {
            actors[i] = spawnActor(markers[i], CREATURES[i], SCRIPTS[i], markers);
            if (!isIdValid(actors[i]))
            {
                record(self, markers, actors);
                clearSquad(self);
                sendSystemMessageTestingOnly(self, "Mounted squad test: could not create " + CREATURES[i] + ".");
                return;
            }
        }
        record(self, markers, actors);
        sendSystemMessageTestingOnly(self, "Mounted squad test: squad spawned at /way tatooine 3420 -5120.");
    }

    public obj_id spawnActor(obj_id marker, String creature, String scripts, obj_id[] markers) throws InterruptedException
    {
        int level = -1;
        dictionary row = utils.dataTableGetRow(create.CREATURE_TABLE, creature);
        if (row != null)
        {
            level = row.getInt("BaseLevel");
        }
        obj_id npc = create.object(creature, getLocation(marker), level);
        if (!isIdValid(npc))
        {
            return null;
        }
        create.addDestroyMessage(npc, "npcDied", 3, marker);
        setYaw(npc, getYaw(marker));
        setObjVar(npc, "SENTINEL", 1);
        ai_lib.setDefaultCalmBehavior(npc, ai_lib.BEHAVIOR_SENTINEL);
        bind(npc, markers);
        String[] list = split(scripts, ',');
        for (String script : list)
        {
            attachScript(npc, script);
        }
        return npc;
    }

    // Written before any squad script attaches, so their first lookup sees the real IDs.
    public void bind(obj_id target, obj_id[] markers) throws InterruptedException
    {
        setObjVar(target, squad_binding.OBJVAR_PROFILE, PROFILE);
        setObjVar(target, squad_binding.PREFIX + "coordinatorSpawner", markers[LEADER].toString());
        setObjVar(target, squad_binding.PREFIX + "leaderSpawner", markers[LEADER].toString());
        setObjVar(target, squad_binding.PREFIX + "escort1Spawner", markers[ESCORT_1].toString());
        setObjVar(target, squad_binding.PREFIX + "escort2Spawner", markers[ESCORT_2].toString());
        setObjVar(target, squad_binding.PREFIX + "riderSpawner", markers[LEADER].toString());
        setObjVar(target, squad_binding.PREFIX + "mountSpawner", markers[MOUNT].toString());
    }

    public void record(obj_id self, obj_id[] markers, obj_id[] actors) throws InterruptedException
    {
        // Actors first so clearing removes riders before their markers.
        obj_id[] all = new obj_id[actors.length + markers.length];
        for (int i = 0; i < actors.length; i++)
        {
            all[i] = isIdValid(actors[i]) ? actors[i] : obj_id.NULL_ID;
        }
        for (int i = 0; i < markers.length; i++)
        {
            all[actors.length + i] = isIdValid(markers[i]) ? markers[i] : obj_id.NULL_ID;
        }
        setObjVar(self, OBJVAR_OBJECTS, all);
    }

    public int liveCount(obj_id self) throws InterruptedException
    {
        obj_id[] all = hasObjVar(self, OBJVAR_OBJECTS) ? getObjIdArrayObjVar(self, OBJVAR_OBJECTS) : null;
        int count = 0;
        if (all != null)
        {
            for (obj_id item : all)
            {
                if (isIdValid(item) && exists(item))
                {
                    count++;
                }
            }
        }
        return count;
    }

    public int clearSquad(obj_id self) throws InterruptedException
    {
        obj_id[] all = hasObjVar(self, OBJVAR_OBJECTS) ? getObjIdArrayObjVar(self, OBJVAR_OBJECTS) : null;
        int removed = 0;
        if (all != null)
        {
            for (obj_id item : all)
            {
                if (isIdValid(item) && exists(item))
                {
                    destroyObject(item);
                    removed++;
                }
            }
        }
        removeObjVar(self, OBJVAR_OBJECTS);
        return removed;
    }
}
