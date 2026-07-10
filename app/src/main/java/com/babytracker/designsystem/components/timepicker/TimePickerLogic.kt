package com.babytracker.designsystem.components.timepicker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
 * 滚轮选择器 — LazyColumn + 自动吸附 + 弹性动画。
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

    // 选中文字放大动画
    val selectedScale by animateFloatAsState(
        targetValue = 1.15f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "selectedScale",
    )

    // 初次定位
    LaunchedEffect(Unit) {
        listState.scrollToItem(maxOf(0, initialIndex - halfVisible))
    }

    // 滑动停止 → 弹簧吸附居中
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) selectedTextColor else unselectedTextColor,
                        textAlign = TextAlign.Center,
                        modifier = if (isSelected) Modifier.scale(selectedScale) else Modifier,
                    )
                }
            }
        }
    }
}
