package com.babytracker.feature.sleep
import com.babytracker.core.ui.AppShapes
import com.babytracker.core.ui.AppSpacing

import android.content.SharedPreferences
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.babytracker.core.ui.components.chip.AppFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import io.elyon.kmp.basic.Icon
import io.elyon.kmp.basic.SnackbarHostState
import io.elyon.kmp.theme.ElyonTheme
import io.elyon.kmp.basic.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babytracker.navigation.Navigator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import com.babytracker.core.ui.Gradients
import com.babytracker.core.ui.components.scaffold.AppScaffold
import com.babytracker.core.ui.components.iconbutton.AppIconButton
import com.babytracker.core.ui.components.button.AppButton
import com.babytracker.core.ui.components.card.AppCard
import com.babytracker.core.ui.components.input.AppInput
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.SleepRepository
import com.babytracker.core.ui.components.BottomNavBar
import com.babytracker.core.ui.components.recordcard.RecordCard
import com.babytracker.core.ui.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.core.ui.components.dialog.AppFormSheet
import com.babytracker.core.ui.components.EmptyState
import com.babytracker.core.ui.components.topbar.AppTopBar
import com.babytracker.core.ui.components.snackbar.AppSnackbar
import com.babytracker.core.ui.components.snackbar.AppSnackbarHost
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SleepListScreen(navigator: Navigator) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val shapes = com.babytracker.core.ui.AppShapes
    val sleepRepo: SleepRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val sleeps by sleepRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingSleep by remember { mutableStateOf<Sleep?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

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

    AppScaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "睡眠记录",
                onBack = { navigator.pop() },
                actions = {
                    AppIconButton(
                        icon = Icons.Default.DateRange,
                        onClick = { showDatePicker = true },
                        contentDescription = "选择日期",
                        tint = c.onSurface,
                    )
                },
            )
        },
        bottomBar = { BottomNavBar(navigator) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(c.background),
        ) {
            // —— 日期选择器 ——
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(horizontal = spacing.md, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "$dateLabel ${selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                    style = ElyonTheme.textStyles.subtitle,
                    fontWeight = FontWeight.Medium,
                    color = c.onSurface,
                )
                Spacer(Modifier.width(spacing.xs))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = c.onSurfaceVariantSummary,
                    modifier = Modifier.size(18.dp),
                )
            }

            if (filteredSleeps.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "",
                        title = "还没有睡眠记录",
                        subtitle = "点击底部按钮，记录宝宝的睡眠时间",
                        actionText = "记录睡眠",
                        icon = Icons.Filled.Bedtime,
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
                    val cardShape = RoundedCornerShape(shapes.large)

                    AppCard(
                        containerColor = Color.Transparent,
                        elevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.md)
                            .padding(bottom = spacing.md),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(Gradients.sleepHeader(ElyonTheme.colorScheme), cardShape)
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(spacing.xl)
                                            .clip(RoundedCornerShape(shapes.medium))
                                            .background(c.onSecondaryContainer.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Filled.Bedtime,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp),
                                            tint = c.onSecondaryContainer,
                                        )
                                    }
                                    Spacer(Modifier.width(spacing.sm))
                                    Text(
                                        "夜间睡眠",
                                        style = ElyonTheme.textStyles.title3,
                                        fontWeight = FontWeight.Medium,
                                        color = c.onSecondaryContainer,
                                    )
                                }
                                Spacer(Modifier.height(20.dp))
                                Text(
                                    DateUtils.durationFullText(durSec),
                                    style = ElyonTheme.textStyles.headline2,
                                    fontWeight = FontWeight.Bold,
                                    color = c.onSecondaryContainer,
                                )
                                Spacer(Modifier.height(spacing.xs))
                                Text(
                                    timeRange,
                                    style = ElyonTheme.textStyles.body2,
                                    color = c.onSecondaryContainer.copy(alpha = 0.75f),
                                )
                            }
                        }
                    }

                    // —— 睡眠详情 ——
                    AppCard(
                        containerColor = c.surface,
                        elevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.md)
                            .padding(bottom = spacing.md),
                    ) {
                        Column(Modifier.padding(spacing.md)) {
                            Text(
                                "睡眠详情",
                                style = ElyonTheme.textStyles.subtitle,
                                fontWeight = FontWeight.SemiBold,
                                color = c.onSurface,
                            )
                            Spacer(Modifier.height(spacing.md))
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
                        style = ElyonTheme.textStyles.subtitle,
                        fontWeight = FontWeight.SemiBold,
                        color = c.onSurface,
                        modifier = Modifier.padding(horizontal = spacing.md, vertical = 0.dp)
                            .padding(bottom = 12.dp),
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = spacing.md,
                        end = spacing.md,
                        bottom = spacing.sm,
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
                                    scope.launch {
                                        sleepRepo.delete(nap)
                                        appSnackbar.showUndo(message = "已删除小睡记录") { sleepRepo.update(nap) }
                                    }
                                },
                                onClick = {},
                                onLongClick = {
                                    editingSleep = nap
                                    showForm = true
                                },
                                modifier = Modifier.padding(bottom = spacing.sm),
                                accentColor = c.secondary,
                            ) {
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(shapes.large))
                                            .background(c.secondary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.WbSunny,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = c.secondary,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    range,
                                    style = ElyonTheme.textStyles.subtitle,
                                    color = c.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    DateUtils.durationFullText(durSec),
                                    style = ElyonTheme.textStyles.subtitle,
                                    color = c.secondary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    // 底部留白给按钮
                    item { Spacer(Modifier.height(spacing.sm)) }
                }
            }

            // —— 底部固定按钮 ——
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .padding(horizontal = spacing.md, vertical = 12.dp),
            ) {
                AppButton(
                    onClick = {
                        editingSleep = null
                        showForm = true
                    },
                    label = "记录睡眠",
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth(),
                )
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
    DateTimeCascadeDialog(
        show = showDatePicker,
        initialDateTime = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + " 00:00",
        dateOnly = true,
        onConfirm = { dt ->
            selectedDate = LocalDate.parse(dt.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
            showDatePicker = false
        },
        onDismiss = { showDatePicker = false },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepFormDialog(
    babyId: Int,
    editEntity: Sleep? = null,
    onDismiss: () -> Unit,
    onSave: (Sleep) -> Unit,
) {
    val spacing = com.babytracker.core.ui.AppSpacing
    val isEdit = editEntity != null
    var selectedType by remember { mutableStateOf(editEntity?.let { SleepType.raw(it.type) } ?: "night") }
    val now = LocalDateTime.now()
    val prefs: SharedPreferences = koinInject()

    var startTime by remember {
        mutableStateOf(
            editEntity?.startTime?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) { now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }
            } ?: if (prefs.getBoolean("sleep_timer_running", false)) {
                prefs.getString("sleep_timer_form_start_time", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))!!
            } else {
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            }
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

    // 计时器（持久化：关闭表单再打开继续计时）
    var timerRunning by remember {
        mutableStateOf(prefs.getBoolean("sleep_timer_running", false))
    }
    var timerStartMs by remember {
        mutableLongStateOf(prefs.getLong("sleep_timer_start_millis", 0L))
    }
    var elapsed by remember {
        mutableIntStateOf(
            if (editEntity != null && !prefs.getBoolean("sleep_timer_running", false)) {
                val start = try { LocalDateTime.parse(editEntity.startTime, DateTimeFormatter.ISO_DATE_TIME) } catch (_: Exception) { null }
                val end = try { LocalDateTime.parse(editEntity.endTime, DateTimeFormatter.ISO_DATE_TIME) } catch (_: Exception) { null }
                if (start != null && end != null) Duration.between(start, end).seconds.toInt() else 0
            } else {
                0
            }
        )
    }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (true) {
                elapsed = ((System.currentTimeMillis() - timerStartMs) / 1000).toInt()
                delay(1000L)
            }
        }
    }

    val timerDisplay = String.format("%02d:%02d", elapsed / 60, elapsed % 60)
    val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    val buildEntity = {
        if (timerRunning) {
            timerRunning = false
            endTime = LocalDateTime.now().format(timeFormatter)
            prefs.edit()
                .putBoolean("sleep_timer_running", false)
                .remove("sleep_timer_form_start_time")
                .apply()
        }
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            AppFilterChip(selected = selectedType == "night", onClick = { selectedType = "night" }, label = "夜间睡眠", modifier = Modifier.weight(1f))
            AppFilterChip(selected = selectedType == "nap", onClick = { selectedType = "nap" }, label = "小睡", modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(spacing.md))
        // 计时器 UI
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        ) {
            Text(
                text = timerDisplay,
                style = ElyonTheme.textStyles.headline2,
                fontWeight = FontWeight.Bold,
                color = if (timerRunning)  ElyonTheme.colorScheme.primary else  ElyonTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.weight(1f).widthIn(min = 100.dp),
                textAlign = TextAlign.Start,
            )
            if (timerRunning) {
                AppButton(
                    onClick = {
                        timerRunning = false
                        val endNow = LocalDateTime.now()
                        endTime = endNow.format(timeFormatter)
                        prefs.edit()
                            .putBoolean("sleep_timer_running", false)
                            .remove("sleep_timer_form_start_time")
                            .apply()
                    },
                    label = "结束计时",
                )
            } else {
                AppButton(
                    onClick = {
                        val currentStartTime = startTime
                        timerStartMs = System.currentTimeMillis()
                        elapsed = 0
                        timerRunning = true
                        prefs.edit()
                            .putBoolean("sleep_timer_running", true)
                            .putLong("sleep_timer_start_millis", timerStartMs)
                            .putString("sleep_timer_form_start_time", currentStartTime)
                            .apply()
                    },
                    label = "开始计时",
                )
            }
        }
        AppInput(value = startTime, onValueChange = {}, label = "开始时间", enabled = false, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 0; showCascadePicker = true })
        Spacer(Modifier.height(12.dp))
        AppInput(value = endTime, onValueChange = {}, label = "结束时间", enabled = false, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 1; showCascadePicker = true })
        Spacer(Modifier.height(12.dp))
        AppInput(value = note, onValueChange = { note = it }, label = "备注", modifier = Modifier.fillMaxWidth())
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
    c: io.elyon.kmp.theme.Colors,
) {
    val spacing = com.babytracker.core.ui.AppSpacing
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            value,
            style = ElyonTheme.textStyles.title3,
            fontWeight = FontWeight.Bold,
            color = c.onSurface,
        )
        Spacer(Modifier.height(spacing.xxs))
        Text(
            label,
            style = ElyonTheme.textStyles.footnote1,
            color = c.onSurfaceVariantSummary,
        )
    }
}
