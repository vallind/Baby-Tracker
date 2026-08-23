# 项目结构与模块索引

> 最后更新：2026-08-23 · 对应版本：2.2.0
>
> 从 AGENTS.md 拆分，供需要定位代码时查阅。

## 目录结构

```
app/src/main/java/com/babytracker/
├── designsystem/                # 设计系统（主题令牌 + 可复用组件 + Hooks + i18n）
│   ├── theme/                   # AppTokens（核心令牌）/ AppComponentTokens（组件令牌）/ AppDensity 密度体系（含 DensityController.kt）/ Theme.kt
│   ├── components/              # 可复用组件（29 个目录 + 根级组件，含 Defaults）
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
│   └── util/                    # BabyController/DateUtils/VaccineSchedule/NetworkMonitor/TokenAuditChecker（令牌审计检查器，Gradle 任务与 JVM 单测双路复用）
├── feature/                     # 业务功能（16 个模块）
│   ├── home/feeding/sleep/diaper/growth/
│   ├── vaccination/health/stats/timeline/
│   ├── message/development/reminder/settings/
│   │   └── settings/            # SettingsMenuScreen（使用偏好：界面密度入口）/ SettingsScreen（DensityPickerSheet/ThemePickerSheet）/ SettingsViewModel
│   └── ai/auth/family/
└── navigation/                  # 导航（AppNavigation.kt，类型安全 @Serializable 路由，25+ 页面）
```

仓库根级模块与构建配置：

```
detekt-rules/                    # detekt 自定义规则模块（HardcodedColor/TokenBypass，ServiceLoader 注册；单测 HardcodedColorRuleTest/TokenBypassRuleTest）
config/detekt/detekt.yml         # detekt 显式枚举配置（Termux 下 buildUponDefaultConfig 全量规则超时，见文件头注释）
```

令牌审计门禁：`app/build.gradle.kts` 注册 `themeTokenAudit` JavaExec 任务（group verification），classpath 直接引用 `debugCompileClasspath`（AGP 9 无 sourceSets 容器，lessons #17），详见 `docs/design-system.md`「令牌审计门禁」。

设计系统相关测试（`app/src/test/java/com/babytracker/`）：

```
designsystem/
├── theme/                       # DensityTokensTest / ComponentTokensStateAuditTest / ThemeTokenizationStaticAuditTest / TypographyTokensTest / AppColorScaleTest
├── components/                  # A11ySemanticsAuditTest（自定义可交互组件语义静态审计）
├── showcase/                    # DesignShowcaseTest（Paparazzi 截图 Showcase，仅 x86_64 可跑，见 docs/design-system.md）
└── core/util/                   # TokenAuditCheckerTest（共享检查器拦截/白名单/防呆样本）
```

无障碍基线文档：`docs/a11y-baseline.md`（组件语义承诺表 + 装饰隔离 + 审计说明）。

## 核心模块索引

| 诉求 | 去哪改 |
|---|---|
| 宝宝逻辑 | `core/util/BabyController.kt` / `core/data/repository/` |
| 喂养表单 | `feature/feeding/FeedingListScreen.kt` |
| 睡眠统计 | `feature/sleep/SleepListScreen.kt` + `feature/home/HomeViewModel.kt` |
| 生长图表 | `feature/growth/GrowthScreen.kt`（Canvas + WHO 参考线） |
| 疫苗计划 | `core/util/VaccineSchedule.kt`（21 条预设） |
| 核心令牌 | `designsystem/theme/AppTokens.kt` — AppColors(42字段)/Spacing/Shapes/Elevation/Opacity/Motion/Typography |
| 组件令牌 | `designsystem/theme/AppComponentTokens.kt` — 37 种组件令牌（derive{} 部分覆盖为 TODO，未实现）；AppDensityTokens 为非组件令牌（见 design-system.md） |
| 密度/无障碍 | `designsystem/theme/DensityController.kt` + `DensityPickerSheet`（`feature/settings/SettingsScreen.kt`）；`docs/a11y-baseline.md` |
| 组件库 | `designsystem/components/`（29 个组件目录 + 根级组件，含 Defaults；badge/statcell/actionbar 为收敛批次新增） |
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
| 令牌审计门禁 | `core/util/TokenAuditChecker.kt` + `app/build.gradle.kts`（themeTokenAudit 任务）+ `detekt-rules/`（HardcodedColor/TokenBypass）+ `config/detekt/detekt.yml` |

## 技术栈

| 层面 | 选型 |
|---|---|
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose 2.9.1（类型安全 @Serializable 路由，25+ 页面） |
| 数据库 | Room 2.8.4 + KSP（version 8，15 张 @Entity，exportSchema 开启） |
| DI | Koin 4.2.1（`viewModel { }` 注册） |
| 异步 | Coroutines + Flow |
| 网络 | Retrofit 3.0.0 + OkHttp 5.4.0（WebDAV） + Supabase Kotlin BOM 3.6.0 |
| 文件 | DocumentFile 1.0.1（SAF） |
| 设置存储 | Jetpack DataStore + kotlinx.serialization（AppSettings） |
| 构建 | Java 17 / compileSdk 36 / minSdk 24 |
| 静态分析 | detekt 1.23.8（report-only，config/detekt/detekt.yml 显式枚举）+ :detekt-rules 自定义规则模块 |
