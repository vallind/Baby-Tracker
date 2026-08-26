package com.babytracker.designsystem.components.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.i18n.AppStrings

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
 *
 * 需要自定义表单内容时传 content 插槽（与 text 互斥）：
 *   AppDialog(
 *       show = showDialog,
 *       title = "编辑",
 *       content = { AppInput(...) },
 *       confirmEnabled = input.isNotBlank(),
 *       onConfirm = { save() },
 *       onDismiss = { showDialog = false },
 *   )
 */
@Composable
fun AppDialog(
    show: Boolean,
    title: String,
    text: String? = null,
    content: (@Composable () -> Unit)? = null,
    confirmText: String = AppStrings.confirm,
    cancelText: String? = AppStrings.cancel,
    confirmEnabled: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    cornerRadius: Dp = DialogDefaults.cornerRadius(),
    containerColor: Color = DialogDefaults.containerColor(),
    contentColor: Color = DialogDefaults.contentColor(),
    elevation: Dp = DialogDefaults.elevation(),
    modifier: Modifier = Modifier,
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
        text = content ?: text?.let { { Text(it) } },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = confirmEnabled) {
                Text(confirmText)
            }
        },
        // 取消按钮可空：传 null 即单按钮确认态（Overlay/Layer 参照能力）
        dismissButton = if (cancelText != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text(cancelText)
                }
            }
        } else null,
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
    cancelText: String? = AppStrings.cancel,
    onDismiss: () -> Unit,
    cornerRadius: Dp = DialogDefaults.cornerRadius(),
    containerColor: Color = DialogDefaults.containerColor(),
    contentColor: Color = DialogDefaults.contentColor(),
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
                    TextButton(
                        onClick = {
                            onDismiss()
                            onClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(label, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        dismissButton = if (cancelText != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text(cancelText)
                }
            }
        } else null,
    )
}
