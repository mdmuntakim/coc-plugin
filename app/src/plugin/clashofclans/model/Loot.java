package plugin.clashofclans.model;

/** Loot arithmetic used by the tools screen. Pure model. */
public final class Loot {

    private Loot() {}

    /** Loot from one attack on a base holding {@code available} of a resource. */
    public static long fromAttack(long available, int destructionPercent) {
        int pct = Math.max(0, Math.min(100, destructionPercent));
        return available * pct / 100L;
    }

    /** Loot from {@code attacks} attacks, each taking {@code percent} of a base. */
    public static long fromRaids(long perBase, int destructionPercent, int attacks) {
        return fromAttack(perBase, destructionPercent) * Math.max(0, attacks);
    }

    /** Whole raids needed to reach {@code target}, rounded up. */
    public static long raidsNeeded(long target, long perRaid) {
        if (target <= 0) return 0;
        if (perRaid <= 0) return -1;
        return (target + perRaid - 1) / perRaid;
    }

    /** Builder-time lost when {@code builders} work in parallel is already in the total. */
    public static long parallelSeconds(long totalSeconds, int builders) {
        if (builders <= 0) return totalSeconds;
        return (totalSeconds + builders - 1) / builders;
    }
}
