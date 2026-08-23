package com.babytracker.designsystem.components.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.tintContainer
import com.babytracker.designsystem.theme.LocalAppColors

/**
 * 图标徽章 — 列表行/卡片内的 emoji 徽章，全站统一规格（40dp + medium 圆角）。
 *
 * 底色取语义色分档浅底（亮色 shade100 / 暗色 shade800），替代此前散落各页的
 * `color.copy(alpha = 0.12f)` 现场调色。emoji 为装饰性内容，对读屏静默，
 * 语义由所在行的文本承担（见 docs/a11y-baseline.md 装饰隔离约定）。
 *
 * 用法：
 *   AppEmojiBadge(emoji = "🍼", tint = c.primary)
 */
@Composable
fun AppEmojiBadge(
    emoji: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Box(
        modifier
            .size(BadgeDefaults.size())
            .clip(RoundedCornerShape(BadgeDefaults.cornerRadius()))
            .background(AppColorScale.fromSeed(tint).tintContainer(c))
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = BadgeDefaults.fontSize())
    }
}
