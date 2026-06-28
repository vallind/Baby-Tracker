package com.babytracker.designsystem.components.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.LocalAppColors

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFormSheet(
    title: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveText: String = "保存",
    saveEnabled: Boolean = true,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalAppColors.current

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier
                .padding(horizontal = DT.pageMargin.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // 标题
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = c.textPrimary,
            )
            Spacer(Modifier.height(16.dp))

            // 业务表单字段
            content()

            // 保存按钮
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(DT.buttonRadius.dp),
                enabled = saveEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.primary,
                    contentColor = Color.White,
                ),
            ) {
                Text(saveText)
            }
        }
    }
}
