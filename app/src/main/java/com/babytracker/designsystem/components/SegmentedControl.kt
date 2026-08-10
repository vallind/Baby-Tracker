package com.babytracker.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.elyon.kmp.basic.Text
import io.elyon.kmp.theme.ElyonTheme

/**
 * 分段选择器 —— 用于标签切换（全部/喂养/睡眠/...）。
 *
 * 圆角：外层 shapes.large（12dp），内层选中项 shapes.small（6dp）。
 *
 * 用法：
 * ```
 * var selected by remember { mutableIntStateOf(0) }
 * SegmentedControl(
 *     labels = listOf("全部", "喂养", "睡眠"),
 *     selectedIndex = selected,
 *     onSelect = { selected = it },
 * )
 * ```
 */
@Composable
fun SegmentedControl(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = ElyonTheme.colorScheme.surfaceContainer,
    selectedContainerColor: androidx.compose.ui.graphics.Color = ElyonTheme.colorScheme.surfaceContainerHighest,
    selectedContentColor: androidx.compose.ui.graphics.Color = ElyonTheme.colorScheme.onSurfaceContainerHighest,
    unselectedContentColor: androidx.compose.ui.graphics.Color = ElyonTheme.colorScheme.onSurfaceVariantSummary,
    cornerRadius: Dp = 12.dp,
    innerCornerRadius: Dp = 6.dp,
    borderWidth: Dp = 2.dp,
    fontSize: TextUnit = 13.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    selectedFontWeight: FontWeight = FontWeight.SemiBold,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .padding(borderWidth),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(innerCornerRadius))
                    .then(if (selected) Modifier.background(selectedContainerColor) else Modifier)
                    .clickable { onSelect(index) }
                    .semantics {
                        // 无障碍：声明 Tab 角色与选中态，让 TalkBack 朗读"哪个段、选中与否"
                        role = Role.Tab
                        this.selected = selected
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = fontSize,
                    fontWeight = if (selected) selectedFontWeight else fontWeight,
                    color = if (selected) selectedContentColor else unselectedContentColor,
                )
            }
        }
    }
}
