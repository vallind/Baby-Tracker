package com.babytracker.designsystem.components.sheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.components.sheet.SheetDefaults as AppSheetDefaults

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    skipPartiallyExpanded: Boolean = true,
    containerColor: Color = AppSheetDefaults.containerColor(),
    contentColor: Color = AppSheetDefaults.contentColor(),
    cornerRadius: Dp = AppSheetDefaults.cornerRadius(),
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!show) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
        modifier = modifier,
        content = content,
    )
}
