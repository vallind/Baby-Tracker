package com.babytracker.ui.growth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.domain.model.Growth
import com.babytracker.core.theme.DT
import com.babytracker.core.theme.LocalThemeColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.data.repository.GrowthRepository

import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GrowthScreen(navController: NavController) {
    val c = LocalThemeColors.current
    val growthRepo: GrowthRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    val growths by growthRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var deletingGrowth by remember { mutableStateOf<Growth?>(null) }

    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("身高", "体重", "头围")
    val types = listOf("height", "weight", "head")

    if (babyId == 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("请先添加宝宝", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("生长记录") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } })
    }, floatingActionButton = {
        FloatingActionButton(onClick = { showForm = true }, containerColor = MaterialTheme.colorScheme.primary) {
            Icon(Icons.Default.Add, contentDescription = "添加记录", tint = Color.White)
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.padding(horizontal = DT.pageMargin.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(2.dp)) {
                tabs.forEachIndexed { i, label ->
                    Box(Modifier.weight(1f).background(if (tab == i) MaterialTheme.colorScheme.surface else Color.Transparent, RoundedCornerShape(6.dp)).clickable { tab = i }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(label, color = if (tab == i) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (tab == i) FontWeight.SemiBold else null, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            val chartData = remember(growths, tab) { growths.filter { it.type == types[tab] }.sortedBy { it.measuredAt } }
            val chartProgress by animateFloatAsState(
                targetValue = if (chartData.size > 1) 1f else 0f,
                animationSpec = tween(durationMillis = 800),
            )
            val minVal = remember(chartData) { chartData.minOfOrNull { it.value } }
            val maxVal = remember(chartData) { chartData.maxOfOrNull { it.value } }
            val gridColor = MaterialTheme.colorScheme.outlineVariant
            val lineColor = MaterialTheme.colorScheme.primary
            val bgColor = MaterialTheme.colorScheme.background
            Box(Modifier.fillMaxWidth().height(300.dp)) {
                Column(Modifier.fillMaxHeight().width(36.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    val labels = if (minVal != null && maxVal != null) {
                        (0..4).map { i -> "%.1f".format(maxVal - (maxVal - minVal) * i / 4.0) }
                    } else listOf("--", "--", "--", "--", "--")
                    labels.forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Canvas(Modifier.fillMaxSize().padding(start = 36.dp, bottom = 24.dp)) {
                    val w = size.width; val h = size.height
                    for (i in 0..3) { drawLine(gridColor, Offset(0f, h * i / 4), Offset(w, h * i / 4), strokeWidth = 1f) }
                    if (chartData.size > 1 && chartProgress > 0f && minVal != null && maxVal != null) {
                        val range = (maxVal - minVal).coerceAtLeast(1.0)
                        val points = chartData.mapIndexed { i, g -> Offset(w * i / (chartData.size - 1), h * (1f - ((g.value - minVal) / range).toFloat())) }
                        val visibleCount = ((points.size - 1) * chartProgress).toInt().coerceIn(0, points.size - 1)
                        for (i in 0 until visibleCount) { drawLine(lineColor, points[i], points[i + 1], strokeWidth = 3.dp.toPx()) }
                        for (i in 0..visibleCount) { drawCircle(lineColor, 4.dp.toPx(), points[i]); drawCircle(bgColor, 2.dp.toPx(), points[i]) }
                    }
                }
                if (chartData.size > 1) {
                    Row(Modifier.fillMaxWidth().padding(start = 36.dp, top = 300.dp - 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        chartData.forEachIndexed { i, g ->
                            if (i % maxOf(1, chartData.size / 5) == 0 || i == chartData.size - 1) {
                                Text(
                                    g.measuredAt.format(DateTimeFormatter.ofPattern("MM/dd")),
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            growths.filter { it.type == types[tab] }.sortedByDescending { it.measuredAt }.groupBy { it.measuredAt.toLocalDate().toString() }.forEach { (date, items) ->
                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 8.dp))
                items.forEach { g ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { it / 2 },
                ) {
                Card(
                    Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 4.dp).fillMaxWidth().combinedClickable(onLongClick = { deletingGrowth = g }, onClick = {}),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(c.green.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Text("📏", style = MaterialTheme.typography.titleLarge) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${DateUtils.growthTypeLabel(g.type)} ${g.value}", style = MaterialTheme.typography.titleSmall)
                            Text(DateUtils.formatDate(g.measuredAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingGrowth = null }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthFormDialog(babyId: Int, onDismiss: () -> Unit, onSave: (Growth) -> Unit) {
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
                shape = MaterialTheme.shapes.small,
            )

            OutlinedTextField(
                value = measuredAt, onValueChange = {}, readOnly = true,
                label = { Text("测量时间") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showDatePicker = true },
                shape = MaterialTheme.shapes.small, enabled = false,
                colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant),
            )

            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("备注 (可选)") }, singleLine = false,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = MaterialTheme.shapes.small,
                minLines = 2,
            )

            Button(
                onClick = {
                    onSave(Growth(
                        babyId = babyId, type = type,
                        value = value.toDoubleOrNull() ?: 0.0,
                        measuredAt = LocalDateTime.parse(measuredAt.replace(" ", "T"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                        note = note.ifBlank { null },
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.small,
            ) { Text("保存") }
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