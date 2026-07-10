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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 滚轮选择器 — LazyColumn + 实时缩放 + 自动吸附。
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
    val allValues = remember(range) { range.toList() }
    val initialIndex = (value - range.first).coerceIn(0, allValues.lastIndex)

    val listState = rememberLazyListState()
    var snapping by remember { mutableStateOf(false) }

    // 初次定位
    LaunchedEffect(Unit) {
        listState.scrollToItem(maxOf(0, initialIndex - halfVisible))
    }

    // 滑动停止 → 吸附居中
    LaunchedEffect(listState) {
        launch {
            snapshotFlow { listState.isScrollInProgress }
                .filter { !it && !snapping }
                .collect {
                    val layout = listState.layoutInfo
                    val viewportCenter = layout.viewportEndOffset / 2
                    val closest = layout.visibleItemsInfo.minByOrNull { info ->
                        val itemCenter = info.offset + info.size / 2
                        abs(itemCenter - viewportCenter)
                    }
                    if (closest != null) {
                        val centeredValue = range.first + closest.index
                        onValueChanged(centeredValue)
                        snapping = true
                        val snapIndex = maxOf(0, closest.index - halfVisible)
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
            items(allValues, key = { it }) { v ->
                val itemIndex = v - range.first
                // 实时计算该项距视口中心的距离 → 缩放比
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
                        .height(itemHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "%02d".format(v),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) selectedTextColor else unselectedTextColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .scale(itemScale)
                            .graphicsLayer { alpha = itemAlpha },
                    )
                }
            }
        }
    }
}
