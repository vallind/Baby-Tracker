package com.babytracker.feature.feeding

import android.content.SharedPreferences
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.navigation.NavController
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.button.AppTextButton
import com.babytracker.designsystem.components.button.PrimaryButton
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.FeedingRepository
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedingListScreen(navController: NavController) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val feedingRepo: FeedingRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val feedings by feedingRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingFeeding by remember { mutableStateOf<Feeding?>(null) }
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "喂养记录",
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
                    style = LocalAppTypography.current.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                )
                Spacer(Modifier.width(spacing.xs))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = c.textTertiary,
                    modifier = Modifier.size(18.dp),
                )
            }

            if (filteredFeedings.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "\uD83C\uDF7C",
                        title = "还没有喂养记录",
                        subtitle = "点击底部按钮，记录宝宝的每一次进食",
                        actionText = "记录喂养",
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
                    onDelete = { f ->
                        scope.launch {
                            feedingRepo.delete(f)
                            appSnackbar.showUndo(message = "已删除喂养记录") { feedingRepo.update(f) }
                        }
                    },
                    onEdit = { f ->
                        editingFeeding = f
                        showForm = true
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            // —— 底部固定按钮 ——
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .padding(horizontal = spacing.md, vertical = 12.dp),
            ) {
                PrimaryButton(
                    onClick = {
                        editingFeeding = null
                        showForm = true
                    },
                    label = "记录喂养",
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
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
                    if (editingFeeding != null) {
                        feedingRepo.update(feeding)
                    } else {
                        feedingRepo.insert(feeding)
                    }
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

/** 喂养类型对应的颜色 */
private fun feedingColor(type: FeedingType, c: AppColors): Color = when (type) {
    FeedingType.BREAST -> c.danger
    FeedingType.FORMULA -> c.primary
    FeedingType.FOOD -> c.warning
    else -> c.info
}

/** 喂养类型对应的 emoji */
private fun feedingEmoji(type: FeedingType): String = when (type) {
    FeedingType.BREAST -> "\uD83E\uDD31"
    FeedingType.FORMULA -> "\uD83C\uDF7C"
    FeedingType.FOOD -> "\uD83E\uDD63"
    else -> "\uD83E\uDD64"
}

/** 喂养记录摘要文本 */
private fun feedingSummary(f: Feeding): String = when (f.type) {
    FeedingType.BREAST -> {
        val side = f.breastSide?.let { com.babytracker.core.domain.model.BreastSide.raw(it) } ?: "双侧"
        val mlPart = f.amountMl?.let { "${it}ml" } ?: ""
        if (mlPart.isNotEmpty()) "$mlPart, $side \u00B7 ${f.durationMin}分钟"
        else "$side \u00B7 ${f.durationMin}分钟"
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
    onDelete: (Feeding) -> Unit,
    onEdit: (Feeding) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            ) {
                // —— 左侧时间轴（时间 + 圆点 + 竖线）——
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(spacing.xxl),
                ) {
                    Text(
                        time,
                        style = LocalAppTypography.current.labelSmall,
                        color = c.textTertiary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                    // 竖线填满剩余空间
                    Box(
                        Modifier
                            .width(spacing.xxs)
                            .weight(1f)
                            .background(c.divider),
                    )
                }

                Spacer(Modifier.width(12.dp))

                // —— 右侧卡片 ——
                RecordCard(
                    onDelete = { onDelete(f) },
                    onClick = {},
                    onLongClick = { onEdit(f) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 10.dp),
                    accentColor = color,
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(shapes.large))
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) { Text(emoji, style = LocalAppTypography.current.titleLarge) }
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
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingFormDialog(
    babyId: Int,
    editEntity: Feeding? = null,
    onDismiss: () -> Unit,
    onSave: (Feeding) -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val isEdit = editEntity != null
    var type by remember { mutableStateOf(editEntity?.let { FeedingType.raw(it.type) } ?: "breast") }
    var amountMl by remember { mutableStateOf(editEntity?.amountMl?.toString() ?: "") }
    var durationMin by remember { mutableStateOf(editEntity?.durationMin?.toString() ?: "") }
    var breastSide by remember { mutableStateOf(editEntity?.breastSide?.let { com.babytracker.core.domain.model.BreastSide.raw(it) } ?: "双侧") }
    var foodName by remember { mutableStateOf(editEntity?.foodName ?: "") }
    var amountG by remember { mutableStateOf(editEntity?.amountG?.toString() ?: "") }
    var brand by remember { mutableStateOf(editEntity?.brand ?: "") }
    val now = LocalDateTime.now()
    val prefs: SharedPreferences = koinInject()

    var feedingDateTime by remember {
        mutableStateOf(
            editEntity?.timestamp?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) { now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }
            } ?: if (prefs.getBoolean("feeding_timer_running", false)) {
                prefs.getString("feeding_timer_form_start_time", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))!!
            } else {
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            }
        )
    }
    var showCascadePicker by remember { mutableStateOf(false) }

    // 计时器（持久化：关闭表单再打开继续计时）
    var timerRunning by remember {
        mutableStateOf(prefs.getBoolean("feeding_timer_running", false))
    }
    var timerStartMs by remember {
        mutableLongStateOf(prefs.getLong("feeding_timer_start_millis", 0L))
    }
    var elapsed by remember {
        mutableIntStateOf(
            if (editEntity != null && !prefs.getBoolean("feeding_timer_running", false)) {
                (editEntity.durationMin ?: 0) * 60
            } else {
                0
            }
        )
    }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (true) {
                elapsed = ((System.currentTimeMillis() - timerStartMs) / 1000).toInt()
                delay(1000L)
            }
        }
    }

    val timerDisplay = String.format("%02d:%02d", elapsed / 60, elapsed % 60)

    val buildEntity = {
        if (timerRunning) {
            timerRunning = false
            durationMin = (elapsed / 60).toString()
            feedingDateTime = java.time.Instant.ofEpochMilli(timerStartMs)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            prefs.edit()
                .putBoolean("feeding_timer_running", false)
                .remove("feeding_timer_form_start_time")
                .apply()
        }
        if (isEdit) {
            editEntity.copy(
                type = FeedingType.fromRaw(type),
                amountMl = amountMl.toIntOrNull(),
                durationMin = durationMin.toIntOrNull(),
                breastSide = if (type == "breast") com.babytracker.core.domain.model.BreastSide.fromRaw(breastSide) else null,
                foodName = if (type == "food") foodName else null,
                amountG = amountG.toIntOrNull(),
                brand = brand.ifBlank { null },
                timestamp = feedingDateTime.replace(" ", "T") + ":00",
            )
        } else {
            Feeding(
                babyId = babyId, type = FeedingType.fromRaw(type),
                amountMl = amountMl.toIntOrNull(),
                durationMin = durationMin.toIntOrNull(),
                breastSide = if (type == "breast") com.babytracker.core.domain.model.BreastSide.fromRaw(breastSide) else null,
                foodName = if (type == "food") foodName else null,
                amountG = amountG.toIntOrNull(),
                brand = brand.ifBlank { null },
                timestamp = feedingDateTime.replace(" ", "T") + ":00",
            )
        }
    }

    AppFormSheet(
        title = if (isEdit) "编辑喂养" else "记录喂养",
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) "更新" else "保存",
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = spacing.md), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            listOf("breast" to "🤱 母乳", "formula" to "💧 配方", "food" to "🥣 辅食", "water" to "🥤 饮水").forEach { (t, label) ->
                AppFilterChip(
                    selected = type == t,
                    onClick = { type = t },
                    label = label,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        when (type) {
            "breast" -> {
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    listOf("左侧", "右侧", "双侧").forEach { s ->
                        AppFilterChip(selected = breastSide == s, onClick = { breastSide = s }, label = s, modifier = Modifier.weight(1f))
                    }
                }
                // 计时器
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                ) {
                    Text(
                        text = timerDisplay,
                        style = LocalAppTypography.current.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (timerRunning) c.primary else c.textSecondary,
                        modifier = Modifier.weight(1f).widthIn(min = 100.dp),
                        textAlign = TextAlign.Start,
                    )
                    if (timerRunning) {
                        PrimaryButton(
                    onClick = {
                        timerRunning = false
                        durationMin = (elapsed / 60).toString()
                        feedingDateTime = java.time.Instant.ofEpochMilli(timerStartMs)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        prefs.edit()
                            .putBoolean("feeding_timer_running", false)
                            .remove("feeding_timer_form_start_time")
                            .apply()
                    },
                            label = "结束计时",
                        )
                    } else {
                        PrimaryButton(
                            onClick = {
                        timerStartMs = System.currentTimeMillis()
                        elapsed = 0
                        timerRunning = true
                        prefs.edit()
                            .putBoolean("feeding_timer_running", true)
                            .putLong("feeding_timer_start_millis", timerStartMs)
                            .putString("feeding_timer_form_start_time", feedingDateTime)
                            .apply()
                            },
                            label = "开始计时",
                        )
                    }
                }
                AppInput(
                    value = durationMin,
                    onValueChange = { durationMin = it.filter { c -> c.isDigit() } },
                    label = "时长 (分钟)",
                    leadingIcon = { Text("⏱", style = LocalAppTypography.current.titleLarge) },
                    isError = durationMin.toIntOrNull()?.let { it < 0 || it > 600 } ?: false,
                    errorMessage = if (durationMin.toIntOrNull()?.let { it < 0 || it > 600 } == true) "请输入 0-600 之间的数字" else null,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            "formula" -> {
                AppInput(
                    value = amountMl,
                    onValueChange = { amountMl = it.filter { c -> c.isDigit() } },
                    label = "奶量 (ml)",
                    leadingIcon = { Text("💧", style = LocalAppTypography.current.titleLarge) },
                    isError = amountMl.toIntOrNull()?.let { it <= 0 || it > 500 } ?: false,
                    errorMessage = if (amountMl.toIntOrNull()?.let { it <= 0 || it > 500 } == true) "请输入 1-500 之间的数字" else null,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
                AppInput(
                    value = brand,
                    onValueChange = { brand = it },
                    label = "品牌 (可选)",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            "food" -> {
                AppInput(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = "食物名称",
                    leadingIcon = { Text("🥣", style = LocalAppTypography.current.titleLarge) },
                    modifier = Modifier.fillMaxWidth(),
                )
                AppInput(
                    value = amountG,
                    onValueChange = { amountG = it.filter { c -> c.isDigit() } },
                    label = "分量 (g)",
                    isError = amountG.toIntOrNull()?.let { it < 0 || it > 1000 } ?: false,
                    errorMessage = if (amountG.toIntOrNull()?.let { it < 0 || it > 1000 } == true) "请输入 0-1000 之间的数字" else null,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            "water" -> {
                AppInput(
                    value = amountMl,
                    onValueChange = { amountMl = it.filter { c -> c.isDigit() } },
                    label = "饮水量 (ml)",
                    leadingIcon = { Text("🥤", style = LocalAppTypography.current.titleLarge) },
                    isError = amountMl.toIntOrNull()?.let { it < 0 || it > 1000 } ?: false,
                    errorMessage = if (amountMl.toIntOrNull()?.let { it < 0 || it > 1000 } == true) "请输入 0-1000 之间的数字" else null,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        AppInput(
            value = feedingDateTime,
            onValueChange = {},
            label = "时间 (yyyy-MM-dd HH:mm)",
            enabled = false,
            modifier = Modifier.fillMaxWidth().clickable { showCascadePicker = true },
        )
    }

    DateTimeCascadeDialog(
        show = showCascadePicker,
        initialDateTime = feedingDateTime,
        onConfirm = { feedingDateTime = it },
        onDismiss = { showCascadePicker = false },
    )
}
