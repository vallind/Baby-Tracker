# MyApp — 宝宝记录 (Android)

Android 原生宝宝护理记录 App。Jetpack Compose + Material 3（完整 M3 主题系统），MVVM + Koin + Room。

---

## AI 编辑硬规则（AI 助手必须遵守）

### 禁止行为

1. 不允许新增 `implementation()` 依赖，必须先在对话中确认
2. 不允许修改 `app/build.gradle.kts` 的版本号
3. 不允许重命名 Entity 字段 / Repository 接口方法 / ViewModel 构造参数
4. 不允许在 `@Composable` 函数体内定义另一个 `@Composable` 函数
5. 不允许嵌套 `AlertDialog` 的 if 块（必须平级）
6. 不允许引入 `isSystemInDarkTheme()`（当前未实现系统暗色跟随）
7. 不允许硬编码路径（如 `/data/data/com.termux/...`）
8. 不允许用 `single { }` 注册 ViewModel（必须用 `viewModel { }`）
9. 不允许在 `if (xxx == 0) return` 早退模式后还依赖 remember 状态
10. 不允许把 `coerceAtMost(1f)` 用于可能为负的百分比（必须 `coerceIn(0f, 1f)`）

### 强制行为

1. 改完任何 .kt 文件必须验证 brace balance = 0
2. 改完必须验证所有用到的类型都有对应 import
3. 修改 Entity 字段必须同步：DAO / Repository / Mapper / BackupManager
4. 单次对话改动文件数 ≤ 5，超过必须拆分
5. 修改 `core/theme/` 必须人工 review
6. 修改 `core/database/Entities.kt` 必须人工 review + 写 Migration
7. 新增 ViewModel 必须用 `viewModel { }` 注册 + `koinViewModel()` 获取
8. "按 ID 加载数据"的 ViewModel 必须用 `_trigger + flatMapLatest` 模式
9. "今日"过滤必须用 `it.xxx.startsWith(today)`，不能用 `firstOrNull()`
10. 所有 Repository 接口方法返回 `Flow<List<XxxEntity>>`，UI 层直接用 Entity（Domain Model 占位中）

### 历史踩坑（请勿再犯）

| # | 坑 | 教训 |
|---|---|---|
| 1 | 嵌套 `@Composable NumberPicker` 定义 | Composable 函数必须定义在文件顶层 |
| 2 | WebDAV 恢复 AlertDialog if 嵌套进本地恢复弹窗 | 多个 AlertDialog 必须平级 if |
| 3 | `Theme.kt` isDark/darkTheme 判断源不一致 | darkTheme 唯一来源 `theme.name == "night"` |
| 4 | Termux 硬编码路径打进仓库 | 所有路径必须用环境变量或相对路径 |
| 5 | ViewModel 用 `single` 注册导致永生 | 必须用 `viewModel { }` |
| 6 | BabyController 不监听列表导致删除后假死 | BabyController 必须监听 `watchAll()` 自动 fallback |
| 7 | HomeViewModel Flow 不取消旧 Flow | 必须用 `_trigger + flatMapLatest` |
| 8 | SleepListScreen 取最近 night 而非今日 night | "今日"过滤必须 `startsWith(today)` |
| 9 | 进度条 `coerceAtMost(1f)` 不 clamp 负数 | 百分比 clamp 用 `coerceIn(0f, 1f)` |
| 10 | 未使用 import 累积 | 改完必须 grep 验证 import 都被使用 |
| 11 | `dynamicColor` 参数死代码 | `MainActivity` 永远传 `dynamicColor = false` |
| 12 | `ResultHolder` 未使用数据类 | 不要为未来可能用到而留空类 |

### 文件分级

| 级别 | 文件 | 改动要求 |
|---|---|---|
| 🟢 自由修改 | `ui/*/Screen.kt` / `ui/*/FormDialog` / `ui/components/*` | AI 可直接改 |
| 🟡 谨慎修改 | `ui/*/ViewModel.kt` / `data/repository/*` / `core/util/*` | 需同步多文件，AI 改完必须 review |
| 🔴 禁区 | `core/database/Entities.kt` / `core/theme/Theme.kt` / `core/di/Modules.kt` / `app/build.gradle.kts` / `AndroidManifest.xml` | 必须人工确认 |

### 验证检查清单（改完自检）

