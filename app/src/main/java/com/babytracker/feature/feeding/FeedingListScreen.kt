package com.babytracker.feature.feeding

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.navigation.NavController
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.FeedingRepository
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedingListScreen(navController: NavController) {
    val c = LocalAppColors.current
    val feedingRepo: FeedingRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val feedings by feedingRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingFeeding by remember { mutableStateOf<Feeding?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        containerColor = c.pageBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "喂养记录",
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "选择日期",
                            tint = c.textPrimary,
                        )
                    }
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
                    .padding(horizontal = DT.pageMargin.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "$dateLabel ${selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                    style = MaterialTheme.typography.titleSmall,
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
                            val deleted = f
                            feedingRepo.delete(deleted)
                            val result = snackbarHostState.showSnackbar(
                                message = "已删除喂养记录",
                                actionLabel = "撤销",
                                duration = SnackbarDuration.Short,
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                feedingRepo.insert(deleted)
                            }
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
                    .padding(horizontal = DT.pageMargin.dp, vertical = 12.dp),
            ) {
                Button(
                    onClick = {
                        editingFeeding = null
                        showForm = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(DT.buttonRadius.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("记录喂养", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
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
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.toEpochDay() * 86400000L)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = LocalDate.ofEpochDay(millis / 86400000L)
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
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

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = DT.pageMargin.dp,
            end = DT.pageMargin.dp,
            top = 4.dp,
            bottom = 8.dp,
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
                    modifier = Modifier.width(48.dp),
                ) {
                    Text(
                        time,
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textTertiary,
                        fontSize = 11.sp,
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
                            .width(DT.timelineLineWidth.dp)
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
                            .size(DT.iconBgSize.dp)
                            .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) { Text(emoji, style = MaterialTheme.typography.titleLarge) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            typeLabel,
                            style = MaterialTheme.typography.titleSmall,
                            color = c.textPrimary,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            feedingSummary(f),
                            style = MaterialTheme.typography.bodySmall,
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
    val isEdit = editEntity != null
    var type by remember { mutableStateOf(editEntity?.let { FeedingType.raw(it.type) } ?: "breast") }
    var amountMl by remember { mutableStateOf(editEntity?.amountMl?.toString() ?: "") }
    var durationMin by remember { mutableStateOf(editEntity?.durationMin?.toString() ?: "") }
    var breastSide by remember { mutableStateOf(editEntity?.breastSide?.let { com.babytracker.core.domain.model.BreastSide.raw(it) } ?: "双侧") }
    var foodName by remember { mutableStateOf(editEntity?.foodName ?: "") }
    var amountG by remember { mutableStateOf(editEntity?.amountG?.toString() ?: "") }
    var brand by remember { mutableStateOf(editEntity?.brand ?: "") }
    val now = LocalDateTime.now()
    var feedingDateTime by remember {
        mutableStateOf(
            editEntity?.timestamp?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) { now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }
            } ?: now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        )
    }
    var showCascadePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = DT.pageMargin.dp).verticalScroll(rememberScrollState())) {
            Text(if (isEdit) "编辑喂养" else "记录喂养", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                listOf("breast" to "🤱 母乳", "formula" to "💧 配方", "food" to "🥣 辅食", "water" to "🥤 饮水").forEach { (t, label) ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t },
                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    )
                }
            }

            when (type) {
                "breast" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                        listOf("左侧", "右侧", "双侧").forEach { s ->
                            FilterChip(selected = breastSide == s, onClick = { breastSide = s }, label = { Text(s) })
                        }
                    }
                    OutlinedTextField(
                        value = durationMin, onValueChange = { durationMin = it.filter { c -> c.isDigit() } },
                        label = { Text("时长 (分钟)") }, singleLine = true,
                        leadingIcon = { Text("⏱", style = MaterialTheme.typography.titleMedium) },
                        isError = durationMin.toIntOrNull()?.let { it < 0 || it > 600 } ?: false,
                        supportingText = { if (durationMin.toIntOrNull()?.let { it < 0 || it > 600 } == true) Text("请输入 0-600 之间的数字") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                    )
                }
                "formula" -> {
                    OutlinedTextField(
                        value = amountMl, onValueChange = { amountMl = it.filter { c -> c.isDigit() } },
                        label = { Text("奶量 (ml)") }, singleLine = true,
                        leadingIcon = { Text("💧", style = MaterialTheme.typography.titleMedium) },
                        isError = amountMl.toIntOrNull()?.let { it <= 0 || it > 500 } ?: false,
                        supportingText = { if (amountMl.toIntOrNull()?.let { it <= 0 || it > 500 } == true) Text("请输入 1-500 之间的数字") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = MaterialTheme.shapes.medium,
                    )
                    OutlinedTextField(
                        value = brand, onValueChange = { brand = it },
                        label = { Text("品牌 (可选)") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                    )
                }
                "food" -> {
                    OutlinedTextField(
                        value = foodName, onValueChange = { foodName = it },
                        label = { Text("食物名称") }, singleLine = true,
                        leadingIcon = { Text("🥣", style = MaterialTheme.typography.titleMedium) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = MaterialTheme.shapes.medium,
                    )
                    OutlinedTextField(
                        value = amountG, onValueChange = { amountG = it.filter { c -> c.isDigit() } },
                        label = { Text("分量 (g)") }, singleLine = true,
                        isError = amountG.toIntOrNull()?.let { it < 0 || it > 1000 } ?: false,
                        supportingText = { if (amountG.toIntOrNull()?.let { it < 0 || it > 1000 } == true) Text("请输入 0-1000 之间的数字") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                    )
                }
                "water" -> {
                    OutlinedTextField(
                        value = amountMl, onValueChange = { amountMl = it.filter { c -> c.isDigit() } },
                        label = { Text("饮水量 (ml)") }, singleLine = true,
                        leadingIcon = { Text("🥤", style = MaterialTheme.typography.titleMedium) },
                        isError = amountMl.toIntOrNull()?.let { it < 0 || it > 1000 } ?: false,
                        supportingText = { if (amountMl.toIntOrNull()?.let { it < 0 || it > 1000 } == true) Text("请输入 0-1000 之间的数字") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = feedingDateTime, onValueChange = {}, readOnly = true, label = { Text("时间 (yyyy-MM-dd HH:mm)") }, modifier = Modifier.fillMaxWidth().clickable { showCascadePicker = true }, singleLine = true, shape = MaterialTheme.shapes.medium, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val feeding = if (isEdit) {
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
                    onSave(feeding)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium,
            ) { Text(if (isEdit) "更新" else "保存") }
            Spacer(Modifier.height(24.dp))
        }
    }

    DateTimeCascadeDialog(
        show = showCascadePicker,
        initialDateTime = feedingDateTime,
        onConfirm = { feedingDateTime = it },
        onDismiss = { showCascadePicker = false },
    )
}
