# Acceptance record

Build and runtime checks that go with `COCPlugin 1.0.0`. The commands below are
the ones that were actually run.

## Build

| Item | Value |
| --- | --- |
| Host | Ubuntu 24.04, JDK 17 (Temurin), 4 vCPU, 16 GiB |
| SDK | build-tools 37.0.0, platform android-34 |
| Command | `bash build.sh` |
| Unit tests | 429 checks, all passed |
| APK | `dist/COCPlugin-1.0.0.apk`, 58,073 bytes |
| Package | `plugin.clashofclans` (checked with `aapt2 dump packagename`) |
| SDK levels | min 26, target 34 |
| Signer | `CN=COCPlugin`, v3 signature, verified with `apksigner verify` |
| SHA-256 | local build `e6fb36af…acc7ff`; published 1.0.0 asset `4fcc807b…d4a9f` |
| Native code | none: the APK contains only `classes.dex` and resources |
| Release | https://github.com/mdmuntakim/coc-plugin/releases/tag/coc-plugin-1.0.0 |

Because there is no native library, the APK is ABI independent. The runtime
matrix below was executed on an x86_64 emulator; the same dex runs on
`armeabi-v7a`, `arm64-v8a` and `x86`.

## Runtime matrix

Device: Android 14 (API 34), `google_apis` x86_64, Pixel 5 AVD, 1080x2340,
hardware KVM, fresh install (`adb install`).

| Check | Result |
| --- | --- |
| Cold start, no crash, `MainActivity` resumed | pass |
| Account tab: hero card, XP meter, league ladder, career, clan | pass |
| Village tab: builders, 16 buildings, costs and times | pass |
| Start a builder upgrade, countdown and progress bar | pass |
| Force-stop and relaunch: the upgrade timer and level are restored | pass |
| Army tab: lab card, 22 troops, research rows | pass |
| War tab: state, live countdown, stars, attacks, log | pass |
| Record a 2-star attack (7 -> 9 stars, attacks 9 -> 10) | pass |
| Finish war (win added to the log, 132 -> 133) | pass |
| Start a new war (opponent, 24 h preparation countdown) | pass |
| Tools tab: season clock, loot calculator, planner | pass |
| Calculate 4 raids and bank them (chips 2.5M -> 4.7M gold) | pass |
| Edit profile, Save (2,450 -> 3,000 trophies, league updates) | pass |
| Soft keyboard open: Save/Cancel stay above the IME | pass |
| Settings sheet, switch to Dark Elixir accent, app recreates | pass |
| Reset the local profile from settings | pass |
| Rotation portrait -> landscape -> portrait | pass |
| `logcat` after the whole run: no FATAL, no ANR | pass |

## Screenshots

`docs/screenshots/account.png`, `village.png`, `army.png`, `war.png`,
`tools.png` are the frames captured on the emulator above.

## Release asset

The published 1.0.0 APK was downloaded again from the release, installed on a
clean emulator, launched to the Account screen and checked for FATAL entries in
`logcat`; it is the same build as the matrix above. Its SHA-256 is
`4fcc807b188432535815522e730192c680d944599428976c6c1e96b25f6d4a9f`.

A rebuild is not byte-for-byte reproducible: zip entry timestamps and the
signing block change, so a fresh `bash build.sh` produces a different hash with
the same package name and signing certificate.

## Notes

- The app has no `INTERNET` permission. Every value is entered or generated
  locally; the planner never queries a Supercell service.
- Timers are absolute end times in `SharedPreferences`, which is why the
  force-stop test above is meaningful.
- No game artwork, font or sound is bundled. Icons and the launcher foreground
  are original canvas/vector drawings in this repository.
