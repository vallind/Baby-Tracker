# 设计系统详细文档

> 最后更新：2026-08-08 · 对应版本：1.8.0
>
> 从 AGENTS.md 拆分，供需要深入了解设计系统时查阅。

## 令牌驱动架构

```
优先级模型：
  显式参数 > XxxDefaults > 组件令牌 > 核心语义令牌 > 可控回退值

三层令牌：
  designsystem/theme/AppTokens.kt           — 核心语义令牌（AppColors 39字段/Spacing/Elevation/Opacity/Motion/Shapes/自建 12 级 AppTypography/ControlSizeTokens/AppDensity 密度体系）
  designsystem/theme/AppComponentTokens.kt  — 组件令牌（33 种：Button/Card/Input/Select/SelectionControl/Switch/Table/Dialog/Menu/Tag/Divider/Surface/SnackbarHost/Progress/Skeleton/Steps/Pagination/Slider/Rate/AppBar/Chip/Fab/BottomBar/ListItem/IconButton/Scaffold/BorderContainer/TimePicker/DatePicker/DateTimeCascade/Sheet/SegmentedControl/EmptyState；AppDensityTokens 为非组件令牌，不在计数内）
  designsystem/util/AppDefaults.kt          — 快照（非 Composable 环境下的默认值访问，已同步令牌结构）

Typography 自建 12 级：displayLarge/headlineLarge/headlineMedium/headlineSmall/titleLarge/titleMedium/titleSmall/bodyLarge/bodyMedium/bodySmall/labelMedium/labelSmall（禁止直接使用 M3 Typography，仅 theme 层桥接）

## 令牌设计约定（参照 shadcn/ui）

### 颜色：surface/foreground 配对
- 每个有 `containerColor` 的组件令牌必须有对应的 `contentColor`
- 成对赋值：`containerColor = c.surface` + `contentColor = c.onSurface`
- 如 ButtonTokens：`contentColor = c.onPrimary`（容器色由 M3 内部处理）

### 圆角：从 AppShapes 基准派生
- AppShapes 提供 6 级 + `radiusScale: Float` 全局缩放（参照 shadcn `--radius`）
- 组件 cornerRadius 从 shapes 基准计算，必须标注对应关系
- 示例：`cornerRadius: Dp = 24.dp  // shapes.medium * 2`
- 新增组件圆角通过 `shapes.scaled(shapes.large)` 计算，不硬编码
```

## derive() 模式

```kotlin
AppColors.derive(primary) → HSL 色相位移，自动重算所有 39 个字段（✅ 已实现）
AppComponentTokens.default(colors) → 从 AppColors 自动派生组件令牌颜色（✅ 已实现）
tokens.derive { field = value } → 部分覆盖语法糖（⏳ TODO，TT-032，尚未实现，需逐个字段手动覆盖）
```

## 组件用法速查

```kotlin
AppCard { Text("内容") }                          // 替代 Card + shadow + shape + CardDefaults 样板
AppTopBar(title = "标题", onBack = { ... })       // 替代 CenterAlignedTopAppBar
AppButton(label = "保存", onClick = { ... }, variant = ButtonVariant.Secondary) // 统一按钮（Primary/Secondary/Text 变体枚举）
AppDivider(thickness = 0.5.dp)                      // 分割线（替代 HorizontalDivider）
AppSurface { ... }                                  // 表面容器（替代 M3 Surface，可选 color/shape）
AppSnackbarHost(hostState)                          // 全局提示宿主（替代 M3 SnackbarHost）
AppDialog(show, title, content = { ... }, confirmEnabled, onConfirm, onDismiss) // 表单/自定义内容对话框
AppConfirmDialog(show, onConfirm, onDismiss)       // 确认类对话框
AppFormSheet(show, onSave, ...)                    // 表单底部弹层
AppBottomSheet(show = true, onDismiss = { ... })   // 通用底部弹层
AppActionSheet(show, actions = listOf("编辑" to {...})) // 操作选择弹层
AppInput(value, onValueChange, label)               // 替代 OutlinedTextField
AppIconButton(icon = Icons.Default.Add, onClick = { ... }) // 图标按钮（支持 tint）
AppRadioButton(selected, onClick)                  // 单选按钮
AppSwitch(checked, onCheckedChange)                // 开关
AppSlider(value, onValueChange)                    // 滑块
AppCircularProgress()                              // 加载指示器
AppChip / AppFilterChip(label, selected, onClick)  // 标签/筛选胶囊
AppListItem(emoji, label, onClick)                 // 列表项
AppMarkdownText(markdown = content)                // 安全渲染文本 Markdown，不加载远程图片或执行 HTML
DateTimeCascadeDialog(...)                          // 级联日期时间选择（含 dateOnly 模式）
RecordCard(record, ...)                             // 记录卡片（左滑删除 + Snackbar 撤销）
snackbar.showUndo(onUndo = { repo.update(r) })     // 替代 showSnackbar + ActionPerformed 样板（撤销=恢复软删除行）
```

> 完整组件/令牌清单与对应关系以代码为准（`designsystem/components/`、`AppComponentTokens.kt`）。

## 密度变体（AppDensity）

页面级密度三档，全项目零组件迁移即可生效：

| 档位 | key | label | spacingScale | controlHeightDelta（仅 medium 档） |
|---|---|---|---|---|
| 紧凑 | `compact` | 紧凑 | 0.85f | -8dp（48 → 40dp） |
| 舒适（默认） | `comfortable` | 舒适 | 1.0f | 0dp（48dp） |
| 宽松 | `large` | 宽松 | 1.15f | +8dp（48 → 56dp） |

- 定义于 `designsystem/theme/AppTokens.kt`：`AppDensity` 枚举（含 `fromKey` 解析，未知 key 回退舒适档）+ `AppDensity.tokens` 扩展属性映射三档数值；`AppDensityTokens` 是**非组件令牌**（间距缩放系数 + 控件高度调整量），不进入 `AppComponentTokens` 聚合，不影响组件令牌 33 个聚合字段计数。
- `LocalAppDensity`（`staticCompositionLocalOf`，默认舒适档）暴露当前密度，`BabyTrackerTheme(density = ...)` 读取后统一缩放。
- **缩放机制（零迁移）**：
  1. `AppSpacing().scaled(spacingScale)` — 全量间距令牌按系数缩放（`none` 不缩放），注入 `LocalAppSpacing`；
  2. `AppControlTokens.densityAdjusted(density)` — 仅调整 medium 档控件高度（±8dp），注入 `LocalAppControl`；
  3. 全部组件自动生效，无需逐组件改动；组件若需按密度区分，用枚举参数（如 `density: AppDensity`），不新增函数。
- **存储与切换**：`AppSettings.appearance.density`（DataStore 持久化）；`DensityController`（`designsystem/theme/DensityController.kt`，仿 ThemeController：订阅设置流 + `mutableStateOf` + `switchDensity`）以 `single` 注册进 `core/di/Modules.kt`。
- **设置页入口**：`feature/settings/SettingsMenuScreen.kt`（使用偏好）「界面密度」（📐）→ `DensityPickerSheet`（`SettingsScreen.kt` 内，AppBottomSheet 三选一卡片，label 走 AppStrings.densityLabel）。
- 密度相关回归：`DensityTokensTest`（缩放/三档数值/fromKey/densityAdjusted 4 项）。

## Logic 模式（纯 Kotlin，可 JVM 单测）

| Logic 类 | 功能 |
|---|---|
| `ButtonLogic(scope, debounceMs)` | isPressed/isLoading/防抖 |
| `FormLogic(scope, initial, validator)` | fields/errors/touched/submitting |
| `TableLogic(scope, data)` | sorting/selection/pagination |

## 判断标准

新增组件到 designsystem/components/ 之前，必须先通过以下检查。

### 计数规则

- 按「视觉形态」计数，不是函数调用次数，也不是复制粘贴次数
- 同一视觉形态在 UI 稿里出现 ≥ 2 处即达标
- 跨功能重复算 — 不同页面/模块，只要视觉形态一样就计数
- 当前只有 1 处 → 先内联实现，等第 2 次出现再抽象
- 禁止"预判性抽象" — 预期会复用但当前只有 1 处，也先不抽象

### 原则一：两次规则

同一个 UI 模式在项目中出现 ≥ 2 处，才考虑抽象。
- 反例：某个页面独有的空态插图，只出现一次，不需要抽象

### 原则二：必须封装设计令牌

组件的价值在于统一设计令牌（颜色/圆角/间距/elevation），而非单纯减少代码行数。
- 需要封装令牌 → 值得新增
- 只是把几个原生组件包一层、没有令牌封装 → 不新增，直接用 Compose 组合即可
- 反例：一个只含 Row + Text + Icon 的简单列表项，没有特殊令牌，不需要抽象

### 原则三：不是一次性 UI

预判该 UI 模式在未来其他页面也会用到。
- 是一次性 UI → 直接用 Compose 原生或内联实现
- 反例：设置页某个特殊的开关项，其他页面不会用，不需要抽象

### 决策检查清单

- [ ] 这个视觉形态在 UI 稿里已出现 ≥ 2 处？（按视觉形态计数，跨功能重复算）
- [ ] 它需要封装设计令牌（颜色/圆角/间距）？
- [ ] 它不是一次性 UI，预判会在其他地方复用？
- [ ] 如果以上有任何一项为"否"，是否可以考虑不新增，先内联实现？

## 新增组件指引

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

## 详细报告

参见 `docs/Palette组件库设计深度分析报告.md`。
