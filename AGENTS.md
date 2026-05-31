# MyApp - Android 原生项目

## 技术栈（约束，请严格遵守）
- UI：Jetpack Compose + Material 3 组件
- 网络：Retrofit
- 图片：Coil
- 数据库：Room
- 异步：Kotlin 协程 + Flow
- 依赖注入：Koin（比 Hilt 配置更少，对 AI 更友好）
- 架构：MVVM（ViewModel + StateFlow）
- 构建：Gradle 9.5.1 + AGP 8.9.3
- compileSdk / targetSdk: 34, minSdk: 24

## 构建
```bash
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_AAPT2_DAEMON_MODE=false
./gradlew assembleDebug
```

## 目录结构
```
MyApp/
  app/src/main/java/com/example/myapp/
    MainActivity.kt      入口 Activity
    ui/theme/            主题文件（Color, Type, Theme）
  app/src/main/res/      资源文件
  build.gradle.kts       根构建文件
  app/build.gradle.kts   模块构建文件
```
