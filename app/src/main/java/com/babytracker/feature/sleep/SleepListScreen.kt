package com.babytracker.feature.sleep

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.babytracker.designsystem.composites.herostat.AppHeroStatCard
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.core.util.DateUtils
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
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SleepListScreen(
    state: SleepUiState,
    babyId: Int,
    bottomBar: @Composable () -> Unit = {},
    onBack: () -> Unit,
    onAdd: (Sleep) -> Unit,
    onUpdate: (Sleep) -> Unit,
    onDelete: (Sleep) -> Unit,
    onUndoDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val scope = rememberCoroutineScope()
    if (babyId == 0) return
    val sleeps = state.sleeps
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
                onBack = onBack,
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
        bottomBar = bottomBar,
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

                    // 夜间睡眠合并行：渐变紫大卡 + 时长 + 入睡/起床两格
                    // 收编：渐变底容器统一走设计系统 AppHeroStatCard（观感与原实现一致，点击进详情保留在调用点）
                    val nightTypography = LocalAppTypography.current
                    val nightShapes = LocalAppShapes.current
                    val nightContentColor = c.onSecondary
                    AppHeroStatCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.md)
                            .padding(bottom = spacing.md)
                            .clip(RoundedCornerShape(nightShapes.largeIncreased))
                            .clickable(onClick = { detailSleep = nightSleep }),
                        gradient = Gradients.sleepHeader(c),
                        contentColor = nightContentColor,
                        emoji = "\uD83C\uDF19",
                        title = AppStrings.nightSleep,
                    ) {
                        Text(
                            DateUtils.durationFullText(durSec),
                            style = nightTypography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = nightContentColor,
                        )
                        Spacer(Modifier.height(spacing.xs))
                        Text(
                            timeRange,
                            style = nightTypography.bodyMedium,
                            color = nightContentColor.copy(alpha = 0.78f),
                        )
                        Spacer(Modifier.height(spacing.lg))
                        Row(Modifier.fillMaxWidth()) {
                            QuickStatPill(value = nightStart.format(DateTimeFormatter.ofPattern("HH:mm")), label = AppStrings.fallAsleepTime, contentColor = nightContentColor, modifier = Modifier.weight(1f))
                            QuickStatPill(value = nightEnd.format(DateTimeFormatter.ofPattern("HH:mm")), label = AppStrings.wakeUpTime, contentColor = nightContentColor, modifier = Modifier.weight(1f))
                        }
                    }
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
                                        onDelete(nap)
                                        appSnackbar.showUndo(message = AppStrings.deletedNap) { onUndoDelete() }
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
                    onDelete(s)
                    appSnackbar.showUndo(message = AppStrings.deletedSleep) { onUndoDelete() }
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
                    if (editingSleep != null) onUpdate(sleep) else onAdd(sleep)
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
