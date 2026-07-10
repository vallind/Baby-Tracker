package com.babytracker.designsystem.components.timepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * TDesign 风格滚轮选择器。
 *
 * 围绕 currentValue 生成 visibleItems 个项（前后各 halfVisible 个），
 * Column 固定在 Box 内零偏移，第 halfVisible 项自然对齐高亮背景。
 * 拖拽通过累积 dragOffset → 松手吸附到最近项。
 */
@Composable
fun TimePickerLogic(
    value: Int,
    range: IntRange,
    itemHeight: Dp,
    visibleItems: Int,
    selectedBgColor: Color,
    selectedTextColor: Color,
    unselectedTextColor: Color,
    dividerColor: Color,
    onValueChanged: (Int) -> Unit,
) {
    val halfVisible = visibleItems / 2
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    // 内部状态：当前显示的值（拖拽过程中实时更新）
    var currentValue by remember { mutableIntStateOf(value) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    // 外部 value 变化时同步
    LaunchedEffect(value) {
        currentValue = value
    }

    // 生成可见项的值列表（围绕 currentValue，始终 visibleItems 个）
    // 边界处不 coerceIn（避免重复），超出范围用 null 占位
    val visibleValues = remember(currentValue, range) {
        val half = visibleItems / 2
        val start = currentValue - half
        val end = currentValue + half
        (start..end).map { v ->
            if (v in range) v else null
        }
    }

    // Column 零偏移：visibleValues[halfVisible] 即 currentValue，自然对齐高亮背景
    val contentOffsetY = dragOffset

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight * visibleItems)
            .clipToBounds(),
    ) {
        // 选中行高亮背景（固定在中间行）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .offset { IntOffset(0, (itemHeightPx * halfVisible).roundToInt()) }
                .background(selectedBgColor, RoundedCornerShape(4.dp)),
        )
        // 上分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .offset { IntOffset(0, (itemHeightPx * halfVisible).roundToInt()) }
                .background(dividerColor),
        )
        // 下分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .offset { IntOffset(0, (itemHeightPx * (halfVisible + 1)).roundToInt()) }
                .background(dividerColor),
        )

        // 可见项内容
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, contentOffsetY.roundToInt()) }
                .pointerInput(range) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            // 根据拖拽距离计算新值
                            val dragItems = (dragOffset / itemHeightPx).roundToInt()
                            val newValue = (currentValue - dragItems)
                                .coerceIn(range.first, range.last)
                            dragOffset = 0f
                            currentValue = newValue
                            onValueChanged(newValue)
                        },
                        onDragCancel = {
                            dragOffset = 0f
                        },
                        onVerticalDrag = { _, dragAmount ->
                            dragOffset += dragAmount
                        },
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            visibleValues.forEach { v ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    if (v != null) {
                        val isSelected = v == currentValue
                        Text(
                            text = "%02d".format(v),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = if (isSelected) selectedTextColor else unselectedTextColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
