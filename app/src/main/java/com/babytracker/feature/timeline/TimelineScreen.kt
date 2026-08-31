package com.babytracker.feature.timeline

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.dialog.AppOptionPickerSheet
import com.babytracker.designsystem.components.progress.AppCircularProgress
import com.babytracker.designsystem.components.chip.AppFilterChip
import com.babytracker.designsystem.components.feedback.EmptyState
import com.babytracker.designsystem.components.divider.AppDivider
import com.babytracker.ui.patterns.records.RecordDetailSheet
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.ui.patterns.records.RecordCard
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.designsystem.components.selection.SegmentedControl
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.core.util.DateUtils
import com.babytracker.core.domain.model.*
import com.babytracker.feature.diaper.DiaperFormDialog
import com.babytracker.feature.feeding.FeedingFormDialog
import com.babytracker.feature.growth.GrowthFormDialog
import com.babytracker.feature.health.HealthFormDialog
import com.babytracker.feature.sleep.SleepFormDialog
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    state: TimelineUiState,
    babyId: Int,
    editing: Any?,
    bottomBar: @Composable () -> Unit = {},
    onDelete: (TimelineItem) -> Unit,
    onUndoDelete: () -> Unit,
    onRequestEdit: (TimelineItem) -> Unit,
    onDismissEdit: () -> Unit,
    onAddFeeding: (Feeding) -> Unit,
    onAddSleep: (Sleep) -> Unit,
    onAddDiaper: (Diaper) -> Unit,
    onUpdateFeeding: (Feeding) -> Unit,
    onUpdateSleep: (Sleep) -> Unit,
    onUpdateDiaper: (Diaper) -> Unit,
    onUpdateGrowth: (Growth) -> Unit,
    onUpdateHealth: (HealthRecord) -> Unit,
    onOpenGrowth: () -> Unit,
    onOpenVaccination: () -> Unit,
    onOpenHealth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current

    val scope = rememberCoroutineScope()

    var showTypePicker by remember { mutableStateOf(false) }
    var showAddFeeding by remember { mutableStateOf(false) }
    var showAddSleep by remember { mutableStateOf(false) }
    var showAddDiaper by remember { mutableStateOf(false) }
    var typeFilter by remember { mutableStateOf("") }
    var detailRecord by remember { mutableStateOf<TimelineItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

    // 类型 → 颜色映射（与首页宫格分区色一致：喂养珊瑚/睡眠紫/尿布青/生长绿/健康蓝）
    val typeColor: (String) -> Color = {
        when (it) {
            "feeding" -> c.danger
            "sleep" -> c.secondary
            "diaper" -> c.tertiary
            "growth" -> c.success
            "health" -> c.primary
            else -> c.primary
        }
    }

    // 类型 → emoji 映射
    val typeEmoji: (String) -> String = {
        when (it) {
            "feeding" -> "🤱"
            "sleep" -> "🌙"
            "diaper" -> "🧷"
            "growth" -> "📏"
            "health" -> "❤️"
            else -> "📝"
        }
    }

    // 无宝宝时不渲染（与原 LaunchedEffect 前守卫一致；宝宝由 VM 的 babyId 状态提供）
    if (babyId == 0) return

    AppScaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(title = AppStrings.records)
        },
        bottomBar = bottomBar,
        fab = {
            AppFAB(icon = Icons.Default.Add, onClick = { showTypePicker = true })
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(c.pageBackground),
        ) {
            // —— 类型筛选行（2.1 H6：capsule chips + 分区色选中态，替换挤不下的 SegmentedControl） ——
            val filterKeys = listOf("", "feeding", "sleep", "diaper", "growth", "health")
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                filterKeys.forEach { key ->
                    val selected = typeFilter == key
                    AppFilterChip(
                        selected = selected,
                        onClick = { typeFilter = key },
                        label = when (key) {
                            "" -> AppStrings.filterAll
                            "feeding" -> AppStrings.feeding
                            "sleep" -> AppStrings.sleep
                            "diaper" -> AppStrings.diaper
                            "growth" -> AppStrings.growth
                            else -> AppStrings.health
                        },
                        selectedColor = when (key) {
                            "feeding" -> c.danger
                            "sleep" -> c.secondary
                            "diaper" -> c.tertiary
                            "growth" -> c.success
                            "health" -> c.primary
                            else -> c.primary
                        },
                    )
                }
            }
            AppDivider(color = c.divider, thickness = 0.5.dp)

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AppCircularProgress(indicatorColor = c.primary)
                }
            } else if (state.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "📝",
                        title = AppStrings.noRecordsTitle,
                        subtitle = AppStrings.noRecordsSubtitle,
                        actionText = AppStrings.startRecording,
                        onAction = { showTypePicker = true },
                    )
                }
            } else {
                val filtered = remember(state.items, typeFilter) {
                    if (typeFilter.isEmpty()) state.items
                    else state.items.filter { it.recordType == typeFilter }
                }

                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            emoji = typeEmoji(typeFilter),
                            title = "没有${when (typeFilter) {
                                "feeding" -> AppStrings.feeding
                                "sleep" -> AppStrings.sleep
                                "diaper" -> AppStrings.diaper
                                "growth" -> AppStrings.growth
                                "health" -> AppStrings.health
                                else -> ""
                            }}记录",
                            subtitle = AppStrings.emptyFilteredSubtitle,
                        )
                    }
                } else {
                    val grouped = remember(filtered) { filtered.groupBy { it.date } }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = spacing.md,
                            end = spacing.md,
                            top = spacing.sm,
                            bottom = 80.dp,
                        ),
                    ) {
                        grouped.forEach { (date, records) ->
                            // —— 日期分组标题 ——
                            item(key = "header-$date") {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = spacing.sm, bottom = spacing.xs),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = DateUtils.relativeDate(date),
                                        style = typography.labelMedium,
                                        color = c.textSecondary,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(shapes.full))
                                            .background(c.divider)
                                            .padding(horizontal = 6.dp, vertical = 1.dp),
                                    ) {
                                        Text(
                                            "${String.format(Locale.US, AppStrings.countTimesCompact, records.size)}",
                                            style = typography.labelMedium,
                                            color = c.textTertiary,
                                        )
                                    }
                                }
                            }

                            // —— 记录卡片列表 ——
                            items(items = records, key = { "${it.recordType}-${it.id}" }) { record ->
                                val accent = typeColor(record.recordType)
                                RecordCard(
                                    modifier = Modifier.padding(bottom = spacing.sm),
                                    accentColor = accent,
                                    onDelete = {
                                        scope.launch {
                                            onDelete(record)
                                            appSnackbar.showUndo(message = "${String.format(Locale.US, AppStrings.deletedWithTitle, record.title)}") { onUndoDelete() }
                                        }
                                    },
                                    onClick = { detailRecord = record },
                                    onLongClick = { detailRecord = record },
                                ) {
                                    AppEmojiBadge(emoji = record.emoji, tint = accent)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            record.title,
                                            // 与记录三页对齐：行标题 titleSmall / 副标题 bodySmall
                                            style = typography.titleSmall,
                                            color = c.textPrimary,
                                        )
                                        Text(
                                            record.subtitle,
                                            style = typography.bodySmall,
                                            color = c.textSecondary,
                                        )
                                    }
                                    if (record.time.isNotEmpty()) {
                                        Text(
                                            record.time,
                                            style = typography.labelMedium,
                                            color = c.textTertiary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── 类型选择底部弹窗 ──
    if (showTypePicker) {
        AppOptionPickerSheet(
            title = AppStrings.pickRecordType,
            options = listOf(
                "feeding" to AppStrings.pickerOptionFeeding,
                "sleep" to AppStrings.pickerOptionSleep,
                "diaper" to AppStrings.pickerOptionDiaper,
                "growth" to AppStrings.pickerOptionGrowth,
                "vaccine" to AppStrings.pickerOptionVaccine,
                "health" to AppStrings.pickerOptionHealth,
            ),
            selectedKey = null,
            onSelect = { key ->
                when (key) {
                    "feeding" -> showAddFeeding = true
                    "sleep" -> showAddSleep = true
                    "diaper" -> showAddDiaper = true
                    "growth" -> onOpenGrowth()
                    "vaccine" -> onOpenVaccination()
                    "health" -> onOpenHealth()
                }
            },
            onDismiss = { showTypePicker = false },
        )
    }

    // 单击卡片 = 详情弹层（编辑按类型分发到各表单）
    detailRecord?.let { record ->
        RecordDetailSheet(
            show = true,
            title = record.title,
            emoji = record.emoji,
            tint = typeColor(record.recordType),
            fields = listOf(
                AppStrings.detailDate to record.date,
                AppStrings.detailTime to record.time,
                AppStrings.detailContent to record.subtitle,
            ),
            onEdit = {
                detailRecord = null
                onRequestEdit(record)
            },
            onDelete = {
                detailRecord = null
                scope.launch {
                    onDelete(record)
                    appSnackbar.showUndo(message = "${String.format(Locale.US, AppStrings.deletedWithTitle, record.title)}") { onUndoDelete() }
                }
            },
            onDismiss = { detailRecord = null },
        )
    }

    // ── 编辑表单弹窗（编辑目标在 ViewModel：requestEdit/dismissEdit 维护）──
    when (val entity = editing) {
        is Feeding -> FeedingFormDialog(
            babyId = babyId,
            editEntity = entity,
            onDismiss = onDismissEdit,
            onSave = { updated ->
                scope.launch {
                    onUpdateFeeding(updated)
                    onDismissEdit()
                }
            },
        )

        is Sleep -> SleepFormDialog(
            babyId = babyId,
            editEntity = entity,
            onDismiss = onDismissEdit,
            onSave = { updated ->
                scope.launch {
                    onUpdateSleep(updated)
                    onDismissEdit()
                }
            },
        )

        is Diaper -> DiaperFormDialog(
            babyId = babyId,
            editEntity = entity,
            onDismiss = onDismissEdit,
            onSave = { updated ->
                scope.launch {
                    onUpdateDiaper(updated)
                    onDismissEdit()
                }
            },
        )

        is Growth -> GrowthFormDialog(
            babyId = babyId,
            editEntity = entity,
            onDismiss = onDismissEdit,
            onSave = { updated ->
                scope.launch {
                    onUpdateGrowth(updated)
                    onDismissEdit()
                }
            },
        )

        is HealthRecord -> HealthFormDialog(
            babyId = babyId,
            editEntity = entity,
            onDismiss = onDismissEdit,
            onSave = { updated ->
                scope.launch {
                    onUpdateHealth(updated)
                    onDismissEdit()
                }
            },
        )

        else -> Unit
    }

    // ── 快速新增表单 ──
    if (showAddFeeding) {
        FeedingFormDialog(
            babyId = babyId,
            editEntity = null,
            onDismiss = { showAddFeeding = false },
            onSave = { feeding ->
                scope.launch {
                    onAddFeeding(feeding)
                    showAddFeeding = false
                }
            },
        )
    }

    if (showAddSleep) {
        SleepFormDialog(
            babyId = babyId,
            editEntity = null,
            onDismiss = { showAddSleep = false },
            onSave = { sleep ->
                scope.launch {
                    onAddSleep(sleep)
                    showAddSleep = false
                }
            },
        )
    }

    if (showAddDiaper) {
        DiaperFormDialog(
            babyId = babyId,
            editEntity = null,
            onDismiss = { showAddDiaper = false },
            onSave = { diaper ->
                scope.launch {
                    onAddDiaper(diaper)
                    showAddDiaper = false
                }
            },
        )
    }
}
