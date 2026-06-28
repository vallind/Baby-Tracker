package com.babytracker.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes

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
) {
    val c = LocalAppColors.current
    val shapes = LocalAppShapes.current
    // 容器背景色：primaryContainer + 透明度
    // 参照：容器容器容器色 + 内容内容色的对照关系
    val bg = c.primaryContainer.copy(alpha = 0.25f)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(shapes.large))
            .background(bg)
            .padding(2.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(shapes.small))
                    .then(if (selected) Modifier.background(c.surface) else Modifier)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) c.primary else c.textSecondary,
                )
            }
        }
    }
}
