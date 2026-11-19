# Live account connection

COCPlugin 2.0.0 removed the invented profile. Every number on **Account**,
**Army** and **War** now comes from the official Supercell developer API over
HTTPS; the **Village** tab is the only local part left, and it says so.

## Data flow

```
 player tag + token
        │
        ▼
 plugin.clashofclans.net.CocApi          HttpURLConnection, Bearer auth
        │                               GET api.clashofclans.com/v1/...
        ▼
 JSON  ──►  plugin.clashofclans.model.CocJson  ──►  Player / Clan / ClanWar / WarLog
        │
        ▼
 data/Store  ──cached verbatim in SharedPreferences──►  data/Live
        │
        ▼
 AccountPanel, ArmyPanel, WarPanel
```

A successful sync stores the raw JSON, the host and a timestamp, so an offline
launch shows the last real account with an age ("synced 12m ago") instead of
inventing values. A failed refresh keeps the old snapshot and reports the
error in the header and on the Account tab.

## Endpoints used

| Screen | Request |
| --- | --- |
| Account hero, career, clan tag | `GET /players/{tag}` |
| Clan card and member list | `GET /clans/{tag}` |
| War board | `GET /clans/{tag}/currentwar` |
| War log | `GET /clans/{tag}/warlog?limit=20` |
| Army tab | troop, hero, spell and pet levels inside the player document |

Nothing else is called. There is no login, no game client protocol and no
private endpoint: the app speaks the documented developer API only.

## Getting a token

1. Open <https://developer.clashofclans.com> and sign in with a Supercell ID.
2. **My Account → Create New API Key**.
3. Name it `COCPlugin`, describe it, and note the IP field.
4. Copy the token into the app's connect sheet, put the player tag above it and
   press **Test**; a green `OK` line shows the real name and trophy count.

The token is a JWT that is bound to the IP address(es) written into the key.
That is the one awkward part of the API and the source of the `403
accessDenied` error everyone hits:

- On Wi-Fi with a static address, allowlist that address.
- On mobile data the address changes constantly. Either create the key with a
  wide range (the portal accepts `0.0.0.0/0` on most accounts), or use a proxy.

## Proxy (recommended for phones)

Two drop-in proxies are in `proxy/`:

| File | Where it runs | Why |
| --- | --- | --- |
| `proxy/worker.js` | Cloudflare Workers | free, HTTPS out of the box |
| `proxy/server.js` | any VPS with a static IP | one allowlisted address, no dependencies |

Both keep the real token on the server and accept a **client key** in the same
`Authorization: Bearer …` header the API uses, so the app needs no special
support: paste the client key into the token field and set the base URL to
`https://your-proxy/v1`.

They also cache a successful read for 60 seconds, which keeps a household of
phones under the API rate limit.

## What the API cannot do

| Wish | Reality |
| --- | --- |
| Building and wall levels | not exposed by any official endpoint, so the Village tab is a local planner |
| Resources, gems | not exposed, so the gold/elixir strip is gone |
| Starting an upgrade, attacking, donating | not exposed; the app never writes to an account |
| Research timers | not exposed; the Army tab shows the level, not a countdown |
| Private war logs | the clan must make its log public, otherwise the endpoint answers 403 |

Those are limits of the server, not of the client. Nothing is faked to hide
them: the screen says what is missing.

## Privacy

- The token lives in the app's private `SharedPreferences`; `allowBackup` is
  `false` so it is not copied into a cloud backup.
- It is sent only to the base URL shown in the connect sheet.
- **Forget this account** in the connect sheet wipes the token and the cached
  account.
- The app has no analytics, no ads and no third-party SDK.

## Verifying the connection

```bash
# offline suites: parsers, request shape, error mapping (local HTTP server)
bash tests/run.sh

# real HTTPS round trip; without a token the API answers 403, which still
# proves DNS, TLS and the client work
bash tests/live.sh

# with a token: prints the real account, clan and war
COC_API_TOKEN=... COC_PLAYER_TAG=#YOURTAG bash tests/live.sh

# against a proxy
COC_API_BASE=https://your-proxy/v1 COC_API_TOKEN=client-key bash tests/live.sh
```
