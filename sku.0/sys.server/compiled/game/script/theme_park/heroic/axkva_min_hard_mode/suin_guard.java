package script.theme_park.heroic.axkva_min_hard_mode;

import script.dictionary;
import script.library.ai_lib;
import script.library.combat;
import script.library.trial;
import script.library.utils;
import script.obj_id;
import script.systems.combat.combat_actions;

public class suin_guard extends script.theme_park.heroic.axkva_min.suin_guard
{
    private static final String VAR_CLOAK_RECOVERY_PENDING = encounter.VAR_PREFIX + ".suin_guard_cloak_recovery";

    public int OnExitedCombat(obj_id self) throws InterruptedException
    {
        obj_id owner = getHardModeOwner(self);
        if (isIdValid(owner) && combat_actions.isHardModeAllyCloaked(owner))
        {
            scheduleCloakRecovery(self, 2.5f);
            return SCRIPT_CONTINUE;
        }
        return super.OnExitedCombat(self);
    }

    public int resumeAfterParentCloak(obj_id self, dictionary params) throws InterruptedException
    {
        utils.removeScriptVar(self, VAR_CLOAK_RECOVERY_PENDING);
        if (!isIdValid(self) || !exists(self) || ai_lib.isDead(self))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id owner = getHardModeOwner(self);
        if (isIdValid(owner) && combat_actions.isHardModeAllyCloaked(owner))
        {
            scheduleCloakRecovery(self, 0.5f);
            return SCRIPT_CONTINUE;
        }
        if (!isIdValid(owner) || !exists(owner) || ai_lib.isDead(owner) || !ai_lib.isInCombat(owner))
        {
            return super.OnExitedCombat(self);
        }
        encounter.addOwnerHateList(owner, self);
        obj_id target = combat.getRandomHateTarget(owner);
        if (isIdValid(target) && exists(target) && !ai_lib.isDead(target))
        {
            setHate(self, target, 1.0f);
            startCombat(self, target);
        }
        return SCRIPT_CONTINUE;
    }

    private void scheduleCloakRecovery(obj_id self, float delay) throws InterruptedException
    {
        if (utils.hasScriptVar(self, VAR_CLOAK_RECOVERY_PENDING))
        {
            return;
        }
        utils.setScriptVar(self, VAR_CLOAK_RECOVERY_PENDING, true);
        messageTo(self, "resumeAfterParentCloak", null, delay, false);
    }

    private obj_id getHardModeOwner(obj_id self) throws InterruptedException
    {
        if (!utils.hasScriptVar(self, encounter.VAR_SUIN_OWNER))
        {
            return obj_id.NULL_ID;
        }
        return utils.getObjIdScriptVar(self, encounter.VAR_SUIN_OWNER);
    }
}
