# 设计系统组件库差距分析

> 对照对象：《组件库分类与规模规划》原始版
> 盘点基线：本仓库 `designsystem/` 现状（versionName 2.4.0 / versionCode 48）
> 生成日期：2026-08-26 · 方法：全量扫描 143 个 designsystem 文件 + AppComponentTokens 消费对账 + feature 层盘点
> 关联文档：收敛蓝图《component-taxonomy.md》（组件多变体裁定 · 变体收编对照表 · 验收方式）；本文件负责现状盘点与路线图，数量相关表述以蓝图的"去数量指标"裁定为准。

---

## 一、结论摘要

1. **规划与仓库方向一致，不是转向，而是补齐。** 规划第十七节的核心原则（三轴模型、Token 驱动、Variant/State 收敛、业务组件下放）已经是本仓库的既定约定，且有三条审计规则守门（`FeatureLayerGenericCard`、`MotionHardcodedDuration`、`ComponentTokensMissingRegistration`）。`AppButton`（variant × size × loading/selected）与 `AppCard`（视觉轴 × 结构槽位 × 交互轴）就是按该思想落地的样板。
2. **现状体量：** 约 85 个公共 UI Composable + 8 个 hooks + **52 个组件令牌组**（全部在 `AppComponentTokens.kt` 注册）。规划 14 类中 **10 类已有实质覆盖**。
3. **真实缺口有精确坐标：** 对账发现恰好 **5 组令牌注册后从未被任何组件消费**——`.select` / `.table` / `.menu` / `.steps` / `.pagination`。这就是"令牌先行"留下的建设清单：Select/Dropdown、DataTable、Menu、Stepper、Pagination 五个组件。
4. **规划清单中约四分之一条目属于"不该建"：** Compose 原生能力包装（AppBox/AppSpacer/AppPager…）、变体拆分组件（AppFilledTextField/AppOutlinedTextField…）、状态拆分组件（AppSuccessState/AppWarningState…，违反仓库四态规范）、双轨反馈（Toast 与 Snackbar 并存）。逐条裁决见第五节。
5. **数量对账：** 补完 P0+P1 后核心 Composable 约 100~115 个，正落在规划"100~150 核心 + 大量 Variant 能力"的目标区间内。

---

## 二、总体覆盖对照表

| # | 规划分类 | 覆盖度 | 现有（约） | 缺口性质 |
|---|---------|:-----:|:------:|---------|
| 1 | Foundation 基础 | ★★★★★ | 11 | 仅缺 AppIcons 图标注册表 |
| 2 | Layout 布局 | ★★★★☆ | 7 | 大半规划项为 Compose 原生能力，不必建 |
| 3 | Navigation 导航 | ★★★☆☆ | 4 | 缺 Tab/Rail/Drawer；多数对手机单栏 App 非必需 |
| 4 | Button 操作 | ★★★★★ | 4 | 三轴模型已达标；仅缺 MenuButton（依赖 Menu）|
| 5 | Input 输入 | ★★★☆☆ | 6 | 缺 SearchField/Autocomplete/FilePicker 等 |
| 6 | Selection 选择 | ★★★☆☆ | 9 | 缺 DropdownMenu（令牌已备）/MultiSelect/ToggleGroup |
| 7 | Display 展示 | ★★★★☆ | 20 | 缺 Timeline/Image 异步图封装 |
| 8 | Feedback 反馈 | ★★★★☆ | 7 | 四态规范已固化；缺 Tooltip |
| 9 | Overlay 覆盖层 | ★★★★☆ | 9 | 缺 Menu/DropdownMenu（令牌已备）、Popover |
| 10 | Data 数据 | ★★☆☆☆ | 3 | **最大缺口**：Table/Pagination 令牌已备未建，Timeline 全缺 |
| 11 | Progress 进度 | ★★★★☆ | 5 | 缺 Stepper（令牌已备）；Shimmer 已并入 Skeleton 无需独立件 |
| 12 | Gesture 手势 | ★★☆☆☆ | 1 | 滑动删除已内嵌 RecordCard；PullToRefresh/Reorder 待抽 |
| 13 | Animation 动画 | ★★★☆☆ | 2 | 令牌+审计已守门；包装器类条目不建议建 |
| 14 | Specialized 专用 | ★★★☆☆ | 6 | 缺 PinInput/OTP、QRCode、ImageViewer/MediaPicker |

