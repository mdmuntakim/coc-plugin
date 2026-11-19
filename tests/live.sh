#!/usr/bin/env bash
# Live smoke test against the real Clash of Clans API.
#
#   COC_API_TOKEN=... COC_PLAYER_TAG=#YOURTAG bash tests/live.sh
#
# With no token the probe still performs a real HTTPS request: the API answers
# 401/403, which proves DNS, TLS and the client all work. Set COC_API_BASE to
# test a proxy instead of api.clashofclans.com.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/build/test-classes"
JAVAC="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"

mkdir -p "$OUT"
"$JAVAC" -encoding UTF-8 --release 11 -d "$OUT" \
  "$ROOT"/app/src/plugin/clashofclans/model/*.java \
  "$ROOT"/app/src/plugin/clashofclans/net/*.java \
  "$ROOT"/tests/src/plugin/clashofclans/net/LiveProbe.java

"$JAVA" -cp "$OUT" plugin.clashofclans.net.LiveProbe
