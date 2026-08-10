package com.babytracker.core.ui.components.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.Button
import io.elyon.kmp.basic.ButtonDefaults
import io.elyon.kmp.basic.Text
import io.elyon.kmp.overlay.OverlayBottomSheet
import io.elyon.kmp.theme.ElyonTheme

/**
 * 通用表单底部弹层 — 消除 5 个 XxxFormDialog 的 ModalBottomSheet 样板。
 *
 * 提供统一的容器、标题、保存按钮，中间 [content] 插槽由业务填充表单字段。
 * 调用方负责用 `if (show) { ... }` 控制可见性，不需要额外 `show` 参数。
 *
 * 用法：
 *   AppFormSheet(
 *       title = if (isEdit) "编辑喂养" else "记录喂养",
 *       onDismiss = onDismiss,
 *       onSave = { onSave(buildEntity()) },
 *       saveText = if (isEdit) "更新" else "保存",
 *   ) {
 *       // 业务表单字段...
 *   }
 */
@Composable
fun AppFormSheet(
    title: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveText: String = "保存",
    saveEnabled: Boolean = true,
    containerColor: Color = ElyonTheme.colorScheme.surfaceContainer,
    contentColor: Color = ElyonTheme.colorScheme.onSurfaceContainer,
    content: @Composable () -> Unit,
) {
    OverlayBottomSheet(
        show = true,
        title = title,
        onDismissRequest = onDismiss,
        backgroundColor = containerColor,
        modifier = modifier,
    ) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // 业务表单字段
            content()

            // 保存按钮
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = saveEnabled,
                colors = ButtonDefaults.buttonColors(
                    color = ElyonTheme.colorScheme.primary,
                    contentColor = ElyonTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(saveText)
            }
        }
    }
}
