package com.babytracker.ui.vaccination

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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.core.database.entity.VaccinationEntity
import com.babytracker.core.theme.DT
import com.babytracker.core.theme.Gradients
import com.babytracker.core.theme.LocalThemeColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.VaccineSchedule
import com.babytracker.data.repository.VaccinationRepository
import com.babytracker.data.repository.BabyRepository
import com.babytracker.ui.components.rememberHaptic
import com.babytracker.ui.components.SwipeToDeleteContainer
import com.babytracker.ui.components.EmptyState
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VaccinationListScreen(navController: NavController) {
    val c = LocalThemeColors.current
    val vacRepo: VaccinationRepository = koinInject()
    val babyRepo: BabyRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val haptic = rememberHaptic()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val vaccinations by vacRepo.watchByBaby(babyId).collectAsState(initial = emptyList())

    var filter by remember { mutableStateOf("pending") }
    var showForm by remember { mutableStateOf(false) }
    var editingVac by remember { mutableStateOf<VaccinationEntity?>(null) }
    var deletingVac by remember { mutableStateOf<VaccinationEntity?>(null) }
    var showGenerateConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(containerColor = c.bg, topBar = {
        CenterAlignedTopAppBar(title = { Text("疫苗接种", fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = c.primaryLight,
                titleContentColor = c.textPrimary,
                navigationIconContentColor = c.textPrimary,
            ))
    }, snackbarHost = { SnackbarHost(snackbarHostState) },
    floatingActionButton = {
        FloatingActionButton(onClick = {
            editingVac = null
            showForm = true
        }, containerColor = c.primary, contentColor = Color.White) {
            Icon(Icons.Default.Add, contentDescription = "添加")
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).background(c.bg)) {
            // —— 顶部 Tab 区 ——
            Box(Modifier.fillMaxWidth().background(Gradients.pageHeader(c)).padding(horizontal = DT.pageMargin.dp, vertical = 12.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    listOf("pending" to "接种计划", "done" to "接种记录").forEach { (s, l) ->
                        Column(Modifier.weight(1f).clickable { filter = s }.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(l, color = if (filter == s) c.primary else c.textSecondary, fontWeight = if (filter == s) FontWeight.SemiBold else null, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(6.dp))
                            Box(Modifier.width(24.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(if (filter == s) c.primary else Color.Transparent))
                        }
                    }
                }
            }
            Spacer(Modifier.height(DT.cardGap.dp))
            val filtered = vaccinations.filter { it.status == filter }
            if (filtered.isEmpty()) {
                EmptyState(
                    emoji = if (filter == "pending") "💉" else "✅",
                    title = if (filter == "pending") "暂无接种计划" else "暂无接种记录",
                    subtitle = if (filter == "pending") "点击下方按钮生成默认接种计划，或手动添加" else "完成接种后，状态会自动切换到此处",
                    actionText = if (filter == "pending") "生成接种计划" else null,
                    onAction = if (filter == "pending") ({ showGenerateConfirm = true }) else null,
                )
            }
            filtered.forEach { v ->
                    val itemShape = RoundedCornerShape(DT.cardRadius.dp)
                    SwipeToDeleteContainer(
                        onDelete = {
                            scope.launch {
                                val deleted = v
                                vacRepo.delete(deleted)
                                val result = snackbarHostState.showSnackbar(
                                    message = "已删除「${deleted.name}」",
                                    actionLabel = "撤销",
                                    duration = SnackbarDuration.Short,
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    vacRepo.insert(deleted)
                                }
                            }
                        },
                    ) {
                        Card(
                            Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 4.dp).fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        editingVac = v
                                        showForm = true
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        deletingVac = v
                                    },
                                )
                                .shadow(elevation = DT.cardElevation.dp, shape = itemShape),
                            shape = itemShape,
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            colors = CardDefaults.cardColors(containerColor = c.card),
                        ) {
                        Row(Modifier.padding(DT.cardInnerPadding.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(DT.iconBgSize.dp).clip(RoundedCornerShape(DT.iconBgRadius.dp)).background(if (v.status == "done") c.success.copy(alpha = 0.14f) else c.accent.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) { Text(if (v.status == "done") "✅" else "💉", style = MaterialTheme.typography.titleLarge) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(v.name, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                Text("${v.dose ?: ""}${if (v.scheduledDate != null) " · ${DateUtils.formatDate(LocalDateTime.parse(v.scheduledDate, DateTimeFormatter.ISO_DATE_TIME))}" else ""}", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            }
                            val tagColor = if (v.status == "done") c.success else c.accent
                            Box(Modifier.background(tagColor.copy(alpha = 0.12f), RoundedCornerShape(DT.chipRadius.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                Text(if (v.status == "done") "已接种" else if (v.status == "pending") "未接种" else "已跳过", style = MaterialTheme.typography.labelSmall, color = tagColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    }
                }
            if (filter == "pending") {
                Spacer(Modifier.height(8.dp))
                Text(
                    "以上计划根据国家免疫规划制定，具体接种时间请遵医嘱。",
                    fontSize = 11.sp,
                    color = c.textSecondary,
                    modifier = Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 12.dp),
                )
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showForm) {
        VaccinationFormDialog(
            babyId = babyId,
            editEntity = editingVac,
            onSave = { vac ->
                scope.launch {
                    if (editingVac != null) {
                        vacRepo.update(vac)
                    } else {
                        vacRepo.insert(vac)
                    }
                    showForm = false
                    editingVac = null
                }
            },
            onDismiss = {
                showForm = false
                editingVac = null
            },
        )
    }

    if (showGenerateConfirm) {
        AlertDialog(
            onDismissRequest = { showGenerateConfirm = false },
            title = { Text("生成接种计划") },
            text = { Text("将根据宝宝出生日期自动生成 21 条默认接种计划。已存在的记录不会被覆盖。") },
            confirmButton = {
                TextButton(onClick = {
                    showGenerateConfirm = false
                    scope.launch {
                        val baby = babyRepo.getById(babyId)
                        if (baby != null) {
                            VaccineSchedule.createForBaby(babyId, baby.birthDate).forEach { vacRepo.insert(it) }
                        }
                    }
                }) { Text("生成") }
            },
            dismissButton = {
                TextButton(onClick = { showGenerateConfirm = false }) { Text("取消") }
            },
        )
    }

    deletingVac?.let { v ->
        AlertDialog(
            onDismissRequest = { deletingVac = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${v.name}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val deleted = v
                        vacRepo.delete(deleted)
                        deletingVac = null
                        val result = snackbarHostState.showSnackbar(
                            message = "已删除「${deleted.name}」",
                            actionLabel = "撤销",
                            duration = SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            vacRepo.insert(deleted)
                        }
                    }
                }) { Text("删除", color = c.danger) }
            },
            dismissButton = {
                TextButton(onClick = { deletingVac = null }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationFormDialog(
    babyId: Int,
    editEntity: VaccinationEntity? = null,
    onSave: (VaccinationEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalThemeColors.current
    val isEdit = editEntity != null
    var name by remember { mutableStateOf(editEntity?.name ?: "") }
    var dose by remember { mutableStateOf(editEntity?.dose ?: "") }
    var status by remember { mutableStateOf(editEntity?.status ?: "pending") }
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 0.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text(if (isEdit) "编辑疫苗" else "添加疫苗", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("疫苗名称") }, leadingIcon = { Text("💉", style = MaterialTheme.typography.titleMedium) }, isError = name.isBlank(), supportingText = { if (name.isBlank()) Text("名称不能为空") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), singleLine = true, shape = MaterialTheme.shapes.medium)

            OutlinedTextField(value = dose, onValueChange = { dose = it }, label = { Text("剂次 (可选)") }, placeholder = { Text("第1剂") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), singleLine = true, shape = MaterialTheme.shapes.medium)

            Text("状态", style = MaterialTheme.typography.bodySmall, color = c.textSecondary, modifier = Modifier.padding(bottom = 8.dp))
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("pending" to "未接种", "done" to "已接种", "skipped" to "已跳过").forEach { (s, l) ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text(l, style = MaterialTheme.typography.bodySmall) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = c.primary.copy(alpha = 0.12f), selectedLabelColor = c.primary)
                    )
                }
            }

            OutlinedTextField(value = scheduledDate, onValueChange = {}, readOnly = true, label = { Text("计划接种日期 (可选)") }, leadingIcon = { Text("📅", style = MaterialTheme.typography.titleMedium) }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showScheduledDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.medium, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = c.cardBorder, disabledTextColor = c.textPrimary, disabledLabelColor = c.textSecondary))

            if (status == "done") {
                OutlinedTextField(value = administeredDate, onValueChange = {}, readOnly = true, label = { Text("实际接种日期 (可选)") }, leadingIcon = { Text("✅", style = MaterialTheme.typography.titleMedium) }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showAdministeredDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.medium, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = c.cardBorder, disabledTextColor = c.textPrimary, disabledLabelColor = c.textSecondary))
            }

            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注 (可选)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), singleLine = true, shape = MaterialTheme.shapes.medium)

            Button(
                onClick = {
                    val scheduledDateTime = if (scheduledDate.isNotBlank()) "${scheduledDate}T00:00:00" else null
                    val administeredDateTime = if (administeredDate.isNotBlank()) "${administeredDate}T00:00:00" else null
                    val vac = if (isEdit) {
                        editEntity.copy(
                            name = name,
                            dose = dose.ifBlank { null },
                            scheduledDate = scheduledDateTime,
                            administeredDate = administeredDateTime,
                            status = status,
                            note = note.ifBlank { null }
                        )
                    } else {
                        VaccinationEntity(
                            babyId = babyId,
                            name = name,
                            dose = dose.ifBlank { null },
                            scheduledDate = scheduledDateTime,
                            administeredDate = administeredDateTime,
                            status = status,
                            note = note.ifBlank { null }
                        )
                    }
                    onSave(vac)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(DT.buttonRadius.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                enabled = name.isNotBlank()
            ) {
                Text(if (isEdit) "更新" else "保存", color = Color.White, style = MaterialTheme.typography.titleSmall)
            }
        }
    }

    if (showScheduledDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showScheduledDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                showScheduledDatePicker = false
                datePickerState.selectedDateMillis?.let { millis ->
                    val instant = java.time.Instant.ofEpochMilli(millis)
                    scheduledDate = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                        .toLocalDate().toString()
                }
            }) { Text("确定") }
        }, dismissButton = { TextButton(onClick = { showScheduledDatePicker = false }) { Text("取消") } }) {
            DatePicker(state = datePickerState)
        }
    }

    if (showAdministeredDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showAdministeredDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                showAdministeredDatePicker = false
                datePickerState.selectedDateMillis?.let { millis ->
                    val instant = java.time.Instant.ofEpochMilli(millis)
                    administeredDate = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                        .toLocalDate().toString()
                }
            }) { Text("确定") }
        }, dismissButton = { TextButton(onClick = { showAdministeredDatePicker = false }) { Text("取消") } }) {
            DatePicker(state = datePickerState)
        }
    }
}
