package com.babytracker.feature.diaper
import com.babytracker.core.ui.AppShapes
import com.babytracker.core.ui.AppSpacing

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.babytracker.core.ui.components.chip.AppFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import io.elyon.kmp.basic.SnackbarHostState
import io.elyon.kmp.theme.ElyonTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babytracker.navigation.Navigator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.domain.model.DiaperType
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
import com.babytracker.core.ui.components.input.AppInput
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.DiaperRepository
import kotlinx.coroutines.launch
import com.babytracker.core.ui.components.BottomNavBar
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.core.ui.components.dialog.AppFormSheet
import com.babytracker.core.ui.components.recordcard.RecordCard
import com.babytracker.core.ui.components.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.core.ui.components.snackbar.AppSnackbar
import com.babytracker.core.ui.components.snackbar.AppSnackbarHost
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaperListScreen(navigator: Navigator) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val shapes = com.babytracker.core.ui.AppShapes
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
                            .background(Gradients.diaperSummary(ElyonTheme.colorScheme), cardShape)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(spacing.xl)
                                        .clip(RoundedCornerShape(shapes.medium))
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center,
                                ) { Text("🧷", style = ElyonTheme.textStyles.title3) }
                                Spacer(Modifier.width(spacing.sm))
                                Text(
                                    "今日尿布",
                                    style = ElyonTheme.textStyles.title3,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f),
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            Text(
                                "${filtered.size} 次",
                                style = ElyonTheme.textStyles.headline2,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Spacer(Modifier.height(spacing.xs))
                            Text(
                                "💧$wetCount  ·  💩$poopCount  ·  🔄$bothCount",
                                style = ElyonTheme.textStyles.body2,
                                color = Color.White.copy(alpha = 0.75f),
                            )
                        }
                    }
                }

                // —— 换尿布详情 ——
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
                            "换尿布详情",
                            style = ElyonTheme.textStyles.subtitle,
                            fontWeight = FontWeight.SemiBold,
                            color = c.onSurface,
                        )
                        Spacer(Modifier.height(spacing.md))
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
                            DiaperType.POOP -> c.secondary
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
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(shapes.large))
                                    .background(accentColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) { Text(typeEmoji, style = ElyonTheme.textStyles.title1) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    label,
                                    style = ElyonTheme.textStyles.subtitle,
                                    color = c.onSurface,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    timeStr,
                                    style = ElyonTheme.textStyles.footnote1,
                                    color = c.onSurfaceVariantSummary,
                                )
                            }
                            if (!d.note.isNullOrBlank()) {
                                Text(
                                    d.note.take(8),
                                    style = ElyonTheme.textStyles.footnote2,
                                    color = c.onSurfaceVariantSummary,
                                    modifier = Modifier.padding(start = spacing.sm),
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
                        editingDiaper = null
                        showForm = true
                    },
                    label = "记录尿布",
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth(),
                )
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
    val spacing = com.babytracker.core.ui.AppSpacing
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

@Composable
private fun DiaperStatCell(
    emoji: String,
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
        Text(emoji, style = ElyonTheme.textStyles.title1)
        Spacer(Modifier.height(spacing.xs))
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
