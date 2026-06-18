#!/data/data/com.termux/files/usr/bin/bash
set -e

AAPT2_SRC="$HOME/android-sdk/build-tools/34.0.0/aapt2"
export ANDROID_AAPT2_OVERRIDE="$AAPT2_SRC"

# Ensure the aapt2 binary exists in Gradle's transform cache
# (AGP downloads Linux x86_64 aapt2 which can't run on ARM)
CACHE_DIR="$HOME/.gradle/caches/9.5.1/transforms"
if [ -d "$CACHE_DIR" ]; then
    find "$CACHE_DIR" -path "*/transformed/*aapt2*/aapt2" -type f 2>/dev/null | while read f; do
        cp -f "$AAPT2_SRC" "$f"
    done
fi

./gradlew assembleRelease "$@"

# After first run, populate caches for subsequent runs
find "$CACHE_DIR" -path "*/transformed/*aapt2*/aapt2" -type f 2>/dev/null | while read f; do
    if ! cmp -s "$AAPT2_SRC" "$f"; then
        cp -f "$AAPT2_SRC" "$f"
    fi
done
