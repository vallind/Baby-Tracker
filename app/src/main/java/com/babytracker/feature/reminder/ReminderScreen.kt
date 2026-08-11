package com.babytracker.feature.reminder
import com.babytracker.core.ui.AppShapes
import com.babytracker.core.ui.AppSpacing

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.ExperimentalFoundationApi
import io.elyon.kmp.basic.Icon
import io.elyon.kmp.basic.SnackbarHostState
import io.elyon.kmp.basic.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytracker.navigation.Navigator
import io.elyon.kmp.theme.ElyonTheme
import com.babytracker.core.ui.Gradients
import com.babytracker.core.ui.components.scaffold.AppScaffold
import com.babytracker.core.ui.components.card.AppCard
import com.babytracker.core.ui.components.switchcontrol.AppSwitch
import com.babytracker.core.ui.components.dialog.AppConfirmDialog
import com.babytracker.core.ui.components.recordcard.RecordCard
import com.babytracker.core.ui.components.snackbar.AppSnackbar
import com.babytracker.core.ui.components.snackbar.AppSnackbarHost
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.core.domain.model.Reminder
import com.babytracker.core.domain.model.ReminderType
import com.babytracker.core.data.repository.ReminderRepository
import com.babytracker.core.ui.components.EmptyState
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 提醒中心 —— 待办提醒 + 历史提醒。
 */