```bash
# 1. Brace balance
for f in $(find app/src/main/java -name "*.kt"); do
  depth=$(awk 'BEGIN{d=0} { for(i=1;i<=length($0);i++){c=substr($0,i,1); if(c=="{")d++; else if(c=="}")d--} } END{print d}' "$f")
  [ "$depth" != "0" ] && echo "❌ $f: $depth"
done

# 2. Import 完整性
for f in $(find app/src/main/java -name "*.kt"); do
  # 检查用了 X 但没 import X（排除同包内引用）
  grep -E "[^.a-zA-Z](EmptyState|Gradients|longPressDeletable|rememberHaptic|VaccineSchedule)\(" "$f" | while read line; do
    name=$(echo "$line" | grep -oE "(EmptyState|Gradients|longPressDeletable|rememberHaptic|VaccineSchedule)" | head -1)
    grep -q "^import.*$name$" "$f" || echo "❌ $f uses $name but no import"
  done
done

# 3. 编译验证（如有 kotlinc）
./gradlew assembleDebug
```

---

## 技术栈（约束）

| 层面 | 选型 | 说明 |
|---|---|---|
| UI | Jetpack Compose + Material 3 | full ColorScheme / Typography / Shapes |
| 启动屏 | androidx.core:core-splashscreen 1.0.1 | MainActivity `installSplashScreen()` |
| 导航 | Navigation Compose | 11 条路由 |
| 数据库 | Room 2.6.1 + KSP | 8 张表，Migration 增量升级 |
| 异步 | Kotlin Coroutines + Flow | — |
| DI | Koin 3.5.6 | ViewModel 用 `viewModel { }` 注册 |
| 架构 | MVVM（ViewModel + StateFlow） | 2 个 ViewModel |
| 网络 | Retrofit 2.9.0 + OkHttp 4.12.0 | WebDAV 备份 |
| 文件 | DocumentFile 1.0.1 | SAF 目录选择 |
| 图片 | Coil 2.6.0 | — |
| 构建 | Gradle 9.5.1 + AGP 8.9.3 | R8 minify + 资源压缩 |
| 编译 | Kotlin 1.9.24, Java 17, SDK 34, minSdk 24 | — |
| 发布 | `isMinifyEnabled = true` + `isShrinkResources = true` | proguard-rules.pro 已配 |

## Build, Test, and Development Commands

```bash
./gradlew assembleDebug              # 构建 Debug APK
./gradlew assembleRelease            # 构建 Release APK（启用 R8 + 资源压缩）
./gradlew test                       # 运行 JVM 单元测试
./gradlew connectedDebugAndroidTest  # 运行仪器测试（需模拟器/真机）
./gradlew lint                       # 运行 Android Lint
```

## 构建前置条件

- JDK 17（`java -version` 验证）
- Android SDK 34（`local.properties` 配置 `sdk.dir`，**不要硬编码 Termux 路径**）
- 无需 Firebase / google-services.json
- 无需 Node.js / pnpm（纯原生项目）
- 推荐用 Android Studio Hedgehog 或更高

首次构建：

```bash
cp local.properties.example local.properties  # 修改 sdk.dir 为本机路径
./gradlew assembleDebug
```

环境变量（可选，仅 Termux 环境需要）：

```bash
# 普通开发机不需要设置；Termux 环境才需要
# ANDROID_AAPT2_OVERRIDE=/path/to/android-sdk/build-tools/34.0.0/aapt2
```

## Coding Style & Naming Conventions

本仓库使用 `.editorconfig` 统一格式：

- Kotlin/Gradle 脚本：4 空格缩进，最大行长 120
- XML/JSON：2 空格缩进
- Markdown/YAML：2 空格缩进，允许尾随空格（用于对齐）

命名习惯：

- Kotlin 类遵循 PascalCase（如 `FeedingEntity` / `HomeViewModel`）
- 测试类以 `*Test` 结尾
- 包名小写（`com.babytracker.ui.feeding`）
- 主题键值小写（`"pure"` / `"aurora"` / `"night"`）
- 表名复数（`feedings` / `sleeps`），实体名单数（`FeedingEntity`）

## Testing Guidelines

测试框架：JUnit 4 + AndroidX Test + Coroutines Test。

- 单元测试命名：`FooTest.kt`，放 `app/src/test/java/com/babytracker/`
- 仪器测试命名：`FooInstrumentedTest.kt`，放 `app/src/androidTest/java/com/babytracker/`
- ViewModel 测试用 `@OptIn(ExperimentalCoroutinesApi::class)` + `runTest { }`
- Repository 测试用 in-memory Room：`Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()`
- 当前覆盖率：0%，新业务逻辑应配套新增测试
- 纯 UI Composable 暂不强制测试，优先测 ViewModel 与 Repository

测试文件命名：

