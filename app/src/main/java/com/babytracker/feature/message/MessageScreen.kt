package com.babytracker.feature.message

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.CategoryStripAccentColors
import com.babytracker.designsystem.theme.tintContainer
import com.babytracker.designsystem.theme.accentContent
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.ui.patterns.message.AppCategoryStrip
import com.babytracker.ui.patterns.message.AppCategoryTab
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.core.domain.model.AppMessage
import com.babytracker.core.domain.model.MessageType
import com.babytracker.designsystem.components.feedback.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.i18n.AppStrings
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private data class CategoryOverview(
    val type: MessageType,
    val label: String,
    val emoji: String,
    val bgColor: Color,
    val contentColor: Color,
)

@Composable
private fun categoryOverviews(): List<CategoryOverview> {
    val c = LocalAppColors.current
    return listOf(
        CategoryOverview(MessageType.INTERACTION, AppStrings.messageCategoryInteraction, "\uD83D\uDCAC", c.primary, c.onPrimary),
        CategoryOverview(MessageType.SYSTEM, AppStrings.messageCategorySystem, "\uD83D\uDD14", c.primary, c.onPrimary),
        CategoryOverview(MessageType.SERVICE, AppStrings.messageCategoryService, "\u2B50", c.secondary, c.onSecondary),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    state: MessageUiState,
    onMarkAllRead: () -> Unit,
    onMarkRead: (Long) -> Unit,
    onDelete: (AppMessage) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current

    var filterType by remember { mutableStateOf<MessageType?>(null) }

    val filteredMessages = remember(state.messages, filterType) {
        if (filterType == null) state.messages
        else state.messages.filter { it.type == filterType }
    }

    var deleteTarget by remember { mutableStateOf<AppMessage?>(null) }

    AppScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = AppStrings.messageCenter,
                actions = {
                    val canMarkAll = state.totalUnread > 0
                    Text(
                        AppStrings.markAllRead,
                        style = LocalAppTypography.current.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (canMarkAll) c.primary else c.textTertiary,
                        modifier = Modifier
                            .clickable(enabled = canMarkAll, onClick = onMarkAllRead)
                            .padding(horizontal = spacing.xs),
                    )
                },
            )
        },
        bottomBar = bottomBar,
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
                        title = AppStrings.messageNoData,
                        subtitle = if (filterType != null) AppStrings.messageEmptyFiltered else AppStrings.messageEmptyAll,
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
                        top = spacing.sm,
                        bottom = spacing.md,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    items(filteredMessages, key = { it.id }) { message ->
                        // 消息行：整卡即一条记录，左图标 + 标题/摘要 + 已读点（G3 收编为调用点内联组合）
                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onMarkRead(message.id) },
                            onLongClick = { deleteTarget = message },
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
                                            style = LocalAppTypography.current.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = c.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Spacer(Modifier.width(spacing.sm))
                                        Text(
                                            relativeTime(message.createTime),
                                            style = LocalAppTypography.current.labelMedium,
                                            color = c.textTertiary,
                                        )
                                    }
                                    Spacer(Modifier.height(spacing.xs))
                                    Text(
                                        message.content,
                                        style = LocalAppTypography.current.bodyMedium,
                                        color = c.textSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Spacer(Modifier.width(spacing.sm))
                                if (!message.isRead) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(c.danger),
                                    )
                                } else {
                                    Text(AppStrings.messageRead, style = LocalAppTypography.current.labelMedium, color = c.textTertiary)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(spacing.md)) }
                }
            }
        }
    }

    AppConfirmDialog(
        show = deleteTarget != null,
        title = AppStrings.confirmDelete,
        message = AppStrings.confirmDeleteMessage,
        onConfirm = {
            deleteTarget?.let(onDelete)
            deleteTarget = null
        },
        onDismiss = { deleteTarget = null },
    )
}

// 分类统计条已收编为设计系统 AppCategoryStrip；MessageType ↔ key 映射留在 feature 私有层。
private fun messageTypeKey(type: MessageType): String = type.name

@Composable
private fun CategoryOverviewBar(
    interactionUnread: Int,
    systemUnread: Int,
    serviceUnread: Int,
    selectedType: MessageType?,
    onSelect: (MessageType) -> Unit,
) {
    val c = LocalAppColors.current
    val unreadMap = mapOf(
        MessageType.INTERACTION to interactionUnread,
        MessageType.SYSTEM to systemUnread,
        MessageType.SERVICE to serviceUnread,
    )
    val overviews = categoryOverviews()
    AppCategoryStrip(
        tabs = overviews.map { cat ->
            AppCategoryTab(
                key = messageTypeKey(cat.type),
                emoji = cat.emoji,
                label = cat.label,
                badgeCount = unreadMap[cat.type] ?: 0,
            )
        },
        selectedKey = selectedType?.let(::messageTypeKey),
        onSelect = { key -> onSelect(MessageType.valueOf(key)) },
        // 逐分类强调组保真源码：互动/系统=主色组，服务=次要色组
        tabAccents = overviews.associate { cat ->
            messageTypeKey(cat.type) to CategoryStripAccentColors(cat.bgColor, cat.contentColor)
        },
    )
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
                    .background(AppColorScale.fromSeed(pickColor).tintContainer(c)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initial,
                    style = LocalAppTypography.current.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = pickColor,
                )
            }
        }
        MessageType.SYSTEM -> {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(shapes.large))
                    .background(AppColorScale.fromSeed(c.primary).tintContainer(c)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AppColorScale.fromSeed(c.primary).accentContent(c),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        MessageType.SERVICE -> {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(shapes.large))
                    .background(AppColorScale.fromSeed(c.secondary).tintContainer(c)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AppColorScale.fromSeed(c.secondary).accentContent(c),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun relativeTime(dt: LocalDateTime): String {
    val now = LocalDateTime.now()
    val mins = ChronoUnit.MINUTES.between(dt, now)
    if (mins < 1) return AppStrings.timeJustNow
    if (mins < 60) return String.format(Locale.US, AppStrings.timeMinutesAgo, mins)
    if (dt.toLocalDate() == now.toLocalDate()) {
        return dt.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    if (dt.year == now.year) {
        return dt.format(DateTimeFormatter.ofPattern("MM-dd"))
    }
    return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}
