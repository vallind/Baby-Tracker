# 设计系统 Token 三层架构

> 组件库五阶段收敛 · Phase 3（Token 三层化文档化）。
> 配套：《组件收敛蓝图》`component-taxonomy.md`（四层 API 契约 §六）。
> 基线：designsystem v2.4.0 现状（52 组组件令牌 + 4 组辅助颜色 + 注册中心）。

---

## 一、三层模型

```text
┌─────────────────────────────────────────────────────────────┐
│ ① Primitive（原始值）  AppColorScale·SoftPalettes·数值令牌族   │
│     → 只有值，没有语义（Gray100/Blue500 的等价物）             │
├─────────────────────────────────────────────────────────────┤
│ ② Semantic（语义层）   AppColors·AppSemanticScales·AppDensity │
│     → 值与含义绑定（Surface/ContentPrimary/ActionDanger）     │
├─────────────────────────────────────────────────────────────┤
│ ③ Component（组件层）  AppComponentTokens（52 组）            │
│     → 每组件一组，只被 *Defaults 工厂消费                     │
└─────────────────────────────────────────────────────────────┘
```

**核心承诺：换主题 = 改 ①② 的装配，③ 与全部组件代码零改动。**

---

## 二、Primitive 层（`theme/AppColorScale.kt` + `theme/AppTokens.kt`）

### 2.1 色板原始值

| Primitive | 结构 | 说明 |
|---|---|---|
| `AppColorScale` | `default + shade50~shade900`（10 档 + 主值档） | 任意品牌种子可 `fromSeed(seed)` 生成整条色阶（亮档向白、暗档向黑） |
| `SoftPalettes` | `blue` / `violet` / `teal` / `green` / `amber` / `coral` / `stone`（中性暖灰）+ `softSemantics` 集合 | 官方锚点色板，仅此文件允许硬编码色值（detekt HardcodedColor 白名单层） |

取用约定（禁止 `.copy(alpha)` 伪造浅档）：浅底/徽章底 → `shade100`；选中底/hover → `shade200`；前景强调 → `shade600`（浅色）/ `shade400`（暗色）；主按钮容器 → `default`。

### 2.2 数值令牌原始值（AppTokens.kt 默认值即 Primitive 表）

| 族 | 档位 |
|---|---|
| `AppSpacing` | 8 级 4pt 网格：none 0 / xxs 2 / xs 4 / sm 8 / md 16 / lg 24 / xl 32 / xxl 48；`scaled(factor)` 全局缩放 |
| `AppElevation` | 6 级暖棕阴影：level0 0 / level1 2 / level2 4 / level3 8 / level4 12 / level5 20 |
| `AppOpacity` | 7 级：disabled .38 / subtle .12 / hover .08 / pressed .16 / scrim .48 / overlay .32 / divider .12 |
| `AppMotion` | 时长 3 级：fast 150 / medium 300 / long 600；缓动 3 条：standard(decelerate) / decelerate / accelerate |
| `AppShapes` | 10 级圆角 + `radiusScale` 全局缩放：none 0 / extraSmall 8 / small 12 / medium 16 / large 20 / largeIncreased 24 / extraLarge 32 / extraLargeIncreased 36 / extraExtraLarge 48 / full 胶囊 |
| `AppTypography` | 12 级自建字阶：displayLarge 40 → labelSmall 11（Bold→Medium→Normal，行高宽松） |
| `AppControlTokens` | 控件尺寸三档：small 32/13sp/16dp 图标 / medium 48/15sp/20dp / large 56/16sp/24dp |

## 三、Semantic 层

| Semantic | 结构 | 说明 |
|---|---|---|
| `AppColors` | primary/onPrimary/primaryContainer、secondary、tertiary、surface/onSurface、background、error/success/warning（各带 on*）、outline、scrim、textPrimary~textDisabled、pageBackground、surfaceElevated/surfaceOverlay/surfaceMuted、borderHover/Focus/Disabled、bgDisabled/Hover/Pressed/Selected、divider/overlay/shadow/shadowFocus/shadowError、info/danger + 6 组 `*Scale` | `light()` / `dark()` 两套官方装配；`derive(primary=…, surface=…, …)` 供自定义主题——**换主题唯一入口** |
| `AppSemanticScales` | primary/secondary/success/warning/danger 五色 `AppColorScale` 集合 | `derive()` 可选注入项，主题自定义时整体替换 |
| `AppDensity` + `AppDensityTokens` | Comfortable 1.0 / Compact .85 / Large 1.15（spacingScale）+ 控件高度增量 ±8dp | 全局密度三档，`AppControlTokens.densityAdjusted()` 只动 medium 档；组件零迁移 |
| `Gradients` | 预置渐变画刷（hero 卡/头部） | 语义渐变，非组件内联 |

