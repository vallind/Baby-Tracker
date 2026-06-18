# Termux 环境编译 SDK 35 的 AAPT2 问题

## 问题

Termux 系统包 `aapt2` (v13.0.0.6-23, AAPT internal 2.19) 无法加载 `android-35/android.jar`，报错：

```
ERROR: AAPT: error: failed to load include path .../platforms/android-35/android.jar.
```

原因：Termux 仓库的 aapt2 包未更新到支持 android-35 的版本。

## 解决方案

从 [ReVanced/aapt2](https://github.com/ReVanced/aapt2) 下载预编译的 `aapt2-arm64-v8a`（基于 build-tools 35.0.2），替换系统 aapt2。

### 步骤

```bash
# 1. 下载 aapt2-arm64-v8a（v1.1.0，build-tools 35.0.2）
curl -L -o $PREFIX/bin/aapt2 \
  "https://github.com/ReVanced/aapt2/releases/download/v1.1.0/aapt2-arm64-v8a"
chmod +x $PREFIX/bin/aapt2

# 2. 验证
aapt2 version
# 应该输出: Android Asset Packaging Tool (aapt) 2.19-OS3.0.303.0.WPKCNXM

# 3. 替换 AGP 缓存的 aapt2（避免 Daemon startup failed）
AGP_AAPT2_DIR=$(find ~/.gradle/caches -name "aapt2-*-linux" -type d 2>/dev/null | head -1)
if [ -n "$AGP_AAPT2_DIR" ]; then
  cp $PREFIX/bin/aapt2 "$AGP_AAPT2_DIR/aapt2"
  chmod +x "$AGP_AAPT2_DIR/aapt2"
fi

# 4. 替换 build-tools 35.0.0 的 aapt2（如有）
if [ -f $PREFIX/opt/android-sdk/build-tools/35.0.0/aapt2 ]; then
  cp $PREFIX/bin/aapt2 $PREFIX/opt/android-sdk/build-tools/35.0.0/aapt2
fi

# 5. 确保 gradle.properties 中有 aapt2 覆盖路径
echo "android.aapt2FromMavenOverride=$PREFIX/bin/aapt2" >> gradle.properties

# 6. 编译验证
./gradlew assembleDebug
```

### 验证

```bash
# 测试新 aapt2 能否加载 android-35
aapt2 link -o /dev/null \
  -I $PREFIX/opt/android-sdk/platforms/android-35/android.jar \
  --manifest app/src/main/AndroidManifest.xml 2>&1 | head -3
# 如果没有 "failed to load include path" 错误即正常
```

## 原理

| 问题 | 原因 | 解决 |
|------|------|------|
| `failed to load include path` | Termux 系统 aapt2 版本过旧，不支持 android-35 的 android.jar | 替换为 ReVanced 预编译版 |
| `Daemon startup failed` | AGP 自带的 aapt2-linux 是 x86_64，无法在 AArch64 上运行 | 将 AGP 缓存中的 x86_64 aapt2 替换为 AArch64 版 |
| `syntax error: unexpected '('` | Gradle 下载的 glibc aapt2 在 Termux (bionic libc) 上无法运行 | 通过 gradle.properties 的 `aapt2FromMavenOverride` 指定 Termux 本地 aapt2 |

## 参考

- [ReVanced/aapt2 releases](https://github.com/ReVanced/aapt2/releases)
- [Termux PR #23671](https://github.com/termux/termux-packages/pull/23671) — aapt bump to 15.0.0.23
- [Termux PR #28994](https://github.com/termux/termux-packages/pull/28994) — aapt bump to buildtools 35.0.2
- [Termux Issue #26858](https://github.com/termux/termux-packages/issues/26858) — aapt2 outdated
