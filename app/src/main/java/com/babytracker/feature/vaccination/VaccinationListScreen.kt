package com.babytracker.feature.vaccination

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.domain.model.Vaccination
import com.babytracker.core.domain.model.VaccinationStatus
import com.babytracker.core.util.DateUtils
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.SegmentedControl
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.chip.AppFilterChip
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.tintContainer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private data class FilterPill(val key: String, val label: String)

private val FILTER_PILLS = listOf(
    FilterPill("all", AppStrings.filterAll),
    FilterPill("pending", AppStrings.vaccineUpcoming),
    FilterPill("done", AppStrings.vaccineDoneTab),
    FilterPill("expired", AppStrings.expired),
)

private fun suggestedAgeText(scheduledDate: String?, birthDate: String): String {
    if (scheduledDate == null) return ""
    return try {
        val sched = LocalDate.parse(scheduledDate.take(10))
        val birth = LocalDate.parse(birthDate.take(10))
        val m = ((sched.year - birth.year) * 12L + (sched.monthValue - birth.monthValue))
        if (m <= 0) "出生时"
        else if (m < 12) "${m}月龄"
        else "${m / 12}岁${m % 12}个月"
    } catch (_: Exception) { "" }
}

private fun isExpired(v: Vaccination): Boolean {
    if (v.status != VaccinationStatus.PENDING) return false
    val sched = v.scheduledDate ?: return false
    return try {
        LocalDate.parse(sched.take(10)).isBefore(LocalDate.now())
    } catch (_: Exception) { false }
}

