package script.theme_park.heroic.axkva_min_hard_mode;

import script.dictionary;
import script.location;
import script.obj_id;
import script.library.ai_lib;
import script.library.trial;
import script.library.utils;

public class false_hazard extends script.base_script
{
    public int OnAttach(obj_id self) throws InterruptedException
    {
        setInvulnerable(self, true);
        messageTo(self, "validateIllusion", null, 1.0f, false);
        if (hasObjVar(self, illusions.POOL))
        {
            messageTo(self, "poolPulse", null, getFloatObjVar(self, "illusion_initial"), false);
        }
        else
        {
            messageTo(self, "acquireTarget", null, 1.0f, false);
            messageTo(self, "stormPulse", null, 2.0f, false);
            messageTo(self, "endLifeCycle", null, 18.0f, false);
        }
        return SCRIPT_CONTINUE;
    }

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        trial.cleanupObject(self);
        return SCRIPT_CONTINUE;
    }

    private boolean valid(obj_id self) throws InterruptedException
    {
        obj_id owner = getObjIdObjVar(self, illusions.OWNER);
        obj_id top = trial.getTop(self);
        if (!isIdValid(owner) || !exists(owner) || ai_lib.isDead(owner) || trial.getTop(owner) != top
                || !encounter.isRunning(top) || getIntObjVar(self, illusions.ATTEMPT) != trial.getSession(top, encounter.VAR_PREFIX))
        {
            trial.cleanupObject(self);
            return false;
        }
        return true;
    }

    public int validateIllusion(obj_id self, dictionary params) throws InterruptedException
    {
        if (valid(self))
        {
            messageTo(self, "validateIllusion", null, 1.0f, false);
        }
        return SCRIPT_CONTINUE;
    }

    public int acquireTarget(obj_id self, dictionary params) throws InterruptedException
    {
        if (!valid(self))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id[] targets = utils.getObjIdArrayScriptVar(self, trial.KIMARU_HATE_LIST);
        if (targets != null && targets.length > 0)
        {
            int first = rand(0, targets.length - 1);
            for (int i = 0; i < targets.length; i++)
            {
                obj_id target = targets[(first + i) % targets.length];
                if (isIdValid(target) && exists(target) && !ai_lib.isDead(target) && trial.getTop(target) == trial.getTop(self))
                {
                    follow(self, target, 0.0f, 0.1f);
                    break;
                }
            }
        }
        messageTo(self, "acquireTarget", null, 6.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int stormPulse(obj_id self, dictionary params) throws InterruptedException
    {
        if (!valid(self))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id[] targets = trial.getValidTargetsInRadius(self, 5.0f);
        if (targets != null && targets.length > 0)
        {
            playClientEffectLoc(getObjIdObjVar(self, illusions.OWNER), "appearance/pt_kimaru_force_storm.prt", getLocation(self), 0.0f);
        }
        messageTo(self, "stormPulse", null, 1.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int poolPulse(obj_id self, dictionary params) throws InterruptedException
    {
        if (!valid(self))
        {
            return SCRIPT_CONTINUE;
        }
        playClientEffectLoc(getObjIdObjVar(self, illusions.OWNER), getStringObjVar(self, "illusion_effect"), getLocation(self), 0.0f);
        int left = getIntObjVar(self, "illusion_loops") - 1;
        if (left <= 0)
        {
            trial.cleanupObject(self);
        }
        else
        {
            setObjVar(self, "illusion_loops", left);
            messageTo(self, "poolPulse", null, getFloatObjVar(self, "illusion_interval"), false);
        }
        return SCRIPT_CONTINUE;
    }

    public int endLifeCycle(obj_id self, dictionary params) throws InterruptedException
    {
        trial.cleanupObject(self);
        return SCRIPT_CONTINUE;
    }
}