## 四、Component 层（`theme/AppComponentTokens.kt`，52 组主令牌 + 注册中心）

| 域 | 令牌组 |
|---|---|
| 动作 | `ButtonTokens` · `IconButtonTokens` · `FabTokens` |
| 输入/选择 | `InputTokens`（含本轮新增 size 三档字段）· `SelectTokens` · `SliderTokens` · `SwitchTokens` · `SelectionControlTokens` · `ChipTokens` · `SegmentedControlTokens` · `Rating`→`RateTokens` |
| 展示 | `CardTokens` · `ListItemTokens` · `TagTokens`(+`TagVariantColors`) · `BadgeTokens` · `Emoji`→并入 Badge · `StatCellTokens` · `QuickStatPillTokens` · `KeyValueRowTokens` · `SummaryCardTokens` · `MiniChartTokens` · `AppBarTokens` · `ActionBarTokens` · `Avatar`→（无独立组，走 surface/typography） |
| 导航 | `BottomBarTokens` · `MenuTokens` · `PaginationTokens` · `StepsTokens` · `DateNavCapsuleTokens` |
| 覆盖/反馈 | `DialogTokens` · `SheetTokens` · `SnackbarHostTokens` · `InlineBannerTokens`(+`BannerSeverityColors`) · `EmptyStateTokens` · `ErrorState`→复用 EmptyState · `ProgressTokens` · `SkeletonTokens` · `ScaffoldTokens` · `BorderContainerTokens` |
| 日期时间 | `TimePickerTokens` · `DatePickerTokens` · `DateTimeCascadeTokens` |
| 数据 | `TableTokens` · `TimelineTokens` |
| 业务形态（随 Phase 4 外移后 Token 仍留库内） | `SettingItemTokens` · `HeroStatCardTokens` · `TileGridTokens` · `ScoreSelectorTokens`(+`ScoreSelectorOptionColors`) · `CategoryStripTokens`(+`CategoryStripAccentColors`) · `ChatBubbleTokens` · `ChatInputBarTokens` · `CollapsedHeaderTokens` · `RecordDetailSheetTokens` |
| 注册中心 | `AppComponentTokens`（`default(colors, spacing, shapes, typography, opacity, motion, elevation, control, darkTheme)` 聚合装配全部 52 组；`LocalAppComponentTokens` 注入） |

## 五、装配链与换主题路径

```text
AppTheme(AppColors.light()/dark()/自定义 derive)
        ↓
AppComponentTokens.default(colors, spacing, shapes, typography, opacity, motion, elevation, control, darkTheme)
        ↓
LocalAppComponentTokens / LocalAppColors / LocalAppSpacing / LocalAppShapes /
LocalAppMotion / LocalAppElevation / LocalAppOpacity / LocalAppTypography / LocalAppControl / LocalAppDensity
        ↓
*Defaults 工厂（L3 唯一消费点）→ 组件（L1/L2 API）→ feature / patterns
```

**换主题操作清单**（理论上零组件代码改动）：
1. 换品牌色 → 改 `AppColors.light()/dark()` 的 `derive(primary=…)` 或新增 `softPalettes` 版锚点；
2. 换圆角风格 → `AppShapes.radiusScale`（0.8 更方 / 1.2 更圆）；
3. 换密度 → `AppDensity`（全局，组件零迁移）；
4. 换动效节奏 → `AppMotion` 三档；
5. 换组件级观感 → 对应 `*Tokens.default()` 装配处（`AppComponentTokens.default()`），不改组件代码。

## 六、守门与验收

- `ThemeTokenizationStaticAuditTest`：主题/组件层的令牌化静态审计（禁止直读 `MaterialTheme.*`）。
- `ComponentTokensStateAuditTest`：暗色模式与状态色响应（组件级令牌必须随主题派生）。
- `TokenAuditChecker`（`themeTokenAudit`）：规则 6 `FeatureLayerGenericCard`、规则 7 `MotionHardcodedDuration`、`DefaultsMissingLocalAppComponentTokens` / `DefaultsHardcodedColor` / `DefaultsImportsLocalAppColors`（Defaults 工厂禁直读核心令牌/硬编码色/直 import LocalAppColors）。
- detekt 自定义规则：`HardcodedColorRule`（仅 `theme/` 白名单允许色值字面量）、`TokenBypassRule`。
- `AppButtonApiAuditTest` / `AppInputApiAuditTest`（Phase 2 新增）：业务代码禁给 Core 组件传裸 token 参数。

**验收口径**：新增主题后执行 `./gradlew themeTokenAudit testDebugUnitTest detekt` 全绿，且不修改任何 `components/` 组件代码，即为"换主题 ≠ 改组件"达成。