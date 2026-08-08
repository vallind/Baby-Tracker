package com.babytracker.feature.reminder

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.navigation.NavController
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.switchcontrol.AppSwitch
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.button.AppTextButton
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.core.domain.model.Reminder
import com.babytracker.core.domain.model.ReminderType
import com.babytracker.core.data.repository.ReminderRepository
import com.babytracker.designsystem.components.EmptyState
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 提醒中心 —— 待办提醒 + 历史提醒。
 */
@Composable
fun ReminderScreen(navController: NavController) {
    val c = LocalAppColors.current
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                .background(c.pageBackground),
        ) {
            ReminderHeader(onBack = { navController.popBackStack() })

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
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    Box(
        Modifier
            .fillMaxWidth()
            .background(Gradients.pageHeader(c)),
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
                    tint = c.textPrimary,
                )
            }
            Spacer(Modifier.width(spacing.xs))
            Text(
                "提醒中心",
                style = LocalAppTypography.current.titleLarge,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
            )
        }
    }
}

@Composable
private fun ReminderTabBar(tab: ReminderTab, onSwitch: (ReminderTab) -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
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
                    style = LocalAppTypography.current.bodyLarge,
                    fontWeight = if (tab == t) FontWeight.Bold else FontWeight.Normal,
                    color = if (tab == t) c.primary else c.textSecondary,
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
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
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
            Text(emoji, style = LocalAppTypography.current.titleLarge)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                reminder.title,
                style = LocalAppTypography.current.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (reminder.description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    reminder.description,
                    style = LocalAppTypography.current.labelMedium,
                    color = c.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (reminder.type == ReminderType.MEDICATION && reminder.repeatRule.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    reminder.repeatRule,
                    style = LocalAppTypography.current.labelMedium,
                    color = c.textTertiary,
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
                        style = LocalAppTypography.current.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (overdue) c.danger else c.primary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        DateUtils.formatDate(reminder.dueDate),
                        style = LocalAppTypography.current.labelMedium,
                        color = c.textTertiary,
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
                        tint = c.success,
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
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
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
            Text(emoji, style = LocalAppTypography.current.titleLarge)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                reminder.title,
                style = LocalAppTypography.current.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = c.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(doneText, style = LocalAppTypography.current.labelMedium, color = c.textTertiary)
        }
        Text("\u2705", style = LocalAppTypography.current.titleLarge)
    }
}

private fun ReminderType.toVisual(c: AppColors): Pair<String, Color> = when (this) {
    ReminderType.VACCINE    -> "\uD83D\uDC89" to c.warning
    ReminderType.CHECKUP    -> "\uD83C\uDFE5" to c.primary
    ReminderType.MEDICATION -> "\uD83D\uDC8A" to c.success
    ReminderType.ASSESSMENT -> "\uD83D\uDCCB" to c.secondary
    ReminderType.OTHER      -> "\uD83D\uDCCC" to c.textTertiary
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
