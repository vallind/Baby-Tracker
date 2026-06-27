package com.babytracker.designsystem.components.chip

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
import com.babytracker.designsystem.components.chip.ChipDefaults
import com.babytracker.designsystem.theme.LocalThemeColors

/**
 * 标签组件 — 对标 Palette Tag/Chip 组件，消费 AppComponentTokens.chip
 *
 * 用法：
 *   AppChip("已完成")
 *   AppChip("进行中", backgroundColor = Color.Green.copy(alpha = 0.12f), textColor = Color.Green)
 */
@Composable
fun AppChip(
    label: String,
    backgroundColor: Color = LocalThemeColors.current.accent.copy(alpha = 0.12f),
    textColor: Color = LocalThemeColors.current.accent,
    cornerRadius: Dp = ChipDefaults.cornerRadius(),
    fontSize: TextUnit = ChipDefaults.fontSize(),
    fontWeight: FontWeight = ChipDefaults.fontWeight(),
    horizontalPadding: Dp = ChipDefaults.horizontalPadding(),
    verticalPadding: Dp = ChipDefaults.verticalPadding(),
    modifier: Modifier = Modifier,
) {
    Text(
        label,
        color = textColor,
        fontSize = fontSize,
        fontWeight = fontWeight,
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
    )
}
