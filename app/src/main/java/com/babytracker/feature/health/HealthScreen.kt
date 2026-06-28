package com.babytracker.feature.health

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.HealthRepository
import kotlinx.coroutines.launch
import com.babytracker.core.domain.model.HealthRecord
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.designsystem.components.topbar.AppTopBar
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val healthCategoryLabels = mapOf("allergy" to "过敏史", "medicalHistory" to "既往病史", "exam" to "体检记录", "note" to "备注", "birth_info" to "出生信息", "visit" to "就诊记录", "medication" to "用药记录", "doctor_note" to "医生备注")
val healthCategoryIcons = mapOf("allergy" to "🤧", "medicalHistory" to "📋", "exam" to "🏥", "note" to "📝", "birth_info" to "🍼", "visit" to "🏥", "medication" to "💊", "doctor_note" to "📋")

// 显示顺序（出生信息 → 过敏 → 既往病史 → 体检 → 就诊 → 用药 → 医生备注 → 备注）
private val healthCategoryOrder = listOf("birth_info", "allergy", "medicalHistory", "exam", "visit", "medication", "doctor_note", "note")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HealthScreen(navController: NavController) {
    val c = LocalAppColors.current
    val healthRepo: HealthRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val records by healthRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<HealthRecord?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(containerColor = c.pageBackground, topBar = {
        AppTopBar(title = "健康档案", onBack = { navController.popBackStack() })
    }, snackbarHost = { SnackbarHost(snackbarHostState) },
    floatingActionButton = {
        AppFAB(icon = Icons.Default.Add, onClick = { editingRecord = null; showForm = true })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(c.pageBackground)) {
            // —— 顶部标题区（浅蓝渐变背景）——
            Box(Modifier.fillMaxWidth().background(Gradients.pageHeader(c)).padding(horizontal = DT.pageMargin.dp, vertical = 20.dp)) {
                Column {
                    Text("健康档案", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("记录过敏、用药、就诊等信息，建立完整健康档案", fontSize = 12.sp, color = c.textSecondary)
                }
            }
            Spacer(Modifier.height(DT.cardGap.dp))

            if (records.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "❤️",
                        title = "还没有健康记录",
                        subtitle = "点击右下角按钮，添加宝宝的健康信息",
                    )
                }
            } else {
                val grouped = remember(records) { records.groupBy { it.category } }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                ) {
                    healthCategoryOrder.forEachIndexed { index, category ->
                        val items = grouped[category] ?: return@forEachIndexed
                        item(key = category) {
                            HealthCategoryCard(
                                category = category,
                                items = items,
                                useAccent = index % 2 == 1,
                                onClick = { record ->
                                    editingRecord = record
                                    showForm = true
                                },
                                onDelete = { record ->
                                    scope.launch {
                                        healthRepo.delete(record)
                                        val result = snackbarHostState.showSnackbar(
                                            message = "已删除「${record.description.take(20)}」",
                                            actionLabel = "撤销",
                                            duration = SnackbarDuration.Short,
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            healthRepo.insert(record)
                                        }
                                    }
                                },
                            )
                        }
                        item { Spacer(Modifier.height(DT.cardGapSm.dp)) }
                    }
                }
            }
        }
    }
    if (showForm) {
        HealthFormDialog(
            babyId = babyId,
            editEntity = editingRecord,
            onDismiss = {
                showForm = false
                editingRecord = null
            },
            onSave = { record ->
                scope.launch {
                    if (editingRecord != null) {
                        healthRepo.update(record)
                    } else {
                        healthRepo.insert(record)
                    }
                    showForm = false
                    editingRecord = null
                }
            }
        )
    }

}

