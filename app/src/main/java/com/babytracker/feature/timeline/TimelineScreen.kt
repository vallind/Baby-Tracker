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
import androidx.navigation.NavController
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.designsystem.components.progress.AppCircularProgress
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.divider.AppDivider
import com.babytracker.designsystem.components.recorddetail.RecordDetailSheet
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.designsystem.components.SegmentedControl
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.core.util.BabyController
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.core.util.DateUtils
import com.babytracker.core.domain.model.*
import com.babytracker.feature.diaper.DiaperFormDialog
import com.babytracker.feature.feeding.FeedingFormDialog
import com.babytracker.feature.growth.GrowthFormDialog
import com.babytracker.feature.health.HealthFormDialog
import com.babytracker.feature.sleep.SleepFormDialog
import com.babytracker.navigation.Growth as GrowthRoute
import com.babytracker.navigation.Health
import com.babytracker.navigation.Vaccination
import kotlinx.coroutines.launch
import java.util.Locale
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(navController: NavController) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    val viewModel: TimelineViewModel = koinViewModel()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val state by viewModel.state.collectAsState()

    var showTypePicker by remember { mutableStateOf(false) }
    var showAddFeeding by remember { mutableStateOf(false) }
    var showAddSleep by remember { mutableStateOf(false) }
    var showAddDiaper by remember { mutableStateOf(false) }
    var editingFeeding by remember { mutableStateOf<Feeding?>(null) }
    var editingSleep by remember { mutableStateOf<Sleep?>(null) }
    var editingDiaper by remember { mutableStateOf<Diaper?>(null) }
    var editingGrowth by remember { mutableStateOf<Growth?>(null) }
    var editingHealth by remember { mutableStateOf<HealthRecord?>(null) }
    var typeFilter by remember { mutableStateOf("") }
    var detailRecord by remember { mutableStateOf<TimelineItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

    LaunchedEffect(babyId) { viewModel.load(babyId) }

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

    AppScaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(title = AppStrings.records)
        },
        bottomBar = { BottomNavBar(navController) },
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
            // —— 类型筛选 Tab ——
            val filterKeys = listOf("", "feeding", "sleep", "diaper", "growth", "health")
            SegmentedControl(
                labels = listOf(AppStrings.filterAll, AppStrings.filterFeeding, AppStrings.filterSleep, AppStrings.filterDiaper, AppStrings.filterGrowth, AppStrings.filterHealth),
                selectedIndex = filterKeys.indexOf(typeFilter).coerceAtLeast(0),
                onSelect = { typeFilter = filterKeys[it] },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
            )
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
                                            viewModel.delete(record)
                                            appSnackbar.showUndo(message = "${String.format(Locale.US, AppStrings.deletedWithTitle, record.title)}") { viewModel.undoLastDelete() }
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
        AppBottomSheet(
            show = true,
            onDismiss = { showTypePicker = false },
        ) {
            Column(Modifier.padding(horizontal = spacing.md, vertical = spacing.sm)) {
                Text(
                    AppStrings.pickRecordType,
                    style = typography.headlineMedium,
                    modifier = Modifier.padding(bottom = spacing.md),
                )
                val types = listOf(
                    AppStrings.pickerOptionFeeding to { showAddFeeding = true },
                    AppStrings.pickerOptionSleep to { showAddSleep = true },
                    AppStrings.pickerOptionDiaper to { showAddDiaper = true },
                    AppStrings.pickerOptionGrowth to { navController.navigate(GrowthRoute) },
                    AppStrings.pickerOptionVaccine to { navController.navigate(Vaccination) },
                    AppStrings.pickerOptionHealth to { navController.navigate(Health) },
                )
                types.forEach { (label, onSelect) ->
                    AppButton(
                        variant = ButtonVariant.Text,
                        onClick = {
                            showTypePicker = false
                            onSelect()
                        },
                        label = label,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    )
                }
                Spacer(Modifier.height(spacing.lg))
            }
        }
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
                when (record.recordType) {
                    "feeding" -> editingFeeding = viewModel.findFeeding(record.id)
                    "sleep" -> editingSleep = viewModel.findSleep(record.id)
                    "diaper" -> editingDiaper = viewModel.findDiaper(record.id)
                    "growth" -> editingGrowth = viewModel.findGrowth(record.id)
                    "health" -> editingHealth = viewModel.findHealth(record.id)
                }
            },
            onDelete = {
                detailRecord = null
                scope.launch {
                    viewModel.delete(record)
                    appSnackbar.showUndo(message = "${String.format(Locale.US, AppStrings.deletedWithTitle, record.title)}") { viewModel.undoLastDelete() }
                }
            },
            onDismiss = { detailRecord = null },
        )
    }

    // ── 编辑表单弹窗 ──
    editingFeeding?.let { f ->
        FeedingFormDialog(
            babyId = babyId,
            editEntity = f,
            onDismiss = { editingFeeding = null },
            onSave = { updated ->
                scope.launch {
                    viewModel.updateFeeding(updated)
                    editingFeeding = null
                }
            },
        )
    }

    editingSleep?.let { s ->
        SleepFormDialog(
            babyId = babyId,
            editEntity = s,
            onDismiss = { editingSleep = null },
            onSave = { updated ->
                scope.launch {
                    viewModel.updateSleep(updated)
                    editingSleep = null
                }
            },
        )
    }

    editingDiaper?.let { d ->
        DiaperFormDialog(
            babyId = babyId,
            editEntity = d,
            onDismiss = { editingDiaper = null },
            onSave = { updated ->
                scope.launch {
                    viewModel.updateDiaper(updated)
                    editingDiaper = null
                }
            },
        )
    }

    editingGrowth?.let { g ->
        GrowthFormDialog(
            babyId = babyId,
            editEntity = g,
            onDismiss = { editingGrowth = null },
            onSave = { updated ->
                scope.launch {
                    viewModel.updateGrowth(updated)
                    editingGrowth = null
                }
            },
        )
    }

    editingHealth?.let { h ->
        HealthFormDialog(
            babyId = babyId,
            editEntity = h,
            onDismiss = { editingHealth = null },
            onSave = { updated ->
                scope.launch {
                    viewModel.updateHealth(updated)
                    editingHealth = null
                }
            },
        )
    }

    // ── 快速新增表单 ──
    if (showAddFeeding) {
        FeedingFormDialog(
            babyId = babyId,
            editEntity = null,
            onDismiss = { showAddFeeding = false },
            onSave = { feeding ->
                scope.launch {
                    viewModel.addFeeding(feeding)
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
                    viewModel.addSleep(sleep)
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
                    viewModel.addDiaper(diaper)
                    showAddDiaper = false
                }
            },
        )
    }
}
