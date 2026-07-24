package com.babytracker.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.babytracker.core.util.BabyController
import com.babytracker.designsystem.components.SegmentedControl
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.progress.AppCircularProgress
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypographyStyle
import com.babytracker.designsystem.components.scaffold.AppScaffold
import org.koin.compose.koinInject

@Composable
fun StatsScreen(navController: NavController) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current
    val viewModel: StatsViewModel = org.koin.androidx.compose.koinViewModel()
    val state by viewModel.state.collectAsState()

    val babyCtrl: BabyController = koinInject()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return

    LaunchedEffect(babyId) {
        viewModel.loadData(babyId)
    }

    AppScaffold(
        topBar = {
            AppTopBar(title = "统计分析")
        },
        bottomBar = { BottomNavBar(navController) },
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
                    .padding(horizontal = spacing.lg)
                    .padding(top = spacing.md, bottom = 12.dp),
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
                        viewModel.selectPeriod(babyId, p)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            DateRangeNav(
                dateRangeText = state.dateRangeText,
                canGoBack = true,
                canGoForward = state.periodOffset < 0,
                onBack = { viewModel.goBack(babyId) },
                onForward = { viewModel.goForward(babyId) },
            )

            Spacer(Modifier.height(spacing.md))

            when {
                state.isLoading -> StatsLoadingState()
                state.errorMessage != null -> {
                    EmptyState(
                        emoji = "⚠️",
                        title = "统计数据加载失败",
                        subtitle = state.errorMessage.orEmpty(),
                        actionText = "重新加载",
                        onAction = viewModel::retry,
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
                        FeedingCard(
                            count = state.feedingCount,
                            breastFeedCount = state.breastFeedCount,
                            formulaCount = state.formulaCount,
                            formulaTotalMl = state.formulaTotalMl,
                            compare = state.feedingCompare,
                            points = state.feedingPoints,
                        )
                        SleepCard(
                            minutes = state.sleepMinutes,
                            compare = state.sleepCompare,
                            points = state.sleepPoints,
                        )
                        HeightCard(
                            value = state.height,
                            compare = state.heightCompare,
                            points = state.heightPoints,
                        )
                        WeightCard(
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
    val typography = LocalAppTypographyStyle.current
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
private fun DateRangeNav(
    dateRangeText: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.ChevronLeft,
            contentDescription = "上一周期",
            tint = if (canGoBack) c.textSecondary else c.textDisabled,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(enabled = canGoBack) { onBack() },
        )
        Text(
            dateRangeText,
            style = typography.bodyLarge,
            color = c.textSecondary,
            modifier = Modifier.padding(horizontal = spacing.md),
            textAlign = TextAlign.Center,
        )
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = "下一周期",
            tint = if (canGoForward) c.textSecondary else c.textDisabled,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(enabled = canGoForward) { onForward() },
        )
    }
}

@Composable
private fun FeedingCard(
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
    val typography = LocalAppTypographyStyle.current
    val showBreast = breastFeedCount > 0
    val showFormula = formulaCount > 0
    StatCardFrame(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                StatCardIcon("🍼", c.warning)
                Spacer(Modifier.height(spacing.sm))
                Text("喂养", style = typography.label, color = c.textTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
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
                    StatCompareLabel(compare)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        StatChartArea(
            hasData = points.any { it > 0f },
            emptyText = "本周期暂无喂养记录",
        ) {
            MiniBarChart(points, Modifier.fillMaxWidth().height(52.dp), barColor = c.warning)
        }
    }
}

@Composable
private fun SleepCard(
    minutes: Long,
    compare: String,
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current
    val hours = minutes / 60
    val mins = minutes % 60
    StatCardFrame(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                StatCardIcon("🌙", c.secondary)
                Spacer(Modifier.height(spacing.sm))
                Text("睡眠时长", style = typography.label, color = c.textTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${hours}时${mins}分", style = typography.titleLarge, fontWeight = FontWeight.Bold, color = c.textPrimary)
                if (compare.isNotEmpty()) {
                    StatCompareLabel(compare)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        StatChartArea(
            hasData = points.any { it > 0f },
            emptyText = "本周期暂无睡眠记录",
        ) {
            MiniBarChart(points, Modifier.fillMaxWidth().height(52.dp), barColor = c.secondary)
        }
    }
}

@Composable
private fun HeightCard(
    value: String,
    compare: String,
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current
    StatCardFrame(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                StatCardIcon("📏", c.primary)
                Spacer(Modifier.height(spacing.sm))
                Text("身高增长", style = typography.label, color = c.textTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(value, style = typography.titleLarge, fontWeight = FontWeight.Bold, color = c.textPrimary)
                if (compare.isNotEmpty()) {
                    StatCompareLabel(compare)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        StatChartArea(
            hasData = points.isNotEmpty(),
            emptyText = "本周期暂无身高记录",
        ) {
            MiniLineChart(points, Modifier.fillMaxWidth().height(52.dp))
        }
    }
}

@Composable
private fun WeightCard(
    value: String,
    compare: String,
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current
    StatCardFrame(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                StatCardIcon("⚖️", c.success)
                Spacer(Modifier.height(spacing.sm))
                Text("体重增长", style = typography.label, color = c.textTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(value, style = typography.titleLarge, fontWeight = FontWeight.Bold, color = c.textPrimary)
                if (compare.isNotEmpty()) {
                    StatCompareLabel(compare)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        StatChartArea(
            hasData = points.isNotEmpty(),
            emptyText = "本周期暂无体重记录",
        ) {
            MiniLineChart(points, Modifier.fillMaxWidth().height(52.dp))
        }
    }
}

@Composable
private fun StatChartArea(
    hasData: Boolean,
    emptyText: String,
    content: @Composable () -> Unit,
) {
    val c = LocalAppColors.current
    val typography = LocalAppTypographyStyle.current
    if (hasData) {
        content()
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(emptyText, style = typography.label, color = c.textTertiary)
        }
    }
}

@Composable
private fun StatCardFrame(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    AppCard(
        containerColor = c.surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(spacing.md), content = content)
    }
}

@Composable
private fun StatCardIcon(emoji: String, tint: Color, modifier: Modifier = Modifier) {
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current
    Box(
        modifier
            .size(spacing.xl)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, style = typography.titleMedium)
    }
}

@Composable
private fun StatCompareLabel(compare: String) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current
    val isPositive = compare.startsWith("+")
    Text(
        compare,
        style = typography.label,
        color = if (isPositive) c.success else c.textSecondary,
        modifier = Modifier.padding(top = spacing.xs),
    )
}

@Composable
fun MiniBarChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = LocalAppColors.current.primary,
) {
    val shapes = LocalAppShapes.current
    Canvas(modifier) {
        if (points.none { it > 0f }) return@Canvas

        val maxVal = points.max().coerceAtLeast(1f)
        val barCount = points.size
        val gapRatio = 0.35f
        val totalBars = barCount + (barCount - 1) * gapRatio
        val barWidth = size.width / totalBars
        val gapWidth = barWidth * gapRatio
        val barCornerRadius = shapes.extraSmall.toPx()

        points.forEachIndexed { i, v ->
            if (v <= 0f) return@forEachIndexed
            val barHeight = ((v / maxVal).coerceIn(0f, 1f) * size.height)
            val x = i * (barWidth + gapWidth)
            val y = size.height - barHeight
            val radius = minOf(barCornerRadius, barWidth / 2f, barHeight / 2f)
            drawRoundRect(
                color = barColor.copy(alpha = if (v == maxVal) 1f else 0.6f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(radius, radius),
            )
        }
    }
}

@Composable
fun MiniLineChart(points: List<Float>, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val lineColor = c.primary
    val areaBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(c.primary.copy(alpha = 0.25f), Color.Transparent),
    )
    Canvas(modifier) {
        if (points.size < 2) {
            if (points.size == 1) {
                drawCircle(color = lineColor, radius = 3.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
            }
            return@Canvas
        }
        val max = points.max()
        val min = points.min()
        val range = (max - min).coerceAtLeast(1f)
        val stepX = size.width / (points.size - 1)
        val pointRadius = 3.dp.toPx()
        val chartHeight = (size.height - pointRadius * 2).coerceAtLeast(0f)
        val coords = points.mapIndexed { i, v ->
            val progress = ((v - min) / range).coerceIn(0f, 1f)
            Offset(i * stepX, pointRadius + chartHeight * (1f - progress))
        }
        val areaPath = Path().apply {
            moveTo(coords.first().x, size.height)
            coords.forEach { lineTo(it.x, it.y) }
            lineTo(coords.last().x, size.height)
            close()
        }
        drawPath(areaPath, areaBrush)
        for (i in 0 until coords.size - 1) {
            drawLine(
                color = lineColor,
                start = coords[i],
                end = coords[i + 1],
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        drawCircle(lineColor, 3.dp.toPx(), coords.last())
    }
}
