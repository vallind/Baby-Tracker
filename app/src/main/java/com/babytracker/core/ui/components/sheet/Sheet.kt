package com.babytracker.core.ui.components.sheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.elyon.kmp.overlay.OverlayBottomSheet
import io.elyon.kmp.theme.ElyonTheme

/**
 * 表单底部弹层 — 对标 Palette Dialog/ActionSheet 组件体系
 *
 * 消除 6+ 处重复的 rememberModalBottomSheetState + ModalBottomSheet 样板。
 *
 * 用法：
 *   AppBottomSheet(show = showForm, onDismiss = { showForm = false }) {
 *       // 表单内容
 *   }
 */
@Composable
fun AppBottomSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    skipPartiallyExpanded: Boolean = true,
    containerColor: Color = ElyonTheme.colorScheme.surfaceContainer,
    contentColor: Color = ElyonTheme.colorScheme.onSurfaceContainer,
    cornerRadius: Dp = 28.dp,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    content: @Composable () -> Unit,
) {
    OverlayBottomSheet(
        show = show,
        onDismissRequest = onDismiss,
        backgroundColor = containerColor,
        cornerRadius = cornerRadius,
        modifier = modifier,
        content = content,
    )
}
