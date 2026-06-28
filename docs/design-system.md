# 设计系统详细文档

> 从 AGENTS.md 拆分，供需要深入了解设计系统时查阅。

## 令牌驱动架构

```
优先级模型：
  显式参数 > XxxDefaults > 组件令牌 > 核心语义令牌 > 可控回退值

三层令牌：
  designsystem/theme/AppTokens.kt           — 核心语义令牌（AppColors 39字段/Spacing/Elevation/Opacity/Motion/Shapes/Typography/ControlSizeTokens）
  designsystem/theme/AppComponentTokens.kt  — 组件令牌（21 种：Button/Card/Input/Select/SelectionControl/Switch/Table/Dialog/Menu/Tag/Progress/Skeleton/Steps/Pagination/Slider/Rate + AppBar/Chip/Fab/BottomBar/ListItem）
  designsystem/util/AppDefaults.kt           — 快照（非 Composable 环境下的默认值访问，已同步令牌结构）
```

## derive() 模式

```kotlin
AppColors.derive(primary) → HSL 色相位移，自动重算所有 39 个字段
AppComponentTokens.default(colors) → 从 AppColors 自动派生组件令牌颜色
tokens.derive { field = value } → 部分覆盖语法糖
```

## 组件用法速查

```kotlin
AppCard { Text("内容") }                          // 替代 Card + shadow + shape + CardDefaults 样板
AppTopBar(title = "标题", onBack = { ... })       // 替代 CenterAlignedTopAppBar
PrimaryButton(onClick = { ... }, label = "保存")   // 主按钮
PaiButton("保存", onClick = { ... })               // 简化工厂
AppConfirmDialog(show, onConfirm, onDismiss)       // 替代 AlertDialog 样板
snackbar.showUndo(onUndo = { repo.insert(r) })    // 替代 showSnackbar + ActionPerformed 样板
```

## Logic 模式（纯 Kotlin，可 JVM 单测）

| Logic 类 | 功能 |
|---|---|
| `ButtonLogic(scope, debounceMs)` | isPressed/isLoading/防抖 |
| `FormLogic(scope, initial, validator)` | fields/errors/touched/submitting |
| `TableLogic(scope, data)` | sorting/selection/pagination |

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
