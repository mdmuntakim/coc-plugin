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
| SHA-256 | `e6fb36afba3d35aac28729b5ba33b6350ad8a667d28891cb45158e02b9acc7ff` |
| Native code | none: the APK contains only `classes.dex` and resources |

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

## Notes

- The app has no `INTERNET` permission. Every value is entered or generated
  locally; the planner never queries a Supercell service.
- Timers are absolute end times in `SharedPreferences`, which is why the
  force-stop test above is meaningful.
- No game artwork, font or sound is bundled. Icons and the launcher foreground
  are original canvas/vector drawings in this repository.
