package com.babytracker.designsystem.components.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.components.sheet.SheetDefaults as AppSheetDefaults
import com.babytracker.designsystem.theme.LocalAppSpacing

/**
 * 底部弹层 —— 超级参照组件：Gesture / Physics / Motion。
 *
 * Gesture 手势轴：下拉关闭由 M3 嵌套滚动接管，档位数量经 skipPartiallyExpanded 控制；
 *   注：不把实验型的 SheetState 暴露进公开签名（会向全部调用方传染 @OptIn），
 *   程序化展开/确认拦截等高级策略待真实需求出现时以独立重载提供；
 * Physics 物理轴：展开/收起阻尼曲线由 M3 spring 提供，本组件不覆盖物理参数；
 * Motion 动效轴：进出场的位移/淡入时长走 M3 内置动效，遮罩与把手颜色全部令牌化。
 *
 * 用法：
 *   AppBottomSheet(show = showForm, onDismiss = { showForm = false }) {
 *       // 表单内容
 *   }
 *   AppBottomSheet(show = true, onDismiss = { ... }, showDragHandle = false) { ... }
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = true,
    showDragHandle: Boolean = true,
    containerColor: Color = AppSheetDefaults.containerColor(),
    contentColor: Color = AppSheetDefaults.contentColor(),
    scrimColor: Color = AppSheetDefaults.scrimColor(),
    cornerRadius: Dp = AppSheetDefaults.cornerRadius(),
    tonalElevation: Dp = AppSheetDefaults.elevation(),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!show) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerColor,
        contentColor = contentColor,
        scrimColor = scrimColor,
        tonalElevation = tonalElevation,
        shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
        dragHandle = if (showDragHandle) {
            { AppSheetDragHandle() }
        } else {
            null
        },
        modifier = modifier,
        content = content,
    )
}

/** 令牌化拖拽把手：几何/颜色读 SheetTokens，替代 M3 默认灰色把手 */
@Composable
private fun AppSheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = LocalAppSpacing.current.sm)
            .width(AppSheetDefaults.dragHandleWidth())
            .height(AppSheetDefaults.dragHandleHeight())
            .clip(RoundedCornerShape(percent = 50))
            .background(AppSheetDefaults.dragHandleColor()),
        contentAlignment = Alignment.Center,
    ) {}
}
