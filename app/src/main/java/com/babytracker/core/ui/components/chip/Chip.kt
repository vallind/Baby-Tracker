package com.babytracker.core.ui.components.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.elyon.kmp.basic.Surface
import io.elyon.kmp.basic.Text
import io.elyon.kmp.theme.ElyonTheme

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
    backgroundColor: Color = ElyonTheme.colorScheme.secondaryContainer,
    textColor: Color = ElyonTheme.colorScheme.onSecondaryContainer,
    cornerRadius: Dp = 20.dp,
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    horizontalPadding: Dp = 10.dp,
    verticalPadding: Dp = 4.dp,
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

/**
 * 统一 FilterChip 组件 — 接入 AppColors 主题系统。
 *
 * 变体：
 * - 默认（selectedColor=null）：选中时使用主题 primary 色
 * - 着色（selectedColor 指定）：用于日志等级、健康分类等场景
 *
 * 用法：
 *   AppFilterChip(selected = type == "wet", onClick = { type = "wet" }, label = "小便")
 *   AppFilterChip(selected = true, onClick = {}, label = "警告", selectedColor = c.warning)
 */
@Composable
fun AppFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    selectedColor: Color? = null,
    shape: Shape = RoundedCornerShape(8.dp),
    modifier: Modifier = Modifier,
) {
    val activeColor = selectedColor ?: ElyonTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        shape = shape,
        color = if (selected) activeColor.copy(alpha = 0.16f) else Color.Transparent,
        contentColor = if (selected) activeColor else ElyonTheme.colorScheme.onSurfaceVariantSummary,
        modifier = modifier,
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}
