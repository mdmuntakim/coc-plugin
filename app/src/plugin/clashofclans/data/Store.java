package plugin.clashofclans.data;

import android.content.Context;
import android.content.SharedPreferences;

import plugin.clashofclans.model.Building;
import plugin.clashofclans.model.Catalog;
import plugin.clashofclans.model.Clan;
import plugin.clashofclans.model.ClanWar;
import plugin.clashofclans.model.CocJson;
import plugin.clashofclans.model.Player;
import plugin.clashofclans.model.Upgrade;
import plugin.clashofclans.model.WarLog;
import plugin.clashofclans.net.CocApi;

/**
 * On-device state of the app.
 *
 * <p>Two clearly separated halves:
 * <ul>
 *   <li><b>Live account</b> - the connection settings and the last reply of
 *       the official API, cached verbatim so the app can show the real
 *       account while offline. Nothing here is invented.</li>
 *   <li><b>Planner</b> - the local building levels and upgrade reminders. The
 *       API has no endpoint for building levels, so the chief types them once
 *       and the planner does the arithmetic.</li>
 * </ul>
 */
public final class Store {

    public static final int BUILDERS = 5;

    private static final String PREFS = "cocplugin.state";
    private static final String LEGACY = "cocplugin.profile";

    private final SharedPreferences p;
    private Live cache;

    public Store(Context c) {
        p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        // The 1.0 build seeded a fictional chief into another prefs file.
        // Drop it once so no invented number can come back through an upgrade.
        SharedPreferences legacy = c.getSharedPreferences(LEGACY, Context.MODE_PRIVATE);
        if (legacy.getBoolean("seeded", false)) {
            legacy.edit().clear().apply();
        }
        settle();
    }

    // -------------------------------------------------------- connection
    public String token() { return s("api.token", ""); }
    public void setToken(String v) { p.edit().putString("api.token", v == null ? "" : v.trim()).apply(); }

    public String baseUrl() { return s("api.base", CocApi.OFFICIAL_BASE); }
    public void setBaseUrl(String v) {
        String base = v == null ? "" : v.trim();
        if (base.isEmpty()) base = CocApi.OFFICIAL_BASE;
        p.edit().putString("api.base", base).apply();
    }

    public String tag() { return s("player.tag", ""); }
    public void setTag(String v) { p.edit().putString("player.tag", v == null ? "" : v.trim()).apply(); }

    public CocApi api() {
        return new CocApi(baseUrl(), token());
    }

    /** A tag, a token and a reachable base: all three are needed to sync. */
    public boolean configured() {
        return !tag().isEmpty() && !token().isEmpty();
    }

    public String host() {
        try {
            return new java.net.URL(baseUrl()).getHost();
        } catch (Exception e) {
            return baseUrl();
        }
    }

    public void disconnect() {
        p.edit()
                .remove("api.token")
                .remove("player.tag")
                .remove("live.player")
                .remove("live.clan")
                .remove("live.war")
                .remove("live.warlog")
                .remove("live.at")
                .remove("live.host")
                .remove("live.warning")
                .remove("live.error")
                .apply();
        cache = null;
    }

    // -------------------------------------------------------- live cache
    public Live live() {
        if (cache != null) return cache;
        String playerJson = s("live.player", "");
        if (playerJson.isEmpty()) {
            // No snapshot yet: keep the last failure so the first sync can
            // explain itself instead of leaving a blank card behind.
            String error = s("live.error", "");
            cache = new Live(null, null, null, null, 0, s("live.host", ""),
                    error.isEmpty() ? null : error, null);
            return cache;
        }
        Player player = null;
        Clan clan = null;
        ClanWar war = null;
        WarLog log = null;
        try {
            player = CocJson.player(playerJson);
        } catch (RuntimeException ignored) {
        }
        try {
            String json = s("live.clan", "");
            if (!json.isEmpty()) clan = CocJson.clan(json);
        } catch (RuntimeException ignored) {
        }
        try {
            String json = s("live.war", "");
            if (!json.isEmpty()) war = CocJson.war(json);
        } catch (RuntimeException ignored) {
        }
        try {
            String json = s("live.warlog", "");
            if (!json.isEmpty()) log = CocJson.warLog(json);
        } catch (RuntimeException ignored) {
        }
        cache = new Live(player, clan, war, log,
                p.getLong("live.at", 0), s("live.host", ""), null, s("live.warning", ""));
        return cache;
    }