- 单元测试：`FooTest.kt`
- 仪器测试：`FooInstrumentedTest.kt` 或 `*Test.kt`

## 项目结构

```
app/src/main/java/com/babytracker/
├── BabyTrackerApp.kt           # Application + Koin 启动
├── MainActivity.kt             # Compose Activity（dynamicColor=false, installSplashScreen）
├── core/
│   ├── theme/
│   │   ├── DesignTokens.kt     # DT 布局令牌 + 6 个 AppTheme + ThemeColors
│   │   ├── Theme.kt            # MaterialTheme 封装 + ColorScheme/Typography/Shapes
│   │   ├── ThemeController.kt  # SharedPreferences 主题持久化
│   │   └── Gradients.kt        # 渐变 Brush 工具（primary/primarySoft/progress/chartArea）
│   ├── database/
│   │   ├── Entities.kt         # 8 个实体
│   │   ├── dao/Daos.kt         # 8 个 DAO
│   │   └── AppDatabase.kt      # Room DB（Migration 1→2）
│   ├── di/Modules.kt           # Koin appModule + databaseModule（viewModel { } 注册）
│   ├── backup/BackupManager.kt # 本地备份 + SAF 目录备份 + JSON 导入数据
│   └── util/
│       ├── DateUtils.kt        # 日期格式化 + 时长 + safeParse（避免散落 try-catch）
│       ├── BabyController.kt   # 当前宝宝上下文（监听 watchAll 自动 fallback）
│       └── VaccineSchedule.kt  # 21 条默认疫苗接种计划生成器
├── data/
│   ├── repository/Repositories.kt  # 7 个 Repository 接口 + 实现
│   └── database/mapper/Mappers.kt  # Entity ↔ Domain Model 双向映射（占位，待启用）
├── domain/
│   └── model/Models.kt         # Domain Model + 枚举（占位，待启用）
└── ui/
    ├── navigation/AppNavigation.kt   # 路由定义（sealed class Screen）
    ├── home/                   # HomeScreen + HomeViewModel（_trigger + flatMapLatest）
    ├── feeding/                # 喂养时间轴 + 记录弹窗 + 长按删除（haptic）
    ├── sleep/                  # 睡眠列表 + 深色头卡 + 进度条 + 记录弹窗 + 长按删除
    ├── diaper/                 # 尿布列表 + 记录弹窗 + 长按删除
    ├── growth/                 # 生长 Tab + Canvas 曲线图（动态刻度+渐变填充+WHO 参考线）+ 记录弹窗
    ├── vaccination/            # 疫苗列表 + 状态标签 + 一键生成计划 + 记录弹窗 + 长按删除
    ├── health/                 # 健康列表 + 分类 + DatePicker + 记录弹窗 + 长按删除
    ├── stats/                  # 统计卡片 + MiniLineChart
    ├── settings/               # 我的 + 宝宝管理 + 备份 + 主题切换
    └── components/             # EmptyState + HapticExtensions + BabyIllustration
```

## Concepts

### Baby

宝宝实体，所有记录的 owner。当前选中的宝宝由 `BabyController` 管理。

- 自动监听 `BabyRepository.watchAll()`：删除当前宝宝后 fallback 到第一个；列表为空时重置 id 为 0
- 调用方仍可主动通过 `selectBaby(id)` 切换
- 6 个列表页通过 `babyCtrl.currentBabyId` 获取当前 id

文件：
- `app/src/main/java/com/babytracker/core/database/entity/Entities.kt` → `BabyEntity`
- `app/src/main/java/com/babytracker/core/util/BabyController.kt`

### Feeding

喂养记录，4 种类型，每种有不同字段集：

| type | 必填字段 | 可选字段 |
|---|---|---|
| `breast` | `durationMin` | `breastSide`（左侧/右侧/双侧） |
| `formula` | `amountMl` | `brand` |
| `food` | `foodName` | `amountG` |
| `water` | `amountMl` | — |

表单 UI 根据 type 动态显示字段，每个字段有 `leadingIcon` + `isError` 范围校验。

文件：
- `app/src/main/java/com/babytracker/core/database/entity/Entities.kt` → `FeedingEntity`
- `app/src/main/java/com/babytracker/ui/feeding/FeedingListScreen.kt` → `FeedingFormDialog`

### Sleep

睡眠记录，2 种类型：

- `night`：夜间睡眠，参与首页"今日睡眠"统计（只统计 `type=night && startTime.startsWith(today)`）
- `nap`：小睡，不参与顶部统计

