package com.babytracker.feature.diaper

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.babytracker.designsystem.components.chip.AppOptionChipRow
import com.babytracker.ui.patterns.records.DateNavCapsule
import com.babytracker.ui.patterns.records.appDateNavLabel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.domain.model.DiaperType
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.ui.patterns.dashboard.AppHeroStatCard
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.core.util.DateUtils
import kotlinx.coroutines.launch
import com.babytracker.ui.patterns.records.RecordDetailSheet
import com.babytracker.designsystem.components.quickstat.QuickStatPill
import com.babytracker.ui.patterns.records.AppDateTimeField
import com.babytracker.ui.patterns.records.DateTimeCascadeDialog
import com.babytracker.ui.patterns.records.QuickTimeChipRow
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.ui.patterns.records.RecordCard
import com.babytracker.designsystem.components.actionbar.AppActionBar
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.designsystem.components.statcell.StatCell
import com.babytracker.designsystem.components.feedback.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.ui.patterns.dashboard.AppSummaryCard
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.ui.i18n.AppStringsProduct
import com.babytracker.designsystem.theme.accentContent
import com.babytracker.designsystem.theme.tintContainer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 尿布记录页 — 纯 UI 渲染层：只收 state + 命名回调，不接触导航 / Koin / Repository / Controller。
 * 表单 Sheet 显隐、日期选择器显隐等 UI 临时状态留在本地 remember；
 * 数据加载、删除/撤销、表单保存全部在 DiaperViewModel；
 * 日期筛选为页面状态（原则 #9），与表单显隐一起留 Screen 本地 remember。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaperListScreen(
    state: DiaperUiState,
    bottomBar: @Composable () -> Unit = {},
    onBack: () -> Unit = {},
    onDelete: (Diaper) -> Unit = {},
    onUndoDelete: () -> Unit = {},
    onSave: (Diaper) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val scope = rememberCoroutineScope()
    val babyId = state.babyId
    if (babyId == 0) return
    val diapers = state.diapers
    var showForm by remember { mutableStateOf(false) }
    var editingDiaper by remember { mutableStateOf<Diaper?>(null) }
    var detailDiaper by remember { mutableStateOf<Diaper?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

    // 日期选择状态（筛选为页面状态留 Screen，显隐也留 Screen）
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
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

    // 日期显示文本（简写：今天 · 8月23日，不再拼 ISO 日期）
    val dateLabel = remember(selectedDate, today) { appDateNavLabel(selectedDate, today) }

    AppScaffold(
        modifier = modifier,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = AppStringsProduct.diaperRecords,
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

            if (filtered.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "🧷",
                        title = AppStringsProduct.emptyDiaperTitle,
                        subtitle = AppStringsProduct.emptyDiaperSubtitle,
                        actionText = AppStringsProduct.recordDiaper,
                        onAction = {
                            editingDiaper = null
                            showForm = true
                        },
                    )
                }
            } else {
                // 今日尿布合并行：青渐变大卡 + n 次大数字 + 小便/大便/混合三格（2.1 合并原「换尿布详情」卡）
                // 收编：渐变底容器统一走设计系统 AppHeroStatCard（观感与原实现一致）
                val summaryTypography = LocalAppTypography.current
                val contentColor = c.onTertiary
                AppHeroStatCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md)
                        .padding(bottom = spacing.md),
                    gradient = Gradients.diaperSummary(c),
                    contentColor = contentColor,
                    emoji = "🧷",
                    title = AppStringsProduct.diaperToday,
                ) {
                    Text(
                        "${String.format(Locale.US, AppStringsProduct.countTimes, filtered.size)}",
                        style = summaryTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                    )
                    Spacer(Modifier.height(spacing.lg))
                    Row(Modifier.fillMaxWidth()) {
                        QuickStatPill(value = wetCount.toString(), label = AppStringsProduct.diaperWet, unit = AppStringsProduct.countsUnit, contentColor = contentColor, modifier = Modifier.weight(1f))
                        QuickStatPill(value = poopCount.toString(), label = AppStringsProduct.diaperPoop, unit = AppStringsProduct.countsUnit, contentColor = contentColor, modifier = Modifier.weight(1f))
                        QuickStatPill(value = bothCount.toString(), label = AppStringsProduct.diaperBoth, unit = AppStringsProduct.countsUnit, contentColor = contentColor, modifier = Modifier.weight(1f))
                    }
                }

                // —— 记录列表 ——
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
                        // 分区色纪律：小便=青 / 大便=琥珀 / 混合=双色徽章（error 红退出尿布页）
                        val accentColor = when (d.type) {
                            DiaperType.WET -> c.tertiary
                            DiaperType.POOP -> c.warning
                            DiaperType.BOTH -> c.tertiary
                        }

                        RecordCard(
                        onDelete = {
                            onDelete(d)
                            scope.launch {
                                appSnackbar.showUndo(message = AppStringsProduct.deletedDiaper) { onUndoDelete() }
                            }
                        },
                            onClick = { detailDiaper = d },
                            onLongClick = {
                                editingDiaper = d
                                showForm = true
                            },
                            modifier = Modifier.padding(bottom = spacing.sm),
                        ) {
                            AppEmojiBadge(
                                emoji = typeEmoji,
                                tint = accentColor,
                                twoTone = if (d.type == DiaperType.BOTH) (c.tertiary to c.warning) else null,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    label,
                                    style = LocalAppTypography.current.titleSmall,
                                    color = c.textPrimary,
                                    fontWeight = FontWeight.Medium,
                                )
                                if (!d.note.isNullOrBlank()) {
                                    Text(
                                        d.note.take(12),
                                        style = LocalAppTypography.current.bodySmall,
                                        color = c.textSecondary,
                                        maxLines = 1,
                                    )
                                }
                            }
                            Text(
                                timeStr,
                                style = LocalAppTypography.current.labelMedium,
                                color = c.textTertiary,
                            )
                        }
                    }

                    // 底部留白给按钮
                    item { Spacer(Modifier.height(spacing.sm)) }
                }
            }

            // —— 底部主操作条（DS 统一件） ——
            AppActionBar(
                label = AppStringsProduct.recordDiaper,
                icon = Icons.Default.Add,
                onClick = {
                    editingDiaper = null
                    showForm = true
                },
            )
        }
    }

    // 单击卡片 = 详情弹层
    detailDiaper?.let { d ->
        RecordDetailSheet(
            show = true,
            title = DateUtils.diaperTypeLabel(DiaperType.raw(d.type)),
            emoji = when (d.type) { DiaperType.WET -> "💧"; DiaperType.POOP -> "💩"; DiaperType.BOTH -> "🔄" },
            tint = when (d.type) { DiaperType.WET -> c.tertiary; DiaperType.POOP -> c.warning; DiaperType.BOTH -> c.tertiary },
            fields = diaperDetailFields(d),
            onEdit = {
                detailDiaper = null
                editingDiaper = d
                showForm = true
            },
            onDelete = {
                onDelete(d)
                scope.launch {
                    appSnackbar.showUndo(message = AppStringsProduct.deletedDiaper) { onUndoDelete() }
                }
            },
            onDismiss = { detailDiaper = null },
        )
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
                onSave(d)
                showForm = false
                editingDiaper = null
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
fun DiaperFormDialog(
    babyId: Int,
    editEntity: Diaper? = null,
    onDismiss: () -> Unit,
    onSave: (Diaper) -> Unit,
) {
    val spacing = LocalAppSpacing.current
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
        title = if (isEdit) AppStringsProduct.editDiaper else AppStringsProduct.recordDiaper,
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) AppStringsProduct.updateLabel else AppStrings.save,
    ) {
        AppOptionChipRow(
            options = listOf("wet" to AppStringsProduct.diaperOptionWet, "poop" to AppStringsProduct.diaperOptionPoop, "both" to AppStringsProduct.diaperOptionBoth),
            selectedKey = selectedType,
            onSelect = { selectedType = it },
            modifier = Modifier.padding(bottom = spacing.md),
        )

        Spacer(Modifier.height(12.dp))
        QuickTimeChipRow(onPick = { diaperDateTime = it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) })
        Spacer(Modifier.height(12.dp))
        AppDateTimeField(
            label = AppStringsProduct.detailTime,
            value = diaperDateTime,
            onPick = { diaperDateTime = it },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        AppInput(
            value = note,
            onValueChange = { note = it },
            label = AppStringsProduct.noteOptional,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}



/** 尿布记录详情字段 */
private fun diaperDetailFields(d: Diaper): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    val time = try {
        java.time.LocalDateTime.parse(d.timestamp, DateTimeFormatter.ISO_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    } catch (_: Exception) { d.timestamp }
    list += AppStringsProduct.detailTime to time
    list += AppStringsProduct.detailType to DateUtils.diaperTypeLabel(DiaperType.raw(d.type))
    d.note?.takeIf { it.isNotBlank() }?.let { list += AppStringsProduct.detailNote to it }
    return list
}