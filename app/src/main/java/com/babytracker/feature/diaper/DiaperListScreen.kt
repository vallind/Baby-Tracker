package com.babytracker.feature.diaper

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
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.core.database.entity.DiaperEntity
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalThemeColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.DiaperRepository
import kotlinx.coroutines.launch
import com.babytracker.designsystem.components.rememberHaptic
import com.babytracker.designsystem.components.SwipeToDeleteContainer
import com.babytracker.designsystem.components.EmptyState
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaperListScreen(navController: NavController) {
    val c = LocalThemeColors.current
    val diaperRepo: DiaperRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val haptic = rememberHaptic()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val diapers by diaperRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingDiaper by remember { mutableStateOf<DiaperEntity?>(null) }
    var deletingDiaper by remember { mutableStateOf<DiaperEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = c.bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingDiaper = null
                    showForm = true
                },
                containerColor = c.primary,
                contentColor = c.card,
                shape = RoundedCornerShape(DT.buttonRadius.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
                text = { Text("记录尿布", style = MaterialTheme.typography.titleSmall) },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(c.bg).verticalScroll(rememberScrollState())) {
            // —— 顶部页头 ——
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Gradients.pageHeader(c))
                    .padding(horizontal = DT.pageMargin.dp)
                    .height(DT.appBarHeight.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = c.textPrimary)
                }
                Text(
                    "尿布记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                )
            }

            Column(Modifier.padding(horizontal = DT.pageMargin.dp)) {
                if (diapers.isEmpty()) {
                    EmptyState(
                        emoji = "🧷",
                        title = "还没有尿布记录",
                        subtitle = "点击下方按钮，记录每次换尿布",
                        actionText = "记录尿布",
                        onAction = {
                            editingDiaper = null
                            showForm = true
                        },
                    )
                }
                val grouped = diapers.groupBy { it.timestamp.take(10) }
                var groupIndex = 0
                grouped.forEach { (date, items) ->
                    Text(
                        date,
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textSecondary,
                        modifier = Modifier.padding(top = if (groupIndex == 0) DT.cardGap.dp else DT.cardGapSm.dp, bottom = 4.dp),
                    )
                    items.forEachIndexed { i, d ->
                        val diaperCardShape = RoundedCornerShape(DT.cardRadius.dp)
                        val tint = if (i % 2 == 1) c.accent else c.primary
                        SwipeToDeleteContainer(
                            onDelete = {
                                scope.launch {
                                    val deleted = d
                                    diaperRepo.delete(deleted)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "已删除尿布记录",
                                        actionLabel = "撤销",
                                        duration = SnackbarDuration.Short,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        diaperRepo.insert(deleted)
                                    }
                                }
                            },
                        ) {
                            Card(
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth()
                                    .shadow(elevation = DT.cardElevation.dp, shape = diaperCardShape)
                                    .combinedClickable(
                                        onClick = {
                                            editingDiaper = d
                                            showForm = true
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            deletingDiaper = d
                                        },
                                    ),
                                shape = diaperCardShape,
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                colors = CardDefaults.cardColors(containerColor = c.card),
                            ) {
                                Row(Modifier.padding(DT.cardInnerPadding.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(DT.iconBgSize.dp)
                                            .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                                            .background(tint.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center,
                                    ) { Text("🧷", style = MaterialTheme.typography.titleLarge) }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(DateUtils.diaperTypeLabel(d.type), style = MaterialTheme.typography.titleSmall, color = c.textPrimary, fontWeight = FontWeight.Medium)
                                        Text(try { LocalDateTime.parse(d.timestamp, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) } catch (_: Exception) { "" }, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                                    }
                                    if (!d.note.isNullOrBlank()) {
                                        Text(d.note.take(6), style = MaterialTheme.typography.labelSmall, color = c.textHint, modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                            }
                        }
                    }
                    groupIndex++
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showForm) {
        DiaperFormDialog(
            babyId = babyId,
            editEntity = editingDiaper,
            onDismiss = {
                showForm = false
                editingDiaper = null
            },
            onSave = { d ->
                scope.launch {
                    if (editingDiaper != null) {
                        diaperRepo.update(d)
                    } else {
                        diaperRepo.insert(d)
                    }
                    showForm = false
                    editingDiaper = null
                }
            },
        )
    }

    deletingDiaper?.let { d ->
        AlertDialog(
            onDismissRequest = { deletingDiaper = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条尿布记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val deleted = d
                        diaperRepo.delete(deleted)
                        deletingDiaper = null
                        val result = snackbarHostState.showSnackbar(
                            message = "已删除尿布记录",
                            actionLabel = "撤销",
                            duration = SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            diaperRepo.insert(deleted)
                        }
                    }
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingDiaper = null }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaperFormDialog(
    babyId: Int,
    editEntity: DiaperEntity? = null,
    onDismiss: () -> Unit,
    onSave: (DiaperEntity) -> Unit,
) {
    val isEdit = editEntity != null
    var selectedType by remember { mutableStateOf(editEntity?.type ?: "wet") }
    val now = LocalDateTime.now()
    var diaperDateTime by remember {
        mutableStateOf(
            editEntity?.timestamp?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) { now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }
            } ?: now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf(editEntity?.note ?: "") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = DT.pageMargin.dp).verticalScroll(rememberScrollState())) {
            Text(if (isEdit) "编辑尿布" else "记录尿布", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                listOf("wet" to "💧 小便", "poop" to "💩 大便", "both" to "💧💩 混合").forEach { (t, label) ->
                    FilterChip(
                        selected = selectedType == t,
                        onClick = { selectedType = t },
                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = diaperDateTime, onValueChange = {}, readOnly = true, label = { Text("时间") }, modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.medium, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注 (可选)") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val diaper = if (isEdit) {
                        editEntity.copy(
                            type = selectedType,
                            timestamp = diaperDateTime.replace(" ", "T") + ":00",
                            note = note.ifBlank { null },
                        )
                    } else {
                        DiaperEntity(
                            babyId = babyId,
                            type = selectedType,
                            timestamp = diaperDateTime.replace(" ", "T") + ":00",
                            note = note.ifBlank { null },
                        )
                    }
                    onSave(diaper)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium,
            ) { Text(if (isEdit) "更新" else "保存") }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                showDatePicker = false
                datePickerState.selectedDateMillis?.let { millis ->
                    val instant = java.time.Instant.ofEpochMilli(millis)
                    val date = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val timePart = diaperDateTime.substring(11)
                    diaperDateTime = "$date $timePart"
                }
                showTimePicker = true
            }) { Text("下一步") }
        }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        var hour by remember { mutableIntStateOf(diaperDateTime.substring(11, 13).toIntOrNull() ?: 12) }
        var minute by remember { mutableIntStateOf(diaperDateTime.substring(14, 16).toIntOrNull() ?: 0) }
        AlertDialog(onDismissRequest = { showTimePicker = false }, title = { Text("选择时间") }, text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { if (hour < 23) hour++ }) { Text("▲", style = MaterialTheme.typography.bodySmall) }
                        Text("%02d".format(hour), style = MaterialTheme.typography.headlineMedium)
                        TextButton(onClick = { if (hour > 0) hour-- }) { Text("▼", style = MaterialTheme.typography.bodySmall) }
                        Text("时", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(" : ", style = MaterialTheme.typography.headlineMedium)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { if (minute < 59) minute++ }) { Text("▲", style = MaterialTheme.typography.bodySmall) }
                        Text("%02d".format(minute), style = MaterialTheme.typography.headlineMedium)
                        TextButton(onClick = { if (minute > 0) minute-- }) { Text("▼", style = MaterialTheme.typography.bodySmall) }
                        Text("分", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                val datePart = diaperDateTime.take(10)
                diaperDateTime = "$datePart ${"%02d".format(hour)}:${"%02d".format(minute)}"
                showTimePicker = false
            }) { Text("确定") }
        }, dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } })
    }
}
