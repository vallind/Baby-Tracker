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
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.switchcontrol.AppSwitch
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.button.AppTextButton
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.core.domain.model.Reminder
import com.babytracker.core.domain.model.ReminderType
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.longPressDeletable
import com.babytracker.designsystem.components.rememberHaptic
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 提醒中心 —— 待办提醒 + 历史提醒。
 *
 * 视觉规范：
 * - 页面背景 `c.pageBackground` 浅蓝；顶部 `Gradients.pageHeader(c)` 渐变 header + 返回按钮 + 标题"提醒中心"。
 * - Tab 切换：待办提醒 / 历史提醒（选中 `c.primary` + 下方 3dp 圆角指示器）。
 * - 待办卡片：`DT.cardRadius`(16dp) 白卡 + `DT.cardElevation` 阴影；左侧 `DT.iconBgSize`(40dp) 圆角图标背景，按 type 着色；
 *   右侧倒计时（未到期 `c.primary` / 已逾期 `c.danger`）或用药类 `Switch`。
 * - 历史卡片：灰色调，显示完成日期 + ✅。
 * - 长按卡片 → 删除确认对话框。
 * - 无 + FAB（提醒由系统/其他模块生成）。
 */
@Composable
fun ReminderScreen(navController: NavController) {
    val c = LocalAppColors.current
    val viewModel: ReminderViewModel = org.koin.androidx.compose.koinViewModel()
    val babyCtrl: BabyController = koinInject()
    val haptic = rememberHaptic()
    val state by viewModel.state.collectAsState()
    val babyId = babyCtrl.currentBabyId

    var deletingReminder by remember { mutableStateOf<Reminder?>(null) }

    LaunchedEffect(babyId) {
        if (babyId != 0) viewModel.load(babyId)
    }

    AppScaffold { padding ->
        if (babyId == 0) {
            EmptyState(
                emoji = "🍼",
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
            // —— 顶部渐变 header + 返回按钮 + 标题 ——
            ReminderHeader(onBack = { navController.popBackStack() })

            // —— Tab 切换 ——
            ReminderTabBar(tab = state.tab, onSwitch = viewModel::switchTab)

            // —— 列表 ——
            val list = if (state.tab == ReminderTab.PENDING) state.pending else state.history
            if (list.isEmpty()) {
                EmptyState(
                    emoji = if (state.tab == ReminderTab.PENDING) "🔔" else "📜",
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
                            haptic = haptic,
                            onMarkDone = { viewModel.markDone(reminder.id) },
                            onToggleEnabled = { viewModel.setEnabled(reminder.id, it) },
                            onLongPress = { deletingReminder = reminder },
                        )
                    } else {
                        HistoryReminderCard(
                            reminder = reminder,
                            haptic = haptic,
                            onLongPress = { deletingReminder = reminder },
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    // —— 删除确认对话框 ——
    deletingReminder?.let { r ->
        AppConfirmDialog(
            show = true,
            title = "删除提醒",
            message = "确定要删除「${r.title}」吗？",
            confirmText = "删除",
            cancelText = "取消",
            onConfirm = {
                viewModel.delete(r)
                deletingReminder = null
            },
            onDismiss = { deletingReminder = null },
        )
    }
}

// —— 顶部 header ——

@Composable
private fun ReminderHeader(onBack: () -> Unit) {
    val c = LocalAppColors.current
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
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = c.textPrimary,
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                "提醒中心",
                style = LocalAppTypography.current.titleLarge,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
            )
        }
    }
}

// —— Tab 切换 ——

@Composable
private fun ReminderTabBar(tab: ReminderTab, onSwitch: (ReminderTab) -> Unit) {
    val c = LocalAppColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        ReminderTab.entries.forEach { t ->
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSwitch(t) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    if (t == ReminderTab.PENDING) "待办提醒" else "历史提醒",
                    fontSize = 15.sp,
                    fontWeight = if (tab == t) FontWeight.Bold else FontWeight.Normal,
                    color = if (tab == t) c.primary else c.textSecondary,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (tab == t) c.primary else Color.Transparent),
                )
            }
        }
    }
}

// —— 待办卡片 ——

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PendingReminderCard(
    reminder: Reminder,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onMarkDone: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onLongPress: () -> Unit,
) {
    val c = LocalAppColors.current
    val (emoji, typeColor) = reminder.type.toVisual(c)

    AppCard(
        cornerRadius = 12.dp,
        elevation = 2.dp,
        containerColor = c.surface,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .longPressDeletable(haptic, onLongClick = onLongPress),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 左侧：类型图标圆角背景
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(typeColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            // 中间：标题 + 描述（+ 用药类重复规则）
            Column(Modifier.weight(1f)) {
                Text(
                    reminder.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (reminder.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        reminder.description,
                        fontSize = 12.sp,
                        color = c.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (reminder.type == ReminderType.MEDICATION && reminder.repeatRule.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        reminder.repeatRule,
                        fontSize = 11.sp,
                        color = c.textTertiary,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            // 右侧：用药类 → Switch；其它 → 倒计时 + 完成按钮
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (overdue) c.danger else c.primary,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            DateUtils.formatDate(reminder.dueDate),
                            fontSize = 11.sp,
                            color = c.textTertiary,
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(50))
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
}

// —— 历史卡片 ——

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryReminderCard(
    reminder: Reminder,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onLongPress: () -> Unit,
) {
    val c = LocalAppColors.current
    val (emoji, typeColor) = reminder.type.toVisual(c)
    val doneText = reminder.doneDate?.let { "完成于 ${DateUtils.formatDate(it)}" } ?: "已完成"

    AppCard(
        cornerRadius = 12.dp,
        elevation = 2.dp,
        containerColor = c.surface.copy(alpha = 0.7f),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .longPressDeletable(haptic, onLongClick = onLongPress),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(typeColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    reminder.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(doneText, fontSize = 12.sp, color = c.textTertiary)
            }
            Text("✅", fontSize = 18.sp)
        }
    }
}

// —— 辅助：类型 → emoji + 主题色（跟随 LocalThemeColors）——

private fun ReminderType.toVisual(c: AppColors): Pair<String, Color> = when (this) {
    ReminderType.VACCINE    -> "💉" to c.warning    // 橙
    ReminderType.CHECKUP    -> "🏥" to c.primary    // 蓝
    ReminderType.MEDICATION -> "💊" to c.success    // 绿
    ReminderType.ASSESSMENT -> "📋" to c.secondary     // 紫
    ReminderType.OTHER      -> "📌" to c.textTertiary   // 灰
}

// —— 辅助：倒计时文案 ——

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
