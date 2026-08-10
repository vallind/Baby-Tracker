# 项目结构与模块索引

> 最后更新：2026-08-11 · 旧自建设计系统已删除，UI 基座为 Elyon
>
> 从 AGENTS.md 拆分，供需要定位代码时查阅。

## 目录结构

```
app/src/main/java/com/babytracker/
├── core/                        # 业务基础设施
│   ├── ui/                      # Elyon UI 基座
│   │   ├── ElyonAppTheme.kt     # 主题根（ElyonTheme 驱动）
│   │   ├── ElyonThemeResolver.kt# 主题名 → Elyon ThemeController 参数映射
│   │   ├── ThemeController.kt / DensityController.kt
│   │   ├── BlurPolicy.kt        # 毛玻璃启用策略（API 33 + 运行时着色器）
│   │   ├── AppDimens.kt         # AppSpacing / AppShapes（纯 dp 常量）
│   │   ├── Gradients.kt         # 渐变 Brush（消费 Elyon Colors）
│   │   └── components/          # 应用级组件（RecordCard/TimePicker/DateTimeCascade/AppInput/Snackbar/BottomNavBar…）
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
├── i18n/                        # AppStrings（全部用户可见文案）
├── feature/                     # 业务功能（16 个模块）
│   ├── home/feeding/sleep/diaper/growth/
│   ├── vaccination/health/stats/timeline/
│   ├── message/development/reminder/settings/
│   │   └── settings/            # SettingsMenuScreen（主题/密度入口）/ SettingsScreen（DensityPickerSheet/ThemePickerSheet）/ SettingsViewModel
│   └── ai/auth/family/
└── navigation/                  # elyon-nav 导航（AppNavigation.kt：NavDisplay + 25 条 Route + Navigator + AppRouteGraph）
```

仓库根级模块与构建配置：

```
detekt-rules/                    # detekt 自定义规则模块（HardcodedColor/TokenBypass，ServiceLoader 注册）
config/detekt/detekt.yml         # detekt 显式枚举配置（Termux 下 buildUponDefaultConfig 全量规则超时）
../elegant/                      # Elyon 复合构建（includeBuild，elyon-core/ui/effects/blur/nav）
```

退役门禁（`app/src/test/java/com/babytracker/`）：

```
core/ui/DesignSystemRetirementTest.kt   # 主源码禁止再出现 com.babytracker.designsystem
navigation/NavigationMigrationTest.kt   # 主源码禁止再出现 androidx.navigation.*
navigation/RouteGraphTest.kt            # 路由注册表完整性（25 条唯一）
core/ui/ElyonThemeResolverTest.kt       # 主题名 → Elyon 参数映射
core/ui/BlurPolicyTest.kt               # 毛玻璃启用策略
navigation/NavigatorTest.kt             # Navigator 栈语义
```

## 核心模块索引

| 诉求 | 去哪改 |
|---|---|
| 宝宝逻辑 | `core/util/BabyController.kt` / `core/data/repository/` |
| 喂养表单 | `feature/feeding/FeedingListScreen.kt` |
| 睡眠统计 | `feature/sleep/SleepListScreen.kt` + `feature/home/HomeViewModel.kt` |
| 生长图表 | `feature/growth/GrowthScreen.kt`（Canvas + WHO 参考线） |
| 疫苗计划 | `core/util/VaccineSchedule.kt`（21 条预设） |
| 主题/色板/排版 | `core/ui/ElyonAppTheme.kt` + `ElyonThemeResolver.kt`；颜色 `ElyonTheme.colorScheme`，排版 `ElyonTheme.textStyles` |
| 密度/无障碍 | `core/ui/DensityController.kt` + `DensityPickerSheet`（`feature/settings/SettingsScreen.kt`） |
| 应用级组件 | `core/ui/components/` |
| 国际化 | `i18n/AppStrings.kt` |
| 备份逻辑 | `core/backup/BackupManager.kt` |
| 数据库升级 | `core/database/AppDatabase.kt`（同步写 Migration + schema JSON） |
| 同步引擎 | `core/sync/SyncEngine.kt`（push/pull/fullSync/markExistingPending） |
| 同步触发 | `core/sync/SyncTrigger.kt` + `SyncWorker.kt`（WorkManager） |
| Realtime | `core/sync/RealtimeManager.kt` |
| AI 助手 | `core/ai/`（配置/供应商适配/安全校验）+ `feature/ai/`（聊天/设置） |
| 家庭/登录 | `core/data/FamilyService.kt` / `core/auth/AuthService.kt` |
| 导航/路由 | `navigation/AppNavigation.kt` |
| DI | `core/di/Modules.kt` |
| 毛玻璃策略 | `core/ui/BlurPolicy.kt` + `core/ui/components/BottomNavBar.kt` |

## 技术栈

| 层面 | 选型 |
|---|---|
| UI | Jetpack Compose + Elyon（vide/elegant 复合构建：elyon-core/ui/effects/blur/nav） |
| 导航 | elyon-nav（Route sealed interface + Navigator + NavDisplay） |
| 数据库 | Room 2.8.4 + KSP（version 8，15 张 @Entity，exportSchema 开启） |
| DI | Koin 4.2.1（`viewModel { }` 注册） |
| 异步 | Coroutines + Flow |
| 网络 | Retrofit 3.0.0 + OkHttp 5.4.0（WebDAV） + Supabase Kotlin BOM 3.6.0 |
| 文件 | DocumentFile 1.0.1（SAF） |
| 设置存储 | Jetpack DataStore + kotlinx.serialization（AppSettings） |
| 构建 | Java 21 / compileSdk 37 / minSdk 33（elyon-blur 要求）/ AGP 9.3.1（与 Elyon 复合构建统一） |
| 静态分析 | detekt 1.23.8（report-only，config/detekt/detekt.yml 显式枚举）+ :detekt-rules 自定义规则模块 |
