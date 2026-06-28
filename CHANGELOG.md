## 更新日志
遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 规范。
版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

### [1.4.0] — 2026-06-25

**记录编辑与删除优化：**
- **编辑功能**：喂养/睡眠/尿布/生长/疫苗/健康 6 种记录全部支持点击编辑，表单预填原有数据，保存调用 `repo.update()`
- **滑动删除**：新增 `SwipeToDeleteContainer` 组件，左滑红色背景 + 删除图标松手即删；`SwipeToEditDeleteContainer` 双方向（左滑删/右滑编）
- **撤销删除**：所有删除操作后弹出 Snackbar「撤销」，点击自动重新插入原记录

### [Unreleased] — 2026-06-27

**性能优化 + 视觉改进 + 记录页闪退修复：**

**性能优化：**
- 7 个列表屏幕 `Column+verticalScroll` → `LazyColumn` + `stickyHeader`（Feeding/Sleep/Diaper/Growth/Vaccination/Timeline/Health）
- `groupBy` / `filter` 加 `remember` 缓存，不再每次重组重算
- TimelineViewModel 加 `distinctUntilChanged`，相同数据不触发全量重建
- StatsViewModel 日统计从 O(N×365) 嵌套循环改为 `groupBy{date}` O(N+365)

**记录页闪退修复：**
- LazyColumn + `weight(1f)` + `contentPadding(bottom=80dp)` 组合导致划到底闪退
- 回退到 `Column+verticalScroll` 稳定版，保留其他功能

**视觉改进：**
- 日期头显示相对时间 + 计数：`今天 · 5次`（`DateUtils.relativeDate()`）
- RecordCard 加 `accentColor` 参数，左侧 3dp 类型色条（drawBehind 绘制，无布局影响）
- TimelineScreen 按记录类型传色：喂养橙/睡眠紫/尿布蓝/生长绿/健康蓝灰
- 首页"查看全部"导航修复：FeedingScreen → TimelineScreen

**删除体验统一：**
- 所有屏幕卡片编辑改为长按触发（`onClick` → `onLongClick`），Vaccination 保持点击
- 左滑删除统一弹出 `AppConfirmDialog` 确认后执行
- 7 个屏幕长按删除全部移除，仅保留左滑删除

**记录页直接编辑：**
- TimelineScreen 点击卡片直接弹出编辑表单（不再跳转功能页）
- TimelineViewModel 新增 `findXxx(id)` / `updateXxx(entity)` 方法

**单元测试：**
- 新增 `DateUtilsTest`（相对日期 4 场景）
- 新增 `TimelineViewModelTest`（数据映射 + find/sort 验证）

**Warning 清零：**
- 移除 DT `@Deprecated` 注解（227 warnings）
- `centerAlignedTopAppBarColors` → `topAppBarColors`（7 处）
- `Icons.Outlined.List/Message` → `Icons.AutoMirrored.Outlined.*`
- `Icons.Filled.DirectionsRun` → `Icons.AutoMirrored.Filled.DirectionsRun`
- `statusBarColor` deprecation 压制
- 4 个 ViewModel 加 `@OptIn(ExperimentalCoroutinesApi::class)`
- BackupManager 移除多余 `?.` 和 `?:` dead code

**Palette 令牌系统完整实施（Round 5）：**
- **核心令牌升级**：`AppColors` 从 16 字段扩展到 39 字段（含 textPrimary/textSecondary/textTertiary/textDisabled、pageBackground/surfaceElevated、bgHover/bgPressed/bgSelected/bgDisabled、divider/overlay/shadow 等全部语义状态色）；`AppSemanticColors` 移除，所有语义色合并入 `AppColors`
- **derive() 升级**：HSL 色相位移覆盖全部 39 个字段，支持 `accent`/`accentLight` 参数；内置 LRU 缓存 12 条
- **组件令牌扩展**：`AppComponentTokens` 从 9 种 → 21 种令牌（新增 Select/SelectionControl/Switch/Table/Dialog/Menu/Tag/Progress/Steps/Pagination/Slider/Rate），全部带 `@Immutable`
- **颜色派生**：`AppComponentTokens.default(colors)` 从 `AppColors` 自动派生 7 个组件的颜色令牌
- **控件尺寸令牌**：`AppControl` → `AppControlTokens` + `ControlSizeTokens`，提供 small/medium/large 三档预设
- **Default 快照同步**：`AppDefaults` 同步更新七类令牌快照（8 级间距 / 6 级阴影 / 7 级透明 / 6 级圆角 / 动效令牌）
- **Logic 解耦**：新增 `ButtonLogic`（isPressed/isLoading/防抖）、`FormLogic`（fields/errors/touched/submitting）、`TableLogic`（sorting/selection/pagination），纯 Kotlin 可 JVM 单测
- **作用域桥接**：`rememberButtonLogic`/`rememberFormLogic`/`rememberTableLogic` 接受 `CoroutineScope?`，为 null 时 `consoleWarn` 提示传入 viewModelScope
- **PaiButton 工厂**：PRIMARY/SECONDARY/TEXT 三变体简化工厂
- **静态审计测试**：`ThemeTokenizationStaticAuditTest` 验证所有 Defaults 文件路由正确性
- **组件脚手架**：`scripts/generate-component.sh` 一键生成 Xxx.kt / XxxDefaults.kt / XxxLogic.kt / XxxLogicTest.kt

