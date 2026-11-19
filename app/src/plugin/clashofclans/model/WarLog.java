package plugin.clashofclans.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The public clan war log, newest first. */
public final class WarLog {

    /** One finished war. */
    public static final class Entry {
        public final String result;       // win, lose, draw
        public final long endTime;
        public final String opponentName;
        public final String opponentTag;
        public final int teamSize;
        public final int clanStars;
        public final int opponentStars;
        public final double clanDestruction;
        public final double opponentDestruction;

        public Entry(String result, long endTime, String opponentName, String opponentTag,
                     int teamSize, int clanStars, int opponentStars,
                     double clanDestruction, double opponentDestruction) {
            this.result = result == null ? "" : result;
            this.endTime = endTime;
            this.opponentName = opponentName;
            this.opponentTag = opponentTag;
            this.teamSize = Math.max(0, teamSize);
            this.clanStars = Math.max(0, clanStars);
            this.opponentStars = Math.max(0, opponentStars);
            this.clanDestruction = clanDestruction;
            this.opponentDestruction = opponentDestruction;
        }

        public boolean won() {
            return "win".equalsIgnoreCase(result);
        }
    }

    public final List<Entry> entries;

    public WarLog(List<Entry> entries) {
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public static WarLog empty() {
        return new WarLog(Collections.<Entry>emptyList());
    }

    public int wins() {
        int n = 0;
        for (Entry e : entries) if (e.won()) n++;
        return n;
    }

    public int losses() {
        int n = 0;
        for (Entry e : entries) if ("lose".equalsIgnoreCase(e.result)) n++;
        return n;
    }

    public int draws() {
        int n = 0;
        for (Entry e : entries) if ("draw".equalsIgnoreCase(e.result)) n++;
        return n;
    }

    public int played() {
        return entries.size();
    }

    public int winRate() {
        return played() == 0 ? 0 : Math.round(wins() * 100f / played());
    }
}