时长用 `DateUtils.durationToTotalSeconds(start, end)` 计算，进度条用 `Gradients.progress(c)` 渐变 + `coerceIn(0f, 1f)` 双向 clamp。

文件：
- `app/src/main/java/com/babytracker/core/database/entity/Entities.kt` → `SleepEntity`
- `app/src/main/java/com/babytracker/ui/sleep/SleepListScreen.kt`

### Growth

生长记录，3 种类型（`height` / `weight` / `head`），Canvas 折线图可视化。

图表特征：
- 动态 Y 轴刻度（基于 `minOf/maxOf`，非写死）
- 渐变填充区域（`Gradients.chartArea`）
- 圆角端点（`StrokeCap.Round`）
- 双层圆数据点
- WHO 85th/50th/15th 百分位虚线参考
- 进入动画（`animateFloatAsState` 800ms）

文件：
- `app/src/main/java/com/babytracker/core/database/entity/Entities.kt` → `GrowthEntity`
- `app/src/main/java/com/babytracker/ui/growth/GrowthScreen.kt`
- WHO 参考值函数：`GrowthScreen.kt` 文件末尾 `whoReferenceLines()`

### VaccineSchedule

默认接种计划生成器，21 条预设疫苗按月龄排期（0-72 月）。

调用时机：
1. 新建宝宝时自动调用（`BabyManagementScreen` 的 `onSave`）
2. 疫苗页空状态点"生成接种计划"按钮手动调用

调用方式：
```kotlin
VaccineSchedule.createForBaby(babyId, baby.birthDate).forEach { vacRepo.insert(it) }
```

文件：`app/src/main/java/com/babytracker/core/util/VaccineSchedule.kt`

### ThemeColors

主题色集合，通过 `LocalThemeColors` CompositionLocal 注入。

所有 UI 文件用 `val c = LocalThemeColors.current` 获取，不用 `DT` 静态色值（`DT` 仅放几何常量）。

6 套主题：`pure` / `aurora` / `warm` / `sunny` / `night` / `morandi`，每套定义完整 `ThemeColors`。

`Theme.kt` 在 `BabyTrackerTheme` 中合并 `AppTheme.colors` 与 `colorScheme`，最终通过 `LocalThemeColors` 暴露给 UI。

文件：
- `app/src/main/java/com/babytracker/core/theme/DesignTokens.kt` → `ThemeColors` / `AppTheme`
- `app/src/main/java/com/babytracker/core/theme/Theme.kt` → `LocalThemeColors` / `BabyTrackerTheme`
- `app/src/main/java/com/babytracker/core/theme/ThemeController.kt` → 主题持久化

### BackupManager

备份管理器，3 种模式：

1. **本地 SAF 目录备份**：`createLocalBackupToUri(context, uri)` — 用户选目录，生成 zip
2. **WebDAV 云备份**：`createWebDAVBackup()` — PUT 到 WebDAV 服务器
3. **zip 导入恢复**：`restoreFromUri(context, uri)` / `restoreFromWebDAV()` — 解压并写入数据库

导出格式：zip 包内 `data.json`，包含所有 7 张业务表数据。

恢复逻辑：以新 id 重新插入所有宝宝和关联记录（避免 id 冲突）。

文件：`app/src/main/java/com/babytracker/core/backup/BackupManager.kt`

### Domain Model（占位）

`domain/model/Models.kt` 定义了 8 个纯 Kotlin 数据类 + 6 个枚举（FeedingType/SleepType/GrowthType/VaccinationStatus/DiaperType/BreastSide）。

`data/database/mapper/Mappers.kt` 提供双向映射函数（`toDomain()` / `toEntity()`）。

**当前未启用** — Repository 仍返回 Entity，UI 仍直接用 Entity。后续重构时切换：

1. Repository 接口返回 `Flow<List<DomainModel>>`
2. RepositoryImpl 内部用 Mapper 转换
3. UI 层 import 从 `entity.*` 改为 `domain.model.*`

文件：
- `app/src/main/java/com/babytracker/domain/model/Models.kt`
- `app/src/main/java/com/babytracker/data/database/mapper/Mappers.kt`

## 设计规范

### Material 3（完整主题系统）

| 要素 | 说明 |
|---|---|
| ColorScheme | 20+ 色槽，`AppTheme.toColorScheme()` 映射 |
| Typography | 13 级（displayLarge → labelSmall） |
| Shapes | 5 级：small=16dp / medium=20dp / large=28dp / extraLarge=32dp |
| Dynamic Colors | `dynamicColor=false`，使用 6 个自定义主题 |
| AppBar | 所有页面统一 `primaryContainer` 背景 + `onPrimaryContainer` 文字 |

