# AGENTS.md

本文件是本仓库的唯一 agent 规范入口（`CLAUDE.md` 指向此处）。内容以当前代码为准，改动后请同步更新本文。

## 1. 项目概览

Android 原生宝宝护理记录 App。单 module（`:app`）+ 自定义 detekt 规则模块（`:detekt-rules`），Jetpack Compose + Material 3，MVVM + Koin + Room + Supabase 家庭云同步。

| 维度 | 选型（版本见 `gradle/libs.versions.toml`） |
|---|---|
| 构建 | Gradle 9.7.1 · AGP 9.3.1 · Kotlin 2.4.10 · KSP 2.3.11 · Java 17 |
| 目标 | compileSdk/targetSdk 36 · minSdk 24 · versionName 2.4.0 · versionCode 48 |
| UI | Compose BOM + Material 3 · Navigation Compose 2.9.1（类型安全 @Serializable 路由） |
| DI / 异步 | Koin 4.2.1 · Coroutines + Flow · WorkManager |
| 数据 | Room 2.8.4（exportSchema 开启）· DataStore · kotlinx-serialization |
| 云 | Supabase BOM 3.6.0（PostgREST/Realtime/Auth/Storage）· Edge Function `supabase/functions/ai-bootstrap` |
| 网络 | Retrofit 3.0.0 + OkHttp 5.4.0（WebDAV 备份）· Coil 2.7.0 |
| 质量 | detekt 1.23.8（含自定义规则）· Paparazzi 截图测试 · JUnit4 |

## 2. 常用命令

```bash
./gradlew assembleDebug        # 构建调试包
./gradlew testDebugUnitTest    # JVM 单元测试 —— 任何改动后必跑，全绿才算完成
./gradlew themeTokenAudit      # 令牌化静态审计（TokenAuditChecker，违规 exit 1）
./gradlew detekt               # 静态分析（config/detekt/detekt.yml）
./gradlew lint                 # Android Lint
./gradlew assembleRelease      # 发布包（R8 + 资源压缩）
```

- detekt 配置为 report-only（`ignoreFailures = true`）：不阻断构建，但必须人工查看报告，不得对新增违规视而不见。
- Paparazzi 截图测试在 aarch64 宿主被自动排除（`app/build.gradle.kts` 按 `os.arch` 过滤）；x86_64 上照常执行。

## 3. 分层架构与依赖红线

```
app/src/main/java/com/babytracker/
├── designsystem/   # 纯 UI 层：theme 令牌 / components / foundation / hooks / i18n(AppStrings 通用区)
├── ui/             # app 层 UI 框架：patterns/（业务形态模式：settings records dashboard chat avatar）
│                   #   framework/（FormLogic/TableLogic）· i18n/（AppStringsProduct 产品域文案）
├── core/           # 基础设施：ai / auth / backup / database / data(repository) / di / domain /
│                   #   settings(DataStore) / sync / util
├── feature/        # 业务模块 ×16：home feeding sleep diaper growth vaccination health stats
│                   #   timeline message development reminder settings ai auth family
└── navigation/     # AppNavigation.kt：类型安全路由集中定义
```

依赖方向只允许 `feature → core`、`feature → designsystem`、`feature → ui`、`ui → designsystem`、`core → designsystem`（如需）。以下红线由静态审计测试自动守门：

1. **designsystem 是纯 UI 层**：禁止 import `core` / `feature` / `navigation` / `androidx.navigation` / `org.koin`（`DesignSystemBoundaryAuditTest`）。UI 长什么样它负责；业务、数据、导航、DI 一概不知。
2. **Screen 只做展示**：`feature/**/*Screen.kt` 禁止 import navigation / koin / Repository / 各 Controller（`ScreenBoundaryAuditTest`）。Screen 只收 `viewModel + state + 回调`；DI 与导航放在同名 `*Route.kt`。
3. **feature 层禁止新定义通用卡片容器**（`*Card` 命名的 Composable，审计规则 `FeatureLayerGenericCard`）：一律消费设计系统的 `AppCard` 三轴模型。
4. **动画时长必须走令牌**：禁止 `tween(<字面量毫秒>)`，时长一律读 `LocalAppMotion`（审计规则 `MotionHardcodedDuration`）。
5. **业务代码禁止直用原生 M3 组件与令牌**：不直接 import material3 控件、不读 `MaterialTheme.colorScheme/typography/shapes`；使用 `App*` 组件与 AppTokens 体系。theme 桥接层豁免。
6. **`app/ui` 与 DesignSystem 同边界**（四层架构 Patterns 层，见 `docs/designsystem/component-taxonomy.md` §〇.4）：`ui/patterns/` 与 `ui/framework/` 禁止 import `core` / `feature` / `navigation` / `androidx.navigation` / `org.koin`，且**禁止回流 designsystem**（只消费公开组件与 `*Defaults` 工厂，不直读 `LocalAppComponentTokens` 等 L3 令牌——Patterns 层新增代码按此守门）。

