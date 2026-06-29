package com.babytracker.feature.diaper

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.domain.model.DiaperType
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.DiaperRepository
import kotlinx.coroutines.launch
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaperListScreen(navController: NavController) {
    val c = LocalAppColors.current
    val diaperRepo: DiaperRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val diapers by diaperRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingDiaper by remember { mutableStateOf<Diaper?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState, scope) }

    // 日期选择状态
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }

    // 按所选日期过滤
    val filtered = remember(diapers, selectedDate) {
        val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        diapers.filter { it.timestamp.take(10) == dateStr }
    }

    // 分类计数
    val wetCount = remember(filtered) { filtered.count { it.type == DiaperType.WET } }
    val poopCount = remember(filtered) { filtered.count { it.type == DiaperType.POOP } }
    val bothCount = remember(filtered) { filtered.count { it.type == DiaperType.BOTH } }

    // 排序后的记录
    val sortedRecords = remember(filtered) {
        filtered.sortedByDescending { it.timestamp }
    }

    // 日期显示文本
    val dateLabel = remember(selectedDate, today) {
        when {
            selectedDate == today -> "今天"
            selectedDate == today.minusDays(1) -> "昨天"
            selectedDate == today.plusDays(1) -> "明天"
            else -> selectedDate.format(DateTimeFormatter.ofPattern("MM月dd日"))
        }
    }

    Scaffold(
        containerColor = c.pageBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "尿布记录",
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "选择日期",
                            tint = c.textPrimary,
                        )
                    }
                },
            )
        },
        bottomBar = { BottomNavBar(navController) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(c.pageBackground),
        ) {
            // —— 日期选择器 ——
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(horizontal = DT.pageMargin.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "$dateLabel ${selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = c.textTertiary,
                    modifier = Modifier.size(18.dp),
                )
            }

            if (filtered.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "🧷",
                        title = "还没有尿布记录",
                        subtitle = "点击底部按钮，记录宝宝每次换尿布",
                        actionText = "记录尿布",
                        onAction = {
                            editingDiaper = null
                            showForm = true
                        },
                    )
                }
            } else {
                // —— 今日汇总大卡：移出 LazyColumn，正常布局消除负 padding ——
                val cardShape = RoundedCornerShape(DT.cardRadiusLg.dp)

                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DT.pageMargin.dp)
                        .padding(bottom = 16.dp),
                    shape = cardShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Gradients.diaperSummary(c), cardShape)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center,
                                ) { Text("🧷", fontSize = 16.sp) }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "今日尿布",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f),
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            Text(
                                "${filtered.size} 次",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "💧$wetCount  ·  💩$poopCount  ·  🔄$bothCount",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.75f),
                            )
                        }
                    }
                }

                // —— 换尿布详情 ——
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DT.pageMargin.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(DT.cardRadius.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = c.surface),
                ) {
                    Column(Modifier.padding(DT.cardInnerPadding.dp)) {
                        Text(
                            "换尿布详情",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textPrimary,
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth()) {
                            DiaperStatCell(
                                emoji = "💧",
                                label = "小便",
                                value = "${wetCount}次",
                                modifier = Modifier.weight(1f),
                                c = c,
                            )
                            DiaperStatCell(
                                emoji = "💩",
                                label = "大便",
                                value = "${poopCount}次",
                                modifier = Modifier.weight(1f),
                                c = c,
                            )
                            DiaperStatCell(
                                emoji = "🔄",
                                label = "混合",
                                value = "${bothCount}次",
                                modifier = Modifier.weight(1f),
                                c = c,
                            )
                        }
                    }
                }

                // —— 记录列表 ——
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = DT.pageMargin.dp,
                        end = DT.pageMargin.dp,
                        bottom = 8.dp,
                    ),
                ) {
                    items(items = sortedRecords, key = { it.id }) { d ->
                        val label = DateUtils.diaperTypeLabel(DiaperType.raw(d.type))
                        val timeStr = try {
                            LocalDateTime.parse(d.timestamp, DateTimeFormatter.ISO_DATE_TIME)
                                .format(DateTimeFormatter.ofPattern("HH:mm"))
                        } catch (_: Exception) { "" }
                        val typeEmoji = when (d.type) {
                            DiaperType.WET -> "💧"
                            DiaperType.POOP -> "💩"
                            DiaperType.BOTH -> "🔄"
                        }
                        val accentColor = when (d.type) {
                            DiaperType.WET -> c.primary
                            DiaperType.POOP -> c.warning
                            DiaperType.BOTH -> c.error
                        }

                        RecordCard(
                        onDelete = {
                            scope.launch { diaperRepo.delete(d) }
                            appSnackbar.showUndo(message = "已删除尿布记录") { diaperRepo.insert(d) }
                        },
                            onClick = {},
                            onLongClick = {
                                editingDiaper = d
                                showForm = true
                            },
                            modifier = Modifier.padding(bottom = 8.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(DT.iconBgSize.dp)
                                    .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                                    .background(accentColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) { Text(typeEmoji, style = MaterialTheme.typography.titleLarge) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = c.textPrimary,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    timeStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textSecondary,
                                )
                            }
                            if (!d.note.isNullOrBlank()) {
                                Text(
                                    d.note.take(8),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.textTertiary,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                        }
                    }

                    // 底部留白给按钮
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }

            // —— 底部固定按钮 ——
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .padding(horizontal = DT.pageMargin.dp, vertical = 12.dp),
            ) {
                Button(
                    onClick = {
                        editingDiaper = null
                        showForm = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(DT.buttonRadius.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("记录尿布", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
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

    // 日期选择对话框
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.toEpochDay() * 86400000L)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = LocalDate.ofEpochDay(millis / 86400000L)
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaperFormDialog(
    babyId: Int,
    editEntity: Diaper? = null,
    onDismiss: () -> Unit,
    onSave: (Diaper) -> Unit,
) {
    val isEdit = editEntity != null
    var selectedType by remember { mutableStateOf(editEntity?.let { DiaperType.raw(it.type) } ?: "wet") }
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
    var showCascadePicker by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf(editEntity?.note ?: "") }

    val buildEntity = {
        if (isEdit) {
            editEntity.copy(
                type = DiaperType.fromRaw(selectedType),
                timestamp = diaperDateTime.replace(" ", "T") + ":00",
                note = note.ifBlank { null },
            )
        } else {
            Diaper(
                babyId = babyId,
                type = DiaperType.fromRaw(selectedType),
                timestamp = diaperDateTime.replace(" ", "T") + ":00",
                note = note.ifBlank { null },
            )
        }
    }

    AppFormSheet(
        title = if (isEdit) "编辑尿布" else "记录尿布",
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) "更新" else "保存",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            listOf("wet" to "💧 小便", "poop" to "💩 大便", "both" to "🔄 混合").forEach { (t, label) ->
                FilterChip(
                    selected = selectedType == t,
                    onClick = { selectedType = t },
                    label = { Text(label) },
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = diaperDateTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("时间") },
            modifier = Modifier.fillMaxWidth().clickable { showCascadePicker = true },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("备注 (可选)") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        )
    }

    DateTimeCascadeDialog(
        show = showCascadePicker,
        initialDateTime = diaperDateTime,
        onConfirm = { diaperDateTime = it },
        onDismiss = { showCascadePicker = false },
    )
}

@Composable
private fun DiaperStatCell(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    c: AppColors,
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = c.textPrimary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = c.textSecondary,
        )
    }
}
