package com.babytracker.feature.timeline

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.navigation.NavController
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.recordcard.RecordCard
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
    var editingFeeding by remember { mutableStateOf<Feeding?>(null) }
    var editingSleep by remember { mutableStateOf<Sleep?>(null) }
    var editingDiaper by remember { mutableStateOf<Diaper?>(null) }
    var editingGrowth by remember { mutableStateOf<Growth?>(null) }
    var editingHealth by remember { mutableStateOf<HealthRecord?>(null) }
    var typeFilter by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(babyId) { viewModel.load(babyId) }

    Scaffold(
        containerColor = c.pageBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showTypePicker = true },
                containerColor = c.primary,
                contentColor = c.surface,
                shape = RoundedCornerShape(DT.buttonRadius.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
                text = { Text("记录", style = MaterialTheme.typography.titleSmall) },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(c.pageBackground).verticalScroll(rememberScrollState())) {
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
                    "记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                )
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = DT.pageMargin.dp, vertical = 6.dp)) {
                listOf("" to "全部", "feeding" to "🤱喂", "sleep" to "😴睡", "diaper" to "🧷尿", "growth" to "📏长", "health" to "❤️健").forEach { (key, label) ->
                    Box(Modifier.weight(1f).clickable { typeFilter = key }, contentAlignment = Alignment.Center) {
                        Text(label, fontSize = 12.sp, color = if (typeFilter == key) c.primary else c.textSecondary, fontWeight = if (typeFilter == key) FontWeight.SemiBold else null)
                    }
                }
            }
            HorizontalDivider(color = c.divider, thickness = 0.5.dp)

            Column(Modifier.padding(horizontal = DT.pageMargin.dp)) {
                if (state.loading) {
                    Spacer(Modifier.height(200.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = c.primary)
                    }
                } else if (state.items.isEmpty()) {
                    EmptyState(
                        emoji = "📝",
                        title = "还没有记录",
                        subtitle = "点击下方按钮，记录宝宝的每一次成长",
                        actionText = "记录",
                        onAction = { showTypePicker = true },
                    )
                } else {
                    val filtered = remember(state.items, typeFilter) { if (typeFilter.isEmpty()) state.items else state.items.filter { it.recordType == typeFilter } }
                    val typeColor: (String) -> Color = { when (it) {
                        "feeding" -> c.warning; "sleep" -> c.secondary; "diaper" -> c.primary
                        "growth" -> c.success; "health" -> c.primary; else -> Color.Unspecified
                    } }
                    val grouped = remember(filtered) { filtered.groupBy { it.date } }
                    var groupIndex = 0
                    grouped.forEach { (date, items) ->
                        Text(
                            text = "${DateUtils.relativeDate(date)} · ${items.size}次",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textSecondary,
                            modifier = Modifier.padding(top = if (groupIndex == 0) 0.dp else DT.cardGapSm.dp, bottom = 4.dp),
                        )
                        items.forEachIndexed { i, item ->
                            val tint = if (item.accent) c.warning else c.primary
                            RecordCard(
                                modifier = Modifier.padding(bottom = 8.dp),
                                accentColor = typeColor(item.recordType),
                                onDelete = {
                                    scope.launch {
                                        viewModel.delete(item)
                                        val result = snackbarHostState.showSnackbar(
                                            message = "已删除「${item.title}」",
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
                                    when (item.recordType) {
                                        "feeding" -> editingFeeding = viewModel.findFeeding(item.id)
                                        "sleep" -> editingSleep = viewModel.findSleep(item.id)
                                        "diaper" -> editingDiaper = viewModel.findDiaper(item.id)
                                        "growth" -> editingGrowth = viewModel.findGrowth(item.id)
                                        "health" -> editingHealth = viewModel.findHealth(item.id)
                                    }
                                },
                            ) {
                                Box(
                                    Modifier
                                        .size(DT.iconBgSize.dp)
                                        .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                                        .background(typeColor(item.recordType).takeIf { it != Color.Unspecified }?.copy(alpha = 0.14f) ?: c.primary.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center,
                                ) { Text(item.emoji, style = MaterialTheme.typography.titleLarge) }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary, fontWeight = FontWeight.Medium)
                                    Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                                }
                                if (item.time.isNotEmpty()) {
                                    Text(item.time, style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
                                }
                            }
                        }
                        groupIndex++
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showTypePicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { showTypePicker = false }, sheetState = sheetState) {
            Column(Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 8.dp)) {
                Text("选择记录类型", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
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
                            navController.navigate(screen.route)
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
}
