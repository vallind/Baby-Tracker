# UI 体系约定（Elyon）

> 最后更新：2026-08-11 · 旧自建设计系统（`com.babytracker.designsystem`）已整体删除

应用 UI 基座为 **Elyon**（`vide/elegant` 复合构建，rootProject 名 `elyon`），
模块：`elyon-core` / `elyon-ui` / `elyon-effects` / `elyon-blur` / `elyon-nav`。

## 主题

根组件：`core/ui/ElyonAppTheme.kt` 的 `BabyTrackerElyonTheme(themeName)`。

- 主题名 → Elyon `ThemeController` 参数的映射在 `core/ui/ElyonThemeResolver.kt`（纯函数，有单测）；
- 固定色板定义在 `core/ui/BabyTrackerPalettes.kt`（旧 6 套主题配色直接映射 Elyon `Colors`，不走 Monet 动态色板）；
- 颜色：`ElyonTheme.colorScheme.*`；排版：`ElyonTheme.textStyles.*`；
- 暗色判定只读主题名（`ElyonThemeResolver.isDark`），不读系统暗色；
- 控制器：`core/ui/ThemeController.kt` / `core/ui/DensityController.kt`（Koin `single` 注册）。

## 组件

基础组件直接用 `io.elyon.kmp.basic.*`（Button/TextButton/Card/Surface/TextField/TopAppBar/
Scaffold/Switch/RadioButton/Checkbox/Slider/ProgressIndicator/SnackbarHost/IconButton/
HorizontalDivider/FloatingActionButton/NavigationBar…），弹层用 `io.elyon.kmp.overlay.*`
（OverlayDialog/OverlayBottomSheet），导航用 `io.elyon.kmp.nav.*`。

应用级组件（Elyon 缺失且跨功能重复）放 `core/ui/components`：

| 组件 | 说明 |
|---|---|
| `recordcard/RecordCard` | 滑动删除 + 点击编辑记录卡片（M3 SwipeToDismissBox 为 TODO） |
| `dialog/AppDialog`、`AppConfirmDialog`、`AppFormSheet`、`AppActionSheet` | Elyon Overlay 封装 |
| `sheet/AppBottomSheet` | OverlayBottomSheet 封装 |
| `input/AppInput` | 支持 error 态/支持文本（Elyon TextField 暂无 error 态，内部 M3 TODO） |
| `timepicker/*`、`datetimecascade/*` | 滚轮时间/级联日期时间（纯 Elyon 原语） |
| `snackbar/AppSnackbar`、`AppSnackbarHost` | Elyon Snackbar 封装，`showUndo` 模式 |
| `EmptyState`、`SegmentedControl`、`AppCardGroup`、`Section`、`AppChip`/`AppFilterChip`、`AppMarkdownText`、`AppSwitch`/`AppRadioButton`/`AppCheckbox`、`AppSlider`、`AppFAB`、`AppProgress` | 应用级组合组件 |

间距/圆角常量：`core/ui/AppDimens.kt`（`AppSpacing`/`AppShapes`，纯 dp）。

## 毛玻璃与导航

- 统一修饰符：`core/ui/components/BlurModifiers.kt` 的 `Modifier.appBlur()`（API 33+ 且有
  Scaffold backdrop 时叠加 `textureBlur`，否则原样返回）。
- 已铺开毛玻璃的组件：底部导航（BottomNavBar）、顶栏（AppTopBar，半透明玻璃）、
  FAB（AppFAB）、Snackbar（AppSnackbarHost）；底部弹层与对话框为半透明玻璃质感
  （独立弹窗无法采样页面，不做真实 backdrop 模糊）。
- 顶栏真毛玻璃：列表打头的屏幕用 `AppScaffold(edgeToEdgeTop = true)` 让内容延伸到
  顶栏下（当前：健康档案），其余顶栏下有固定筛选头的屏幕保持半透明。
- 不适用毛玻璃的组件：卡片/列表项/输入框/开关/滑块/图表等页面内容组件（背后没有
  可模糊的内容），以及下拉菜单/弹窗（独立窗口）。
- 导航根：`navigation/AppNavigation.kt`（elyon-nav `NavDisplay` + `Route` + `Navigator`），
  路由注册表 `AppRouteGraph` 由 `RouteGraphTest` 守护。

## 禁用项

- 禁止新增 `com.babytracker.designsystem` 包或自建令牌层（`DesignSystemRetirementTest` 守护）；
- 禁止直接用原生 M3 组件（存量 TODO 除外）；
- 禁止 `isSystemInDarkTheme()` 作为应用暗色来源。

## i18n

新增用户可见文本必须写入 `com.babytracker.i18n.AppStrings`，禁止硬编码中文。
