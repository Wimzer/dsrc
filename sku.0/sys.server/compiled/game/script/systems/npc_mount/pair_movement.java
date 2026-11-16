package script.systems.npc_mount;

import script.*;
import script.library.utils;

public class pair_movement extends script.base_script
{
    // Same saved-patrol and follow cache contract as ai_combat_movement.aiFollow,
    // but do not cache a request the native controller rejected. No spawn/species
    // policy and no second autonomous mount mover live in this helper.
    public static boolean approach(obj_id rider, obj_id target, float stopDistance) throws InterruptedException
    {
        if (aiGetMovementState(rider) == MOVEMENT_FOLLOW
                && utils.getIntScriptVar(rider, "ai.combat.moveMode") == script.ai.ai_combat_movement.MOVEMODE_FOLLOW
                && utils.getObjIdScriptVar(rider, "ai.combat.follow.target") == target
                && utils.getFloatScriptVar(rider, "ai.combat.follow.minDistance") == 0.0f
                && utils.getFloatScriptVar(rider, "ai.combat.follow.maxDistance") == stopDistance)
        {
            return true;
        }
        if (aiGetMovementState(rider) == MOVEMENT_PATROL && !hasSuspendedMovement(rider))
        {
            suspendMovement(rider);
        }
        boolean accepted = follow(rider, target, 0.0f, stopDistance);
        if (!accepted)
        {
            utils.setScriptVar(rider, "ai.combat.moveMode", script.ai.ai_combat_movement.MOVEMODE_INVALID);
            utils.removeScriptVar(rider, "ai.combat.follow.target");
            utils.removeScriptVar(rider, "ai.combat.follow.minDistance");
            utils.removeScriptVar(rider, "ai.combat.follow.maxDistance");
            return false;
        }
        utils.setScriptVar(rider, "ai.combat.moveMode", script.ai.ai_combat_movement.MOVEMODE_FOLLOW);
        utils.setScriptVar(rider, "ai.combat.follow.target", target);
        utils.setScriptVar(rider, "ai.combat.follow.minDistance", 0.0f);
        utils.setScriptVar(rider, "ai.combat.follow.maxDistance", stopDistance);
        return true;
    }
}
