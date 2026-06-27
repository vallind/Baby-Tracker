package com.babytracker.feature.sleep

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.core.database.entity.SleepEntity
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalThemeColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.SleepRepository
import kotlinx.coroutines.launch
import com.babytracker.designsystem.components.SwipeToDeleteContainer
import com.babytracker.designsystem.components.EmptyState
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepListScreen(navController: NavController) {
    val c = LocalThemeColors.current
    val sleepRepo: SleepRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val sleeps by sleepRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingSleep by remember { mutableStateOf<SleepEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val today = java.time.LocalDate.now().toString()
    val night = sleeps.filter { it.type == "night" && it.startTime.startsWith(today) }.firstOrNull()
    val nightDurSec = night?.let { DateUtils.durationToTotalSeconds(LocalDateTime.parse(it.startTime, DateTimeFormatter.ISO_DATE_TIME), LocalDateTime.parse(it.endTime, DateTimeFormatter.ISO_DATE_TIME)) } ?: 0
    val nightRange = night?.let {
        val s = LocalDateTime.parse(it.startTime, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("HH:mm"))
        val e = LocalDateTime.parse(it.endTime, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("HH:mm"))
        "$s-$e"
    } ?: "--"

    Scaffold(
        containerColor = c.bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingSleep = null
                    showForm = true
                },
                containerColor = c.primary,
                contentColor = c.card,
                shape = RoundedCornerShape(DT.buttonRadius.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
                text = { Text("记录睡眠", style = MaterialTheme.typography.titleSmall) },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(c.bg).verticalScroll(rememberScrollState())) {
            // —— 顶部页头 ——
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Gradients.pageHeader(c))
                    .padding(horizontal = DT.pageMargin.dp)
                    .height(DT.appBarHeight.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = c.textPrimary)
                }
                Text(
                    "睡眠记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                )
            }

            // —— 今日夜间睡眠大卡 ——
            val nightCardShape = RoundedCornerShape(DT.cardRadiusLg.dp)
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DT.pageMargin.dp, vertical = DT.cardGap.dp)
                    .shadow(elevation = DT.cardElevation.dp, shape = nightCardShape),
                shape = nightCardShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = c.card),
            ) {
                Column(Modifier.padding(DT.cardInnerPadding.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(DT.iconBgSizeLg.dp)
                                .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                                .background(c.purple.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) { Text("🌙", style = MaterialTheme.typography.headlineSmall) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("今日睡眠", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                DateUtils.durationFullText(nightDurSec),
                                style = MaterialTheme.typography.headlineMedium,
                                color = c.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(nightRange, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    val goalSeconds = 14L * 3600L
                    val goalPercent = (nightDurSec.toFloat() / goalSeconds.toFloat()).coerceIn(0f, 1f)
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(c.divider)) {
                        Box(Modifier.fillMaxWidth(goalPercent).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Gradients.progress(c)))
                    }
                }
            }

            if (sleeps.isEmpty()) {
                EmptyState(
                    emoji = "😴",
                    title = "还没有睡眠记录",
                    subtitle = "点击下方按钮，记录宝宝的睡眠时间",
                    actionText = "记录睡眠",
                    onAction = {
                        editingSleep = null
                        showForm = true
                    },
                )
            }
            val grouped = sleeps.groupBy { it.startTime.take(10) }
            grouped.forEach { (date, items) ->
                Text(date, style = MaterialTheme.typography.labelSmall, color = c.textSecondary, modifier = Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 8.dp))
                items.forEachIndexed { i, s ->
                val start = LocalDateTime.parse(s.startTime, DateTimeFormatter.ISO_DATE_TIME)
                val end = LocalDateTime.parse(s.endTime, DateTimeFormatter.ISO_DATE_TIME)
                val sleepCardShape = RoundedCornerShape(DT.cardRadius.dp)
                val tint = if (i % 2 == 1) c.accent else c.primary
                SwipeToDeleteContainer(
                    modifier = Modifier.padding(bottom = 8.dp),
                    onDelete = {
                        scope.launch {
                            val deleted = s
                            sleepRepo.delete(deleted)
                            val result = snackbarHostState.showSnackbar(
                                message = "已删除睡眠记录",
                                actionLabel = "撤销",
                                duration = SnackbarDuration.Short,
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                sleepRepo.insert(deleted)
                            }
                        }
                    },
                ) {
                    Card(
                        Modifier
                            .padding(horizontal = DT.pageMargin.dp)
                            .fillMaxWidth()
                            .shadow(elevation = DT.cardElevation.dp, shape = sleepCardShape)
                            .clickable(onClick = {
                                editingSleep = s
                                showForm = true
                            }),
                        shape = sleepCardShape,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(containerColor = c.card),
                    ) {
                        Row(Modifier.padding(DT.cardInnerPadding.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(DT.iconBgSize.dp)
                                    .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                                    .background(tint.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center,
                            ) { Text(if (s.type == "night") "🌙" else "☀️", style = MaterialTheme.typography.titleLarge) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(if (s.type == "night") "夜间睡眠" else "小睡", style = MaterialTheme.typography.titleSmall, color = c.textPrimary, fontWeight = FontWeight.Medium)
                                Text("${start.format(DateTimeFormatter.ofPattern("HH:mm"))}-${end.format(DateTimeFormatter.ofPattern("HH:mm"))}", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            }
                            Text(DateUtils.durationFullText(DateUtils.durationToTotalSeconds(start, end)), color = c.primary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showForm) {
        SleepFormDialog(
            babyId = babyId,
            editEntity = editingSleep,
            onDismiss = {
                showForm = false
                editingSleep = null
            },
            onSave = { sleep ->
                scope.launch {
                    if (editingSleep != null) {
                        sleepRepo.update(sleep)
                    } else {
                        sleepRepo.insert(sleep)
                    }
                    showForm = false
                    editingSleep = null
                }
            },
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepFormDialog(
    babyId: Int,
    editEntity: SleepEntity? = null,
    onDismiss: () -> Unit,
    onSave: (SleepEntity) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val isEdit = editEntity != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedType by remember { mutableStateOf(editEntity?.type ?: "night") }
    val now = LocalDateTime.now()
    var startTime by remember {
        mutableStateOf(
            editEntity?.startTime?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) { now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }
            } ?: now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        )
    }
    var endTime by remember {
        mutableStateOf(
            editEntity?.endTime?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) { now.plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }
            } ?: now.plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        )
    }
    var note by remember { mutableStateOf(editEntity?.note ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableIntStateOf(0) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
            Text(if (isEdit) "编辑睡眠" else "记录睡眠", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedType == "night", onClick = { selectedType = "night" }, label = { Text("🌙 夜间睡眠") })
                FilterChip(selected = selectedType == "nap", onClick = { selectedType = "nap" }, label = { Text("☀️ 小睡") })
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = startTime, onValueChange = {}, readOnly = true, label = { Text("开始时间") }, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 0; showDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.medium, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = endTime, onValueChange = {}, readOnly = true, label = { Text("结束时间") }, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 1; showDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.medium, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small)
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                val sleep = if (isEdit) {
                    editEntity.copy(
                        type = selectedType,
                        startTime = startTime.replace(" ", "T") + ":00",
                        endTime = endTime.replace(" ", "T") + ":00",
                        note = note.ifBlank { null },
                    )
                } else {
                    SleepEntity(
                        babyId = babyId,
                        type = selectedType,
                        startTime = startTime.replace(" ", "T") + ":00",
                        endTime = endTime.replace(" ", "T") + ":00",
                        note = note.ifBlank { null },
                    )
                }
                onSave(sleep)
            }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.small) {
                Text(if (isEdit) "更新" else "保存")
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
