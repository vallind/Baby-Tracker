package com.babytracker.designsystem.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors

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
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = LocalAppColors.current.danger)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelText)
            }
        },
    )
}
