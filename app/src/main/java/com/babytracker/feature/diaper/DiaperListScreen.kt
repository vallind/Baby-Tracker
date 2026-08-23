package com.babytracker.feature.diaper

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.DiaperRepository
import kotlinx.coroutines.launch
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.recorddetail.RecordDetailSheet
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.datetimecascade.QuickTimeChipRow
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.actionbar.AppActionBar
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.designsystem.components.statcell.StatCell
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.designsystem.components.summarycard.AppSummaryCard
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.accentContent
import com.babytracker.designsystem.theme.tintContainer
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaperListScreen(navController: NavController) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val diaperRepo: DiaperRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val diapers by diaperRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingDiaper by remember { mutableStateOf<Diaper?>(null) }
    var detailDiaper by remember { mutableStateOf<Diaper?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

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
                title = AppStrings.diaperRecords,
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

            if (filtered.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "🧷",
                        title = AppStrings.emptyDiaperTitle,
                        subtitle = AppStrings.emptyDiaperSubtitle,
                        actionText = AppStrings.recordDiaper,
                        onAction = {
                            editingDiaper = null
                            showForm = true
                        },
                    )
                }
            } else {
                // —— 今日汇总大卡：移出 LazyColumn，正常布局消除负 padding ——

                AppSummaryCard(
                    emoji = "🧷",
                    title = AppStrings.diaperToday,
                    value = "${String.format(Locale.US, AppStrings.countTimes, filtered.size)}",
                    subtitle = "💧$wetCount  ·  💩$poopCount  ·  🔄$bothCount",
                    gradient = Gradients.diaperSummary(c),
                    contentColor = c.onTertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md)
                        .padding(bottom = spacing.md),
                )

                // —— 换尿布详情 ——
                AppCard(
                    containerColor = c.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md)
                        .padding(bottom = spacing.md),
                ) {
                    Column(Modifier.padding(spacing.md)) {
                        Text(
                            AppStrings.diaperDetail,
                            style = LocalAppTypography.current.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textPrimary,
                        )
                        Spacer(Modifier.height(spacing.md))
                        Row(Modifier.fillMaxWidth()) {
                            StatCell(
                                value = wetCount.toString(),
                                label = AppStrings.diaperWet,
                                unit = AppStrings.countsUnit,
                                emoji = "💧",
                                modifier = Modifier.weight(1f),
                            )
                            StatCell(
                                value = poopCount.toString(),
                                label = AppStrings.diaperPoop,
                                unit = AppStrings.countsUnit,
                                emoji = "💩",
                                modifier = Modifier.weight(1f),
                            )
                            StatCell(
                                value = bothCount.toString(),
                                label = AppStrings.diaperBoth,
                                unit = AppStrings.countsUnit,
                                emoji = "🔄",
                                modifier = Modifier.weight(1f),
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
                            scope.launch {
                                diaperRepo.delete(d)
                                appSnackbar.showUndo(message = AppStrings.deletedDiaper) { diaperRepo.update(d) }
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
                label = AppStrings.recordDiaper,
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
                scope.launch {
                    diaperRepo.delete(d)
                    appSnackbar.showUndo(message = AppStrings.deletedDiaper) { diaperRepo.update(d) }
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
        title = if (isEdit) AppStrings.editDiaper else AppStrings.recordDiaper,
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) AppStrings.updateLabel else AppStrings.save,
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = spacing.md), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            listOf("wet" to AppStrings.diaperOptionWet, "poop" to AppStrings.diaperOptionPoop, "both" to AppStrings.diaperOptionBoth).forEach { (t, label) ->
                AppFilterChip(
                    selected = selectedType == t,
                    onClick = { selectedType = t },
                    label = label,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        QuickTimeChipRow(onPick = { diaperDateTime = it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) })
        Spacer(Modifier.height(12.dp))
        AppInput(
            value = diaperDateTime,
            onValueChange = {},
            label = AppStrings.detailTime,
            enabled = false,
            modifier = Modifier.fillMaxWidth().clickable { showCascadePicker = true },
        )
        Spacer(Modifier.height(12.dp))
        AppInput(
            value = note,
            onValueChange = { note = it },
            label = AppStrings.noteOptional,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    DateTimeCascadeDialog(
        show = showCascadePicker,
        initialDateTime = diaperDateTime,
        onConfirm = { diaperDateTime = it },
        onDismiss = { showCascadePicker = false },
    )
}



/** 尿布记录详情字段 */
private fun diaperDetailFields(d: Diaper): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    val time = try {
        java.time.LocalDateTime.parse(d.timestamp, DateTimeFormatter.ISO_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    } catch (_: Exception) { d.timestamp }
    list += AppStrings.detailTime to time
    list += AppStrings.detailType to DateUtils.diaperTypeLabel(DiaperType.raw(d.type))
    d.note?.takeIf { it.isNotBlank() }?.let { list += AppStrings.detailNote to it }
    return list
}