### 主题

| 名称 | 主色 | 键值 |
|---|---|---|
| 纯净蓝 pure | #2563EB | `"pure"` |
| 极光紫 aurora | #7C6CF0 | `"aurora"` |
| 暖阳粉 warm | #FF8A80 | `"warm"` |
| 阳光黄 sunny | #F59E0B | `"sunny"` |
| 暗夜深 night | #5C6BC0 | `"night"` |
| 莫兰迪 morandi | #B0BEC5 | `"morandi"` |

### 布局令牌（DT）

```
页面边距: 20dp, 卡片间距: 16dp, 卡片圆角: 8dp
按钮圆角: 8dp, 输入框圆角: 8dp, 图标尺寸: 22dp, 图标背景: 40dp
```

### 渐变（Gradients）

```
Gradients.primary(c)        — 主色横向渐变（CTA / FAB）
Gradients.primarySoft(c)    — 主色到背景的纵向渐变（首页头部）
Gradients.primaryVertical(c) — 主色到紫色的纵向渐变
Gradients.progress(c)       — 进度条渐变
Gradients.chartArea(c)      — 图表下方填充渐变
Gradients.sleepHeader()     — 睡眠页深色头卡固定渐变
```

### 配色映射

```
c.primary      → MaterialTheme.colorScheme.primary
c.primaryLight → MaterialTheme.colorScheme.primaryContainer
c.bg           → MaterialTheme.colorScheme.background
c.card         → MaterialTheme.colorScheme.surface
c.cardBorder   → MaterialTheme.colorScheme.outlineVariant
c.textPrimary  → MaterialTheme.colorScheme.onSurface
c.textSecondary→ MaterialTheme.colorScheme.onSurfaceVariant
c.pink/yellow/cyan/green → 无 M3 等价色，保留自定义
```

## 数据库

| 表 | 实体 | DAO |
|---|---|---|
| babies | BabyEntity | BabyDao |
| feedings | FeedingEntity | FeedingDao |
| sleeps | SleepEntity | SleepDao |
| growths | GrowthEntity | GrowthDao |
| vaccinations | VaccinationEntity | VaccinationDao |
| health_records | HealthRecordEntity | HealthRecordDao |
| diapers | DiaperEntity | DiaperDao |
| backup_config | BackupConfigEntity | BackupConfigDao |

Migration：`fallbackToDestructiveMigration()` → 正式 `Migration(1, 2)` 增量升级（新增 diapers 表）。

## 页面列表

| 路由 | 页面 | 说明 |
|---|---|---|
| `/` | 首页 | 底部 Tab：首页/统计/我的 |
| `/feeding` | 喂养 | 时间轴 + 记录弹窗 + 长按删除 |
| `/sleep` | 睡眠 | 深色头卡 + 列表 + 记录弹窗 + 长按删除 |
| `/diaper` | 尿布 | 列表 + 记录弹窗 + 长按删除 |
| `/growth` | 生长 | Tab + Canvas 图表 + 记录弹窗 + 长按删除 |
| `/vaccination` | 疫苗 | 状态标签 + 一键生成计划 + 记录弹窗 + 长按删除 |
| `/health` | 健康 | 分类列表 + DatePicker + 记录弹窗 + 长按删除 |
| `/stats` | 统计 | 2×3 卡片 + MiniLineChart |
| `/settings` | 我的 | 设置列表 + 主题切换 + 状态栏适配 |
| `/settings/babies` | 宝宝信息 | 编辑/删除 + 表单弹窗 |
| `/settings/backup` | 备份 | 目录选择 + 本地备份 + WebDAV |

### 交互规范

- 所有列表页**长按卡片** → 触觉反馈 + 确认删除
- 所有弹窗 `skipPartiallyExpanded = true` 全展开
- 时间输入统一 `yyyy-MM-dd HH:mm` 格式（DatePicker + TimePicker 组合）
- 列表**跨日**显示日期分隔标签；**同日**隐藏
- 空列表统一显示 `EmptyState` 组件（emoji + 标题 + 副标题 + 可选 action）
- 表单字段统一 `leadingIcon` + `isError` + `supportingText` 范围校验

## 构建

详见上方 "Build, Test, and Development Commands" 与 "构建前置条件" 章节。

## 文档规范

- 所有注释必须使用中文，禁止英文注释
- 文档统一使用中文
- commit message 使用中文
- `CHANGELOG.md` 每次发布版本必须更新
- `AGENTS.md` 是 AI 协作的真相之源，与代码冲突时以 AGENTS.md 为准
