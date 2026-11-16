package script.systems.npc_mount;

import script.*;
import script.library.ai_lib;
import script.library.create;
import script.library.utils;

// Content bindings only. The existing rider adapter owns mounting/combat/cleanup,
// and the normal POI spawners still own actor creation and independent respawns.
public class encounter_setup extends script.base_script
{
    public static final String TABLE = "datatables/npc_mount/encounters.iff";
    public static final String RIDER_SCRIPT = "systems.npc_mount.mounted_sandtrooper";
    public static final String MOUNT_SCRIPT = "systems.npc_mount.mount_candidate";

    public encounter_setup()
    {
    }

    public int OnAttach(obj_id self) throws InterruptedException
    {
        configureActor(self);
        return SCRIPT_CONTINUE;
    }

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        configureActor(self);
        return SCRIPT_CONTINUE;
    }

    public void configurationFailure(obj_id self, String stage) throws InterruptedException
    {
        setObjVar(self, mounted_sandtrooper.OBJVAR_FAILURE_STAGE, stage);
    }

    public boolean matchesExisting(obj_id self, String key, String value) throws InterruptedException
    {
        return !hasObjVar(self, key) || value.equals(getStringObjVar(self, key));
    }

    public void configureActor(obj_id self) throws InterruptedException
    {
        if (!isIdValid(self) || !exists(self) || isPlayer(self) || isDead(self)
                || ai_lib.isIncapacitated(self) || hasObjVar(self, mounted_sandtrooper.OBJVAR_RETIRED)
                || utils.hasScriptVar(self, mounted_sandtrooper.SCRIPTVAR_CLEANUP)
                || utils.hasScriptVar(self, mounted_sandtrooper.SCRIPTVAR_CLEANUP_FINISHED))
        {
            return;
        }
        if (!dataTableOpen(TABLE))
        {
            configurationFailure(self, "configurationTable");
            return;
        }
        int selected = -1;
        int rows = dataTableGetNumRows(TABLE);
        // Resolve using existing spawn provenance, not proximity or creature name.
        // Two rows/roles claiming the same actor are an error.
        for (int row = 0; row < rows; row++)
        {
            String rider = squad_binding.spawner(self, TABLE, row, "riderSpawner");
            String mount = squad_binding.spawner(self, TABLE, row, "mountSpawner");
            if (mounted_sandtrooper.isFromSpawner(self, rider) || mounted_sandtrooper.isFromSpawner(self, mount))
            {
                if (selected != -1)
                {
                    configurationFailure(self, "configurationAmbiguous");
                    return;
                }
                selected = row;
            }
        }
        if (selected == -1)
        {
            configurationFailure(self, "configurationMissing");
            return;
        }
        String name = dataTableGetString(TABLE, selected, "name");
        String riderSpawner = squad_binding.spawner(self, TABLE, selected, "riderSpawner");
        String mountSpawner = squad_binding.spawner(self, TABLE, selected, "mountSpawner");
        String device = dataTableGetString(TABLE, selected, "controlDeviceTemplate");
        String route = dataTableGetString(TABLE, selected, "patrolRoute");
        if (name == null || name.length() == 0 || riderSpawner == null || riderSpawner.length() == 0
                || mountSpawner == null || mountSpawner.length() == 0 || riderSpawner.equals(mountSpawner)
                || device == null || device.length() == 0 || route == null)
        {
            configurationFailure(self, "configurationInvalid");
            return;
        }
        boolean rider = mounted_sandtrooper.isFromSpawner(self, riderSpawner);
        boolean mount = mounted_sandtrooper.isFromSpawner(self, mountSpawner);
        if (rider == mount || !matchesExisting(self, "npcMount.mountSpawner", mountSpawner)
                || (rider && (!matchesExisting(self, "npcMount.riderSpawner", riderSpawner)
                || !matchesExisting(self, "npcMount.controlDeviceTemplate", device)
                || !matchesExisting(self, mounted_sandtrooper.OBJVAR_PATROL_ROUTE, route))))
        {
            // Do not partially rewrite an already configured/claimed actor to a
            // different pair. Such content changes require an explicit migration.
            configurationFailure(self, "configurationConflict");
            return;
        }
        setObjVar(self, "npcMount.mountSpawner", mountSpawner);
        if (rider)
        {
            // A scoped creature-data variant can change equipment without
            // inventing a new client name or replacing a stock creature row.
            String baseCreature = getStringObjVar(self, "npcMount.baseCreature");
            if (baseCreature != null)
            {
                if (baseCreature.length() == 0 || dataTableSearchColumnForString(baseCreature, "creatureName", create.CREATURE_TABLE) < 0)
                {
                    configurationFailure(self, "configurationIdentity");
                    return;
                }
                if (!baseCreature.equals(getStringObjVar(self, "npcMount.identityBase")))
                {
                    create.randomlyNameCreature(self, baseCreature);
                    setObjVar(self, "npcMount.identityBase", baseCreature);
                }
            }
            setObjVar(self, "npcMount.riderSpawner", riderSpawner);
            setObjVar(self, "npcMount.controlDeviceTemplate", device);
            // Explicit blank means stationary idle, not an implicit route.
            setObjVar(self, mounted_sandtrooper.OBJVAR_PATROL_ROUTE, route);
        }
        String failure = getStringObjVar(self, mounted_sandtrooper.OBJVAR_FAILURE_STAGE);
        if (failure != null && failure.startsWith("configuration"))
        {
            removeObjVar(self, mounted_sandtrooper.OBJVAR_FAILURE_STAGE);
        }
        String actorScript = rider ? RIDER_SCRIPT : MOUNT_SCRIPT;
        if (!hasScript(self, actorScript))
        {
            attachScript(self, actorScript);
        }
    }
}