**滑动删除体验改进：**
- **滑动确认弹窗**：`SwipeToDeleteContainer` / `SwipeToEditDeleteContainer` 左滑不再直接删除，改为弹出 `AppConfirmDialog` 确认后执行
- **红色背景裁剪**：滑动容器外层加 `Box.clip(RoundedCornerShape(DT.cardRadius.dp))`，解决红色超出卡片边界的问题
- **卡片间距外移**：卡片内部 `padding(vertical=4dp)` 移到 `SwipeToDeleteContainer` 外层 modifier，避免红色填充卡片间隙

**记录页直接编辑：**
- **TimelineScreen 点击编辑**：点击时间线卡片直接弹出对应类型的编辑表单（喂养/睡眠/尿布/生长/健康），不再跳转功能页
- **TimelineViewModel 新增**：`findXxx(id)` / `updateXxx(entity)` 方法供直接编辑使用

**长按删除统一移除：**
- 移除 Feeding/Sleep/Diaper/Growth/Vaccination/Health/Timeline 共 7 个屏幕的长按删除逻辑，仅保留左滑删除（已含确认弹窗）

**组件目录化 + 语义色阶补齐 + Typography 令牌化：**
- **组件目录化**：14 个组件各自独立目录，`AppComponentDefaults.kt` 拆分为 9 个独立 `XxxDefaults.kt` 各归其位
- **新增组件**：`AppSwitch`（主题化 Switch，颜色自动跟随主题）、`AppScaffold`（Scaffold 包装，自动 `containerColor = c.bg`）
- **深色令牌**：`AppComponentTokens.dark()` 自动暗色变体，`BabyTrackerTheme` 根据 `theme.name == "night"` 自动选择
- **DT 弃用**：`DesignTokens.DT` 加 `@Deprecated`，组件级 8 处引用已迁移至 XxxDefaults/inline dp
- **语义色阶**：`ThemeColors` 新增 `borderHover` `borderFocus` `textDisabled` `bgHover` `bgPressed`，6 主题全部补全
- **Typography 令牌**：新增 `LocalAppTypography` CompositionLocal，4 个组件从 `MaterialTheme.typography` 迁移至令牌读取

**设计系统骨架（参照 Palette 令牌驱动架构）：**

**Round 1 — 核心令牌 + 组件层：**
- **核心令牌层**：新建 `AppTokens.kt`（Spacing 6 级 / Elevation 4 级 / Opacity 5 级 / Motion 3 级 / Shapes 6 级）+ 5 个 CompositionLocal
- **组件令牌层**：新建 `AppComponentTokens.kt`（9 组：Card / AppBar / Button / Input / Chip / Fab / BottomBar / ListItem / Skeleton）
- **组件 Defaults 层**：新建 `AppComponentDefaults.kt`（9 个对象，对标 Palette 的 XxxDefaults 模式）
- **Theme.kt 升级**：`BabyTrackerTheme` 注入全部 7 个 CompositionLocal
- **新增组件**：`AppCard` / `AppTopBar` / `PrimaryButton`+`SecondaryButton`+`AppTextButton` / `AppChip` / `SectionHeader`+`AppListItem` / `AppInput` / `AppFAB`
- **文件整理**：`AnimatedListItem`/`animateNumber` → `Animations.kt`；`SwipeToDeleteContainer` 系列 → `SwipeContainers.kt`；`HapticExtensions.kt` 精简

