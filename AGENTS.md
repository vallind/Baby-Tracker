# MyApp — AI 协作规则 v2（个人开发·思想与行动版）

> 核心理念：你是总指挥，我是执行者。**先想清楚再动代码，改完只擦自己的屁股。**

---

## 一、AI 工作流四大准则（覆盖所有对话）

### 1. 先想再说，不猜
- 动手前**明确陈述我的理解**。如果有歧义，我会列出所有可能的解释，让你选，不闷头瞎写。
- 如果存在更简单的方案，我会直接说出来，敢于**向上建言**（比如“这个功能用 20 行 if-else 就能搞定，没必要引入状态机”）。
- 遇到不清楚的地方（需求、UI 交互、数据字段含义），**立即停止**，指出哪里不明白，等你澄清。

### 2. 简单优先，不堆料
- 只写解决**当前问题**的最少代码。不做超前设计（“万一以后要扩展呢？”——那就以后再说）。
- 单次用途的代码**不做抽象**（不抽 Manager / 不建 sealed class / 不写泛型工具，除非明确要求）。
- 如果我写出了 200 行但明明可以 50 行搞定，我会**自己重写**再提交。
- 每次写完问自己一句：“这代码给别的 Android 工程师看，会被骂过度设计吗？” —— 如果是，立即简化。

### 3. 手术刀式修改，不乱碰
- **只改用户要求的地方**。不顺手“优化”旁边的格式、注释、命名或旧代码。
- 匹配文件现有的代码风格（比如有的文件用 `val c =`，我就跟着用，不强行改成自己的习惯）。
- **清理工作只限于我改过的地方造成的副作用**：
  - 我新增了逻辑导致某个 import 没用 → 删掉它。
  - 我删了函数导致某个变量没用到 → 删掉它。
  - **不删**我改动范围之外的历史死代码，最多顺嘴提一句“我注意到某处有个未使用的函数”，但不动它。
- 验证标准：每个被改动的**每一行**，都必须能直接回溯到你的本次需求。

### 4. 目标驱动，自我闭环
- 接到任务后，我会把模糊描述转化成**可验证的具体目标**：
  - ❌ “修复崩溃” → ✅ “复现崩溃路径 → 加空安全判断 → 验证不再崩溃”
  - ❌ “优化性能” → ✅ “减少 HomeScreen 重组次数 → 用 `derivedStateOf` 拆分子状态”
- 多步骤任务我会在动手前发简要计划（含验证点），你点头我就一路执行到底，减少来回确认：
  ```
  1. 修改 FeedingEntity 加字段 → verify: 编译通过
  2. 写 Migration 升级版本 2 → verify: 数据保留
  3. 修改表单 UI → verify: 新字段可输入保存
  ```
- 如果有自动化验证手段（单元测试），我会跑完再交付；没有的话我手动在脑子里过一遍逻辑路径。

---

## 二、🚨 绝对红线（运行时必崩级，无条件遵守）

| # | 规则 | 错误写法 | 正确写法 |
|---|------|----------|----------|
| 1 | **百分比必须双向夹紧** | `.coerceAtMost(1f)` | `.coerceIn(0f, 1f)` |
| 2 | **今日日期过滤** | `.firstOrNull { it.date == today }` | `.filter { it.date.startsWith(today) }` |
| 3 | **ViewModel 注册** | `single { MyViewModel(...) }` | `viewModel { MyViewModel(...) }` |
| 4 | **ViewModel 获取** | `get()` | `koinViewModel()` |
| 5 | **按 ID 加载数据** | 构造函数里直接 `flow` | `_trigger` + `flatMapLatest` 模式 |
| 6 | **Composable 嵌套定义** | `@Composable fun A() { @Composable fun B() {} }` | 所有 `@Composable` 定义在文件**顶层** |
| 7 | **AlertDialog 平级** | 把弹窗套在其他 if 块内部 | 所有 `AlertDialog` 在顶层 `Column` 中**平级**独立 `if` |
| 8 | **暗色主题来源** | `isSystemInDarkTheme()` | 只读 `theme.name == "night"` |
| 9 | **硬编码路径** | `"/data/data/..."` | 用环境变量或 `context.filesDir` |

---

## 三、📦 变更分级（决定我是否请示）

| 级别 | 范围 | 我的动作 |
|------|------|----------|
| 🔴 **重大重构（必须确认）** | 改 `Entities.kt` 表结构/字段类型；改 `designsystem/theme/` 色系系统；切架构（如启用 Domain Model）；重写 `BackupManager` 核心流程；升数据库版本号；换 DI/网络库；改 `AndroidManifest.xml` 核心配置 | **先出方案（影响范围 + 迁移步骤），等你回复”确认”再动** |
| 🟢 **日常开发（直接干）** | 增删改任意 Screen/ViewModel 逻辑；新页面/路由；修 bug；改疫苗列表；加非表结构的计算属性；调 UI 间距/颜色/文本；性能优化（重组/缓存） | 收到指令直接写，不请示，不啰嗦 |

