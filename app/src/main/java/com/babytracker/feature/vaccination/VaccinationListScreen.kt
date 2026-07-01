package com.babytracker.feature.vaccination

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.babytracker.core.domain.model.Vaccination
import com.babytracker.core.domain.model.VaccinationStatus
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.button.PrimaryButton
import com.babytracker.designsystem.components.button.AppTextButton
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.VaccineSchedule
import com.babytracker.core.data.repository.VaccinationRepository
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

// —— 筛选胶囊元数据 ——
private data class FilterPill(val key: String, val label: String)

private val FILTER_PILLS = listOf(
    FilterPill("all", "全部"),
    FilterPill("pending", "待接种"),
    FilterPill("done", "已接种"),
    FilterPill("expired", "已过期"),
)

/** 计算建议月龄文案，如 "12月龄" 或 "1岁2个月"。 */
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

/** PENDING 且 scheduledDate 已过 today → 已过期。 */
private fun isExpired(v: Vaccination): Boolean {
    if (v.status != VaccinationStatus.PENDING) return false
    val sched = v.scheduledDate ?: return false
    return try {
        LocalDate.parse(sched.take(10)).isBefore(LocalDate.now())
    } catch (_: Exception) { false }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationListScreen(navController: NavController) {
    val c = LocalAppColors.current
    val vacRepo: VaccinationRepository = koinInject()
    val babyRepo: BabyRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val vaccinations by vacRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    val babies by babyRepo.watchAll().collectAsState(initial = emptyList())
    val baby = babies.find { it.id == babyId }

    val today = remember { LocalDate.now().toString().take(10) }

    // tab: plan（接种计划）/ done（接种记录）
    var tab by remember { mutableStateOf("plan") }
    // statusFilter: all / pending / done / expired
    var statusFilter by remember { mutableStateOf("all") }

    // 根据 tab + filter 过滤
    val filtered = remember(vaccinations, tab, statusFilter, today) {
        vaccinations.filter { v ->
            val baseOk = when (tab) {
                "plan" -> v.status == VaccinationStatus.PENDING || v.status == VaccinationStatus.SKIPPED
                else -> v.status == VaccinationStatus.DONE
            }
            if (!baseOk) return@filter false
            when (statusFilter) {
                "pending" -> v.status == VaccinationStatus.PENDING && !isExpired(v)
                "done" -> v.status == VaccinationStatus.DONE
                "expired" -> v.status == VaccinationStatus.PENDING && isExpired(v)
                else -> true // all
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
        topBar = {
            AppTopBar(title = "疫苗接种", onBack = { navController.popBackStack() })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        fab = {
            AppFAB(icon = Icons.Default.Add, onClick = { editingVac = null; showForm = true })
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(c.pageBackground),
        ) {
            // —— Segment 标签页（接种计划 / 接种记录）——
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(c.pageBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                listOf("plan" to "接种计划", "done" to "接种记录").forEachIndexed { i, (key, label) ->
                    val active = tab == key
                    Column(
                        Modifier
                            .weight(1f)
                            .clickable {
                                tab = key
                                statusFilter = "all"
                            }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            label,
                            fontSize = 16.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) c.primary else c.textSecondary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier
                                .width(24.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (active) c.primary else Color.Transparent),
                        )
                    }
                }
            }

            // —— 胶囊筛选栏 ——
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FILTER_PILLS.forEach { pill ->
                    val active = statusFilter == pill.key
                    val pillColor = when (pill.key) {
                        "done" -> Color(0xFF4CAF50)
                        "expired" -> Color(0xFFF44336)
                        else -> c.primary
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (active) pillColor else Color.Transparent)
                            .then(
                                if (active) Modifier
                                else Modifier.border(1.dp, c.outline, RoundedCornerShape(999.dp))
                            )
                            .clickable { statusFilter = pill.key }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Text(
                            pill.label,
                            fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (active) Color.White else c.textSecondary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // —— 内容区 ——
            if (filtered.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = if (tab == "plan") "💉" else "✅",
                        title = if (tab == "plan") "暂无接种计划" else "暂无接种记录",
                        subtitle = when {
                            tab == "plan" && vaccinations.none { it.status == VaccinationStatus.PENDING } ->
                                "点击下方按钮生成默认接种计划，或手动添加"
                            statusFilter == "expired" -> "暂无过期疫苗，继续保持 👏"
                            statusFilter == "pending" -> "所有计划疫苗均已按时接种或已过期"
                            else -> ""
                        },
                        actionText = if (tab == "plan" && vaccinations.none { it.status == VaccinationStatus.PENDING }) "生成接种计划" else null,
                        onAction = if (tab == "plan" && vaccinations.none { it.status == VaccinationStatus.PENDING }) ({ showGenerateConfirm = true }) else null,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 80.dp,
                    ),
                ) {
                    items(items = filtered, key = { it.id }) { v ->
                        VaccinationCard(
                            vaccination = v,
                            birthDate = baby?.birthDate ?: "",
                            onClick = {
                                editingVac = v
                                showForm = true
                            },
                            onDelete = {
                                scope.launch {
                                    vacRepo.delete(v)
                                    appSnackbar.showUndo(message = "已删除「${v.name}」") { vacRepo.insert(v) }
                                }
                            },
                        )
                    }
                    if (tab == "plan") {
                        item {
                            Text(
                                "以上计划根据国家免疫规划制定，具体接种时间请遵医嘱。",
                                fontSize = 12.sp,
                                color = c.textTertiary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        VaccinationFormDialog(
            babyId = babyId,
            editEntity = editingVac,
            onSave = { vac ->
                scope.launch {
                    if (editingVac != null) vacRepo.update(vac) else vacRepo.insert(vac)
                    showForm = false
                    editingVac = null
                }
            },
            onDismiss = { showForm = false; editingVac = null },
        )
    }

    if (showGenerateConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showGenerateConfirm = false },
            title = { Text("生成接种计划") },
            text = { Text("将根据宝宝出生日期自动生成 21 条默认接种计划。已存在的记录不会被覆盖。") },
            confirmButton = {
                AppTextButton(onClick = {
                    showGenerateConfirm = false
                    scope.launch {
                        val b = babyRepo.getById(babyId)
                        if (b != null) {
                            VaccineSchedule.createForBaby(babyId, b.birthDate).forEach { vacRepo.insert(it) }
                        }
                    }
                }, label = "生成")
            },
            dismissButton = { AppTextButton(onClick = { showGenerateConfirm = false }, label = "取消") },
        )
    }
}

// —— 疫苗卡片（名称 + 剂次 + 状态标签 / 建议月龄 + 建议日期）——
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VaccinationCard(
    vaccination: Vaccination,
    birthDate: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = LocalAppColors.current
    val ageText = remember(vaccination.scheduledDate, birthDate) {
        suggestedAgeText(vaccination.scheduledDate, birthDate)
    }
    val dateText = remember(vaccination.scheduledDate) {
        vaccination.scheduledDate?.let { s ->
            try {
                val dt = LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME)
                "建议 ${DateUtils.formatDate(dt)}"
            } catch (_: Exception) { s.take(10) }
        } ?: ""
    }

    val isExpired = isExpired(vaccination)

    // 状态标签颜色 & 文案
    val (tagColor, tagLabel) = when {
        vaccination.status == VaccinationStatus.DONE -> c.success to "已接种"
        vaccination.status == VaccinationStatus.SKIPPED -> c.textTertiary to "已跳过"
        isExpired -> Color(0xFFF44336) to "已过期"
        else -> c.warning to "未接种"
    }

    AppCard(
        cornerRadius = 12.dp,
        elevation = 2.dp,
        containerColor = c.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(onClick = onClick, onLongClick = onDelete),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            // Row 1: 名称 + 剂次 + 状态标签
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    vaccination.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                )
                if (!vaccination.dose.isNullOrBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        vaccination.dose,
                        fontSize = 13.sp,
                        color = c.textSecondary,
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(tagColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        tagLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tagColor,
                    )
                }
            }
            // Row 2: 建议月龄 + 建议日期
            if (ageText.isNotBlank() || dateText.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (ageText.isNotBlank()) {
                        Text(ageText, fontSize = 13.sp, color = c.textSecondary)
                    }
                    Spacer(Modifier.weight(1f))
                    if (dateText.isNotBlank()) {
                        Text(dateText, fontSize = 13.sp, color = c.textSecondary)
                    }
                }
            }
        }
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
        Column(Modifier.padding(horizontal = 16.dp, vertical = 0.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text(if (isEdit) "编辑疫苗" else "添加疫苗", style = LocalAppTypography.current.titleMedium, modifier = Modifier.padding(bottom = 16.dp))

            AppInput(value = name, onValueChange = { name = it }, label = "疫苗名称", isError = name.isBlank(), errorMessage = "名称不能为空", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))

            AppInput(value = dose, onValueChange = { dose = it }, label = "剂次 (可选)", placeholder = "第1剂", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))

            Text("状态", style = LocalAppTypography.current.bodySmall, color = c.textSecondary, modifier = Modifier.padding(bottom = 8.dp))
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("pending" to "未接种", "done" to "已接种", "skipped" to "已跳过").forEach { (s, l) ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text(l, style = LocalAppTypography.current.bodySmall) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = c.primary.copy(alpha = 0.12f), selectedLabelColor = c.primary)
                    )
                }
            }

            AppInput(value = scheduledDate, onValueChange = {}, label = "计划接种日期 (可选)", enabled = false, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showScheduledDatePicker = true })

            if (status == "done") {
                AppInput(value = administeredDate, onValueChange = {}, label = "实际接种日期 (可选)", enabled = false, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showAdministeredDatePicker = true })
            }

            AppInput(value = note, onValueChange = { note = it }, label = "备注 (可选)", modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))

            PrimaryButton(
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

    if (showScheduledDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showScheduledDatePicker = false }, confirmButton = {
            AppTextButton(onClick = {
                showScheduledDatePicker = false
                datePickerState.selectedDateMillis?.let { millis ->
                    val instant = java.time.Instant.ofEpochMilli(millis)
                    scheduledDate = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                        .toLocalDate().toString()
                }
            }, label = "确定")
            }, dismissButton = { AppTextButton(onClick = { showScheduledDatePicker = false }, label = "取消") }) {
            DatePicker(state = datePickerState)
        }
    }

    if (showAdministeredDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showAdministeredDatePicker = false }, confirmButton = {
            AppTextButton(onClick = {
                showAdministeredDatePicker = false
                datePickerState.selectedDateMillis?.let { millis ->
                    val instant = java.time.Instant.ofEpochMilli(millis)
                    administeredDate = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                        .toLocalDate().toString()
                }
            }, label = "确定")
            }, dismissButton = { AppTextButton(onClick = { showAdministeredDatePicker = false }, label = "取消") }) {
            DatePicker(state = datePickerState)
        }
    }
}
