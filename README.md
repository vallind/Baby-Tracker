# MyApp

Android 原生应用，Kotlin + Jetpack Compose + Material 3。

## 技术栈

| 类别 | 方案 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 网络 | Retrofit + OkHttp + Gson |
| 图片 | Coil |
| 数据库 | Room + KSP |
| 异步 | Kotlin 协程 + Flow |
| 依赖注入 | Koin |
| 架构 | MVVM (ViewModel + StateFlow) |
| 构建 | Gradle 9.5.1 + AGP 8.9.3 |
| 最低 SDK | Android 7.0 (API 24) |
| 目标 SDK | Android 14 (API 34) |

## 构建

```bash
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_AAPT2_DAEMON_MODE=false
./gradlew assembleDebug
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 项目结构

```
MyApp/
├── app/
│   └── src/main/
│       ├── java/com/example/myapp/
│       │   ├── MainActivity.kt
│       │   └── ui/theme/
│       └── res/
├── build.gradle.kts       # 根构建文件
├── app/build.gradle.kts   # 模块构建文件
└── settings.gradle.kts
```

## 开发环境

- Termux (Android 终端)
- OpenJDK 21
- Android SDK 34
- Gradle 9.5.1
