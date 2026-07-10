package com.babytracker.designsystem.components.timepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * TDesign 风格滚轮选择器 — LazyColumn + 自动吸附。
 *
 * 初始化时渲染全部数字，滑动由 LazyColumn 原生滚动驱动，
 * 松手后自动吸附到最近项，无自定义手势处理，流畅不卡顿。
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

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex,
    )
    val centerPadding = itemHeight * halfVisible

    // 滑动停止后通知选中值
    LaunchedEffect(listState) {
        launch {
            snapshotFlow { listState.isScrollInProgress }
                .filter { !it }
                .map {
                    val layout = listState.layoutInfo
                    val viewportCenter = layout.viewportEndOffset / 2
                    layout.visibleItemsInfo.minByOrNull { info ->
                        val itemCenter = info.offset + info.size / 2
                        abs(itemCenter - viewportCenter)
                    }?.let { info ->
                        if (info.index in allValues.indices) allValues[info.index]
                        else null
                    }
                }
                .distinctUntilChanged()
                .drop(1) // 跳过初始值
                .collect { centered ->
                    if (centered != null && centered in range) {
                        onValueChanged(centered)
                    }
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight * visibleItems)
            .clipToBounds(),
    ) {
        // 选中行高亮背景
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

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                top = centerPadding,
                bottom = centerPadding,
            ),
        ) {
            items(allValues, key = { it }) { v ->
                val isSelected = v == value
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center,
                ) {
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
