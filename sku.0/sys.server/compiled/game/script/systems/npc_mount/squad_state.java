package script.systems.npc_mount;

// Pure state/bit helpers for the event-driven three-NPC squad.  Spawn ownership,
// native movement, persistence and script messaging remain in their existing
// owners; this class deliberately has no engine calls or mutable static state.
public final class squad_state
{
    public static final int ROLE_INVALID = -1;
    public static final int ROLE_LEADER = 0;
    public static final int ROLE_ESCORT_1 = 1;
    public static final int ROLE_ESCORT_2 = 2;
    public static final int ROLE_COUNT = 3;
    public static final int ALL_MEMBERS = 0x07;

    public static final int ASSEMBLING = 0;
    public static final int DEPARTING = 1;
    public static final int PATROLLING = 2;
    public static final int COMBAT = 3;
    public static final int REGROUPING = 4;

    // script_class_loader constructs script-package helpers reflectively, even
    // when their behavior is entirely static. Keep its zero-argument entry public.
    public squad_state()
    {
    }

    public static int bit(int role)
    {
        return role >= ROLE_LEADER && role < ROLE_COUNT ? (1 << role) : 0;
    }

    public static boolean validRole(int role)
    {
        return role >= ROLE_LEADER && role < ROLE_COUNT;
    }

    public static boolean complete(int mask)
    {
        return (mask & ALL_MEMBERS) == ALL_MEMBERS;
    }

    public static boolean contains(int mask, int role)
    {
        int roleBit = bit(role);
        return roleBit != 0 && (mask & roleBit) != 0;
    }

    public static int add(int mask, int role)
    {
        return mask | bit(role);
    }

    public static int remove(int mask, int role)
    {
        return mask & ~bit(role);
    }

    public static boolean canDepart(int state, int occupiedMask, int readyMask, int assembledMask,
            boolean mountedLeader, boolean continuingOnFoot)
    {
        return state == ASSEMBLING && complete(occupiedMask) && complete(readyMask)
                && complete(assembledMask) && (mountedLeader || continuingOnFoot);
    }

    public static boolean recoveryComplete(int combatMask, int recoveryMask)
    {
        return (combatMask & ALL_MEMBERS) == 0 && (recoveryMask & ALL_MEMBERS) == 0;
    }

    public static boolean mayRestoreMovement(int state, int combatMask, int recoveryMask, boolean casualty)
    {
        return state == PATROLLING && !casualty && recoveryComplete(combatMask, recoveryMask);
    }
}
