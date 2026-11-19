package plugin.clashofclans.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import plugin.clashofclans.model.Clan;
import plugin.clashofclans.model.ClanWar;
import plugin.clashofclans.model.CocJson;
import plugin.clashofclans.model.Json;
import plugin.clashofclans.model.Player;
import plugin.clashofclans.model.Tags;
import plugin.clashofclans.model.WarLog;

/**
 * The real client of the official Clash of Clans API.
 *
 * <pre>
 *   GET https://api.clashofclans.com/v1/players/%23TAG   Authorization: Bearer TOKEN
 * </pre>
 *
 * <p>No mock, no fixture: every method performs the HTTP request and returns
 * what the server sent. Pure Java, so the same class runs in the plain-JVM
 * test suite where it is pointed at a local {@code HttpServer}, and on the
 * device against {@code api.clashofclans.com}.
 *
 * <p>A different base URL can be supplied: the API token is bound to the IP
 * address that created it, which is awkward on mobile data, so a small proxy
 * (see {@code proxy/worker.js}) can be used instead. Both the official host
 * and a proxy use the same JSON schema.
 */
public final class CocApi {

    public static final String OFFICIAL_BASE = "https://api.clashofclans.com/v1";
    public static final String USER_AGENT =
            "COCPlugin/2.0 (Android; +https://github.com/mdmuntakim/coc-plugin)";

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 20_000;

    private final String base;
    private final String token;

    public CocApi(String base, String token) {
        String b = base == null ? "" : base.trim();
        while (b.endsWith("/")) b = b.substring(0, b.length() - 1);
        this.base = b.isEmpty() ? OFFICIAL_BASE : b;
        this.token = token == null ? "" : token.trim();
    }

    public String baseUrl() {
        return base;
    }

    /** Host shown in the UI, e.g. {@code api.clashofclans.com}. */
    public String host() {
        try {
            return new URL(base).getHost();
        } catch (Exception e) {
            return base;
        }
    }

    public boolean hasToken() {
        return !token.isEmpty();
    }

    // ------------------------------------------------------------ resources
    public Player player(String tag) throws IOException, ApiException {
        return CocJson.player(get("/players/" + pathTag(tag)));
    }

    public Clan clan(String tag) throws IOException, ApiException {
        return CocJson.clan(get("/clans/" + pathTag(tag)));
    }

    public ClanWar currentWar(String tag) throws IOException, ApiException {
        return CocJson.war(get("/clans/" + pathTag(tag) + "/currentwar"));
    }

    public WarLog warLog(String tag) throws IOException, ApiException {
        return CocJson.warLog(get("/clans/" + pathTag(tag) + "/warlog?limit=20"));
    }

    /** Raw body for the diagnostics line in the connect sheet. */
    public String raw(String path) throws IOException, ApiException {
        return get(path.startsWith("/") ? path : "/" + path);
    }

    // ----------------------------------------------------------------- http
    private String get(String path) throws IOException, ApiException {
        HttpURLConnection c = null;
        try {
            URL url = new URL(base + path);
            c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(CONNECT_TIMEOUT_MS);
            c.setReadTimeout(READ_TIMEOUT_MS);
            c.setInstanceFollowRedirects(true);
            c.setRequestProperty("Accept", "application/json");
            c.setRequestProperty("Accept-Encoding", "gzip");
            c.setRequestProperty("User-Agent", USER_AGENT);
            if (!token.isEmpty()) {
                c.setRequestProperty("Authorization", "Bearer " + token);
            }

            int code = c.getResponseCode();
            if (code >= 200 && code < 300) {
                return read(c.getInputStream(), c.getContentEncoding());
            }

            String body = "";
            InputStream err = c.getErrorStream();
            if (err != null) {
                body = read(err, c.getContentEncoding());
            }
            String reason = "";
            String message = "";
            if (!body.isEmpty()) {
                try {
                    Map<String, Object> json = Json.asObject(Json.parse(body));
                    reason = Json.str(json, "reason");
                    message = Json.str(json, "message");
                } catch (RuntimeException ignored) {
                    message = body.length() > 200 ? body.substring(0, 200) : body;
                }
            }
            if (reason.isEmpty() && message.isEmpty()) message = "HTTP " + code;
            throw new ApiException(code, reason, message);
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private static String read(InputStream in, String encoding) throws IOException {
        InputStream stream = in;
        try {
            if (encoding != null && encoding.toLowerCase().contains("gzip")) {
                stream = new GZIPInputStream(in);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream(8_192);
            byte[] buffer = new byte[8_192];
            int n;
            while ((n = stream.read(buffer)) != -1) out.write(buffer, 0, n);
            return new String(out.toByteArray(), UTF8);
        } finally {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Normalizes and url-encodes a tag: {@code #2PP} becomes {@code %232PP}.
     * An invalid tag throws before a request is wasted.
     */
    public static String pathTag(String raw) {
        String tag = Tags.normalize(raw);
        if (tag.isEmpty()) {
            throw new IllegalArgumentException("That is not a valid clan or player tag");
        }
        try {
            return URLEncoder.encode(tag, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return tag.replace("#", "%23");
        }
    }
}
