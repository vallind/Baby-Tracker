package com.babytracker.ui.growth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.core.database.entity.GrowthEntity
import com.babytracker.core.theme.DT
import com.babytracker.core.theme.Gradients
import com.babytracker.core.theme.LocalThemeColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.data.repository.GrowthRepository
import kotlinx.coroutines.launch
import com.babytracker.ui.components.rememberHaptic
import com.babytracker.ui.components.longPressDeletable
import com.babytracker.ui.components.EmptyState
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GrowthScreen(navController: NavController) {
    val c = LocalThemeColors.current
    val growthRepo: GrowthRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val haptic = rememberHaptic()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val growths by growthRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var deletingGrowth by remember { mutableStateOf<GrowthEntity?>(null) }

    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("身高", "体重", "头围")
    val types = listOf("height", "weight", "head")

    Scaffold(containerColor = c.bg, topBar = {
        CenterAlignedTopAppBar(title = { Text("生长记录", fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = c.primaryLight,
                titleContentColor = c.textPrimary,
                navigationIconContentColor = c.textPrimary,
            ))
    }, floatingActionButton = {
        FloatingActionButton(onClick = { showForm = true }, containerColor = c.primary, contentColor = Color.White) {
            Icon(Icons.Default.Add, contentDescription = "添加记录")
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).background(c.bg)) {
            // —— 顶部 Tab 区（浅蓝渐变背景 + 小圆角指示器）——
            Box(Modifier.fillMaxWidth().background(Gradients.pageHeader(c)).padding(horizontal = DT.pageMargin.dp, vertical = 12.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    tabs.forEachIndexed { i, label ->
                        Column(Modifier.weight(1f).clickable { tab = i }.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, color = if (tab == i) c.primary else c.textSecondary, fontWeight = if (tab == i) FontWeight.SemiBold else null, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(6.dp))
                            Box(Modifier.width(24.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(if (tab == i) c.primary else Color.Transparent))
                        }
                    }
                }
            }
            Spacer(Modifier.height(DT.cardGap.dp))
            val chartData = remember(growths, tab) { growths.filter { it.type == types[tab] }.sortedBy { it.measuredAt } }
            // —— 当前数值大字显示 + 正常范围说明 ——
            val latest = chartData.lastOrNull()
            val normalRangeHint = when (types[tab]) {
                "height" -> "WHO 参考范围 50-80 cm（6 月龄约 67 cm）"
                "weight" -> "WHO 参考范围 3-12 kg（6 月龄约 7.5 kg）"
                "head" -> "WHO 参考范围 34-48 cm（6 月龄约 43 cm）"
                else -> ""
            }
            val currentShape = RoundedCornerShape(DT.cardRadius.dp)
            Card(
                Modifier.padding(horizontal = DT.pageMargin.dp).fillMaxWidth().shadow(elevation = DT.cardElevation.dp, shape = currentShape),
                shape = currentShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = c.card),
            ) {
                Row(Modifier.padding(DT.cardInnerPadding.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(DT.iconBgSize.dp).clip(RoundedCornerShape(DT.iconBgRadius.dp)).background(c.primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                        Text(when (tab) { 0 -> "📏"; 1 -> "⚖️"; else -> "📐" }, fontSize = DT.iconSize.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("当前${tabs[tab]}", fontSize = 12.sp, color = c.textSecondary)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            latest?.let { String.format("%.1f", it.value) } ?: "--",
                            fontSize = DT.textSizeXxl.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.textPrimary,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(normalRangeHint, fontSize = 11.sp, color = c.textSecondary)
                    }
                }
            }
            Spacer(Modifier.height(DT.cardGap.dp))
            if (chartData.isEmpty()) {
                EmptyState(
                    emoji = when (tab) { 0 -> "📏"; 1 -> "⚖️"; else -> "📐" },
                    title = "还没有${tabs[tab]}记录",
                    subtitle = "点击右下角按钮，记录宝宝的${tabs[tab]}变化",
                )
            }
            val chartProgress by animateFloatAsState(
                targetValue = if (chartData.size > 1) 1f else 0f,
                animationSpec = tween(durationMillis = 800),
            )
            val gridColor = c.divider
            val lineColor = c.primary
            val bgColor = c.card
            val areaBrush = Gradients.growthChart(c)
            // 动态刻度：基于实际数据 min/max
            val minVal = chartData.minOfOrNull { it.value } ?: 0.0
            val maxVal = chartData.maxOfOrNull { it.value } ?: 100.0
            val range = (maxVal - minVal).coerceAtLeast(1.0)
            val yLabels = remember(minVal, maxVal) {
                (0..3).map { i ->
                    val v = maxVal - (range * i / 3)
                    String.format("%.1f", v)
                }
            }
            val chartCardShape = RoundedCornerShape(DT.cardRadius.dp)
            Card(
                Modifier.padding(horizontal = DT.pageMargin.dp).fillMaxWidth().shadow(elevation = DT.cardElevation.dp, shape = chartCardShape),
                shape = chartCardShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = c.card),
            ) {
                Box(Modifier.fillMaxWidth().height(300.dp).padding(DT.cardInnerPadding.dp)) {
                Column(Modifier.fillMaxHeight().width(36.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    yLabels.forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = c.textSecondary) }
                }
                Canvas(Modifier.fillMaxSize().padding(start = 36.dp, bottom = 24.dp)) {
                    val w = size.width; val h = size.height
                    // 网格线
                    for (i in 0..3) { drawLine(gridColor, Offset(0f, h * i / 4), Offset(w, h * i / 4), strokeWidth = 1f) }
                    // WHO 参考百分位虚线（仅当有数据且类型为 weight/height 时显示）
                    val whoLines = whoReferenceLines(types[tab], minVal, maxVal, range)
                    whoLines.forEach { percentile ->
                        val y = h * (1f - ((percentile - minVal) / range).toFloat()).coerceIn(0f, h)
                        val dashed = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        drawLine(
                            color = lineColor.copy(alpha = 0.25f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f,
                            pathEffect = dashed,
                        )
                    }
                    if (chartData.size > 1 && chartProgress > 0f) {
                        val points = chartData.mapIndexed { i, g -> Offset(w * i / (chartData.size - 1), h * (1f - ((g.value - minVal) / range).toFloat())) }
                        val visibleCount = ((points.size - 1) * chartProgress).toInt().coerceIn(0, points.size - 1)
                        // 渐变填充区域
                        val areaPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(points[0].x, h)
                            for (i in 0..visibleCount) { lineTo(points[i].x, points[i].y) }
                            lineTo(points[visibleCount].x, h)
                            close()
                        }
                        drawPath(areaPath, brush = areaBrush)
                        // 折线
                        for (i in 0 until visibleCount) { drawLine(lineColor, points[i], points[i + 1], strokeWidth = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round) }
                        // 数据点：双层圆
                        for (i in 0..visibleCount) {
                            drawCircle(lineColor, 5.dp.toPx(), points[i])
                            drawCircle(bgColor, 2.5.dp.toPx(), points[i])
                        }
                    } else if (chartData.size == 1) {
                        drawCircle(lineColor, 5.dp.toPx(), Offset(w / 2, h / 2))
                        drawCircle(bgColor, 2.5.dp.toPx(), Offset(w / 2, h / 2))
                    }
                }
                val data2 = chartData  // 复用同一变量，避免重复计算
                if (data2.size > 1) {
                    Row(Modifier.fillMaxWidth().padding(start = 36.dp, top = 300.dp - 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        data2.forEachIndexed { i, g ->
                            if (i % maxOf(1, data2.size / 5) == 0 || i == data2.size - 1) {
                                Text(
                                    try { LocalDateTime.parse(g.measuredAt, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("MM/dd")) } catch (_: Exception) { "" },
                                    style = MaterialTheme.typography.labelSmall, color = c.textSecondary,
                                )
                            }
                        }
                    }
                }
                }
            }
            Spacer(Modifier.height(DT.cardGap.dp))
            growths.filter { it.type == types[tab] }.sortedByDescending { it.measuredAt }.groupBy { it.measuredAt.take(10) }.forEach { (date, items) ->
                Text(date, style = MaterialTheme.typography.labelSmall, color = c.textSecondary, modifier = Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 8.dp))
                items.forEach { g ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { it / 2 },
                ) {
                val itemShape = RoundedCornerShape(DT.cardRadius.dp)
                Card(
                    Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 4.dp).fillMaxWidth().longPressDeletable(haptic) { deletingGrowth = g }.shadow(elevation = DT.cardElevation.dp, shape = itemShape),
                    shape = itemShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = c.card),
                ) {
                    Row(Modifier.padding(DT.cardInnerPadding.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(DT.iconBgSize.dp).clip(RoundedCornerShape(DT.iconBgRadius.dp)).background(c.green.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) { Text("📏", style = MaterialTheme.typography.titleLarge) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${DateUtils.growthTypeLabel(g.type)} ${g.value}", style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                            Text(DateUtils.formatDate(java.time.LocalDateTime.parse(g.measuredAt, java.time.format.DateTimeFormatter.ISO_DATE_TIME)), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                        }
                    }
                }
                }
            }
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showForm) {
        GrowthFormDialog(
            babyId = babyId,
            onDismiss = { showForm = false },
            onSave = { growth ->
                scope.launch { growthRepo.insert(growth) }
                showForm = false
            },
        )
    }

    deletingGrowth?.let { g ->
        AlertDialog(
            onDismissRequest = { deletingGrowth = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条生长记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { growthRepo.delete(g) }
                    deletingGrowth = null
                }) { Text("删除", color = c.danger) }
            },
            dismissButton = {
                TextButton(onClick = { deletingGrowth = null }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthFormDialog(babyId: Int, onDismiss: () -> Unit, onSave: (GrowthEntity) -> Unit) {
    val c = LocalThemeColors.current
    var type by remember { mutableStateOf("height") }
    var value by remember { mutableStateOf("") }
    var measuredAt by remember { mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))) }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = DT.pageMargin.dp).verticalScroll(rememberScrollState())) {
            Text("记录生长", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                listOf("height" to "📏 身高", "weight" to "⚖️ 体重", "head" to "📐 头围").forEach { (t, label) ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t },
                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    )
                }
            }

            OutlinedTextField(
                value = value, onValueChange = { value = it },
                label = { Text("数值") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = MaterialTheme.shapes.medium,
            )

            OutlinedTextField(
                value = measuredAt, onValueChange = {}, readOnly = true,
                label = { Text("测量时间") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showDatePicker = true },
                shape = MaterialTheme.shapes.medium, enabled = false,
                colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = c.cardBorder, disabledTextColor = c.textPrimary, disabledLabelColor = c.textSecondary),
            )

            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("备注 (可选)") }, singleLine = false,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = MaterialTheme.shapes.medium,
                minLines = 2,
            )

            Button(
                onClick = {
                    onSave(GrowthEntity(
                        babyId = babyId, type = type,
                        value = value.toDoubleOrNull() ?: 0.0,
                        measuredAt = measuredAt.replace(" ", "T") + ":00",
                        note = note.ifBlank { null },
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(DT.buttonRadius.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary),
            ) { Text("保存", color = Color.White) }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                showDatePicker = false
                datePickerState.selectedDateMillis?.let { millis ->
                    val instant = java.time.Instant.ofEpochMilli(millis)
                    val date = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val timePart = measuredAt.substring(11)
                    measuredAt = "$date $timePart"
                }
                showTimePicker = true
            }) { Text("下一步") }
        }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        var hour by remember { mutableIntStateOf(measuredAt.substring(11, 13).toIntOrNull() ?: 12) }
        var minute by remember { mutableIntStateOf(measuredAt.substring(14, 16).toIntOrNull() ?: 0) }
        AlertDialog(onDismissRequest = { showTimePicker = false }, title = { Text("选择时间") }, text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { if (hour < 23) hour++ }) { Text("▲", style = MaterialTheme.typography.bodySmall) }
                        Text("%02d".format(hour), style = MaterialTheme.typography.headlineMedium)
                        TextButton(onClick = { if (hour > 0) hour-- }) { Text("▼", style = MaterialTheme.typography.bodySmall) }
                        Text("时", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(" : ", style = MaterialTheme.typography.headlineMedium)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { if (minute < 59) minute++ }) { Text("▲", style = MaterialTheme.typography.bodySmall) }
                        Text("%02d".format(minute), style = MaterialTheme.typography.headlineMedium)
                        TextButton(onClick = { if (minute > 0) minute-- }) { Text("▼", style = MaterialTheme.typography.bodySmall) }
                        Text("分", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                val datePart = measuredAt.take(10)
                measuredAt = "$datePart ${"%02d".format(hour)}:${"%02d".format(minute)}"
                showTimePicker = false
            }) { Text("确定") }
        }, dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } })
    }
}
/**
 * WHO 0-2 岁参考百分位（简化版，仅作图表参考虚线使用）。
 * 返回当前数据范围内可见的百分位值列表。
 *
 * 数据来源：WHO Child Growth Standards（男孩/女孩 0-2 岁平均值，取近似中位 ± 偏移）。
 * 简化处理：只返回与当前数据 [minVal, maxVal] 范围有交集的中位/15th/85th 三条线。
 */
private fun whoReferenceLines(type: String, minVal: Double, maxVal: Double, range: Double): List<Double> {
    // 各类型 6 月龄参考值（中位数）
    val median = when (type) {
        "height" -> 67.0  // 6 月龄身高中位 cm
        "weight" -> 7.5   // 6 月龄体重中位 kg
        "head" -> 43.0    // 6 月龄头围中位 cm
        else -> return emptyList()
    }
    // 生成 3 条参考线：85th / 50th / 15th
    val offsets = listOf(0.10, 0.0, -0.10)  // +10% / 中位 / -10%
    return offsets.map { median * (1 + it) }
        .filter { it in (minVal - range * 0.2)..(maxVal + range * 0.2) }
}