---

## 三、逐类明细

图例：✅ 已有 · 🟡 有替代实现 · ❌ 真实缺口 · ⛔ 建议不建（附理由）

### 1. Foundation 基础 —— 基本完成

| 规划项 | 状态 | 说明 |
|--------|:---:|------|
| AppTheme / AppColorScheme | ✅ | `AppTheme` 枚举 + `BabyTrackerTheme` + M3 桥接（Theme.kt，唯一事实来源） |
| AppTypography | ✅ | `AppTypography` 12 级字阶 + `LocalAppTypography`（AppTokens.kt L224） |
| AppSpacing / AppShapes / AppElevation / AppMotion | ✅ | `LocalAppSpacing` / `LocalAppShapes` / `LocalAppElevation` / `LocalAppMotion` 全齐 |
| AppSurface | ✅ | components/surface |
| AppDivider | ✅ | divider/ |
| AppColorScheme 色板 | ✅ | 另有超出规划的 `AppColorScale`、`Gradients` |
| AppText / AppIcon | 🟡 | 不建：原生 `Text`/`Icon` + `LocalAppTypography` 已是事实标准（见裁决 5-4） |
| AppContentAlpha | ⛔ | 用场景极少，`AppColors` 系列已有弱化色通道 |
| AppIcons 图标注册表 | ❌ | 各处散用 material-icons；若要做图标换肤/收敛可建，非紧迫 |

### 2. Layout 布局 —— 够用，克制扩张

✅ AppRow、AppColumn、AppSurface、BorderContainer、CenterVerticallyRow、AppScaffold、SubPageScaffold、SectionHeader
🟡 AppSection → SectionHeader + AppCard 组合即可；AppGrid → AppTileGrid（已覆盖本 App 宫格场景）
⛔ AppBox/AppStack/AppSpacer/AppAspectRatio：Compose 的 Box/Spacer/Modifier.aspectRatio 是语言原语，包装层零增益（Flutter 才需要）；AppFlowRow 唯一用到时再建（foundation 有现成 FlowRow 可直接引）
❌ AppAdaptiveLayout/TwoPane/SplitLayout：手机竖屏单栏 App，无使用场景，不建

### 3. Navigation 导航

✅ AppTopBar、AppNavigationBar（含 item 数据模型）、SegmentedControl（可承担顶部页签）
🟡 AppBottomBar → AppNavigationBar 即是；AppBackButton → TopBar 内置；AppNavigationItem → 已含于 AppNavigationBar
🟡 本仓特色超出规划：DateNavCapsule（日期导航胶囊，home/stats 核心交互件）
❌ AppTabBar（独立于 SegmentedControl 前）、AppPageIndicator：等出现横向 Pager 场景再议
⛔ AppBreadcrumb/AppNavigationRail/AppNavigationDrawer：手机单栏信息架构不需要

### 4. Button 操作 —— 规划思想的样板间

✅ AppButton（variant：Primary/Tonal/Outline/Ghost/Danger × size：S/M/L × state：normal/loading/disabled/selected + icon 槽位）——与规划第十七节的树完全同构
✅ AppIconButton、AppFAB、AppActionBar（批量操作条）
🟡 规划中的 Link variant 未提供（App 内无超链接语义场景）；ToggleButton → `AppButton(selected=true)` 覆盖
⛔ AppTextButton/AppOutlinedButton/AppFilledButton 等独立组件：规划自己反对的模式，仓库审计也不允许走回头路
❌ AppSplitButton/AppMenuButton/AppButtonGroup：依赖 Menu 组件落地后顺手评估（P1）

### 5. Input 输入

✅ AppInput（统一输入框）、AppDateTimeField + DateTimeCascadeDialog（日期时间级联，本仓最强专用件）、TimePickerDialog、QuickTimeChipRow、AppSlider/AppLabeledSlider、AppChatInputBar
🟡 Number/Phone/Password/Multiline → 走 AppInput 的 `keyboardType` / `visualTransformation` / 行数参数，不新建四个组件；TextArea 同理
❌ AppSearchField（home/timeline 搜索入口需要）、Autocomplete/SearchableDropdown（配合 Menu 落地）、FilePicker/MediaPicker（健康记录照片、头像上传时建）
⛔ RangeSlider：当前无取值范围场景

