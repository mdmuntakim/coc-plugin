package plugin.clashofclans.model;

/** One laboratory troop. Pure model (no Android imports). */
public final class Troop {
    public final String id;
    public final String name;
    public final String house;
    public final int maxLevel;
    public final long baseCost;
    public final boolean dark;
    public final double baseHours;

    public Troop(String id, String name, String house, int maxLevel,
                 long baseCost, boolean dark, double baseHours) {
        this.id = id;
        this.name = name;
        this.house = house;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
        this.dark = dark;
        this.baseHours = baseHours;
    }

    public String currencyName() {
        return dark ? "Dark Elixir" : "Elixir";
    }
}
