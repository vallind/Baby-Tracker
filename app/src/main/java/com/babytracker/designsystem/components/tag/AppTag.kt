package com.babytracker.designsystem.components.tag

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/**
 * 语义标签组件 — 对标 Palette Tag 组件，消费 AppComponentTokens.tag（5 色变体）。
 *
 * 用法：
 *   AppTag("已完成", variant = TagVariant.SUCCESS)
 *   AppTag("高风险", variant = TagVariant.DANGER)
 *   AppTag("自定义", backgroundColor = Color.Cyan.copy(0.12f), textColor = Color.Cyan)
 */
enum class TagVariant { PRIMARY, SUCCESS, WARNING, DANGER, DEFAULT }

@Composable
fun AppTag(
    label: String,
    variant: TagVariant = TagVariant.DEFAULT,
    backgroundColor: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    cornerRadius: Dp = TagDefaults.cornerRadius(),
    fontSize: TextUnit = TagDefaults.fontSize(),
    fontWeight: FontWeight = TagDefaults.fontWeight(),
    horizontalPadding: Dp = TagDefaults.horizontalPadding(),
    verticalPadding: Dp = TagDefaults.verticalPadding(),
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = if (backgroundColor != Color.Unspecified && textColor != Color.Unspecified) {
        backgroundColor to textColor
    } else when (variant) {
        TagVariant.PRIMARY -> TagDefaults.primary()
        TagVariant.SUCCESS -> TagDefaults.success()
        TagVariant.WARNING -> TagDefaults.warning()
        TagVariant.DANGER -> TagDefaults.danger()
        TagVariant.DEFAULT -> TagDefaults.default()
    }

    Text(
        text = label,
        color = fg,
        fontSize = fontSize,
        fontWeight = fontWeight,
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(bg)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
    )
}
