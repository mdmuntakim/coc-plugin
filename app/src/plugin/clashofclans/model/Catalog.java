package plugin.clashofclans.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The catalogue of buildings and troops the planner ships with. */
public final class Catalog {

    private Catalog() {}

    public static final int MAX_TOWN_HALL = 17;

    public static final List<Building> BUILDINGS;
    public static final List<Troop> TROOPS;

    static {
        List<Building> b = new ArrayList<>();
        b.add(new Building("cannon", "Cannon", "Defense", 21, 1_200, Building.GOLD, 1.0));
        b.add(new Building("archer_tower", "Archer Tower", "Defense", 21, 1_500, Building.GOLD, 1.2));
        b.add(new Building("mortar", "Mortar", "Defense", 16, 2_000, Building.GOLD, 1.5));
        b.add(new Building("air_defense", "Air Defense", "Defense", 15, 3_000, Building.GOLD, 2.0));
        b.add(new Building("wizard_tower", "Wizard Tower", "Defense", 16, 5_000, Building.GOLD, 3.0));
        b.add(new Building("x_bow", "X-Bow", "Defense", 12, 40_000, Building.GOLD, 8.0));
        b.add(new Building("inferno_tower", "Inferno Tower", "Defense", 10, 80_000, Building.GOLD, 12.0));
        b.add(new Building("eagle_artillery", "Eagle Artillery", "Defense", 6, 200_000, Building.GOLD, 24.0));
        b.add(new Building("scattershot", "Scattershot", "Defense", 5, 300_000, Building.GOLD, 36.0));
        b.add(new Building("monolith", "Monolith", "Defense", 3, 500_000, Building.GOLD, 48.0));
        b.add(new Building("spell_tower", "Spell Tower", "Defense", 5, 60_000, Building.GOLD, 10.0));
        b.add(new Building("clan_castle", "Clan Castle", "Utility", 13, 10_000, Building.GOLD, 6.0));
        b.add(new Building("wall", "Wall Segment", "Utility", 18, 500, Building.GOLD, 0.2));
        b.add(new Building("gold_mine", "Gold Mine", "Resource", 16, 1_000, Building.ELIXIR, 0.8));
        b.add(new Building("elixir_collector", "Elixir Collector", "Resource", 16, 1_000, Building.GOLD, 0.8));
        b.add(new Building("dark_elixir_drill", "Dark Elixir Drill", "Resource", 10, 20_000, Building.ELIXIR, 6.0));
        BUILDINGS = Collections.unmodifiableList(b);

        List<Troop> t = new ArrayList<>();
        t.add(new Troop("barbarian", "Barbarian", "Barracks", 13, 200, false, 0.15));
        t.add(new Troop("archer", "Archer", "Barracks", 13, 300, false, 0.18));
        t.add(new Troop("giant", "Giant", "Barracks", 13, 600, false, 0.30));
        t.add(new Troop("goblin", "Goblin", "Barracks", 10, 400, false, 0.22));
        t.add(new Troop("wall_breaker", "Wall Breaker", "Barracks", 13, 500, false, 0.25));
        t.add(new Troop("balloon", "Balloon", "Barracks", 12, 900, false, 0.35));
        t.add(new Troop("wizard", "Wizard", "Barracks", 13, 1_500, false, 0.50));
        t.add(new Troop("dragon", "Dragon", "Barracks", 11, 5_000, false, 0.90));
        t.add(new Troop("pekka", "P.E.K.K.A", "Barracks", 11, 6_000, false, 1.10));
        t.add(new Troop("baby_dragon", "Baby Dragon", "Barracks", 10, 4_000, false, 0.80));
        t.add(new Troop("electro_dragon", "Electro Dragon", "Barracks", 8, 12_000, false, 1.60));
        t.add(new Troop("dragon_rider", "Dragon Rider", "Barracks", 5, 20_000, false, 2.20));
        t.add(new Troop("electro_titan", "Electro Titan", "Barracks", 5, 24_000, false, 2.40));
        t.add(new Troop("root_rider", "Root Rider", "Barracks", 5, 28_000, false, 2.80));
        t.add(new Troop("minion", "Minion", "Dark Barracks", 13, 300, true, 0.18));
        t.add(new Troop("hog_rider", "Hog Rider", "Dark Barracks", 13, 800, true, 0.30));
        t.add(new Troop("valkyrie", "Valkyrie", "Dark Barracks", 11, 1_200, true, 0.45));
        t.add(new Troop("golem", "Golem", "Dark Barracks", 12, 2_000, true, 0.70));
        t.add(new Troop("witch", "Witch", "Dark Barracks", 10, 3_000, true, 0.90));
        t.add(new Troop("lava_hound", "Lava Hound", "Dark Barracks", 10, 6_000, true, 1.20));
        t.add(new Troop("bowler", "Bowler", "Dark Barracks", 8, 5_000, true, 1.00));
        t.add(new Troop("headhunter", "Headhunter", "Dark Barracks", 4, 9_000, true, 1.30));
        TROOPS = Collections.unmodifiableList(t);
    }

    public static Building building(String id) {
        for (Building b : BUILDINGS) {
            if (b.id.equals(id)) return b;
        }
        return null;
    }

    public static Troop troop(String id) {
        for (Troop t : TROOPS) {
            if (t.id.equals(id)) return t;
        }
        return null;
    }

    /** A believable starting level for a village of the given Town Hall. */
    public static int defaultLevel(Building b, int townHall) {
        double share = Math.max(0.05, Math.min(1.0, townHall / (double) MAX_TOWN_HALL));
        int level = (int) Math.round(b.maxLevel * Math.pow(share, 1.15));
        if (level < 1) level = 1;
        return Math.min(b.maxLevel, level);
    }

    public static int defaultLevel(Troop t, int townHall) {
        double share = Math.max(0.05, Math.min(1.0, townHall / (double) MAX_TOWN_HALL));
        int level = (int) Math.round(t.maxLevel * share);
        if (level < 1) level = 1;
        return Math.min(t.maxLevel, level);
    }

    /** Total gold-value of every remaining upgrade in the village. */
    public static long costToMax(java.util.Map<String, Integer> levels) {
        long total = 0;
        for (Building b : BUILDINGS) {
            int level = levels.containsKey(b.id) ? levels.get(b.id) : 1;
            for (int l = level; l < b.maxLevel; l++) {
                total += Upgrade.forBuilding(b, l).cost;
            }
        }
        return total;
    }

    /** Total build-time of every remaining upgrade in the village, in seconds. */
    public static long secondsToMax(java.util.Map<String, Integer> levels) {
        long total = 0;
        for (Building b : BUILDINGS) {
            int level = levels.containsKey(b.id) ? levels.get(b.id) : 1;
            for (int l = level; l < b.maxLevel; l++) {
                total += Upgrade.forBuilding(b, l).seconds;
            }
        }
        return total;
    }
}
