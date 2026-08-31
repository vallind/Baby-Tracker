package com.babytracker.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.selection.SegmentedControl
import com.babytracker.designsystem.components.feedback.EmptyState
import com.babytracker.designsystem.components.chart.MiniBarChart
import com.babytracker.designsystem.components.chart.MiniLineChart
import com.babytracker.ui.patterns.records.DateNavCapsule
import com.babytracker.designsystem.components.errorstate.AppErrorState
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.components.progress.AppCircularProgress
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.ui.patterns.dashboard.AppMetricCard
import com.babytracker.ui.patterns.dashboard.MetricTrendLabel
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.scaffold.AppScaffold

@Composable
fun StatsScreen(
    state: StatsUiState,
    bottomBar: @Composable () -> Unit = {},
    onSelectPeriod: (StatsPeriod) -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current

    AppScaffold(
        topBar = {
            AppTopBar(title = "统计分析")
        },
        bottomBar = bottomBar,
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(c.pageBackground),
        ) {
            val periodLabels = listOf("日", "周", "月", "年")
            val selectedIndex = when (state.period) {
                StatsPeriod.DAY -> 0
                StatsPeriod.WEEK -> 1
                StatsPeriod.MONTH -> 2
                StatsPeriod.YEAR -> 3
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
            ) {
                SegmentedControl(
                    labels = periodLabels,
                    selectedIndex = selectedIndex,
                    onSelect = { idx ->
                        val p = when (idx) {
                            0 -> StatsPeriod.DAY
                            1 -> StatsPeriod.WEEK
                            2 -> StatsPeriod.MONTH
                            3 -> StatsPeriod.YEAR
                            else -> StatsPeriod.WEEK
                        }
                        onSelectPeriod(p)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // 与记录四页统一为 DateNavCapsule 形态（无日期选择器，胶囊只读展示）
            DateNavCapsule(
                dateLabel = state.dateRangeText,
                onPrev = onGoBack,
                onNext = { if (state.periodOffset < 0) onGoForward() },
                onOpenPicker = {},
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.xs),
            )

            Spacer(Modifier.height(spacing.md))

            when {
                state.isLoading -> StatsLoadingState()
                state.errorMessage != null -> {
                    AppErrorState(
                        title = AppStrings.errorStatsTitle,
                        message = state.errorMessage,
                        retryLabel = AppStrings.reload,
                        onRetry = onRetry,
                    )
                }
                !state.hasAnyData -> {
                    EmptyState(
                        emoji = "📊",
                        title = "本周期暂无统计数据",
                        subtitle = "完成喂养、睡眠或生长记录后，这里会显示对应趋势",
                    )
                }
                else -> {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.md),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FeedingMetric(
                            count = state.feedingCount,
                            breastFeedCount = state.breastFeedCount,
                            formulaCount = state.formulaCount,
                            formulaTotalMl = state.formulaTotalMl,
                            compare = state.feedingCompare,
                            points = state.feedingPoints,
                        )
                        SleepMetric(
                            minutes = state.sleepMinutes,
                            compare = state.sleepCompare,
                            points = state.sleepPoints,
                        )
                        HeightMetric(
                            value = state.height,
                            compare = state.heightCompare,
                            points = state.heightPoints,
                        )
                        WeightMetric(
                            value = state.weight,
                            compare = state.weightCompare,
                            points = state.weightPoints,
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StatsLoadingState() {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppCircularProgress()
        Spacer(Modifier.height(spacing.md))
        Text("正在加载统计数据", style = typography.bodyMedium, color = c.textSecondary)
    }
}

@Composable
private fun FeedingMetric(
    count: Int,
    breastFeedCount: Int,
    formulaCount: Int,
    formulaTotalMl: Int,
    compare: String,
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val showBreast = breastFeedCount > 0
    val showFormula = formulaCount > 0
    AppMetricCard(
        emoji = "🍼",
        title = "喂养",
        accentColor = c.danger,
        modifier = modifier,
        valueContent = {
            if (showBreast && showFormula) {
                Text("母乳 ${breastFeedCount}次", style = typography.titleMedium, fontWeight = FontWeight.Bold, color = c.textPrimary)
                Text("配方 ${formulaTotalMl}ml", style = typography.titleMedium, fontWeight = FontWeight.Bold, color = c.textPrimary)
            } else if (showBreast) {
                Text("母乳 ${breastFeedCount}次", style = typography.titleLarge, fontWeight = FontWeight.Bold, color = c.textPrimary)
            } else if (showFormula) {
                Text("配方 ${formulaTotalMl}ml", style = typography.titleLarge, fontWeight = FontWeight.Bold, color = c.textPrimary)
            } else {
                Text("${count}次", style = typography.titleLarge, fontWeight = FontWeight.Bold, color = c.textPrimary)
            }
            if (compare.isNotEmpty()) {
                MetricTrendLabel(compare)
            }
        },
        chart = { MiniBarChart(points, Modifier.fillMaxWidth().height(52.dp), barColor = c.danger) },
        hasChartData = points.any { it > 0f },
        chartEmptyText = "本周期暂无喂养记录",
    )
}

@Composable
private fun SleepMetric(
    minutes: Long,
    compare: String,
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val hours = minutes / 60
    val mins = minutes % 60
    AppMetricCard(
        emoji = "🌙",
        title = "睡眠时长",
        accentColor = c.secondary,
        modifier = modifier,
        valueContent = {
            Text("${hours}时${mins}分", style = typography.titleLarge, fontWeight = FontWeight.Bold, color = c.textPrimary)
            if (compare.isNotEmpty()) {
                MetricTrendLabel(compare)
            }
        },
        chart = { MiniBarChart(points, Modifier.fillMaxWidth().height(52.dp), barColor = c.secondary) },
        hasChartData = points.any { it > 0f },
        chartEmptyText = "本周期暂无睡眠记录",
    )
}

@Composable
private fun HeightMetric(
    value: String,
    compare: String,
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    AppMetricCard(
        emoji = "📏",
        title = "身高增长",
        accentColor = c.primary,
        modifier = modifier,
        valueContent = {
            Text(value, style = typography.titleLarge, fontWeight = FontWeight.Bold, color = c.textPrimary)
            if (compare.isNotEmpty()) {
                MetricTrendLabel(compare)
            }
        },
        chart = { MiniLineChart(points, Modifier.fillMaxWidth().height(52.dp)) },
        hasChartData = points.isNotEmpty(),
        chartEmptyText = "本周期暂无身高记录",
    )
}

@Composable
private fun WeightMetric(
    value: String,
    compare: String,
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    AppMetricCard(
        emoji = "⚖️",
        title = "体重增长",
        accentColor = c.success,
        modifier = modifier,
        valueContent = {
            Text(value, style = typography.titleLarge, fontWeight = FontWeight.Bold, color = c.textPrimary)
            if (compare.isNotEmpty()) {
                MetricTrendLabel(compare)
            }
        },
        chart = { MiniLineChart(points, Modifier.fillMaxWidth().height(52.dp)) },
        hasChartData = points.isNotEmpty(),
        chartEmptyText = "本周期暂无体重记录",
    )
}
