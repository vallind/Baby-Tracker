package com.babytracker.ui.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.core.theme.DT
import com.babytracker.core.theme.Gradients
import com.babytracker.core.theme.LocalThemeColors
import com.babytracker.core.util.BabyController
import com.babytracker.ui.components.BottomNavBar
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController) {
    val c = LocalThemeColors.current
    val viewModel: StatsViewModel = org.koin.androidx.compose.koinViewModel()
    val state by viewModel.state.collectAsState()

    val babyCtrl: BabyController = koinInject()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return

    LaunchedEffect(babyId) {
        viewModel.loadData(babyId)
    }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("统计分析", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = c.primaryLight,
                    titleContentColor = c.textPrimary,
                    navigationIconContentColor = c.textPrimary,
                ),
            )
        },
        bottomBar = { BottomNavBar(navController) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).background(c.bg)) {
            // —— 顶部周期筛选区（浅蓝渐变背景 + 胶囊标签）——
            Box(Modifier.fillMaxWidth().background(Gradients.pageHeader(c)).padding(horizontal = DT.pageMargin.dp, vertical = 12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatsPeriod.entries.forEach { p ->
                        val selected = state.period == p
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(DT.chipRadius.dp))
                                .background(if (selected) c.primary else c.card)
                                .clickable { viewModel.loadData(babyId, p) }
                                .padding(horizontal = 18.dp, vertical = 8.dp),
                        ) {
                            Text(
                                when (p) {
                                    StatsPeriod.WEEK -> "周"
                                    StatsPeriod.MONTH -> "月"
                                    StatsPeriod.YEAR -> "年"
                                },
                                color = if (selected) Color.White else c.textSecondary,
                                fontWeight = if (selected) FontWeight.SemiBold else null,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(DT.cardGap.dp))
            Row(Modifier.padding(horizontal = DT.pageMargin.dp), horizontalArrangement = Arrangement.spacedBy(DT.cardGap.dp)) {
                StatCard("喂养次数", "${state.feedingCount}次", state.feedingPoints)
                StatCard("睡眠时长", "${state.sleepHours}h", state.sleepPoints)
            }
            Spacer(Modifier.height(DT.cardGap.dp))
            Row(Modifier.padding(horizontal = DT.pageMargin.dp), horizontalArrangement = Arrangement.spacedBy(DT.cardGap.dp)) {
                StatCard("身高增长", state.height, state.heightPoints)
                StatCard("体重增长", state.weight, state.weightPoints)
            }
            Spacer(Modifier.height(DT.cardGap.dp))
            Row(Modifier.padding(horizontal = DT.pageMargin.dp), horizontalArrangement = Arrangement.spacedBy(DT.cardGap.dp)) {
                StatCard("换尿布", "${state.diaperCount}次", emptyList())
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun RowScope.StatCard(title: String, value: String, points: List<Float>) {
    val c = LocalThemeColors.current
    val cardShape = RoundedCornerShape(DT.cardRadius.dp)
    Card(
        Modifier.weight(1f).shadow(elevation = DT.cardElevation.dp, shape = cardShape),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = c.card),
    ) {
        Column(Modifier.padding(DT.cardInnerPadding.dp)) {
            Text(title, fontSize = 12.sp, color = c.textSecondary)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = DT.textSizeXxl.sp, fontWeight = FontWeight.Bold, color = c.primary)
            if (points.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                MiniLineChart(points, Modifier.fillMaxWidth().height(50.dp))
            }
        }
    }
}

@Composable
fun MiniLineChart(points: List<Float>, modifier: Modifier = Modifier) {
    val c = LocalThemeColors.current
    val lineColor = c.primary
    val areaBrush = Gradients.chartArea(c)
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
        drawPath(areaPath, brush = areaBrush)
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
        // 末端数据点（强调当前值）
        drawCircle(lineColor, 3.dp.toPx(), coords.last())
    }
}
