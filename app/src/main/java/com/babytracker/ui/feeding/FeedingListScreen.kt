package com.babytracker.ui.feeding

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.navigation.NavController
import com.babytracker.core.database.entity.FeedingEntity
import com.babytracker.core.theme.DT
import com.babytracker.core.theme.LocalThemeColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.data.repository.FeedingRepository
import com.babytracker.ui.components.rememberHaptic
import com.babytracker.ui.components.EmptyState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingListScreen(navController: NavController) {
    val c = LocalThemeColors.current
    val feedingRepo: FeedingRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val haptic = rememberHaptic()
    val scope = rememberCoroutineScope()
    val babyId = babyCtrl.currentBabyId
    if (babyId == 0) return
    val feedings by feedingRepo.watchByBaby(babyId).collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var deletingFeeding by remember { mutableStateOf<FeedingEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("喂养记录") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = DT.pageMargin.dp, vertical = 12.dp),
                color = Color.Transparent,
            ) {
                Button(
                    onClick = { showForm = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("记录喂养", style = MaterialTheme.typography.titleSmall)
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = DT.pageMargin.dp).verticalScroll(rememberScrollState())) {
            if (feedings.isEmpty()) {
                EmptyState(
                    emoji = "🍼",
                    title = "还没有喂养记录",
                    subtitle = "点击下方按钮，记录宝宝的每一次进食",
                )
            }
            val grouped = feedings.groupBy { it.timestamp.take(10) }
            grouped.forEach { (date, items) ->
                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                items.forEach { f ->
                TimelineItem(
                    time = try { LocalDateTime.parse(f.timestamp, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("HH:mm")) } catch (_: Exception) { "" },
                    color = when (f.type) { "breast" -> c.blue; "formula" -> c.green; "food" -> c.yellow; else -> c.cyan },
                    emoji = when (f.type) { "breast" -> "🤱"; "formula" -> "💧"; "food" -> "🥣"; else -> "🥤" },
                    title = DateUtils.feedingTypeLabel(f.type),
                    subtitle = when (f.type) {
                        "breast" -> "${f.breastSide ?: "双侧"} · ${f.durationMin}分钟"
                        "formula" -> "${f.amountMl}ml${if (f.brand != null) " · ${f.brand}" else ""}"
                        "food" -> "${f.foodName} ${f.amountG}g"
                        else -> "${f.amountMl}ml"
                    },
                    onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); deletingFeeding = f },
                )
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showForm) {
        FeedingFormDialog(
            babyId = babyId,
            onDismiss = { showForm = false },
            onSave = { feeding ->
                scope.launch { feedingRepo.insert(feeding) }
                showForm = false
            },
        )
    }

    deletingFeeding?.let { f ->
        AlertDialog(
            onDismissRequest = { deletingFeeding = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条喂养记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { feedingRepo.delete(f) }
                    deletingFeeding = null
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingFeeding = null }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingFormDialog(babyId: Int, onDismiss: () -> Unit, onSave: (FeedingEntity) -> Unit) {
    var type by remember { mutableStateOf("breast") }
    var amountMl by remember { mutableStateOf("") }
    var durationMin by remember { mutableStateOf("") }
    var breastSide by remember { mutableStateOf("双侧") }
    var foodName by remember { mutableStateOf("") }
    var amountG by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    val now = LocalDateTime.now()
    var feedingDateTime by remember { mutableStateOf(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = DT.pageMargin.dp).verticalScroll(rememberScrollState())) {
            Text("记录喂养", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 16.dp))

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
            OutlinedTextField(value = feedingDateTime, onValueChange = {}, readOnly = true, label = { Text("时间 (yyyy-MM-dd HH:mm)") }, modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }, singleLine = true, shape = MaterialTheme.shapes.medium, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(FeedingEntity(
                        babyId = babyId, type = type,
                        amountMl = amountMl.toIntOrNull(),
                        durationMin = durationMin.toIntOrNull(),
                        breastSide = if (type == "breast") breastSide else null,
                        foodName = if (type == "food") foodName else null,
                        amountG = amountG.toIntOrNull(),
                        brand = brand.ifBlank { null },
                        timestamp = feedingDateTime.replace(" ", "T") + ":00",
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium,
            ) { Text("保存") }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                showDatePicker = false
                datePickerState.selectedDateMillis?.let { millis ->
                    val instant = java.time.Instant.ofEpochMilli(millis)
                    val date = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val timePart = feedingDateTime.substring(11)
                    feedingDateTime = "$date $timePart"
                }
                showTimePicker = true
            }) { Text("下一步") }
        }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        var hour by remember { mutableIntStateOf(feedingDateTime.substring(11, 13).toIntOrNull() ?: 12) }
        var minute by remember { mutableIntStateOf(feedingDateTime.substring(14, 16).toIntOrNull() ?: 0) }
        AlertDialog(onDismissRequest = { showTimePicker = false }, title = { Text("选择时间") }, text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { if (hour < 23) hour++ }) { Text("▲") }
                        Text("%02d".format(hour), style = MaterialTheme.typography.headlineMedium)
                        TextButton(onClick = { if (hour > 0) hour-- }) { Text("▼") }
                        Text("时", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(" : ", style = MaterialTheme.typography.headlineMedium)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { if (minute < 59) minute++ }) { Text("▲") }
                        Text("%02d".format(minute), style = MaterialTheme.typography.headlineMedium)
                        TextButton(onClick = { if (minute > 0) minute-- }) { Text("▼") }
                        Text("分", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                val datePart = feedingDateTime.take(10)
                feedingDateTime = "$datePart ${"%02d".format(hour)}:${"%02d".format(minute)}"
                showTimePicker = false
            }) { Text("确定") }
        }, dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineItem(time: String, color: androidx.compose.ui.graphics.Color, emoji: String, title: String, subtitle: String, onLongClick: () -> Unit = {}) {
    val c = LocalThemeColors.current
    Row(Modifier.padding(vertical = 8.dp)) {
        Text(time, Modifier.width(60.dp).padding(top = 8.dp), style = MaterialTheme.typography.titleSmall)
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(color).border(3.dp, color.copy(alpha = 0.2f), CircleShape))
            Box(Modifier.width(2.dp).height(60.dp).background(MaterialTheme.colorScheme.outlineVariant))
        }
        Spacer(Modifier.width(12.dp))
        Card(
            Modifier.fillMaxWidth().heightIn(min = 80.dp).combinedClickable(onLongClick = onLongClick, onClick = {}),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(DT.iconBgSize.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Text(emoji, style = MaterialTheme.typography.titleLarge) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}
