package com.babytracker.feature.health

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.designsystem.components.chip.AppFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.draw.rotate
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.HealthRepository
import com.babytracker.core.data.repository.VaccinationRepository
import kotlinx.coroutines.launch
import com.babytracker.core.domain.model.HealthRecord
import com.babytracker.core.domain.model.Vaccination
import com.babytracker.core.domain.model.VaccinationStatus
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.recorddetail.RecordDetailSheet
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private data class HealthCategoryMeta(
    val key: String,
    val label: String,
    val emoji: String,
    val bgColor: Color,
)

internal fun completedVaccinationCount(vaccinations: List<Vaccination>): Int =
    vaccinations.count { it.status == VaccinationStatus.DONE }

private fun healthCategories(c: AppColors) = listOf(
    // 分区色纪律（T3）：除疫苗（琥珀）外全部统一珊瑚分区浅底，类别靠 emoji/文案区分，去彩虹色
    HealthCategoryMeta("birth_info", "出生信息", "🍼", c.danger),
    HealthCategoryMeta("allergy", "过敏史", "🤧", c.danger),
    HealthCategoryMeta("medicalHistory", "既往病史", "📋", c.danger),
    HealthCategoryMeta("visit", "就诊记录", "🏥", c.danger),
    HealthCategoryMeta("medication", "用药记录", "💊", c.danger),
    HealthCategoryMeta("vaccination", "疫苗接种记录", "💉", c.warning),
    HealthCategoryMeta("doctor_note", "医生备注", "📋", c.danger),
)

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
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val healthRepo: HealthRepository = koinInject()
    val vacRepo: VaccinationRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val records by healthRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    val vaccinations by vacRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<HealthRecord?>(null) }
    var detailRecord by remember { mutableStateOf<HealthRecord?>(null) }
    var expandedCategory by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

    val grouped = remember(records) { records.groupBy { it.category } }
    val vaccinatedCount = remember(vaccinations) {
        completedVaccinationCount(vaccinations)
    }

    AppScaffold(
        topBar = { AppTopBar(title = "健康档案", onBack = { navController.popBackStack() }) },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        fab = {
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
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            ) {
                healthCategories(c).forEach { meta ->
                    item(key = meta.key) {
                        if (meta.key == "vaccination") {
                            VaccinationSummaryCard(
                                emoji = meta.emoji,
                                bgColor = meta.bgColor,
                                label = meta.label,
                                count = vaccinatedCount,
                                onClick = { navController.navigate(com.babytracker.navigation.Vaccination) },
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
                            AnimatedVisibility(visible = isExpanded && items.isNotEmpty()) {
                                ExpandedCategoryItems(
                                    items = items,
                                    onDetail = { detailRecord = it },
                                    onEdit = { record ->
                                        editingRecord = record
                                        showForm = true
                                    },
                                    onDelete = { record ->
                                        scope.launch {
                                            healthRepo.delete(record)
                                            appSnackbar.showUndo(message = "已删除「${record.description.take(20)}」") { healthRepo.update(record) }
                                        }
                                    },
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(spacing.sm)) }
                }
            }
        }
    }

    // 单击卡片 = 详情弹层（编辑入口在弹层内）
    detailRecord?.let { r ->
        RecordDetailSheet(
            show = true,
            title = healthCategoryLabel(r.category),
            emoji = r.categoryEmoji(),
            tint = c.danger,
            fields = healthDetailFields(r),
            onEdit = {
                detailRecord = null
                editingRecord = r
                showForm = true
            },
            onDelete = {
                detailRecord = null
                scope.launch {
                    healthRepo.delete(r)
                    appSnackbar.showUndo(message = "已删除「${r.description.take(20)}」") { healthRepo.update(r) }
                }
            },
            onDismiss = { detailRecord = null },
        )
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
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    AppCard(
        containerColor = c.surface,
        modifier = Modifier
            .padding(horizontal = spacing.md)
            .fillMaxWidth()
            .clickable(enabled = hasItems, onClick = onClick),
    ) {
        Row(
            Modifier.padding(horizontal = spacing.md, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppEmojiBadge(emoji = emoji, tint = bgColor)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = typography.titleMedium, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                Spacer(Modifier.height(spacing.xxs))
                Text(
                    summary,
                    style = typography.bodyMedium,
                    color = if (hasItems) c.textSecondary else c.textTertiary,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(spacing.sm))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(if (isExpanded) 90f else 0f),
            )
        }
    }
}

@Composable
private fun VaccinationSummaryCard(
    emoji: String,
    bgColor: Color,
    label: String,
    count: Int,
    onClick: () -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val summary = if (count > 0) "已接种${count}针" else "暂无接种记录"
    AppCard(
        containerColor = c.surface,
        modifier = Modifier
            .padding(horizontal = spacing.md)
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(horizontal = spacing.md, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppEmojiBadge(emoji = emoji, tint = bgColor)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = typography.titleMedium, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                Spacer(Modifier.height(spacing.xxs))
                Text(
                    summary,
                    style = typography.bodyMedium,
                    color = if (count > 0) c.textSecondary else c.textTertiary,
                )
            }
            Spacer(Modifier.width(spacing.sm))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(90f), // 疫苗卡固定指向（点击即跳疫苗页）
            )
        }
    }
}

