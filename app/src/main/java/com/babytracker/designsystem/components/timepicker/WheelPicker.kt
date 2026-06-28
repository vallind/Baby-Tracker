package com.babytracker.designsystem.components.timepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * TDesign 风格滚轮选择器 — 供 TimePickerDialog 和 DateTimeCascadeDialog 共享使用。
 *
 * 特征：
 * - 中间行高亮（品牌色浅色背景）
 * - 中间行文字品牌色加粗
 * - 非选中行透明度递减
 * - 支持垂直拖拽切换值
 * - 上/下分隔线标识选中行范围
 */
@Composable
fun WheelPicker(
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
    val totalItems = visibleItems + 2 // 上下各多显示一个做缓冲区
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    // 拖拽状态
    var dragOffset by remember { mutableFloatStateOf(0f) }
    // 显示偏移：用 value 计算当前项在中间的偏移
    val baseOffset = -value * itemHeightPx

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(itemHeight * totalItems)
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        // 选中行高亮背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(selectedBgColor, RoundedCornerShape(4.dp)),
        )
        // 上分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer { translationY = itemHeightPx * halfVisible },
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(dividerColor),
            )
        }
        // 下分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer { translationY = itemHeightPx * (halfVisible + 1) },
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(dividerColor),
            )
        }

        // 滚轮内容
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = baseOffset + dragOffset + itemHeightPx * halfVisible
                }
                .pointerInput(range) {
                    var accumulatedDrag = 0f
                    detectVerticalDragGestures(
                        onDragEnd = {
                            // 吸附到最近项
                            val snappedValue = (value - (accumulatedDrag / itemHeightPx))
                                .toInt()
                                .coerceIn(range.first, range.last)
                            onValueChanged(snappedValue)
                            dragOffset = 0f
                            accumulatedDrag = 0f
                        },
                        onDragCancel = {
                            dragOffset = 0f
                            accumulatedDrag = 0f
                        },
                        onVerticalDrag = { _, dragAmount ->
                            accumulatedDrag += dragAmount
                            dragOffset = accumulatedDrag
                        },
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 上缓冲
            repeat(halfVisible + 1) { idx ->
                val v = value - halfVisible - 1 + idx
                if (v in range) {
                    WheelItem(
                        text = "%02d".format(v),
                        height = itemHeight,
                        isSelected = false,
                        selectedTextColor = selectedTextColor,
                        unselectedTextColor = unselectedTextColor,
                        distanceFromCenter = halfVisible + 1 - idx,
                    )
                } else {
                    Spacer(modifier = Modifier.height(itemHeight))
                }
            }
            // 中间可见区
            repeat(visibleItems) { idx ->
                val v = value - halfVisible + idx
                val isCenter = idx == halfVisible
                if (v in range) {
                    WheelItem(
                        text = "%02d".format(v),
                        height = itemHeight,
                        isSelected = isCenter,
                        selectedTextColor = selectedTextColor,
                        unselectedTextColor = unselectedTextColor,
                        distanceFromCenter = kotlin.math.abs(idx - halfVisible),
                    )
                } else {
                    Spacer(modifier = Modifier.height(itemHeight))
                }
            }
            // 下缓冲
            repeat(halfVisible + 1) { idx ->
                val v = value + halfVisible + 1 + idx
                if (v in range) {
                    WheelItem(
                        text = "%02d".format(v),
                        height = itemHeight,
                        isSelected = false,
                        selectedTextColor = selectedTextColor,
                        unselectedTextColor = unselectedTextColor,
                        distanceFromCenter = halfVisible + 1 + idx,
                    )
                } else {
                    Spacer(modifier = Modifier.height(itemHeight))
                }
            }
        }
    }
}

/**
 * 滚轮单项。
 */
@Composable
private fun WheelItem(
    text: String,
    height: Dp,
    isSelected: Boolean,
    selectedTextColor: Color,
    unselectedTextColor: Color,
    distanceFromCenter: Int,
) {
    val alpha = when (distanceFromCenter) {
        0 -> 1f
        1 -> 0.6f
        2 -> 0.35f
        else -> 0.15f
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            ),
            color = if (isSelected) selectedTextColor else unselectedTextColor.copy(alpha = alpha),
            textAlign = TextAlign.Center,
        )
    }
}
