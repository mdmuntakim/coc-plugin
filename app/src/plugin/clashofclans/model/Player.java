package plugin.clashofclans.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A player profile exactly as the official Clash of Clans API returns it.
 *
 * <p>Everything here is filled from the server response, never invented:
 * {@link #name}, {@link #tag}, trophies, the clan card and the twenty-odd
 * unit levels are the values Supercell returned for that tag.
 */
public final class Player {

    /** One troop, spell, hero or pet with its real level. */
    public static final class Unit {
        public final String name;
        public final String kind;     // "troop", "spell", "hero", "pet"
        public final String village;  // "home" or "builderBase"
        public final int level;
        public final int maxLevel;

        public Unit(String name, String kind, String village, int level, int maxLevel) {
            this.name = name;
            this.kind = kind;
            this.village = village;
            this.level = Math.max(0, level);
            this.maxLevel = Math.max(this.level, maxLevel);
        }

        public boolean maxed() {
            return maxLevel > 0 && level >= maxLevel;
        }

        public float progress() {
            return maxLevel <= 0 ? 1f : Math.min(1f, level / (float) maxLevel);
        }
    }

    public final String tag;
    public final String name;
    public final String role;
    public final String leagueName;
    public final String builderLeagueName;
    public final String clanTag;
    public final String clanName;
    public final int clanLevel;

    public final int townHallLevel;
    public final int townHallWeaponLevel;
    public final int expLevel;
    public final int trophies;
    public final int bestTrophies;
    public final int warStars;
    public final int attackWins;
    public final int attackLosses;
    public final int defenseWins;
    public final int defenseLosses;
    public final int builderHallLevel;
    public final int builderBaseTrophies;
    public final int bestBuilderBaseTrophies;
    public final int donations;
    public final int donationsReceived;
    public final int warWins;
    public final int warTies;
    public final int warLosses;
    public final long clanCapitalContributions;

    public final List<Unit> units;

    public Player(String tag, String name, String role, String leagueName, String builderLeagueName,
                  String clanTag, String clanName, int clanLevel,
                  int townHallLevel, int townHallWeaponLevel, int expLevel,
                  int trophies, int bestTrophies, int warStars,
                  int attackWins, int attackLosses, int defenseWins, int defenseLosses,
                  int builderHallLevel, int builderBaseTrophies, int bestBuilderBaseTrophies,
                  int donations, int donationsReceived,
                  int warWins, int warTies, int warLosses, long clanCapitalContributions,
                  List<Unit> units) {
        this.tag = tag;
        this.name = name;
        this.role = role;
        this.leagueName = leagueName;
        this.builderLeagueName = builderLeagueName;
        this.clanTag = clanTag;
        this.clanName = clanName;
        this.clanLevel = clanLevel;
        this.townHallLevel = townHallLevel;
        this.townHallWeaponLevel = townHallWeaponLevel;
        this.expLevel = expLevel;
        this.trophies = trophies;
        this.bestTrophies = bestTrophies;
        this.warStars = warStars;
        this.attackWins = attackWins;
        this.attackLosses = attackLosses;
        this.defenseWins = defenseWins;
        this.defenseLosses = defenseLosses;
        this.builderHallLevel = builderHallLevel;
        this.builderBaseTrophies = builderBaseTrophies;
        this.bestBuilderBaseTrophies = bestBuilderBaseTrophies;
        this.donations = donations;
        this.donationsReceived = donationsReceived;
        this.warWins = warWins;
        this.warTies = warTies;
        this.warLosses = warLosses;
        this.clanCapitalContributions = clanCapitalContributions;
        this.units = Collections.unmodifiableList(new ArrayList<>(units));
    }

    public boolean inClan() {
        return !clanTag.isEmpty();
    }

    public int warPlayed() {
        return warWins + warLosses + warTies;
    }

    public String warRecord() {
        return warWins + "W " + warLosses + "L " + warTies + "D";
    }

    /** Donated divided by received, in percent; 0 when nothing was received. */
    public int donationRatio() {
        if (donationsReceived <= 0) return donations > 0 ? 100 : 0;
        return Math.round(donations * 100f / donationsReceived);
    }

    public List<Unit> units(String kind, String village) {
        List<Unit> out = new ArrayList<>();
        for (Unit u : units) {
            if (u.kind.equals(kind) && u.village.equals(village)) out.add(u);
        }
        return out;
    }

    public int maxedUnits() {
        int n = 0;
        for (Unit u : units) if (u.maxed()) n++;
        return n;
    }

    /** Total levels reached over total possible levels, 0..1. */
    public float unitProgress(List<Unit> subset) {
        int level = 0;
        int max = 0;
        for (Unit u : subset) {
            level += u.level;
            max += u.maxLevel;
        }
        return max <= 0 ? 0f : Math.min(1f, level / (float) max);
    }
}
