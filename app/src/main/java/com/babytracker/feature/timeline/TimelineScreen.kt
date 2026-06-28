package com.babytracker.feature.timeline

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.navigation.NavController
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.SegmentedControl
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.core.domain.model.*
import com.babytracker.feature.diaper.DiaperFormDialog
import com.babytracker.feature.feeding.FeedingFormDialog
import com.babytracker.feature.growth.GrowthFormDialog
import com.babytracker.feature.health.HealthFormDialog
import com.babytracker.feature.sleep.SleepFormDialog
import com.babytracker.navigation.Screen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(navController: NavController) {
    val c = LocalAppColors.current
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

    LaunchedEffect(babyId) { viewModel.load(babyId) }

    // 类型 → 颜色映射
    val typeColor: (String) -> Color = {
        when (it) {
            "feeding" -> c.warning
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

    Scaffold(
        containerColor = c.pageBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "记录",
                onBack = { navController.popBackStack() },
            )
        },
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
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
                labels = listOf("全部", "🤱喂", "😴睡", "🧷尿", "📏长", "❤️健"),
                selectedIndex = filterKeys.indexOf(typeFilter).coerceAtLeast(0),
                onSelect = { typeFilter = filterKeys[it] },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DT.pageMargin.dp, vertical = 6.dp),
            )
            HorizontalDivider(color = c.divider, thickness = 0.5.dp)

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.primary)
                }
            } else if (state.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "📝",
                        title = "还没有记录",
                        subtitle = "点击右下角按钮，记录宝宝的每一次成长",
                        actionText = "开始记录",
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
                                "feeding" -> "喂养"
                                "sleep" -> "睡眠"
                                "diaper" -> "尿布"
                                "growth" -> "生长"
                                "health" -> "健康"
                                else -> ""
                            }}记录",
                            subtitle = "点击右下角按钮开始记录",
                        )
                    }
                } else {
                    val grouped = remember(filtered) { filtered.groupBy { it.date } }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = DT.pageMargin.dp,
                            end = DT.pageMargin.dp,
                            top = 8.dp,
                            bottom = 80.dp,
                        ),
                    ) {
                        grouped.forEach { (date, records) ->
                            // —— 日期分组标题 ——
                            item(key = "header-$date") {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = DateUtils.relativeDate(date),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = c.textSecondary,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(c.divider)
                                            .padding(horizontal = 6.dp, vertical = 1.dp),
                                    ) {
                                        Text(
                                            "${records.size}次",
                                            fontSize = 10.sp,
                                            color = c.textTertiary,
                                        )
                                    }
                                }
                            }

                            // —— 记录卡片列表 ——
                            items(items = records, key = { "${it.recordType}-${it.id}" }) { record ->
                                val accent = typeColor(record.recordType)
                                RecordCard(
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    accentColor = accent,
                                    onDelete = {
                                        scope.launch {
                                            viewModel.delete(record)
                                            val result = snackbarHostState.showSnackbar(
                                                message = "已删除「${record.title}」",
                                                actionLabel = "撤销",
                                                duration = SnackbarDuration.Short,
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.undoLastDelete()
                                            }
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
                                            .size(DT.iconBgSize.dp)
                                            .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                                            .background(accent.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            record.emoji,
                                            style = MaterialTheme.typography.titleLarge,
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            record.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = c.textPrimary,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            record.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = c.textSecondary,
                                        )
                                    }
                                    if (record.time.isNotEmpty()) {
                                        Text(
                                            record.time,
                                            style = MaterialTheme.typography.labelMedium,
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
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { showTypePicker = false }, sheetState = sheetState) {
            Column(Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 8.dp)) {
                Text(
                    "选择记录类型",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                val types = listOf(
                    Screen.Feeding to "🤱 喂养",
                    Screen.Sleep to "😴 睡眠",
                    Screen.Diaper to "🧷 尿布",
                    Screen.Growth to "📏 生长",
                    Screen.Vaccination to "💉 疫苗",
                    Screen.Health to "❤️ 健康",
                )
                types.forEach { (screen, label) ->
                    TextButton(
                        onClick = {
                            showTypePicker = false
                            when (screen) {
                                Screen.Feeding -> showAddFeeding = true
                                Screen.Sleep -> showAddSleep = true
                                Screen.Diaper -> showAddDiaper = true
                                else -> navController.navigate(screen.route)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyLarge, color = c.textPrimary)
                    }
                }
                Spacer(Modifier.height(24.dp))
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
