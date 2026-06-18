package com.babytracker.ui.sleep

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.domain.model.Sleep
import com.babytracker.core.theme.DT
import com.babytracker.core.theme.LocalThemeColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.data.repository.SleepRepository

import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val NIGHT_GOAL_HOURS = 14
private const val NIGHT_GOAL_SECONDS = NIGHT_GOAL_HOURS * 3600

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SleepListScreen(navController: NavController) {
    val sleepRepo: SleepRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val sleeps by sleepRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var deletingSleep by remember { mutableStateOf<Sleep?>(null) }

    val today = java.time.LocalDate.now().toString()
    val night = sleeps.filter { it.type == "night" && it.startTime.toLocalDate().toString() == today }.firstOrNull()
    val nightDurSec = night?.let { java.time.Duration.between(it.startTime, it.endTime).seconds.coerceAtLeast(0) } ?: 0L
    val nightRange = night?.let {
        val s = it.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        val e = it.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        "$s-$e"
    } ?: ""

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("睡眠记录") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } })
    }, floatingActionButton = {
        FloatingActionButton(onClick = { showForm = true }, containerColor = MaterialTheme.colorScheme.primary) {
            Icon(Icons.Default.Add, contentDescription = "添加睡眠记录")
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Card(
                Modifier.fillMaxWidth().padding(horizontal = DT.pageMargin.dp, vertical = 16.dp),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, Color(0xFF312E81)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text("今日睡眠", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA5B4FC))
                            Spacer(Modifier.height(4.dp))
                            Text(DateUtils.durationFullText(nightDurSec), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                            Spacer(Modifier.height(2.dp))
                            Text(nightRange, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA5B4FC))
                        }
                        Text("🌙", style = MaterialTheme.typography.headlineLarge)
                    }
                    Spacer(Modifier.height(16.dp))
                    val goalPercent = (nightDurSec.toFloat() / NIGHT_GOAL_SECONDS.toFloat()).coerceIn(0f, 1f)
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.1f))) {
                        Box(Modifier.fillMaxWidth(goalPercent).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB)))))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            val grouped = sleeps.groupBy { it.startTime.toLocalDate().toString() }
            grouped.forEach { (date, items) ->
                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 8.dp))
                items.forEach { s ->
                val start = s.startTime
                val end = s.endTime
                Card(
                    Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 4.dp).fillMaxWidth().combinedClickable(onLongClick = { deletingSleep = s }, onClick = {}),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (s.type == "night") "🌙" else "☀️", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(if (s.type == "night") "夜间睡眠" else "小睡", style = MaterialTheme.typography.titleSmall)
                            Text("${start.format(DateTimeFormatter.ofPattern("HH:mm"))}-${end.format(DateTimeFormatter.ofPattern("HH:mm"))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(DateUtils.durationFullText(DateUtils.durationToTotalSeconds(start, end)), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                    }
                }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showForm) {
        SleepFormDialog(babyId = babyId, sleepRepo = sleepRepo, onDismiss = { showForm = false })
    }

    deletingSleep?.let { s ->
        AlertDialog(
            onDismissRequest = { deletingSleep = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条睡眠记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { sleepRepo.delete(s) }
                    deletingSleep = null
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingSleep = null }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepFormDialog(babyId: Int, sleepRepo: SleepRepository, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedType by remember { mutableStateOf("night") }
    val now = LocalDateTime.now()
    var startTime by remember { mutableStateOf(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))) }
    var endTime by remember { mutableStateOf(now.plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))) }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableIntStateOf(0) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedType == "night", onClick = { selectedType = "night" }, label = { Text("🌙 夜间睡眠") })
                FilterChip(selected = selectedType == "nap", onClick = { selectedType = "nap" }, label = { Text("☀️ 小睡") })
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = startTime, onValueChange = {}, readOnly = true, label = { Text("开始时间") }, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 0; showDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.small, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = endTime, onValueChange = {}, readOnly = true, label = { Text("结束时间") }, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 1; showDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.small, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small)
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                scope.launch {
                    sleepRepo.insert(Sleep(
                        babyId = babyId,
                        type = selectedType,
                        startTime = LocalDateTime.parse(startTime.replace(" ", "T"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                        endTime = LocalDateTime.parse(endTime.replace(" ", "T"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                        note = note.ifBlank { null },
                    ))
                    onDismiss()
                }
            }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.small) {
                Text("保存")
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    fun pickerField() = if (pickerTarget == 0) startTime else endTime
    fun updatePickerField(v: String) { if (pickerTarget == 0) startTime = v else endTime = v }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                showDatePicker = false
                datePickerState.selectedDateMillis?.let { millis ->
                    val instant = java.time.Instant.ofEpochMilli(millis)
                    val date = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val timePart = pickerField().substring(11)
                    updatePickerField("$date $timePart")
                }
                showTimePicker = true
            }) { Text("下一步") }
        }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val current = pickerField()
        var tpHour by remember { mutableIntStateOf(current.substring(11, 13).toIntOrNull() ?: 12) }
        var tpMinute by remember { mutableIntStateOf(current.substring(14, 16).toIntOrNull() ?: 0) }
        AlertDialog(onDismissRequest = { showTimePicker = false }, title = { Text("选择时间") }, text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { if (tpHour < 23) tpHour++ }) { Text("▲", style = MaterialTheme.typography.bodySmall) }
                        Text("%02d".format(tpHour), style = MaterialTheme.typography.headlineMedium)
                        IconButton(onClick = { if (tpHour > 0) tpHour-- }) { Text("▼", style = MaterialTheme.typography.bodySmall) }
                        Text("时", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(" : ", style = MaterialTheme.typography.headlineMedium)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { if (tpMinute < 59) tpMinute++ }) { Text("▲", style = MaterialTheme.typography.bodySmall) }
                        Text("%02d".format(tpMinute), style = MaterialTheme.typography.headlineMedium)
                        IconButton(onClick = { if (tpMinute > 0) tpMinute-- }) { Text("▼", style = MaterialTheme.typography.bodySmall) }
                        Text("分", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                val datePart = pickerField().take(10)
                updatePickerField("$datePart ${"%02d".format(tpHour)}:${"%02d".format(tpMinute)}")
                showTimePicker = false
            }) { Text("确定") }
        }, dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } })
    }
}

@Composable
fun RowScope.SleepStatCell(label: String, value: String) {
    Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}