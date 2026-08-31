# 组件清退审计 —— 哪些不应继续留在 designsystem 包内

> 审计口径：97 个公共 Composable 全量扫描，统计「feature/navigation 层外部引用」与「designsystem 内部真实引用」（剔除定义文件自身与 AppComponents.kt 注释索引的假阳性）。
> 判据三条：① 零真实消费＝死代码；② 单一业务耦合＝公共性不足；③ 与路线图冲突＝被收敛蓝图取代。
> 关联：《component-taxonomy.md》《component-gap-analysis.md》《reference-components.md》

---

## 一、A 类 · 立即清退清单（零真实消费，12 组 ≈ 20 个公共符号）

| # | 目标 | 类型 | 说明 |
|---|------|------|------|
| A1 | `composites/insightcard/` | 整目录 | AppInsightCard 零消费 |
| A2 | `composites/chartcontainer/` | 整目录 | AppChartContainer 零消费 |
| A3 | `foundation/` **整层** | 整目录×2 | border/BorderContainer、layout/(AppRow·AppColumn·CenterVerticallyRow) 全部零消费；Compose 语言原语已覆盖其职责（收敛蓝图裁决）|
| A4 | `hooks/Hooks.kt` | 文件 | useDebounce / useState / useLatestState / consoleWarn —— React 风 hooks 移植物，违反蓝图"Framework-coupled"反模式，零消费 |
| A5 | `hooks/ButtonLogic.kt` | 文件 | ButtonLogic + rememberButtonLogic 零消费 |
| A6 | `hooks/FormLogic.kt` | 文件 | FormLogic + rememberFormLogic 零消费 |
| A7 | `hooks/TableLogic.kt` | 拆分处置 | TableLogic / PageConfig / rememberTableLogic 死代码删除；**SortConfig 迁往 `components/table/`**（AppDataTable 契约在用）；TimerState.kt 存活保留 |
| A8 | `components/timepicker/` 内 TimePickerDialog | 单函数 | 弹窗本体零消费（TimePickerLogic 被 DateTimeCascade 复用，保留）|
| A9 | `components/skeleton/Skeleton.kt` 内 SkeletonLoader | 单函数 | SkeletonBar 被 AppCard 加载态复用保留；Loader 本体零消费 |
| A10 | `components/progress/` 内 AppLinearProgress | 单函数 | 圆形档存活(4 处)，线性档零消费 |
| A11 | `components/slider/` 内 AppLabeledSlider | 单函数 | AppSlider 存活；带标签档零消费 |
| A12 | 六个独立单件 | 目录/文件 | AppActionSheet(dialog/ 内，蓝图已裁由 Sheet 收编，直接删) · AppRate(rate/ 整目录) · AppColorDots(colordots/ 整目录) · AppCheckbox(switchcontrol/ 内单文件，Switch/Radio 存活) · CountdownChip(components 根) · BadgeIcon(components 根) · BabyIllustration(components 根) |

> 共同点：全部只剩 AppComponents.kt 索引注释这一种"引用"。其中 React 风 hooks 四文件是最大宗僵尸代码。

**连带动作**：AppComponentTokens 摘除对应空挂注册（如 RateTokens 等，逐个核销）；AppComponents.kt 索引注释同步清理；三闸全绿后单提交 `设计系统 清退：…`。

## 二、B 类 · 观察名单（仅 1 个 feature 消费，暂缓迁移）

| 组件 | 唯一消费方 | 备注 |
|------|-----------|------|
| AppChatBubble / AppChatInputBar / AppCollapsedHeader / AppTypingIndicator | ai ×4 | AI 聊天四件套；G4 波次刚收编且已令牌化，翻烧饼成本＞收益 |
| AppScoreSelector | development | 发育评估打分条 |
| AppCategoryStrip | message | 消息分类条 |
| AppMarkdownText | ai | Markdown 渲染，形态通用性最强 |
| MiniBarChart / MiniLineChart / MetricTrendLabel | stats | 经 AppMetricCard 组合链存活 |
| AppTileGrid | home | 功能宫格 |
| AppInitialAvatar | family/settings | 形态通用 |
| AppRecordRow | timeline | 记录行 |

**处置原则**：不立即迁回。触发条件二选一——① 出现第二消费模块即转正为公共件；② 对应模块重构且确认无跨模块前景时迁入 `feature/<模块>/components/`。

## 三、豁免与存活说明

- **P0/P1 八个新组件**（Menu/Pagination/Stepper/Select/DataTable/PinInput/Timeline/PullToRefresh）外部零消费属**路线图预期**（能力先行，等待页面接入），不在清退之列。
- **组合存活件**：SkeletonBar←AppCard、QuickStatPill←AppHeroStatCard、AppEmojiBadge←记录三处、AppFilterChip←芯片族、TimePickerLogic←DateTimeCascade。
- **高消费核心件**（ext≥10）：AppScaffold(22)/AppTopBar(21)/AppCard(18)/AppInput(16)/EmptyState(13)/AppButton(13) —— 设计系统骨架，无讨论余地。

## 四、命名债务（另册，不清退）

裸名存量 21 个（RecordCard/StatCell/SegmentedControl…）按差距分析 6.1 节分批收编，与本审计解耦。

## 五、执行建议

A 类一次性清退（预计删 15~18 个文件 + AppComponentTokens 摘除 1~2 组空挂令牌 + 索引同步），B 类不动。执行后全量三闸 + Paparazzi（x86_64）复核截图基线。