    /**
     * Stores exactly what the server answered. {@code null} documents are left
     * out instead of being replaced by an empty object.
     */
    public void saveLive(String playerJson, String clanJson, String warJson, String logJson,
                         String host, long fetchedAt, String warning) {
        SharedPreferences.Editor e = p.edit()
                .putString("live.player", playerJson)
                .putLong("live.at", fetchedAt)
                .putString("live.host", host == null ? "" : host)
                .putString("live.warning", warning == null ? "" : warning)
                .putString("live.error", "");
        putOrRemove(e, "live.clan", clanJson);
        putOrRemove(e, "live.war", warJson);
        putOrRemove(e, "live.warlog", logJson);
        e.apply();
        cache = null;
    }

    public void clearLive() {
        p.edit()
                .remove("live.player")
                .remove("live.clan")
                .remove("live.war")
                .remove("live.warlog")
                .remove("live.at")
                .remove("live.host")
                .remove("live.warning")
                .apply();
        cache = null;
    }

    public long lastSync() { return p.getLong("live.at", 0); }

    /** The last failed refresh, cleared by the next good one. */
    public String lastError() { return s("live.error", ""); }

    public void setLastError(String message) {
        p.edit().putString("live.error", message == null ? "" : message).apply();
        cache = null;
    }

    private static void putOrRemove(SharedPreferences.Editor e, String key, String value) {
        if (value == null || value.isEmpty()) e.remove(key);
        else e.putString(key, value);
    }

    // ------------------------------------------------------------ profile
    public int townHall() {
        Player player = live().player;
        if (player != null && player.townHallLevel > 0) return player.townHallLevel;
        return Math.max(0, Math.min(Catalog.MAX_TOWN_HALL, p.getInt("planner.th", 0)));
    }

    public void setTownHall(int v) {
        p.edit().putInt("planner.th", Math.max(0, Math.min(Catalog.MAX_TOWN_HALL, v))).apply();
    }

    // ------------------------------------------------------------- village
    public int buildingLevel(Building b) {
        return Math.max(1, Math.min(b.maxLevel, p.getInt("b." + b.id, 1)));
    }

    public void setBuildingLevel(Building b, int level) {
        p.edit().putInt("b." + b.id, Math.max(1, Math.min(b.maxLevel, level))).apply();
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

    // ------------------------------------------------------------ settings
    public int theme() { return p.getInt("theme", 0); }
    public void setTheme(int i) { p.edit().putInt("theme", Math.max(0, Math.min(2, i))).apply(); }

    /** Clears the planner, keeps the connection. */
    public void resetPlanner() {
        SharedPreferences.Editor e = p.edit();
        e.putInt("planner.th", 0);
        for (Building b : Catalog.BUILDINGS) {
            e.remove("b." + b.id);
            e.remove("t." + b.id + ".start");
            e.remove("t." + b.id + ".end");
            e.remove("t." + b.id + ".lv");
        }
        e.apply();
    }

    // ------------------------------------------------------------- timers
    /** Applies every planner upgrade whose timer has run out. */
    public void settle() {
        long now = System.currentTimeMillis();
        SharedPreferences.Editor e = p.edit();
        boolean changed = false;
        for (Building b : Catalog.BUILDINGS) {
            long end = p.getLong("t." + b.id + ".end", 0);
            if (end > 0 && end <= now) {
                int target = Math.max(1, Math.min(b.maxLevel,
                        p.getInt("t." + b.id + ".lv", buildingLevel(b) + 1)));
                e.putInt("b." + b.id, target);
                e.remove("t." + b.id + ".end");
                e.remove("t." + b.id + ".start");
                e.remove("t." + b.id + ".lv");
                changed = true;
            }
        }
        if (changed) e.apply();
    }

    private String s(String key, String fallback) {
        String v = p.getString(key, fallback);
        return v == null ? fallback : v;
    }
}
