package plugin.clashofclans.net;

import java.util.Map;

import plugin.clashofclans.model.Clan;
import plugin.clashofclans.model.ClanWar;
import plugin.clashofclans.model.Player;

/**
 * Command-line smoke test for the real server.
 *
 * <pre>
 *   COC_API_TOKEN=... COC_PLAYER_TAG=#TAG bash tests/live.sh
 * </pre>
 *
 * Without a token it still proves the endpoint is reachable: the API answers
 * 401/403, which is a real HTTP round trip through the same code the app uses.
 */
public final class LiveProbe {

    public static void main(String[] args) {
        Map<String, String> env = System.getenv();
        String base = value(env, "COC_API_BASE", CocApi.OFFICIAL_BASE);
        String token = value(env, "COC_API_TOKEN", "");
        String tag = value(env, "COC_PLAYER_TAG", "#2PP");

        CocApi api = new CocApi(base, token);
        System.out.println("base  : " + api.baseUrl());
        System.out.println("tag   : " + tag);
        System.out.println("token : " + (token.isEmpty() ? "none (reachability probe)" : "set"));

        try {
            Player player = api.player(tag);
            System.out.println();
            System.out.println("OK  " + player.name + "  " + player.tag
                    + "  TH " + player.townHallLevel
                    + "  level " + player.expLevel
                    + "  " + player.trophies + " trophies"
                    + "  clan " + player.clanName);
            System.out.println("    units: " + player.units.size()
                    + "  maxed: " + player.maxedUnits());

            if (!player.clanTag.isEmpty()) {
                Clan clan = api.clan(player.clanTag);
                System.out.println("    clan: " + clan.name + "  level " + clan.level
                        + "  " + clan.warRecord());
                ClanWar war = api.currentWar(player.clanTag);
                System.out.println("    war : " + war.stateName()
                        + (war.inWar() ? "  vs " + war.opponent.name : ""));
            }
            System.exit(0);
        } catch (ApiException e) {
            if (token.isEmpty() && e.isAuthProblem()) {
                System.out.println();
                System.out.println("REACHABLE  the API answered " + e.code
                        + " (" + e.reason + "), so the connection works and only a token is missing.");
                System.exit(0);
            }
            System.out.println();
            System.out.println("FAILED  " + e.friendly());
            System.exit(1);
        } catch (Exception e) {
            System.out.println();
            System.out.println("FAILED  " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.exit(1);
        }
    }

    private static String value(Map<String, String> env, String key, String fallback) {
        String v = env.get(key);
        return v == null || v.trim().isEmpty() ? fallback : v.trim();
    }
}
