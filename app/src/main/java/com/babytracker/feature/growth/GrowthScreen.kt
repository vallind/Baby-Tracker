package com.babytracker.feature.growth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.babytracker.core.domain.model.Growth
import com.babytracker.core.domain.model.GrowthType
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.core.data.repository.GrowthRepository
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.RequireBaby
import com.babytracker.designsystem.components.SegmentedControl
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.button.PrimaryButton
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.input.AppInput
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GrowthScreen(navController: NavController) {
    val c = LocalAppColors.current
    val growthRepo: GrowthRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    RequireBaby(babyId = babyId.toLong(), navController = navController) {
    val growths by growthRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingGrowth by remember { mutableStateOf<Growth?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("身高", "体重", "头围")
    val types = listOf(GrowthType.HEIGHT, GrowthType.WEIGHT, GrowthType.HEAD)
    val units = listOf("cm", "kg", "cm")
    val normalRanges = listOf(
        "71.2-85.1cm",
        "8.1-12.5kg",
        "44.0-49.0cm",
    )

    AppScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "生长记录",
                onBack = { navController.popBackStack() },
                actions = {
                    AppIconButton(
                        icon = Icons.Default.DateRange,
                        onClick = { /* 日历选择：暂时保留入口，后续可接日期筛选 */ },
                        contentDescription = "日历",
                        tint = c.textPrimary,
                    )
                },
            )
        },
        bottomBar = { BottomNavBar(navController) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(c.pageBackground),
        ) {
            // —— Tab 切换区 ——
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                SegmentedControl(
                    labels = tabs,
                    selectedIndex = tab,
                    onSelect = { tab = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            val chartData = remember(growths, tab) {
                growths.filter { it.type == types[tab] }.sortedBy { it.measuredAt }
            }
            val latest = chartData.lastOrNull()
            val grouped = remember(growths, tab) {
                growths.filter { it.type == types[tab] }
                    .sortedByDescending { it.measuredAt }
                    .groupBy { it.measuredAt.take(10) }
            }

            if (chartData.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = when (tab) { 0 -> "📏"; 1 -> "⚖️"; else -> "📐" },
                        title = "还没有${tabs[tab]}记录",
                        subtitle = "点击底部按钮，记录宝宝的${tabs[tab]}变化",
                        actionText = "记录${tabs[tab]}",
                        onAction = {
                            editingGrowth = null
                            showForm = true
                        },
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 8.dp,
                    ),
                ) {
                    // —— Hero 大数值卡 ——
                    if (latest != null) {
                        item {
                            val valText = when {
                                latest.value == latest.value.toLong().toDouble() && latest.value != 0.0 ->
                                    String.format("%.0f", latest.value)
                                else -> String.format("%.1f", latest.value)
                            }
                            val measuredDate = try {
                                LocalDateTime.parse(latest.measuredAt, DateTimeFormatter.ISO_DATE_TIME)
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            } catch (_: Exception) { "" }

                            AppCard(
                                cornerRadius = 16.dp,
                                containerColor = c.surface,
                                elevation = 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                            ) {
                                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "当前${tabs[tab]}",
                                        style = LocalAppTypography.current.bodyMedium,
                                        color = c.textSecondary,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                    ) {
                                        Text(
                                            valText,
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.textPrimary,
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            units[tab],
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = c.textSecondary,
                                            modifier = Modifier.padding(bottom = 10.dp),
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "$measuredDate 测量",
                                        style = LocalAppTypography.current.bodySmall,
                                        color = c.textTertiary,
                                    )
                                }
                            }
                        }
                    }

                    // —— 生长曲线图 ——
                    item {
                        val chartProgress by animateFloatAsState(
                            targetValue = if (chartData.size > 1) 1f else 0f,
                            animationSpec = tween(durationMillis = 800),
                        )
                        val chartCardShape = RoundedCornerShape(12.dp)
                        val gridColor = c.divider
                        val lineColor = c.primary
                        val bgColor = c.surface
                        val areaBrush = Gradients.growthChart(c)
                        val minVal = chartData.minOfOrNull { it.value } ?: 0.0
                        val maxVal = chartData.maxOfOrNull { it.value } ?: 100.0
                        val range = (maxVal - minVal).coerceAtLeast(1.0)
                        val yLabels = remember(minVal, maxVal) {
                            (0..3).map { i ->
                                val v = maxVal - (range * i / 3)
                                String.format("%.1f", v)
                            }
                        }

                        AppCard(
                            cornerRadius = 12.dp,
                            containerColor = c.surface,
                            elevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        ) {
                            BoxWithConstraints(Modifier.fillMaxWidth().padding(16.dp)) {
                                val chartHeight = maxHeight * 0.7f
                                // Y 轴标签
                                Column(
                                    Modifier.height(chartHeight).width(36.dp).padding(bottom = 24.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    yLabels.forEach { Text(it, style = LocalAppTypography.current.labelSmall, color = c.textSecondary) }
                                }

                                Canvas(Modifier.fillMaxWidth().height(chartHeight).padding(start = 36.dp, bottom = 24.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    // 网格线
                                    for (i in 0..3) {
                                        drawLine(gridColor, Offset(0f, h * i / 4), Offset(w, h * i / 4), strokeWidth = 1f)
                                    }
                                    // WHO 参考虚线
                                    val whoLines = whoReferenceLines(GrowthType.raw(types[tab]), minVal, maxVal, range)
                                    whoLines.forEach { percentile ->
                                        val y = h * (1f - ((percentile - minVal) / range).toFloat()).coerceIn(0f, h)
                                        val dashed = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                            floatArrayOf(10f, 10f), 0f,
                                        )
                                        drawLine(
                                            color = lineColor.copy(alpha = 0.25f),
                                            start = Offset(0f, y),
                                            end = Offset(w, y),
                                            strokeWidth = 1f,
                                            pathEffect = dashed,
                                        )
                                    }
                                    if (chartData.size > 1 && chartProgress > 0f) {
                                        val points = chartData.mapIndexed { i, g ->
                                            Offset(w * i / (chartData.size - 1), h * (1f - ((g.value - minVal) / range).toFloat()))
                                        }
                                        val visibleCount = ((points.size - 1) * chartProgress).toInt().coerceIn(0, points.size - 1)
                                        // 面积填充
                                        val areaPath = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(points[0].x, h)
                                            for (i in 0..visibleCount) { lineTo(points[i].x, points[i].y) }
                                            lineTo(points[visibleCount].x, h)
                                            close()
                                        }
                                        drawPath(areaPath, brush = areaBrush)
                                        // 连线
                                        for (i in 0 until visibleCount) {
                                            drawLine(lineColor, points[i], points[i + 1], strokeWidth = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                        }
                                        // 数据点
                                        for (i in 0..visibleCount) {
                                            drawCircle(lineColor, 5.dp.toPx(), points[i])
                                            drawCircle(bgColor, 2.5.dp.toPx(), points[i])
                                        }
                                    } else if (chartData.size == 1) {
                                        drawCircle(lineColor, 5.dp.toPx(), Offset(w / 2, h / 2))
                                        drawCircle(bgColor, 2.5.dp.toPx(), Offset(w / 2, h / 2))
                                    }
                                }

                                // X 轴标签
                                if (chartData.size > 1) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(start = 36.dp, top = chartHeight - 20.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        chartData.forEachIndexed { i, g ->
                                            if (i % maxOf(1, chartData.size / 5) == 0 || i == chartData.size - 1) {
                                                Text(
                                                    try {
                                                        LocalDateTime.parse(g.measuredAt, DateTimeFormatter.ISO_DATE_TIME)
                                                            .format(DateTimeFormatter.ofPattern("MM/dd"))
                                                    } catch (_: Exception) { "" },
                                                    style = LocalAppTypography.current.labelSmall,
                                                    color = c.textSecondary,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // —— 正常范围卡片 ——
                    item {
                        AppCard(
                            cornerRadius = 12.dp,
                            containerColor = c.surface,
                            elevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(c.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("📊", fontSize = 16.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "${tabs[tab]}正常范围",
                                        style = LocalAppTypography.current.bodySmall,
                                        color = c.textSecondary,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        normalRanges[tab],
                                        style = LocalAppTypography.current.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = c.textPrimary,
                                    )
                                }
                            }
                        }
                    }

                    // —— 历史记录列表 ——
                    grouped.forEach { (date, items) ->
                        stickyHeader(key = date) {
                            Text(
                                date,
                                style = LocalAppTypography.current.labelMedium,
                                color = c.textSecondary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                        items(items = items, key = { it.id }) { g ->
                            val label = DateUtils.growthTypeLabel(GrowthType.raw(g.type))
                            val valStr = String.format("%.1f", g.value)
                            val dateStr = try {
                                LocalDateTime.parse(g.measuredAt, DateTimeFormatter.ISO_DATE_TIME)
                                    .format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
                            } catch (_: Exception) { "" }
                            val unitStr = when (g.type) {
                                GrowthType.HEIGHT -> "cm"
                                GrowthType.WEIGHT -> "kg"
                                GrowthType.HEAD -> "cm"
                            }
                            RecordCard(
                            onDelete = {
                                scope.launch {
                                    growthRepo.delete(g)
                                    appSnackbar.showUndo(message = "已删除生长记录") { growthRepo.insert(g) }
                                }
                            },
                                onClick = {},
                                onLongClick = {
                                    editingGrowth = g
                                    showForm = true
                                },
                                modifier = Modifier.padding(bottom = 8.dp),
                            ) {
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(c.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        when (g.type) {
                                            GrowthType.HEIGHT -> "📏"
                                            GrowthType.WEIGHT -> "⚖️"
                                            GrowthType.HEAD -> "📐"
                                        },
                                        style = LocalAppTypography.current.titleLarge,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "$label $valStr $unitStr",
                                        style = LocalAppTypography.current.titleSmall,
                                        color = c.textPrimary,
                                    )
                                    Text(
                                        dateStr,
                                        style = LocalAppTypography.current.bodySmall,
                                        color = c.textSecondary,
                                    )
                                }
                            }
                        }
                    }

                    // 底部留白给按钮
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }

            // —— 底部固定按钮 ——
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                PrimaryButton(
                    onClick = {
                        editingGrowth = null
                        showForm = true
                    },
                    label = "记录${tabs[tab]}",
                    icon = Icons.Default.Add,
                    height = 48.dp,
                    cornerRadius = 12.dp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showForm) {
        GrowthFormDialog(
            babyId = babyId,
            editEntity = editingGrowth,
            onDismiss = {
                showForm = false
                editingGrowth = null
            },
            onSave = { growth ->
                scope.launch {
                    if (editingGrowth != null) {
                        growthRepo.update(growth)
                    } else {
                        growthRepo.insert(growth)
                    }
                    appSnackbar.showSuccess("已保存")
                    showForm = false
                    editingGrowth = null
                }
            },
        )
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthFormDialog(
    babyId: Int,
    editEntity: Growth? = null,
    onDismiss: () -> Unit,
    onSave: (Growth) -> Unit,
) {
    val c = LocalAppColors.current
    val isEdit = editEntity != null
    var type by remember { mutableStateOf(editEntity?.let { GrowthType.raw(it.type) } ?: "height") }
    var value by remember {
        mutableStateOf(
            editEntity?.value?.let {
                if (it == it.toLong().toDouble() && it == 0.0) "" else String.format("%.1f", it)
            } ?: "",
        )
    }
    var measuredAt by remember {
        mutableStateOf(
            editEntity?.measuredAt?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) {
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                }
            } ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        )
    }
    var note by remember { mutableStateOf(editEntity?.note ?: "") }
    var showCascadePicker by remember { mutableStateOf(false) }

    val buildEntity = {
        if (isEdit) {
            editEntity.copy(
                type = GrowthType.fromRaw(type),
                value = value.toDoubleOrNull() ?: 0.0,
                measuredAt = measuredAt.replace(" ", "T") + ":00",
                note = note.ifBlank { null },
            )
        } else {
            Growth(
                babyId = babyId,
                type = GrowthType.fromRaw(type),
                value = value.toDoubleOrNull() ?: 0.0,
                measuredAt = measuredAt.replace(" ", "T") + ":00",
                note = note.ifBlank { null },
            )
        }
    }

    AppFormSheet(
        title = if (isEdit) "编辑生长" else "记录生长",
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) "更新" else "保存",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            listOf("height" to "📏 身高", "weight" to "⚖️ 体重", "head" to "📐 头围").forEach { (t, label) ->
                FilterChip(
                    selected = type == t,
                    onClick = { type = t },
                    label = { Text(label, style = LocalAppTypography.current.bodySmall) },
                )
            }
        }

        AppInput(
            value = value,
            onValueChange = { value = it },
            label = "数值",
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )

        AppInput(
            value = measuredAt,
            onValueChange = {},
            label = "测量时间",
            enabled = false,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showCascadePicker = true },
        )

        AppInput(
            value = note,
            onValueChange = { note = it },
            label = "备注 (可选)",
            modifier = Modifier.fillMaxWidth(),
        )
    }

    DateTimeCascadeDialog(
        show = showCascadePicker,
        initialDateTime = measuredAt,
        onConfirm = { measuredAt = it },
        onDismiss = { showCascadePicker = false },
    )
}

/**
 * WHO 0-2 岁参考百分位（简化版，仅作图表参考虚线使用）。
 */
private fun whoReferenceLines(type: String, minVal: Double, maxVal: Double, range: Double): List<Double> {
    val median = when (type) {
        "height" -> 67.0
        "weight" -> 7.5
        "head" -> 43.0
        else -> return emptyList()
    }
    val offsets = listOf(0.10, 0.0, -0.10)
    return offsets.map { median * (1 + it) }
        .filter { it in (minVal - range * 0.2)..(maxVal + range * 0.2) }
}
