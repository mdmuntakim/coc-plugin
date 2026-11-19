package plugin.clashofclans.model;

/**
 * Cost and duration of the next level of a building or troop.
 *
 * <p>The tables in {@link Catalog} carry a level-one base value; every further
 * level is an exponential step. That keeps the catalogue small while still
 * producing believable planner numbers.
 */
public final class Upgrade {
    public final long cost;
    public final long seconds;

    public Upgrade(long cost, long seconds) {
        this.cost = cost;
        this.seconds = seconds;
    }

    public long millis() {
        return seconds * 1000L;
    }

    public static Upgrade forBuilding(Building b, int currentLevel) {
        return of(b.baseCost, b.baseHours, currentLevel);
    }

    public static Upgrade forTroop(Troop t, int currentLevel) {
        // Laboratory research is slower than a builder upgrade.
        return of(t.baseCost, t.baseHours, currentLevel);
    }

    static Upgrade of(long baseCost, double baseHours, int currentLevel) {
        int level = Math.max(1, currentLevel);
        double cost = baseCost * Math.pow(1.42, level - 1);
        double hours = baseHours * Math.pow(1.21, level - 1);
        long roundedCost = roundTo(100, cost);
        long seconds = Math.max(60L, Math.round(hours * 3600d));
        return new Upgrade(roundedCost, seconds);
    }

    static long roundTo(long step, double value) {
        if (value <= 0) return step;
        return Math.max(step, (Math.round(value / step)) * step);
    }
}
