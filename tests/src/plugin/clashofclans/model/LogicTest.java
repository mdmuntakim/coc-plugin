package plugin.clashofclans.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Plain-JVM regression suite for the planner maths.
 *
 * <p>It compiles against the pure {@code model} package only, so it runs in a
 * few hundred milliseconds without an emulator or the Android SDK.
 */
public final class LogicTest {

    private static int checks = 0;
    private static int failures = 0;

    public static void main(String[] args) {
        tags();
        experience();
        formatting();
        upgrades();
        catalog();
        leagues();
        loot();
        war();
        season();
        json();
        playerCard();
        clanCard();
        warCard();
        warLogCard();

        System.out.println();
        if (failures == 0) {
            System.out.println("OK  " + checks + " checks passed");
        } else {
            System.out.println("FAILED " + failures + " of " + checks + " checks");
            System.exit(1);
        }
    }

    private static void tags() {
        eq("#2P0LYQ8GR", Tags.normalize("# 2p0lyq8gr"), "tags normalize");
        eq("", Tags.normalize("hello"), "tags reject letters");
        eq("", Tags.normalize("#2"), "tags reject too short");
        eq("", Tags.normalize("#2P0LYQ8GRXYZ"), "tags reject too long");
        eq("#2P0LYQ8GR", Tags.normalize("2p0lyq8gr"), "tags add the hash");
        yes(Tags.isValid("#2P0LYQ8GR"), "valid tag accepted");
        no(Tags.isValid("#2P0LYQ8B"), "letter B is not in the tag alphabet");
        eq("#2P0LYQ8GR", Tags.require("2P0LYQ8GR"), "require normalizes");
        try {
            Tags.require("nope");
            fail("require should throw for an invalid tag");
        } catch (IllegalArgumentException expected) {
            pass();
        }
    }

    private static void experience() {
        eq(1, Xp.levelForXp(0), "level 1 at 0 xp");
        yes(Xp.levelForXp(1_000_000) > 1, "level grows with xp");
        yes(Xp.toNext(10) > Xp.toNext(1), "later levels need more xp");
        yes(Xp.atStart(5) == Xp.toNext(1) + Xp.toNext(2) + Xp.toNext(3) + Xp.toNext(4),
                "cumulative xp");
        for (long xp : new long[]{0, 199, 200, 5_000, 250_000, 9_000_000}) {
            float p = Xp.progress(xp);
            yes(p >= 0f && p <= 1f, "xp progress in range at " + xp);
            yes(Xp.remaining(xp) >= 0, "xp remaining non-negative");
        }
    }

    private static void formatting() {
        eq("0s", Fmt.duration(0), "zero duration");
        eq("30s", Fmt.duration(30_000), "seconds");
        eq("5m 00s", Fmt.duration(300_000), "minutes");
        eq("3h 01m", Fmt.duration(3 * 3_600_000L + 60_000L), "hours");
        eq("2d 3h", Fmt.duration(2 * 86_400_000L + 3 * 3_600_000L), "days");
        eq("3.7M", Fmt.shortNumber(3_700_000), "millions");
        eq("12.5K", Fmt.shortNumber(12_500), "thousands");
        eq("999", Fmt.shortNumber(999), "small numbers");
        eq("1,234,567", Fmt.number(1_234_567), "grouped number");
        eq("42%", Fmt.percent(0.42f), "percent");
    }

    private static void upgrades() {
        Building cannon = Catalog.building("cannon");
        Upgrade first = Upgrade.forBuilding(cannon, 1);
        eq(cannon.baseCost, first.cost, "level one costs the base cost");
        yes(first.seconds >= 60, "upgrade takes at least a minute");
        Upgrade later = Upgrade.forBuilding(cannon, 5);
        yes(later.cost > first.cost, "cost grows");
        yes(later.seconds > first.seconds, "time grows");
        eq(0L, later.cost % 100L, "costs are rounded to 100");
        eq(0L, Upgrade.forBuilding(cannon, 0).cost % 100L, "level 0 is clamped");

        Troop barbarian = Catalog.troop("barbarian");
        yes(Upgrade.forTroop(barbarian, 2).cost > barbarian.baseCost, "troop upgrade grows");
    }

    private static void catalog() {
        Set<String> ids = new HashSet<>();
        for (Building b : Catalog.BUILDINGS) {
            yes(ids.add(b.id), "unique building id " + b.id);
            yes(b.maxLevel >= 1, "building has levels " + b.id);
            for (int th = 1; th <= Catalog.MAX_TOWN_HALL; th++) {
                int level = Catalog.defaultLevel(b, th);
                yes(level >= 1 && level <= b.maxLevel, "default level in range " + b.id);
            }
        }
        ids.clear();
        for (Troop t : Catalog.TROOPS) {
            yes(ids.add(t.id), "unique troop id " + t.id);
            yes(t.maxLevel >= 1, "troop has levels " + t.id);
        }
        yes(Catalog.BUILDINGS.size() >= 15, "catalogue covers the village");
        yes(Catalog.TROOPS.size() >= 15, "catalogue covers the army");
        eq(0L, Catalog.costToMax(java.util.Collections.<String, Integer>emptyMap()) > 0 ? 0L : 1L,
                "cost to max is positive");
    }

