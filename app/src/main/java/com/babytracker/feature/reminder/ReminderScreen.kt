package com.babytracker.feature.reminder

import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.switchcontrol.AppSwitch
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.ui.patterns.records.RecordCard
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.util.DateUtils
import com.babytracker.core.domain.model.Reminder
import com.babytracker.core.domain.model.ReminderType
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.SegmentedControl
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.i18n.AppStrings
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

/** 提醒中心 Tab —— 待办 / 历史（纯 UI 局部状态，留在 Screen；原在 ViewModel，Batch 2 迁出） */
enum class ReminderTab { PENDING, HISTORY }

/**
 * 提醒中心 —— 待办提醒 + 历史提醒。
 *
 * 纯 UI 渲染层：只收 state + baby + 命名回调，不接触导航 / Koin / Repository / Controller。
 * 弹层显隐、Snackbar、选中 Tab 等 UI 临时状态留在本地 remember。
 */
@Composable
fun ReminderScreen(
    state: ReminderUiState,
    baby: Baby?,
    onBack: () -> Unit,
    onMarkDone: (Int) -> Unit,
    onToggleEnabled: (Int, Boolean) -> Unit,
    onDelete: (Reminder) -> Unit,
    onRestore: (Reminder) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }
    var tab by remember { mutableStateOf(ReminderTab.PENDING) }

    AppScaffold(
        modifier = modifier,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = AppStrings.reminderCenter,
                onBack = onBack,
            )
        },
    ) { padding ->
        if (baby == null) {
            EmptyState(
                emoji = "\uD83C\uDF7C",
                title = AppStrings.noBabyTitle,
                subtitle = AppStrings.reminderNoBabySubtitle,
                modifier = Modifier.padding(padding),
            )
            return@AppScaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(c.pageBackground),
        ) {
            val spacing = LocalAppSpacing.current
            ReminderTabBar(tab = tab, onSwitch = { tab = it })

            val list = if (tab == ReminderTab.PENDING) state.pending else state.history
            if (list.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = if (tab == ReminderTab.PENDING) "\uD83D\uDD14" else "\uD83D\uDCDC",
                        title = if (tab == ReminderTab.PENDING) AppStrings.reminderNoPending else AppStrings.reminderNoHistory,
                        subtitle = if (tab == ReminderTab.PENDING)
                            AppStrings.reminderNoPendingSubtitle
                        else
                            AppStrings.reminderNoHistorySubtitle,
                    )
                }
            } else {
                // 懒加载列表（2.1，P1）
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(top = spacing.xs, bottom = 88.dp),
                ) {
                    items(list, key = { it.id }) { reminder ->
                        if (tab == ReminderTab.PENDING) {
                            // 待办提醒行：滑动删除 + 徽章 + 标题/描述 + 开关或倒计时/完成（G3 收编为调用点内联组合）
                            val shapes = LocalAppShapes.current
                            val (emoji, typeColor) = reminder.type.toVisual(c)
                            RecordCard(
                                onDelete = {
                                    onDelete(reminder)
                                    scope.launch {
                                        appSnackbar.showUndo(
                                            message = String.format(Locale.US, AppStrings.reminderDeleted, reminder.title),
                                        ) { onRestore(reminder) }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = spacing.md, vertical = 6.dp),
                            ) {
                                AppEmojiBadge(emoji = emoji, tint = typeColor)
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
                                        onCheckedChange = { onToggleEnabled(reminder.id, it) },
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
                                        // 完成按钮：40dp 圆形触控目标 + 柔底（2.1 放大，C7）
                                        Box(
                                            Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(shapes.full))
                                                .background(c.surfaceMuted)
                                                .clickable(onClick = { onMarkDone(reminder.id) }),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = AppStrings.reminderMarkDone,
                                                tint = c.success,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // 历史提醒行：柔底 + 完成时间 + 对勾（G3 收编为调用点内联组合）
                            val (emoji, typeColor) = reminder.type.toVisual(c)
                            val doneText = reminder.doneDate?.let { String.format(Locale.US, AppStrings.reminderDoneAt, DateUtils.formatDate(it)) }
                                ?: AppStrings.reminderDone
                            RecordCard(
                                onDelete = {
                                    onDelete(reminder)
                                    scope.launch {
                                        appSnackbar.showUndo(
                                            message = String.format(Locale.US, AppStrings.reminderDeleted, reminder.title),
                                        ) { onRestore(reminder) }
                                    }
                                },
                                containerColor = c.surfaceMuted,
                                modifier = Modifier.padding(horizontal = spacing.md, vertical = 6.dp),
                            ) {
                                AppEmojiBadge(emoji = emoji, tint = typeColor)
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
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderTabBar(tab: ReminderTab, onSwitch: (ReminderTab) -> Unit) {
    val spacing = LocalAppSpacing.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md, vertical = spacing.sm),
    ) {
        SegmentedControl(
            labels = listOf(AppStrings.reminderPending, AppStrings.reminderHistory),
            selectedIndex = if (tab == ReminderTab.PENDING) 0 else 1,
            onSelect = { idx -> onSwitch(if (idx == 0) ReminderTab.PENDING else ReminderTab.HISTORY) },
            modifier = Modifier.fillMaxWidth(),
        )
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
        days > 0 -> String.format(Locale.US, AppStrings.reminderDaysLeft, days)
        days == 0L -> AppStrings.today
        else -> String.format(Locale.US, AppStrings.reminderOverdueDays, -days)
    }
}
