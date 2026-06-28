package com.babytracker.feature.growth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.babytracker.core.domain.model.Growth
import com.babytracker.core.domain.model.GrowthType
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.GrowthRepository
import kotlinx.coroutines.launch
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.designsystem.components.topbar.AppTopBar
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
    if (babyId == 0) return
    val growths by growthRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingGrowth by remember { mutableStateOf<Growth?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("身高", "体重", "头围")
    val types = listOf(GrowthType.HEIGHT, GrowthType.WEIGHT, GrowthType.HEAD)

    Scaffold(containerColor = c.pageBackground, topBar = {
        AppTopBar(title = "生长记录", onBack = { navController.popBackStack() })
    }, snackbarHost = { SnackbarHost(snackbarHostState) },
    floatingActionButton = {
        AppFAB(icon = Icons.Default.Add, onClick = { editingGrowth = null; showForm = true })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(c.pageBackground)) {
            // —— 顶部 Tab 区 ——
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
            val latest = chartData.lastOrNull()
            val normalRangeHint = when (types[tab]) {
                GrowthType.HEIGHT -> "WHO 参考范围 50-80 cm（6 月龄约 67 cm）"
                GrowthType.WEIGHT -> "WHO 参考范围 3-12 kg（6 月龄约 7.5 kg）"
                GrowthType.HEAD -> "WHO 参考范围 34-48 cm（6 月龄约 43 cm）"
                else -> ""
            }
            val currentShape = RoundedCornerShape(DT.cardRadius.dp)
            Card(
                Modifier.padding(horizontal = DT.pageMargin.dp).fillMaxWidth().shadow(elevation = DT.cardElevation.dp, shape = currentShape),
                shape = currentShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = c.surface),
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
            val chartCardShape = RoundedCornerShape(DT.cardRadius.dp)
            Card(
                Modifier.padding(horizontal = DT.pageMargin.dp).fillMaxWidth().shadow(elevation = DT.cardElevation.dp, shape = chartCardShape),
                shape = chartCardShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = c.surface),
            ) {
                Box(Modifier.fillMaxWidth().height(300.dp).padding(DT.cardInnerPadding.dp)) {
                Column(Modifier.fillMaxHeight().width(36.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    yLabels.forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = c.textSecondary) }
                }
                Canvas(Modifier.fillMaxSize().padding(start = 36.dp, bottom = 24.dp)) {
                    val w = size.width; val h = size.height
                    for (i in 0..3) { drawLine(gridColor, Offset(0f, h * i / 4), Offset(w, h * i / 4), strokeWidth = 1f) }
                    val whoLines = whoReferenceLines(GrowthType.raw(types[tab]), minVal, maxVal, range)
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
                        val areaPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(points[0].x, h)
                            for (i in 0..visibleCount) { lineTo(points[i].x, points[i].y) }
                            lineTo(points[visibleCount].x, h)
                            close()
                        }
                        drawPath(areaPath, brush = areaBrush)
                        for (i in 0 until visibleCount) { drawLine(lineColor, points[i], points[i + 1], strokeWidth = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round) }
                        for (i in 0..visibleCount) {
                            drawCircle(lineColor, 5.dp.toPx(), points[i])
                            drawCircle(bgColor, 2.5.dp.toPx(), points[i])
                        }
                    } else if (chartData.size == 1) {
                        drawCircle(lineColor, 5.dp.toPx(), Offset(w / 2, h / 2))
                        drawCircle(bgColor, 2.5.dp.toPx(), Offset(w / 2, h / 2))
                    }
                }
                val data2 = chartData
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
            val grouped = remember(growths, tab) {
                growths.filter { it.type == types[tab] }.sortedByDescending { it.measuredAt }.groupBy { it.measuredAt.take(10) }
            }
            if (grouped.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("暂无记录", fontSize = 15.sp, color = c.textSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = DT.pageMargin.dp,
                        end = DT.pageMargin.dp,
                        bottom = 80.dp,
                    ),
                ) {
                    grouped.forEach { (date, items) ->
                        stickyHeader(key = date) {
                            Text(date, style = MaterialTheme.typography.labelSmall, color = c.textSecondary, modifier = Modifier.padding(vertical = 4.dp))
                        }
                        items(items = items, key = { it.id }) { g ->
                            RecordCard(
                                modifier = Modifier.padding(bottom = 8.dp),
                                onDelete = {
                                    scope.launch {
                                        val deleted = g
                                        growthRepo.delete(deleted)
                                        val result = snackbarHostState.showSnackbar(
                                            message = "已删除生长记录",
                                            actionLabel = "撤销",
                                            duration = SnackbarDuration.Short,
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            growthRepo.insert(deleted)
                                        }
                                    }
                                },
                                onClick = {},
                                onLongClick = {
                                    editingGrowth = g
                                    showForm = true
                                },
                            ) {
                                Box(Modifier.size(DT.iconBgSize.dp).clip(RoundedCornerShape(DT.iconBgRadius.dp)).background(c.success.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) { Text("📏", style = MaterialTheme.typography.titleLarge) }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("${DateUtils.growthTypeLabel(GrowthType.raw(g.type))} ${g.value}", style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                    Text(DateUtils.formatDate(java.time.LocalDateTime.parse(g.measuredAt, java.time.format.DateTimeFormatter.ISO_DATE_TIME)), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                                }
                            }
                        }
                    }
                }
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
                    showForm = false
                    editingGrowth = null
                }
            },
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    var value by remember { mutableStateOf(editEntity?.value?.let { if (it == it.toLong().toDouble() && it == 0.0) "" else String.format("%.1f", it) } ?: "") }
    var measuredAt by remember {
        mutableStateOf(
            editEntity?.measuredAt?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) { LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }
            } ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        )
    }
    var note by remember { mutableStateOf(editEntity?.note ?: "") }
    var showCascadePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = DT.pageMargin.dp).verticalScroll(rememberScrollState())) {
            Text(if (isEdit) "编辑生长" else "记录生长", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

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
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showCascadePicker = true },
                shape = MaterialTheme.shapes.medium, enabled = false,
                colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = c.outline, disabledTextColor = c.textPrimary, disabledLabelColor = c.textSecondary),
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
                    val growth = if (isEdit) {
                        editEntity.copy(
                            type = GrowthType.fromRaw(type),
                            value = value.toDoubleOrNull() ?: 0.0,
                            measuredAt = measuredAt.replace(" ", "T") + ":00",
                            note = note.ifBlank { null },
                        )
                    } else {
                        Growth(
                            babyId = babyId, type = GrowthType.fromRaw(type),
                            value = value.toDoubleOrNull() ?: 0.0,
                            measuredAt = measuredAt.replace(" ", "T") + ":00",
                            note = note.ifBlank { null },
                        )
                    }
                    onSave(growth)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(DT.buttonRadius.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary),
            ) { Text(if (isEdit) "更新" else "保存", color = Color.White) }
            Spacer(Modifier.height(24.dp))
        }
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