    private static void leagues() {
        eq("Unranked", Leagues.forTrophies(0).name, "unranked at zero");
        eq("Silver II", Leagues.forTrophies(900).name, "silver II at 900");
        eq("Legend", Leagues.forTrophies(5_000).name, "legend at 5000");
        eq("Bronze III", Leagues.next(0).name, "next league after zero");
        eq(null, Leagues.next(6_000), "no league above legend");
        for (int t : new int[]{0, 399, 400, 1_234, 2_999, 4_999}) {
            float p = Leagues.progress(t);
            yes(p >= 0f && p <= 1f, "league progress in range at " + t);
        }
        yes(Leagues.progress(4_999) > 0.99f, "legend progress approaches one");
    }

    private static void loot() {
        eq(500_000L, Loot.fromAttack(1_000_000, 50), "half of a base");
        eq(0L, Loot.fromAttack(1_000_000, 0), "no destruction no loot");
        eq(1_500_000L, Loot.fromRaids(1_000_000, 50, 3), "three raids");
        eq(3L, Loot.raidsNeeded(2_500, 1_000), "rounds up");
        eq(-1L, Loot.raidsNeeded(2_500, 0), "no loot per raid");
        eq(0L, Loot.raidsNeeded(0, 1_000), "nothing to farm");
        eq(4L, Loot.parallelSeconds(10, 3), "parallel builders round up");
        eq(10L, Loot.parallelSeconds(10, 0), "no builders no division");
    }

    private static void war() {
        eq("Victory", War.result(3, 2), "more stars wins");
        eq("Defeat", War.result(1, 2), "fewer stars loses");
        eq("Draw", War.result(2, 2), "equal stars draw");
        eq(3, War.starsForDestruction(100), "three stars at 100%");
        eq(2, War.starsForDestruction(70), "two stars at 70%");
        eq(1, War.starsForDestruction(50), "one star at 50%");
        eq(0, War.starsForDestruction(49), "no star below 50%");
        eq(15, War.attacksLeft(10, 5), "attacks left");
        eq(0, War.attacksLeft(10, 99), "attacks never negative");
        float d = War.destruction(3);
        yes(d > 0f && d <= 1f, "destruction in range");
        eq(1f, War.destruction(3, 3), "a full attack destroys the base");
        yes(War.destruction(7, 45) < 0.2f, "seven of 45 war stars is not a full bar");
        eq(0f, War.destruction(1, 0), "no maximum no destruction");
    }

    private static void season() {
        long now = 1_766_000_000_000L; // 2025-12-15
        long until = Season.millisUntilReset(now);
        yes(until > 0, "season reset is in the future");
        yes(until < 32L * 86_400_000L, "season reset is within a month");
        yes(Season.label(now).endsWith("season"), "season label");
    }

    // ---------------------------------------------------------- live feed
    private static String fixture(String name) {
        String dir = System.getProperty("fixtures", "tests/fixtures");
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get(dir, name));
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private static void json() {
        String text = fixture("player.json");
        yes(!text.isEmpty(), "player fixture is readable");
        Map<String, Object> root = Json.asObject(Json.parse(text));
        eq("Test Chief", Json.str(root, "name"), "json reads a string");
        eq(16L, Json.number(root, "townHallLevel", 0), "json reads an integer");
        eq(5120, Json.integer(root, "trophies"), "json reads a number as int");
        eq(1, Json.list(root, "troops").size() > 0 ? 1 : 0, "json reads an array");
        eq("#2YQ8L0PR", Json.str(Json.object(root, "clan"), "tag"), "json reads a nested object");
        yes(Json.bool(Json.asObject(Json.parse(fixture("clan.json"))), "isWarLogPublic"),
                "json reads a boolean");
        eq(-1.0, Json.dbl(root, "noSuchKey", -1), "a missing key falls back");
        eq("star", Json.str(Json.asObject(Json.parse("{\"k\":\"st\\u0061r\",\"n\":1.5}")), "k"),
                "unicode escapes are decoded");
        eq(1.5, Json.dbl(Json.asObject(Json.parse("{\"n\":1.5}")), "n", 0), "decimals are doubles");
        eq("", Json.str(Collections.emptyMap(), "missing"), "a missing key is empty");
        try {
            Json.parse("{\"a\":1} trailing");
            fail("trailing characters should be rejected");
        } catch (IllegalArgumentException expected) {
            pass();
        }
        try {
            Json.parse("{\"a\":");
            fail("a truncated document should be rejected");
        } catch (IllegalArgumentException expected) {
            pass();
        }
    }

