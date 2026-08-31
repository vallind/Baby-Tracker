package com.babytracker.feature.feeding

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.chip.AppFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ChevronRight
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.core.util.DateUtils
import com.babytracker.ui.patterns.records.appDateNavLabel
import com.babytracker.ui.patterns.records.DateNavCapsule
import com.babytracker.ui.patterns.records.RecordDetailSheet
import com.babytracker.ui.patterns.records.DateNavCapsule
import com.babytracker.ui.patterns.records.RecordCard
import com.babytracker.designsystem.components.actionbar.AppActionBar
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.ui.patterns.records.DateTimeCascadeDialog
import com.babytracker.ui.patterns.records.QuickTimeChipRow
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.feature.common.feedingTone
import com.babytracker.designsystem.theme.accentContent
import com.babytracker.designsystem.theme.tintContainer
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedingListScreen(
    state: FeedingUiState,
    babyId: Int,
    bottomBar: @Composable () -> Unit = {},
    onBack: () -> Unit,
    onAdd: (Feeding) -> Unit,
    onUpdate: (Feeding) -> Unit,
    onDelete: (Feeding) -> Unit,
    onUndoDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val scope = rememberCoroutineScope()
    if (babyId == 0) return
    val feedings = state.feedings
    var showForm by remember { mutableStateOf(false) }
    var editingFeeding by remember { mutableStateOf<Feeding?>(null) }
    var detailFeeding by remember { mutableStateOf<Feeding?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

    // 日期选择状态：默认"今天"
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }

    // 按所选日期过滤
    val filteredFeedings = remember(feedings, selectedDate) {
        val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        feedings.filter { it.timestamp.take(10) == dateStr }
    }

    // 日期显示文本（简写：今天 · 8月23日，不再拼 ISO 日期）
    val dateLabel = remember(selectedDate, today) { appDateNavLabel(selectedDate, today) }

    AppScaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = AppStrings.feedingRecords,
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
            // —— 日期导航（DS 统一组件） ——
            DateNavCapsule(
                dateLabel = dateLabel,
                onPrev = { selectedDate = selectedDate.minusDays(1) },
                onNext = { selectedDate = selectedDate.plusDays(1) },
                onOpenPicker = { showDatePicker = true },
                onToday = if (selectedDate != today) ({ selectedDate = today }) else null,
                modifier = Modifier.padding(horizontal = spacing.md),
            )

            if (filteredFeedings.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "\uD83C\uDF7C",
                        title = AppStrings.emptyFeedingTitle,
                        subtitle = AppStrings.emptyFeedingSubtitle,
                        actionText = AppStrings.recordFeeding,
                        onAction = {
                            editingFeeding = null
                            showForm = true
                        },
                    )
                }
            } else {
                // —— 时间轴列表 ——
                FeedingTimeline(
                    feedings = filteredFeedings,
                    snackbarHostState = snackbarHostState,
                    onDetail = { f -> detailFeeding = f },
                    onDelete = { f ->
                        scope.launch {
                            onDelete(f)
                            appSnackbar.showUndo(message = AppStrings.deletedFeeding) { onUndoDelete() }
                        }
                    },
                    onEdit = { f ->
                        editingFeeding = f
                        showForm = true
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            // —— 底部主操作条（DS 统一件） ——
            AppActionBar(
                label = AppStrings.recordFeeding,
                icon = Icons.Default.Add,
                onClick = {
                    editingFeeding = null
                    showForm = true
                },
            )
        }
    }

    // 单击卡片 = 详情弹层（编辑/确认删除在弹层内，删除走 Snackbar 撤销）
    detailFeeding?.let { d ->
        RecordDetailSheet(
            show = true,
            title = DateUtils.feedingTypeLabel(FeedingType.raw(d.type)),
            emoji = feedingEmoji(d.type),
            tint = feedingColor(d.type, c),
            fields = feedingDetailFields(d),
            onEdit = {
                detailFeeding = null
                editingFeeding = d
                showForm = true
            },
            onDelete = {
                scope.launch {
                    onDelete(d)
                    appSnackbar.showUndo(message = AppStrings.deletedFeeding) { onUndoDelete() }
                }
            },
            onDismiss = { detailFeeding = null },
        )
    }

    if (showForm) {
        FeedingFormDialog(
            babyId = babyId,
            editEntity = editingFeeding,
            onDismiss = {
                showForm = false
                editingFeeding = null
            },
            onSave = { feeding ->
                scope.launch {
                    if (editingFeeding != null) onUpdate(feeding) else onAdd(feeding)
                    showForm = false
                    editingFeeding = null
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

// ═══════════════════════════════════════════════════════════
//  喂养时间轴
// ═══════════════════════════════════════════════════════════

/** 喂养类型视觉映射（emoji+分区色）已收敛至 feature/common/RecordTone.feedingTone */
private fun feedingColor(type: FeedingType, c: AppColors): Color = feedingTone(type, c).second
private fun feedingEmoji(type: FeedingType): String = when (type) {
    FeedingType.BREAST -> "\uD83E\uDD31"
    FeedingType.FORMULA -> "\uD83C\uDF7C"
    FeedingType.FOOD -> "\uD83E\uDD63"
    else -> "\uD83E\uDD64"
}

/** 喂养记录摘要文本 */
private fun feedingSummary(f: Feeding): String = when (f.type) {
    FeedingType.BREAST -> {
        val side = f.breastSide?.let { com.babytracker.core.domain.model.BreastSide.raw(it) } ?: AppStrings.breastSideBoth
        val mlPart = f.amountMl?.let { "${it}${AppStrings.feedingAmountMl}" } ?: ""
        if (mlPart.isNotEmpty()) "$mlPart, $side \u00B7 ${String.format(Locale.US, AppStrings.minutesCompactFormat, f.durationMin)}"
        else "$side \u00B7 ${String.format(Locale.US, AppStrings.minutesCompactFormat, f.durationMin)}"
    }
    FeedingType.FORMULA -> {
        "${f.amountMl}ml${if (!f.brand.isNullOrBlank()) " \u00B7 ${f.brand}" else ""}"
    }
    FeedingType.FOOD -> {
        "${f.foodName} ${f.amountG}g"
    }
    else -> {
        "${f.amountMl}ml"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedingTimeline(
    feedings: List<Feeding>,
    snackbarHostState: SnackbarHostState,
    onDetail: (Feeding) -> Unit,
    onDelete: (Feeding) -> Unit,
    onEdit: (Feeding) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = spacing.md,
            end = spacing.md,
            top = spacing.xs,
            bottom = spacing.sm,
        ),
    ) {
        items(items = feedings, key = { it.id }) { f ->
            val color = feedingColor(f.type, c)
            val emoji = feedingEmoji(f.type)
            val time = try {
                LocalDateTime.parse(f.timestamp, DateTimeFormatter.ISO_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (_: Exception) { "" }
            val typeLabel = DateUtils.feedingTypeLabel(FeedingType.raw(f.type))

            // 记录卡片行：粉彩徽章 + 标题/摘要 + 时间（时间轴竖线改为卡片呼吸间距）
            RecordCard(
                onDelete = { onDelete(f) },
                onClick = { onDetail(f) },
                onLongClick = { onEdit(f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                accentColor = color,
            ) {
                AppEmojiBadge(emoji = emoji, tint = color)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        typeLabel,
                        style = LocalAppTypography.current.titleSmall,
                        color = c.textPrimary,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        feedingSummary(f),
                        style = LocalAppTypography.current.bodySmall,
                        color = c.textSecondary,
                        maxLines = 1,
                    )
                }
                Text(
                    time,
                    style = LocalAppTypography.current.labelMedium,
                    color = c.textTertiary,
                )
            }
        }
    }
}


/** 喂养记录详情字段（按类型展示实际数据，无数据字段不上） */
private fun feedingDetailFields(f: Feeding): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    val time = try {
        java.time.LocalDateTime.parse(f.timestamp, DateTimeFormatter.ISO_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    } catch (_: Exception) { f.timestamp }
    list += AppStrings.detailTime to time
    list += AppStrings.detailType to DateUtils.feedingTypeLabel(FeedingType.raw(f.type))
    when (f.type) {
        FeedingType.BREAST -> {
            f.durationMin?.let { list += AppStrings.detailDuration to "${String.format(Locale.US, AppStrings.minutesFormat, it)}" }
            f.amountMl?.let { list += AppStrings.detailAmount to "${it} ml" }
            com.babytracker.core.domain.model.BreastSide.raw(f.breastSide)?.let { list += AppStrings.detailSide to it }
        }
        FeedingType.FORMULA -> {
            f.amountMl?.let { list += AppStrings.detailAmount to "${it} ml" }
            f.brand?.takeIf { it.isNotBlank() }?.let { list += AppStrings.detailBrand to it }
        }
        FeedingType.FOOD -> {
            f.foodName?.takeIf { it.isNotBlank() }?.let { list += AppStrings.detailFood to it }
            f.amountG?.let { list += AppStrings.detailPortion to "${it} g" }
        }
        FeedingType.WATER -> f.amountMl?.let { list += AppStrings.detailWaterAmount to "${it} ml" }
    }
    f.note?.takeIf { it.isNotBlank() }?.let { list += AppStrings.detailNote to it }
    return list
}