@Composable
private fun ExpandedCategoryItems(
    items: List<HealthRecord>,
    onEdit: (HealthRecord) -> Unit,
    onDelete: (HealthRecord) -> Unit,
    onDetail: (HealthRecord) -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val sorted = remember(items) { items.sortedByDescending { it.recordDate } }
    Column(Modifier.padding(horizontal = spacing.md)) {
        sorted.forEachIndexed { i, r ->
            RecordCard(
                modifier = Modifier.padding(bottom = 12.dp),
                onDelete = { onDelete(r) },
                onClick = { onDetail(r) },
                onLongClick = { onEdit(r) },
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(r.description, style = typography.bodyLarge, fontWeight = FontWeight.Medium, color = c.textPrimary)
                        Spacer(Modifier.weight(1f))
                        val dateText = try {
                            DateUtils.formatDate(LocalDateTime.parse(r.recordDate, DateTimeFormatter.ISO_DATE_TIME))
                        } catch (_: Exception) { r.recordDate.take(10) }
                        Text(dateText, style = typography.labelMedium, color = c.textSecondary)
                    }
                    if (!r.doctorName.isNullOrBlank()) {
                        Spacer(Modifier.height(spacing.xs))
                        Text("👨‍⚕️ ${r.doctorName}", style = typography.labelMedium, color = c.textSecondary)
                    }
                    if (!r.note.isNullOrBlank()) {
                        Spacer(Modifier.height(spacing.xxs))
                        Text(r.note, style = typography.labelMedium, color = c.textSecondary, maxLines = 2)
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
    val spacing = LocalAppSpacing.current
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

    val buildEntity = {
        if (isEdit) {
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
    }

    AppFormSheet(
        title = if (isEdit) "编辑健康记录" else "添加健康记录",
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) "更新" else "保存",
        saveEnabled = description.isNotBlank(),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            categories.forEach { (key, label) ->
                AppFilterChip(
                    selected = category == key,
                    onClick = { category = key },
                    label = label,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(spacing.md))
        AppInput(
            value = description,
            onValueChange = { description = it },
            label = "描述",
            isError = description.isBlank(),
            errorMessage = "描述不能为空",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        AppInput(
            value = doctorName,
            onValueChange = { doctorName = it },
            label = "医生 (可选)",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        AppInput(
            value = recordDate,
            onValueChange = {},
            label = "记录日期",
            enabled = false,
            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
        )
        Spacer(Modifier.height(12.dp))
        AppInput(
            value = note,
            onValueChange = { note = it },
            label = "备注 (可选)",
            modifier = Modifier.fillMaxWidth(),
        )
    }

    DateTimeCascadeDialog(
        show = showDatePicker,
        initialDateTime = "$recordDate 00:00",
        dateOnly = true,
        onConfirm = { dt ->
            recordDate = dt.take(10)
            showDatePicker = false
        },
        onDismiss = { showDatePicker = false },
    )
}


/** 健康分类中文名（与 healthCategories 文案一致，供详情弹层标题复用） */
private fun healthCategoryLabel(category: String): String = when (category) {
    "birth_info" -> "出生信息"
    "allergy" -> "过敏史"
    "medicalHistory" -> "既往病史"
    "visit" -> "就诊记录"
    "medication" -> "用药记录"
    "vaccination" -> "疫苗接种记录"
    "doctor_note" -> "医生备注"
    else -> "健康记录"
}

private fun HealthRecord.categoryEmoji(): String = when (category) {
    "birth_info" -> "🍼"
    "allergy" -> "🤧"
    "medicalHistory" -> "📋"
    "visit" -> "🏥"
    "medication" -> "💊"
    "vaccination" -> "💉"
    "doctor_note" -> "📋"
    else -> "❤️"
}

/** 健康记录详情字段 */
private fun healthDetailFields(r: HealthRecord): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    list += "日期" to r.recordDate.take(10)
    list += "描述" to r.description
    r.doctorName?.takeIf { it.isNotBlank() }?.let { list += "医生" to it }
    r.note?.takeIf { it.isNotBlank() }?.let { list += "备注" to it }
    return list
}
