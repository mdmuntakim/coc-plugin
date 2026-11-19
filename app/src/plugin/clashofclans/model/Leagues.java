package plugin.clashofclans.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Trophy ladder of the home village. Pure model. */
public final class Leagues {

    public static final class League {
        public final String name;
        public final int minTrophies;
        public final int color; // ARGB, rendered by the UI layer

        League(String name, int minTrophies, int color) {
            this.name = name;
            this.minTrophies = minTrophies;
            this.color = color;
        }
    }

    public static final List<League> LADDER;

    static {
        List<League> l = new ArrayList<>();
        l.add(new League("Unranked", 0, 0xFF8C97A3));
        l.add(new League("Bronze III", 400, 0xFFB0794A));
        l.add(new League("Bronze II", 500, 0xFFB0794A));
        l.add(new League("Bronze I", 600, 0xFFB0794A));
        l.add(new League("Silver III", 800, 0xFFC3CBD3));
        l.add(new League("Silver II", 900, 0xFFC3CBD3));
        l.add(new League("Silver I", 1000, 0xFFC3CBD3));
        l.add(new League("Gold III", 1200, 0xFFF0C24B));
        l.add(new League("Gold II", 1300, 0xFFF0C24B));
        l.add(new League("Gold I", 1400, 0xFFF0C24B));
        l.add(new League("Crystal III", 1600, 0xFF7FD6F0));
        l.add(new League("Crystal II", 1700, 0xFF7FD6F0));
        l.add(new League("Crystal I", 1800, 0xFF7FD6F0));
        l.add(new League("Master III", 2000, 0xFFC08BF0));
        l.add(new League("Master II", 2100, 0xFFC08BF0));
        l.add(new League("Master I", 2200, 0xFFC08BF0));
        l.add(new League("Champion III", 2400, 0xFFF08B6E));
        l.add(new League("Champion II", 2500, 0xFFF08B6E));
        l.add(new League("Champion I", 2600, 0xFFF08B6E));
        l.add(new League("Titan III", 2800, 0xFF8BE0B0));
        l.add(new League("Titan II", 2900, 0xFF8BE0B0));
        l.add(new League("Titan I", 3000, 0xFF8BE0B0));
        l.add(new League("Legend", 5000, 0xFFFFD966));
        LADDER = Collections.unmodifiableList(l);
    }

    private Leagues() {}

    public static League forTrophies(int trophies) {
        League found = LADDER.get(0);
        for (League l : LADDER) {
            if (trophies >= l.minTrophies) found = l;
        }
        return found;
    }

    /** The next league above the current trophy count, or null at Legend. */
    public static League next(int trophies) {
        for (League l : LADDER) {
            if (trophies < l.minTrophies) return l;
        }
        return null;
    }

    /** How far the player is through the current league, 0..1. */
    public static float progress(int trophies) {
        League current = forTrophies(trophies);
        League next = next(trophies);
        if (next == null) return 1f;
        int span = next.minTrophies - current.minTrophies;
        if (span <= 0) return 1f;
        float p = (trophies - current.minTrophies) / (float) span;
        return Math.max(0f, Math.min(1f, p));
    }
}
