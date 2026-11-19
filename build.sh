#!/usr/bin/env bash
# ============================================================================
# build.sh -- build, test, sign and verify COCPlugin without Gradle
# ----------------------------------------------------------------------------
# The APK is produced with the Android SDK build tools directly: aapt2 compiles
# the resources, javac compiles the Java sources, d8 makes the dex, zipalign
# aligns and apksigner signs. No AndroidX, no Gradle daemon.
#
# The APK talks to the official Clash of Clans API at runtime; the build
# itself needs no network.
#
#   bash build.sh              # -> dist/COCPlugin-2.0.0.apk
#   VERSION=2.0.1 VERSION_CODE=3 bash build.sh
# ============================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

VERSION="${VERSION:-2.0.0}"
VERSION_CODE="${VERSION_CODE:-2}"
MIN_SDK="${MIN_SDK:-26}"
TARGET_SDK="${TARGET_SDK:-34}"

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
[ -n "$SDK" ] && [ -d "$SDK" ] || {
    echo "error: set ANDROID_HOME to an Android SDK" >&2
    exit 1
}

# Prefer a JDK 17 for the SDK tools; newer JDKs are fine for javac but the
# build tools are the most predictable on 17.
if [ -z "${JAVA_HOME:-}" ] && [ -d /usr/lib/jvm/temurin-17-jdk-amd64 ]; then
    export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
fi
if [ -n "${JAVA_HOME:-}" ] && [ -d "$JAVA_HOME/bin" ]; then
    PATH="$JAVA_HOME/bin:$PATH"
    export PATH
fi

BT="$(ls -d "$SDK"/build-tools/*/ 2>/dev/null | sort -V | tail -1)"
BT="${BT%/}"
[ -n "$BT" ] && [ -d "$BT" ] || { echo "error: no build-tools in $SDK" >&2; exit 1; }

ANDROID_JAR=""
for candidate in "$SDK/platforms/android-$TARGET_SDK/android.jar" \
                 "$SDK/platforms/android-35/android.jar" \
                 "$SDK/platforms/android-36/android.jar"; do
    if [ -f "$candidate" ]; then ANDROID_JAR="$candidate"; break; fi
done
if [ -z "$ANDROID_JAR" ]; then
    ANDROID_JAR="$(find "$SDK/platforms" -maxdepth 2 -name android.jar 2>/dev/null | sort -V | tail -1)"
fi
[ -f "${ANDROID_JAR:-}" ] || { echo "error: no android.jar in $SDK/platforms" >&2; exit 1; }

AAPT2="$BT/aapt2"
D8="$BT/d8"
ZIPALIGN="$BT/zipalign"
APKSIGNER="$BT/apksigner"
for tool in "$AAPT2" "$D8" "$ZIPALIGN" "$APKSIGNER"; do
    [ -x "$tool" ] || { echo "error: missing $tool" >&2; exit 1; }
done

KS="${COC_KEYSTORE:-$ROOT/keystore/coc-plugin.jks}"
KS_PASS="${COC_KEYSTORE_PASSWORD:-cocplugin}"
KEY_ALIAS="${COC_KEY_ALIAS:-cocplugin}"
KEY_PASS="${COC_KEY_PASSWORD:-cocplugin}"

echo "==> toolchain"
echo "    build-tools $(basename "$BT")"
echo "    platform    $(basename "$(dirname "$ANDROID_JAR")")"
echo "    java        $("$JAVA_HOME/bin/java" -version 2>&1 | head -1)"

# ---------------------------------------------------------------- keystore
if [ ! -f "$KS" ]; then
    echo "==> generating the release keystore"
    mkdir -p "$(dirname "$KS")"
    keytool -genkeypair -keystore "$KS" -alias "$KEY_ALIAS" \
        -storepass "$KS_PASS" -keypass "$KEY_PASS" \
        -keyalg RSA -keysize 2048 -validity 10950 \
        -dname "CN=COCPlugin, OU=Unofficial fan tool, O=mdmuntakim, C=US" >/dev/null 2>&1
fi

# ------------------------------------------------------------------ tests
echo "==> unit tests"
bash "$ROOT/tests/run.sh"

# -------------------------------------------------------------- resources
echo "==> compiling resources"
rm -rf build dist
mkdir -p build/gen build/classes build/dex dist
"$AAPT2" compile --dir app/res -o build/res.zip

echo "==> linking resources and manifest"
"$AAPT2" link \
    -o build/app-unsigned.apk \
    -I "$ANDROID_JAR" \
    --manifest app/AndroidManifest.xml \
    --java build/gen \
    --min-sdk-version "$MIN_SDK" \
    --target-sdk-version "$TARGET_SDK" \
    --version-code "$VERSION_CODE" \
    --version-name "$VERSION" \
    --auto-add-overlay \
    build/res.zip

# ------------------------------------------------------------------- java
echo "==> compiling Java sources"
find app/src build/gen -name '*.java' > build/sources.txt
"$JAVA_HOME/bin/javac" -encoding UTF-8 --release 11 \
    -classpath "$ANDROID_JAR" \
    -d build/classes \
    @build/sources.txt

# -------------------------------------------------------------------- dex
echo "==> dexing"
find build/classes -name '*.class' > build/classes.txt
"$D8" --release --min-api "$MIN_SDK" --lib "$ANDROID_JAR" \
    --output build/dex \
    @"build/classes.txt"

cp build/app-unsigned.apk build/app-with-dex.apk
(cd build/dex && zip -q -X "$ROOT/build/app-with-dex.apk" classes.dex)

echo "==> aligning and signing"
"$ZIPALIGN" -f -p 4 build/app-with-dex.apk build/app-aligned.apk
APK="dist/COCPlugin-$VERSION.apk"
"$APKSIGNER" sign \
    --ks "$KS" --ks-pass "pass:$KS_PASS" \
    --key-pass "pass:$KEY_PASS" --ks-key-alias "$KEY_ALIAS" \
    --min-sdk-version "$MIN_SDK" \
    --out "$APK" build/app-aligned.apk

# ----------------------------------------------------------------- verify
echo "==> verifying"
PKG="$("$AAPT2" dump packagename "$APK")"
[ "$PKG" = "plugin.clashofclans" ] || {
    echo "error: unexpected package $PKG" >&2
    exit 1
}
unzip -l "$APK" | grep -q 'classes.dex' || {
    echo "error: the APK has no classes.dex" >&2
    exit 1
}
"$APKSIGNER" verify --min-sdk-version "$MIN_SDK" --print-certs "$APK" \
    | sed 's/^/    /'

SIZE="$(stat -c %s "$APK")"
SHA="$(sha256sum "$APK" | cut -d' ' -f1)"
printf '%s\n' "$SHA  $(basename "$APK")" > dist/COCPlugin-"$VERSION".sha256

echo
echo "package : $PKG"
echo "version : $VERSION ($VERSION_CODE)"
echo "sdk     : min $MIN_SDK, target $TARGET_SDK"
echo "size    : $SIZE bytes"
echo "sha256  : $SHA"
echo "apk     : $APK"
