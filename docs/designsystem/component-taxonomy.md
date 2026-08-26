# 设计系统组件收敛蓝图

> 本文件取代原《组件库分类与规模规划》中的**数量目标**与**变体拆分式组件清单**。
> 配套文档：《组件差距分析》`component-gap-analysis.md`（现状盘点与 P0/P1/P2 路线）。
> 版本基线：designsystem v2.4.0 现状。

---

## 〇、本次修订的三条裁定

1. **组件多变体，禁止变体即组件。**
   一个交互域只允许一个核心组件；一切视觉/形态/状态差异收进该组件的参数轴（variant × size × state × content/slot）。凡是"某变体的专用组件"（AppFilledButton、AppOutlinedTextField、AppRatingBar、AppToast……）一律视为反模式，逐条收编（见第二节对照表）。
   变体需求的正确出口有且只有两个：**参数轴** 与 ***Defaults 预设工厂**（如 `AppButtonDefaults.danger()` 返回预设组，而不是新建 DangerButton 组件）。

2. **删除全部数量指标。**
   原规划的"建议数量 8~15 / 15~25"、"150~220 能力"、"100~150 核心 Composable"等数字目标全部作废。组件数量是能力覆盖的**结果**，不是目标。衡量标准改为每类一张**能力核对单**：页面需要的交互能力是否有一个组件能承接、是否有完整的状态轴——够了就停，不为凑数造组件。

3. **分类保留，交叉合法。**
   原 14 个类别作为**逻辑标签**继续使用（便于检索与规划对账），但不要求物理目录一一对应，也不要求每个组件唯一归属（Chip 同时属于 Selection 与 Display、Tooltip 同时属于 Overlay 与 Feedback 都是合法的）。物理结构维持现状：`theme / components/<域>/ / composites/ / foundation/ / hooks/ / i18n/`。

---

## 一、收敛公式

每个核心组件按同一骨架展开：

```text
<App核心组件>
├── variant    视觉语义轴（Primary/Tonal/Outline/Ghost/Danger …）
├── size       密度轴（Small/Medium/Large，映射 AppControlTokens）
├── state      状态轴（normal/pressed/disabled/loading/selected/error）
├── content    内容槽位（text/icon/leading/trailing/header/footer/custom）
└── presets    *Defaults 工厂预设（语义组合的快捷出口，非平行组件）
```

**黑名单命名法**：以下后缀的独立组件永远不应该出现——
`Filled*` / `Outlined*` / `Ghost*` / `Text*Button` / `*Field(除领域专名)` / `Circular*` / `Linear*` / `*RatingBar` / `*Toast` / `Success*State` / `Warning*State` / `Info*State` / `*LoadingState`。

---

## 二、变体收编对照表（原规划条目 → 收敛归宿）

> 覆盖原规划全部清单条目，一条不漏。归宿栏即目标形态；"存量"指本仓库已有实现。

