package plugin.clashofclans.data;

import android.content.Context;
import android.content.SharedPreferences;

import plugin.clashofclans.model.Building;
import plugin.clashofclans.model.Catalog;
import plugin.clashofclans.model.Troop;
import plugin.clashofclans.model.Upgrade;
import plugin.clashofclans.model.War;
import plugin.clashofclans.model.Xp;

/**
 * The on-device profile of the plugin. Everything is local: the app never
 * talks to the game, it is a companion notebook for the numbers a chief checks
 * every day.
 */
public final class Store {

    public static final int BUILDERS = 5;
    private static final String PREFS = "cocplugin.profile";

    private final SharedPreferences p;

    public Store(Context c) {
        p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!p.getBoolean("seeded", false)) seed();
        settle();
    }

    private void seed() {
        SharedPreferences.Editor e = p.edit();
        e.putBoolean("seeded", true);
        e.putString("name", "Chief");
        e.putString("tag", "#2P0LYQ8GR");
        e.putInt("th", 12);
        e.putLong("xp", 4_800);
        e.putInt("trophies", 2_450);
        e.putInt("best", 2_680);
        e.putInt("builderTrophies", 2_100);
        e.putInt("warStars", 420);
        e.putInt("donations", 1_250);
        e.putInt("received", 980);
        e.putString("clanName", "Iron Vanguard");
        e.putString("clanTag", "#2YQ8L0PR");
        e.putInt("clanLevel", 9);
        e.putInt("members", 15);
        e.putLong("gold", 2_450_000);
        e.putLong("elixir", 1_800_000);
        e.putLong("dark", 120_000);
        e.putLong("gems", 640);
        e.putInt("th", 12);
        for (Building b : Catalog.BUILDINGS) {
            e.putInt("b." + b.id, Catalog.defaultLevel(b, 12));
        }
        for (Troop t : Catalog.TROOPS) {
            e.putInt("a." + t.id, Catalog.defaultLevel(t, 12));
        }
        e.putString("opponent", "Nightfall Legion");
        e.putInt("warState", War.STATE_BATTLE);
        e.putLong("warEnd", System.currentTimeMillis() + 5L * 3600L * 1000L);
        e.putInt("starsOur", 7);
        e.putInt("starsTheir", 4);
        e.putInt("attacksUsed", 9);
        e.putInt("warWins", 132);
        e.putInt("warLosses", 48);
        e.putInt("warDraws", 6);
        e.putInt("theme", 0);
        e.apply();
    }

    // ------------------------------------------------------------ profile
    public String name() { return s("name", "Chief"); }
    public void setName(String v) { p.edit().putString("name", clean(v, "Chief")).apply(); }

    public String tag() { return s("tag", "#2P0LYQ8GR"); }
    public void setTag(String v) { p.edit().putString("tag", clean(v, "#2P0LYQ8GR")).apply(); }

    public int th() { return Math.max(1, Math.min(Catalog.MAX_TOWN_HALL, p.getInt("th", 12))); }
    public void setTh(int v) { p.edit().putInt("th", Math.max(1, Math.min(Catalog.MAX_TOWN_HALL, v))).apply(); }

    public long xp() { return Math.max(0, p.getLong("xp", 4_800)); }
    public void setXp(long v) { p.edit().putLong("xp", Math.max(0, v)).apply(); }
    public int level() { return Xp.levelForXp(xp()); }

    public int trophies() { return p.getInt("trophies", 2_450); }
    public void setTrophies(int v) { p.edit().putInt("trophies", Math.max(0, v)).apply(); }

    public int bestTrophies() { return p.getInt("best", 2_680); }
    public void setBestTrophies(int v) { p.edit().putInt("best", Math.max(0, v)).apply(); }

    public int builderTrophies() { return p.getInt("builderTrophies", 2_100); }
    public void setBuilderTrophies(int v) { p.edit().putInt("builderTrophies", Math.max(0, v)).apply(); }

    public int warStars() { return p.getInt("warStars", 420); }
    public void setWarStars(int v) { p.edit().putInt("warStars", Math.max(0, v)).apply(); }

    public int donations() { return p.getInt("donations", 1_250); }
    public void setDonations(int v) { p.edit().putInt("donations", Math.max(0, v)).apply(); }

    public int received() { return p.getInt("received", 980); }
    public void setReceived(int v) { p.edit().putInt("received", Math.max(0, v)).apply(); }

    public String clanName() { return s("clanName", "Iron Vanguard"); }
    public String clanTag() { return s("clanTag", "#2YQ8L0PR"); }
    public void setClan(String name, String tag, int level) {
        p.edit().putString("clanName", clean(name, "Iron Vanguard"))
                .putString("clanTag", clean(tag, "#2YQ8L0PR"))
                .putInt("clanLevel", Math.max(1, level)).apply();
    }
    public int clanLevel() { return p.getInt("clanLevel", 9); }
    public int members() { return p.getInt("members", 15); }
    public void setMembers(int v) { p.edit().putInt("members", Math.max(1, Math.min(50, v))).apply(); }

    // ---------------------------------------------------------- resources
    public long gold() { return p.getLong("gold", 2_450_000); }
    public long elixir() { return p.getLong("elixir", 1_800_000); }
    public long dark() { return p.getLong("dark", 120_000); }
    public long gems() { return p.getLong("gems", 640); }

    public void addResources(long gold, long elixir, long dark) {
        p.edit()
                .putLong("gold", Math.max(0, this.gold() + gold))
                .putLong("elixir", Math.max(0, this.elixir() + elixir))
                .putLong("dark", Math.max(0, this.dark() + dark))
                .apply();
    }

    public void setGems(long v) { p.edit().putLong("gems", Math.max(0, v)).apply(); }

    // ------------------------------------------------------------- village
    public int buildingLevel(Building b) {
        return Math.max(1, Math.min(b.maxLevel, p.getInt("b." + b.id, 1)));
    }

    public boolean isUpgrading(Building b) {
        return p.getLong("t." + b.id + ".end", 0) > System.currentTimeMillis();
    }

    public long upgradeStart(Building b) { return p.getLong("t." + b.id + ".start", 0); }
    public long upgradeEnd(Building b) { return p.getLong("t." + b.id + ".end", 0); }
    public int upgradeTarget(Building b) { return p.getInt("t." + b.id + ".lv", buildingLevel(b)); }

    public boolean startUpgrade(Building b) {
        int level = buildingLevel(b);
        if (level >= b.maxLevel || isUpgrading(b) || buildersFree() <= 0) return false;
        Upgrade u = Upgrade.forBuilding(b, level);
        long now = System.currentTimeMillis();
        p.edit()
                .putInt("t." + b.id + ".lv", level + 1)
                .putLong("t." + b.id + ".start", now)
                .putLong("t." + b.id + ".end", now + u.millis())
                .apply();
        return true;
    }

    public int buildersBusy() {
        long now = System.currentTimeMillis();
        int busy = 0;
        for (Building b : Catalog.BUILDINGS) {
            if (p.getLong("t." + b.id + ".end", 0) > now) busy++;
        }
        return busy;
    }

    public int buildersFree() { return Math.max(0, BUILDERS - buildersBusy()); }

    public int maxedBuildings() {
        int n = 0;
        for (Building b : Catalog.BUILDINGS) {
            if (buildingLevel(b) >= b.maxLevel) n++;
        }
        return n;
    }

    // ---------------------------------------------------------------- army
    public int troopLevel(Troop t) {
        return Math.max(1, Math.min(t.maxLevel, p.getInt("a." + t.id, 1)));
    }

    public boolean labActive() { return p.getLong("lab.end", 0) > System.currentTimeMillis(); }
    public long labStart() { return p.getLong("lab.start", 0); }
    public long labEnd() { return p.getLong("lab.end", 0); }
    public String labTroop() { return s("lab.troop", ""); }
    public int labTarget() { return p.getInt("lab.lv", 0); }

    public Troop labTroopObject() {
        Troop t = Catalog.troop(labTroop());
        return t == null ? null : t;
    }

    public boolean startLab(Troop t) {
        int level = troopLevel(t);
        if (level >= t.maxLevel || labActive()) return false;
        Upgrade u = Upgrade.forTroop(t, level);
        long now = System.currentTimeMillis();
        p.edit()
                .putString("lab.troop", t.id)
                .putInt("lab.lv", level + 1)
                .putLong("lab.start", now)
                .putLong("lab.end", now + u.millis())
                .apply();
        return true;
    }

    // ----------------------------------------------------------------- war
    public String opponent() { return s("opponent", "Nightfall Legion"); }
    public int warState() { return p.getInt("warState", War.STATE_BATTLE); }
    public long warEnd() { return p.getLong("warEnd", System.currentTimeMillis()); }
    public int starsOur() { return p.getInt("starsOur", 7); }
    public int starsTheir() { return p.getInt("starsTheir", 4); }
    public int attacksUsed() { return p.getInt("attacksUsed", 9); }
    public int warWins() { return p.getInt("warWins", 132); }
    public int warLosses() { return p.getInt("warLosses", 48); }
    public int warDraws() { return p.getInt("warDraws", 6); }

    public void newWar(String opponent, int seconds) {
        p.edit()
                .putString("opponent", clean(opponent, "Unknown clan"))
                .putInt("warState", War.STATE_PREP)
                .putLong("warEnd", System.currentTimeMillis() + Math.max(60, seconds) * 1000L)
                .putInt("starsOur", 0)
                .putInt("starsTheir", 0)
                .putInt("attacksUsed", 0)
                .apply();
    }

    public void setWarPhase(int phase) {
        p.edit().putInt("warState", phase).apply();
    }

    public void addAttack(int stars) {
        p.edit()
                .putInt("starsOur", starsOur() + Math.max(0, Math.min(3, stars)))
                .putInt("attacksUsed", Math.min(members() * 2, attacksUsed() + 1))
                .apply();
    }

    public void setTheirStars(int stars) {
        p.edit().putInt("starsTheir", Math.max(0, stars)).apply();
    }

    public String finishWar() {
        String result = War.result(starsOur(), starsTheir());
        SharedPreferences.Editor e = p.edit().putInt("warState", War.STATE_ENDED);
        if ("Victory".equals(result)) e.putInt("warWins", warWins() + 1);
        else if ("Defeat".equals(result)) e.putInt("warLosses", warLosses() + 1);
        else e.putInt("warDraws", warDraws() + 1);
        e.apply();
        return result;
    }

    public int warPlayed() { return warWins() + warLosses() + warDraws(); }

    // ------------------------------------------------------------ settings
    public int theme() { return p.getInt("theme", 0); }
    public void setTheme(int i) { p.edit().putInt("theme", Math.max(0, Math.min(2, i))).apply(); }

    public void reset() {
        p.edit().clear().apply();
        seed();
        settle();
    }

    // ------------------------------------------------------------- timers
    /** Applies every upgrade whose timer has run out. Cheap, called on resume. */
    public void settle() {
        long now = System.currentTimeMillis();
        SharedPreferences.Editor e = p.edit();
        boolean changed = false;
        for (Building b : Catalog.BUILDINGS) {
            long end = p.getLong("t." + b.id + ".end", 0);
            if (end > 0 && end <= now) {
                int target = Math.max(1, Math.min(b.maxLevel, p.getInt("t." + b.id + ".lv", buildingLevel(b) + 1)));
                e.putInt("b." + b.id, target);
                e.remove("t." + b.id + ".end");
                e.remove("t." + b.id + ".start");
                e.remove("t." + b.id + ".lv");
                changed = true;
            }
        }
        long labEnd = p.getLong("lab.end", 0);
        if (labEnd > 0 && labEnd <= now) {
            Troop t = Catalog.troop(s("lab.troop", ""));
            int target = p.getInt("lab.lv", 0);
            if (t != null && target > 0) e.putInt("a." + t.id, Math.min(t.maxLevel, target));
            e.remove("lab.end");
            e.remove("lab.start");
            e.remove("lab.troop");
            e.remove("lab.lv");
            changed = true;
        }
        if (changed) e.apply();
    }

    private String s(String key, String fallback) {
        String v = p.getString(key, fallback);
        return v == null ? fallback : v;
    }

    private static String clean(String v, String fallback) {
        if (v == null) return fallback;
        String t = v.trim();
        return t.isEmpty() ? fallback : t;
    }
}
