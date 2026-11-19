package plugin.clashofclans.model;

/** Experience level curve of an account. Pure model. */
public final class Xp {

    private Xp() {}

    /** Experience needed to advance from {@code level} to {@code level + 1}. */
    public static long toNext(int level) {
        int l = Math.max(1, level);
        return 200L + 50L * (l - 1) * l / 2L;
    }

    /** Cumulative experience at the start of {@code level}. */
    public static long atStart(int level) {
        long total = 0;
        for (int l = 1; l < level; l++) total += toNext(l);
        return total;
    }

    public static int levelForXp(long xp) {
        long remaining = Math.max(0, xp);
        int level = 1;
        while (level < 500) {
            long need = toNext(level);
            if (remaining < need) break;
            remaining -= need;
            level++;
        }
        return level;
    }

    /** 0..1 progress through the current level. */
    public static float progress(long xp) {
        long start = atStart(levelForXp(xp));
        long need = toNext(levelForXp(xp));
        if (need <= 0) return 1f;
        float p = (xp - start) / (float) need;
        return Math.max(0f, Math.min(1f, p));
    }

    public static long remaining(long xp) {
        int level = levelForXp(xp);
        return Math.max(0, atStart(level) + toNext(level) - Math.max(0, xp));
    }
}
