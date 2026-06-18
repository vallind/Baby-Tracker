package com.babytracker.ui.vaccination

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.core.database.entity.VaccinationEntity
import com.babytracker.core.theme.DT
import com.babytracker.core.theme.LocalThemeColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.VaccineSchedule
import com.babytracker.data.repository.VaccinationRepository
import com.babytracker.data.repository.BabyRepository
import com.babytracker.ui.components.rememberHaptic
import com.babytracker.ui.components.longPressDeletable
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
    var deletingVac by remember { mutableStateOf<VaccinationEntity?>(null) }
    var showGenerateConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("疫苗接种") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ))
    }, floatingActionButton = {
        FloatingActionButton(onClick = { showForm = true }, containerColor = MaterialTheme.colorScheme.primary) {
            Icon(Icons.Default.Add, null, tint = Color.White)
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 12.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(DT.buttonRadius.dp)).padding(4.dp)) {
                listOf("pending" to "接种计划", "done" to "接种记录").forEach { (s, l) ->
                    Box(Modifier.weight(1f).background(if (filter == s) MaterialTheme.colorScheme.surface else Color.Transparent, MaterialTheme.shapes.small).clickable { filter = s }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(l, style = MaterialTheme.typography.bodyMedium, fontWeight = if (filter == s) FontWeight.SemiBold else null, color = if (filter == s) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
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
                    Card(
                        Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 4.dp).fillMaxWidth().longPressDeletable(haptic) { deletingVac = v },
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(if (v.status == "done") c.green.copy(alpha = 0.1f) else c.pink.copy(alpha = 0.1f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text(if (v.status == "done") "✅" else "💉", style = MaterialTheme.typography.titleLarge) }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(v.name, style = MaterialTheme.typography.titleSmall)
                            Text("${v.dose ?: ""}${if (v.scheduledDate != null) " · ${DateUtils.formatDate(LocalDateTime.parse(v.scheduledDate, DateTimeFormatter.ISO_DATE_TIME))}" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(Modifier.background(if (v.status == "done") c.green.copy(alpha = 0.1f) else c.tagBg, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text(if (v.status == "done") "已接种" else if (v.status == "pending") "未接种" else "已跳过", style = MaterialTheme.typography.labelSmall, color = if (v.status == "done") c.green else c.tagText)
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { showForm = false }, sheetState = sheetState) {
            VaccinationFormDialog(
                babyId = babyId,
                onSave = { vac ->
                    scope.launch {
                        vacRepo.insert(vac)
                        showForm = false
                    }
                }
            )
        }
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
                    scope.launch { vacRepo.delete(v) }
                    deletingVac = null
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
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
    onSave: (VaccinationEntity) -> Unit,
) {
    val c = LocalThemeColors.current
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("pending") }
    var scheduledDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var administeredDate by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showScheduledDatePicker by remember { mutableStateOf(false) }
    var showAdministeredDatePicker by remember { mutableStateOf(false) }

    Column(Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 0.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
        Text("添加疫苗", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("疫苗名称") }, leadingIcon = { Text("💉", style = MaterialTheme.typography.titleMedium) }, isError = name.isBlank(), supportingText = { if (name.isBlank()) Text("名称不能为空") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), singleLine = true, shape = MaterialTheme.shapes.medium)

        OutlinedTextField(value = dose, onValueChange = { dose = it }, label = { Text("剂次 (可选)") }, placeholder = { Text("第1剂") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), singleLine = true, shape = MaterialTheme.shapes.medium)

        Text("状态", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("pending" to "未接种", "done" to "已接种", "skipped" to "已跳过").forEach { (s, l) ->
                FilterChip(
                    selected = status == s,
                    onClick = { status = s },
                    label = { Text(l, style = MaterialTheme.typography.bodySmall) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), selectedLabelColor = MaterialTheme.colorScheme.primary)
                )
            }
        }

        OutlinedTextField(value = scheduledDate, onValueChange = {}, readOnly = true, label = { Text("计划接种日期 (可选)") }, leadingIcon = { Text("📅", style = MaterialTheme.typography.titleMedium) }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showScheduledDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.medium, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))

        if (status == "done") {
            OutlinedTextField(value = administeredDate, onValueChange = {}, readOnly = true, label = { Text("实际接种日期 (可选)") }, leadingIcon = { Text("✅", style = MaterialTheme.typography.titleMedium) }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showAdministeredDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.medium, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
        }

        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注 (可选)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), singleLine = true, shape = MaterialTheme.shapes.medium)

        Button(
            onClick = {
                val scheduledDateTime = if (scheduledDate.isNotBlank()) "${scheduledDate}T00:00:00" else null
                val administeredDateTime = if (administeredDate.isNotBlank()) "${administeredDate}T00:00:00" else null
                onSave(VaccinationEntity(
                    babyId = babyId,
                    name = name,
                    dose = dose.ifBlank { null },
                    scheduledDate = scheduledDateTime,
                    administeredDate = administeredDateTime,
                    status = status,
                    note = note.ifBlank { null }
                ))
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = name.isNotBlank()
        ) {
            Text("保存", color = Color.White, style = MaterialTheme.typography.titleSmall)
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