| 原规划条目 | 收敛归宿 |
|-----------|---------|
| AppFilledButton · AppTextButton · AppOutlinedButton · AppToggleButton | `AppButton(variant=…, selected=…)` ✅存量 |
| AppFloatingActionButton · AppExtendedFAB | `AppFAB(size=…, label 槽位)` ✅存量 |
| AppMenuButton · AppSplitButton | `AppButton` 的 `menu:` 槽位扩展（依赖 AppMenu 落地）🔜 |
| AppTextField · AppOutlinedTextField · AppFilledTextField | `AppInput` ✅存量 |
| AppNumberField · AppPhoneField · AppSearchField | `AppInput(type=number/phone/search)` 🧩扩轴 |
| AppPasswordField | `AppInput(type=password)` 🧩扩轴 |
| AppMultilineField · AppTextArea | `AppInput(minLines/maxLines)` 🧩扩轴 |
| AppAutocomplete · AppSearchableDropdown | `AppInput(suggestions: 槽位)` + `AppMenu` 🔜 |
| AppDateField · AppTimeField · AppDateTimeField | `AppDateTimeField(mode=date/time/datetime)` ✅存量 |
| AppRangeSlider | `AppSlider(range: Boolean)` 🧩扩轴 |
| AppFilePicker · AppMediaPicker · AppFileUpload | `AppFilePicker(source=pick/media/upload)` 🔜P2 |
| AppColorPicker | `AppColorDots`(轻量)✅存量；完整取色器无场景不设 ⛔ |
| AppChoiceChip · AppFilterChip · AppActionChip | `AppChip(mode=assist/choice/filter)` ✅存量 |
| AppToggle | `AppSwitch` ✅存量 |
| AppToggleGroup · AppSegmentedButton · AppTabBar · AppTab | `AppSegmentedControl(mode=single/multi, 页签复用)` ✅存量 |
| AppSelect · AppSelector · AppSelectionList · AppMultiSelect · AppDropdown | `AppPickerSheet(single/multiple)` ✅存量 + `AppDropdownMenu` 🔜P0 |
| AppRichText | `AppMarkdownText` ✅存量 |
| AppDescriptionList · AppKeyValue | `AppKeyValueRow` ✅存量 |
| AppStat · AppMetric | `AppStat`(值/单位/趋势/标签轴)；Hero/Metric/Summary 卡降级为 composites ✅存量 |
| AppList · AppLazyList · AppGrid · AppLazyGrid · AppDataView | 原生 Lazy 系列，不设包装 ⛔ |
| AppTable · AppDataTable | `AppDataTable(sort/filter 工具条=header 槽位)` 🔜P0 |
| AppSortBar · AppFilterBar · AppDataToolbar | 上者与列表容器的 `header/footer` 槽位，非独立组件 🔜P0 |
| AppCalendar · AppDatePicker · AppTimePicker | `AppDateTimeField` 弹层内部实现；独立日历视图无场景不设 ⛔ |
| AppCircularProgress · AppLinearProgress · AppProgressIndicator | `AppProgress(shape=linear/circular)` ✅存量(两函数即 shape 轴) |
| AppShimmer | `SkeletonLoader(shimmer=true)` 参数 ✅存量 |
| AppStepIndicator · AppStepper | `AppStepper(direction=horizontal/vertical)` 🔜P0 |
| AppCountdown | `AppCountdown` ✅存量(CountdownChip) |
| AppSwipeToDismiss · AppDismissGesture | 记录卡内建；第二消费者出现时抽 `Modifier.appSwipeToDismiss()` 🧩 |
| AppPullToRefresh · AppDraggable · AppReorderable · AppZoom · AppPager | 原生 pointerInput/Pager 即语言能力；仅 PullToRefresh 值得令牌化封装 🔜P1 |
| AppAnimatedVisibility · AppAnimatedContent · AppFade · AppScale · AppSlide · AppExpand · AppCollapse | `animate*AsState` + `LocalAppMotion` 曲线，不设包装 ⛔（审计守门时长） |
| AppSharedTransition | 出现首页→详情转场需求时评估 🔜P2 |
| AppAlertDialog | `AppDialog(variant=confirm/danger 预设)` 🧩存量收编 |
| AppModalBottomSheet · AppActionSheet · AppFormSheet · AppOptionPickerSheet · RecordDetailSheet · AppSideSheet | 目标形态 `AppSheet(kind=action/form/option/detail)`；存量经 Defaults 预设渐进收编 🧩 |
| AppPopup · AppPopover · AppContextMenu · AppDropdownMenu | `AppMenu(placement=dropdown/context/popup)` 🔜P0 |
| AppTooltip · AppTip | `AppTooltip(placement 轴)` 🔜P2 |
| AppToast · AppSnackbar · AppInlineMessage | `AppSnackbar(+Host)` 单轨收口 ✅存量；Toast 不设 ⛔ |
| AppBanner · AppAlert | `AppBanner(severity=info/success/warning/error)` ✅存量 |
| AppSuccessState · AppWarningState · AppInfoState · AppLoadingState · AppStatus | 四态规范内解决：Loading→Skeleton、Empty→EmptyState、其余 severity 并入 `AppErrorState(status=…)` 轴 ⛔新组件 |
| AppErrorState · AppEmptyState | 保留两个公共状态件（错误/空语义不同），只扩枚举不改数量 ✅存量 |
| AppBreadcrumb · AppNavigationRail · AppNavigationDrawer · AppBackButton · AppPageIndicator · AppNavigationTransition | 手机单栏信息架构不需要 ⛔ |
| AppBox · AppStack · AppSpacer · AppAspectRatio · AppFlowColumn · AppContainer · AppTwoPane · AppSplitLayout · AppAdaptiveLayout | Compose 语言原语/无场景 ⛔（FlowRow 出现需求时直接用 foundation 原生）|
| AppText · AppIcon | 原生 Text/Icon + `LocalAppTypography` 已是事实标准 ⛔ |
| AppContentAlpha | 弱化色通道已在 AppColors 内 ⛔ |
| AppRatingBar | `AppRate` ✅存量 |
| AppColorSwatch | `AppColorDots` ✅存量 |
| AppCodeInput · AppOTPInput · AppPinInput | `AppPinInput(length, obscure)` 单组件吸收三者 🔜P1 |
| AppImageViewer · AppQRCode | 各一个组件 🔜P2（QR 先做依赖评估） |

---

## 三、收敛后的分类目录（逻辑标签视角）

> 状态标记：✅ 已存在 · 🔜 待建 · 🧩 存量收编/扩轴 · ⛔ 明确不设

### Foundation —— Token 家族，非组件层
AppTheme · AppColors(+AppColorScale/Gradients) · AppTypography(12 级字阶) · AppSpacing · AppShapes · AppElevation · AppMotion · AppComponentTokens(52 组件令牌组注册中心) · AppSurface ✅ · AppDivider ✅ · AppStrings(i18n) · AppDefaults 快照
🔜 唯一候选：AppIcons 注册表（图标换肤需要时）

### Layout
AppRow ✅ · AppColumn ✅ · AppScaffold ✅ · SubPageScaffold ✅ · BorderContainer ✅ · CenterVerticallyRow ✅ · AppTileGrid ✅(宫格即 Grid 实现) · AppSection 🧩(SectionHeader 并入 header/footer 槽位) · Surface ✅

