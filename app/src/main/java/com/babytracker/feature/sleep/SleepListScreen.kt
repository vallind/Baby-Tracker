package com.babytracker.feature.sleep

import android.content.SharedPreferences
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.babytracker.designsystem.components.chip.AppFilterChip
import com.babytracker.designsystem.components.datenav.DateNavCapsule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.SleepRepository
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.recorddetail.RecordDetailSheet
import com.babytracker.designsystem.components.quickstat.QuickStatPill
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.actionbar.AppActionBar
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.datetimecascade.QuickTimeChipRow
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.designsystem.components.summarycard.AppSummaryCard
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.accentContent
import com.babytracker.designsystem.theme.tintContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SleepListScreen(navController: NavController) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val sleepRepo: SleepRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val sleeps by sleepRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingSleep by remember { mutableStateOf<Sleep?>(null) }
    var detailSleep by remember { mutableStateOf<Sleep?>(null) }
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

    // 日期显示文本（简写：今天 · 8月23日，不再拼 ISO 日期）
    val dateLabel = remember(selectedDate, today) {
        val md = selectedDate.format(DateTimeFormatter.ofPattern("M月d日"))
        when {
            selectedDate == today -> "${AppStrings.today} · $md"
            selectedDate == today.minusDays(1) -> "${AppStrings.yesterday} · $md"
            selectedDate == today.plusDays(1) -> "${AppStrings.tomorrow} · $md"
            else -> md
        }
    }

    AppScaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = AppStrings.sleepRecords,
                onBack = { navController.popBackStack() },
                actions = {
                    AppIconButton(
                        icon = Icons.Default.DateRange,
                        onClick = { showDatePicker = true },
                        contentDescription = AppStrings.selectDate,
                        tint = c.textPrimary,
                    )
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
            // —— 日期选择器（现代胶囊行） ——
            DateNavCapsule(
                dateLabel = dateLabel,
                onPrev = { selectedDate = selectedDate.minusDays(1) },
                onNext = { selectedDate = selectedDate.plusDays(1) },
                onOpenPicker = { showDatePicker = true },
                onToday = if (selectedDate != today) ({ selectedDate = today }) else null,
                modifier = Modifier.padding(horizontal = spacing.md),
            )

            if (filteredSleeps.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "\uD83D\uDE34",
                        title = AppStrings.emptySleepTitle,
                        subtitle = AppStrings.emptySleepSubtitle,
                        actionText = AppStrings.recordSleep,
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

                    // 夜间睡眠合并卡：时长 + 时段 + 入睡/起床两格（2.1 合并原「睡眠详情」卡）
                    NightSleepCard(
                        emoji = "\uD83C\uDF19",
                        title = AppStrings.nightSleep,
                        value = DateUtils.durationFullText(durSec),
                        subtitle = timeRange,
                        fallAsleep = nightStart.format(DateTimeFormatter.ofPattern("HH:mm")),
                        wakeUp = nightEnd.format(DateTimeFormatter.ofPattern("HH:mm")),
                        onClick = { detailSleep = nightSleep },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.md)
                            .padding(bottom = spacing.md),
                    )
                }

                // —— 小睡记录 ——
                if (naps.isNotEmpty()) {
                    Text(
                        AppStrings.napRecords,
                        style = LocalAppTypography.current.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
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
                                        appSnackbar.showUndo(message = AppStrings.deletedNap) { sleepRepo.update(nap) }
                                    }
                                },
                                onClick = { detailSleep = nap },
                                onLongClick = {
                                    editingSleep = nap
                                    showForm = true
                                },
                                modifier = Modifier.padding(bottom = spacing.sm),
                                accentColor = c.warning,
                            ) {
                                AppEmojiBadge(emoji = "\u2600\uFE0F", tint = c.warning)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    range,
                                    style = LocalAppTypography.current.titleSmall,
                                    color = c.textPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    DateUtils.durationFullText(durSec),
                                    style = LocalAppTypography.current.titleSmall,
                                    color = c.warning,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    // 底部留白给按钮
                    item { Spacer(Modifier.height(spacing.sm)) }
                }
            }

            // —— 底部主操作条（DS 统一件） ——
            AppActionBar(
                label = AppStrings.recordSleep,
                icon = Icons.Default.Add,
                onClick = {
                    editingSleep = null
                    showForm = true
                },
            )
        }
    }

    // 单击卡片 = 详情弹层（夜间大卡与小睡卡共用）
    detailSleep?.let { s ->
        RecordDetailSheet(
            show = true,
            title = if (s.type == SleepType.NIGHT) AppStrings.nightSleep else AppStrings.nap,
            emoji = if (s.type == SleepType.NIGHT) "🌙" else "☀️",
            tint = if (s.type == SleepType.NIGHT) c.secondary else c.tertiary,
            fields = sleepDetailFields(s),
            onEdit = {
                detailSleep = null
                editingSleep = s
                showForm = true
            },
            onDelete = {
                scope.launch {
                    sleepRepo.delete(s)
                    appSnackbar.showUndo(message = AppStrings.deletedSleep) { sleepRepo.update(s) }
                }
            },
            onDismiss = { detailSleep = null },
        )
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
    val spacing = LocalAppSpacing.current
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

    val timerDisplay = String.format(Locale.US, "%02d:%02d", elapsed / 60, elapsed % 60)
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
        title = if (isEdit) AppStrings.editSleep else AppStrings.recordSleep,
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) AppStrings.updateLabel else AppStrings.save,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            AppFilterChip(selected = selectedType == "night", onClick = { selectedType = "night" }, label = AppStrings.sleepOptionNight, modifier = Modifier.weight(1f))
            AppFilterChip(selected = selectedType == "nap", onClick = { selectedType = "nap" }, label = AppStrings.sleepOptionNap, modifier = Modifier.weight(1f))
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
                style = LocalAppTypography.current.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (timerRunning) LocalAppColors.current.primary else LocalAppColors.current.textSecondary,
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
                    label = AppStrings.timerStop,
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
                    label = AppStrings.timerStart,
                )
            }
        }
        QuickTimeChipRow(onPick = { startTime = it.format(timeFormatter) })
        Spacer(Modifier.height(spacing.xs))
        AppInput(value = startTime, onValueChange = {}, label = AppStrings.startTimeLabel, enabled = false, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 0; showCascadePicker = true })
        Spacer(Modifier.height(12.dp))
        AppInput(value = endTime, onValueChange = {}, label = AppStrings.endTimeLabel, enabled = false, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 1; showCascadePicker = true })
        Spacer(Modifier.height(12.dp))
        AppInput(value = note, onValueChange = { note = it }, label = AppStrings.detailNote, modifier = Modifier.fillMaxWidth())
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
    val spacing = LocalAppSpacing.current
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            value,
            style = LocalAppTypography.current.titleMedium,
            fontWeight = FontWeight.Bold,
            color = c.textPrimary,
        )
        Spacer(Modifier.height(spacing.xxs))
        Text(
            label,
            style = LocalAppTypography.current.bodySmall,
            color = c.textSecondary,
        )
    }
}


