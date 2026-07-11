package com.babytracker.designsystem.components.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.theme.LocalAppColors

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
    backgroundColor: Color = ChipDefaults.backgroundColor(),
    textColor: Color = ChipDefaults.textColor(),
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    selectedColor: Color? = null,
    shape: Shape = RoundedCornerShape(8.dp),
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val activeColor = selectedColor ?: c.primary

    val colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = activeColor.copy(alpha = 0.16f),
        selectedLabelColor = activeColor,
        labelColor = c.textSecondary,
        containerColor = Color.Transparent,
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = colors,
        shape = shape,
        modifier = modifier,
    )
}