**Round 2 — 对标 Palette 完整结构补全：**
- **i18n**：新建 `core/i18n/AppStrings.kt`（60+ 中文文案集中管理，对标 PaiStrings）
- **hooks**：新建 `core/hooks/Hooks.kt`（`useDebounce` / `useState` / `useLatestState`，对标 Palette hooks）
- **快照**：新建 `core/util/AppDefaults.kt`（非 Composable Token 快照，对标 PaiDefaults）
- **foundation**：新建 `foundation/border/BorderContainer.kt` + `foundation/layout/CenterVerticallyRow.kt`
- **新组件**：`AppConfirmDialog`（确认删除）/ `AppSnackbar`（撤销 Snackbar）/ `AppIconButton`（主题化图标）/ `AppBottomSheet`（表单弹层）
- **导出索引**：新建 `ui/components/AppComponents.kt`（对标 Palette Pai.kt 桶文件）

**Round 4 — 设计系统统一迁移至 `designsystem/`：**
- `core/components/` + `core/theme/` + `core/hooks/` + `core/i18n/` + `core/foundation/` + `core/util/AppDefaults` → `designsystem/`
- 包路径统一为 `com.babytracker.designsystem.*`

**最终目录结构：**
```
core/theme/     — 主题入口（7 文件）
core/tokens →   AppTokens / AppComponentTokens / AppComponentDefaults / DesignTokens
core/hooks/     — 轻量 Hooks（useDebounce/useState/useLatestState）
core/i18n/      — 国际化文案（AppStrings）
core/util/      — 工具 + AppDefaults 快照
foundation/     — 基础组件（BorderContainer/CenterVerticallyRow）
ui/components/  — 19 个 Composable 文件 + 导出索引
```

**优先级模型（对标 Palette 五级）：**
```
显式参数 > Defaults 参数 > 组件令牌 > 语义令牌 > 硬编码回退
```
- **TimelineViewModel**：新增 `undoLastDelete()` 方法暂存最近删除实体用于撤销

### [1.3.0] — 2026-06-18

**依赖升级：**
- **SDK 36**：compileSdk/targetSdk 35→36，配合新版 AndroidX 库
- **AGP 9.2.1**：构建工具链更新
- **Kotlin 2.3.21 + KSP 2.3.9**：语言及注解处理升级
- **Compose BOM 2026.05.01**：Compose 1.11.x 系列
- **Koin 4.2.1**：ViewModel DSL 从 `koin-androidx-viewmodel` 迁移到 `koin-core-module-dsl`
- **Retrofit 3.0.0 / OkHttp 5.4.0**：网络栈升级
- **Room 2.8.4 / Lifecycle 2.10.0 / Core KTX 1.18.0**：AndroidX 全线升级

**基础设施：**
- **版本目录**：从直接声明依赖迁移到 `gradle/libs.versions.toml` 统一管理
- **Termux AAPT2 修复**：替换为 ReVanced 预编译 aapt2-arm64-v8a（build-tools 35.0.2），支持 SDK 35+ 编译
- **添加 AAPT2 修复文档**：`docs/aapt2-termux-fix.md`

### [1.2.0] — 2026-06-18

**架构改进：**
- **ViewModel 生命周期修复**：`HomeViewModel` / `StatsViewModel` 从 Koin `single` 改为 `viewModel` 注册，UI 用 `koinViewModel()` 获取，Activity 重建后正确重置状态
- **BabyController 自动 fallback**：监听 `BabyRepository.watchAll()`，删除当前宝宝后自动切换到第一个宝宝；当前 id 为 0 且列表非空时自动选中第一个
- **HomeViewModel Flow 修复**：改用 `_trigger + flatMapLatest`，避免每次切换宝宝启动新 Flow 不取消旧 Flow
- **HomeViewModel 移除未用依赖**：构造参数从 4 个减为 3 个（移除 growthRepo）
- **Domain Model 层占位**：新增 `domain/model/Models.kt` 定义 8 个纯 Kotlin 数据类 + 枚举（FeedingType/SleepType/GrowthType/VaccinationStatus/DiaperType/BreastSide）
- **Mapper 占位**：新增 `data/database/mapper/Mappers.kt` 提供 Entity ↔ Domain Model 双向映射（待后续 Repository 接口切换时启用）

