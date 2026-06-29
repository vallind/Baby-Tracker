package com.babytracker.feature.sleep

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
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.SleepRepository
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SleepListScreen(navController: NavController) {
    val c = LocalAppColors.current
    val sleepRepo: SleepRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val sleeps by sleepRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingSleep by remember { mutableStateOf<Sleep?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState, scope) }

    // 日期选择状态：默认"今天"
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }

    // 按所选日期过滤
    val filteredSleeps = remember(sleeps, selectedDate) {
        val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        sleeps.filter { it.startTime.take(10) == dateStr }
    }

    // 夜间睡眠（当天 startTime 匹配的 NIGHT 记录）
    val nightSleep = remember(filteredSleeps) {
        filteredSleeps.filter { it.type == SleepType.NIGHT }.firstOrNull()
    }

    // 小睡列表（按开始时间排序）
    val naps = remember(filteredSleeps) {
        filteredSleeps.filter { it.type == SleepType.NAP }.sortedBy { it.startTime }
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
                title = "睡眠记录",
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

            if (filteredSleeps.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "\uD83D\uDE34",
                        title = "还没有睡眠记录",
                        subtitle = "点击底部按钮，记录宝宝的睡眠时间",
                        actionText = "记录睡眠",
                        onAction = {
                            editingSleep = null
                            showForm = true
                        },
                    )
                }
            } else {
                // —— 夜间睡眠大卡：移出 LazyColumn，自然铺满页面宽度 ——
                if (nightSleep != null) {
                    val nightStart = LocalDateTime.parse(nightSleep.startTime, DateTimeFormatter.ISO_DATE_TIME)
                    val nightEnd = LocalDateTime.parse(nightSleep.endTime, DateTimeFormatter.ISO_DATE_TIME)
                    val durSec = DateUtils.durationToTotalSeconds(nightStart, nightEnd)
                    val timeRange = "${nightStart.format(DateTimeFormatter.ofPattern("HH:mm"))}-${nightEnd.format(DateTimeFormatter.ofPattern("HH:mm"))}"
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
                                .background(Gradients.sleepHeader(c), cardShape)
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center,
                                    ) { Text("\uD83C\uDF19", fontSize = 16.sp) }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "夜间睡眠",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.9f),
                                    )
                                }
                                Spacer(Modifier.height(20.dp))
                                Text(
                                    DateUtils.durationFullText(durSec),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    timeRange,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.75f),
                                )
                            }
                        }
                    }

                    // —— 睡眠详情 ——
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
                                "睡眠详情",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = c.textPrimary,
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth()) {
                                val sleepStart = LocalDateTime.parse(nightSleep.startTime, DateTimeFormatter.ISO_DATE_TIME)
                                val sleepEnd = LocalDateTime.parse(nightSleep.endTime, DateTimeFormatter.ISO_DATE_TIME)
                                SleepStatCell(
                                    label = "入睡时间",
                                    value = sleepStart.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    modifier = Modifier.weight(1f),
                                    c = c,
                                )
                                SleepStatCell(
                                    label = "起床时间",
                                    value = sleepEnd.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    modifier = Modifier.weight(1f),
                                    c = c,
                                )
                                SleepStatCell(
                                    label = "夜醒次数",
                                    value = "-",
                                    modifier = Modifier.weight(1f),
                                    c = c,
                                )
                            }
                        }
                    }
                }

                // —— 小睡记录 ——
                if (naps.isNotEmpty()) {
                    Text(
                        "小睡记录",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                        modifier = Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 0.dp)
                            .padding(bottom = 12.dp),
                    )
                }

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
                    if (naps.isNotEmpty()) {
                        items(items = naps, key = { it.id }) { nap ->
                            val napStart = LocalDateTime.parse(nap.startTime, DateTimeFormatter.ISO_DATE_TIME)
                            val napEnd = LocalDateTime.parse(nap.endTime, DateTimeFormatter.ISO_DATE_TIME)
                            val durSec = DateUtils.durationToTotalSeconds(napStart, napEnd)
                            val range = "${napStart.format(DateTimeFormatter.ofPattern("HH:mm"))}-${napEnd.format(DateTimeFormatter.ofPattern("HH:mm"))}"

                            RecordCard(
                                onDelete = {
                                    scope.launch { sleepRepo.delete(nap) }
                                    appSnackbar.showUndo(message = "已删除小睡记录") { sleepRepo.insert(nap) }
                                },
                                onClick = {},
                                onLongClick = {
                                    editingSleep = nap
                                    showForm = true
                                },
                                modifier = Modifier.padding(bottom = 8.dp),
                                accentColor = c.warning,
                            ) {
                                Box(
                                    Modifier
                                        .size(DT.iconBgSize.dp)
                                        .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                                        .background(c.warning.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center,
                                ) { Text("\u2600\uFE0F", style = MaterialTheme.typography.titleLarge) }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    range,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = c.textPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    DateUtils.durationFullText(durSec),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = c.warning,
                                    fontWeight = FontWeight.Bold,
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
                        editingSleep = null
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
                    Text("记录睡眠", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    if (showForm) {
        SleepFormDialog(
            babyId = babyId,
            editEntity = editingSleep,
            onDismiss = {
                showForm = false
                editingSleep = null
            },
            onSave = { sleep ->
                scope.launch {
                    if (editingSleep != null) {
                        sleepRepo.update(sleep)
                    } else {
                        sleepRepo.insert(sleep)
                    }
                    showForm = false
                    editingSleep = null
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
fun SleepFormDialog(
    babyId: Int,
    editEntity: Sleep? = null,
    onDismiss: () -> Unit,
    onSave: (Sleep) -> Unit,
) {
    val isEdit = editEntity != null
    var selectedType by remember { mutableStateOf(editEntity?.let { SleepType.raw(it.type) } ?: "night") }
    val now = LocalDateTime.now()
    var startTime by remember {
        mutableStateOf(
            editEntity?.startTime?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) { now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }
            } ?: now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        )
    }
    var endTime by remember {
        mutableStateOf(
            editEntity?.endTime?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) { now.plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }
            } ?: now.plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        )
    }
    var note by remember { mutableStateOf(editEntity?.note ?: "") }
    var showCascadePicker by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableIntStateOf(0) }

    val buildEntity = {
        if (isEdit) {
            editEntity.copy(
                type = SleepType.fromRaw(selectedType),
                startTime = startTime.replace(" ", "T") + ":00",
                endTime = endTime.replace(" ", "T") + ":00",
                note = note.ifBlank { null },
            )
        } else {
            Sleep(
                babyId = babyId,
                type = SleepType.fromRaw(selectedType),
                startTime = startTime.replace(" ", "T") + ":00",
                endTime = endTime.replace(" ", "T") + ":00",
                note = note.ifBlank { null },
            )
        }
    }

    AppFormSheet(
        title = if (isEdit) "编辑睡眠" else "记录睡眠",
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) "更新" else "保存",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedType == "night", onClick = { selectedType = "night" }, label = { Text("\uD83C\uDF19 夜间睡眠") })
            FilterChip(selected = selectedType == "nap", onClick = { selectedType = "nap" }, label = { Text("\u2600\uFE0F 小睡") })
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = startTime, onValueChange = {}, readOnly = true, label = { Text("开始时间") }, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 0; showCascadePicker = true }, singleLine = true, shape = MaterialTheme.shapes.medium, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = endTime, onValueChange = {}, readOnly = true, label = { Text("结束时间") }, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 1; showCascadePicker = true }, singleLine = true, shape = MaterialTheme.shapes.medium, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small)
    }

    fun pickerField() = if (pickerTarget == 0) startTime else endTime
    fun updatePickerField(v: String) { if (pickerTarget == 0) startTime = v else endTime = v }

    DateTimeCascadeDialog(
        show = showCascadePicker,
        initialDateTime = pickerField(),
        onConfirm = { updatePickerField(it) },
        onDismiss = { showCascadePicker = false },
    )
}

@Composable
private fun SleepStatCell(
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
