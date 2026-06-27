# Palette 组件库设计深度分析报告

> Compose Multiplatform 主题令牌化体系审计与改进路线图
>
> 审计日期：2026-06-27 | 审计方：Z.ai 技术审计团队

---

## 目录

- [1 项目概览与审计范围](#1-项目概览与审计范围)
- [2 主题系统架构评价](#2-主题系统架构评价)
- [3 令牌化完成度审核](#3-令牌化完成度审核)
- [4 组件覆盖审计](#4-组件覆盖审计)
- [5 令牌深度缺口分析](#5-令牌深度缺口分析)
- [6 工程化成熟度评估](#6-工程化成熟度评估)
- [7 竞品主题系统对比](#7-竞品主题系统对比)
- [8 改进路线图](#8-改进路线图)

---

## 1 项目概览与审计范围

### 1.1 项目背景

Palette 是一个基于 Compose Multiplatform 的组件库，目标平台覆盖 Android、Desktop（JVM）和 iOS。项目采用令牌驱动（Token-first）的主题系统设计，通过 `PaletteTheme` 提供颜色、间距、形状、排版、透明度、动效、阴影和控件密度等核心令牌的统一注入，并支持 `PaletteComponentThemes` 实现组件级令牌覆盖，使产品团队能够在应用根节点统一调控组件样式，而无需逐实例传递样式参数。

项目当前发布版本为 `0.1.0`，Maven 坐标为 `xyz.junerver.compose:palette:0.1.0`，目标 Android `minSdk 24`、Desktop JVM、iOS `arm64` / `simulatorArm64`。库内部集成了 [compose-hooks](https://github.com/junerver/compose-hooks) 用于 React 风格的状态管理模式。

### 1.2 审计范围与方法

本次审计覆盖以下三个方面：

1. **源码全量扫描**：对 `palette/src/commonMain` 下所有 Kotlin 源文件进行静态分析，包括 86 个组件目录、核心令牌文件、主题注入链、Defaults 文件等
2. **CSV 50 项任务对照**：逐项核实 `docs/theme-tokenization-todo.csv` 中 50 项标记为 DONE 的任务在实际代码中的完成状态
3. **自动化审计**：通过 ripgrep 正则扫描统计硬编码常量、@Deprecated 标注、PaletteTheme 引用等

### 1.3 关键审计数据概览

| 指标 | 数值 |
|------|------|
| 组件目录总数 | 86 |
| 令牌组总数（PaletteComponentThemes 内） | 37 |
| 令牌字段总数 | 927 |
| Defaults 文件数 | 86 |
| 旧硬编码 `val` 常量总数 | 389 |
| 已弃用常量（@Deprecated） | 4（1%） |
| 完全清洁的 Defaults 文件（无硬编码残留） | 2 |
| 静态审计规则 | 2 |
| 测试行覆盖率（Kover） | 90.03% |
| 测试文件数 | 46 commonTest + ~95 desktopTest |
| 独立 Gradle 模块 | 6（palette / palette-code / palette-markdown / palette-mermaid / app / benchmark） |
| 支持语言（i18n） | 2（zh-CN / en-US） |

---

## 2 主题系统架构评价

### 2.1 CompositionLocal 注入链

Palette 的主题系统基于 Compose 的 `CompositionLocal` 机制，通过 11 个 `staticCompositionLocalOf` 将令牌注入到组件树中。`PaletteTheme` 可组合函数作为单一入口，接受所有令牌参数并通过 `CompositionLocalProvider` 向子树提供：

```kotlin
// PaletteTheme 注入的 11 个 CompositionLocal
LocalPaletteColors          // PaletteColors（34 字段）
LocalPaletteSpacing         // PaletteSpacing（6 级）
LocalPaletteShapes          // PaletteShapes（3 级）
LocalPaletteTypography      // PaletteTypography（3 级）
LocalPaletteOpacity         // PaletteOpacity（11 字段）
LocalPaletteMotion          // PaletteMotion（5 字段）
LocalPaletteElevation       // PaletteElevation（6 字段）
LocalPaletteControl         // PaletteControlTokens（3 级尺寸 + 边框/焦点）
LocalPaletteComponentThemes // PaletteComponentThemes（37 个组件令牌组）
LocalPaletteDarkTheme       // Boolean
LocalPaletteStrings         // PaletteStrings（22 字段 i18n）
```

`PaletteMaterialTheme` 在 `PaletteTheme` 的基础上额外包装了 Material3 的 `MaterialTheme`，将 Palette 的语义令牌映射到 Material3 的 `ColorScheme`，使得 Palette 组件与 Material3 组件可以共存并视觉对齐。这种双层桥接设计是合理的——它允许应用在 Palette 令牌体系下工作，同时保持与 Material3 生态的兼容性。

### 2.2 令牌分层模型

Palette 的令牌系统采用三层架构：

**第一层：核心语义令牌（Core Semantic Tokens）**

| 令牌类 | 字段数 | 示例字段 |
|--------|--------|---------|
| `PaletteColors` | 34 | primary, onPrimary, borderHover, bgSelected, surfaceElevated, textDisabled |
| `PaletteSpacing` | 6 | none, extraSmall, small, medium, large, extraLarge |
| `PaletteShapes` | 3 | small, medium, large |
| `PaletteTypography` | 3 | title, body, label |
| `PaletteOpacity` | 11 | disabled, subtle, hover, pressed, selected, overlay |
| `PaletteMotion` | 5 | durationFast, durationNormal, durationSlow, overlayEnter, overlayExit |
| `PaletteElevation` | 6 | none, raised, overlay, modal, floating, focusShadowBlur |
| `PaletteControlTokens` | 3级×6字段 + 4 | small/medium/large × (height/fontSize/iconSize/padding/radius) + borderWidth |

**第二层：组件令牌（Component Tokens）**

`PaletteComponentThemes` 包含 37 个不可变数据类，每个对应一组组件的样式令牌：

```
PaletteBorderContainerTokens   PaletteButtonTokens          PaletteCheckboxTokens
PaletteRadioTokens             PaletteSwitchTokens           PaletteSelectionControlTokens
PaletteFormTokens              PaletteTextFieldTokens        PaletteSelectTokens
PaletteDateTimeTokens          PaletteInputTokens            PaletteCardTokens
PaletteTableTokens             PaletteDataGridTokens         PaletteDataDisplayTokens
PaletteDataEntryTokens         PaletteNavigationMenuTokens   PaletteAppBarTokens
PaletteFloatingLayerTokens     PaletteDialogTokens           PaletteDrawerTokens
PalettePopupTokens             PaletteActionSheetTokens      PaletteMessageTokens
PaletteNotificationTokens      PaletteToastTokens            PaletteTagTokens
PaletteFeedbackDisplayTokens   PaletteProgressTokens         PaletteMediaTokens
PaletteUtilityTokens           PaletteLayoutTokens           PaletteFloatingActionTokens
PaletteScreenTokens            PaletteBorderContainerTokens
```

总计 927 个令牌字段，覆盖颜色、尺寸、排版、圆角、边框、阴影和动效等视觉属性。

**第三层：Defaults 桥接层**

每个组件的 `XxxDefaults` 对象通过 `@Composable` 函数从 `PaletteTheme.componentThemes.xxx` 读取令牌值，作为组件参数的默认值来源。例如：

```kotlin
object CardDefaults {
    // 旧 API（硬编码常量，未弃用）
    val CornerRadius: Dp = 12.dp

    // 新 API（令牌驱动）
    @Composable
    fun cornerRadius(): Dp = PaletteTheme.componentThemes.card.cornerRadius
}
```

### 2.3 优先级模型

Palette 文档定义了五级样式优先级：

```
显式组件参数 > XxxDefaults 参数 > 组件令牌 > 语义令牌 > 受控回退值
```

这意味着用户在调用组件时传入的参数始终具有最高优先级，不会令牌覆盖。测试文件 `ComponentThemeOverrideUiTest` 验证了 7 个组件（TextField、Checkbox、Select、Table、Pagination、Tag、Screen）的显式参数优先级正确性。

### 2.4 架构优势

1. **单一入口点**：`PaletteMaterialTheme` 接受所有令牌参数，用户无需多层嵌套 Provider
2. **derive() 自动重算**：修改 `primary` 颜色后，`derive()` 会自动重算 `textSecondary`、`bgHover`、`borderFocus` 等派生语义令牌
3. **组件令牌独立覆盖**：`PaletteComponentThemes` 允许在不修改全局令牌的情况下单独调整组件样式
4. **Material3 桥接**：`toSemanticColors().toMaterialScheme()` 映射 40+ ColorScheme 槽位，保证 Palette 与 Material3 组件视觉一致

### 2.5 架构缺陷

1. **PaletteColorsSnapshot 严重落后**：仅暴露 9/34 字段，74% 的新语义令牌无法通过 `PaletteDefaults.colors` 访问
2. **PaletteTypography 层级不足**：仅 title/body/label 3 级，远低于行业标准的 10+ 级，导致组件无法区分更多文本层级
3. **无密度感知间距**：`PaletteSpacing` 只有 6 级固定值，缺少 compact/comfortable/large 密度预设
4. **双 API 模式缺少弃用引导**：旧常量与新 composable 函数并存，但旧 API 无 `@Deprecated` 标注，消费者无法感知迁移方向

---

## 3 令牌化完成度审核

### 3.1 已完成任务（36 项）

以下任务在代码中有完整的实现证据：

| ID | 任务 | 验证结果 |
|----|------|---------|
| TT-001 | Theme API + PaletteComponentThemes | `PaletteComponentThemes` 含 37 个令牌组，`PaletteTheme`/`PaletteMaterialTheme` 均接受 `componentThemes` 参数 |
| TT-002 | 令牌分类 | `PaletteColors` 含 35 个语义/状态/表面令牌 |
| TT-003 | 优先级模型 | 文档完整，`ComponentThemeOverrideUiTest` 测试 7 个组件 |
| TT-004 | 组件令牌契约 | 37 个 `PaletteXxxTokens` 数据类 |
| TT-005 | copy/derive 工具 | `PaletteColors.copy()` + `derive()` 均实现 |
| TT-006 | Form/control 令牌 | `PaletteControlTokens` 含 3 级尺寸令牌 |
| TT-007 | Material3 桥接 | `PaletteSemanticColors.toMaterialScheme()` 映射 40+ 槽位 |
| TT-011 | Colors 语义/状态令牌 | `textSecondary`/`textDisabled`/`borderHover`/`bgHover`/`overlay` 等全部存在 |
| TT-012 | Spacing/radius/typography | 三个令牌类 + `PaletteControlTokens` 三级尺寸 |
| TT-013 | Motion/elevation/opacity | `PaletteMotion`(5) + `PaletteElevation`(6) + `PaletteOpacity`(11) |
| TT-014 | BorderContainer 令牌 | `PaletteBorderContainerTokens` 完整 |
| TT-015 | Text/Screen 令牌 | `PaletteScreenTokens` + `TextDefaults` 令牌函数 |
| TT-016 | Button 令牌 | `PaletteButtonTokens` 含变体颜色 + 3 级尺寸 + disabledAlpha |
| TT-017 | Checkbox 令牌 | `PaletteCheckboxTokens` 完整 |
| TT-018 | Radio 令牌 | `PaletteRadioTokens` 完整 |
| TT-019 | Switch 令牌 | `PaletteSwitchTokens` 含几何/颜色/动效 |
| TT-020 | Slider/Rate 令牌 | `PaletteSelectionControlTokens` 覆盖 |
| TT-021 | Toggle/Segmented 令牌 | 同上 |
| TT-022 | TextField 令牌 | `PaletteTextFieldTokens` 30+ 字段 |
| TT-023 | Select 令牌 | `PaletteSelectTokens` 完整 |
| TT-024 | DateTime 令牌 | `PaletteDateTimeTokens` 完整 |
| TT-025 | InputNumber/OTP/Search | `PaletteInputTokens` 覆盖 |
| TT-026 | Form 布局令牌 | `PaletteFormTokens` 含 spacing/width/gap/typography/colors |
| TT-027 | Menu 令牌 | `PaletteNavigationMenuTokens` 完整 |
| TT-028 | Tabs/Breadcrumb/Steps/Collapse | 各自令牌覆盖 |
| TT-029 | Toolbar/PageHeader | `PaletteAppBarTokens` |
| TT-030 | Card 令牌 | `PaletteCardTokens` 含变体颜色 + 尺寸 |
| TT-031 | Table/DataGrid 令牌 | 两个独立令牌组 |
| TT-032 | List/Descriptions | `PaletteDataDisplayTokens` |
| TT-033 | Tag/Badge/Avatar | `PaletteTagTokens` + 各自令牌组 |
| TT-034 | Statistic/Result/Empty/Alert | `PaletteFeedbackDisplayTokens` |
| TT-035 | Message/Notification/Toast | 三个独立令牌组 |
| TT-036 | Dialog/Drawer/Popup/ActionSheet | 四个独立令牌组 |
| TT-037 | Popover/Tooltip/Popconfirm/Tour | `PaletteFloatingLayerTokens` |
| TT-041 | Carousel/Image/ColorPicker | `PaletteMediaTokens` |
| TT-043 | Grid/Space/Affix | `PaletteLayoutTokens` |

### 3.2 部分完成任务（9 项）

| ID | 任务 | 完成部分 | 缺失部分 |
|----|------|---------|---------|
| TT-008 | 验证工具 | 静态审计测试存在（2 条规则） | 无 Gradle 专用任务；Detekt 无自定义规则拦截硬编码常量 |
| TT-010 | 文档 | `docs/theming.md` 含指南和矩阵 | 无消费者迁移指南 |
| TT-038 | Progress/Loading/Skeleton | 令牌函数已存在 | 硬编码常量未弃用 |
| TT-039 | Timeline/Steps | 令牌已存在 | DotSize/LineWidth 硬编码未弃用 |
| TT-040 | Transfer/Tree/Sortable/VirtualList | `PaletteDataEntryTokens` 存在 | Transfer 有 11 个硬编码常量未弃用 |
| TT-042 | QRCode/Barcode/Watermark | `PaletteUtilityTokens` 存在 | QR/Barcode 前景色未默认走主题；Watermark 5 个常量未弃用 |
| TT-044 | Backtop/FloatButton | `PaletteFloatingActionTokens` 存在 | 6+6 个硬编码常量未弃用 |
| TT-045 | ComponentSize | `ComponentSize.tokens()` 扩展函数已加 | 枚举构造器仍硬编码 Dp/Sp 值 |
| TT-046 | 原始 alpha 公式 | `PaletteOpacity` 已集中定义 11 个值 | 组件 Defaults 中仍有 0.5f/0.72f 等未引用 `PaletteOpacity` |

### 3.3 未完成任务（5 项）

| ID | 任务 | 缺失详情 | 影响级别 |
|----|------|---------|---------|
| **TT-009** | 迁移安全（弃用标注） | 389 个旧硬编码常量中仅 4 个标了 `@Deprecated`（1%），且无 `ReplaceWith` 指令，无移除时间表 | 🔴 严重 |
| **TT-048** | 主题覆盖测试 | 缺暗色模式组件令牌覆盖测试；缺状态令牌（disabled/hover/focus/error）传播测试 | 🟡 中等 |
| **TT-049** | 暗色模式/状态回归 | 无 `PaletteComponentThemes.default(darkTheme=true)` 与 `darkTheme=false` 差异测试 | 🟡 中等 |
| **TT-050** | API 迁移文档 | 无 `docs/migration.md`；Palette.kt 导出了 37 个令牌类型别名但无旧→新映射表 | 🟡 中等 |
| **TT-047** | 外部样式 API 审计 | 无矩阵文档定义每个组件的公开样式表面 vs 内部细节 | 🟡 中等 |

### 3.4 弃用标注缺口专项分析

这是令牌化改造中最严重的收尾缺口。Palette 采用"双 API"迁移模式——旧常量保留兼容性，新增 `@Composable` 令牌驱动函数作为推荐路径。然而：

**现状数据：**

- 86 个 Defaults 文件中共 **389 个**硬编码 `val` 常量（类型为 Dp/Float/Int/TextUnit/Long/Shape/Color）
- 仅 **4 个**常量标有 `@Deprecated`（全部在 `TextFieldDefaults` 和 `SearchBarDefaults` 中）
- 弃用率仅 **1%**
- 4 个已有弃用标注均**无 `ReplaceWith` 指令**，IDE 无法自动修复
- 无 `DeprecationLevel.WARNING` 或 `ERROR` 级别设定
- 无弃用移除时间表文档

**风险分析：**

没有编译器警告，消费者根本不知道令牌驱动替代方案的存在。旧常量与主题系统脱节——修改 `PaletteComponentThemes.button.disabledAlpha` 不会影响仍在使用 `ButtonDefaults.DisabledAlpha` 常量的代码。这导致主题覆盖功能在实际消费端部分失效。

---

## 4 组件覆盖审计

### 4.1 CSV 未提及的组件（10 个）

代码库中有 86 个组件目录，CSV 只覆盖了其中 76 个。以下 10 个组件在 CSV 50 项任务中**零提及**：

| # | 组件 | 有 Defaults? | 有令牌? | 硬编码情况 | 严重程度 |
|---|------|-------------|---------|-----------|---------|
| 1 | **BottomNavigation** | ✅ | ⚠️ 部分 | 6 个硬编码常量（Height/ItemCornerRadius/ItemPadding 等），颜色走 `navigationMenu` 令牌但尺寸未独立令牌化 | 🟡 中 |
| 2 | **CodeBlock** | ✅ | ✅ | 令牌走 `utility` 组；主文件 `CodeBlock.kt` 有 6 处 `4.dp`/`8.dp` 绕过 Defaults | 🟡 中 |
| 3 | **Container (PContainer)** | ❌ | ❌ | 无 Defaults、无令牌，直接用 `PaletteMaterialTheme.colorScheme` + 硬编码 `0.dp` | 🔴 高 |
| 4 | **Markdown (Editor/Viewer)** | ✅ | ✅ | 令牌走 `utility` 组；主文件有 1 处硬编码 | 🟢 低 |
| 5 | **MermaidDiagram** | ✅ | ⚠️ 部分 | 有 `MermaidDefaults` 走 `utility` 令牌，但**主文件有 174 处硬编码** | 🔴 高 |
| 6 | **Pagination** | ✅ | ✅ | 令牌走 `dataDisplay` 组；仅 2 个旧常量未弃用 | 🟢 低 |
| 7 | **Scaffold (PScaffold)** | ✅ | ❌ | **ScaffoldDefaults 不引用 PaletteTheme**，全硬编码 | 🔴 高 |
| 8 | **SwipeToDismissBox** | ❌ | ❌ | 纯 Material3 封装，无 Palette 令牌 | 🟢 低 |
| 9 | **Text (PText)** | ✅ | ✅ | `TextDefaults` 走 `PaletteTheme.colors`；被静态审计白名单允许 | 🟢 低 |
| 10 | **Upload** | ✅ | ✅ | 令牌走 `dataEntry` 组；3 个旧常量未弃用 | 🟢 低 |

### 4.2 组件主文件硬编码统计

除了 Defaults 文件中的 389 个旧常量外，**26 个组件主文件**中还有约 **220 处**硬编码样式值直接绕过了 Defaults 层：

| 组件文件 | 硬编码数 | 典型问题 |
|---------|---------|---------|
| MermaidDiagram.kt | 174 | nodeWidth=132.dp, nodeHeight=44.dp, padding, radius, alpha 全部写死 |
| BorderTextField.kt | 7 | `4.dp`/`8.dp` 内联 padding |
| ColoredCheckBox.kt | 6 | 0.7f/0.5f 比例常量 |
| CodeBlock.kt | 6 | 内联 dp 值 |
| Badge.kt | 4 | 固定尺寸 |
| EditableTagGroup.kt | 4 | 固定 padding/尺寸 |
| Segmented.kt | 3 | 内联间距 |
| Container.kt | 3 | 硬编码 0.dp + RoundedCornerShape |
| 其他 18 个文件 | 各 1-2 处 | 零散的内联值 |

### 4.3 严重案例：MermaidDiagram

MermaidDiagram.kt 是整个代码库中硬编码最严重的组件。虽然 `MermaidDefaults` 已经接入了 `PaletteTheme.componentThemes.utility` 令牌组，但主渲染代码几乎完全绕过了 Defaults 层：

```kotlin
// MermaidDiagram.kt 中的典型硬编码（仅列举部分）
val nodeWidth = 132.dp       // 应走 MermaidDefaults 或令牌
val nodeHeight = 44.dp       // 应走令牌
.copy(alpha = 0.18f)         // 应走 PaletteOpacity
RoundedCornerShape(8.dp)     // 应走 PaletteShapes 或令牌
.padding(horizontal = 10.dp, vertical = 6.dp)  // 应走 PaletteSpacing 或令牌
3.dp.toPx() / 2.dp.toPx()   // edge stroke width 应走令牌
```

这些硬编码使得 Mermaid 图表的视觉表现完全无法通过主题系统调控，与项目的令牌优先设计理念严重矛盾。

### 4.4 严重案例：Scaffold 和 Container

**ScaffoldDefaults** 是唯一不引用 `PaletteTheme` 的 Defaults 文件：

```kotlin
object ScaffoldDefaults {
    @Composable
    fun colors(containerColor: Color = PaletteMaterialTheme.colorScheme.background): ScaffoldColors
    @Composable
    fun contentPadding(): PaddingValues = PaddingValues(0.dp)       // 硬编码
    @Composable
    fun floatingActionButtonPadding(): PaddingValues = PaddingValues(16.dp)  // 硬编码
}
```

Scaffold 作为应用级布局框架，其 padding 值硬编码意味着无法通过主题调整内容间距，这与"根级覆盖"的设计目标相悖。

**PContainer** 更为严重——没有 Defaults 文件、没有令牌、直接使用 Material3 Surface 组件和硬编码值。作为一个通用容器组件，它应该至少有 `PaletteContainerTokens` 提供颜色、形状、阴影等样式控制。

---

## 5 令牌深度缺口分析

### 5.1 PaletteColorsSnapshot 严重落后

`PaletteDefaults` 提供了一组快照类（`PaletteColorsSnapshot`、`PaletteSpacingSnapshot` 等）用于非 Composable 上下文访问主题值。然而 `PaletteColorsSnapshot` 仅暴露了 PaletteColors 34 个字段中的 9 个，缺失率高达 74%：

| 类别 | 已暴露 | 缺失 |
|------|--------|------|
| 基础色 | primary, onPrimary, border, surface, onSurface, hint, error, success, warning | onError |
| 语义色 | — | info, danger |
| 文本色 | — | textPrimary, textSecondary, textTertiary, textDisabled |
| 反色 | — | inverseSurface, inverseOnSurface |
| 表面/背景 | — | pageBackground, appBackground, surfaceElevated, surfaceOverlay |
| 边框 | — | divider, borderHover, borderFocus, borderDisabled |
| 状态背景 | — | bgDisabled, bgHover, bgPressed, bgSelected |
| 遮罩/阴影 | — | overlay, shadow, shadowFocus, shadowError |

**影响**：通过 `PaletteDefaults.colors` API 无法访问 25 个新语义令牌，使得非 Composable 上下文（如 ViewModel 中的颜色决策、测试断言）无法利用完整的语义令牌体系。

### 5.2 Typography 层级不足

当前 `PaletteTypography` 仅提供 3 级排版：

```kotlin
class PaletteTypography(
    val title: TextStyle = TextStyle(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = Medium),
    val body: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    val label: TextStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = Medium),
)
```

对比行业标准的排版层级：

| 体系 | 层级数 | 典型层级 |
|------|--------|---------|
| **Material 3** | 13 | displayLarge→Small, headlineLarge→Small, titleLarge→Small, bodyLarge→Small, labelLarge→Small |
| **Ant Design 5** | 11 | h1-h6 + body + caption + title + subtitle |
| **Mantine** | 10+ | h1-h7 + xl/xxl sizes |
| **Chakra UI** | 8+ | xs → 4xl + display |
| **Palette** | **3** | title, body, label |

3 级排版导致组件无法区分更多的文本语义层级。例如，一个 Dialog 同时需要标题（headline）、副标题（subtitle）、正文（body）、辅助文字（caption）、按钮标签（label），但 Palette 只能提供 3 种选择，组件开发者不得不在 Defaults 中硬编码额外的 TextStyle。

### 5.3 Spacing 无密度变体

`PaletteSpacing` 当前只有 6 级固定值：

```kotlin
class PaletteSpacing(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
)
```

缺少的关键功能：

1. **密度预设**：compact/comfortable/large 三种密度应影响多个令牌（spacing + control height + padding + font size）的联合变化，而非单独调整
2. **页面级间距**：无 contentPadding、pageMargin、sectionGap 等页面级布局间距令牌
3. **组件内间距别名**：无 iconTextGap、inputHelperGap、cardContentPadding 等语义化间距别名

Mantine 的 `spacing` 属性配合 `theme.spacing` 数组实现密度感知，Chakra UI 的 `size` prop 直接联动 padding/fontSize/height。Palette 目前缺少这种"一改全改"的密度控制能力。

### 5.4 FormTokens 旧常量未迁移

`FormTokens` 保留了 20 个硬编码常量，虽然已经添加了对应的 `@Composable` 代理函数，但旧常量完全未弃用：

```kotlin
object FormTokens {
    // 旧常量（未弃用，仍可直接引用）
    val BorderWidthDefault = 1.dp
    val BorderWidthFocus = 2.dp
    val CornerRadiusSmall = 4.dp
    val CornerRadiusMedium = 6.dp
    val HeightSmall = 24.dp
    val HeightMedium = 32.dp
    // ... 共 20 个

    // 新代理函数（走主题令牌）
    @Composable fun borderWidthDefault() = PaletteTheme.control.borderWidth
    @Composable fun borderWidthFocus() = PaletteTheme.control.focusBorderWidth
    @Composable fun durationFast() = PaletteTheme.motion.durationFast
    // ... 仅 8 个代理函数
}
```

风险在于新代码可能直接引用 `FormTokens.BorderWidthDefault` 而非 `FormTokens.borderWidthDefault()`，从而绕过主题系统。当前仍有部分组件（如 `TextFieldDefaults.BorderWidth`）直接引用 `FormTokens` 常量而非代理函数。

### 5.5 ComponentSize 枚举硬编码

`ComponentSize` 枚举是表单类组件共享的尺寸规格，但其构造器硬编码了 18 个 Dp/Sp 值：

```kotlin
enum class ComponentSize(
    val height: Dp,
    val fontSize: TextUnit,
    val iconSize: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val cornerRadius: Dp
) {
    Small(height = 24.dp, fontSize = 14.sp, iconSize = 16.dp, ...),
    Medium(height = 40.dp, fontSize = 16.sp, iconSize = 20.dp, ...),
    Large(height = 40.dp, fontSize = 18.sp, iconSize = 24.dp, ...),
}
```

虽然有扩展函数 `ComponentSize.tokens()` 可从 `PaletteTheme.control` 读取主题令牌，但枚举属性 `.height`、`.fontSize` 等是直接的硬编码值，绕过主题。这意味着如果用户通过 `PaletteControlTokens` 自定义了 small.height = 28.dp，使用 `ComponentSize.Small.height` 的代码仍然得到 24.dp。

---

## 6 工程化成熟度评估

### 6.1 测试覆盖

**已建立：**
- Kover 行覆盖率 90.03%，CI 门禁阈值 ≥80%
- `commonTest`：46 个测试文件（逻辑/纯函数测试）
- `desktopTest`：~95 个测试文件（Compose UI 测试）
- 关键测试：
  - `ThemeTokenizationStaticAuditTest`：2 条规则（禁止 Defaults 直接读 PaletteTheme.colors；禁止 Color.Black/White/inline alpha）
  - `ComponentThemeOverrideUiTest`：28+ 组件令牌组的覆盖传播测试 + 7 组件显式参数优先级测试

**缺失：**

| 测试场景 | 状态 |
|---------|------|
| 暗色模式组件令牌覆盖 | 🔴 缺失 — 无 `PaletteComponentThemes.default(darkTheme=true)` 与 `darkTheme=false` 的差异测试 |
| 状态令牌传播（disabled/hover/focus/error） | 🔴 缺失 — 现有测试仅覆盖每组的 1 个代表色 + 1 个尺寸 |
| Error/Warning/Success 边框令牌传播 | 🔴 缺失 — TextFieldTokens 定义了 errorBorderColor 等，但无覆盖测试 |
| 显式参数优先级（状态色） | 🟡 部分 — 仅测试了基础颜色参数，未测试 disabledColor/focusColor 等 |
| PaletteColors → ComponentThemes → 状态色 集成链 | 🟡 缺失 |
| 渲染后组件视觉验证 | 🟡 缺失 — 所有测试验证 Defaults 函数返回值，非实际渲染结果 |

### 6.2 CI/CD 体系

**已建立：**

| 工具 | 状态 | 配置位置 |
|------|------|---------|
| Detekt | ✅ 活跃 | `config/detekt/detekt.yml`，`maxIssues: 0` |
| Ktlint | ✅ 活跃 | 集成于 `build.gradle.kts` |
| Kover 覆盖率门禁 | ✅ 活跃 | ≥80%，当前 90.03% |
| CI 质量检查 | ✅ 活跃 | `.github/workflows/ci.yml` → `runQualityChecks` |
| CI 测试 | ✅ 活跃 | `.github/workflows/ci.yml` → `runCoverageChecks` |
| 发布前校验 | ✅ 活跃 | `.github/workflows/release-check.yml` → `verifyReleaseReadiness` |

**缺失：**
- 无自定义 Detekt 规则拦截 Defaults 中的硬编码常量（`val X: Dp = <literal>.dp`）
- 无 `themeTokenAudit` 专用 Gradle 任务（审计测试依赖 `allTests` 隐式运行）
- 无 CI 性能回归门禁（有 Desktop/Android 基准测试但不在 CI 中自动校验）
- 无自动发布到 Maven Central 闭环

### 6.3 文档体系

| 文档 | 状态 | 详情 |
|------|------|------|
| `docs/theming.md` | ✅ 存在 | 189 行，含 4 个覆盖示例 + 令牌-组件矩阵 + 优先级规则 |
| README theming 章节 | ✅ 存在 | 中英文版，含 derive()/PaletteComponentThemes 代码示例 |
| CHANGELOG.md | ✅ 存在 | Unreleased 段记录令牌变更 |
| AGENTS.md | ✅ 存在 | 162 行编码规范，含主题/令牌规则 |
| `docs/maturity-gaps.md` | ✅ 存在 | 成熟度缺口自评估 |
| `docs/migration.md` | ❌ 缺失 | 无消费者迁移指南（旧常量 → 新函数映射表） |
| 在线 API 文档 | ❌ 缺失 | 无 Dokka 生成或在线文档站点 |
| 交互式主题调试器 | ❌ 缺失 | Demo 有主题切换但无专用调试页 |

### 6.4 无障碍

**现状：**
- 37/86 个组件有 `contentDescription` / `semantics` 代码
- 无系统化无障碍测试基线
- `docs/maturity-gaps.md` 自评"未达成"

**缺失：**
- 无独立无障碍测试与验收基线
- 无 `semantics` 合规扫描规则
- 无 TalkBack/VoiceOver 自动化验证
- 无焦点顺序（tab order）系统化设计

### 6.5 国际化

**现状：**
- `PaletteStrings` 支持 zh-CN 和 en-US，共 22 个字段
- `PaletteTheme.strings` 可注入自定义语言包

**缺失：**
- 无 zh-TW / ja / ko 等语言
- 无 RTL 布局系统化支持（仅 5 个文件有 LayoutDirection 引用）
- 无系统化资源文件方案（当前硬编码在 PaletteStrings 中）
- 无语言包动态加载机制

### 6.6 独立模块与主题系统断裂

项目有 3 个独立 Gradle 模块：

| 模块 | 内容 | 依赖 `:palette` | 主题令牌 |
|------|------|----------------|---------|
| palette-code | 代码语法高亮引擎（29 文件，Lexer/Grammar/Tokenizer） | ❌ 不依赖 | 不需要（纯逻辑库） |
| palette-markdown | Markdown 解析/渲染器（4 文件） | ❌ 不依赖 | 不需要（纯逻辑库） |
| palette-mermaid | Mermaid 图表解析器（18 文件，15 种图表解析器） | ❌ 不依赖 | 不需要（纯逻辑库） |

这三个模块本身是解析/逻辑层，不含 Compose UI 代码，不需要主题令牌。然而，**palette 主模块中的 UI 消费者**存在问题：

- `MermaidDiagram.kt`：174 处硬编码样式值，MermaidDefaults 形同虚设
- `MarkdownViewer.kt`：1 处硬编码
- `CodeBlock.kt`：6 处绕过 Defaults 的硬编码

逻辑层与 UI 层的断裂导致主题系统无法穿透到这些组件的渲染代码中。

---

## 7 竞品主题系统对比

### 7.1 六维度横向对比

| 维度 | Palette | Material 3 | Chakra UI | Ant Design 5 | Mantine |
|------|---------|-----------|-----------|-------------|---------|
| **令牌层级** | 3 层（核心/组件/桥接） | 3 层（核心/组件/表面） | 2 层（语义/组件） | 5 层（种子/映射/别名/组件/变体） | 3 层（颜色/间距/组件） |
| **组件令牌覆盖** | 37 组 / 86 组件 | 全覆盖 | 全覆盖 | 全覆盖 + Design Token | 全覆盖 |
| **暗色模式** | derive(darkTheme=true) | 动态 ColorScheme | colorMode 切换 | algorithm 生成 | colorScheme 对象 |
| **密度系统** | ❌ 无 | 无（仅 Compose 紧凑） | size=sm/md/lg | sizeToken | spacing scale |
| **无障碍** | 37/86 组件 | 系统化 | 系统化 | 系统化 | 系统化 |
| **迁移工具** | 无（仅 4 个 @Deprecated） | codemod | codemod | less-loader | 暂无 |

### 7.2 Palette 的独特优势

1. **Compose Multiplatform 跨平台**：Android/Desktop/iOS 统一代码，而 Chakra/Ant/Mantine 仅限 Web
2. **derive() 自动派生重算**：修改 primary 颜色后自动重算 textSecondary/bgHover/borderFocus 等派生令牌，Material 3 需要手动构建 ColorScheme
3. **令牌优先架构**：从项目初始就采用令牌驱动设计（非事后补丁），保证了架构一致性
4. **组件令牌根级覆盖**：`PaletteComponentThemes` 允许在应用根节点调整任意组件样式，无需修改组件调用处

### 7.3 Palette 的关键差距

1. **Ant Design 5 的五层令牌体系**：种子令牌 → 映射令牌 → 别名令牌 → 组件令牌 → 变体令牌，比 Palette 的三层更精细。Palette 缺少"别名令牌"层来统一 disabled/hover/focus 等跨组件共享的状态色
2. **Mantine/Chakra 的密度感知**：Mantine 的 `theme.spacing` 配合 `defaultRadius` 和 `fontSizes` 实现整体密度控制；Chakra 的 `size` prop 联动 height/padding/fontSize/minW。Palette 的 `PaletteControlTokens` 虽有三级尺寸但缺少全局密度预设
3. **Material 3 的动态取色**：Android 12+ 的动态颜色（Dynamic Color）从壁纸生成完整 ColorScheme。Palette 的 derive() 是静态的，无法响应用户系统主题变化
4. **全行业系统化无障碍**：竞品均内置 a11y 支持（ARIA 属性、焦点管理、屏幕阅读器语义），Palette 仅 37/86 组件有基础 semantics

---

## 8 改进路线图

### 8.1 P0 阶段：关键修复（2-3 周）

| # | 任务 | 工作量 | 前置依赖 | 验收标准 |
|---|------|--------|---------|---------|
| 1 | 批量为 389 个旧常量添加 `@Deprecated(message="Use XxxDefaults.xxx()", replaceWith=ReplaceWith("xxx()"))` | 3 天 | 无 | 所有 Defaults 文件的旧 `val` 常量有弃用标注 |
| 2 | ScaffoldDefaults 令牌化（接入 PaletteTheme，创建 LayoutTokens 中的 scaffold 字段） | 1 天 | 无 | ScaffoldDefaults 所有函数读取 PaletteTheme |
| 3 | Container 令牌化（创建 PaletteContainerTokens，添加 Defaults 文件） | 1 天 | 无 | PContainer 参数走令牌 |
| 4 | MermaidDiagram 主文件清理（174 处硬编码走 MermaidDefaults） | 2 天 | MermaidDefaults 扩展字段 | MermaidDiagram 无内联 dp/sp 值 |
| 5 | FormTokens 20 个旧常量标 @Deprecated | 0.5 天 | 无 | FormTokens 旧常量有弃用标注 |
| 6 | 为现有 4 个 @Deprecated 添加 ReplaceWith 指令 | 0.5 天 | 无 | IDE 可自动修复弃用 |

### 8.2 P1 阶段：深度补强（4-6 周）

| # | 任务 | 工作量 | 前置依赖 | 验收标准 |
|---|------|--------|---------|---------|
| 1 | PaletteColorsSnapshot 补全 25 个缺失字段 | 1 天 | 无 | PaletteDefaults.colors 暴露全部 34 字段 |
| 2 | PaletteTypography 扩展至 8+ 级（增加 headline/subtitle/caption） | 3 天 | 无 | 组件可使用 8+ 排版层级 |
| 3 | 暗色模式组件令牌覆盖测试 | 2 天 | 无 | darkTheme=true 下 37 个令牌组覆盖测试通过 |
| 4 | 状态令牌传播测试（disabled/hover/focus/error） | 3 天 | 无 | 10+ 组件的状态令牌覆盖测试通过 |
| 5 | 创建 docs/migration.md 消费者迁移指南 | 2 天 | P0-1 完成 | 每个组件有旧→新映射表 |
| 6 | 添加 themeTokenAudit Gradle 专用任务 | 1 天 | 无 | CI 中 themeTokenAudit 可独立运行 |
| 7 | BottomNavigation 独立令牌组 | 1 天 | 无 | BottomNavigationDefaults 全部走令牌 |
| 8 | CodeBlock.kt / BorderTextField.kt 硬编码清理 | 1 天 | 无 | 主文件无内联样式值 |

### 8.3 P2 阶段：工程化完善（6-10 周）

| # | 任务 | 工作量 | 前置依赖 | 验收标准 |
|---|------|--------|---------|---------|
| 1 | 无障碍审计基线 + 86 组件 semantics 补全 | 5 天 | 无 | a11y 审计通过，所有组件有 semantics |
| 2 | i18n 扩展（ja/ko/zh-TW） + RTL 适配 | 5 天 | 无 | 5+ 语言支持，RTL 布局正确 |
| 3 | PaletteSpacing 密度变体（compact/comfortable/large） | 3 天 | 无 | 间距/控件高度/字体联动调整 |
| 4 | CI 性能回归门禁 | 2 天 | 无 | 基准测试结果自动与基线对比 |
| 5 | ComponentSize 枚举迁移（token-backed） | 2 天 | 密度变体完成 | 枚举属性读取主题令牌 |
| 6 | 在线 API 文档（Dokka） | 3 天 | 无 | 文档站点可访问 |
| 7 | 独立模块 UI 消费者令牌对齐 | 2 天 | 无 | MermaidDiagram/MarkdownViewer/CodeBlock 全部走令牌 |
| 8 | 自定义 Detekt 规则拦截硬编码常量 | 2 天 | 无 | 新增 val X: Dp = <literal>.dp 被 CI 拦截 |

### 8.4 里程碑验收

```
P0 完成 ──→ 所有 Defaults 旧常量有弃用标注
            MermaidDiagram/Scaffold/Container 令牌化
            ThemeTokenizationStaticAuditTest 通过

P1 完成 ──→ 暗色模式 + 状态令牌测试通过
            Typography 8+ 级
            消费者迁移指南可用
            PaletteColorsSnapshot 完整

P2 完成 ──→ a11y 审计通过
            5+ i18n 语言
            密度变体可用
            在线文档可用
            CI 全门禁生效
```

---

*本报告基于 2026-06-27 对 Palette 仓库（commit: HEAD）的源码审计生成。*
