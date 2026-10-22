package script.theme_park.heroic.axkva_min_hard_mode;

import script.dictionary;
import script.obj_id;
import script.library.trial;

// Presentation state stays on the existing attempt controller, not on the actor.
public class entrance extends script.base_script
{
    public static final String PREFIX = encounter.VAR_PREFIX + ".entrance";
    public static final String LAST_DEATH = PREFIX + ".last_death";
    public static final String WAITING = PREFIX + ".waiting";
    public static final String REACTION = PREFIX + ".reaction";
    public static final String SCHEDULED = PREFIX + ".scheduled";
    public static final String ARRIVED = PREFIX + ".arrived";
    public static final String SPOKEN = PREFIX + ".spoken";
    public static final String DELIVERED = PREFIX + ".delivered";

    public static void request(obj_id top) throws InterruptedException
    {
        if (!encounter.isRunning(top) || hasObjVar(top, WAITING))
        {
            return;
        }
        setObjVar(top, WAITING, 1);
        String key = getStringObjVar(top, LAST_DEATH);
        if (key == null || key.length() == 0)
        {
            return;
        }
        setObjVar(top, REACTION, key);
        scheduleAfterReaction(top, key);
    }

    public static void spoken(obj_id top, String key) throws InterruptedException
    {
        if (key.startsWith("death_"))
        {
            setObjVar(top, DELIVERED + "." + key, (float)getGameTime());
            scheduleAfterReaction(top, key);
        }
        else if ("axkva_entry".equals(key) && hasObjVar(top, ARRIVED) && !hasObjVar(top, SPOKEN))
        {
            setObjVar(top, SPOKEN, 1);
            // Combat starts four seconds after actual speech, including any queue delay.
            messageTo(top, "activateAxkvaCombat", trial.getSessionDict(top, encounter.VAR_PREFIX), 4.0f, false);
        }
    }

    private static void scheduleAfterReaction(obj_id top, String key) throws InterruptedException
    {
        if (!hasObjVar(top, WAITING) || hasObjVar(top, SCHEDULED)
                || !key.equals(getStringObjVar(top, REACTION)) || !hasObjVar(top, DELIVERED + "." + key))
        {
            return;
        }
        setObjVar(top, SCHEDULED, 1);
        float delay = Math.max(0.0f, getFloatObjVar(top, DELIVERED + "." + key) + 3.0f - getGameTime());
        messageTo(top, "spawnAxkvaAfterReaction", trial.getSessionDict(top, encounter.VAR_PREFIX), delay, false);
    }

    public static boolean isPending(obj_id top) throws InterruptedException
    {
        return hasObjVar(top, WAITING) && !hasObjVar(top, encounter.VAR_AXKVA_RELEASED);
    }
}
