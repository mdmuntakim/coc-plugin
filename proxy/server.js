#!/usr/bin/env node
/**
 * COCPlugin API proxy for a small server with a static IP.
 * ---------------------------------------------------------------------------
 * Use this when the Supercell developer portal only accepts a single address
 * for the token. The token never leaves the server; the phone sends a client
 * key in the same header the real API expects.
 *
 *   COC_API_TOKEN=... CLIENT_KEY=long-random-password PORT=8787 node proxy/server.js
 *
 * Then publish it over HTTPS (Caddy is two lines) and use
 *   https://your-host/v1
 * as the API base URL in the app, with CLIENT_KEY as the token.
 *
 * No dependencies, Node 18 or newer.
 */

"use strict";

const http = require("node:http");

const TOKEN = process.env.COC_API_TOKEN || "";
const CLIENT_KEY = process.env.CLIENT_KEY || "";
const PORT = Number(process.env.PORT || 8787);
const HOST = process.env.HOST || "0.0.0.0";
const UPSTREAM = process.env.COC_API_URL || "https://api.clashofclans.com/v1";
const TTL_MS = 60_000;
const ALLOWED = /^\/(players|clans|leagues|locations|labels)(\/|$)/;

if (!TOKEN) {
  console.error("COC_API_TOKEN is required");
  process.exit(1);
}
if (!CLIENT_KEY) {
  console.warn("warning: CLIENT_KEY is empty, the proxy is open to anyone who finds it");
}

const cache = new Map(); // url -> { at, status, body, type }

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, "http://localhost");
  if (req.method !== "GET") return reply(res, 405, { reason: "methodNotAllowed" });
  if (!ALLOWED.test(url.pathname)) return reply(res, 403, { reason: "pathNotAllowed" });

  if (CLIENT_KEY) {
    const header = String(req.headers.authorization || "");
    const key = header.replace(/^Bearer\s+/i, "").trim();
    if (key !== CLIENT_KEY) return reply(res, 401, { reason: "accessDenied", message: "Bad client key" });
  }

  const target = UPSTREAM + url.pathname + (url.search || "");
  const hit = cache.get(target);
  if (hit && Date.now() - hit.at < TTL_MS) {
    res.writeHead(hit.status, {
      "content-type": hit.type,
      "cache-control": "public, max-age=60",
      "x-proxy-cache": "hit",
    });
    return res.end(hit.body);
  }

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 20_000);
  try {
    const upstream = await fetch(target, {
      signal: controller.signal,
      headers: {
        authorization: `Bearer ${TOKEN}`,
        accept: "application/json",
        "user-agent": "COCPlugin-proxy/2.0 (+https://github.com/mdmuntakim/coc-plugin)",
      },
    });
    const body = await upstream.text();
    const type = upstream.headers.get("content-type") || "application/json";
    if (upstream.status === 200) cache.set(target, { at: Date.now(), status: 200, body, type });
    res.writeHead(upstream.status, {
      "content-type": type,
      "cache-control": upstream.status === 200 ? "public, max-age=60" : "no-store",
      "x-proxy-cache": "miss",
    });
    res.end(body);
  } catch (e) {
    const timeout = e.name === "AbortError";
    reply(res, timeout ? 504 : 502, {
      reason: timeout ? "upstreamTimeout" : "upstreamError",
      message: String(e.message || e),
    });
  } finally {
    clearTimeout(timer);
  }
});

function reply(res, status, payload) {
  const body = JSON.stringify(payload);
  res.writeHead(status, {
    "content-type": "application/json; charset=utf-8",
    "cache-control": "no-store",
  });
  res.end(body);
}

server.listen(PORT, HOST, () => {
  console.log(`COCPlugin proxy on http://${HOST}:${PORT}/v1 -> ${UPSTREAM}`);
});
