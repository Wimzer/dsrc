package script.library;

import script.obj_id;

public class holocron_introduction extends script.base_script
{
    public holocron_introduction()
    {
    }

    public static final String INTRO_SHOWN = "holocron.introShown";
    public static final String AUTO_REQUESTED = "holocron.autoRequested";
    public static final String STATION = "object/building/general/npe_space_station.iff";
    public static final String ACTIVATED_PREFIX = "holocronActivated:";
    public static final String PLAY_PREFIX = "holocronIntro:";

    public static boolean canRequestAutomatic(obj_id player) throws InterruptedException
    {
        return isIdValid(player) && exists(player) && isPlayer(player) && isPlayerConnected(player) &&
            !(hasObjVar(player, AUTO_REQUESTED) && getIntObjVar(player, AUTO_REQUESTED) > 0) &&
            !(hasObjVar(player, INTRO_SHOWN) && getIntObjVar(player, INTRO_SHOWN) > 0);
    }

    public static boolean requestAutomatic(obj_id player, obj_id station) throws InterruptedException
    {
        if (!canRequestAutomatic(player) || !isIdValid(station) || !exists(station) ||
            getTopMostContainer(player) != station || !STATION.equals(getTemplateName(station)))
        {
            return false;
        }
        if (!openHolocronToPage(player, "Root"))
        {
            return false;
        }
        // Count the request, not a visible-opening acknowledgement. Keep audio's
        // separate record untouched so this request cannot silence the first intro.
        setObjVar(player, AUTO_REQUESTED, 1);
        return true;
    }

    // This response is emitted by the Holocron mediator only after a usable page
    // is visible. Legacy openHolocron responses merely acknowledge a request.
    public static boolean handleActivation(obj_id player, String action) throws InterruptedException
    {
        if (action == null || !action.startsWith(ACTIVATED_PREFIX))
        {
            return false;
        }
        int openingId = getOpeningId(action);
        if (openingId == 0 || !isIdValid(player) || !isPlayer(player))
        {
            return true;
        }
        if (hasObjVar(player, INTRO_SHOWN) && getIntObjVar(player, INTRO_SHOWN) > 0)
        {
            return true;
        }

        // Seeing and immediately closing the window still counts. The client
        // binds this audio authorization to this opening and discards stale replies.
        // Do not clear this record when the temporary npe objvars are removed.
        setObjVar(player, INTRO_SHOWN, 1);
        newbieTutorialRequest(player, PLAY_PREFIX + openingId);
        return true;
    }

    public static int getOpeningId(String action)
    {
        if (action == null || !action.startsWith(ACTIVATED_PREFIX))
        {
            return 0;
        }
        int offset = ACTIVATED_PREFIX.length();
        int digits = action.length() - offset;
        if (digits < 1 || digits > 10 || action.charAt(offset) == '0')
        {
            return 0;
        }
        int result = 0;
        for (int i = offset; i < action.length(); ++i)
        {
            char digit = action.charAt(i);
            if (digit < '0' || digit > '9')
            {
                return 0;
            }
            int value = digit - '0';
            if (result > (2147483647 - value) / 10)
            {
                return 0;
            }
            result = result * 10 + value;
        }
        return result;
    }
}