**Bug 修复：**
- **SleepListScreen 今日睡眠过滤**：原来取"最近一条 night 记录"，会显示历史数据为今日睡眠。改为 `it.startTime.startsWith(today)` 过滤
- **SleepListScreen 进度条双向 clamp**：`coerceAtMost(1f)` 改为 `coerceIn(0f, 1f)`，避免负数时长导致进度反向
- **SleepListScreen 进度条渐变**：硬编码颜色改为 `Gradients.progress(c)` 跟随主题
- **HealthScreen › 引导符**：移除有引导符但 onClick 为空的反模式
- **BackupManager 不可达代码**：删除 `restoreFromUri` 末尾永远不会执行的 `Result.failure(Exception("未知错误"))`
- **健康/疫苗表单 DatePicker**：手敲 yyyy-MM-dd 改为 DatePickerDialog，加 leadingIcon + 校验

**发布配置：**
- **release minify 开启**：`isMinifyEnabled = true` + `isShrinkResources = true`
- **proguard-rules.pro**：新增完整 keep 规则（Room/Koin/Retrofit/OkHttp/Gson/Coroutines/Compose/Coil）
- **AndroidManifest 备份规则**：`allowBackup=false` + `dataExtractionRules` + `fullBackupContent` 排除数据库与 SP（避免与 App 自身备份系统冲突）
- **SplashScreen**：引入 `androidx.core:core-splashscreen`，新增 `Theme.BabyTracker.Splash` 主题，MainActivity `installSplashScreen()`

**性能与健壮性：**
- **HomeScreen FeatureGrid**：`items.indexOf()` O(n²) 查找改为 `forEachIndexed` 直接获取 index
- **DateUtils 新增 safeParse**：`safeParseDateTime(s)` / `safeParseDate(s)` 返回 null 而非抛异常，UI 层避免散落 try-catch

**功能补全：**
- **疫苗页"生成接种计划"**：空状态加 action 按钮，弹确认框后调用 `VaccineSchedule.createForBaby` 一键生成 21 条默认计划

**文档清理：**
- 删除严重过时的 `UPGRADE_ROADMAP.md`（声称缺表单 BottomSheet，实际已完成）

### [1.1.0] — 2026-06-18

**Bug 修复：**
- 修复 `FeedingListScreen` 中嵌套定义的 `NumberPicker` 死代码导致的编译风险
- 修复 `SettingsScreen` WebDAV 恢复确认弹窗被错误嵌套进本地恢复弹窗 if 块，导致完全不显示的问题
- 简化 `Theme.kt` 的 `isDark`/`darkTheme` 不一致 — 移除无用的 `dynamicColor` 分支与 `isSystemInDarkTheme` 调用
- 移除 `gradle.properties` 中的 Termux 专有 `aapt2FromMavenOverride` 路径
- 清理 6 个 UI 文件未使用的 `BabyRepository` import
- 删除 `BackupManager` 中未引用的 `ResultHolder` 数据类

**视觉与交互升级：**
- **Shapes 升级**：列表 Card 16dp / 容器 20dp / 弹窗 28dp
- **卡片质感**：移除全部 `BorderStroke(1.dp, outlineVariant)`，改用 `cardElevation = 1.dp` 微阴影
- **Gradients 工具**：新增 `core/theme/Gradients.kt`
- **首页头部**：背景从纯色升级为渐变；宝宝头像圆框用主色渐变
- **AppBar 主题色化**：所有页面统一 `primaryContainer` 背景
- **数字动画**：首页今日概览 `animateIntAsState` 平滑过渡
- **EmptyState 组件**：7 处列表空状态应用
- **触觉反馈**：6 处长按删除点统一加 `HapticFeedbackType.LongPress` 震动
- **Growth 图表精致化**：动态 Y 轴刻度 + 渐变填充 + 圆角端点 + 双层圆点
- **表单体验**：FeedingFormDialog 加 leadingIcon + isError 范围校验

### [1.0.0] — 2026-06-02

**初始实现：** 基于 MyApp 技术栈的宝宝记录 App。

- 首页 / 喂养 / 睡眠 / 生长 / 疫苗 / 健康 / 统计 / 设置 8 个页面
- 架构：MVVM + Koin + Room + Navigation Compose + Material 3
- 技术栈：Kotlin 1.9.24, Java 17, Gradle 9.5.1, AGP 8.9.3, SDK 34