### 6. Selection 选择

✅ AppSwitch、AppCheckbox、AppRadioButton、SegmentedControl、AppFilterChip、AppOptionChipRow、AppChipCarouselRow、AppScoreSelector、AppSettingChoiceItem
🟡 AppSelect → AppOptionPickerSheet 已覆盖"底部弹层单选"；AppToggle → AppSwitch
❌ **AppDropdownMenu（SelectTokens 已注册未消费！）**、MultiSelect（多选弹层，vaccination 打针多选等潜在场景）、ToggleGroup（可由 SegmentedControl 兼任，暂缓）

### 7. Display 展示 —— 数量最多的已覆盖类别

✅ AppTag（5 色变体）、AppChip/AppEmojiBadge/BadgeIcon、AppInitialAvatar、AppCard（三轴）、AppListItem、AppKeyValueRow、StatCell、QuickStatPill、AppSummaryCard、AppHeroStatCard、AppMetricCard(+MetricTrendLabel)、AppInsightCard、MiniBarChart/MiniLineChart、AppChartContainer、AppMarkdownText、AppCategoryStrip、AppTileGrid、AppChatBubble、AppCollapsedHeader、BabyIllustration
🟡 AppRichText → AppMarkdownText；AppDescriptionList/AppStat/AppMetric/AppKeyValue → KeyValueRow + StatCell 家族
❌ AppTimeline（timeline/home"最近记录"流可复用）、异步 AppImage 封装（Coil 已在依赖里，出现第二处图片加载时收口）

### 8. Feedback 反馈 —— 四态规范已固化

✅ AppSnackbar + AppSnackbarHost、AppInlineBanner（severity 轴）、EmptyState、AppErrorState（status 轴）、SkeletonLoader（shimmer 参数已在 SkeletonTokens 中）、AppTypingIndicator、rememberHaptic
🟡 AppAlert → AppConfirmDialog（归 Overlay 使用）；AppLoadingState → 规范即 Skeleton
⛔ AppSuccessState/AppWarningState/AppInfoState/AppStatus：**违反仓库四态规范**（禁止新增私有 `*State`）。如需成功/警告空态，扩展现有 EmptyState 或 AppErrorState 的 status 枚举，而不是新组件
⛔ AppToast：与 Snackbar 双轨没有收益， Snackbar 收口
❌ AppTooltip（P2，长按气泡场景出现再建）

### 9. Overlay 覆盖层

✅ AppDialog、AppConfirmDialog、AppActionSheet、AppBottomSheet、AppFormSheet、AppOptionPickerSheet、RecordDetailSheet、TimePickerDialog、DateTimeCascadeDialog
❌ **AppDropdownMenu/AppMenu（MenuTokens 已注册未消费！）**、Popover（依附 Menu 能力）
⛔ AppSideSheet/AppContextMenu：手机场景优先级极低

### 10. Data 数据 —— 最大缺口区

✅ AppKeyValueRow（结构化键值展示）
🟡 List/LazyList/LazyGrid → Compose 原生 Lazy* 即语言能力，不建包装；FilterBar → AppFilterChip + AppChipCarouselRow 组合可用；Calendar/DatePicker → 内嵌于 DateTimeCascade，未独立成件（独立日历视图暂无需求）
❌ **AppTable/AppDataTable（TableLogic hook + TableTokens 双双就绪，只差 UI 层！）** —— stats/health 数值对比表是直接受益方
❌ **AppPagination（PaginationTokens 就绪）** —— timeline/message 长列表
❌ AppTimeline、AppDataToolbar/SortBar（出现排序需求时随 Table 一起设计）

### 11. Progress 进度

✅ AppLinearProgress、AppCircularProgress、SkeletonLoader、CountdownChip、AppTimerRow + rememberTimerState/TimerTickEffect
🟡 AppShimmer → SkeletonTokens 已含 shimmer 色/时长参数，无需独立组件；AppCountdown → CountdownChip
❌ **AppStepper/AppStepIndicator（StepsTokens 已注册未消费！）** —— development 发育评估的多步流程是最自然的落点

