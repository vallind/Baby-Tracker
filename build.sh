#!/system/bin/sh
# Termux 编译脚本
# 用法: sh build.sh [assembleDebug|assembleRelease]

TASK="${1:-assembleDebug}"

export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_AAPT2_DAEMON_MODE=false

echo "==> Gradle $TASK"
./gradlew "$TASK"
