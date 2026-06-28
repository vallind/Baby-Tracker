package com.babytracker.feature.health

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.HealthRepository
import com.babytracker.core.data.repository.VaccinationRepository
import kotlinx.coroutines.launch
import com.babytracker.core.domain.model.HealthRecord
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.designsystem.components.topbar.AppTopBar
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// —— 分类元数据：emoji + 标签 + 配色 ——
private data class HealthCategoryMeta(
    val key: String,
    val label: String,
    val emoji: String,
    val bgColor: Color,
)

private val HEALTH_CATEGORIES = listOf(
    HealthCategoryMeta("birth_info", "出生信息", "🍼", Color(0xFF9C27B0)),
    HealthCategoryMeta("allergy", "过敏史", "🤧", Color(0xFFFFC107)),
    HealthCategoryMeta("medicalHistory", "既往病史", "📋", Color(0xFF4CAF50)),
    HealthCategoryMeta("visit", "就诊记录", "🏥", Color(0xFFF44336)),
    HealthCategoryMeta("medication", "用药记录", "💊", Color(0xFF2196F3)),
    HealthCategoryMeta("vaccination", "疫苗接种记录", "💉", Color(0xFFFF9800)),
    HealthCategoryMeta("doctor_note", "医生备注", "📋", Color(0xFFFF5722)),
)

/** 根据分类和已有记录生成摘要副标题。 */
private fun categorySummary(category: String, items: List<HealthRecord>): String {
    if (items.isEmpty()) return "暂无记录"
    val latest = items.maxByOrNull { it.recordDate } ?: return items.first().description
    return when (category) {
        "birth_info" -> latest.recordDate.take(10)
        "allergy", "medicalHistory", "doctor_note" -> latest.description
        "visit", "medication" -> "${items.size}条记录"
        else -> latest.description
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(navController: NavController) {
    val c = LocalAppColors.current
    val healthRepo: HealthRepository = koinInject()
    val vacRepo: VaccinationRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val records by healthRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    val vaccinations by vacRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<HealthRecord?>(null) }
    // 展开/收起某个分类
    var expandedCategory by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val grouped = remember(records) { records.groupBy { it.category } }
    // 已接种疫苗数
    val vaccinatedCount = remember(vaccinations) {
        vaccinations.count { it.status.name == "COMPLETED" || it.status.name == "ADMINISTERED" }
    }

    Scaffold(
        containerColor = c.pageBackground,
        topBar = { AppTopBar(title = "健康档案", onBack = { navController.popBackStack() }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AppFAB(icon = Icons.Default.Add, onClick = { editingRecord = null; showForm = true })
        },
    ) { padding ->
        if (grouped.isEmpty() && vaccinatedCount == 0) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    emoji = "❤️",
                    title = "还没有健康记录",
                    subtitle = "点击右下角按钮，添加宝宝的健康信息",
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).background(c.pageBackground),
                contentPadding = PaddingValues(top = DT.cardGap.dp, bottom = 80.dp),
            ) {
                HEALTH_CATEGORIES.forEach { meta ->
                    item(key = meta.key) {
                        if (meta.key == "vaccination") {
                            // 疫苗接种卡片：读取 Vaccination 数据
                            VaccinationSummaryCard(
                                emoji = meta.emoji,
                                bgColor = meta.bgColor,
                                label = meta.label,
                                count = vaccinatedCount,
                                onClick = { navController.navigate(com.babytracker.navigation.Screen.Vaccination.route) },
                            )
                        } else {
                            val items = grouped[meta.key].orEmpty()
                            val summary = categorySummary(meta.key, items)
                            val isExpanded = expandedCategory == meta.key
                            HealthCategorySummaryCard(
                                emoji = meta.emoji,
                                bgColor = meta.bgColor,
                                label = meta.label,
                                summary = summary,
                                hasItems = items.isNotEmpty(),
                                isExpanded = isExpanded,
                                onClick = {
                                    if (items.isNotEmpty()) {
                                        expandedCategory = if (isExpanded) null else meta.key
                                    }
                                },
                            )
                            // 展开后的记录列表（内嵌在同一卡片下方）
                            AnimatedVisibility(visible = isExpanded && items.isNotEmpty()) {
                                ExpandedCategoryItems(
                                    items = items,
                                    onEdit = { record ->
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
                        }
                    }
                    item { Spacer(Modifier.height(DT.cardGapSm.dp)) }
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
            },
        )
    }
}

// —— 通用分类摘要卡片（紧凑行）——
@Composable
private fun HealthCategorySummaryCard(
    emoji: String,
    bgColor: Color,
    label: String,
    summary: String,
    hasItems: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val c = LocalAppColors.current
    Card(
        Modifier
            .padding(horizontal = DT.pageMargin.dp)
            .fillMaxWidth()
            .clickable(enabled = hasItems, onClick = onClick),
        shape = RoundedCornerShape(DT.cardRadius.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = DT.cardElevation.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 左侧：彩色圆形 emoji
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            // 中间：标题 + 副标题
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(
                    summary,
                    fontSize = 13.sp,
                    color = if (hasItems) c.textSecondary else c.textTertiary,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(8.dp))
            // 右侧箭头
            Text("›", fontSize = 22.sp, color = c.textTertiary)
        }
    }
}

// —— 疫苗接种专用卡片（点击跳转疫苗接种页）——
@Composable
private fun VaccinationSummaryCard(
    emoji: String,
    bgColor: Color,
    label: String,
    count: Int,
    onClick: () -> Unit,
) {
    val c = LocalAppColors.current
    val summary = if (count > 0) "已接种${count}针" else "暂无接种记录"
    Card(
        Modifier
            .padding(horizontal = DT.pageMargin.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(DT.cardRadius.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = DT.cardElevation.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(
                    summary,
                    fontSize = 13.sp,
                    color = if (count > 0) c.textSecondary else c.textTertiary,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text("›", fontSize = 22.sp, color = c.textTertiary)
        }
    }
}

// —— 展开后的记录列表 ——
@Composable
private fun ExpandedCategoryItems(
    items: List<HealthRecord>,
    onEdit: (HealthRecord) -> Unit,
    onDelete: (HealthRecord) -> Unit,
) {
    val c = LocalAppColors.current
    val sorted = remember(items) { items.sortedByDescending { it.recordDate } }
    Column(Modifier.padding(horizontal = DT.pageMargin.dp)) {
        sorted.forEachIndexed { i, r ->
            RecordCard(
                modifier = Modifier.padding(bottom = 12.dp),
                onDelete = { onDelete(r) },
                onClick = { onEdit(r) },
                onLongClick = { onEdit(r) },
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(r.description, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
                        Spacer(Modifier.weight(1f))
                        val dateText = try {
                            DateUtils.formatDate(LocalDateTime.parse(r.recordDate, DateTimeFormatter.ISO_DATE_TIME))
                        } catch (_: Exception) { r.recordDate.take(10) }
                        Text(dateText, fontSize = 12.sp, color = c.textSecondary)
                    }
                    if (!r.doctorName.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text("👨‍⚕️ ${r.doctorName}", fontSize = 12.sp, color = c.textSecondary)
                    }
                    if (!r.note.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(r.note, fontSize = 12.sp, color = c.textSecondary, maxLines = 2)
                    }
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
    var category by remember { mutableStateOf(editEntity?.category ?: "birth_info") }
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
        "birth_info" to "🍼 出生信息",
        "allergy" to "🤧 过敏史",
        "medicalHistory" to "📋 既往病史",
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
