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
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.components.scaffold.AppScaffold
import org.koin.compose.koinInject

/**
 * 统计分析页 — 日/周/月/年维度切换，带日期导航箭头和四张统计卡。
 *
 * 使用 SegmentedControl 做周期切换，左/右箭头翻页，
 * 四张卡分别展示：喂养次数（柱状图）、睡眠时长（柱状图）、身高增长（折线图）、体重增长（折线图）。
 */
@Composable
fun StatsScreen(navController: NavController) {
    val c = LocalAppColors.current
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
            // ── 周期选择器（SegmentedControl） ──
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
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 12.dp),
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

            // ── 日期范围导航 ──
            DateRangeNav(
                dateRangeText = state.dateRangeText,
                canGoBack = true,
                canGoForward = state.periodOffset < 0,
                onBack = { viewModel.goBack(babyId) },
                onForward = { viewModel.goForward(babyId) },
            )

            // ── 统计卡片（纵向列表） ──
            Spacer(Modifier.height(16.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FeedingCard(
                    count = state.feedingCount,
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

            Spacer(Modifier.height(80.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  日期范围导航
// ═══════════════════════════════════════════════════════════

@Composable
private fun DateRangeNav(
    dateRangeText: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
) {
    val c = LocalAppColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
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
            fontSize = 14.sp,
            color = c.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp),
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

// ═══════════════════════════════════════════════════════════
//  四张统计卡
// ═══════════════════════════════════════════════════════════

/** 喂养次数卡 — 图标 + 次数在顶部行，柱状图在下方 */
@Composable
private fun FeedingCard(
    count: Int,
    compare: String,
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    StatCardFrame(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                StatCardIcon("🍼", c.warning)
                Spacer(Modifier.height(8.dp))
                Text("喂养次数", fontSize = 12.sp, color = c.textTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${count}次", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                if (compare.isNotEmpty()) {
                    StatCompareLabel(compare)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        MiniBarChart(points, Modifier.fillMaxWidth().height(52.dp), barColor = c.warning)
    }
}

/** 睡眠时长卡 — 图标 + 时长在顶部行，柱状图在下方 */
@Composable
private fun SleepCard(
    minutes: Long,
    compare: String,
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
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
                Spacer(Modifier.height(8.dp))
                Text("睡眠时长", fontSize = 12.sp, color = c.textTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${hours}时${mins}分", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                if (compare.isNotEmpty()) {
                    StatCompareLabel(compare)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        MiniBarChart(points, Modifier.fillMaxWidth().height(52.dp), barColor = c.secondary)
    }
}

/** 身高卡 — 图标 + 数值在顶部行，折线图在下方 */
@Composable
private fun HeightCard(
    value: String,
    compare: String,
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    StatCardFrame(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                StatCardIcon("📏", c.primary)
                Spacer(Modifier.height(8.dp))
                Text("身高增长", fontSize = 12.sp, color = c.textTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                if (compare.isNotEmpty()) {
                    StatCompareLabel(compare)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        MiniLineChart(points, Modifier.fillMaxWidth().height(52.dp))
    }
}

/** 体重卡 — 图标 + 数值在顶部行，折线图在下方 */
@Composable
private fun WeightCard(
    value: String,
    compare: String,
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    StatCardFrame(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                StatCardIcon("⚖️", c.success)
                Spacer(Modifier.height(8.dp))
                Text("体重增长", fontSize = 12.sp, color = c.textTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                if (compare.isNotEmpty()) {
                    StatCompareLabel(compare)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        MiniLineChart(points, Modifier.fillMaxWidth().height(52.dp))
    }
}

// ═══════════════════════════════════════════════════════════
//  卡片骨架 + 通用子组件
// ═══════════════════════════════════════════════════════════

/** 统计卡外框 — 使用 AppCard 统一圆角 */
@Composable
private fun StatCardFrame(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shapes = LocalAppShapes.current
    val c = LocalAppColors.current
    AppCard(
        cornerRadius = shapes.medium,
        containerColor = c.surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

/** 图标圆底 */
@Composable
private fun StatCardIcon(emoji: String, tint: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = 16.sp)
    }
}

/** 对比标签（如 "+6次"、"-2时"） */
@Composable
private fun StatCompareLabel(compare: String) {
    val c = LocalAppColors.current
    val isPositive = compare.startsWith("+")
    Text(
        compare,
        fontSize = 11.sp,
        color = if (isPositive) c.success else c.textSecondary,
        modifier = Modifier.padding(top = 4.dp),
    )
}

// ═══════════════════════════════════════════════════════════
//  图表组件
// ═══════════════════════════════════════════════════════════

/**
 * 迷你柱状图 — 喂养/睡眠卡片使用。
 *
 * 圆角：柱顶使用 shapes.extraSmall（4dp），与 AppShapes 体系对齐。
 */
@Composable
fun MiniBarChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = LocalAppColors.current.primary,
) {
    val shapes = LocalAppShapes.current
    Canvas(modifier) {
        if (points.isEmpty()) return@Canvas

        val maxVal = points.max().coerceAtLeast(1f)
        val barCount = points.size
        val gapRatio = 0.35f
        val totalBars = barCount + (barCount - 1) * gapRatio
        val barWidth = size.width / totalBars
        val gapWidth = barWidth * gapRatio
        val barCornerRadius = shapes.extraSmall.toPx() // 4dp 柱顶圆角

        points.forEachIndexed { i, v ->
            val barHeight = (v / maxVal) * size.height
            val x = i * (barWidth + gapWidth)
            val y = size.height - barHeight

            // 绘制柱子（顶部带圆角，底部平直）
            val barPath = Path().apply {
                // 左下 → 左上（左侧） → 顶部圆角 → 右上 → 右下
                moveTo(x, size.height)
                lineTo(x, y + barCornerRadius)
                // 左上角圆角
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(x, y, x + barCornerRadius * 2, y + barCornerRadius * 2),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )
                // 顶部 → 右上角
                lineTo(x + barWidth - barCornerRadius, y)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        x + barWidth - barCornerRadius * 2, y,
                        x + barWidth, y + barCornerRadius * 2,
                    ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )
                // 右下
                lineTo(x + barWidth, size.height)
                close()
            }
            drawPath(
                barPath,
                color = barColor.copy(alpha = if (v == maxVal) 1f else 0.6f),
            )
        }
    }
}

/**
 * 迷你折线图 — 身高/体重卡片使用。
 *
 * 绘制渐变填充区域 + 折线 + 末端数据点强调。
 */
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
        val coords = points.mapIndexed { i, v ->
            Offset(i * stepX, size.height - ((v - min) / range) * size.height)
        }
        // 渐变填充区域
        val areaPath = Path().apply {
            moveTo(coords.first().x, size.height)
            coords.forEach { lineTo(it.x, it.y) }
            lineTo(coords.last().x, size.height)
            close()
        }
        drawPath(areaPath, areaBrush)
        // 折线
        for (i in 0 until coords.size - 1) {
            drawLine(
                color = lineColor,
                start = coords[i],
                end = coords[i + 1],
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        // 末端数据点
        drawCircle(lineColor, 3.dp.toPx(), coords.last())
    }
}
