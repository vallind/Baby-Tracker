package com.babytracker.ui.diaper

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.core.database.entity.DiaperEntity
import com.babytracker.core.theme.DT
import com.babytracker.core.theme.LocalThemeColors
import com.babytracker.core.util.DateUtils
import com.babytracker.data.repository.DiaperRepository
import com.babytracker.data.repository.BabyRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaperListScreen(navController: NavController) {
    val diaperRepo: DiaperRepository = koinInject()
    val babyRepo: BabyRepository = koinInject()
    val scope = rememberCoroutineScope()
    val babies by babyRepo.watchAll().collectAsState(initial = emptyList())
    val babyId = babies.firstOrNull()?.id ?: return
    val diapers by diaperRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var deletingDiaper by remember { mutableStateOf<DiaperEntity?>(null) }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("尿布记录") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } })
    }, floatingActionButton = {
        FloatingActionButton(onClick = { showForm = true }, containerColor = MaterialTheme.colorScheme.primary) {
            Icon(Icons.Default.Add, null, tint = Color.White)
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            val grouped = diapers.groupBy { it.timestamp.take(10) }
            grouped.forEach { (date, items) ->
                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 8.dp))
                items.forEach { d ->
                Card(
                    Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 4.dp).fillMaxWidth().combinedClickable(onLongClick = { deletingDiaper = d }, onClick = {}),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🧷", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(DateUtils.diaperTypeLabel(d.type), style = MaterialTheme.typography.titleSmall)
                            Text(try { LocalDateTime.parse(d.timestamp, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) } catch (_: Exception) { "" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showForm) {
        DiaperFormDialog(
            babyId = babyId,
            onDismiss = { showForm = false },
            onSave = { d ->
                scope.launch { diaperRepo.insert(d) }
                showForm = false
            },
        )
    }

    deletingDiaper?.let { d ->
        AlertDialog(
            onDismissRequest = { deletingDiaper = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条尿布记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { diaperRepo.delete(d) }
                    deletingDiaper = null
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingDiaper = null }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaperFormDialog(babyId: Int, onDismiss: () -> Unit, onSave: (DiaperEntity) -> Unit) {
    var selectedType by remember { mutableStateOf("wet") }
    val now = LocalDateTime.now()
    var diaperDateTime by remember { mutableStateOf(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = DT.pageMargin.dp).verticalScroll(rememberScrollState())) {
            Text("记录尿布", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                listOf("wet" to "💧 小便", "poop" to "💩 大便", "both" to "💧💩 混合").forEach { (t, label) ->
                    FilterChip(
                        selected = selectedType == t,
                        onClick = { selectedType = t },
                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = diaperDateTime, onValueChange = {}, readOnly = true, label = { Text("时间") }, modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.small, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注 (可选)") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(DiaperEntity(
                        babyId = babyId,
                        type = selectedType,
                        timestamp = diaperDateTime.replace(" ", "T") + ":00",
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
                    val timePart = diaperDateTime.substring(11)
                    diaperDateTime = "$date $timePart"
                }
                showTimePicker = true
            }) { Text("下一步") }
        }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        var hour by remember { mutableIntStateOf(diaperDateTime.substring(11, 13).toIntOrNull() ?: 12) }
        var minute by remember { mutableIntStateOf(diaperDateTime.substring(14, 16).toIntOrNull() ?: 0) }
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
                val datePart = diaperDateTime.take(10)
                diaperDateTime = "$datePart ${"%02d".format(hour)}:${"%02d".format(minute)}"
                showTimePicker = false
            }) { Text("确定") }
        }, dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } })
    }
}