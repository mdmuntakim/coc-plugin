package plugin.clashofclans.data;

import plugin.clashofclans.model.Clan;
import plugin.clashofclans.model.ClanWar;
import plugin.clashofclans.model.Player;
import plugin.clashofclans.model.WarLog;

/**
 * One fetched window on the real account: the player, their clan, the clan's
 * current war and the public war log, all straight from the API. When the
 * device is offline the last good copy is shown, with {@link #fetchedAt}
 * telling how old it is.
 */
public final class Live {

    public final Player player;
    public final Clan clan;
    public final ClanWar war;
    public final WarLog warLog;
    public final long fetchedAt;
    public final String host;
    public final String error;    // last failure, null when the fetch worked
    public final String warning;  // partial failure, e.g. a private war log

    public Live(Player player, Clan clan, ClanWar war, WarLog warLog,
                long fetchedAt, String host, String error, String warning) {
        this.player = player;
        this.clan = clan;
        this.war = war;
        this.warLog = warLog;
        this.fetchedAt = fetchedAt;
        this.host = host == null ? "" : host;
        this.error = error;
        this.warning = warning;
    }

    public static Live empty() {
        return new Live(null, null, null, null, 0, "", null, null);
    }

    public boolean hasPlayer() {
        return player != null;
    }

    public boolean hasClan() {
        return clan != null;
    }

    public boolean hasWar() {
        return war != null && war.inWar();
    }

    public boolean hasWarLog() {
        return warLog != null && warLog.played() > 0;
    }

    public boolean hasAnything() {
        return hasPlayer() || hasClan();
    }
}