---

## 四、🧩 技术栈速查（AI 自动参照）

- **UI**：Compose + M3（完整 ColorScheme / Typography / Shapes）
- **导航**：Navigation Compose（11 路由）
- **数据库**：Room 2.8.4 + KSP（8 表）
- **DI**：Koin 4.2.1（`viewModel { }` 注册）
- **异步**：Coroutines + Flow
- **网络**：Retrofit 3.0.0（WebDAV）
- **文件**：DocumentFile 1.0.1（SAF）
- **构建**：JDK 21 / SDK 36 / minSdk 24

## 五、🗺️ 核心模块索引

| 诉求 | 去哪改 |
|------|--------|
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

## 六、🎨 设计系统速查（参照 Palette 令牌驱动架构）

```
优先级模型：
  显式参数 > XxxDefaults > 组件令牌 > 核心语义令牌 > 可控回退值

三层令牌：
  designsystem/theme/AppTokens.kt           — 核心语义令牌（AppColors 39字段/Spacing/Elevation/Opacity/Motion/Shapes/Typography/ControlSizeTokens）
  designsystem/theme/AppComponentTokens.kt  — 组件令牌（21 种：Button/Card/Input/Select/SelectionControl/Switch/Table/Dialog/Menu/Tag/Progress/Skeleton/Steps/Pagination/Slider/Rate + AppBar/Chip/Fab/BottomBar/ListItem）
  designsystem/util/AppDefaults.kt           — 快照（非 Composable 环境下的默认值访问，已同步令牌结构）

derive() 模式：
  AppColors.derive(primary) → HSL 色相位移，自动重算所有 39 个字段
  AppComponentTokens.default(colors) → 从 AppColors 自动派生组件令牌颜色
  tokens.derive { field = value } → 部分覆盖语法糖

组件用法示例：
  AppCard { Text("内容") }                          // 替代 Card + shadow + shape + CardDefaults 样板
  AppTopBar(title = "标题", onBack = { ... })       // 替代 CenterAlignedTopAppBar
  PrimaryButton(onClick = { ... }, label = "保存")   // 主按钮
  PaiButton("保存", onClick = { ... })               // 简化工厂
  AppConfirmDialog(show, onConfirm, onDismiss)       // 替代 AlertDialog 样板
  snackbar.showUndo(onUndo = { repo.insert(r) })    // 替代 showSnackbar + ActionPerformed 样板

Logic 模式（纯 Kotlin，可 JVM 单测）：
  ButtonLogic(scope, debounceMs) → isPressed/isLoading/防抖
  FormLogic(scope, initial, validator) → fields/errors/touched/submitting
  TableLogic(scope, data) → sorting/selection/pagination
```

## 七、📁 目录结构

```
com/babytracker/
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

---

## 八、新增组件指引

新增标准组件步骤（也可用 `scripts/generate-component.sh` 生成骨架）：

### Step 1：定义令牌
在 `AppComponentTokens.kt` 中新增 `XxxTokens` 数据类，然后在 `AppComponentTokens` 聚合中添加字段和 `default(colors)` 派生：

```kotlin
@Immutable
data class XxxTokens(
    val height: Dp = 48.dp,
    val cornerRadius: Dp = 12.dp,
    val containerColor: Color = Color.Unspecified,  // 由 default(colors) 派生
)
// AppComponentTokens 加一行: val xxx: XxxTokens = XxxTokens()
// companion object default() 加一行: xxx = XxxTokens(containerColor = colors.primary)
```

### Step 2：实现 Xxx.kt + XxxDefaults.kt
**XxxDefaults.kt** 从令牌系统读取值，**Xxx.kt** 纯 UI 层不含业务逻辑。

### Step 3：判断是否需要 Logic 文件
只有**管理内部交互状态**的组件才需要（如 Button 的 isPressed、Swipe 的滑动进度）。纯视觉/纯回调组件**不需要**（如 Card、TopBar）。

### Step 4：实现 XxxLogic + 桥接（如需要）
纯 Kotlin 类（接受 `CoroutineScope`），然后在 `Hooks.kt` 加 `rememberXxxLogic(scope: CoroutineScope? = null)` 桥接 Composable。

### Step 5：可选 — 简化工厂
`@Composable fun PaiXxx(...) = AppXxx(...)` 自动填充所有 Defaults。

### Step 6：更新快照
`AppDefaults.kt` 添加非 Composable 环境的快照字段。

### Step 7：优先级验证
```
显式参数 > XxxDefaults > 组件令牌 > 核心语义令牌 > 回退值
```
集成测试在 `ThemeTokenizationStaticAuditTest` 中补充。

---

## 九、📝 注释与提交规范

- 所有注释 **必须中文**。
- Commit message **必须中文**。
- 每次构建成功文件有变动必须提交
- 必须先更新CHANGELOG再提交
- 复杂逻辑写注释解释 **为什么这么做**（why），不重复代码表面意思（what）。

---
