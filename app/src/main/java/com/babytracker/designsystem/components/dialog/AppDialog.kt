package com.babytracker.designsystem.components.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.button.AppTextButton
import com.babytracker.designsystem.components.dialog.DialogDefaults as AppDialogDefaults

/**
 * 通用对话框组件 — 对标 Palette Dialog，消费 AppComponentTokens.dialog。
 *
 * 用法：
 *   AppDialog(
 *       show = showDialog,
 *       title = "确认操作",
 *       text = "确定要删除这条记录吗？",
 *       confirmText = "删除",
 *       onConfirm = { deleteItem() },
 *       onDismiss = { showDialog = false },
 *   )
 */
@Composable
fun AppDialog(
    show: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    confirmText: String = "确认",
    onConfirm: (() -> Unit)? = null,
    dismissText: String = "取消",
    onDismiss: () -> Unit,
    content: @Composable (() -> Unit)? = null,
    message: String? = null,
    cornerRadius: Dp = AppDialogDefaults.cornerRadius(),
    containerColor: Color = AppDialogDefaults.containerColor(),
    contentColor: Color = AppDialogDefaults.contentColor(),
    elevation: Dp = AppDialogDefaults.elevation(),
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        containerColor = containerColor,
        textContentColor = contentColor,
        tonalElevation = elevation,
        title = { Text(title) },
        text = content ?: (message?.let { { Text(it) } }),
        confirmButton = {
            if (onConfirm != null) AppTextButton(onClick = onConfirm, label = confirmText)
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss, label = dismissText)
        },
    )
}

/**
 * 通用底部弹层对话框（ActionSheet 风格），用于多选项操作。
 *
 * 用法：
 *   AppActionSheet(
 *       show = showSheet,
 *       title = "选择操作",
 *       actions = listOf("编辑" to { editItem() }, "删除" to { deleteItem() }),
 *       onDismiss = { showSheet = false },
 *   )
 */
@Composable
fun AppActionSheet(
    show: Boolean,
    title: String? = null,
    actions: List<Pair<String, () -> Unit>>,
    cancelText: String = "取消",
    onDismiss: () -> Unit,
    cornerRadius: Dp = AppDialogDefaults.cornerRadius(),
    containerColor: Color = AppDialogDefaults.containerColor(),
    contentColor: Color = AppDialogDefaults.contentColor(),
    modifier: Modifier = Modifier,
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
        ),
        containerColor = containerColor,
        textContentColor = contentColor,
        confirmButton = {},
        title = title?.let { { Text(it) } },
        text = {
            Column {
                actions.forEach { (label, onClick) ->
                    AppTextButton(
                        onClick = {
                            onDismiss()
                            onClick()
                        },
                        label = label,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss, label = cancelText)
        },
    )
}
