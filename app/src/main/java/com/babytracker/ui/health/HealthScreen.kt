package com.babytracker.ui.health

import androidx.compose.foundation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.core.theme.DT
import com.babytracker.core.theme.LocalThemeColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.data.repository.HealthRepository

import kotlinx.coroutines.launch
import com.babytracker.domain.model.HealthRecord
import org.koin.compose.koinInject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val healthCategoryLabels = mapOf("allergy" to "过敏史", "medicalHistory" to "既往病史", "exam" to "体检记录", "note" to "备注", "birth_info" to "出生信息", "visit" to "就诊记录", "medication" to "用药记录", "doctor_note" to "医生备注")
val healthCategoryIcons = mapOf("allergy" to "🤧", "medicalHistory" to "📋", "exam" to "🏥", "note" to "📝", "birth_info" to "🍼", "visit" to "🏥", "medication" to "💊", "doctor_note" to "📋")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HealthScreen(navController: NavController) {
    val healthRepo: HealthRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val records by healthRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var deletingRecord by remember { mutableStateOf<HealthRecord?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("健康档案") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } })
    }, floatingActionButton = {
        FloatingActionButton(onClick = { showForm = true }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
            Icon(Icons.Default.Add, null)
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            records.forEach { r ->
                Card(
                    Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 4.dp).fillMaxWidth().combinedClickable(onLongClick = { deletingRecord = r }, onClick = {}),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 68.dp).padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(healthCategoryIcons[r.category] ?: "📋", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(r.description, fontWeight = FontWeight.Medium, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                        Text("${healthCategoryLabels[r.category] ?: r.category} · ${r.recordDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    }
    if (showForm) {
        HealthFormDialog(
            babyId = babyId,
            onDismiss = { showForm = false },
            onSave = { record ->
                scope.launch {
                    healthRepo.insert(record)
                    showForm = false
                }
            }
        )
    }

    deletingRecord?.let { r ->
        AlertDialog(
            onDismissRequest = { deletingRecord = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条健康记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { healthRepo.delete(r) }
                    deletingRecord = null
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingRecord = null }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthFormDialog(
    babyId: Int,
    onDismiss: () -> Unit,
    onSave: (HealthRecord) -> Unit,
) {
    var category by remember { mutableStateOf("allergy") }
    var description by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf("") }
    var recordDate by remember { mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories = listOf(
        "allergy" to "🤧 过敏史",
        "medicalHistory" to "📋 既往病史",
        "exam" to "🏥 体检记录",
        "note" to "📝 备注",
        "birth_info" to "🍼 出生信息",
        "visit" to "🏥 就诊记录",
        "medication" to "💊 用药记录",
        "doctor_note" to "📋 医生备注",
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = DT.pageMargin.dp).padding(bottom = 32.dp)) {
            Text("添加健康记录", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                categories.forEach { (key, label) ->
                    FilterChip(
                        selected = category == key,
                        onClick = { category = key },
                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("描述") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = doctorName,
                onValueChange = { doctorName = it },
                label = { Text("医生 (可选)") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = recordDate, onValueChange = {}, readOnly = true,
                label = { Text("记录日期") },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                shape = MaterialTheme.shapes.small, enabled = false,
                colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注 (可选)") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(HealthRecord(babyId = babyId, category = category, description = description, doctorName = doctorName.ifBlank { null }, recordDate = LocalDate.parse(recordDate), note = note.ifBlank { null }))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text("保存", style = MaterialTheme.typography.titleSmall)
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    recordDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }
                showDatePicker = false
            }) { Text("确定") }
        }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }) {
            DatePicker(state = datePickerState)
        }
    }
}