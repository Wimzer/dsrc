package script.theme_park.heroic.ig88_hard_mode;

import script.*;
import script.library.*;

public class hmig extends script.base_script
{
    public hmig()
    {
    }
    public static final String COMBAT_WEAPON_PHASE = "ig88.weapon.phase";
    public static final int COMBAT_WEAPON_PHASE_UNKNOWN = 0;
    public static final int COMBAT_WEAPON_PHASE_ROCKET = 1;
    public static final int COMBAT_WEAPON_PHASE_FLAME_THROWER = 2;
    public static final int COMBAT_WEAPON_TIME_UNKNOWN = 0;
    public static final int COMBAT_WEAPON_TIME_ROCKET = 20;
    public static final int COMBAT_WEAPON_TIME_FLAME_THROWER = 2;
    public static final String COMBAT_ACTION_COMBINATION = "ig88.combination.state";
    public static final String COMBAT_ACTION_COBMINATION_PHASE = "ig88.combination.phase";
    public static final int COMBAT_ACTION_COMBINATION_PAUSE = 10;
    public static final int COMBAT_ACTION_COMBINATION_UNKNOWN = 0;
    public static final int COMBAT_ACTION_COMBINATION_ELECTRICAL = 1;
    public static final int COMBAT_ACTION_COMBINATION_GRENADE = 2;
    public static final int COMBAT_ACTION_COMBINATION_DROIDEKA = 3;
    public static final int COMBAT_ACTION_COMBINATION_INTERMISSION = 4;
    public static final int COMBAT_ACTION_TIME_INTERMISSION = 10;
    public static final int COMBAT_ACTION_PHASE_ELECTRICAL_0 = 0;
    public static final int COMBAT_ACTION_PHASE_ELECTRICAL_1 = 1;
    public static final int COMBAT_ACTION_PHASE_ELECTRICAL_2 = 2;
    public static final int COMBAT_ACTION_PHASE_ELECTRICAL_3 = 3;
    public static final int COMBAT_ACTION_TIME_ELECTRICAL_0 = 1;
    public static final int COMBAT_ACTION_TIME_ELECTRICAL_1 = 1;
    public static final int COMBAT_ACTION_TIME_ELECTRICAL_2 = 1;
    public static final int COMBAT_ACTION_TIME_ELECTRICAL_3 = 1;
    public static final int COMBAT_ACTION_PHASE_GRENADE_0 = 0;
    public static final int COMBAT_ACTION_PHASE_GRENADE_1 = 1;
    public static final int COMBAT_ACTION_PHASE_GRENADE_2 = 2;
    public static final int COMBAT_ACTION_TIME_GRENADE_0 = 2;
    public static final int COMBAT_ACTION_TIME_GRENADE_1 = 1;
    public static final int COMBAT_ACTION_TIME_GRENADE_2 = 1;
    public static final int COMBAT_ACTION_PHASE_DROIDEKA_0 = 0;
    public static final int COMBAT_ACTION_PHASE_DROIDEKA_1 = 1;
    public static final int COMBAT_ACTION_PHASE_DROIDEKA_2 = 2;
    public static final int COMBAT_ACTION_TIME_DROIDEKA_0 = 1;
    public static final int COMBAT_ACTION_TIME_DROIDEKA_1 = 15;
    public static final int COMBAT_ACTION_TIME_DROIDEKA_2 = 10;
    public void shoutRandom(obj_id self, String msg) throws InterruptedException
    {
        msg = msg + rand(1, 10);
        playClientEffectObj(self, "appearance/pt_ig88_glowspeak.prt", self, "");
        obj_id dungeon = getTopMostContainer(self);
        if (!isIdValid(dungeon))
        {
            return;
        }
        obj_id[] targets = trial.getValidTargetsInCell(dungeon, "r1");
        if (targets != null && targets.length > 0)
        {
            prose_package pp = new prose_package();
            pp = prose.setStringId(pp, new string_id("spam", "ig88_system_message_yellow"));
            pp = prose.setTU(pp, new string_id("spam", msg));
            utils.sendSystemMessageProse(targets, pp);
        }
        return;
    }
    public int waypoint1(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        setMovementWalk(self);
        location loc = getLocation(self);
        loc.z += 8;
        utils.setScriptVar(self, "waypoint", loc);
        pathTo(self, loc);
        return SCRIPT_CONTINUE;
    }
    public int waypoint2(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        setMovementWalk(self);
        location loc = new location(0, 0, 10, getLocation(self).area, getLocation(self).cell);
        utils.setScriptVar(self, "waypoint", loc);
        utils.setScriptVar(self, "movingToAttack", 1);
        pathTo(self, loc);
        return SCRIPT_CONTINUE;
    }
    public int shoutRegret(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        shoutRandom(self, "ig88_regret");
        if (utils.hasScriptVar(self, "waypoint"))
        {
            location currentLoc = getLocation(self);
            location targetLoc = utils.getLocationScriptVar(self, "waypoint");
            if (utils.getDistance2D(currentLoc, targetLoc) > 2)
            {
                pathTo(self, targetLoc);
            }
        }
        return SCRIPT_CONTINUE;
    }
    public int shoutBombDroids(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        shoutRandom(self, "ig88_bomb_droids");
        doAnimationAction(self, "threaten");
        return SCRIPT_CONTINUE;
    }
    public int shoutDroidekas(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        shoutRandom(self, "ig88_droidekas");
        doAnimationAction(self, "threaten");
        return SCRIPT_CONTINUE;
    }
    public int shoutSuperDroids(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        shoutRandom(self, "ig88_super_droids");
        doAnimationAction(self, "threaten");
        return SCRIPT_CONTINUE;
    }
    public int shoutAssassination(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        shoutRandom(self, "ig88_assassination");
        doAnimationAction(self, "threaten");
        return SCRIPT_CONTINUE;
    }
    public int shoutBombIntermission(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        shoutRandom(self, "ig88_bomb_intermission");
        doAnimationAction(self, "threaten");
        return SCRIPT_CONTINUE;
    }
    public int shoutFailed(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        if (!utils.hasScriptVar(self, "ig88_shouted_failed"))
        {
            chat.chat(self, chat.CHAT_SHOUT, new string_id("spam", "ig88_failed"));
            utils.setScriptVar(self, "ig88_shouted_failed", 1);
            obj_id dungeon = getTopMostContainer(self);
            if (!isIdValid(dungeon))
            {
                return SCRIPT_CONTINUE;
            }
            obj_id[] targets = trial.getValidTargetsInCell(dungeon, "r1");
            if (targets != null && targets.length > 0)
            {
                prose_package pp = new prose_package();
                pp = prose.setStringId(pp, new string_id("spam", "ig88_system_message_yellow"));
                pp = prose.setTU(pp, new string_id("spam", "ig88_failed"));
                utils.sendSystemMessageProse(targets, pp);
            }
        }
        return SCRIPT_CONTINUE;
    }
    public void findNextTarget(obj_id self) throws InterruptedException
    {
        if (ai_lib.isDead(self))
        {
            return;
        }
        obj_id dungeon = getTopMostContainer(self);
        obj_id target = null;
        if (!isIdValid(dungeon))
        {
            return;
        }
        dictionary params = new dictionary();
        obj_id[] targets = trial.getNonStealthedTargetsInCell(dungeon, "r1");
        if (targets == null || targets.length <= 0)
        {
            return;
        }
        target = trial.getClosest(self, targets);
        if (!isIdValid(target))
        {
            messageTo(self, "findTarget", params, 1, false);
            return;
        }
        int time = getGameTime();
        utils.setScriptVar(self, "nextWeaponPhase", time);
        params.put("nextWeaponPhase", time);
        messageTo(self, "nextWeaponPhase", params, 1, false);
        utils.setScriptVar(self, "nextComboPhase", time);
        params.put("nextComboPhase", time);
        messageTo(self, "nextComboPhase", params, 1, false);
        setHate(self, target, 5000);
    }
    public int findTarget(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        if (isInvulnerable(self))
        {
            setInvulnerable(self, false);
            initializeHardModeHealth(self, new dictionary());
        }
        applyHardModeBuffs(self, params);
        findNextTarget(self);
        obj_id dungeon = getTopMostContainer(self);
        if (!isIdValid(dungeon))
        {
            return SCRIPT_CONTINUE;
        }
        dictionary sessionDict = new dictionary();
        messageTo(self, "nextWeaponPhase", sessionDict, 1, false);
        messageTo(self, "nextComboPhase", sessionDict, 1, false);
        return SCRIPT_CONTINUE;
    }
    public int OnExitedCombat(obj_id self) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        findNextTarget(self);
        return SCRIPT_CONTINUE;
    }
    public int OnAttach(obj_id self) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id dungeon = getTopMostContainer(self);
        if (!isIdValid(dungeon))
        {
            return SCRIPT_CONTINUE;
        }
        dictionary sessionDict = controller.getControllerSessionDict(dungeon);
        messageTo(self, "initializeHardModeHealth", sessionDict, 0.25f, false);
        messageTo(self, "colorize", sessionDict, 0.25f, false);
        messageTo(dungeon, "ig88_failure_check", sessionDict, 5, false);
        return SCRIPT_CONTINUE;
    }

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        if (isInvulnerable(self))
        {
            buff.removeBuff(self, "ig88_hard_enrage");
            removeObjVar(self, controller.VAR_PREFIX + ".combat_buffs_applied");
            removeObjVar(self, controller.VAR_PREFIX + ".health_initialized");
        }
        messageTo(self, "initializeHardModeHealth", new dictionary(), 0.25f, false);
        return SCRIPT_CONTINUE;
    }

    public int initializeHardModeHealth(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || hasObjVar(self, controller.VAR_PREFIX + ".health_initialized"))
        {
            return SCRIPT_CONTINUE;
        }
        boolean staged = isInvulnerable(self);
        if (staged)
        {
            setInvulnerable(self, false);
        }
        setMaxAttrib(self, HEALTH, controller.HMIG_HEALTH);
        setAttrib(self, HEALTH, controller.HMIG_HEALTH);
        setAttrib(self, ACTION, getMaxAttrib(self, ACTION));
        if (staged)
        {
            setInvulnerable(self, true);
        }
        setObjVar(self, controller.VAR_PREFIX + ".health_initialized", 1);
        return SCRIPT_CONTINUE;
    }

    public int colorize(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        hue.setColor(self, hue.INDEX_1, 9);
        hue.setColor(self, hue.INDEX_2, 46);
        return SCRIPT_CONTINUE;
    }
    public int nextWeaponPhase(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || ai_lib.isDead(self))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id dungeon = getTopMostContainer(self);
        if (!isIdValid(dungeon))
        {
            return SCRIPT_CONTINUE;
        }
        dictionary sessionDict = new dictionary();
        int time = 0;
        if (utils.hasScriptVar(self, "nextWeaponPhase"))
        {
            time = utils.getIntScriptVar(self, "nextWeaponPhase");
            int messageTime = params.getInt("nextWeaponPhase");
            if (messageTime != time)
            {
                return SCRIPT_CONTINUE;
            }
        }
        time = getGameTime();
        utils.setScriptVar(self, "nextWeaponPhase", time);
        sessionDict.put("nextWeaponPhase", time);
        int phase = utils.getIntScriptVar(self, COMBAT_WEAPON_PHASE);
        int combination = 0;
        switch (phase)
        {
            default:
            case COMBAT_WEAPON_PHASE_UNKNOWN:
            utils.setScriptVar(self, COMBAT_WEAPON_PHASE, COMBAT_WEAPON_PHASE_ROCKET);
            combination = utils.getIntScriptVar(self, COMBAT_ACTION_COMBINATION);
            if (combination != COMBAT_ACTION_COMBINATION_DROIDEKA)
            {
                aiEquipPrimaryWeapon(self);
            }
            messageTo(self, "nextWeaponPhase", sessionDict, COMBAT_WEAPON_TIME_ROCKET, false);
            break;
            case COMBAT_WEAPON_PHASE_ROCKET:
            utils.setScriptVar(self, COMBAT_WEAPON_PHASE, COMBAT_WEAPON_PHASE_FLAME_THROWER);
            combination = utils.getIntScriptVar(self, COMBAT_ACTION_COMBINATION);
            if (combination != COMBAT_ACTION_COMBINATION_DROIDEKA)
            {
                aiEquipPrimaryWeapon(self);
            }
            messageTo(self, "nextWeaponPhase", sessionDict, COMBAT_WEAPON_TIME_ROCKET, false);
            break;
            case COMBAT_WEAPON_PHASE_FLAME_THROWER:
            utils.setScriptVar(self, COMBAT_WEAPON_PHASE, COMBAT_WEAPON_PHASE_ROCKET);
            combination = utils.getIntScriptVar(self, COMBAT_ACTION_COMBINATION);
            if (combination != COMBAT_ACTION_COMBINATION_DROIDEKA)
            {
                aiEquipSecondaryWeapon(self);
            }
            messageTo(self, "nextWeaponPhase", sessionDict, COMBAT_WEAPON_TIME_FLAME_THROWER, false);
            break;
        }
        return SCRIPT_CONTINUE;
    }
    public int nextComboPhase(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || ai_lib.isDead(self))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id dungeon = getTopMostContainer(self);
        if (!isIdValid(dungeon))
        {
            return SCRIPT_CONTINUE;
        }
        dictionary sessionDict = new dictionary();
        if (!ai_lib.isInCombat(self))
        {
            utils.setScriptVar(self, COMBAT_ACTION_COMBINATION, 0);
            utils.setScriptVar(self, COMBAT_ACTION_COBMINATION_PHASE, 0);
            messageTo(self, "nextComboPhase", sessionDict, COMBAT_ACTION_TIME_INTERMISSION, false);
        }
        int combination = utils.getIntScriptVar(self, COMBAT_ACTION_COMBINATION);
        int phase = utils.getIntScriptVar(self, COMBAT_ACTION_COBMINATION_PHASE);
        if (combination == COMBAT_ACTION_COMBINATION_UNKNOWN)
        {
            combination = rand(1, 3);
            utils.setScriptVar(self, COMBAT_ACTION_COMBINATION, combination);
        }
        float phaseTime = 1.0f;
        removeObjVar(self, "ai.combat.oneShotAction");
        removeObjVar(self, "oneShotActionComplete");
        int time = 0;
        if (utils.hasScriptVar(self, "nextComboPhase"))
        {
            time = utils.getIntScriptVar(self, "nextComboPhase");
            int messageTime = params.getInt("nextComboPhase");
            if (messageTime != time)
            {
                return SCRIPT_CONTINUE;
            }
        }
        time = getGameTime();
        utils.setScriptVar(self, "nextComboPhase", time);
        sessionDict.put("nextComboPhase", time);
        int maxPhase = 0;
        switch (combination)
        {
            case COMBAT_ACTION_COMBINATION_ELECTRICAL:
            switch (phase)
            {
                case COMBAT_ACTION_PHASE_ELECTRICAL_0:
                phaseTime = COMBAT_ACTION_TIME_ELECTRICAL_0;
                setObjVar(self, "ai.combat.oneShotAction", "ig88_hard_droideka_electrify");
                break;
                case COMBAT_ACTION_PHASE_ELECTRICAL_1:
                phaseTime = COMBAT_ACTION_TIME_ELECTRICAL_1;
                break;
                case COMBAT_ACTION_PHASE_ELECTRICAL_2:
                setObjVar(self, "ai.combat.oneShotAction", "ig88_hard_rocket_launch");
                phaseTime = COMBAT_ACTION_TIME_ELECTRICAL_2;
                break;
                case COMBAT_ACTION_PHASE_ELECTRICAL_3:
                utils.setScriptVar(self, COMBAT_ACTION_COMBINATION, COMBAT_ACTION_COMBINATION_INTERMISSION);
                phaseTime = COMBAT_ACTION_TIME_ELECTRICAL_3;
                break;
            }
            maxPhase = COMBAT_ACTION_PHASE_ELECTRICAL_3;
            break;
            case COMBAT_ACTION_COMBINATION_GRENADE:
            switch (phase)
            {
                case COMBAT_ACTION_PHASE_GRENADE_0:
                phaseTime = COMBAT_ACTION_TIME_GRENADE_0;
                setObjVar(self, "ai.combat.oneShotAction", "ig88_hard_shockwave");
                break;
                case COMBAT_ACTION_PHASE_GRENADE_1:
                phaseTime = COMBAT_ACTION_TIME_GRENADE_1;
                setObjVar(self, "ai.combat.oneShotAction", "ig88_hard_grenade");
                break;
                case COMBAT_ACTION_PHASE_GRENADE_2:
                utils.setScriptVar(self, COMBAT_ACTION_COMBINATION, COMBAT_ACTION_COMBINATION_INTERMISSION);
                phaseTime = COMBAT_ACTION_TIME_GRENADE_2;
                break;
            }
            maxPhase = COMBAT_ACTION_PHASE_GRENADE_2;
            break;
            case COMBAT_ACTION_COMBINATION_DROIDEKA:
            switch (phase)
            {
                case COMBAT_ACTION_PHASE_DROIDEKA_0:
                phaseTime = COMBAT_ACTION_TIME_DROIDEKA_0;
                break;
                case COMBAT_ACTION_PHASE_DROIDEKA_1:
                aiUnEquipWeapons(self);
                obj_id[] targets = trial.getObjectsInDungeonWithObjVar(dungeon, controller.VAR_ROLE);
                int droidekaCount = 0;
                if (targets != null)
                {
                    for (obj_id target : targets)
                    {
                        String role = getStringObjVar(target, controller.VAR_ROLE);
                        if ("boss_droideka".equals(role) && isIdValid(target) && !ai_lib.isDead(target))
                        {
                            droidekaCount++;
                        }
                    }
                }
                if (droidekaCount == 0)
                {
                    dictionary dungeonParams = trial.getSessionDict(dungeon);
                    dungeonParams.put("triggerName", "message_ig88_normal_droidekas");
                    dungeonParams.put("target", self);
                    dungeonParams.put("triggerType", "triggerId");
                    messageTo(dungeon, "triggerFired", dungeonParams, 0, false);
                }
                phaseTime = COMBAT_ACTION_TIME_DROIDEKA_1;
                break;
                case COMBAT_ACTION_PHASE_DROIDEKA_2:
                utils.setScriptVar(self, COMBAT_ACTION_COMBINATION, COMBAT_ACTION_COMBINATION_INTERMISSION);
                phaseTime = COMBAT_ACTION_TIME_DROIDEKA_2;
                messageTo(self, "nextWeaponPhase", sessionDict, 1, false);
                break;
            }
            maxPhase = COMBAT_ACTION_TIME_DROIDEKA_2;
            break;
            case COMBAT_ACTION_COMBINATION_INTERMISSION:
            utils.setScriptVar(self, COMBAT_ACTION_COMBINATION, 0);
            utils.setScriptVar(self, COMBAT_ACTION_COBMINATION_PHASE, 0);
            messageTo(self, "nextComboPhase", sessionDict, COMBAT_ACTION_TIME_INTERMISSION, false);
            return SCRIPT_CONTINUE;
        }
        phase++;
        if (phase > maxPhase)
        {
            phase = maxPhase;
        }
        utils.setScriptVar(self, COMBAT_ACTION_COBMINATION_PHASE, phase);
        messageTo(self, "nextComboPhase", sessionDict, phaseTime, false);
        return SCRIPT_CONTINUE;
    }
    public int OnMovePathComplete(obj_id self) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        location targetLoc = getLocation(self);
        targetLoc.x = 0;
        targetLoc.z = 0;
        location myLoc = getLocation(self);
        if (utils.hasScriptVar(self, "movingToAttack"))
        {
            location loc = new location(0, 0, 10, getLocation(self).area, getLocation(self).cell);
            if (utils.getDistance2D(getLocation(self), loc) > 2)
            {
                setMovementPercent(self, 100.0f);
                pathTo(self, loc);
            }
            else
            {
                setMovementPercent(self, 0.0f);
            }
            obj_id dungeon = getTopMostContainer(self);
            if (!isIdValid(dungeon))
            {
                return SCRIPT_CONTINUE;
            }
            dictionary sessionDict = new dictionary();
            messageTo(self, "findTarget", sessionDict, 0.25f, false);
        }
        return SCRIPT_CONTINUE;
    }
    public int OnIncapacitated(obj_id self, obj_id killer) throws InterruptedException
    {
        if (!isIdValid(self))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id dungeon = getTopMostContainer(self);
        obj_id target = null;
        if (!isIdValid(dungeon))
        {
            return SCRIPT_CONTINUE;
        }
        dictionary params = controller.getControllerSessionDict(dungeon);
        params.put("triggerName", "message_ig88_defeated");
        params.put("target", self);
        params.put("triggerType", "triggerId");
        dictionary trigger = trial.getSessionDict(dungeon);
        trigger.put("triggerName", "message_ig88_defeated");
        trigger.put("target", self);
        trigger.put("triggerType", "triggerId");
        messageTo(dungeon, "triggerFired", trigger, 0, false);
        messageTo(dungeon, "hmigDied", params, 1, false);
        return SCRIPT_CONTINUE;
    }
    public int OnDeath(obj_id self, obj_id killer, obj_id corpse) throws InterruptedException
    {
        return SCRIPT_CONTINUE;
    }

    public int applyHardModeBuffs(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || ai_lib.isDead(self))
        {
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(self, controller.VAR_PREFIX + ".combat_buffs_applied"))
        {
            return SCRIPT_CONTINUE;
        }
        setObjVar(self, controller.VAR_PREFIX + ".combat_buffs_applied", 1);
        buff.applyBuff(self, "ig88_hard_enrage", -1.0f);
        obj_id dungeon = getTopMostContainer(self);
        messageTo(self, "overwhelmingNauseaLoop", isIdValid(dungeon) ? controller.getControllerSessionDict(dungeon) : null, 6.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int overwhelmingNauseaLoop(obj_id self, dictionary params) throws InterruptedException
    {
        if (!isIdValid(self) || ai_lib.isDead(self))
        {
            return SCRIPT_CONTINUE;
        }
        obj_id dungeon = getTopMostContainer(self);
        if (!isIdValid(dungeon) || hasObjVar(dungeon, controller.VAR_RESET_PENDING) || !controller.verifyControllerSession(dungeon, params))
        {
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(dungeon, controller.VAR_FLAMES_UNLOCKED))
        {
            obj_id[] players = trial.getValidTargetsInCell(dungeon, "r1");
            if (players != null)
            {
                for (obj_id player : players)
                {
                    if (isIdValid(player) && !isDead(player))
                    {
                        buff.applyBuff(player, "ig88_hard_overwhelming_nausea", 15.0f);
                    }
                }
            }
        }
        messageTo(self, "overwhelmingNauseaLoop", controller.getControllerSessionDict(dungeon), 6.0f, false);
        return SCRIPT_CONTINUE;
    }
}
