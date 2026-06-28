# AGENTS.md — AI 工作说明书

> 核心理念：先想清楚再动代码，改完只擦自己的屁股。

---

## 一、项目概览

Android 原生宝宝护理记录 App（Baby Tracker）。Jetpack Compose + Material 3，MVVM + Koin + Room。

## 二、开发命令

```bash
./gradlew assembleDebug          # 编译
./gradlew assembleRelease        # 发布构建（R8 + 资源压缩）
./gradlew lint                   # Lint 检查
```

## 三、目录结构

```
app/src/main/java/com/babytracker/
├── designsystem/     # 设计系统：主题令牌、可复用组件、Hooks、国际化
│   ├── theme/        # 主题 + Token + Defaults
│   ├── components/   # 可复用组件（21+ 个）
│   ├── hooks/        # useDebounce/useState/useLatestState + Logic 类
│   ├── i18n/         # AppStrings
│   ├── foundation/   # BorderContainer/CenterVerticallyRow
│   └── util/         # AppDefaults 快照
├── core/             # 业务基础设施：数据库、DI、备份、数据仓库、工具类
│   ├── backup/       # BackupManager
│   ├── database/     # Room（AppDatabase/Entities/Daos）
│   ├── di/           # Koin Modules
│   ├── data/         # Repository + Mapper
│   ├── domain/       # Domain Models
│   └── util/         # BabyController/DateUtils/VaccineSchedule
├── feature/          # 13 个业务模块（home/feeding/sleep/diaper/growth/vaccination/health/stats/timeline/settings 等）
└── navigation/       # 路由导航（AppNavigation.kt，11 条路由）
```

> 详细模块索引：`docs/project-structure.md`

## 四、AI 工作流四大准则

### 1. 先想再说，不猜

- 动手前**明确陈述理解**。有歧义列出所有可能解释，让你选。
- 如果存在更简单的方案，敢于**向上建言**。
- 遇到不清楚的地方，**立即停止**，等你澄清。

### 2. 简单优先，不堆料

- 只写解决**当前问题**的最少代码。不做超前设计。
- 单次用途的代码**不做抽象**（不抽 Manager / 不建 sealed class / 不写泛型工具，除非明确要求）。
- 写完自问："这代码会被骂过度设计吗？"——如果是，立即简化。

### 3. 手术刀式修改，不乱碰

- **只改用户要求的地方**。不顺手"优化"旁边的代码。
- 匹配文件现有风格。
- **清理只限于自己改过的地方的副作用**（我删了函数导致变量没用 → 删掉它）。不删改动范围外的死代码，最多顺嘴提一句。
- 验证标准：每个被改动的**每一行**，都必须能直接回溯到本次需求。

### 4. 目标驱动，自我闭环

- 把模糊描述转化成**可验证的具体目标**。
- 多步骤任务先发简要计划，确认后一路执行到底。

---

## 五、🚨 绝对红线（无条件遵守）

| # | 规则 | 错误写法 | 正确写法 |
|---|---|---|---|
| 1 | **百分比双向夹紧** | `.coerceAtMost(1f)` | `.coerceIn(0f, 1f)` |
| 2 | **今日日期过滤** | `.firstOrNull { it.date == today }` | `.filter { it.date.startsWith(today) }` |
| 3 | **ViewModel 注册** | `single { MyViewModel(...) }` | `viewModel { MyViewModel(...) }` |
| 4 | **ViewModel 获取** | `get()` | `koinViewModel()` |
| 5 | **按 ID 加载数据** | 构造函数里直接 `flow` | `_trigger` + `flatMapLatest` 模式 |
| 6 | **Composable 嵌套定义** | `@Composable fun A() { @Composable fun B() {} }` | 所有 `@Composable` 定义在文件**顶层** |
| 7 | **AlertDialog 平级** | 弹窗套在其他 if 块内部 | 所有 `AlertDialog` 在顶层 `Column` 中**平级**独立 `if` |
| 8 | **暗色主题来源** | `isSystemInDarkTheme()` | 只读 `theme.name == "night"` |
| 9 | **硬编码路径** | `"/data/data/..."` | 用 `context.filesDir` 等环境变量 |

---

## 六、变更分级

| 级别 | 范围 | AI 动作 |
|---|---|---|
| 🔴**重大重构（必须确认）** | 改 Entities 表结构/字段类型；改色系系统；切架构；重写 BackupManager 核心流程；升数据库版本号；换 DI/网络库；改 AndroidManifest 核心配置 | **先出方案（影响范围 + 迁移步骤），等确认再动** |
| 🟢**日常开发（直接干）** | 增删改 Screen/ViewModel 逻辑；新页面/路由；修 bug；调 UI 间距/颜色/文本；性能优化 | 收到指令直接写，不请示 |

---

## 七、注释与提交规范

- 所有注释**必须中文**。Commit message **必须中文**。
- 每次构建成功必须新提交。
- 必须先更新 `CHANGELOG.md` 再提交。
- 复杂逻辑写注释解释**为什么**（why），不重复代码表面意思（what）。

---

## 八、关键约束

- **优先使用 designsystem 组件**，禁止直接用原生 M3（Card、TopAppBar、Button、AlertDialog 等）。对应关系：`Card` → `AppCard`，`CenterAlignedTopAppBar` → `AppTopBar`，`Button` → `AppButton`/`PrimaryButton`，`AlertDialog` → `AppConfirmDialog`。完整列表见 `docs/design-system.md`。
- **新增组件判断标准**（两者同时满足才新增）：
  1. 同一视觉形态在项目中已出现 ≥ 2 处（跨功能重复算，按视觉形态计数，不是调用次数）
  2. 需要封装设计令牌（颜色/圆角/间距），而非纯布局组合
  → 否则直接用 Compose 原生或内联实现，不新增组件
- **Snackbar** 用 `snackbar.showUndo(onUndo = { ... })` 模式。

---

## 九、参考文档

| 文档 | 内容 |
|---|---|
| `README.md` | 项目介绍、功能列表 |
| `docs/project-structure.md` | 完整目录结构、模块索引、技术栈 |
| `docs/design-system.md` | 令牌架构、组件用法、新增组件指引 |
| `docs/Palette组件库设计深度分析报告.md` | 设计系统审计报告 |
| `docs/aapt2-termux-fix.md` | Termux AAPT2 兼容问题 |
| `CHANGELOG.md` | 变更日志 |
