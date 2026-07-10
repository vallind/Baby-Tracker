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
import com.babytracker.designsystem.theme.LocalAppTypographyStyle
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppShapes
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

private data class CategoryOverview(
    val type: MessageType,
    val label: String,
    val emoji: String,
    val bgColor: Color,
)

@Composable
private fun categoryOverviews(): List<CategoryOverview> {
    val c = LocalAppColors.current
    return listOf(
        CategoryOverview(MessageType.INTERACTION, "互动消息", "\uD83D\uDCAC", c.primary),
        CategoryOverview(MessageType.SYSTEM, "系统通知", "\uD83D\uDD14", c.primary),
        CategoryOverview(MessageType.SERVICE, "服务通知", "\u2B50", c.secondary),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(navController: NavController) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val viewModel: MessageViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

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
                        style = LocalAppTypographyStyle.current.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (canMarkAll) c.primary else c.textTertiary,
                        modifier = Modifier
                            .clickable(enabled = canMarkAll, onClick = { viewModel.markAllRead() })
                            .padding(horizontal = spacing.xs),
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
            CategoryOverviewBar(
                interactionUnread = state.interactionUnread,
                systemUnread = state.systemUnread,
                serviceUnread = state.serviceUnread,
                selectedType = filterType,
                onSelect = { t ->
                    filterType = if (filterType == t) null else t
                },
            )

            Spacer(Modifier.height(spacing.sm))

            if (filteredMessages.isEmpty()) {
                Box(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        emoji = "\uD83D\uDCED",
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
                        start = spacing.md,
                        end = spacing.md,
                        top = 12.dp,
                        bottom = spacing.md,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    items(filteredMessages, key = { it.id }) { message ->
                        MessageCard(
                            message = message,
                            onClick = { viewModel.markRead(message.id) },
                            onDelete = { viewModel.delete(message) },
                        )
                    }
                    item { Spacer(Modifier.height(spacing.md)) }
                }
            }
        }
    }
}

@Composable
private fun CategoryOverviewBar(
    interactionUnread: Int,
    systemUnread: Int,
    serviceUnread: Int,
    selectedType: MessageType?,
    onSelect: (MessageType) -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val unreadMap = mapOf(
            MessageType.INTERACTION to interactionUnread,
            MessageType.SYSTEM to systemUnread,
            MessageType.SERVICE to serviceUnread,
        )
        categoryOverviews().forEachIndexed { i, cat ->
            val unread = unreadMap[cat.type] ?: 0
            val selected = selectedType == cat.type
            AppCard(
                cornerRadius = shapes.large,
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
                            style = LocalAppTypographyStyle.current.titleLarge,
                        )
                        Spacer(Modifier.height(spacing.xs))
                        Text(
                            cat.label,
                            style = LocalAppTypographyStyle.current.bodyMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) Color.White else c.textPrimary,
                        )
                    }
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
                                style = LocalAppTypographyStyle.current.label,
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

@Composable
private fun MessageCard(
    message: AppMessage,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    AppCard(
        cornerRadius = shapes.large,
        elevation = 2.dp,
        containerColor = c.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier
                .padding(horizontal = spacing.md, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            MessageLeadingIcon(message)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        message.title,
                        style = LocalAppTypographyStyle.current.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(spacing.sm))
                    Text(
                        relativeTime(message.createTime),
                        style = LocalAppTypographyStyle.current.label,
                        color = c.textTertiary,
                    )
                }
                Spacer(Modifier.height(spacing.xs))
                Text(
                    message.content,
                    style = LocalAppTypographyStyle.current.bodyMedium,
                    color = c.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(spacing.sm))
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
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
                    Text("已读", style = LocalAppTypographyStyle.current.label, color = c.textTertiary)
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
    val shapes = LocalAppShapes.current
    when (message.type) {
        MessageType.INTERACTION -> {
            val initial = message.title.take(1)
            val avatarColors = listOf(
                c.error, c.warning, c.primary,
                c.success, c.secondary, c.danger,
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
                    style = LocalAppTypographyStyle.current.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
        MessageType.SYSTEM -> {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(shapes.large))
                    .background(c.primary.copy(alpha = 0.12f)),
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
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(shapes.large))
                    .background(c.secondary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = c.secondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

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
