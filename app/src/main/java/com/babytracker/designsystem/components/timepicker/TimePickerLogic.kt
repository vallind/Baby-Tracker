package com.babytracker.designsystem.components.timepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import com.babytracker.designsystem.theme.LocalAppTypography

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
    val allValues = remember(range) { range.toList() }
    val startValue = range.first
    val count = allValues.size
    val totalItems = halfVisible + count + halfVisible

    val listState = rememberLazyListState()
    var snapping by remember { mutableStateOf(false) }
    var lastSnappedValue by remember { mutableIntStateOf(-1) }

    // 初次定位
    LaunchedEffect(Unit) {
        listState.scrollToItem(maxOf(0, value - startValue))
    }

    // 滑动停止 → 吸附到最近的真实值（跳过 Spacer）
    LaunchedEffect(listState) {
        launch {
            snapshotFlow { listState.isScrollInProgress }
                .filter { !it && !snapping }
                .collect {
                    val layout = listState.layoutInfo
                    val viewportCenter = layout.viewportEndOffset / 2
                    val closest = layout.visibleItemsInfo
                        .filter { it.index in halfVisible until (halfVisible + count) }
                        .minByOrNull { info ->
                            val itemCenter = info.offset + info.size / 2
                            abs(itemCenter - viewportCenter)
                        }
                    if (closest != null) {
                        val centeredValue = startValue + closest.index - halfVisible
                        if (centeredValue == lastSnappedValue) return@collect
                        lastSnappedValue = centeredValue

                        onValueChanged(centeredValue)
                        snapping = true
                        val maxSnap = (totalItems - visibleItems).coerceAtLeast(0)
                        val snapIndex = (closest.index - halfVisible).coerceIn(0, maxSnap)
                        listState.animateScrollToItem(snapIndex)
                        snapping = false
                    }
                }
        }
    }

    // 最大缩放距离：halfVisible 项之外不再缩放
    val maxDistPx = remember(itemHeightPx, halfVisible) { itemHeightPx * halfVisible }

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

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Spacer padding — key 用负数避免与值 key 冲突
            items(halfVisible, key = { -it - 1 }) { Spacer(itemHeight) }
            items(allValues, key = { it }) { v ->
                val itemIndex = halfVisible + (v - startValue)
                val distance by remember {
                    derivedStateOf {
                        val layout = listState.layoutInfo
                        val viewportCenter = layout.viewportEndOffset / 2f
                        val item = layout.visibleItemsInfo.find { it.index == itemIndex }
                        if (item != null) {
                            val itemCenter = item.offset + item.size / 2f
                            abs(itemCenter - viewportCenter)
                        } else {
                            Float.MAX_VALUE
                        }
                    }
                }
                val t = (distance / maxDistPx).coerceIn(0f, 1f)
                val itemScale = 1.25f - 0.55f * t
                val itemAlpha = 1f - 0.4f * t

                val isSelected = v == value
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .semantics(mergeDescendants = true) { selected = isSelected },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "%02d".format(v),
                        style = LocalAppTypography.current.titleLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) selectedTextColor else unselectedTextColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .scale(itemScale)
                            .graphicsLayer { alpha = itemAlpha },
                    )
                }
            }
            // 底部 Spacer — key 用大正数避开值 range
            items(halfVisible, key = { it + 100 }) { Spacer(itemHeight) }
        }
    }
}

@Composable
private fun Spacer(height: Dp) {
    Box(modifier = Modifier.fillMaxWidth().height(height))
}
