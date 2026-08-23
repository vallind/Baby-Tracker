package com.babytracker.feature.diaper

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.babytracker.designsystem.components.chip.AppFilterChip
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
                title = "尿布记录",
                onBack = { navController.popBackStack() },
                actions = {
                    AppIconButton(
                        icon = Icons.Default.DateRange,
                        onClick = { showDatePicker = true },
                        contentDescription = "选择日期",
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
            Row(
                Modifier.fillMaxWidth().padding(horizontal = spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 前一天
                AppIconButton(
                    icon = Icons.Default.ChevronLeft,
                    onClick = { selectedDate = selectedDate.minusDays(1) },
                    contentDescription = "前一天",
                    tint = c.textPrimary,
                )
                // 中间胶囊：点击开日期选择
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(c.surfaceMuted)
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$dateLabel ${selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                                style = LocalAppTypography.current.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = c.textPrimary,
                            )
                            Spacer(Modifier.width(spacing.xs))
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = c.textTertiary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                // 非今天时提供一键回跳
                if (selectedDate != today) {
                    Text(
                        AppStrings.today,
                        style = LocalAppTypography.current.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = c.primaryScale.accentContent(c),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(c.primaryScale.tintContainer(c))
                            .clickable { selectedDate = today }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                    Spacer(Modifier.width(spacing.xs))
                }
                // 后一天
                AppIconButton(
                    icon = Icons.Default.ChevronRight,
                    onClick = { selectedDate = selectedDate.plusDays(1) },
                    contentDescription = "后一天",
                    tint = c.textPrimary,
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

                AppSummaryCard(
                    emoji = "🧷",
                    title = "今日尿布",
                    value = "${filtered.size} 次",
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
                            "换尿布详情",
                            style = LocalAppTypography.current.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textPrimary,
                        )
                        Spacer(Modifier.height(spacing.md))
                        Row(Modifier.fillMaxWidth()) {
                            StatCell(
                                value = wetCount.toString(),
                                label = "小便",
                                unit = "次",
                                emoji = "💧",
                                modifier = Modifier.weight(1f),
                            )
                            StatCell(
                                value = poopCount.toString(),
                                label = "大便",
                                unit = "次",
                                emoji = "💩",
                                modifier = Modifier.weight(1f),
                            )
                            StatCell(
                                value = bothCount.toString(),
                                label = "混合",
                                unit = "次",
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
                        val accentColor = when (d.type) {
                            DiaperType.WET -> c.primary
                            DiaperType.POOP -> c.warning
                            DiaperType.BOTH -> c.error
                        }

                        RecordCard(
                        onDelete = {
                            scope.launch {
                                diaperRepo.delete(d)
                                appSnackbar.showUndo(message = "已删除尿布记录") { diaperRepo.update(d) }
                            }
                        },
                            onClick = {},
                            onLongClick = {
                                editingDiaper = d
                                showForm = true
                            },
                            modifier = Modifier.padding(bottom = spacing.sm),
                        ) {
                            AppEmojiBadge(emoji = typeEmoji, tint = accentColor)
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
                label = "记录尿布",
                icon = Icons.Default.Add,
                onClick = {
                    editingDiaper = null
                    showForm = true
                },
            )
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
        title = if (isEdit) "编辑尿布" else "记录尿布",
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) "更新" else "保存",
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = spacing.md), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            listOf("wet" to "💧 小便", "poop" to "💩 大便", "both" to "🔄 混合").forEach { (t, label) ->
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
            label = "时间",
            enabled = false,
            modifier = Modifier.fillMaxWidth().clickable { showCascadePicker = true },
        )
        Spacer(Modifier.height(12.dp))
        AppInput(
            value = note,
            onValueChange = { note = it },
            label = "备注 (可选)",
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

