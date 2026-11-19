/**
 * COCPlugin API proxy for Cloudflare Workers.
 * ---------------------------------------------------------------------------
 * The Clash of Clans developer token is bound to the IP address that created
 * it. A phone changes address all the time, so the token lives here instead
 * and the app only knows this Worker's URL plus a client key.
 *
 * Deploy
 *   1. npm i -g wrangler
 *   2. wrangler init --yes            (or paste this file into a Worker)
 *   3. wrangler secret put COC_API_TOKEN     # the real developer token
 *   4. wrangler secret put CLIENT_KEY        # any long random password
 *   5. wrangler deploy
 *
 * In the app's connect sheet:
 *   API base URL : https://<worker>.<account>.workers.dev/v1
 *   API token    : the CLIENT_KEY you just stored
 *
 * Note: the real token must allow the Worker's egress addresses. If the
 * developer portal refuses a wide range, run proxy/server.js on a VPS with a
 * static IP instead and allowlist that one address.
 */

const UPSTREAM = "https://api.clashofclans.com/v1";
const ALLOWED = /^\/(players|clans|leagues|locations|labels)(\/|$)/;

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    if (request.method !== "GET") {
      return json({ reason: "methodNotAllowed", message: "GET only" }, 405);
    }
    if (!ALLOWED.test(url.pathname)) {
      return json({ reason: "pathNotAllowed", message: "Unsupported endpoint" }, 403);
    }

    // The app sends the client key in the same header the real API uses.
    if (env.CLIENT_KEY) {
      const header = request.headers.get("authorization") || "";
      const key = header.replace(/^Bearer\s+/i, "").trim();
      if (key !== env.CLIENT_KEY) {
        return json({ reason: "accessDenied", message: "Bad client key" }, 401);
      }
    }

    const target = UPSTREAM + url.pathname + (url.search || "");
    const cache = caches.default;
    const cacheKey = new Request(target, { method: "GET" });

    let response = await cache.match(cacheKey);
    if (!response) {
      response = await fetch(target, {
        headers: {
          authorization: `Bearer ${env.COC_API_TOKEN}`,
          accept: "application/json",
          "user-agent": "COCPlugin-proxy/2.0 (+https://github.com/mdmuntakim/coc-plugin)",
        },
      });

      // Only successful reads are cached; errors must reach the app as they are.
      if (response.status === 200) {
        const body = await response.text();
        response = new Response(body, {
          status: 200,
          headers: {
            "content-type": "application/json; charset=utf-8",
            "cache-control": "public, max-age=60",
          },
        });
        ctx.waitUntil(cache.put(cacheKey, response.clone()));
      }
    }

    return new Response(response.body, {
      status: response.status,
      headers: {
        "content-type": response.headers.get("content-type") || "application/json",
        "cache-control": response.headers.get("cache-control") || "no-store",
      },
    });
  },
};

function json(payload, status) {
  return new Response(JSON.stringify(payload), {
    status,
    headers: { "content-type": "application/json", "cache-control": "no-store" },
  });
}
