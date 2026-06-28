package com.babytracker.feature.feeding

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.navigation.NavController
import com.babytracker.core.database.entity.FeedingEntity
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.FeedingRepository
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.EmptyState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
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
    var editingFeeding by remember { mutableStateOf<FeedingEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = c.pageBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingFeeding = null
                    showForm = true
                },
                containerColor = c.primary,
                contentColor = c.surface,
                shape = RoundedCornerShape(DT.buttonRadius.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
                text = { Text("记录喂养", style = MaterialTheme.typography.titleSmall) },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(c.pageBackground)) {
            // —— 顶部页头 ——
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Gradients.pageHeader(c))
                    .padding(horizontal = DT.pageMargin.dp)
                    .height(DT.appBarHeight.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = c.textPrimary)
                }
                Text(
                    "喂养记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                )
            }

            if (feedings.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "🍼",
                        title = "还没有喂养记录",
                        subtitle = "点击下方按钮，记录宝宝的每一次进食",
                        actionText = "记录喂养",
                        onAction = {
                            editingFeeding = null
                            showForm = true
                        },
                    )
                }
            } else {
                val grouped = remember(feedings) { feedings.groupBy { it.timestamp.take(10) } }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = DT.pageMargin.dp,
                        end = DT.pageMargin.dp,
                        top = DT.cardGap.dp,
                        bottom = 80.dp,
                    ),
                ) {
                    grouped.forEach { (date, records) ->
                        stickyHeader(key = date) {
                            Text(
                                date,
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textSecondary,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                        items(items = records, key = { it.id }) { f ->
                            val tint = if (f.id % 2 == 1) c.warning else c.primary
                            val emoji = when (f.type) { "breast" -> "🤱"; "formula" -> "💧"; "food" -> "🥣"; else -> "🥤" }
                            val time = try { LocalDateTime.parse(f.timestamp, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("HH:mm")) } catch (_: Exception) { "" }
                            RecordCard(
                                modifier = Modifier.padding(bottom = 8.dp),
                                onDelete = {
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
                                onClick = {},
                                onLongClick = {
                                    editingFeeding = f
                                    showForm = true
                                },
                            ) {
                                Box(
                                    Modifier
                                        .size(DT.iconBgSize.dp)
                                        .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                                        .background(tint.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center,
                                ) { Text(emoji, style = MaterialTheme.typography.titleLarge) }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(DateUtils.feedingTypeLabel(f.type), style = MaterialTheme.typography.titleSmall, color = c.textPrimary, fontWeight = FontWeight.Medium)
                                    Text(
                                        when (f.type) {
                                            "breast" -> "${f.breastSide ?: "双侧"} · ${f.durationMin}分钟"
                                            "formula" -> "${f.amountMl}ml${if (f.brand != null) " · ${f.brand}" else ""}"
                                            "food" -> "${f.foodName} ${f.amountG}g"
                                            else -> "${f.amountMl}ml"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textSecondary,
                                    )
                                }
                                Text(time, style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
                            }
                        }
                    }
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

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingFormDialog(
    babyId: Int,
    editEntity: FeedingEntity? = null,
    onDismiss: () -> Unit,
    onSave: (FeedingEntity) -> Unit,
) {
    val isEdit = editEntity != null
    var type by remember { mutableStateOf(editEntity?.type ?: "breast") }
    var amountMl by remember { mutableStateOf(editEntity?.amountMl?.toString() ?: "") }
    var durationMin by remember { mutableStateOf(editEntity?.durationMin?.toString() ?: "") }
    var breastSide by remember { mutableStateOf(editEntity?.breastSide ?: "双侧") }
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
                            type = type,
                            amountMl = amountMl.toIntOrNull(),
                            durationMin = durationMin.toIntOrNull(),
                            breastSide = if (type == "breast") breastSide else null,
                            foodName = if (type == "food") foodName else null,
                            amountG = amountG.toIntOrNull(),
                            brand = brand.ifBlank { null },
                            timestamp = feedingDateTime.replace(" ", "T") + ":00",
                        )
                    } else {
                        FeedingEntity(
                            babyId = babyId, type = type,
                            amountMl = amountMl.toIntOrNull(),
                            durationMin = durationMin.toIntOrNull(),
                            breastSide = if (type == "breast") breastSide else null,
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