/** 单个分类卡片：标题 + 内容列表。 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun HealthCategoryCard(
    category: String,
    items: List<HealthRecord>,
    useAccent: Boolean,
    onClick: (HealthRecord) -> Unit,
    onDelete: (HealthRecord) -> Unit,
) {
    val c = LocalAppColors.current
    val tint = if (useAccent) c.warning else c.primary
    val cardShape = RoundedCornerShape(DT.cardRadius.dp)
    Card(
        Modifier.padding(horizontal = DT.pageMargin.dp).fillMaxWidth().shadow(elevation = DT.cardElevation.dp, shape = cardShape),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
    ) {
        Column(Modifier.padding(DT.cardInnerPadding.dp)) {
            // —— 分类标题行 ——
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(DT.iconBgSize.dp).clip(RoundedCornerShape(DT.iconBgRadius.dp)).background(tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(healthCategoryIcons[category] ?: "📋", fontSize = DT.iconSize.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    healthCategoryLabels[category] ?: category,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                )
                Spacer(Modifier.weight(1f))
                Text("${items.size} 条", fontSize = 12.sp, color = c.textSecondary)
            }
            Spacer(Modifier.height(8.dp))
            // —— 记录列表 ——
            items.forEachIndexed { i, r ->
                RecordCard(
                    modifier = Modifier.padding(bottom = 16.dp),
                    onDelete = { onDelete(r) },
                    onClick = {},
                    onLongClick = { onClick(r) },
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        Modifier
                            .padding(top = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(tint),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(r.description, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
                        Spacer(Modifier.height(2.dp))
                        val dateText = try {
                            DateUtils.formatDate(LocalDateTime.parse(r.recordDate, DateTimeFormatter.ISO_DATE_TIME))
                        } catch (_: Exception) { r.recordDate.take(10) }
                        Text(dateText, fontSize = 11.sp, color = c.textSecondary)
                        if (!r.doctorName.isNullOrBlank()) {
                            Text("医生：${r.doctorName}", fontSize = 11.sp, color = c.textSecondary)
                        }
                        if (!r.note.isNullOrBlank()) {
                            Text(r.note, fontSize = 12.sp, color = c.textSecondary)
                        }
                    }
                }
                if (i != items.lastIndex) {
                    HorizontalDivider(color = c.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HealthFormDialog(
    babyId: Int,
    editEntity: HealthRecord? = null,
    onDismiss: () -> Unit,
    onSave: (HealthRecord) -> Unit,
) {
    val c = LocalAppColors.current
    val isEdit = editEntity != null
    var category by remember { mutableStateOf(editEntity?.category ?: "allergy") }
    var description by remember { mutableStateOf(editEntity?.description ?: "") }
    var doctorName by remember { mutableStateOf(editEntity?.doctorName ?: "") }
    var recordDate by remember {
        mutableStateOf(
            editEntity?.recordDate?.take(10)
                ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        )
    }
    var note by remember { mutableStateOf(editEntity?.note ?: "") }
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
            Text(if (isEdit) "编辑健康记录" else "添加健康记录", style = MaterialTheme.typography.headlineSmall, color = c.textPrimary)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                categories.forEach { (key, label) ->
                    FilterChip(
                        selected = category == key,
                        onClick = { category = key },
                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = c.primary.copy(alpha = 0.12f),
                            selectedLabelColor = c.primary,
                        ),
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
                    disabledBorderColor = c.outline,
                    disabledTextColor = c.textPrimary,
                    disabledLabelColor = c.textSecondary,
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
                    val record = if (isEdit) {
                        editEntity.copy(
                            category = category,
                            description = description,
                            doctorName = doctorName.ifBlank { null },
                            recordDate = recordDate + "T00:00:00",
                            note = note.ifBlank { null },
                        )
                    } else {
                        HealthRecord(
                            babyId = babyId,
                            category = category,
                            description = description,
                            doctorName = doctorName.ifBlank { null },
                            recordDate = recordDate + "T00:00:00",
                            note = note.ifBlank { null },
                        )
                    }
                    onSave(record)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(DT.buttonRadius.dp),
                enabled = description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = Color.White)
            ) {
                Text(if (isEdit) "更新" else "保存", style = MaterialTheme.typography.titleSmall)
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
