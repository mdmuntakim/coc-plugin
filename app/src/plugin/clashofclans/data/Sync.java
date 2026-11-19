package plugin.clashofclans.data;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import plugin.clashofclans.model.CocJson;
import plugin.clashofclans.model.Player;
import plugin.clashofclans.net.ApiException;
import plugin.clashofclans.net.CocApi;

/**
 * Runs the real requests off the UI thread and writes the answers into the
 * {@link Store}. Player first, then the clan card, the current war and the
 * public war log; a private war log is a warning, not a failure.
 */
public final class Sync {

    public interface Listener {
        void onFinished(Live live, String error);
    }

    public interface Probe {
        void onResult(Player player, String error);
    }

    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "coc-api");
        t.setDaemon(true);
        return t;
    });
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final AtomicBoolean BUSY = new AtomicBoolean(false);

    private Sync() {}

    public static boolean busy() {
        return BUSY.get();
    }

    /** Fetches everything for the configured tag. Safe to call repeatedly. */
    public static void refresh(Store store, Listener listener) {
        if (!BUSY.compareAndSet(false, true)) return;

        final String tag = store.tag();
        final String base = store.baseUrl();
        final String token = store.token();

        WORKER.execute(() -> {
            Live result;
            String error = null;
            try {
                if (tag.isEmpty()) {
                    throw new IllegalArgumentException("Add your player tag first.");
                }
                if (token.isEmpty()) {
                    throw new IllegalArgumentException("Add an API token first.");
                }

                CocApi api = new CocApi(base, token);
                String playerJson = api.raw("/players/" + CocApi.pathTag(tag));
                Player player = CocJson.player(playerJson);

                String clanJson = null;
                String warJson = null;
                String logJson = null;
                String warning = null;

                if (!player.clanTag.isEmpty()) {
                    String clanTag = CocApi.pathTag(player.clanTag);
                    try {
                        clanJson = api.raw("/clans/" + clanTag);
                    } catch (Exception e) {
                        warning = "Clan card: " + describe(e);
                    }
                    try {
                        warJson = api.raw("/clans/" + clanTag + "/currentwar");
                    } catch (Exception e) {
                        warning = join(warning, "Current war: " + describe(e));
                    }
                    try {
                        logJson = api.raw("/clans/" + clanTag + "/warlog?limit=20");
                    } catch (Exception e) {
                        warning = join(warning, "War log is not public for this clan.");
                    }
                }

                store.saveLive(playerJson, clanJson, warJson, logJson,
                        api.host(), System.currentTimeMillis(), warning);
                result = store.live();
            } catch (Exception e) {
                // Keep the last good snapshot; report the failure above it.
                Live old = store.live();
                error = describe(e);
                store.setLastError(error);
                result = new Live(old.player, old.clan, old.war, old.warLog,
                        old.fetchedAt, old.host, error, old.warning);
            }

            final Live live = result;
            final String failure = error;
            MAIN.post(() -> {
                BUSY.set(false);
                listener.onFinished(live, failure);
            });
        });
    }

    /**
     * One-shot check used by the connect sheet: fetch only the player. The
     * settings are not touched, so a bad token cannot wipe a working setup.
     */
    public static void probe(String base, String token, String tag, Probe probe) {
        WORKER.execute(() -> {
            Player player = null;
            String error = null;
            try {
                CocApi api = new CocApi(base, token);
                player = api.player(tag);
            } catch (Exception e) {
                error = describe(e);
            }
            final Player p = player;
            final String err = error;
            MAIN.post(() -> probe.onResult(p, err));
        });
    }

    /** A sentence for the UI, mapping the transport errors too. */
    public static String describe(Throwable e) {
        if (e instanceof ApiException) return ((ApiException) e).friendly();
        if (e instanceof UnknownHostException) {
            return "Cannot resolve " + e.getMessage() + ". Check the connection or the base URL.";
        }
        if (e instanceof SocketTimeoutException) {
            return "The server did not answer in time. Try again.";
        }
        if (e instanceof java.io.EOFException) {
            return "The server closed the connection without a reply. Check the base URL and "
                    + "that the proxy is running.";
        }
        if (e instanceof java.net.ConnectException) {
            return "Connection refused. Check the base URL, the port and that the proxy is up.";
        }
        if (e instanceof java.net.NoRouteToHostException) {
            return "No route to that host. Check the connection.";
        }
        if (e instanceof IOException) {
            String m = e.getMessage();
            return "Network error" + (m == null ? "." : ": " + m);
        }
        if (e instanceof IllegalArgumentException) {
            return e.getMessage() == null ? "That value is not valid." : e.getMessage();
        }
        String m = e.getMessage();
        return m == null ? e.getClass().getSimpleName() : m;
    }

    private static String join(String a, String b) {
        if (a == null || a.isEmpty()) return b;
        return a + "  ·  " + b;
    }
}
