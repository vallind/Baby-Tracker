package com.babytracker.feature.health

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.designsystem.components.chip.AppOptionChipRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.rotate
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.core.util.DateUtils
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
import com.babytracker.designsystem.components.datetimecascade.AppDateTimeField
import com.babytracker.designsystem.components.recorddetail.RecordDetailSheet
import com.babytracker.designsystem.i18n.AppStrings
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

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

/**
 * 健康档案 — 纯 UI 渲染层：只收 state + 命名回调，不接触导航 / Koin / Repository / Controller。
 *
 * - 卡片展开/表单显隐/详情弹层/Snackbar 撤销均为 UI 临时状态，留在本地 remember；
 * - 删除 / 撤销 / 保存 / 疫苗页跳转走命名回调（Route 映射到 ViewModel）；
 * - 无 bottomBar（本屏为 AppScaffold 独立页，不挂底部导航）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    state: HealthUiState,
    onBack: () -> Unit,
    onOpenVaccination: () -> Unit,
    onDelete: (HealthRecord) -> Unit,
    onRestore: (HealthRecord) -> Unit,
    onSave: (HealthRecord, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    var showForm by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<HealthRecord?>(null) }
    var detailRecord by remember { mutableStateOf<HealthRecord?>(null) }
    var expandedCategory by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

    val grouped = remember(state.records) { state.records.groupBy { it.category } }
    val vaccinatedCount = remember(state.vaccinations) {
        completedVaccinationCount(state.vaccinations)
    }

    AppScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = "健康档案", onBack = onBack) },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        fab = {
            AppFAB(icon = Icons.Default.Add, onClick = { editingRecord = null; showForm = true })
        },
    ) { padding ->
        if (state.babyId == 0) {
            EmptyState(
                emoji = "❤️",
                title = AppStrings.noBabyTitle,
                subtitle = AppStrings.noBabySubtitle,
                modifier = Modifier.padding(padding),
            )
            return@AppScaffold
        }
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
                            // 疫苗入口行：徽章 + 标题 + 接种摘要 + 固定右箭头（G3 收编为调用点内联组合）
                            val vacSummary = if (vaccinatedCount > 0) "已接种${vaccinatedCount}针" else "暂无接种记录"
                            AppCard(
                                modifier = Modifier
                                    .padding(horizontal = spacing.md)
                                    .fillMaxWidth(),
                                onClick = onOpenVaccination,
                            ) {
                                Row(
                                    Modifier.padding(horizontal = spacing.md, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AppEmojiBadge(emoji = meta.emoji, tint = meta.bgColor)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(meta.label, style = typography.titleMedium, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                                        Spacer(Modifier.height(spacing.xxs))
                                        Text(
                                            vacSummary,
                                            style = typography.bodyMedium,
                                            color = if (vaccinatedCount > 0) c.textSecondary else c.textTertiary,
                                        )
                                    }
                                    Spacer(Modifier.width(spacing.sm))
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = c.textTertiary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .rotate(90f), // 疫苗行固定指向（点击即跳疫苗页）
                                    )
                                }
                            }
                        } else {
                            val items = grouped[meta.key].orEmpty()
                            val summary = categorySummary(meta.key, items)
                            val isExpanded = expandedCategory == meta.key
                            // 分类摘要行：徽章 + 标题 + 摘要 + 展开箭头，点击展开该类记录（G3 收编为调用点内联组合）
                            AppCard(
                                modifier = Modifier
                                    .padding(horizontal = spacing.md)
                                    .fillMaxWidth(),
                                onClick = {
                                    if (items.isNotEmpty()) {
                                        expandedCategory = if (isExpanded) null else meta.key
                                    }
                                },
                                enabled = items.isNotEmpty(),
                            ) {
                                Row(
                                    Modifier.padding(horizontal = spacing.md, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AppEmojiBadge(emoji = meta.emoji, tint = meta.bgColor)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(meta.label, style = typography.titleMedium, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                                        Spacer(Modifier.height(spacing.xxs))
                                        Text(
                                            summary,
                                            style = typography.bodyMedium,
                                            color = if (items.isNotEmpty()) c.textSecondary else c.textTertiary,
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
                            AnimatedVisibility(visible = isExpanded && items.isNotEmpty()) {
                                ExpandedCategoryItems(
                                    items = items,
                                    onDetail = { detailRecord = it },
                                    onEdit = { record ->
                                        editingRecord = record
                                        showForm = true
                                    },
                                    onDelete = { record ->
                                        onDelete(record)
                                        scope.launch {
                                            appSnackbar.showUndo(message = "已删除「${record.description.take(20)}」") { onRestore(record) }
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
                onDelete(r)
                scope.launch {
                    appSnackbar.showUndo(message = "已删除「${r.description.take(20)}」") { onRestore(r) }
                }
            },
            onDismiss = { detailRecord = null },
        )
    }

    if (showForm) {
        HealthFormDialog(
            babyId = state.babyId,
            editEntity = editingRecord,
            onDismiss = {
                showForm = false
                editingRecord = null
            },
            onSave = { record ->
                onSave(record, editingRecord != null)
                showForm = false
                editingRecord = null
            },
        )
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
        AppOptionChipRow(
            options = categories,
            selectedKey = category,
            onSelect = { category = it },
        )
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
        AppDateTimeField(
            label = "记录日期",
            value = recordDate,
            onPick = { recordDate = it },
            dateOnly = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        AppInput(
            value = note,
            onValueChange = { note = it },
            label = "备注 (可选)",
            modifier = Modifier.fillMaxWidth(),
        )
    }
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