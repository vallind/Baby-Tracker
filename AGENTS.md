# AGENTS.md — AI 工作说明书

> ⚠️ 任务前必须读取 [docs/lessons.md](docs/lessons.md)，确认无相关教训后再动手。
>
> 核心理念：先想清楚再动代码，改完只擦自己的屁股。
>
> 本文件只放代码库无法推导的约定（规则、坑、rationale）。目录结构等可自行从代码读取的信息见 `docs/project-structure.md`，不在此重复，避免漂移。

---

## 一、项目概览

Android 原生宝宝护理记录 App（Baby Tracker）。Jetpack Compose + Elyon UI（`vide/elegant` 复合构建），MVVM + Koin + Room + Supabase 同步。

分层：`core`（数据库、DI、备份、同步引擎、AI、工具、UI 基座 `core/ui`）/ `feature`（业务模块）/ `navigation`（路由）。模块索引与路由见 `docs/project-structure.md`。

## 二、开发命令

```bash
./gradlew assembleDebug          # 编译（每次改动后必跑）
./gradlew testDebugUnitTest      # 单元测试（每次改动后必跑，全绿才算完成）
./gradlew assembleRelease        # 发布构建（R8 + 资源压缩）
./gradlew lint                   # Lint（改动涉及 Compose/资源/Manifest/API 时跑）
```

## 三、AI 工作流四大准则

### 1. 先想再说，不猜

- 动手前**明确陈述理解**。有歧义列出所有可能解释，让你选。
- 如果存在更简单的方案，敢于**向上建言**。
- **"不清楚"的判定边界**：改动触碰数据模型、同步链路、跨模块共享 API、数据库 schema → **必须请示**；纯 UI 文案/间距/颜色/布局调整 → 直接干。
- 落在请示范围内的歧义，**立即停止**，等你澄清。

### 2. 简单优先，不堆料

- 只写解决**当前问题**的最少代码。不做超前设计。
- 单次用途的代码**不做抽象**（不抽 Manager / 不建 sealed class / 不写泛型工具，除非明确要求）。
- 写完自问："这代码会被骂过度设计吗？"——如果是，立即简化。

### 3. 手术刀式修改，不乱碰

- **只改用户要求的地方**。不顺手"优化"旁边的代码。
- 匹配文件现有风格。
- **清理只限于自己改过的地方的副作用**（我删了函数导致变量没用 → 删掉它）。不删改动范围外的死代码，最多顺嘴提一句。

### 4. 目标驱动，自我闭环

- 把模糊描述转化成**可验证的具体目标**。
- 多步骤任务先发简要计划，确认后一路执行到底。

---

## 四、🚨 绝对红线（无条件遵守）

| # | 规则 | 错误写法 | 正确写法 |
|---|---|---|---|
| 1 | **百分比双向夹紧** | `.coerceAtMost(1f)` 或 `.coerceAtLeast(0f)` 单独出现 | `.coerceIn(0f, 1f)` |
| 2 | **今日日期过滤** | `.firstOrNull { it.date == today }` 或 `startsWith(today)` 前缀比较 | `.filter { it.date.take(10) == today }` |
| 3 | **ViewModel 注册** | `single { MyViewModel(...) }` | `viewModel { MyViewModel(...) }` |
| 4 | **ViewModel 获取** | `get()` | `koinViewModel()` |
| 5 | **按 ID 加载数据** | 构造函数里直接 `flow` | `_trigger` + `flatMapLatest` 模式（防竞态/陈旧数据，宝宝切换时能重建数据流） |
| 6 | **Composable 嵌套定义** | `@Composable fun A() { @Composable fun B() {} }` | 所有 `@Composable` 定义在文件**顶层** |
| 7 | **弹窗平级** | 弹窗套在其他 if 块内部 | 所有弹窗（OverlayDialog/OverlayBottomSheet 等）在顶层 `Column` 中**平级**独立 `if` |
| 8 | **暗色主题来源** | `isSystemInDarkTheme()` | 只读 `theme.name == "night"` |
| 9 | **硬编码路径** | `"/data/data/..."` | 用 `context.filesDir` 等环境变量 |
| 10 | **改共享 API 不查调用方**（流程规则） | 直接改 DAO/Repository/工具类方法签名或行为 | **先全局搜索所有调用方**，评估影响后再改；改签名需按第六节走 🔴 确认 |

---

## 五、变更分级

| 级别 | 范围 | AI 动作 |
|---|---|---|
| 🔴**重大重构（必须确认）** | 改 Entities 表结构/字段类型；改色系系统；切架构；重写 BackupManager/SyncEngine 核心流程；升数据库版本号；换 DI/网络库；改 AndroidManifest 核心配置；**改 DAO/Repository/SyncEngine 等共享 API 的方法签名或行为（哪怕只加参数）** | **先出方案（影响范围 + 迁移步骤 + 调用方清单），等确认再动** |
| 🟢**日常开发（直接干）** | 增删改 Screen/ViewModel 逻辑；新页面/路由；修 bug；调 UI 间距/颜色/文本；性能优化；补测试 | 收到指令直接写，不请示（仍须遵守准则 1 的请示边界） |