## 4. 设计系统约定

- 核心语义令牌在 `designsystem/theme/AppTokens.kt`（`AppColors.light()/dark()`、`LocalAppSpacing`、`LocalAppShapes`、`LocalAppMotion`）；组件级颜色组集中在 `AppComponentTokens.kt`，新组件的颜色字段必须在其中注册（否则触发 `ComponentTokensMissingRegistration` 审计违规）。
- 新组件用脚手架生成：`./scripts/generate-component.sh <Name>`，产出组件本体 + `*Defaults.kt` + `*Logic.kt`，并手动在 `AppComponentTokens.kt` 追加令牌入口。Defaults 文件禁止直读核心令牌或硬编码颜色（审计规则 `DefaultsMissingLocalAppComponentTokens` / `DefaultsHardcodedColor` / `DefaultsImportsLocalAppColors`）。
- **组件准入三问**（四层架构，见 taxonomy §〇.4/§六）：新增 UI 前先问 ①能否用现有 Foundation+Core 组合解决？②它是否承载产品形态语义（设置行/记录卡/仪表卡/聊天）→ 应进 `app/ui/patterns/<域>/` 而非 designsystem？③是否是"某变体的专用组件"→ 收进参数轴或 `*Defaults` 预设，禁止变体即组件。
- **四层 API**：组件只暴露 Design（全默认）/ Customization（variant·size·style·预设工厂·colors 逃生口）两层；Token API（`LocalAppComponentTokens` 等）仅 designsystem 内部可用。组件签名禁止字面量几何参数（`14.dp`/`16.sp`），一律默认到 `*Defaults` 工厂。
- 用户可见文案分区：通用文案走 `designsystem/i18n/AppStrings.kt`（仅 design system 组件引用集合）；产品域文案（AI/记录/消息/提醒/账户…）走 `app/ui/i18n/AppStringsProduct`——禁止新增产品文案进 `AppStrings`，通用组件禁止硬编码业务默认文案。全层禁止硬编码中文/英文字符串。
- 内容状态四态：Loading → Skeleton / `loading` 态；Empty → `EmptyState`；Error → `AppErrorState(status=…)`；Success 不包装。禁止新增私有 `*LoadingState` / `*ErrorState`。

## 5. 数据库与同步

- Room 当前 version 8，15 张 @Entity；schema JSON 导出到 `app/schemas/`（KSP arg 已配置）。
- **改表必须**：版本号 +1 → 写 Migration → 补迁移审计测试（参照 `Room89MigrationAuditTest` 的写法）→ 确认 `app/schemas/` 生成新版本 JSON。
- 表分两类：9 张同步业务表（babies/feedings/sleeps/growths/vaccinations/health_records/diapers/development_assessments/reminders）+ 仅本地表（messages、backup_config、ai_conversations/ai_messages）。仅本地表严禁进入同步引擎。
- 同步链路在 `core/sync/`：`SyncEngine`（sync_version 游标增量）· `SyncTrigger`（手动/自动/退后台/周期）· `RealtimeManager`（Supabase Realtime）· `SyncWorker`（WorkManager，重试退避）。所有同步查询必须带 family 隔离条件（`FamilyIsolationTest` 守门）。

## 6. AI 助手模块

- 多供应商适配在 `core/ai/provider/`（OpenAI Responses 协议 / OpenAI 兼容协议），配置引导走 Edge Function `supabase/functions/ai-bootstrap`，设备密钥存 `AiDeviceKeyStore`。
- 模型输出必须经过安全校验层（`core/ai/settings` + `feature/ai/AiSafetyRules` / `AiAnswerSafety`）才能上屏；会话历史仅存本地表，不同步。
- 会话历史持久化到 ai_conversations/ai_messages（仅本机）。

## 7. 版本 · 提交 · 文档

- 每次构建成功后新提交。
- CHANGELOG 遵循 Keep a Changelog（中文版）+ SemVer。
- 提交信息用中文，格式 `<范围>：<摘要>`（如 `设计系统 G3：剩余 11 文件私有卡收编`、`docs：补记 composites 目录`）。
- 依赖升级前先确认 `gradle/libs.versions.toml` 中标注的"本地已验证组合"，不要随意拉动 Gradle/AGP/Kotlin/KSP 组合。
- 仓库根目录不要提交构建产物与本地配置：`local.properties`、`record_screens.log` 等属本地环境文件。

## 8. 环境备注

- 开发环境曾在 Termux/aarch64 上运行：detekt 全量默认规则在该环境过慢，故 `detekt.yml` 采用显式枚举规则（勿改回 `buildUponDefaultConfig`）；Paparazzi 仅 x86_64 可跑（见第 2 节）。
- Maven 仓库：插件走官方源（aliyun 镜像对 KSP 新版 marker 返回 502，见 `settings.gradle.kts` 注释）；依赖解析优先 aliyun public 镜像。
