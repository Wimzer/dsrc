package script.theme_park.heroic.axkva_min_hard_mode;

import script.dictionary;
import script.library.chat;
import script.library.create;
import script.library.trial;
import script.location;
import script.obj_id;

public class axkva extends script.theme_park.heroic.axkva_min.axkva
{
    public int OnAttach(obj_id self) throws InterruptedException
    {
        setName(self, "Axkva Min");
        encounter.prepareActorRegistration(self);
        trial.setHp(self, trial.HP_AXKVA * 4);
        messageTo(self, "applyCurrentHardModeRage", null, 3.0f, false);
        return SCRIPT_CONTINUE;
    }
    public int registerHardModeActor(obj_id self, dictionary params) throws InterruptedException
    {
        encounter.registerActor(self, params);
        return SCRIPT_CONTINUE;
    }
    public int applyCurrentHardModeRage(obj_id self, dictionary params) throws InterruptedException
    {
        encounter.applyCurrentRage(self);
        return SCRIPT_CONTINUE;
    }
    public int OnEnteredCombat(obj_id self) throws InterruptedException
    {
        encounter.startWipeCheck(self);
        return super.OnEnteredCombat(self);
    }
    public int axkva_force_whirl(obj_id self, obj_id target, String params, float defaultTime) throws InterruptedException
    {
        location loc1 = new location(-63.6f, 14.4f, 36.4f, "dungeon3", getCellId(trial.getTop(self), "r2"));
        location loc2 = new location(-69.8f, 13.6f, 9.6f, "dungeon3", getCellId(trial.getTop(self), "r2"));
        location loc3 = new location(-44.4f, 7.0f, 1.5f, "dungeon3", getCellId(trial.getTop(self), "r2"));
        location loc4 = new location(-42.7f, 8.3f, 20.9f, "dungeon3", getCellId(trial.getTop(self), "r2"));
        location loc5 = new location(-55.0f, 10.0f, 18.0f, "dungeon3", getCellId(trial.getTop(self), "r2"));
        location loc6 = new location(-49.0f, 10.0f, 13.0f, "dungeon3", getCellId(trial.getTop(self), "r2"));
        location[] path1 = { loc1, loc2, loc3, loc4, loc5, loc6 };
        location[] path2 = { loc2, loc3, loc4, loc1, loc5, loc6 };
        location[] path3 = { loc3, loc4, loc1, loc2, loc5, loc6 };
        location[] path4 = { loc4, loc1, loc2, loc3, loc5, loc6 };
        obj_id mob1 = create.object("axkva_force_whirl", loc1);
        obj_id mob2 = create.object("axkva_force_whirl", loc2);
        obj_id mob3 = create.object("axkva_force_whirl", loc3);
        obj_id mob4 = create.object("axkva_force_whirl", loc4);
        configureForceWhirl(self, mob1, path1);
        configureForceWhirl(self, mob2, path2);
        configureForceWhirl(self, mob3, path3);
        configureForceWhirl(self, mob4, path4);
        return SCRIPT_CONTINUE;
    }
    public int axkva_summon_aspect(obj_id self, obj_id target, String params, float defaultTime) throws InterruptedException
    {
        if (encounter.countActiveAspectAdds(self) >= encounter.AXKVA_MAX_ACTIVE_ASPECT_ADDS)
        {
            return SCRIPT_CONTINUE;
        }
        obj_id aspect = encounter.spawnAspectAdd(self, getLocation(self));
        if (isIdValid(aspect))
        {
            chat.chat(self, "Assist me sisters!");
        }
        return SCRIPT_CONTINUE;
    }
    public int hardContagionBomb(obj_id self, dictionary params) throws InterruptedException
    {
        if (params == null || params.isEmpty() || !encounter.isCurrentAxkva(self))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id top = trial.getTop(self);
        if (params.getInt("hardAttempt") != trial.getSession(top, encounter.VAR_PREFIX))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id player = params.getObjId("player");
        if (isIdValid(player) && exists(player) && trial.getTop(player) == top)
        {
            queueCommand(self, getStringCrc(encounter.AXKVA_CONTAGION_BOMB_COMMAND), player, "", COMMAND_PRIORITY_DEFAULT);
        }
        return SCRIPT_CONTINUE;
    }
    private void configureForceWhirl(obj_id self, obj_id mob, location[] path) throws InterruptedException
    {
        if (!isIdValid(mob))
        {
            return;
        }
        setMaster(mob, self);
        trial.markAsTempObject(mob, true);
        trial.setParent(trial.getTop(self), mob, false);
        setObjVar(mob, "path", path);
        encounter.applyRandomCloneImmunity(mob);
        encounter.applyCurrentAddVulnerability(mob);
    }
}