/** 睡眠记录详情字段 */
private fun sleepDetailFields(s: Sleep): List<Pair<String, String>> {
    fun fmt(v: String): String = try {
        java.time.LocalDateTime.parse(v, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    } catch (_: Exception) { v }
    val list = mutableListOf<Pair<String, String>>()
    list += AppStrings.detailStart to fmt(s.startTime)
    list += AppStrings.detailEnd to fmt(s.endTime)
    val durSec = DateUtils.durationToTotalSeconds(
        java.time.LocalDateTime.parse(s.startTime, java.time.format.DateTimeFormatter.ISO_DATE_TIME),
        java.time.LocalDateTime.parse(s.endTime, java.time.format.DateTimeFormatter.ISO_DATE_TIME),
    )
    list += AppStrings.detailDuration to DateUtils.durationFullText(durSec)
    s.note?.takeIf { it.isNotBlank() }?.let { list += AppStrings.detailNote to it }
    return list
}


/** 夜间睡眠合并卡：渐变紫大卡 + 入睡/起床两格 QuickStatPill（替代「汇总卡+详情卡」两段式） */
@Composable
private fun NightSleepCard(
    emoji: String,
    title: String,
    value: String,
    subtitle: String,
    fallAsleep: String,
    wakeUp: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    val contentColor = c.onSecondary
    Box(
        modifier
            .clip(RoundedCornerShape(shapes.largeIncreased))
            .background(Gradients.sleepHeader(c))
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(spacing.xl)
                        .clip(RoundedCornerShape(shapes.medium))
                        .background(contentColor.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) { Text(emoji, style = typography.titleMedium) }
                Spacer(Modifier.width(spacing.sm))
                Text(
                    title,
                    style = typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = contentColor.copy(alpha = 0.90f),
                )
            }
            Spacer(Modifier.height(spacing.md))
            Text(
                value,
                style = typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                subtitle,
                style = typography.bodyMedium,
                color = contentColor.copy(alpha = 0.78f),
            )
            Spacer(Modifier.height(spacing.lg))
            Row(Modifier.fillMaxWidth()) {
                QuickStatPill(value = fallAsleep, label = AppStrings.fallAsleepTime, contentColor = contentColor, modifier = Modifier.weight(1f))
                QuickStatPill(value = wakeUp, label = AppStrings.wakeUpTime, contentColor = contentColor, modifier = Modifier.weight(1f))
            }
        }
    }
}