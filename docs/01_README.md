# 育儿助手 (Baby Tracker)

面向 0-6 岁儿童家庭的成长记录与健康管理 Android 应用。

## 快速启动

```bash
# 编译 debug 包
sh build.sh

# 编译 release 包
sh build.sh assembleRelease

# 运行单元测试
./gradlew app:testDebugUnitTest

# 运行 UI 测试（需连接设备）
./gradlew app:connectedAndroidTest
```

## 项目结构

```
app/
├── src/main/java/com/example/myapp/
│   ├── MyApp.kt                 # Application
│   ├── MainActivity.kt          # Single Activity
│   ├── di/AppModule.kt          # Koin DI
│   ├── domain/                  # UseCase 层
│   │   ├── feeding/
│   │   ├── sleep/
│   │   └── growth/
│   ├── data/
│   │   ├── event/UiEvent.kt     # MVI 全局事件总线
│   │   ├── room/                # Entity + DAO + Database
│   │   ├── repository/          # Repository 层
│   │   └── datastore/           # DataStore 偏好设置
│   ├── ui/
│   │   ├── designsystem/        # 设计系统组件
│   │   ├── home/
│   │   ├── feeding/
│   │   ├── sleep/
│   │   ├── growth/
│   │   ├── vaccine/
│   │   ├── health/
│   │   ├── stats/
│   │   ├── settings/
│   │   └── about/
│   ├── navigation/              # Route + AppNavGraph
│   ├── worker/ReminderWorker.kt # WorkManager 提醒
│   └── ui/theme/                # Material 3 主题
core/
├── data/                        # 数据层模块
└── designsystem/                # 设计系统模块
```

## 技术栈

| 组件 | 技术选型 |
|------|---------|
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose |
| 数据库 | Room 2.6.1 + KSP |
| DI | Koin 3.5.6 |
| 图表 | Vico Chart 1.13.1 |
| 图片 | Coil 2.6.0 |
| 偏好存储 | DataStore Preferences |
| 后台任务 | WorkManager |
| 架构 | MVVM + MVI (sealed UiEvent) + UseCase |

## 开发环境

- Kotlin 1.9.22 / Java 17
- AGP 8.9.3 / Gradle 9.5.1
- compileSdk 34 / targetSdk 34 / minSdk 24
