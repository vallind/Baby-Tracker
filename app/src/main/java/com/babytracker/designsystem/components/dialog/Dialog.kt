package com.babytracker.designsystem.components.dialog

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.i18n.AppStrings
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import io.elyon.kmp.basic.Text
import io.elyon.kmp.basic.TextButton
import io.elyon.kmp.overlay.OverlayDialog
import io.elyon.kmp.theme.ElyonTheme

/**
 * 确认删除对话框 — 消除 6+ 处重复的 AlertDialog 样板
 *
 * 对标 Palette Dialog 组件，统一标题/内容/确认/取消文案。
 *
 * 用法：
 *   AppConfirmDialog(
 *       show = deletingItem != null,
 *       onConfirm = { repo.delete(id); deletingItem = null },
 *       onDismiss = { deletingItem = null },
 *   )
 */
@Composable
fun AppConfirmDialog(
    show: Boolean,
    title: String = AppStrings.confirmDelete,
    message: String = AppStrings.confirmDeleteMessage,
    confirmText: String = AppStrings.delete,
    cancelText: String = AppStrings.cancel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    containerColor: Color = ElyonTheme.colorScheme.surfaceContainer,
    contentColor: Color = ElyonTheme.colorScheme.onSurfaceContainer,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    OverlayDialog(
        show = show,
        modifier = modifier,
        title = title,
        summary = message,
        backgroundColor = containerColor,
        cornerRadius = cornerRadius,
        onDismissRequest = onDismiss,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                text = confirmText,
                onClick = onConfirm,
                colors = io.elyon.kmp.basic.ButtonDefaults.textButtonColors(
                    textColor = ElyonTheme.colorScheme.error,
                ),
            )
            TextButton(
                text = cancelText,
                onClick = onDismiss,
            )
        }
    }
}
