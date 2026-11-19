package plugin.clashofclans.model;

/**
 * One upgradeable village building.
 *
 * <p>Pure model: no Android imports, so the planner maths can be exercised on a
 * plain JVM by {@code tests/run.sh}.
 */
public final class Building {
    public static final int GOLD = 0;
    public static final int ELIXIR = 1;
    public static final int DARK_ELIXIR = 2;

    public final String id;
    public final String name;
    public final String category;
    public final int maxLevel;
    public final long baseCost;
    public final int currency;
    public final double baseHours;

    public Building(String id, String name, String category, int maxLevel,
                    long baseCost, int currency, double baseHours) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
        this.currency = currency;
        this.baseHours = baseHours;
    }

    public String currencyName() {
        switch (currency) {
            case ELIXIR: return "Elixir";
            case DARK_ELIXIR: return "Dark Elixir";
            default: return "Gold";
        }
    }

    /** Coarse grouping used by the village screen. */
    public boolean isDefense() {
        return "Defense".equals(category) || "Trap".equals(category);
    }
}
