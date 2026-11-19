#!/usr/bin/env bash
# Runs the pure-JVM regression suite (no emulator, no Android SDK needed).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/build/test-classes"
JAVAC="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"

rm -rf "$OUT"
mkdir -p "$OUT"
"$JAVAC" -encoding UTF-8 --release 11 -d "$OUT" \
  "$ROOT"/app/src/plugin/clashofclans/model/*.java \
  "$ROOT"/tests/src/plugin/clashofclans/model/LogicTest.java

"$JAVA" -cp "$OUT" plugin.clashofclans.model.LogicTest
