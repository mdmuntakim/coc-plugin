# Acceptance record

Build and runtime checks that go with `COCPlugin 2.0.0`. The commands below are
the ones that were actually run for this build.

## Build

| Item | Value |
| --- | --- |
| Host | Ubuntu 24.04, JDK 17 (Temurin), 4 vCPU, 16 GiB |
| SDK | build-tools 37.0.0, platform android-34 |
| Command | `bash build.sh` |
| Unit tests | 502 model checks and 32 API checks, all passed (`bash tests/run.sh`) |
| Live probe | `bash tests/live.sh` reached `api.clashofclans.com` and got the expected `403 accessDenied` without a token |
| APK | `dist/COCPlugin-2.0.0.apk`, 78,634 bytes |
| Package | `plugin.clashofclans` (checked with `aapt2 dump packagename`) |
| SDK levels | min 26, target 34 |
| Permissions | `INTERNET`, `ACCESS_NETWORK_STATE` (checked with `aapt2 dump permissions`) |
| Signer | `CN=COCPlugin`, v3 signature, verified with `apksigner verify` |
| SHA-256 | local build `1deb504c…80b64f7`, see `dist/COCPlugin-2.0.0.sha256` |
| Native code | none: the APK contains only `classes.dex` and resources |
| Release | https://github.com/mdmuntakim/coc-plugin/releases/tag/coc-plugin-2.0.0 |

Because there is no native library, the APK is ABI independent. The runtime
matrix below was executed on an x86_64 emulator; the same dex runs on
`armeabi-v7a`, `arm64-v8a` and `x86`.

## Test commands

```bash
bash tests/run.sh     # 502 model checks + 32 API checks, local only
bash tests/live.sh    # real HTTPS request to the official API, no token needed
bash build.sh         # compile, sign, verify, write dist/COCPlugin-2.0.0.apk
```

`tests/run.sh` starts a local `HttpServer` and feeds it the fixtures in
`tests/fixtures/`; it asserts the request paths, the `Authorization` header,
gzip handling, URL encoding of `#TAGS`, and the 400/401/403/404/429/5xx
messages. `tests/live.sh` performs a real TLS request to the official host and
passes when the API answers at all, with or without a token.

## Runtime matrix

Device: Android 14 (API 34), `google_apis` x86_64, Pixel 5 AVD, 1080x2340,
hardware KVM, fresh install (`adb install`). Real requests were served by a
local fixture HTTP server through `adb reverse`, because no developer token
was available to the test machine.

| Check | Result |
| --- | --- |
| Cold start without settings: `NOT CONNECTED`, connect card, no invented profile | pass |
| Account/Village/Army/War/Tools tabs render, no crash | pass |
| Connect sheet: tag, token (masked), base URL, Test, Save, Forget | pass |
| Save and sync against the fixture server: `LIVE · localhost` | pass |
| Server log shows `GET /v1/players/%232PP`, clans, currentwar, warlog with `Authorization` | pass |
| Account tab shows the fixture player (tag, TH 16, 5,120 trophies, league, clan) | pass |
| Army tab shows unit levels from the payload; no lab queue is invented | pass |
| War tab shows state, stars, destruction and the public war log | pass |
| Official host with an invalid token: `NOT SYNCED · api.clashofclans.com` and the 403 IP-lock message | pass |
| Stop the fixture server, tap Sync: `OFFLINE COPY · localhost`, cached data stays, error toast | pass |
| Village tab remains an explicitly labelled local planner (the API has no building levels) | pass |
| Tools tab: season clock and loot calculator only; bank/lab simulation removed | pass |
| Rotation portrait -> landscape -> portrait | pass |
| `logcat` after the whole run: no FATAL, no ANR | pass |

The fixture server is test equipment, not part of the app. It requires an
`Authorization` header and returns the JSON in `tests/fixtures/`, so the checks
above prove the transport, parsing and UI paths, not the contents of a live
account.

## What needs a real token

The official API is the only data source and it requires a developer token
from https://developer.clashofclans.com. Tokens are bound to an IP range, so a
token created for a laptop will not work from a phone on mobile data. Two
supported ways around that:

- create the token with a wide enough IP range, or
- run `proxy/server.js` or `proxy/worker.js` with the token on the server and
  point the app's base URL at it.

The app never ships a token and never falls back to sample data. Without a
token the screens stay empty and show the API error.

## Screenshots

`docs/screenshots/account.png`, `village.png`, `army.png`, `war.png`,
`tools.png` are frames captured from the 1.0.0 offline build. They are kept for
layout reference only; the 2.0.0 screens show live data instead of the seeded
profile.

## Release asset

The published 2.0.0 APK is `COCPlugin-2.0.0.apk` with the source archive
`coc-plugin-coc-plugin-2.0.0-source.tar.gz` and `SHA256SUMS` next to it. A
rebuild is not byte-for-byte reproducible: zip entry timestamps and the
signing block change, so a fresh `bash build.sh` produces a different hash with
the same package name and signing certificate.

## Notes

- `INTERNET` and `ACCESS_NETWORK_STATE` are the only permissions. Cleartext is
  allowed only for `localhost`, `127.0.0.1` and `10.0.2.2` (see
  `app/res/xml/network_security_config.xml`); every other base URL must be
  HTTPS.
- Raw API answers are cached in `SharedPreferences` with a timestamp and the
  host name. Offline the last real copy is shown with its age, never a
  generated one.
- `allowBackup` is false and the token is stored in the app's private prefs.
  Use the proxy if the token must not live on the device at all.
- No game artwork, font or sound is bundled. Icons and the launcher foreground
  are original canvas/vector drawings in this repository.
