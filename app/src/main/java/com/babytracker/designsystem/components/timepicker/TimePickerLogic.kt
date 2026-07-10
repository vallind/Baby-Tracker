package com.babytracker.designsystem.components.timepicker

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
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

    var currentValue by remember { mutableIntStateOf(value) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val snapAnim = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(value) {
        currentValue = value
    }

    // 渲染 visibleItems * 3 的数字，确保拖拽时仍覆盖可见范围
    val visibleValues = remember(currentValue, dragOffset, range) {
        val extra = visibleItems // 额外缓冲区
        val shift = -(dragOffset / itemHeightPx).roundToInt()
        val half = visibleItems / 2 + extra
        val start = currentValue - half + shift
        val end = currentValue + half + shift
        (start..end).map { v ->
            if (v in range) v else null
        }
    }

    val visualOffset = dragOffset + snapAnim.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight * visibleItems)
            .clipToBounds(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .offset { IntOffset(0, (itemHeightPx * halfVisible).roundToInt()) }
                .background(selectedBgColor, RoundedCornerShape(4.dp)),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .offset { IntOffset(0, (itemHeightPx * halfVisible).roundToInt()) }
                .background(dividerColor),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .offset { IntOffset(0, (itemHeightPx * (halfVisible + 1)).roundToInt()) }
                .background(dividerColor),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, visualOffset.roundToInt()) }
                .pointerInput(range) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            val dragItems = (dragOffset / itemHeightPx).roundToInt()
                            val newValue = (currentValue - dragItems)
                                .coerceIn(range.first, range.last)
                            currentValue = newValue
                            onValueChanged(newValue)
                            val remaining = dragOffset
                            dragOffset = 0f
                            scope.launch {
                                snapAnim.snapTo(remaining)
                                snapAnim.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(dampingRatio = 0.6f),
                                )
                            }
                        },
                        onDragCancel = {
                            val remaining = dragOffset
                            dragOffset = 0f
                            scope.launch {
                                snapAnim.snapTo(remaining)
                                snapAnim.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(dampingRatio = 0.6f),
                                )
                            }
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
