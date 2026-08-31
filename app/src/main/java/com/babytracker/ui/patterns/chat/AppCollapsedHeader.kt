package com.babytracker.ui.patterns.chat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.card.CardColors

/**
 * 折叠态胶囊头 — 「emoji 徽章 + 标题 + 尾部副文本 + 展开箭头，点击整条展开」复合组件
 * （G4 聊天族收编）。
 *
 * 参数全部领域无关：emoji/标题/副文本均为调用方传入的 String，
 * 副文本为 null/空时仅省略该段（箭头保留）。整条可点击展开；
 * 页面外边距留在调用方 modifier（与全站卡片调用习惯一致）。
 *
 * 用法：
 *   AppCollapsedHeader(
 *       emoji = "👶", title = babyName, subtitle = modelName,
 *       onClick = onExpand,
 *       modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm).fillMaxWidth(),
 *   )
 */
@Composable
fun AppCollapsedHeader(
    emoji: String,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardColors(containerColor = CollapsedHeaderDefaults.containerColor()),
    ) {
        Row(
            Modifier.padding(
                horizontal = CollapsedHeaderDefaults.horizontalPadding(),
                vertical = CollapsedHeaderDefaults.verticalPadding(),
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, style = CollapsedHeaderDefaults.emojiStyle())
            Spacer(Modifier.width(CollapsedHeaderDefaults.emojiTitleGap()))
            Text(
                text = title,
                style = CollapsedHeaderDefaults.titleStyle(),
                fontWeight = CollapsedHeaderDefaults.titleFontWeight(),
                color = CollapsedHeaderDefaults.titleColor(),
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = CollapsedHeaderDefaults.subtitleStyle(),
                    color = CollapsedHeaderDefaults.subtitleColor(),
                )
            }
            Spacer(Modifier.width(CollapsedHeaderDefaults.titleTrailingGap()))
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = CollapsedHeaderDefaults.chevronTint(),
                modifier = Modifier.size(CollapsedHeaderDefaults.chevronSize()),
            )
        }
    }
}