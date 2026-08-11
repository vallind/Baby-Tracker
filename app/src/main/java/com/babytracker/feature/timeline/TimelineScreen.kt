package com.babytracker.feature.timeline
import com.babytracker.core.ui.AppShapes
import com.babytracker.core.ui.AppSpacing
import io.elyon.kmp.theme.ElyonTheme

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import io.elyon.kmp.basic.SnackbarHostState
import io.elyon.kmp.basic.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.ui.graphics.vector.ImageVector
import io.elyon.kmp.basic.Icon
import com.babytracker.navigation.Navigator
import com.babytracker.core.ui.components.scaffold.AppScaffold
import com.babytracker.core.ui.components.sheet.AppBottomSheet
import com.babytracker.core.ui.components.progress.AppCircularProgress
import com.babytracker.core.ui.components.BottomNavBar
import com.babytracker.core.ui.components.EmptyState
import com.babytracker.core.ui.components.button.AppButton
import com.babytracker.core.ui.components.button.ButtonVariant
import com.babytracker.core.ui.components.divider.AppDivider
import com.babytracker.core.ui.components.fab.AppFAB
import com.babytracker.core.ui.components.recordcard.RecordCard
import com.babytracker.core.ui.components.SegmentedControl
import com.babytracker.core.ui.components.topbar.AppTopBar
import com.babytracker.core.ui.components.snackbar.AppSnackbar
import com.babytracker.core.ui.components.snackbar.AppSnackbarHost
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.core.domain.model.*
import com.babytracker.feature.diaper.DiaperFormDialog
import com.babytracker.feature.feeding.FeedingFormDialog
import com.babytracker.feature.growth.GrowthFormDialog
import com.babytracker.feature.health.HealthFormDialog
import com.babytracker.feature.sleep.SleepFormDialog
import com.babytracker.navigation.Route
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(navigator: Navigator) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val typography = ElyonTheme.textStyles
    val shapes = com.babytracker.core.ui.AppShapes
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

    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

    LaunchedEffect(babyId) { viewModel.load(babyId) }

    // 类型 → 颜色映射
    val typeColor: (String) -> Color = {
        when (it) {
            "feeding" -> c.secondary
            "sleep" -> c.secondary
            "diaper" -> c.tertiaryContainer
            "growth" -> c.tertiaryContainer
            "health" -> c.primary
            else -> c.primary
        }
    }

    // 记录类型 → 矢量图标（替代 emoji，保证跨设备观感一致）
    val typeIcon: (String) -> ImageVector = {
        when (it) {
            "feeding" -> Icons.Filled.Restaurant
            "sleep" -> Icons.Filled.Bedtime
            "diaper" -> Icons.Filled.BabyChangingStation
            "growth" -> Icons.Filled.MonitorWeight
            "health" -> Icons.Filled.Favorite
            else -> Icons.Filled.Add
        }
    }

    AppScaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(title = "记录")
        },
        bottomBar = { BottomNavBar(navigator) },
        fab = {
            AppFAB(icon = Icons.Default.Add, onClick = { showTypePicker = true })
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(c.background),
        ) {
            // —— 类型筛选 Tab ——
            val filterKeys = listOf("", "feeding", "sleep", "diaper", "growth", "health")
            SegmentedControl(
                labels = listOf("全部", "喂", "睡", "尿", "长", "健"),
                selectedIndex = filterKeys.indexOf(typeFilter).coerceAtLeast(0),
                onSelect = { typeFilter = filterKeys[it] },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = 6.dp),
            )
            AppDivider(color = c.dividerLine, thickness = 0.5.dp)

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AppCircularProgress(indicatorColor = c.primary)
                }
            } else if (state.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "",
                        title = "还没有记录",
                        subtitle = "点击右下角按钮，记录宝宝的每一次成长",
                        actionText = "开始记录",
                        onAction = { showTypePicker = true },
                        icon = Icons.Filled.EditNote,
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
                            emoji = "",
                            title = "没有${when (typeFilter) {
                                "feeding" -> "喂养"
                                "sleep" -> "睡眠"
                                "diaper" -> "尿布"
                                "growth" -> "生长"
                                "health" -> "健康"
                                else -> ""
                            }}记录",
                            subtitle = "点击右下角按钮开始记录",
                            icon = typeIcon(typeFilter),
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
                                        style = typography.footnote1,
                                        color = c.onSurfaceVariantSummary,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(shapes.full))
                                            .background(c.dividerLine)
                                            .padding(horizontal = 6.dp, vertical = 1.dp),
                                    ) {
                                        Text(
                                            "${records.size}次",
                                            style = typography.footnote1,
                                            color = c.onSurfaceVariantSummary,
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
                                            appSnackbar.showUndo(message = "已删除「${record.title}」") { viewModel.undoLastDelete() }
                                        }
                                    },
                                    onClick = {},
                                    onLongClick = {
                                        when (record.recordType) {
                                            "feeding" -> editingFeeding = viewModel.findFeeding(record.id)
                                            "sleep" -> editingSleep = viewModel.findSleep(record.id)
                                            "diaper" -> editingDiaper = viewModel.findDiaper(record.id)
                                            "growth" -> editingGrowth = viewModel.findGrowth(record.id)
                                            "health" -> editingHealth = viewModel.findHealth(record.id)
                                        }
                                    },
                                ) {
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(shapes.large))
                                            .background(accent.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            typeIcon(record.recordType),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = accent,
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            record.title,
                                            style = typography.title3,
                                            color = c.onSurface,
                                        )
                                        Text(
                                            record.subtitle,
                                            style = typography.footnote1,
                                            color = c.onSurfaceVariantSummary,
                                        )
                                    }
                                    if (record.time.isNotEmpty()) {
                                        Text(
                                            record.time,
                                            style = typography.footnote1,
                                            color = c.onSurfaceVariantSummary,
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
                    "选择记录类型",
                    style = typography.headline2,
                    modifier = Modifier.padding(bottom = spacing.md),
                )
                val types = listOf(
                    Triple(Route.Feeding, Icons.Filled.Restaurant, "喂养"),
                    Triple(Route.Sleep, Icons.Filled.Bedtime, "睡眠"),
                    Triple(Route.Diaper, Icons.Filled.BabyChangingStation, "尿布"),
                    Triple(Route.Growth, Icons.Filled.MonitorWeight, "生长"),
                    Triple(Route.Vaccination, Icons.Filled.Vaccines, "疫苗"),
                    Triple(Route.Health, Icons.Filled.Favorite, "健康"),
                )
                types.forEach { (screen, icon, label) ->
                    AppButton(
                        variant = ButtonVariant.Text,
                        onClick = {
                            showTypePicker = false
                            when (screen) {
                                Route.Feeding -> showAddFeeding = true
                                Route.Sleep -> showAddSleep = true
                                Route.Diaper -> showAddDiaper = true
                                else -> navigator.navigate(screen)
                            }
                        },
                        label = label,
                        icon = icon,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    )
                }
                Spacer(Modifier.height(spacing.lg))
            }
        }
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
