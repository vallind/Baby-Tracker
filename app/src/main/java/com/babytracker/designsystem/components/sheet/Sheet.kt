package com.babytracker.designsystem.components.sheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable

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
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!show) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        content = content,
    )
}
