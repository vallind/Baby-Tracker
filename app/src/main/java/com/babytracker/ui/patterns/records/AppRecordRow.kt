package com.babytracker.ui.patterns.records

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.designsystem.components.section.AppListItem
import com.babytracker.designsystem.components.section.ListItemDefaults
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 记录行 — 时间线/最近记录的通用「emoji 徽章 + 标题/摘要 + 尾部时间」形态。
 *
 * 由 AppListItem 骨架与 AppEmojiBadge 组合而成：徽章走全站统一 40dp 规格
 * （BadgeTokens，替代源页面的 44dp 内联值——有意的标准化收敛），
 * 标题/摘要/时间字号颜色复用 ListItemTokens 语义（titleSmall/textPrimary、
 * labelMedium/textSecondary、labelMedium/textTertiary），不新增令牌字段。
 * emoji/tint 的业务映射（分区色纪律）留在 feature 调用方。
 *
 * 用法：
 *   AppRecordRow(emoji = "🍼", tint = c.danger, title = "母乳", subtitle = "15 分钟", trailingText = "14:30")
 */
@Composable
fun AppRecordRow(
    emoji: String,
    tint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailingText: String? = null,
) {
    val typography = LocalAppTypography.current
    AppListItem(
        headlineContent = {
            Text(title, style = typography.titleSmall, color = ListItemDefaults.titleColor())
        },
        supportingContent = {
            Text(
                subtitle,
                style = typography.labelMedium,
                color = ListItemDefaults.subtitleColor(),
                maxLines = 1,
            )
        },
        leadingContent = { AppEmojiBadge(emoji = emoji, tint = tint) },
        trailingContent = if (trailingText != null) ({
            Text(trailingText, style = typography.labelMedium, color = LocalAppColors.current.textTertiary)
        }) else null,
        onClick = onClick,
        modifier = modifier,
    )
}