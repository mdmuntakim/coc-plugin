#!/usr/bin/env bash
# Runs the pure-JVM regression suites (no emulator, no Android SDK needed).
#
#   LogicTest  - planner maths and the JSON/API parsers, against fixtures
#   ApiTest    - the real HTTP client against a local server: headers, gzip,
#                401/403/404/429 mapping, url encoding, timeouts
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/build/test-classes"
JAVAC="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"

rm -rf "$OUT"
mkdir -p "$OUT"
"$JAVAC" -encoding UTF-8 --release 11 -d "$OUT" \
  "$ROOT"/app/src/plugin/clashofclans/model/*.java \
  "$ROOT"/app/src/plugin/clashofclans/net/*.java \
  "$ROOT"/tests/src/plugin/clashofclans/model/LogicTest.java \
  "$ROOT"/tests/src/plugin/clashofclans/net/ApiTest.java \
  "$ROOT"/tests/src/plugin/clashofclans/net/LiveProbe.java

"$JAVA" -Dfixtures="$ROOT/tests/fixtures" -cp "$OUT" \
  plugin.clashofclans.model.LogicTest
"$JAVA" -Dfixtures="$ROOT/tests/fixtures" -cp "$OUT" \
  plugin.clashofclans.net.ApiTest
