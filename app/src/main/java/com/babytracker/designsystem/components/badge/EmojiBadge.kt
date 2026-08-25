package com.babytracker.designsystem.components.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
 * 语义由所在行的文本承担。
 *
 * twoTone：双色底（左右对半），用于同时表达两类属性的场景（如尿布「混合」= 青/琥珀），
 * 传入后忽略 tint 的单一浅底。
 *
 * 用法：
 *   AppEmojiBadge(emoji = "🍼", tint = c.primary)
 *   AppEmojiBadge(emoji = "🔄", tint = c.tertiary, twoTone = c.tertiary to c.warning)
 */
@Composable
fun AppEmojiBadge(
    emoji: String,
    tint: Color,
    twoTone: Pair<Color, Color>? = null,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val shape = RoundedCornerShape(BadgeDefaults.cornerRadius())
    Box(
        modifier
            .size(BadgeDefaults.size())
            .clip(shape)
            .background(if (twoTone == null) AppColorScale.fromSeed(tint).tintContainer(c) else Color.Transparent)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        if (twoTone != null) {
            // 双色底：左右两个半区（内角不裁切，外圆角由外层 clip 保证）
            Row(Modifier.matchParentSize()) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(AppColorScale.fromSeed(twoTone.first).tintContainer(c)),
                )
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(AppColorScale.fromSeed(twoTone.second).tintContainer(c)),
                )
            }
        }
        Text(emoji, fontSize = BadgeDefaults.fontSize())
    }
}
