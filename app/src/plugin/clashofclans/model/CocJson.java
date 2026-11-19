package plugin.clashofclans.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Turns the JSON documents of the official Clash of Clans API into the pure
 * model classes. Pure Java: the tests feed it recorded responses.
 */
public final class CocJson {

    private CocJson() {}

    // ------------------------------------------------------------- player
    public static Player player(String json) {
        return player(Json.asObject(Json.parse(json)));
    }

    public static Player player(Map<String, Object> root) {
        Map<String, Object> clan = Json.object(root, "clan");
        Map<String, Object> league = Json.object(root, "league");
        Map<String, Object> builderLeague = Json.object(root, "builderBaseLeague");

        List<Player.Unit> units = new ArrayList<>();
        units(units, Json.list(root, "troops"), "troop");
        units(units, Json.list(root, "heroes"), "hero");
        units(units, Json.list(root, "spells"), "spell");
        units(units, Json.list(root, "pets"), "pet");

        return new Player(
                Json.str(root, "tag"),
                Json.str(root, "name", "Unknown chief"),
                Json.str(root, "role"),
                Json.str(league, "name"),
                Json.str(builderLeague, "name"),
                Json.str(clan, "tag"),
                Json.str(clan, "name"),
                Json.integer(clan, "clanLevel"),
                Json.integer(root, "townHallLevel"),
                Json.integer(root, "townHallWeaponLevel"),
                Json.integer(root, "expLevel"),
                Json.integer(root, "trophies"),
                Json.integer(root, "bestTrophies"),
                Json.integer(root, "warStars"),
                Json.integer(root, "attackWins"),
                Json.integer(root, "attackLosses"),
                Json.integer(root, "defenseWins"),
                Json.integer(root, "defenseLosses"),
                Json.integer(root, "builderHallLevel"),
                Json.integer(root, "builderBaseTrophies"),
                Json.integer(root, "bestBuilderBaseTrophies"),
                Json.integer(root, "donations"),
                Json.integer(root, "donationsReceived"),
                Json.integer(root, "warWins"),
                Json.integer(root, "warTies"),
                Json.integer(root, "warLosses"),
                Json.number(root, "clanCapitalContributions", 0),
                units);
    }

    private static void units(List<Player.Unit> out, List<Object> rows, String kind) {
        for (Object row : rows) {
            Map<String, Object> m = Json.asObject(row);
            String name = Json.str(m, "name");
            if (name.isEmpty()) continue;
            out.add(new Player.Unit(
                    name,
                    kind,
                    Json.str(m, "village", "home"),
                    Json.integer(m, "level"),
                    Json.integer(m, "maxLevel")));
        }
    }

    // --------------------------------------------------------------- clan
    public static Clan clan(String json) {
        return clan(Json.asObject(Json.parse(json)));
    }

    public static Clan clan(Map<String, Object> root) {
        Map<String, Object> warLeague = Json.object(root, "warLeague");
        Map<String, Object> location = Json.object(root, "location");

        List<Clan.Member> members = new ArrayList<>();
        for (Object row : Json.list(root, "memberList")) {
            Map<String, Object> m = Json.asObject(row);
            members.add(new Clan.Member(
                    Json.str(m, "tag"),
                    Json.str(m, "name", "Member"),
                    Json.str(m, "role"),
                    Json.integer(m, "expLevel"),
                    Json.integer(m, "trophies"),
                    Json.integer(m, "donations"),
                    Json.integer(m, "donationsReceived"),
                    Json.integer(m, "clanRank")));
        }

        return new Clan(
                Json.str(root, "tag"),
                Json.str(root, "name", "Unknown clan"),
                Json.str(root, "description"),
                Json.str(root, "warFrequency"),
                Json.str(location, "name"),
                Json.str(warLeague, "name"),
                Json.integer(root, "clanLevel"),
                Json.integer(root, "clanPoints"),
                Json.integer(root, "clanBuilderBasePoints"),
                Json.integer(root, "requiredTrophies"),
                Json.integer(root, "warWins"),
                Json.integer(root, "warTies"),
                Json.integer(root, "warLosses"),
                Json.integer(root, "members"),
                Json.bool(root, "isWarLogPublic"),
                members);
    }

