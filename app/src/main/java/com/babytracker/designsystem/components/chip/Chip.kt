package com.babytracker.designsystem.components.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.accentContent
import com.babytracker.designsystem.theme.tintContainer

/**
 * 交互胶囊组件 — 对标 Palette Tag/Chip 组件，消费 AppComponentTokens.chip
 *
 * 家族职责切分（B 批收敛）：静态语义标签用 AppTag；可点选择胶囊用 AppChip。
 *
 * 三轴：selected（选中实心高亮，颜色组走令牌）× onClick/enabled（交互轴）
 *      × backgroundColor/textColor（未选中态覆盖逃生口，历史 API 保持兼容）。
 *
 * 用法：
 *   AppChip("已完成")                                          // 静态展示
 *   AppChip("7 天", selected = isSel, onClick = { pick(7) })   // 选择胶囊
 */
@Composable
fun AppChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    selected: Boolean = false,
    backgroundColor: Color = ChipDefaults.backgroundColor(),
    textColor: Color = ChipDefaults.textColor(),
    cornerRadius: Dp = ChipDefaults.cornerRadius(),
    fontSize: TextUnit = ChipDefaults.fontSize(),
    fontWeight: FontWeight = ChipDefaults.fontWeight(),
    horizontalPadding: Dp = ChipDefaults.horizontalPadding(),
    verticalPadding: Dp = ChipDefaults.verticalPadding(),
) {
    // 选中态优先走令牌颜色组；未选中回落调用方覆盖或默认令牌
    val bg = if (selected) ChipDefaults.selectedContainerColor() else backgroundColor
    val fg = if (selected) ChipDefaults.selectedTextColor() else textColor
    val interactiveModifier = if (onClick != null && enabled) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    Text(
        label,
        color = fg,
        fontSize = fontSize,
        fontWeight = fontWeight,
        modifier = interactiveModifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(bg)
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
    shape: Shape = RoundedCornerShape(ChipDefaults.cornerRadius()),
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    // 任意语义色统一走分档：选中底=浅底档，选中文字=强调档，禁止 alpha 现场调
    val activeScale = AppColorScale.fromSeed(selectedColor ?: c.primary)

    val colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = activeScale.tintContainer(c),
        selectedLabelColor = activeScale.accentContent(c),
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
