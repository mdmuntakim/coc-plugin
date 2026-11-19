package plugin.clashofclans.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A clan card from the official API, including its public member list. */
public final class Clan {

    /** One row of the member list the server returns for a clan. */
    public static final class Member {
        public final String tag;
        public final String name;
        public final String role;
        public final int level;
        public final int trophies;
        public final int donations;
        public final int donationsReceived;
        public final int rank;

        public Member(String tag, String name, String role, int level, int trophies,
                      int donations, int donationsReceived, int rank) {
            this.tag = tag;
            this.name = name;
            this.role = role;
            this.level = level;
            this.trophies = trophies;
            this.donations = donations;
            this.donationsReceived = donationsReceived;
            this.rank = rank;
        }
    }

    public final String tag;
    public final String name;
    public final String description;
    public final String warFrequency;
    public final String locationName;
    public final String warLeagueName;
    public final int level;
    public final int points;
    public final int builderPoints;
    public final int requiredTrophies;
    public final int warWins;
    public final int warTies;
    public final int warLosses;
    public final int members;
    public final boolean warLogPublic;

    public final List<Member> memberList;

    public Clan(String tag, String name, String description, String warFrequency,
                String locationName, String warLeagueName, int level, int points,
                int builderPoints, int requiredTrophies, int warWins, int warTies,
                int warLosses, int members, boolean warLogPublic, List<Member> memberList) {
        this.tag = tag;
        this.name = name;
        this.description = description;
        this.warFrequency = warFrequency;
        this.locationName = locationName;
        this.warLeagueName = warLeagueName;
        this.level = level;
        this.points = points;
        this.builderPoints = builderPoints;
        this.requiredTrophies = requiredTrophies;
        this.warWins = warWins;
        this.warTies = warTies;
        this.warLosses = warLosses;
        this.members = members;
        this.warLogPublic = warLogPublic;
        this.memberList = Collections.unmodifiableList(new ArrayList<>(memberList));
    }

    public int warPlayed() {
        return warWins + warLosses + warTies;
    }

    public int winRate() {
        int played = warPlayed();
        return played <= 0 ? 0 : Math.round(warWins * 100f / played);
    }

    public String warRecord() {
        return warWins + "W " + warLosses + "L " + warTies + "D";
    }
}