### Navigation
AppTopBar ✅ · AppNavigationBar ✅(含 item 模型，兼任 BottomBar) · AppSegmentedControl ✅(兼顶部页签) · DateNavCapsule ✅(日期导航特色件)

### Button
AppButton ✅(variant×size×state×icon) · AppIconButton ✅ · AppFAB ✅(extended=label 槽位) · AppActionBar ✅ · 🔜 menu 槽位

### Input
AppInput ✅(type 轴待扩 search/password/number/multiline) · AppDateTimeField ✅(mode 轴) · AppSlider ✅(range 待扩) · AppChatInputBar ✅ · AppFilePicker 🔜P2

### Selection
AppSwitch ✅ · AppCheckbox ✅ · AppRadioButton ✅ · AppSegmentedControl ✅ · AppChip ✅(mode 轴) · AppOptionChipRow/AppChipCarouselRow ✅ · AppPickerSheet ✅(multi 待扩) · AppScoreSelector ✅ · AppDropdownMenu 🔜P0

### Display
AppCard ✅(三轴样板) · AppCardGroup ✅ · AppListItem ✅ · AppKeyValueRow ✅ · AppStat ✅(StatCell/QuickStatPill 收编于此) · AppTag ✅ · AppBadge ✅(dot/count/emoji 轴统一 EmojiBadge/BadgeIcon) · AppInitialAvatar ✅ · AppMiniChart ✅(line/bar=shape 轴) · AppChartContainer ✅ · AppMarkdownText ✅ · AppSummaryCard/AppMetricCard/AppHeroStatCard/AppInsightCard ✅(composites 组合层) · AppTimeline 🔜P1 · AppImage 🔜P2(Coil 封装收口)

### Feedback
AppSnackbar+Host ✅(唯一即时反馈通道) · AppBanner ✅(severity 轴) · EmptyState ✅ · AppErrorState ✅(status 轴待扩 warning/info) · SkeletonLoader ✅(shimmer 参数) · AppTypingIndicator ✅ · Haptic hooks ✅

### Overlay
AppDialog ✅(confirm 预设待收编 🧩) · AppSheet ✅(action/form/option/detail 预设 🧩) · AppActionSheet ✅ · AppConfirmDialog ✅ · AppMenu 🔜P0 · AppTooltip 🔜P2

### Data
AppDataTable 🔜P0(TableLogic+TableTokens 就绪) · AppPagination 🔜P0(Tokens 就绪) · sort/filter 工具条=槽位 🔜P0 · 其余走原生 Lazy*

### Progress
AppProgress ✅(linear/circular=shape) · AppStepper 🔜P0(StepsTokens 就绪) · SkeletonLoader ✅ · AppCountdown ✅

### Gesture
Modifier.longPressDeletable ✅ · 滑动删除(RecordCard 内建 🧩待抽) · AppPullToRefresh 🔜P1 · Reorderable 🔜按需

### Animation
AnimatedListItem ✅ · animateNumber ✅ · LocalAppMotion 令牌+MotionHardcodedDuration 审计 ✅ · 不设动效包装组件 ⛔

### Specialized
AppRate ✅ · AppScoreSelector ✅ · AppColorDots ✅ · AppDateTimeField 级联弹窗 ✅ · AppTimerRow+TimerState ✅ · BabyIllustration ✅ · AppSettingItem 家族 ✅ · AppPinInput 🔜P1 · AppQRCode 🔜P2 · AppImageViewer 🔜P2

---

## 四、验收方式（取代数量指标）

每类不再回答"有几个组件"，只回答三个问题：

1. **能力覆盖**：本类能力核对单上的每项交互，是否恰好有一个组件承接？（既无缺口、也无重复建设）
2. **状态完整**：该组件的 state 轴是否覆盖 normal/disabled/loading(/selected/error)，空态/错态是否走四态规范？
3. **零黑名单**：是否存在第二节黑名单后缀的新组件？出现即打回。

配套守门（既有审计规则自动执行）：`FeatureLayerGenericCard`（业务层禁私造卡片）、`MotionHardcodedDuration`（动画时长走令牌）、`ComponentTokensMissingRegistration`（颜色组必须注册）、`DefaultsMissingLocalAppComponentTokens` / `DefaultsHardcodedColor`（预设工厂禁直读核心令牌/硬编码色）。

---

## 五、执行衔接

- 待建优先级与业务受益方见《组件差距分析》第七节路线图（P0：Menu → DataTable → Stepper → Pagination → Select；P1：Search 输入轴、Timeline、PullToRefresh、PinInput；P2：其余）。
- 存量收编（🧩 项）一律走"加预设/加别名/扩枚举"的兼容路径，不做一次性破坏性重命名；命名前缀债务清单见差距分析 6.1 节。
- 本蓝图与差距分析冲突时，以差距分析的现状盘点为准；两者共同作为 AGENTS.md 第 4 节设计系统约定的补充细则。
