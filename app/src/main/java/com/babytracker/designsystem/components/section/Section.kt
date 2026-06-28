package com.babytracker.designsystem.components.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import com.babytracker.designsystem.theme.LocalAppTypography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.section.ListItemDefaults

/**
 * 分区标题 — 对标 Palette LayoutTokens，页面中的分区标题 + 可选操作链接
 *
 * 用法：
 *   SectionHeader(title = "最近记录", actionText = "查看全部", onAction = { ... })
 */
@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = LocalAppTypography.current.titleMedium.fontSize, fontWeight = FontWeight.Bold, color = ListItemDefaults.titleColor())
        Spacer(Modifier.weight(1f))
        if (actionText != null && onAction != null) {
            Text(
                actionText,
                fontSize = LocalAppTypography.current.labelMedium.fontSize,
                color = ListItemDefaults.actionColor(),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

/**
 * 标准列表项 — 对标 Palette ListItem，消费 AppComponentTokens.listItem
 *
 * 用法：
 *   AppListItem(
 *       icon = { Text("🍼") },
 *       title = "喂养",
 *       subtitle = "今天 5 次",
 *       trailing = { Text("14:30") },
 *       onClick = { ... },
 *   )
 */
@Composable
fun AppListItem(
    icon: @Composable (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    minHeight: Dp = ListItemDefaults.minHeight(),
    horizontalPadding: Dp = ListItemDefaults.horizontalPadding(),
    iconSize: Dp = ListItemDefaults.iconSize(),
    titleSize: TextUnit = ListItemDefaults.titleSize(),
    subtitleSize: TextUnit = ListItemDefaults.subtitleSize(),
    dividerAlpha: Float = ListItemDefaults.dividerAlpha(),
    showDivider: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                icon()
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = titleSize, fontWeight = FontWeight.Medium, color = ListItemDefaults.titleColor())
                if (subtitle != null) {
                    Text(subtitle, fontSize = subtitleSize, color = ListItemDefaults.subtitleColor())
                }
            }
            if (trailing != null) {
                trailing()
            }
        }
        if (showDivider) {
            HorizontalDivider(color = ListItemDefaults.dividerColor().copy(alpha = dividerAlpha), thickness = 0.5.dp)
        }
    }
}
