# 无障碍语义基线（a11y-baseline）

> 对应版本：随 P2 发布批次 · 更新日期：2026-08-08

## 原则

1. **M3 内置锚点**：M3 包装组件（AppButton / AppSwitch / AppCheckbox / AppRadioButton / AppSlider / AppFilterChip / AppLinearProgress / AppCircularProgress / AppDialog / AppInput 等）依赖 M3 内置语义作为基线锚点，不重复叠加，不改写 role。
2. **自定义必须显式**：自定义可交互组件（SegmentedControl / RecordCard / AppRate / TimePickerLogic 滚轮 / DateTimeCascade 日历）必须显式提供 `role` / `selected` / `stateDescription` / `customActions`。
3. **装饰隔离**：纯装饰内容（BabyIllustration / BadgeIcon / EmptyState emoji / 分割线 / 骨架屏 / 背景删除图标）必须 `clearAndSetSemantics` 隔离，避免常驻无障碍树。
4. **防重复朗读**：文本标签与 icon 不得重复声明语义——label 变体（AppFAB / BottomNavBar / AppButton 带 icon）的 icon `contentDescription = null`，文本已由组件朗读。

## 组件承诺表

### 可交互组件（M3 包装）

| 组件 | 语义承诺 |
|---|---|
| AppButton | M3 内置（Button/role=Button，enabled 自动映射）；带 icon 时 icon `contentDescription = null`（防与文本双读） |
| AppIconButton | 依赖调用方传 `contentDescription`；漏传 = 无标签（组件不兜底） |
| AppInput | M3 内置（OutlinedTextField，label/error 自动朗读）；密码可见性切换按钮 CD 走 `AppStrings.showPassword/hidePassword` |
| AppSwitch | M3 内置（Switch，选中状态自动朗读） |
| AppCheckbox | M3 内置（Checkbox，选中状态自动朗读） |
| AppRadioButton | M3 内置（RadioButton，`selected` 自动朗读） |
| AppSlider | M3 内置（Slider，value/range 进度语义自动朗读） |
| AppLabeledSlider | M3 内置 + 显式 `contentDescription = label`（AppSlider.kt:91，label 合并进滑块节点，一次朗读） |
| AppFilterChip | M3 内置（FilterChip，选中状态自动朗读） |
| AppTopBar | M3 内置（CenterAlignedTopAppBar）；返回按钮 CD = `AppStrings.back` |

### 可交互组件（自定义，显式承诺）

| 组件 | 语义承诺 |
|---|---|
| SegmentedControl | `Role.Tab` + `selected` + `stateDescription = label`（SegmentedControl.kt:70-74） |
| RecordCard | `customActions` 暴露「删除」自定义动作（label = AppStrings.delete，RecordCard.kt:112-117）；背景删除 icon `clearAndSetSemantics` 隔离 |
| AppRate | 容器 `contentDescription = "评分 %d，共 %d 星"`（AppRate.kt:38）；只读星 icon `clearAndSetSemantics` 单次朗读 |
| TimePickerLogic 滚轮 | 选中项 `selected` 语义（TimePickerLogic.kt:162，与视觉高亮同源） |
| DateTimeCascade 日历 | 日期格 `Role.Button` + `selected`（DateTimeCascade.kt:369-370） |
| AppConfirmDialog / AppDialog / AppActionSheet / AppFormSheet / AppBottomSheet | M3 AlertDialog/BottomSheet 内置 focus trap + 标题朗读；按钮语义随内部 AppButton |
| BottomNavBar | M3 内置（NavigationBarItem，label 文本朗读 + selected 状态）；icon `contentDescription = null`（BottomNav.kt:71/74） |
| AppFAB | label 变体：ExtendedFAB 文本朗读，icon CD = null（Fab.kt:39）；紧凑变体依赖调用方 `contentDescription`（Fab.kt:56） |
| AppSnackbar / AppSnackbarHost | M3 内置（Snackbar 文本 + actionLabel 朗读）；actionLabel 走 AppStrings.undo |
| SectionHeader / AppListItem | 文本自动朗读；可选 onClick 走 `clickable` 触达语义（Section.kt:46/82） |
| TimePickerDialog / DateTimeCascadeDialog | M3 AlertDialog 内置 focus trap；内部滚轮/日历语义见上 |

### 装饰组件（必须隔离）

| 组件 | 语义承诺 |
|---|---|
| BabyIllustration | `clearAndSetSemantics` 隔离（BabyIllustration.kt:38），不参与无障碍树 |
| BadgeIcon | `clearAndSetSemantics` 隔离（BadgeIcon.kt:47），角标计数不朗读 |
| EmptyState emoji | `clearAndSetSemantics` 隔离（EmptyState.kt:44），title/subtitle 保持可朗读 |
| AppDivider | 无语义节点（纯视觉分隔） |
| SkeletonLoader | 无文本、无点击，不产生语义节点 |
| CountdownChip | 文本自动朗读（纯展示，无点击） |
| AppCard / AppCardGroup / AppSurface / AppScaffold | 容器本身无语义承诺，内容逐节点朗读；AppScaffold 自动读 contentWindowInsets |
| AppChip / AppTag | 文本自动朗读（展示型） |
| AppMarkdownText | 文本自动朗读 |
| AnimatedListItem / AppLinearProgress / AppCircularProgress | 无额外语义承诺（动画不改读屏；进度条 M3 内置 indeterminate 语义） |

## 审计

- `A11ySemanticsAuditTest`（静态断言）守护自定义可交互组件语义：SegmentedControl（Role.Tab/selected）、RecordCard（customActions）、AppRate/AppLabeledSlider（contentDescription）、TimePickerLogic/DateTimeCascade（selected）、Fab/BottomNav（contentDescription = null）。
- `ThemeTokenizationStaticAuditTest` 守护令牌合规（无硬编码颜色值）。
- 原则 4（防重复朗读）由代码审查 + 静态测试共同守护；新增 label+icon 组合组件时必须 icon CD = null。