### 12. Gesture 手势

🟡 SwipeToDismiss → 已实现在 RecordCard 内部（滑动删除），暂无第二个消费者故不抽取；长按手势 → `Modifier.longPressDeletable`
❌ PullToRefresh（timeline 手动刷新体验升级时可建，Compose material3 有原生 PullToRefresh 可包一层令牌化皮肤）
⛔ AppPager/AppZoom/AppDraggable：原生 HorizontalPager/pointerInput 属语言能力
❌ Reorderable（记录自定义排序需求立项时建）

### 13. Animation 动画

✅ AnimatedListItem、animateNumber、AppMotion 令牌体系（duration/easing/densityAdjusted）+ `MotionHardcodedDuration` 审计守门
⛔ AppAnimatedVisibility/AppAnimatedContent/AppFade/AppScale/AppSlide/AppExpand/AppCollapse：这些是对 `animate*AsState` + 令牌曲线的一行式调用，包装成组件只会隔一层；保持"令牌 + 原语"路线
❌ AppSharedTransition（Compose 1.7+ experimental；首页→详情转场值得做时评估）

### 14. Specialized 专用

✅ AppRate（星级）、AppScoreSelector（分数选择）、AppColorDots（颜色选择）、DateTimeCascade（日期时间级联选择）、TimePickerDialog、AppTimerRow
❌ AppPinInput/AppOTPInput（auth 登录、family 邀请码加入流程的直接受益方）、AppImageViewer（图片大图查看）、AppMediaPicker、AppQRCode（family 邀请分享，需引入生成库，先做依赖评估）
⛔ AppRatingBar（与 AppRate 重复）、AppColorSwatch（AppColorDots 已覆盖）

---

## 四、"令牌先行"线索：5 组已注册未消费令牌

对账脚本口径：`AppComponentTokens.kt` 注册属性 vs 全仓 `LocalAppComponentTokens.current.<prop>` 消费。52 组中 47 组已被消费，**以下 5 组为零消费**：

| 令牌组 | 属性 | 对应待建组件 | 直接受益模块 |
|--------|------|------------|------------|
| SelectTokens | `.select` | AppDropdownMenu / AppSelect 增强 | settings、feeding 分类选择 |
| TableTokens | `.table` | AppDataTable | stats、health |
| MenuTokens | `.menu` | AppMenu（解锁 SplitButton/ContextMenu） | 全局 |
| StepsTokens | `.steps` | AppStepper / StepIndicator | development 评估流程 |
| PaginationTokens | `.pagination` | AppPagination | timeline、message |

> 这是前人把令牌先写好、组件待建的明确信号。P0 建设顺序建议按此表：Menu → DataTable → Stepper → Pagination → Select 增强。

---

## 五、规划 vs 仓库既有约定：冲突裁决

1. **"变体即组件"清单 → 一律收编为轴参数。** 规划各分类明细里仍散落 AppOutlinedTextField、AppFilledTextField、AppRatingBar 等变体命名，与规划自己的第十七节矛盾。仓库裁决以三轴模型为准（AppButton/AppCard 已示范），审计规则会拦住 feature 层私造通用容器。
2. **状态拆分组件 → 违反四态规范，不建。** AppSuccessState/AppWarningState/AppInfoState/AppLoadingState 均不建；Loading→Skeleton、Empty→EmptyState、Error→AppErrorState(status=…)、Success 不包装。扩展一律走现有组件的 status/severity 枚举。
3. **目录结构 → 不照搬 `components/<14 类>/` 重构。** 理由：(a) 现有按域子目录（button/、card/、switchcontrol/…）+ composites 层 + `AppComponents.kt` 索引已满足可发现性；(b) 规划自身承认 Feedback/Overlay、Display/Data 存在交叉，强行单归会造成反复搬家；(c) 重构会破坏 `generate-component.sh` 流程与全部 import，纯 churn 零用户价值。替代方案：在 AppComponents.kt 注释中为每个组件标注规划类别标签，建立"逻辑分类映射"，物理目录不动。
4. **AppText/AppIcon → 不建。** 业务代码直用原生 Text/Icon 是既成事实且被审计默许（原语级豁免），字体全部经 LocalAppTypography 解析。包装层只会增加转发噪音。
5. **Toast/Snackbar 双轨 → Snackbar 单轨收口。**
6. **Layout 包装器泛滥 → 克制。** Compose 不是 Flutter，Row/Column/Box/Spacer/aspectRatio 是语言原语；只保留带间距语义/主题语义的封装（现状 AppRow/AppColumn/BorderContainer 恰好就是这一档）。

