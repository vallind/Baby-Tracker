package com.babytracker.core.ui.components.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.elyon.kmp.basic.HorizontalDivider
import io.elyon.kmp.basic.Text
import io.elyon.kmp.theme.ElyonTheme

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
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ElyonTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.weight(1f))
        if (actionText != null && onAction != null) {
            Text(
                actionText,
                fontSize = 13.sp,
                color = ElyonTheme.colorScheme.primary,
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
 *       leadingContent = { Text("🍼") },
 *       headlineContent = { Text("喂养") },
 *       supportingContent = { Text("今天 5 次") },
 *       trailingContent = { Text("14:30") },
 *       onClick = { ... },
 *   )
 */
@Composable
fun AppListItem(
    headlineContent: @Composable () -> Unit,
    leadingContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    minHeight: Dp = 48.dp,
    horizontalPadding: Dp = 16.dp,
    dividerAlpha: Float = 0.12f,
    showDivider: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                headlineContent()
                if (supportingContent != null) {
                    supportingContent()
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
        if (showDivider) {
            HorizontalDivider(
                color = ElyonTheme.colorScheme.dividerLine.copy(alpha = dividerAlpha),
                thickness = 0.5.dp,
            )
        }
    }
}