---

## 六、测试纪律

- 每次改动后必跑 `./gradlew testDebugUnitTest`，全绿才算完成。
- 以下情况**必须**补测试：
  - 修复历史 bug（补回归测试，验证根因不再复发）
  - 改动纯逻辑（SyncEngine、StatsViewModel.aggregate、JSON 解析、AI 安全校验等）
  - 新增核心规则或边界逻辑
- **禁止镜像测试**：测试必须调用生产代码，不允许复制生产逻辑到测试里（生产改了测试不会失败 = 无效测试）。
- 已知测试盲区，优先补：SyncEngine、StatsViewModel.aggregate、BackupManager 还原路径。
- 测试代码同样遵守红线（边界值、日期过滤、百分比夹紧等）。

---

## 七、注释、提交与版本规范

- 所有注释**必须中文**。Commit message **必须中文**。
- 复杂逻辑写注释解释**为什么**（why），不重复代码表面意思（what）。
- CHANGELOG.md 按**提交批次**累积条目，**版本号只在发布时提升**（与 `app/build.gradle.kts` 的 versionName/versionCode 同步）。禁止为每次提交都升版本号。
- CHANGELOG 条目不允许留在 `[Unreleased]` 下提交；条目未分配版本号时，在当次发布批次统一挂版本。

---

## 八、UI 体系与 i18n

- **UI 基座为 Elyon**（`vide/elegant` 复合构建：elyon-core/ui/effects/blur/nav）。禁止再造自建设计系统/令牌层，也禁止直接用原生 M3（Card、TopAppBar、Button、AlertDialog 等）；组件一律用 `io.elyon.kmp.basic.*` / `io.elyon.kmp.overlay.*`。
- **主题**：根组件为 `BabyTrackerElyonTheme`（core/ui），颜色用 `ElyonTheme.colorScheme.*`，排版用 `ElyonTheme.textStyles.*`，禁止读 `isSystemInDarkTheme()` 判断应用暗色（红线 8 仍以主题名为准）。
- **应用级组件**：仅当 Elyon 缺失且跨功能重复时才在 `core/ui/components` 新增（如 RecordCard/TimePicker/DateTimeCascade/AppInput-error 态）；组件内部只消费 Elyon 原语与 `ElyonTheme`，不建令牌体系。
- **Elyon 缺失时的决策路径**：允许临时用原生 M3，但必须留下 `// TODO: 迁移到 Elyon 组件` 注释（当前存量：RecordCard 的 SwipeToDismissBox）。
- **i18n**：新增用户可见文本必须写入 `AppStrings`，禁止硬编码中文。存量硬编码文本按批次迁移。
- **Snackbar** 用 `snackbar.showUndo(onUndo = { ... })` 模式。

---

## 九、经验闭环（lessons.md）

- 任务前必须全文读取 `docs/lessons.md`。
- 出现以下情况，**提交时**必须向 lessons.md 追加条目：
  - 修复了跨模块 bug，且根因有普适性（同类问题可能再次出现）
  - 踩了文档未记录的坑
  - 发现本文件或红线遗漏的规则
- 条目格式：现象 → 原因 → 规则（含错误/正确写法）。

---

## 十、文档同步义务

改动以下内容后，必须同步更新对应文档：

| 改了什么 | 要更新的文档 |
|---|---|
| 目录结构/模块/路由 | `docs/project-structure.md` |
| UI 体系/组件约定 | `docs/design-system.md` |
| 同步流程/sync_metadata 表 | `docs/sync-architecture.md` |
| 数据模型/Room 表 | `docs/data-architecture.md` |

---

## 十一、完成定义（验证标准）

一次任务满足以下全部条件才算完成：

1. `./gradlew assembleDebug` 通过
2. `./gradlew testDebugUnitTest` 全绿（新增/修改逻辑有测试覆盖）
3. 被改动的每一行都能回溯到本次需求
4. （如适用）CHANGELOG 已更新、相关文档已同步

---

## 十二、参考文档

| 文档 | 内容 |
|---|---|
| `README.md` | 项目介绍、功能列表 |
| `docs/project-structure.md` | 完整目录结构、模块索引、技术栈 |
| `docs/design-system.md` | Elyon 组件约定、应用级组件、i18n 规则 |
| `docs/sync-architecture.md` | 同步引擎完整链路 |
| `docs/data-architecture.md` | 数据架构 |
| `docs/architecture.md` | 总体架构 |
| `docs/room-supabase-architecture.md` | Room 与 Supabase 对接 |
| `docs/lessons.md` | 开发教训（任务前必读） |
| `docs/aapt2-termux-fix.md` | Termux AAPT2 兼容问题 |
| `CHANGELOG.md` | 变更日志 |
