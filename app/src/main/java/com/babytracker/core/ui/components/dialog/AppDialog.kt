package com.babytracker.core.ui.components.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.Text
import io.elyon.kmp.basic.TextButton
import io.elyon.kmp.overlay.OverlayDialog
import io.elyon.kmp.overlay.OverlayBottomSheet
import io.elyon.kmp.theme.ElyonTheme

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
    confirmText: String = "确认",
    cancelText: String = "取消",
    confirmEnabled: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    cornerRadius: Dp = 24.dp,
    containerColor: Color = ElyonTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
    contentColor: Color = ElyonTheme.colorScheme.onSurfaceContainer,
    elevation: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    OverlayDialog(
        show = show,
        modifier = modifier,
        title = title,
        backgroundColor = containerColor,
        cornerRadius = cornerRadius,
        onDismissRequest = onDismiss,
    ) {
        if (content != null) {
            content()
        } else if (text != null) {
            Text(text, color = contentColor)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                text = confirmText,
                onClick = onConfirm,
                enabled = confirmEnabled,
            )
            TextButton(
                text = cancelText,
                onClick = onDismiss,
            )
        }
    }
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
    cornerRadius: Dp = 24.dp,
    containerColor: Color = ElyonTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
    contentColor: Color = ElyonTheme.colorScheme.onSurfaceContainer,
    modifier: Modifier = Modifier,
) {
    OverlayBottomSheet(
        show = show,
        title = title,
        backgroundColor = containerColor,
        cornerRadius = cornerRadius,
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column {
            actions.forEach { (label, onClick) ->
                TextButton(
                    text = label,
                    onClick = {
                        onDismiss()
                        onClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            TextButton(
                text = cancelText,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
