package script.systems.npc_mount;

import script.*;

public class auxiliary_support extends script.base_script
{
    public auxiliary_support()
    {
    }

    public int OnAboutToBeTransferred(obj_id self, obj_id destContainer, obj_id transferer) throws InterruptedException
    {
        return SCRIPT_OVERRIDE;
    }

    public int OnTransferred(obj_id self, obj_id sourceContainer, obj_id destContainer, obj_id transferer) throws InterruptedException
    {
        notifyOwner(self, "transfer");
        if (exists(self))
        {
            destroyObject(self);
        }
        return SCRIPT_CONTINUE;
    }

    public int OnIncapacitated(obj_id self, obj_id killer) throws InterruptedException
    {
        notifyOwner(self, "incapacitated");
        return SCRIPT_CONTINUE;
    }

    public int OnDeath(obj_id self, obj_id killer, obj_id corpseId) throws InterruptedException
    {
        notifyOwner(self, "death");
        return SCRIPT_CONTINUE;
    }

    public int OnDestroy(obj_id self) throws InterruptedException
    {
        notifyOwner(self, "destroy");
        return SCRIPT_CONTINUE;
    }

    public int OnUnloadedFromMemory(obj_id self) throws InterruptedException
    {
        notifyOwner(self, "unload");
        return SCRIPT_CONTINUE;
    }

    public int OnLostItem(obj_id self, obj_id destContainer, obj_id transferer, obj_id item) throws InterruptedException
    {
        // The native detach operation may not have finished when this fires.
        // Verify once on the rider after the transfer rather than polling.
        if (mounted_sandtrooper.ROLE_MOUNT.equals(getStringObjVar(self, mounted_sandtrooper.OBJVAR_AUXILIARY_ROLE)))
        {
            obj_id owner = getObjIdObjVar(self, mounted_sandtrooper.OBJVAR_AUXILIARY_OWNER);
            if (isIdValid(owner) && item == owner && exists(owner))
            {
                mounted_sandtrooper.traceEvent(owner, "auxiliary.lostRider", "source=" + self);
                messageTo(owner, "verifyMountedPairAfterPostureChange", null, mounted_sandtrooper.DISMOUNT_VERIFY_DELAY, false);
            }
        }
        return SCRIPT_CONTINUE;
    }

    public void notifyOwner(obj_id self) throws InterruptedException
    {
        notifyOwner(self, "unspecified");
    }

    public int OnChangedPosture(obj_id self, int oldPosture, int newPosture) throws InterruptedException
    {
        obj_id owner = getObjIdObjVar(self, mounted_sandtrooper.OBJVAR_AUXILIARY_OWNER);
        mounted_sandtrooper.traceEvent(owner, "auxiliary.posture", "source=" + self + " old=" + oldPosture + " new=" + newPosture);
        return SCRIPT_CONTINUE;
    }

    public void notifyOwner(obj_id self, String event) throws InterruptedException
    {
        if (!isIdValid(self) || !hasObjVar(self, mounted_sandtrooper.OBJVAR_AUXILIARY_OWNER))
        {
            return;
        }
        obj_id owner = getObjIdObjVar(self, mounted_sandtrooper.OBJVAR_AUXILIARY_OWNER);
        if (!isIdValid(owner) || !exists(owner) || !hasScript(owner, "systems.npc_mount.mounted_sandtrooper"))
        {
            return;
        }
        dictionary params = new dictionary();
        mounted_sandtrooper.traceEvent(owner, "auxiliary." + event, "source=" + self
                + " role=" + getStringObjVar(self, mounted_sandtrooper.OBJVAR_AUXILIARY_ROLE));
        params.put("auxiliary", self);
        params.put("role", getStringObjVar(self, mounted_sandtrooper.OBJVAR_AUXILIARY_ROLE));
        params.put("traceEvent", event);
        messageTo(owner, "auxiliaryUnavailable", params, 0.0f, false);
    }
}
