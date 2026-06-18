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
import com.babytracker.domain.model.Vaccination
import com.babytracker.core.theme.DT
import com.babytracker.core.theme.LocalThemeColors
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.VaccineSchedule
import com.babytracker.data.repository.BabyRepository
import com.babytracker.data.repository.VaccinationRepository

import org.koin.compose.koinInject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VaccinationListScreen(navController: NavController) {
    val c = LocalThemeColors.current
    val vacRepo: VaccinationRepository = koinInject()
    val babyRepo: BabyRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val vaccinations by vacRepo.watchByBaby(babyId).collectAsState(initial = emptyList())

    var filter by remember { mutableStateOf("pending") }
    var showForm by remember { mutableStateOf(false) }
    var deletingVac by remember { mutableStateOf<Vaccination?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("疫苗接种") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } })
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
                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("暂无记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (filter == "pending") {
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(onClick = {
                                scope.launch {
                                    val baby = babyRepo.getById(babyId)
                                    val birthDate = (baby?.birthDate ?: java.time.LocalDate.now()).toString()
                                    VaccineSchedule.createForBaby(babyId, birthDate).forEach { vacRepo.insert(it) }
                                }
                            }, shape = MaterialTheme.shapes.small) {
                                Text("生成接种计划")
                            }
                        }
                    }
                }
            } else {
                filtered.forEach { v ->
                    Card(
                        Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 4.dp).fillMaxWidth().combinedClickable(onLongClick = { deletingVac = v }, onClick = {}),
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(if (v.status == "done") c.green.copy(alpha = 0.1f) else c.pink.copy(alpha = 0.1f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text(if (v.status == "done") "✅" else "💉", style = MaterialTheme.typography.titleLarge) }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(v.name, style = MaterialTheme.typography.titleSmall)
                            Text("${v.dose ?: ""}${if (v.scheduledDate != null) " · ${v.scheduledDate}" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(Modifier.background(if (v.status == "done") c.green.copy(alpha = 0.1f) else c.tagBg, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text(if (v.status == "done") "已接种" else if (v.status == "pending") "未接种" else "已跳过", style = MaterialTheme.typography.labelSmall, color = if (v.status == "done") c.green else c.tagText)
                        }
                    }
                }
                }
            }
            Spacer(Modifier.height(80.dp))
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
    onSave: (Vaccination) -> Unit,
) {
    val c = LocalThemeColors.current
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("pending") }
    var scheduledDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var administeredDate by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableIntStateOf(0) }

    Column(Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 0.dp).padding(bottom = 32.dp)) {
        Text("添加疫苗", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("疫苗名称") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), singleLine = true, shape = MaterialTheme.shapes.small)

        OutlinedTextField(value = dose, onValueChange = { dose = it }, label = { Text("剂次 (可选)") }, placeholder = { Text("第1剂") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), singleLine = true, shape = MaterialTheme.shapes.small)

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

        OutlinedTextField(value = scheduledDate, onValueChange = {}, readOnly = true, label = { Text("接种日期 (可选)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { datePickerTarget = 0; showDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.small, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))

        if (status == "done") {
            OutlinedTextField(value = administeredDate, onValueChange = {}, readOnly = true, label = { Text("实际接种日期 (可选)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { datePickerTarget = 1; showDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.small, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
        }


        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注 (可选)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), singleLine = true, shape = MaterialTheme.shapes.small)

        Button(
            onClick = {
                val scheduledDateObj = if (scheduledDate.isNotBlank()) LocalDate.parse(scheduledDate) else null
                val administeredDateObj = if (administeredDate.isNotBlank()) LocalDate.parse(administeredDate) else null
                onSave(Vaccination(
                    babyId = babyId,
                    name = name,
                    dose = dose.ifBlank { null },
                    scheduledDate = scheduledDateObj,
                    administeredDate = administeredDateObj,
                    status = status,
                    note = note.ifBlank { null }
                ))
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = name.isNotBlank()
        ) {
            Text("保存", color = Color.White, style = MaterialTheme.typography.titleSmall)
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    if (datePickerTarget == 0) scheduledDate = date else administeredDate = date
                }
                showDatePicker = false
            }) { Text("确定") }
        }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }) {
            DatePicker(state = datePickerState)
        }
    }
}