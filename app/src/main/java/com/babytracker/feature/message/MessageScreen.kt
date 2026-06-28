package com.babytracker.feature.message

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.core.domain.model.AppMessage
import com.babytracker.core.domain.model.MessageType
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.EmptyState
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(navController: NavController) {
    val c = LocalAppColors.current
    val viewModel: MessageViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = c.pageBackground,
        bottomBar = { BottomNavBar(navController) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(c.pageBackground),
        ) {
            // —— 顶部渐变 Header —— 标题 + 返回 + "全部已读" ——
            MessageHeader(
                totalUnread = state.totalUnread,
                onBack = { navController.popBackStack() },
                onMarkAllRead = { viewModel.markAllRead() },
            )

            // —— Tab 切换 ——
            MessageTabBar(
                currentTab = state.currentTab,
                interactionUnread = state.interactionUnread,
                systemUnread = state.systemUnread,
                serviceUnread = state.serviceUnread,
                onSwitch = { viewModel.switchTab(it) },
            )

            // —— 消息列表 ——
            if (state.messages.isEmpty()) {
                EmptyState(
                    emoji = when (state.currentTab) {
                        MessageType.INTERACTION -> "💬"
                        MessageType.SYSTEM -> "🔔"
                        MessageType.SERVICE -> "📋"
                    },
                    title = "暂无消息",
                    subtitle = "新的消息会在这里显示",
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = DT.pageMargin.dp,
                        vertical = DT.cardGap.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(DT.cardGapSm.dp),
                ) {
                    items(state.messages) { message ->
                        MessageCard(
                            message = message,
                            onClick = { viewModel.markRead(message.id) },
                            onDelete = { viewModel.delete(message) },
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MessageHeader(
    totalUnread: Int,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit,
) {
    val c = LocalAppColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .background(Gradients.pageHeader(c)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = DT.pageMargin.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = c.textPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "消息中心",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
            )
            Spacer(Modifier.weight(1f))
            // "全部已读" 文字按钮 — 已无未读时降透明度
            val canMarkAll = totalUnread > 0
            Text(
                "全部已读",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (canMarkAll) c.primary else c.textTertiary,
                modifier = Modifier
                    .clip(RoundedCornerShape(DT.buttonRadiusSm.dp))
                    .clickable(enabled = canMarkAll, onClick = onMarkAllRead)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun MessageTabBar(
    currentTab: MessageType,
    interactionUnread: Int,
    systemUnread: Int,
    serviceUnread: Int,
    onSwitch: (MessageType) -> Unit,
) {
    val tabs = listOf(
        Triple(MessageType.INTERACTION, "互动消息", interactionUnread),
        Triple(MessageType.SYSTEM, "系统通知", systemUnread),
        Triple(MessageType.SERVICE, "服务通知", serviceUnread),
    )
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = DT.pageMargin.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEach { (type, label, unread) ->
            MessageTabItem(
                label = label,
                unread = unread,
                selected = currentTab == type,
                onClick = { onSwitch(type) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MessageTabItem(
    label: String,
    unread: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val bgColor = if (selected) c.primary else c.surface
    val textColor = if (selected) Color.White else c.textSecondary
    Column(
        modifier
            .clip(RoundedCornerShape(DT.buttonRadius.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor,
            )
            // 未读小红点 — 仅当未读 > 0 时显示
            if (unread > 0) {
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(c.danger),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (unread > 9) "9+" else unread.toString(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        // 下方小圆角指示器
        Box(
            Modifier
                .width(20.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) Color.White else Color.Transparent),
        )
    }
}

@Composable
private fun MessageCard(
    message: AppMessage,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = LocalAppColors.current
    val cardShape = RoundedCornerShape(DT.cardRadius.dp)
    Card(
        Modifier
            .fillMaxWidth()
            .shadow(elevation = DT.cardElevation.dp, shape = cardShape)
            .clickable(onClick = onClick),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
    ) {
        Row(
            Modifier
                .padding(DT.cardInnerPadding.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 左侧头像/图标
            MessageLeadingIcon(message)
            Spacer(Modifier.width(12.dp))
            // 中间 标题 + 内容 + 时间
            Column(Modifier.weight(1f)) {
                Text(
                    message.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    message.content,
                    fontSize = 13.sp,
                    color = c.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    relativeTime(message.createTime),
                    fontSize = 11.sp,
                    color = c.textTertiary,
                )
            }
            Spacer(Modifier.width(8.dp))
            // 右侧未读红点 / 已读灰字 + 删除
            if (message.isRead) {
                Text(
                    "已读",
                    fontSize = 11.sp,
                    color = c.textTertiary,
                )
            } else {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(c.danger),
                )
            }
            Spacer(Modifier.width(6.dp))
            // 删除按钮（长按风格简化为图标点击）
            Icon(
                Icons.Default.Close,
                contentDescription = "删除",
                tint = c.textTertiary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onDelete),
            )
        }
    }
}

@Composable
private fun MessageLeadingIcon(message: AppMessage) {
    val c = LocalAppColors.current
    when (message.type) {
        MessageType.INTERACTION -> {
            // 互动：圆形头像（无 avatar 时显示首字母占位）
            val initial = message.title.take(1)
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Gradients.primary(c)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initial,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
        MessageType.SYSTEM -> {
            // 系统：c.primary 圆角图标
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                    .background(c.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        MessageType.SERVICE -> {
            // 服务：c.warning 圆角图标
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                    .background(c.warning.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Campaign,
                    contentDescription = null,
                    tint = c.warning,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

/**
 * 相对时间格式化：
 * - 1 小时内 → "刚刚" / "N分钟前"
 * - 今天内 → "HH:mm"
 * - 今年内 → "MM-dd"
 * - 跨年 → "yyyy-MM-dd"
 */
private fun relativeTime(dt: LocalDateTime): String {
    val now = LocalDateTime.now()
    val mins = ChronoUnit.MINUTES.between(dt, now)
    if (mins < 1) return "刚刚"
    if (mins < 60) return "${mins}分钟前"
    if (dt.toLocalDate() == now.toLocalDate()) {
        return dt.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    if (dt.year == now.year) {
        return dt.format(DateTimeFormatter.ofPattern("MM-dd"))
    }
    return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}
