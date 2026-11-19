package plugin.clashofclans.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The current clan war exactly as the server describes it. */
public final class ClanWar {

    public static final String NOT_IN_WAR = "notInWar";
    public static final String PREPARATION = "preparation";
    public static final String IN_WAR = "inWar";
    public static final String WAR_ENDED = "warEnded";

    /** One side of the war: our clan or the opponent. */
    public static final class Side {
        public final String tag;
        public final String name;
        public final int level;
        public final int stars;
        public final double destruction;
        public final int attacks;
        public final int members;

        public Side(String tag, String name, int level, int stars, double destruction,
                    int attacks, int members) {
            this.tag = tag;
            this.name = name;
            this.level = level;
            this.stars = stars;
            this.destruction = destruction;
            this.attacks = attacks;
            this.members = members;
        }
    }

    /** One recorded attack inside the war. */
    public static final class Attack {
        public final String attackerTag;
        public final String attackerName;
        public final String defenderTag;
        public final int stars;
        public final double destruction;
        public final int order;

        public Attack(String attackerTag, String attackerName, String defenderTag,
                      int stars, double destruction, int order) {
            this.attackerTag = attackerTag;
            this.attackerName = attackerName;
            this.defenderTag = defenderTag;
            this.stars = stars;
            this.destruction = destruction;
            this.order = order;
        }
    }

    public final String state;
    public final long preparationStart;
    public final long start;
    public final long end;
    public final int teamSize;
    public final int attacksPerMember;
    public final Side clan;
    public final Side opponent;
    public final List<Attack> attacks;

    public ClanWar(String state, long preparationStart, long start, long end, int teamSize,
                   int attacksPerMember, Side clan, Side opponent, List<Attack> attacks) {
        this.state = state == null || state.isEmpty() ? NOT_IN_WAR : state;
        this.preparationStart = preparationStart;
        this.start = start;
        this.end = end;
        this.teamSize = Math.max(0, teamSize);
        this.attacksPerMember = Math.max(1, attacksPerMember);
        this.clan = clan;
        this.opponent = opponent;
        this.attacks = Collections.unmodifiableList(new ArrayList<>(attacks));
    }

    public boolean inWar() {
        return !NOT_IN_WAR.equals(state);
    }

    /** "Preparation", "Battle day", "War ended" or "Not in war". */
    public String stateName() {
        switch (state) {
            case PREPARATION: return "Preparation";
            case IN_WAR: return "Battle day";
            case WAR_ENDED: return "War ended";
            default: return "Not in war";
        }
    }

    public long millisLeftAt(long now) {
        switch (state) {
            case PREPARATION: return Math.max(0, start - now);
            case IN_WAR: return Math.max(0, end - now);
            default: return 0;
        }
    }

    /** Total attacks the war allows on our side. */
    public int totalAttacks() {
        return teamSize * attacksPerMember;
    }

    public int attacksLeft() {
        return Math.max(0, totalAttacks() - (clan == null ? 0 : clan.attacks));
    }

    public String result() {
        if (!WAR_ENDED.equals(state) || clan == null || opponent == null) return "";
        if (clan.stars > opponent.stars) return "Victory";
        if (clan.stars < opponent.stars) return "Defeat";
        if (clan.destruction > opponent.destruction) return "Victory";
        if (clan.destruction < opponent.destruction) return "Defeat";
        return "Draw";
    }
}
