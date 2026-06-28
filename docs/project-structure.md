# 项目结构与模块索引

> 从 AGENTS.md 拆分，供需要定位代码时查阅。

## 目录结构

```
app/src/main/java/com/babytracker/
├── designsystem/                # 设计系统（32+ 文件）
│   ├── theme/                   # 主题 + Token + Defaults
│   ├── components/              # 可复用组件（21+ 个）
│   ├── hooks/                   # useDebounce/useState/useLatestState + Logic 类
│   ├── i18n/                    # AppStrings
│   ├── foundation/              # BorderContainer/CenterVerticallyRow
│   └── util/                    # AppDefaults 快照
├── core/                        # 业务基础设施
│   ├── backup/                  # BackupManager
│   ├── database/                # Room（AppDatabase/Entities/Daos）
│   ├── di/                      # Koin Modules
│   ├── data/                    # Repository + Mapper
│   ├── domain/                  # Domain Models
│   └── util/                    # BabyController/DateUtils/VaccineSchedule
├── feature/                     # 业务功能（13 个模块）
│   ├── home/feeding/sleep/diaper/growth/
│   ├── vaccination/health/stats/timeline/
│   └── message/development/reminder/settings/
└── navigation/                  # 导航
    └── AppNavigation.kt
```

## 核心模块索引

| 诉求 | 去哪改 |
|---|---|
| 宝宝逻辑 | `core/util/BabyController.kt` / `core/data/repository/` |
| 喂养表单 | `feature/feeding/FeedingListScreen.kt` |
| 睡眠统计 | `feature/sleep/SleepListScreen.kt` + `feature/home/HomeViewModel.kt` |
| 生长图表 | `feature/growth/GrowthScreen.kt`（Canvas + WHO 参考线） |
| 疫苗计划 | `core/util/VaccineSchedule.kt`（21 条预设） |
| 主题色 | `designsystem/theme/DesignTokens.kt`（遗留，仅兼容） |
| 核心令牌 | `designsystem/theme/AppTokens.kt` — AppColors(39字段)/Spacing/Shapes/Elevation/Opacity/Motion/ControlSizeTokens |
| 组件令牌 | `designsystem/theme/AppComponentTokens.kt` — 21 种组件令牌 + derive() 部分覆盖 |
| 组件库 | `designsystem/components/`（21+ 个可复用组件 + Defaults） |
| 国际化 | `designsystem/i18n/AppStrings.kt` |
| Hooks/Logic | `designsystem/hooks/Hooks.kt` + `ButtonLogic.kt`/`FormLogic.kt`/`TableLogic.kt` |
| 备份逻辑 | `core/backup/BackupManager.kt` |
| 数据库升级 | `core/database/AppDatabase.kt` + `core/database/Entities.kt`（同步写 Migration） |
| 导航/路由 | `navigation/AppNavigation.kt` |
| DI | `core/di/Modules.kt` |

## 技术栈

| 层面 | 选型 |
|---|---|
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose（11 路由） |
| 数据库 | Room 2.8.4 + KSP（8 表） |
| DI | Koin 4.2.1（`viewModel { }` 注册） |
| 异步 | Coroutines + Flow |
| 网络 | Retrofit 3.0.0（WebDAV） |
| 文件 | DocumentFile 1.0.1（SAF） |
| 构建 | JDK 21 / SDK 36 / minSdk 24 |