    private static void playerCard() {
        Player p = CocJson.player(fixture("player.json"));
        eq("#2PP", p.tag, "player tag");
        eq("Test Chief", p.name, "player name");
        eq(16, p.townHallLevel, "town hall from the server");
        eq(5120, p.trophies, "trophies from the server");
        eq(8420, p.donations, "donations from the server");
        eq("Legend League", p.leagueName, "league name");
        eq("Iron Vanguard", p.clanName, "clan name");
        yes(p.inClan(), "player is in a clan");
        eq(200, p.donationRatio(), "donation ratio");
        eq(3, p.units("troop", "home").size(), "home troops parsed");
        eq(1, p.units("troop", "builderBase").size(), "builder base troops parsed");
        eq(2, p.units("hero", "home").size(), "heroes parsed");
        eq(2, p.units("spell", "home").size(), "spells parsed");
        eq(1, p.units("pet", "home").size(), "pets parsed");
        eq(9, p.units.size(), "all units counted");
        eq(2, p.maxedUnits(), "maxed units counted");
        float progress = p.unitProgress(p.units);
        yes(progress > 0f && progress <= 1f, "unit progress in range");
        yes(!p.units("hero", "home").get(0).maxed(), "a hero is not maxed");
    }

    private static void clanCard() {
        Clan c = CocJson.clan(fixture("clan.json"));
        eq("#2YQ8L0PR", c.tag, "clan tag");
        eq("Iron Vanguard", c.name, "clan name");
        eq(19, c.level, "clan level");
        eq(3, c.members, "member count");
        eq(3, c.memberList.size(), "member list parsed");
        eq("Second", c.memberList.get(1).name, "member names parsed");
        yes(c.warLogPublic, "public war log flag");
        eq("342W 88L 12D", c.warRecord(), "clan war record");
        eq(77, c.winRate(), "clan win rate");
        eq("Champion League I", c.warLeagueName, "war league name");
        eq("International", c.locationName, "location name");
    }

    private static void warCard() {
        ClanWar war = CocJson.war(fixture("war.json"));
        eq(ClanWar.IN_WAR, war.state, "war state");
        eq("Battle day", war.stateName(), "war state name");
        yes(war.inWar(), "a running war is live");
        eq(15, war.teamSize, "team size");
        eq(30, war.totalAttacks(), "two attacks per member");
        eq(16, war.attacksLeft(), "attacks left");
        eq("Nightfall Legion", war.opponent.name, "opponent name");
        eq(21, war.clan.stars, "our stars");
        eq(1709380800000L, war.start, "start time in millis");
        eq(1709467200000L, war.end, "end time in millis");
        yes(war.clan.destruction > 70 && war.clan.destruction <= 100, "destruction is a percentage");
        eq(1, war.attacks.size(), "attack list parsed");
        eq(3, war.attacks.get(0).stars, "attack stars");
        eq("", war.result(), "no result before the war ends");

        ClanWar idle = CocJson.war("{\"state\":\"notInWar\"}");
        no(idle.inWar(), "notInWar is not live");
        eq("Not in war", idle.stateName(), "idle name");
        eq(0, idle.totalAttacks(), "no teams no attacks");
        ClanWar empty = CocJson.war("{}");
        eq(ClanWar.NOT_IN_WAR, empty.state, "an empty document is notInWar");

        eq(1709380800000L, CocJson.time("20240302T120000.000Z"), "timestamp with millis");
        eq(1709380800000L, CocJson.time("20240302T120000Z"), "timestamp without millis");
        eq(0L, CocJson.time(""), "empty timestamp");
        eq(0L, CocJson.time("2024-03-02"), "malformed timestamp");
    }

    private static void warLogCard() {
        WarLog log = CocJson.warLog(fixture("warlog.json"));
        eq(3, log.played(), "three logged wars");
        eq(1, log.wins(), "one win");
        eq(1, log.losses(), "one loss");
        eq(1, log.draws(), "one draw");
        eq(33, log.winRate(), "win rate");
        eq("Stone Wolves", log.entries.get(0).opponentName, "newest war first");
        yes(log.entries.get(0).won(), "the first entry is a win");
        eq(1708430400000L, log.entries.get(0).endTime, "log end time in millis");
        eq(0, WarLog.empty().played(), "empty log");
    }

    // ------------------------------------------------------------- harness
    private static void eq(Object expected, Object actual, String what) {
        checks++;
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        if (!ok) {
            failures++;
            System.out.println("FAIL " + what + ": expected [" + expected + "] got [" + actual + "]");
        } else {
            System.out.println("ok   " + what);
        }
    }

    private static void yes(boolean value, String what) {
        checks++;
        if (!value) {
            failures++;
            System.out.println("FAIL " + what);
        } else {
            System.out.println("ok   " + what);
        }
    }

    private static void no(boolean value, String what) {
        yes(!value, what);
    }

    private static void pass() {
        checks++;
        System.out.println("ok   exception thrown");
    }

    private static void fail(String what) {
        checks++;
        failures++;
        System.out.println("FAIL " + what);
    }
}
