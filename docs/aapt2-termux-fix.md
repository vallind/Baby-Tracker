# Termux 环境 AAPT2 问题

## 问题

Termux 系统包 `aapt2` (v13.0.0.6-23, AAPT internal 2.19) 无法加载 `android-35+` 的 android.jar，报错：

```
ERROR: AAPT: error: failed to load include path .../platforms/android-XX/android.jar.
```

## 解决方案

从 [ReVanced/aapt2](https://github.com/ReVanced/aapt2) 下载预编译的 `aapt2-arm64-v8a`（基于 build-tools 35.0.2），替换系统 aapt2。

### 步骤

```bash
# 1. 下载最新版 aapt2-arm64-v8a
curl -L -o $PREFIX/bin/aapt2 \
  "https://github.com/ReVanced/aapt2/releases/latest/download/aapt2-arm64-v8a"
chmod +x $PREFIX/bin/aapt2

# 2. 验证
aapt2 version

# 3. 确保 gradle.properties 中有 aapt2 覆盖路径
echo "android.aapt2FromMavenOverride=$PREFIX/bin/aapt2" >> gradle.properties

# 4. 编译验证
./gradlew assembleDebug
```

### 验证

```bash
aapt2 link -o /dev/null \
  -I $PREFIX/opt/android-sdk/platforms/android-36/android.jar \
  --manifest app/src/main/AndroidManifest.xml 2>&1 | head -3
```

## 原理

| 问题 | 原因 | 解决 |
|------|------|------|
| `failed to load include path` | Termux 系统 aapt2 版本过旧 | 替换为 ReVanced 预编译版 |
| `Daemon startup failed` | AGP 自带的 aapt2 是 x86_64，无法在 AArch64 上运行 | `aapt2FromMavenOverride` 强制使用本地 aapt2 |
| `syntax error: unexpected '('` | glibc aapt2 在 Termux (bionic libc) 上无法运行 | 同上的 override 方案 |

## SDK 版本管理

由于新版 AndroidX 库不断推高最低 compileSdk 要求（如 lifecycle 2.11.0 需 SDK 37），推荐策略：

- `compileSdk` 和 `targetSdk` 保持同步升级
- 安装对应 API 级别的 platform：`sdkmanager "platforms;android-XX"`
- 更新 `gradle/libs.versions.toml` 中的依赖版本
- 验证 `$PREFIX/bin/aapt2` 能加载目标 android.jar

## 参考

- [ReVanced/aapt2 releases](https://github.com/ReVanced/aapt2/releases)
- [Termux PR #23671](https://github.com/termux/termux-packages/pull/23671)
- [Termux PR #28994](https://github.com/termux/termux-packages/pull/28994)
