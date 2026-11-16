package script.systems.npc_mount;

import script.*;
import script.library.*;

public class mount_candidate extends auxiliary_support
{
    public mount_candidate()
    {
    }

    public int OnAttach(obj_id self) throws InterruptedException
    {
        prepareCandidate(self);
        return SCRIPT_CONTINUE;
    }

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        prepareCandidate(self);
        return SCRIPT_CONTINUE;
    }

    public void prepareCandidate(obj_id self) throws InterruptedException
    {
        if (!mounted_sandtrooper.isFromSpawner(self, mounted_sandtrooper.getMountSpawner(self)))
        {
            return;
        }
        setObjVar(self, mounted_sandtrooper.OBJVAR_SPAWNER, mounted_sandtrooper.getMountSpawner(self));
        setHibernationDelay(self, squad_monitor.HIBERNATION_DELAY);
        setCreatureStatic(self, true);
        setWantSawAttackTriggers(self, false);
        setInvulnerable(self, true);
        setObjVar(self, "loot.numItems", 0);
        removeObjVar(self, "loot.lootTable");
        ai_lib.setDefaultCalmBehavior(self, ai_lib.BEHAVIOR_SENTINEL);
        if (!hasScript(self, "ai.mount_combat"))
        {
            attachScript(self, "ai.mount_combat");
        }
        stop(self);
        suspendMovement(self);
        messageTo(self, "announceMountCandidate", null, 0.5f, false);
    }

    public int announceMountCandidate(obj_id self, dictionary params) throws InterruptedException
    {
        if (!mounted_sandtrooper.isMountCandidate(self))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id[] riders = getAllObjectsWithScript(getLocation(self), mounted_sandtrooper.PAIR_SEARCH_RADIUS, "systems.npc_mount.mounted_sandtrooper");
        if (riders != null)
        {
            for (obj_id rider : riders)
            {
                if (!isPlayer(rider) && !isDead(rider) && mounted_sandtrooper.isFromSpawner(rider, mounted_sandtrooper.getRiderSpawner(rider)))
                {
                    dictionary ready = new dictionary();
                    ready.put("mount", self);
                    messageTo(rider, "mountCandidateReady", ready, 0, false);
                }
            }
        }
        return SCRIPT_CONTINUE;
    }
}
