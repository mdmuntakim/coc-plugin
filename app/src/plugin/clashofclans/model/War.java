package plugin.clashofclans.model;

/** Clan-war bookkeeping. Pure model. */
public final class War {

    public static final int STATE_PREP = 0;
    public static final int STATE_BATTLE = 1;
    public static final int STATE_ENDED = 2;

    private War() {}

    public static String stateName(int state) {
        switch (state) {
            case STATE_BATTLE: return "Battle day";
            case STATE_ENDED: return "War ended";
            default: return "Preparation";
        }
    }

    /** 3 stars beats 2 stars beats 1 star; a tie is a draw. */
    public static String result(int ours, int theirs) {
        if (ours > theirs) return "Victory";
        if (ours < theirs) return "Defeat";
        return "Draw";
    }

    /** 0..1 destruction estimate from the stars of a whole war. */
    public static float destruction(int stars, int maxStars) {
        if (maxStars <= 0) return 0f;
        return Math.max(0f, Math.min(1f, stars / (float) maxStars));
    }

    /** 0..1 destruction of a single attack, 0-3 stars. */
    public static float destruction(int stars) {
        return destruction(stars, 3);
    }

    /** Attacks left out of two per member. */
    public static int attacksLeft(int members, int used) {
        int total = Math.max(0, members) * 2;
        return Math.max(0, total - Math.max(0, used));
    }

    public static int starsForDestruction(int percent) {
        int p = Math.max(0, Math.min(100, percent));
        if (p >= 100) return 3;
        if (p >= 70) return 2;
        if (p >= 50) return 1;
        return 0;
    }
}
