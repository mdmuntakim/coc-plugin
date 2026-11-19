package plugin.clashofclans.net;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPOutputStream;

import plugin.clashofclans.model.Clan;
import plugin.clashofclans.model.ClanWar;
import plugin.clashofclans.model.Player;
import plugin.clashofclans.model.WarLog;

/**
 * Runs the real {@link CocApi} against a local HTTP server. No network, no
 * emulator: the test proves the request shape (path encoding, bearer header,
 * gzip), the success paths and every error mapping the UI depends on.
 */
public final class ApiTest {

    private static int checks = 0;
    private static int failures = 0;

    private static final AtomicReference<String> lastAuth = new AtomicReference<>();
    private static final AtomicReference<String> lastPath = new AtomicReference<>();
    private static final AtomicReference<String> lastAgent = new AtomicReference<>();
    private static final AtomicInteger requests = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1", ApiTest::handle);
        server.start();
        String base = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";

        try {
            happyPath(base);
            encoding(base);
            errors(base);
            transport(base);
        } finally {
            server.stop(0);
        }

        System.out.println();
        if (failures == 0) {
            System.out.println("OK  " + checks + " API checks passed");
        } else {
            System.out.println("FAILED " + failures + " of " + checks + " API checks");
            System.exit(1);
        }
    }

    // ------------------------------------------------------------- checks
    private static void happyPath(String base) throws Exception {
        CocApi api = new CocApi(base, "test-token");
        Player player = api.player("#2PP");
        eq("Test Chief", player.name, "player name arrives");
        eq(5120, player.trophies, "trophies arrive");
        eq(16, player.townHallLevel, "town hall arrives");
        eq(9, player.units.size(), "unit levels arrive");
        eq("Bearer test-token", lastAuth.get(), "the bearer token is sent");
        yes(lastAgent.get() != null && lastAgent.get().contains("COCPlugin"),
                "the user agent identifies the app");

        Clan clan = api.clan("#2YQ8L0PR");
        eq("Iron Vanguard", clan.name, "clan name arrives");
        eq(342, clan.warWins, "clan war wins arrive");

        ClanWar war = api.currentWar("#2YQ8L0PR");
        eq(ClanWar.IN_WAR, war.state, "the war state arrives from a gzip body");
        eq(30, war.totalAttacks(), "war attacks are computed");

        WarLog log = api.warLog("#2YQ8L0PR");
        eq(3, log.played(), "war log arrives");
        eq(1, log.wins(), "war log wins");

        CocApi anonymous = new CocApi(base, "");
        eq("Test Chief", anonymous.player("#2PP").name, "a proxy without a token still works");
        eq(null, lastAuth.get(), "no Authorization header without a token");
    }

    private static void encoding(String base) throws Exception {
        eq("%232PP", CocApi.pathTag("#2PP"), "tags are url encoded");
        eq("%232PP", CocApi.pathTag(" 2pp "), "tags are normalized and upper cased");
        try {
            CocApi.pathTag("nope");
            fail("an invalid tag is rejected before the request");
        } catch (IllegalArgumentException expected) {
            pass();
        }

        int before = requests.get();
        CocApi api = new CocApi(base + "///", "t");
        eq("%232PP", CocApi.pathTag("#2PP"), "pathTag is stable");
        api.player("#8YQ");
        eq(before + 1, requests.get(), "a trailing slash does not break the request");
        eq("http://127.0.0.1", "http://" + api.host().replaceAll(":\\d+$", ""),
                "the host is exposed to the UI");
        eq("/v1/players/%238YQ", lastPath.get(), "the request path is encoded exactly");
    }

    private static void errors(String base) throws Exception {
        CocApi api = new CocApi(base, "test-token");

        try {
            api.player("#2YY");
            fail("404 should throw");
        } catch (ApiException e) {
            eq(404, e.code, "404 code");
            yes(e.isNotFound(), "404 is a not-found");
            yes(e.friendly().contains("tag"), "the 404 message mentions the tag");
        }

        try {
            api.player("#2QQ");
            fail("403 should throw");
        } catch (ApiException e) {
            eq(403, e.code, "403 code");
            yes(e.isAuthProblem(), "403 is an auth problem");
            yes(e.friendly().contains("IP"), "the 403 message explains the IP lock");
        }

        try {
            api.player("#2VV");
            fail("429 should throw");
        } catch (ApiException e) {
            eq(429, e.code, "429 code");
            yes(e.friendly().toLowerCase().contains("wait"), "the 429 message asks to wait");
        }
    }

    private static void transport(String base) {
        CocApi dead = new CocApi("http://127.0.0.1:1/v1", "t");
        try {
            dead.player("#2PP");
            fail("a closed port should throw");
        } catch (ApiException e) {
            fail("a closed port is not an ApiException");
        } catch (IOException expected) {
            pass();
        }
        eq("api.clashofclans.com", new CocApi("", "").host(), "the official host is the default");
        eq(CocApi.OFFICIAL_BASE, new CocApi("  ", "").baseUrl(), "an empty base falls back");
    }

    // -------------------------------------------------------------- server
    private static void handle(HttpExchange exchange) throws IOException {
        requests.incrementAndGet();
        lastAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
        lastAgent.set(exchange.getRequestHeaders().getFirst("User-Agent"));
        lastPath.set(exchange.getRequestURI().getRawPath());

        String path = exchange.getRequestURI().getPath();
        String body;
        String type = "application/json";
        int code = 200;
        boolean gzip = false;

        if (path.contains("/players/")) {
            if (path.endsWith("#2YY")) {
                code = 404;
                body = "{\"reason\":\"notFound\",\"message\":\"Not found\"}";
            } else if (path.endsWith("#2QQ")) {
                code = 403;
                body = "{\"reason\":\"accessDenied\",\"message\":\"Missing or invalid authorization\"}";
            } else if (path.endsWith("#2VV")) {
                code = 429;
                body = "{\"reason\":\"tooManyRequests\",\"message\":\"Slow down\"}";
            } else {
                body = fixture("player.json");
            }
        } else if (path.endsWith("/currentwar")) {
            body = fixture("war.json");
            gzip = true;
        } else if (path.endsWith("/warlog")) {
            body = fixture("warlog.json");
        } else if (path.contains("/clans/")) {
            body = fixture("clan.json");
        } else {
            code = 404;
            body = "{\"reason\":\"notFound\",\"message\":\"No route\"}";
        }

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        if (gzip) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (GZIPOutputStream out = new GZIPOutputStream(buffer)) {
                out.write(bytes);
            }
            bytes = buffer.toByteArray();
            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
        }
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String fixture(String name) {
        String dir = System.getProperty("fixtures", "tests/fixtures");
        try {
            return new String(Files.readAllBytes(Paths.get(dir, name)),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "{}";
        }
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