/**
 * 疫苗接种记录 — 纯 UI 渲染层：只收 state + baby + 命名回调，
 * 不接触导航 / Koin / Repository / Controller。
 *
 * - Tab/状态筛选、表单/确认弹层显隐、Snackbar 撤销均为 UI 临时状态，留在本地 remember；
 * - 删除 / 撤销 / 保存 / 生成计划走命名回调（Route 映射到 ViewModel）；
 * - 无 bottomBar（本屏为 AppScaffold 独立页，不挂底部导航）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationListScreen(
    state: VaccinationUiState,
    baby: Baby?,
    onBack: () -> Unit,
    onDelete: (Vaccination) -> Unit,
    onRestore: (Vaccination) -> Unit,
    onSave: (Vaccination, Boolean) -> Unit,
    onGenerateSchedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current

    var tab by remember { mutableStateOf("plan") }
    var statusFilter by remember { mutableStateOf("all") }

    val today = remember { LocalDate.now().toString().take(10) }

    val filtered = remember(state.vaccinations, tab, statusFilter, today) {
        state.vaccinations.filter { v ->
            val baseOk = when (tab) {
                "plan" -> v.status == VaccinationStatus.PENDING || v.status == VaccinationStatus.SKIPPED
                else -> v.status == VaccinationStatus.DONE
            }
            if (!baseOk) return@filter false
            when (statusFilter) {
                "pending" -> v.status == VaccinationStatus.PENDING && !isExpired(v)
                "done" -> v.status == VaccinationStatus.DONE
                "expired" -> v.status == VaccinationStatus.PENDING && isExpired(v)
                else -> true
            }
        }
    }

    var showForm by remember { mutableStateOf(false) }
    var editingVac by remember { mutableStateOf<Vaccination?>(null) }
    var showGenerateConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

    AppScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(title = "疫苗接种", onBack = onBack)
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        fab = {
            AppFAB(icon = Icons.Default.Add, onClick = { editingVac = null; showForm = true })
        },
    ) { padding ->
        if (baby == null) {
            EmptyState(
                emoji = "\uD83D\uDC89",
                title = AppStrings.noBabyTitle,
                subtitle = AppStrings.noBabySubtitle,
                modifier = Modifier.padding(padding),
            )
            return@AppScaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(c.pageBackground),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
            ) {
                SegmentedControl(
                    labels = listOf(AppStrings.vaccinePlan, AppStrings.vaccineRecordsTab),
                    selectedIndex = if (tab == "plan") 0 else 1,
                    onSelect = { idx ->
                        tab = if (idx == 0) "plan" else "done"
                        statusFilter = "all"
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // 状态筛选双 Tab 均展示四枚（全部/待接种/已接种/已过期），点击即切换不取消
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md)
                    .padding(bottom = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                FILTER_PILLS.forEach { pill ->
                    val active = statusFilter == pill.key
                    val pillColor = when (pill.key) {
                        "done" -> c.success
                        "expired" -> c.error
                        else -> c.primary
                    }
                    AppFilterChip(
                        selected = active,
                        onClick = { statusFilter = pill.key },
                        label = pill.label,
                        selectedColor = pillColor,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(spacing.xs))

            if (filtered.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = if (tab == "plan") "\uD83D\uDC89" else "\u2705",
                        title = if (tab == "plan") "暂无接种计划" else "暂无接种记录",
                        subtitle = when {
                            tab == "plan" && state.vaccinations.none { it.status == VaccinationStatus.PENDING } ->
                                "点击下方按钮生成默认接种计划，或手动添加"
                            statusFilter == "expired" -> "暂无过期疫苗，继续保持 \uD83D\uDC4F"
                            statusFilter == "pending" -> "所有计划疫苗均已按时接种或已过期"
                            else -> ""
                        },
                        actionText = if (tab == "plan" && state.vaccinations.none { it.status == VaccinationStatus.PENDING }) "生成接种计划" else null,
                        onAction = if (tab == "plan" && state.vaccinations.none { it.status == VaccinationStatus.PENDING }) ({ showGenerateConfirm = true }) else null,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(
                        start = spacing.md,
                        end = spacing.md,
                        bottom = 80.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    items(items = filtered, key = { it.id }) { v ->
                        // 接种记录行：滑动删除 + 名称/剂次 + 状态胶囊 + 月龄/建议日期（G3 收编为调用点内联组合）
                        val shapes = LocalAppShapes.current
                        val ageText = remember(v.scheduledDate, baby.birthDate) {
                            suggestedAgeText(v.scheduledDate, baby.birthDate)
                        }
                        val dateText = remember(v.scheduledDate) {
                            v.scheduledDate?.let { s ->
                                try {
                                    val dt = LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME)
                                    "建议 ${DateUtils.formatDate(dt)}"
                                } catch (_: Exception) { s.take(10) }
                            } ?: ""
                        }

                        val isExpired = isExpired(v)

                        val (tagColor, tagLabel) = when {
                            v.status == VaccinationStatus.DONE -> c.success to "已接种"
                            v.status == VaccinationStatus.SKIPPED -> c.textTertiary to "已跳过"
                            isExpired -> c.error to "已过期"
                            else -> c.warning to "未接种"
                        }

                        RecordCard(
                            onDelete = {
                                onDelete(v)
                                scope.launch {
                                    appSnackbar.showUndo(message = "已删除\u300C${v.name}\u300D") { onRestore(v) }
                                }
                            },
                            onClick = {
                                editingVac = v
                                showForm = true
                            },
                            onLongClick = {
                                editingVac = v
                                showForm = true
                            },
                        ) {
                            Column(Modifier.padding(start = spacing.sm)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        v.name,
                                        style = LocalAppTypography.current.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = c.textPrimary,
                                    )
                                    if (!v.dose.isNullOrBlank()) {
                                        Spacer(Modifier.width(spacing.sm))
                                        Text(
                                            v.dose,
                                            style = LocalAppTypography.current.bodyMedium,
                                            color = c.textSecondary,
                                        )
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(shapes.full))
                                            .background(AppColorScale.fromSeed(tagColor).tintContainer(c))
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                    ) {
                                        Text(
                                            tagLabel,
                                            style = LocalAppTypography.current.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = tagColor,
                                        )
                                    }
                                }
                                if (ageText.isNotBlank() || dateText.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (ageText.isNotBlank()) {
                                            Text(ageText, style = LocalAppTypography.current.bodyMedium, color = c.textSecondary)
                                        }
                                        Spacer(Modifier.weight(1f))
                                        if (dateText.isNotBlank()) {
                                            Text(dateText, style = LocalAppTypography.current.bodyMedium, color = c.textSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (tab == "plan") {
                        item {
                            Text(
                                "以上计划根据国家免疫规划制定，具体接种时间请遵医嘱。",
                                style = LocalAppTypography.current.labelMedium,
                                color = c.textTertiary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = spacing.md),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        VaccinationFormDialog(
            babyId = state.babyId,
            editEntity = editingVac,
            onSave = { vac ->
                onSave(vac, editingVac != null)
                showForm = false
                editingVac = null
            },
            onDismiss = { showForm = false; editingVac = null },
        )
    }

    if (showGenerateConfirm) {
        AppConfirmDialog(
            show = true,
            title = "生成接种计划",
            message = "将根据宝宝出生日期自动生成 21 条默认接种计划。已存在的记录不会被覆盖。",
            confirmText = "生成",
            cancelText = "取消",
            onConfirm = {
                showGenerateConfirm = false
                onGenerateSchedule()
            },
            onDismiss = { showGenerateConfirm = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VaccinationFormDialog(
    babyId: Int,
    editEntity: Vaccination? = null,
    onSave: (Vaccination) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val isEdit = editEntity != null
    var name by remember { mutableStateOf(editEntity?.name ?: "") }
    var dose by remember { mutableStateOf(editEntity?.dose ?: "") }
    var status by remember { mutableStateOf(editEntity?.let { VaccinationStatus.raw(it.status) } ?: "pending") }
    var scheduledDate by remember {
        mutableStateOf(
            editEntity?.scheduledDate?.take(10)
                ?: LocalDate.now().toString()
        )
    }
    var administeredDate by remember {
        mutableStateOf(
            editEntity?.administeredDate?.take(10) ?: ""
        )
    }
    var note by remember { mutableStateOf(editEntity?.note ?: "") }
    var showScheduledDatePicker by remember { mutableStateOf(false) }
    var showAdministeredDatePicker by remember { mutableStateOf(false) }

    AppBottomSheet(
        show = true,
        onDismiss = onDismiss,
    ) {
        Column(Modifier.padding(horizontal = spacing.md, vertical = 0.dp).padding(bottom = spacing.xl).verticalScroll(rememberScrollState())) {
            Text(if (isEdit) "编辑疫苗" else "添加疫苗", style = LocalAppTypography.current.titleMedium, modifier = Modifier.padding(bottom = spacing.md))

            AppInput(value = name, onValueChange = { name = it }, label = "疫苗名称", isError = name.isBlank(), errorMessage = "名称不能为空", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))

            AppInput(value = dose, onValueChange = { dose = it }, label = "剂次 (可选)", placeholder = "第1剂", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))

            Text("状态", style = LocalAppTypography.current.bodySmall, color = c.textSecondary, modifier = Modifier.padding(bottom = spacing.sm))
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                listOf("pending" to "未接种", "done" to "已接种", "skipped" to "已跳过").forEach { (s, l) ->
                    AppFilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = l,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            AppInput(value = scheduledDate, onValueChange = {}, label = "计划接种日期 (可选)", enabled = false, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showScheduledDatePicker = true })

            if (status == "done") {
                AppInput(value = administeredDate, onValueChange = {}, label = "实际接种日期 (可选)", enabled = false, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showAdministeredDatePicker = true })
            }

            AppInput(value = note, onValueChange = { note = it }, label = "备注 (可选)", modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))

            AppButton(
                onClick = {
                    val scheduledDateTime = if (scheduledDate.isNotBlank()) "${scheduledDate}T00:00:00" else null
                    val administeredDateTime = if (administeredDate.isNotBlank()) "${administeredDate}T00:00:00" else null
                    val vac = if (isEdit) {
                        editEntity.copy(
                            name = name,
                            dose = dose.ifBlank { null },
                            scheduledDate = scheduledDateTime,
                            administeredDate = administeredDateTime,
                            status = VaccinationStatus.fromRaw(status),
                            note = note.ifBlank { null }
                        )
                    } else {
                        Vaccination(
                            babyId = babyId,
                            name = name,
                            dose = dose.ifBlank { null },
                            scheduledDate = scheduledDateTime,
                            administeredDate = administeredDateTime,
                            status = VaccinationStatus.fromRaw(status),
                            note = note.ifBlank { null }
                        )
                    }
                    onSave(vac)
                },
                label = if (isEdit) "更新" else "保存",
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    DateTimeCascadeDialog(
        show = showScheduledDatePicker,
        initialDateTime = scheduledDate.ifBlank { LocalDate.now().toString() } + " 00:00",
        dateOnly = true,
        onConfirm = { dt ->
            scheduledDate = dt.take(10)
            showScheduledDatePicker = false
        },
        onDismiss = { showScheduledDatePicker = false },
    )

    DateTimeCascadeDialog(
        show = showAdministeredDatePicker,
        initialDateTime = administeredDate.ifBlank { LocalDate.now().toString() } + " 00:00",
        dateOnly = true,
        onConfirm = { dt ->
            administeredDate = dt.take(10)
            showAdministeredDatePicker = false
        },
        onDismiss = { showAdministeredDatePicker = false },
    )
}