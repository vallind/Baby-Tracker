package com.babytracker.feature.message

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.navigation.NavController
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.core.domain.model.AppMessage
import com.babytracker.core.domain.model.MessageType
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// —— 分类概览元数据 ——
private data class CategoryOverview(
    val type: MessageType,
    val label: String,
    val emoji: String,
    val bgColor: Color,
)

private val CATEGORY_OVERVIEWS = listOf(
    CategoryOverview(MessageType.INTERACTION, "互动消息", "💬", Color(0xFF2196F3)),
    CategoryOverview(MessageType.SYSTEM, "系统通知", "🔔", Color(0xFF2196F3)),
    CategoryOverview(MessageType.SERVICE, "服务通知", "⭐", Color(0xFF9C27B0)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(navController: NavController) {
    val c = LocalAppColors.current
    val viewModel: MessageViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    // 分类筛选：null = 显示全部，否则只显示指定类型
    var filterType by remember { mutableStateOf<MessageType?>(null) }

    val filteredMessages = remember(state.messages, filterType) {
        if (filterType == null) state.messages
        else state.messages.filter { it.type == filterType }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "消息中心",
                actions = {
                    val canMarkAll = state.totalUnread > 0
                    Text(
                        "全部已读",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (canMarkAll) c.primary else c.textTertiary,
                        modifier = Modifier
                            .clickable(enabled = canMarkAll, onClick = { viewModel.markAllRead() })
                            .padding(horizontal = 4.dp),
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
            // —— 分类概览卡片区 ——
            CategoryOverviewBar(
                interactionUnread = state.interactionUnread,
                systemUnread = state.systemUnread,
                serviceUnread = state.serviceUnread,
                selectedType = filterType,
                onSelect = { t ->
                    filterType = if (filterType == t) null else t
                },
            )

            Spacer(Modifier.height(8.dp))

            // —— 消息列表 ——
            if (filteredMessages.isEmpty()) {
                Box(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        emoji = "📭",
                        title = "暂无消息",
                        subtitle = if (filterType != null) "该分类暂无消息" else "新的消息会在这里显示",
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredMessages, key = { it.id }) { message ->
                        MessageCard(
                            message = message,
                            onClick = { viewModel.markRead(message.id) },
                            onDelete = { viewModel.delete(message) },
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// —— 分类概览卡片栏（3列宫格）——
@Composable
private fun CategoryOverviewBar(
    interactionUnread: Int,
    systemUnread: Int,
    serviceUnread: Int,
    selectedType: MessageType?,
    onSelect: (MessageType) -> Unit,
) {
    val c = LocalAppColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val unreadMap = mapOf(
            MessageType.INTERACTION to interactionUnread,
            MessageType.SYSTEM to systemUnread,
            MessageType.SERVICE to serviceUnread,
        )
        CATEGORY_OVERVIEWS.forEachIndexed { i, cat ->
            val unread = unreadMap[cat.type] ?: 0
            val selected = selectedType == cat.type
            AppCard(
                cornerRadius = 12.dp,
                elevation = 2.dp,
                containerColor = if (selected) cat.bgColor else c.surface,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(cat.type) },
            ) {
                Row(
                    Modifier
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            cat.emoji,
                            fontSize = 22.sp,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            cat.label,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) Color.White else c.textPrimary,
                        )
                    }
                    // 未读角标
                    if (unread > 0) {
                        Box(
                            Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) Color.White.copy(alpha = 0.3f) else c.danger,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                if (unread > 99) "99+" else unread.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

// —— 消息卡片：互动=圆形头像+未读角标，系统=蓝色铃铛，服务=紫色星标 ——
@Composable
private fun MessageCard(
    message: AppMessage,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = LocalAppColors.current
    AppCard(
        cornerRadius = 12.dp,
        elevation = 2.dp,
        containerColor = c.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // 左侧头像/图标
            MessageLeadingIcon(message)
            Spacer(Modifier.width(12.dp))
            // 中间：标题 + 内容 + 时间
            Column(Modifier.weight(1f)) {
                // 标题行：名称/标题 + 时间
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        message.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        relativeTime(message.createTime),
                        fontSize = 11.sp,
                        color = c.textTertiary,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    message.content,
                    fontSize = 13.sp,
                    color = c.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            // 右侧：未读红点 + 删除按钮
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!message.isRead) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(c.danger),
                    )
                } else {
                    Text("已读", fontSize = 11.sp, color = c.textTertiary)
                }
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
}

@Composable
private fun MessageLeadingIcon(message: AppMessage) {
    val c = LocalAppColors.current
    when (message.type) {
        MessageType.INTERACTION -> {
            // 互动：圆形彩色头像（首字母）
            val initial = message.title.take(1)
            val avatarColors = listOf(
                Color(0xFFF44336), Color(0xFFFF9800), Color(0xFF2196F3),
                Color(0xFF4CAF50), Color(0xFF9C27B0), Color(0xFFFF5722),
            )
            val pickColor = avatarColors[initial.hashCode().let { ((it % avatarColors.size) + avatarColors.size) % avatarColors.size }]
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(pickColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initial,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
        MessageType.SYSTEM -> {
            // 系统：蓝色铃铛圆角图标
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2196F3).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        MessageType.SERVICE -> {
            // 服务：紫色星标圆角图标
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF9C27B0).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(20.dp),
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