    // ---------------------------------------------------------------- war
    public static ClanWar war(String json) {
        return war(Json.asObject(Json.parse(json)));
    }

    public static ClanWar war(Map<String, Object> root) {
        List<ClanWar.Attack> attacks = new ArrayList<>();
        for (Object row : Json.list(root, "attacks")) {
            Map<String, Object> a = Json.asObject(row);
            attacks.add(new ClanWar.Attack(
                    Json.str(a, "attackerTag"),
                    "",
                    Json.str(a, "defenderTag"),
                    Json.integer(a, "stars"),
                    Json.dbl(a, "destructionPercentage", 0),
                    Json.integer(a, "order")));
        }
        Map<String, Object> clan = Json.object(root, "clan");
        Map<String, Object> opponent = Json.object(root, "opponent");
        return new ClanWar(
                Json.str(root, "state"),
                time(Json.str(root, "preparationStartTime")),
                time(Json.str(root, "startTime")),
                time(Json.str(root, "endTime")),
                Json.integer(root, "teamSize"),
                Json.integer(root, "attacksPerMember", 2),
                side(clan),
                side(opponent),
                attacks);
    }

    private static ClanWar.Side side(Map<String, Object> m) {
        if (m.isEmpty()) return null;
        return new ClanWar.Side(
                Json.str(m, "tag"),
                Json.str(m, "name", "Unknown clan"),
                Json.integer(m, "clanLevel"),
                Json.integer(m, "stars"),
                Json.dbl(m, "destructionPercentage", 0),
                Json.integer(m, "attacks"),
                Json.list(m, "members").size());
    }

    // ------------------------------------------------------------- warlog
    public static WarLog warLog(String json) {
        return warLog(Json.asObject(Json.parse(json)));
    }

    public static WarLog warLog(Map<String, Object> root) {
        List<WarLog.Entry> entries = new ArrayList<>();
        for (Object row : Json.list(root, "items")) {
            Map<String, Object> item = Json.asObject(row);
            Map<String, Object> clan = Json.object(item, "clan");
            Map<String, Object> opponent = Json.object(item, "opponent");
            entries.add(new WarLog.Entry(
                    Json.str(item, "result"),
                    time(Json.str(item, "endTime")),
                    Json.str(opponent, "name", "Unknown clan"),
                    Json.str(opponent, "tag"),
                    Json.integer(item, "teamSize"),
                    Json.integer(clan, "stars"),
                    Json.integer(opponent, "stars"),
                    Json.dbl(clan, "destructionPercentage", 0),
                    Json.dbl(opponent, "destructionPercentage", 0)));
        }
        return new WarLog(entries);
    }

    // --------------------------------------------------------------- time
    /**
     * Parses the API timestamp {@code 20240101T000000.000Z} into epoch millis.
     * Returns 0 for an empty or malformed value instead of throwing, so one bad
     * field never hides a whole response.
     */
    public static long time(String value) {
        if (value == null) return 0;
        String s = value.trim();
        if (s.length() < 16 || s.charAt(8) != 'T') return 0;
        try {
            int year = Integer.parseInt(s.substring(0, 4));
            int month = Integer.parseInt(s.substring(4, 6));
            int day = Integer.parseInt(s.substring(6, 8));
            int hour = Integer.parseInt(s.substring(9, 11));
            int minute = Integer.parseInt(s.substring(11, 13));
            int second = Integer.parseInt(s.substring(13, 15));
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            c.clear();
            c.set(year, month - 1, day, hour, minute, second);
            return c.getTimeInMillis();
        } catch (RuntimeException e) {
            return 0;
        }
    }
}
