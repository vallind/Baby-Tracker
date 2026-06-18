package com.babytracker.ui.health

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.babytracker.core.database.entity.HealthRecordEntity
import com.babytracker.ui.components.rememberHaptic
import com.babytracker.ui.components.longPressDeletable
import com.babytracker.ui.components.EmptyState
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val healthCategoryLabels = mapOf("allergy" to "过敏史", "medicalHistory" to "既往病史", "exam" to "体检记录", "note" to "备注", "birth_info" to "出生信息", "visit" to "就诊记录", "medication" to "用药记录", "doctor_note" to "医生备注")
val healthCategoryIcons = mapOf("allergy" to "🤧", "medicalHistory" to "📋", "exam" to "🏥", "note" to "📝", "birth_info" to "🍼", "visit" to "🏥", "medication" to "💊", "doctor_note" to "📋")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HealthScreen(navController: NavController) {
    val healthRepo: HealthRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val haptic = rememberHaptic()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val records by healthRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var deletingRecord by remember { mutableStateOf<HealthRecordEntity?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("健康档案") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ))
    }, floatingActionButton = {
        FloatingActionButton(onClick = { showForm = true }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
            Icon(Icons.Default.Add, null)
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            if (records.isEmpty()) {
                EmptyState(
                    emoji = "❤️",
                    title = "还没有健康记录",
                    subtitle = "记录过敏、用药、就诊等信息，建立完整健康档案",
                )
            }
            records.forEach { r ->
                Card(
                    Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 4.dp).fillMaxWidth().longPressDeletable(haptic) { deletingRecord = r },
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 68.dp).padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(healthCategoryIcons[r.category] ?: "📋", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(r.description, fontWeight = FontWeight.Medium, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                            Text("${healthCategoryLabels[r.category] ?: r.category} · ${DateUtils.formatDate(LocalDateTime.parse(r.recordDate, DateTimeFormatter.ISO_DATE_TIME))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onSave: (HealthRecordEntity) -> Unit,
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
        Column(Modifier.padding(horizontal = DT.pageMargin.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
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
                isError = description.isBlank(),
                supportingText = { if (description.isBlank()) Text("描述不能为空") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = doctorName,
                onValueChange = { doctorName = it },
                label = { Text("医生 (可选)") },
                leadingIcon = { Text("👨‍⚕️", style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = recordDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("记录日期") },
                leadingIcon = { Text("📅", style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                shape = MaterialTheme.shapes.medium,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注 (可选)") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(HealthRecordEntity(
                        babyId = babyId,
                        category = category,
                        description = description,
                        doctorName = doctorName.ifBlank { null },
                        recordDate = recordDate + "T00:00:00",
                        note = note.ifBlank { null },
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = description.isNotBlank(),
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
                showDatePicker = false
                datePickerState.selectedDateMillis?.let { millis ->
                    val instant = java.time.Instant.ofEpochMilli(millis)
                    recordDate = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }
            }) { Text("确定") }
        }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }) {
            DatePicker(state = datePickerState)
        }
    }
}