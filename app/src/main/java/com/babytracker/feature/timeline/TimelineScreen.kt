package com.babytracker.feature.timeline

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.navigation.NavController
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalCareTypePalette
import com.babytracker.designsystem.theme.CareType
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.designsystem.components.progress.AppCircularProgress
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.button.AppTextButton
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.RequireBaby
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.SegmentedControl
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.datetimecascade.CascadeMode
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(navController: NavController) {
    val c = LocalAppColors.current
    val viewModel: TimelineViewModel = koinViewModel()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    RequireBaby(babyId = babyId.toLong(), navController = navController) {
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
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateLabel = remember(selectedDate, today) {
        when {
            selectedDate == today -> "今天"
            selectedDate == today.minusDays(1) -> "昨天"
            selectedDate == today.plusDays(1) -> "明天"
            else -> selectedDate.format(DateTimeFormatter.ofPattern("MM月dd日"))
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

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
    val paletteEmoji = LocalCareTypePalette.current
    val typeEmoji: (String) -> String = {
        when (it) {
            "feeding" -> paletteEmoji.of(CareType.FEEDING).emoji
            "sleep" -> paletteEmoji.of(CareType.SLEEP).emoji
            "diaper" -> paletteEmoji.of(CareType.DIAPER).emoji
            "growth" -> paletteEmoji.of(CareType.GROWTH).emoji
            "health" -> paletteEmoji.of(CareType.HEALTH).emoji
            else -> "📝"
        }
    }

    AppScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(title = "记录")
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
                labels = listOf("全部", "🤱喂", "😴睡", "🧷尿", "📏长", "❤️健"),
                selectedIndex = filterKeys.indexOf(typeFilter).coerceAtLeast(0),
                onSelect = { typeFilter = filterKeys[it] },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
            HorizontalDivider(color = c.divider, thickness = 0.5.dp)

            // —— 日期导航栏 ——
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AppIconButton(
                    icon = Icons.Default.KeyboardArrowLeft,
                    onClick = { selectedDate = selectedDate.minusDays(1) },
                    contentDescription = "前一天",
                    tint = c.textPrimary,
                )
                Row(
                    Modifier.clickable { showDatePicker = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "$dateLabel ${selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                        style = LocalAppTypography.current.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = c.textPrimary,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = c.textTertiary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                AppIconButton(
                    icon = Icons.Default.KeyboardArrowRight,
                    onClick = { selectedDate = selectedDate.plusDays(1) },
                    contentDescription = "后一天",
                    tint = c.textPrimary,
                )
            }
            HorizontalDivider(color = c.divider, thickness = 0.5.dp)

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AppCircularProgress(indicatorColor = c.primary)
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
                val filtered = remember(state.items, typeFilter, selectedDate) {
                    val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val typeFiltered = if (typeFilter.isEmpty()) state.items
                    else state.items.filter { it.recordType == typeFilter }
                    typeFiltered.filter { it.date == dateStr }
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
                            start = 16.dp,
                            end = 16.dp,
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
                                        style = LocalAppTypography.current.labelSmall,
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
                                val editRecord: () -> Unit = {
                                    when (record.recordType) {
                                        "feeding" -> editingFeeding = viewModel.findFeeding(record.id)
                                        "sleep" -> editingSleep = viewModel.findSleep(record.id)
                                        "diaper" -> editingDiaper = viewModel.findDiaper(record.id)
                                        "growth" -> editingGrowth = viewModel.findGrowth(record.id)
                                        "health" -> editingHealth = viewModel.findHealth(record.id)
                                    }
                                }
                                RecordCard(
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    accentColor = accent,
                                    onDelete = {
                                        scope.launch {
                                            viewModel.delete(record)
                                            appSnackbar.showUndo(message = "已删除「${record.title}」") { viewModel.undoLastDelete() }
                                        }
                                    },
                                    onClick = editRecord,
                                    onLongClick = editRecord,
                                ) {
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(accent.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            record.emoji,
                                            style = LocalAppTypography.current.titleLarge,
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            record.title,
                                            style = LocalAppTypography.current.titleSmall,
                                            color = c.textPrimary,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            record.subtitle,
                                            style = LocalAppTypography.current.bodySmall,
                                            color = c.textSecondary,
                                        )
                                    }
                                    if (record.time.isNotEmpty()) {
                                        Text(
                                            record.time,
                                            style = LocalAppTypography.current.labelMedium,
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
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    "选择记录类型",
                    style = LocalAppTypography.current.headlineSmall,
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
                    AppTextButton(
                        onClick = {
                            showTypePicker = false
                            when (screen) {
                                Screen.Feeding -> showAddFeeding = true
                                Screen.Sleep -> showAddSleep = true
                                Screen.Diaper -> showAddDiaper = true
                                else -> navController.navigate(screen.route)
                            }
                        },
                        label = label,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    )
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

    // 日期选择对话框
    DateTimeCascadeDialog(
        show = showDatePicker,
        initialDateTime = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
        mode = CascadeMode.DATE_ONLY,
        onConfirm = { dateStr ->
            selectedDate = LocalDate.parse(dateStr)
        },
        onDismiss = { showDatePicker = false },
    )
    }
}