@Composable
fun ReminderScreen(navigator: Navigator) {
    val c = ElyonTheme.colorScheme
    val viewModel: ReminderViewModel = org.koin.androidx.compose.koinViewModel()
    val babyCtrl: BabyController = koinInject()
    val reminderRepo: ReminderRepository = koinInject()
    val state by viewModel.state.collectAsState()
    val babyId = babyCtrl.currentBabyId
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

    LaunchedEffect(babyId) {
        if (babyId != 0) viewModel.load(babyId)
    }

    AppScaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { padding ->
        if (babyId == 0) {
            EmptyState(
                emoji = "\uD83C\uDF7C",
                title = "还没有添加宝宝",
                subtitle = "添加宝宝后即可查看提醒",
                modifier = Modifier.padding(padding),
            )
            return@AppScaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(c.background),
        ) {
            ReminderHeader(onBack = { navigator.pop() })

            ReminderTabBar(tab = state.tab, onSwitch = viewModel::switchTab)

            val list = if (state.tab == ReminderTab.PENDING) state.pending else state.history
            if (list.isEmpty()) {
                EmptyState(
                    emoji = if (state.tab == ReminderTab.PENDING) "\uD83D\uDD14" else "\uD83D\uDCDC",
                    title = if (state.tab == ReminderTab.PENDING) "暂无待办提醒" else "暂无历史提醒",
                    subtitle = if (state.tab == ReminderTab.PENDING)
                        "疫苗 / 体检 / 用药 / 发育评估到期后会出现在这里"
                    else
                        "完成的提醒会归档至此",
                )
            } else {
                list.forEach { reminder ->
                    if (state.tab == ReminderTab.PENDING) {
                        PendingReminderCard(
                            reminder = reminder,
                            onMarkDone = { viewModel.markDone(reminder.id) },
                            onToggleEnabled = { viewModel.setEnabled(reminder.id, it) },
                            onDelete = {
                                scope.launch {
                                    reminderRepo.delete(reminder)
                                    appSnackbar.showUndo(message = "已删除「${reminder.title}」") { reminderRepo.update(reminder) }
                                }
                            },
                        )
                    } else {
                        HistoryReminderCard(
                            reminder = reminder,
                            onDelete = {
                                scope.launch {
                                    reminderRepo.delete(reminder)
                                    appSnackbar.showUndo(message = "已删除「${reminder.title}」") { reminderRepo.update(reminder) }
                                }
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ReminderHeader(onBack: () -> Unit) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val shapes = com.babytracker.core.ui.AppShapes
    Box(
        Modifier
            .fillMaxWidth()
            .background(Gradients.pageHeader(ElyonTheme.colorScheme)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(shapes.full))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = c.onSurface,
                )
            }
            Spacer(Modifier.width(spacing.xs))
            Text(
                "提醒中心",
                style = ElyonTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
                color = c.onSurface,
            )
        }
    }
}

@Composable
private fun ReminderTabBar(tab: ReminderTab, onSwitch: (ReminderTab) -> Unit) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val shapes = com.babytracker.core.ui.AppShapes
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md, vertical = 12.dp),
    ) {
        ReminderTab.entries.forEach { t ->
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(shapes.large))
                    .clickable { onSwitch(t) }
                    .padding(vertical = spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    if (t == ReminderTab.PENDING) "待办提醒" else "历史提醒",
                    style = ElyonTheme.textStyles.body1,
                    fontWeight = if (tab == t) FontWeight.Bold else FontWeight.Normal,
                    color = if (tab == t) c.primary else c.onSurfaceVariantSummary,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .width(spacing.lg)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (tab == t) c.primary else Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun PendingReminderCard(
    reminder: Reminder,
    onMarkDone: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val shapes = com.babytracker.core.ui.AppShapes
    val (emoji, typeColor) = reminder.type.toVisual(c)

    RecordCard(
        onDelete = onDelete,
        modifier = Modifier.padding(horizontal = spacing.md, vertical = 6.dp),
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(shapes.large))
                .background(typeColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, style = ElyonTheme.textStyles.title1)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                reminder.title,
                style = ElyonTheme.textStyles.body1,
                fontWeight = FontWeight.SemiBold,
                color = c.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (reminder.description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    reminder.description,
                    style = ElyonTheme.textStyles.footnote1,
                    color = c.onSurfaceVariantSummary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (reminder.type == ReminderType.MEDICATION && reminder.repeatRule.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    reminder.repeatRule,
                    style = ElyonTheme.textStyles.footnote1,
                    color = c.onSurfaceVariantSummary,
                )
            }
        }
        Spacer(Modifier.width(spacing.sm))
        if (reminder.type == ReminderType.MEDICATION) {
            AppSwitch(
                checked = reminder.isEnabled,
                onCheckedChange = onToggleEnabled,
                checkedColor = c.primary,
            )
        } else {
            val countdown = reminder.countdownText()
            val overdue = reminder.isOverdue()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        countdown,
                        style = ElyonTheme.textStyles.body2,
                        fontWeight = FontWeight.SemiBold,
                        color = if (overdue) c.error else c.primary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        DateUtils.formatDate(reminder.dueDate),
                        style = ElyonTheme.textStyles.footnote1,
                        color = c.onSurfaceVariantSummary,
                    )
                }
                Spacer(Modifier.width(spacing.xs))
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(shapes.full))
                        .clickable(onClick = onMarkDone),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "标记完成",
                        tint = c.tertiaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryReminderCard(
    reminder: Reminder,
    onDelete: () -> Unit,
) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val shapes = com.babytracker.core.ui.AppShapes
    val (emoji, typeColor) = reminder.type.toVisual(c)
    val doneText = reminder.doneDate?.let { "完成于 ${DateUtils.formatDate(it)}" } ?: "已完成"

    RecordCard(
        onDelete = onDelete,
        containerColor = c.surface.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = spacing.md, vertical = 6.dp),
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(shapes.large))
                .background(typeColor.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, style = ElyonTheme.textStyles.title1)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                reminder.title,
                style = ElyonTheme.textStyles.body1,
                fontWeight = FontWeight.Medium,
                color = c.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(doneText, style = ElyonTheme.textStyles.footnote1, color = c.onSurfaceVariantSummary)
        }
        Text("\u2705", style = ElyonTheme.textStyles.title1)
    }
}

private fun ReminderType.toVisual(c: io.elyon.kmp.theme.Colors): Pair<String, Color> = when (this) {
    ReminderType.VACCINE    -> "\uD83D\uDC89" to c.secondary
    ReminderType.CHECKUP    -> "\uD83C\uDFE5" to c.primary
    ReminderType.MEDICATION -> "\uD83D\uDC8A" to c.tertiaryContainer
    ReminderType.ASSESSMENT -> "\uD83D\uDCCB" to c.secondary
    ReminderType.OTHER      -> "\uD83D\uDCCC" to c.onSurfaceVariantSummary
}

private fun Reminder.isOverdue(): Boolean {
    val today = LocalDate.now()
    return !isDone && dueDate.toLocalDate().isBefore(today)
}

private fun Reminder.countdownText(): String {
    val today = LocalDate.now()
    val days = ChronoUnit.DAYS.between(today, dueDate.toLocalDate())
    return when {
        days > 0 -> "还有${days}天"
        days == 0L -> "今天"
        else -> "已逾期${-days}天"
    }
}
