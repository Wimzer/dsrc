package script.theme_park.heroic.axkva_min_hard_mode;

import script.*;
import script.library.ai_lib;
import script.library.chat;
import script.library.sequencer;
import script.library.trial;

public class dialogue extends script.base_script
{
    public static final String TABLE = "theme_park/heroic/axkva_min_hard_mode";
    public static final String NEXT = encounter.VAR_PREFIX + ".dialogue_next";
    public static final String ESSENCE = encounter.VAR_PREFIX + ".essence";
    public static final String OPENING_END = encounter.VAR_PREFIX + ".opening_end";
    public static final String LAST = encounter.VAR_PREFIX + ".dialogue_last";

    public static void enqueue(obj_id top, String key, obj_id speaker, obj_id corpse, float delay) throws InterruptedException
    {
        if (!encounter.isRunning(top))
        {
            return;
        }
        float now = getGameTime();
        float when = now + delay;
        if (hasObjVar(top, NEXT))
        {
            when = Math.max(when, getFloatObjVar(top, NEXT));
        }
        setObjVar(top, NEXT, when + 3.0f);
        schedule(top, key, speaker, corpse, when - now);
    }

    public static void schedule(obj_id top, String key, obj_id speaker, obj_id corpse, float delay) throws InterruptedException
    {
        dictionary event = trial.getSessionDict(top, encounter.VAR_PREFIX);
        event.put("key", key);
        event.put("speaker", speaker);
        event.put("corpse", corpse);
        messageTo(top, "deliverDialogue", event, delay, false);
    }

    public static void deliver(obj_id top, dictionary event) throws InterruptedException
    {
        String key = event.getString("key");
        // Message delivery may be late, or shade creation may finish after a release
        // was queued. Enforce spacing at delivery as well as reservation time.
        float now = getGameTime();
        float earliest = now;
        if (!key.startsWith("opening_") && hasObjVar(top, OPENING_END))
        {
            earliest = Math.max(earliest, getFloatObjVar(top, OPENING_END));
        }
        if (hasObjVar(top, LAST))
        {
            earliest = Math.max(earliest, getFloatObjVar(top, LAST) + 3.0f);
        }
        if (earliest > now)
        {
            messageTo(top, "deliverDialogue", event, earliest - now, false);
            return;
        }
        obj_id speaker = event.getObjId("speaker");
        // Resolve Axkva at delivery: a queued line may cross her release boundary.
        if (!isIdValid(speaker))
        {
            speaker = (hasObjVar(top, entrance.ARRIVED) || hasObjVar(top, encounter.VAR_AXKVA_RELEASED))
                    ? getObjIdObjVar(top, encounter.VAR_ACTORS + ".axkva_min")
                    : getObjIdObjVar(top, ESSENCE);
        }
        if (!isIdValid(speaker) || !exists(speaker) || ai_lib.isDead(speaker))
        {
            return;
        }
        setObjVar(top, LAST, now);
        if ("opening_emote".equals(key))
        {
            chat.think(speaker, new string_id(TABLE, key));
            obj_id[] players = trial.getPlayersInDungeon(top);
            if (players != null)
            {
                for (obj_id player : players)
                {
                    if (isIdValid(player) && exists(player))
                    {
                        sendSystemMessage(player, new string_id(TABLE, "opening_narration"));
                    }
                }
            }
            return;
        }
        obj_id corpse = event.getObjId("corpse");
        if (isIdValid(corpse) && exists(corpse) && trial.getTop(corpse) == top && ai_lib.isDead(corpse))
        {
            attacker_results attacker = sequencer.makeDummyAttackerResults(speaker);
            defender_results[] defenders = sequencer.makeDummyDefenderResults(corpse);
            // The generic dummy helper assumes an upright defender. Preserve the corpse.
            defenders[0].endPosture = getPosture(corpse);
            doCombatResults("force_drain_1", attacker, defenders);
        }
        chat.chat(speaker, chat.CHAT_SAY, new string_id(TABLE, key));
        entrance.spoken(top, key);
    }
}