---

## 六、边界与债务盘点

### 6.1 命名不统一（技术债，非违规）

以下公共组件未带 `App` 前缀，与 AGENTS.md "App* 组件体系"表述不一致，建议分批加别名或重命名：

RecordCard · StatCell · QuickStatPill · SegmentedControl · EmptyState · SkeletonLoader · TimePickerDialog · DateTimeCascadeDialog · DateNavCapsule · CountdownChip · BadgeIcon · EmojiBadge(文件) · MiniBarChart · MiniLineChart · SectionHeader · SubPageScaffold · CenterVerticallyRow · BorderContainer · QuickTimeChipRow · MetricTrendLabel · BabyIllustration

### 6.2 领域通用件的定位

RecordCard / AppRecordRow / RecordDetailSheet / CountdownChip / AppTimerRow / AppCategoryStrip 是"宝宝记录"域的跨模块抽象（feeding/sleep/diaper/growth/vaccination 共用），放在 designsystem 符合 DRY；但应守住一条线：**只服务单一 feature 的组件必须留在 feature 层**。composites/（ChatBubble、HeroStatCard 等）继续作为"组件+组件"的组合层。

### 6.3 feature 层组织

现状 16+1 个业务模块均无 `components/` 子目录，业务弹窗平铺在模块根（FeedingFormDialog、SleepFormDialog、SettingsDialogs/Sheets 等 11 个文件）。可采纳规划建议的 `feature/*/components/` 新约定约束增量，存量迁移收益有限，列为可选清理项。

---

## 七、建设优先级路线图（✅ P0 已完成，2026-08-26）

**P0 —— 令牌已就位，补 UI 即可（✅ 全部落地）**
1. ✅ AppMenu（MenuTokens）—— components/menu/
2. ✅ AppDataTable（TableLogic + TableTokens，排序即表头轴）—— components/table/
3. ✅ AppStepper（StepsTokens，水平/垂直方向轴）—— components/stepper/
4. ✅ AppPagination（PaginationTokens）—— components/pagination/
5. ✅ AppSelect 下拉形态（SelectTokens；底部弹层单选沿用 AppOptionPickerSheet）—— components/select/

**P1 —— 业务直接受益的新组件（待需求触发）**
6. SearchField → 已裁决并入 `AppInput(type=search)` 扩轴
7. AppTimeline（timeline/home 记录流）
8. PullToRefresh 令牌化封装（timeline 刷新）
9. AppPinInput/AppOTPInput（auth/family 邀请码）
10. Autocomplete/SearchableDropdown → `AppInput(suggestions)` + AppMenu

**P2 —— 出现场景再做**
11. AppTooltip/Popover、AppMultiSelect、AppToggleGroup
12. AppImageViewer、AppMediaPicker、AppQRCode（先做依赖评估）
13. AppIcons 图标注册表、AppSharedTransition、Reorderable

**结果记录：** 五组悬空令牌全部有消费者；新增纯逻辑单测 PageSequenceTest(6 例)、StepStatesTest(5 例)；detekt 新组件目录零违规；参照范式贯彻（Token 驱动 Defaults / Motion 走 LocalAppMotion / 朗读走 AppStrings / 空表走 EmptyState 四态）。体量仅陈述：核心 Composable 约 105~110 个，52 组组件令牌无悬空注册，不设数量指标。

---

## 附：核对方式复现

```powershell
# 未消费令牌组对账（口径见第四节）
$src = Get-Content ".../theme/AppComponentTokens.kt" -Raw
$props = [regex]::Matches($src, 'val (\w+): \w+Tokens') | % { $_.Groups[1].Value }
# 全仓 grep 'LocalAppComponentTokens.current.<prop>' 差集
```
