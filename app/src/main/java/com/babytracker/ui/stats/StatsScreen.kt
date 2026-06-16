package com.babytracker.ui.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babytracker.core.theme.DT
import com.babytracker.core.theme.LocalThemeColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.core.util.BabyController
import com.babytracker.data.repository.BabyRepository
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController) {
    val c = LocalThemeColors.current
    val viewModel: StatsViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    val babyCtrl: BabyController = koinInject()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return

    LaunchedEffect(babyId) {
        viewModel.loadData(babyId)
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("统计分析") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatsPeriod.entries.forEach { p ->
                    FilterChip(
                        selected = state.period == p,
                        onClick = { viewModel.loadData(babyId, p) },
                        label = {
                            Text(
                                when (p) {
                                    StatsPeriod.WEEK -> "周"
                                    StatsPeriod.MONTH -> "月"
                                    StatsPeriod.YEAR -> "年"
                                }
                            )
                        },
                    )
                }
            }
            Row(Modifier.padding(horizontal = DT.pageMargin.dp), horizontalArrangement = Arrangement.spacedBy(DT.cardGap.dp)) {
                StatCard("喂养次数", "${state.feedingCount}次", state.feedingPoints, c.pink)
                StatCard("睡眠时长", "${state.sleepHours}h", state.sleepPoints, c.blue)
            }
            Spacer(Modifier.height(DT.cardGap.dp))
            Row(Modifier.padding(horizontal = DT.pageMargin.dp), horizontalArrangement = Arrangement.spacedBy(DT.cardGap.dp)) {
                StatCard("身高增长", state.height, state.heightPoints, c.green)
                StatCard("体重增长", state.weight, state.weightPoints, c.yellow)
            }
            Spacer(Modifier.height(DT.cardGap.dp))
            Row(Modifier.padding(horizontal = DT.pageMargin.dp), horizontalArrangement = Arrangement.spacedBy(DT.cardGap.dp)) {
                StatCard("换尿布", "${state.diaperCount}次", emptyList(), c.cyan)
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun RowScope.StatCard(title: String, value: String, points: List<Float>, accent: Color) {
    Card(
        Modifier.weight(1f),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (points.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                MiniLineChart(points, accent, Modifier.fillMaxWidth().height(50.dp))
            }
        }
    }
}

@Composable
fun MiniLineChart(points: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (points.size < 2) {
            if (points.size == 1) {
                drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
            }
            return@Canvas
        }
        val max = points.max()
        val min = points.min()
        val range = (max - min).coerceAtLeast(1f)
        val stepX = size.width / (points.size - 1)
        for (i in 0 until points.size - 1) {
            val x1 = i * stepX
            val y1 = size.height - ((points[i] - min) / range) * size.height
            val x2 = (i + 1) * stepX
            val y2 = size.height - ((points[i + 1] - min) / range) * size.height
            drawLine(
                color = color,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}