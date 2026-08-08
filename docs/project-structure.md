# 项目结构与模块索引

> 最后更新：2026-08-08 · 对应版本：1.7.11
>
> 从 AGENTS.md 拆分，供需要定位代码时查阅。

## 目录结构

```
app/src/main/java/com/babytracker/
├── designsystem/                # 设计系统（主题令牌 + 可复用组件 + Hooks + i18n）
│   ├── theme/                   # AppTokens（核心令牌）/ AppComponentTokens（组件令牌）/ Theme.kt
│   ├── components/              # 可复用组件（21+ 个，含 Defaults）
│   ├── hooks/                   # useDebounce/useState/useLatestState + Logic 类
│   ├── i18n/                    # AppStrings
│   ├── foundation/              # BorderContainer/CenterVerticallyRow
│   └── util/                    # AppDefaults 快照
├── core/                        # 业务基础设施
│   ├── ai/                      # AI 配置协调/供应商适配/回答安全校验
│   ├── auth/                    # AuthService
│   ├── backup/                  # BackupManager
│   ├── database/                # Room（AppDatabase/Entities/Daos，version 8）
│   ├── di/                      # Koin Modules
│   ├── data/                    # Repository + FamilyService + Mapper
│   ├── domain/                  # Domain Models
│   ├── settings/                # AppSettings（DataStore 设置聚合）
│   ├── sync/                    # SyncEngine / SyncTrigger / RealtimeManager / SyncWorker
│   └── util/                    # BabyController/DateUtils/VaccineSchedule/NetworkMonitor
├── feature/                     # 业务功能（16 个模块）
│   ├── home/feeding/sleep/diaper/growth/
│   ├── vaccination/health/stats/timeline/
│   ├── message/development/reminder/settings/
│   └── ai/auth/family/
└── navigation/                  # 导航（AppNavigation.kt，sealed class Screen 25+ 路由）
```

## 核心模块索引

| 诉求 | 去哪改 |
|---|---|
| 宝宝逻辑 | `core/util/BabyController.kt` / `core/data/repository/` |
| 喂养表单 | `feature/feeding/FeedingListScreen.kt` |
| 睡眠统计 | `feature/sleep/SleepListScreen.kt` + `feature/home/HomeViewModel.kt` |
| 生长图表 | `feature/growth/GrowthScreen.kt`（Canvas + WHO 参考线） |
| 疫苗计划 | `core/util/VaccineSchedule.kt`（21 条预设） |
| 核心令牌 | `designsystem/theme/AppTokens.kt` — AppColors(39字段)/Spacing/Shapes/Elevation/Opacity/Motion/Typography |
| 组件令牌 | `designsystem/theme/AppComponentTokens.kt` — 21+ 种组件令牌（derive{} 部分覆盖为 TODO，未实现） |
| 组件库 | `designsystem/components/`（21+ 个可复用组件 + Defaults） |
| 国际化 | `designsystem/i18n/AppStrings.kt` |
| Hooks/Logic | `designsystem/hooks/Hooks.kt` + `ButtonLogic.kt`/`FormLogic.kt`/`TableLogic.kt` |
| 备份逻辑 | `core/backup/BackupManager.kt` |
| 数据库升级 | `core/database/AppDatabase.kt`（同步写 Migration + schema JSON） |
| 同步引擎 | `core/sync/SyncEngine.kt`（push/pull/fullSync/markExistingPending） |
| 同步触发 | `core/sync/SyncTrigger.kt` + `SyncWorker.kt`（WorkManager） |
| Realtime | `core/sync/RealtimeManager.kt` |
| AI 助手 | `core/ai/`（配置/供应商适配/安全校验）+ `feature/ai/`（聊天/设置） |
| 家庭/登录 | `core/data/FamilyService.kt` / `core/auth/AuthService.kt` |
| 导航/路由 | `navigation/AppNavigation.kt` |
| DI | `core/di/Modules.kt` |

## 技术栈

| 层面 | 选型 |
|---|---|
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose（sealed class Screen，25+ 路由） |
| 数据库 | Room 2.8.4 + KSP（version 8，15 张 @Entity，exportSchema 开启） |
| DI | Koin 4.2.1（`viewModel { }` 注册） |
| 异步 | Coroutines + Flow |
| 网络 | Retrofit 3.0.0 + OkHttp 5.4.0（WebDAV） + Supabase Kotlin BOM 3.6.0 |
| 文件 | DocumentFile 1.0.1（SAF） |
| 设置存储 | Jetpack DataStore + kotlinx.serialization（AppSettings） |
| 构建 | Java 17 / compileSdk 36 / minSdk 24 |
