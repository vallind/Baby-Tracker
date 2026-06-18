# 宝宝记录 App — Android 实现

## 更新日志

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
