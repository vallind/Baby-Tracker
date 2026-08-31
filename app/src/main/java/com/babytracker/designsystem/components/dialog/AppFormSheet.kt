package com.babytracker.designsystem.components.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.ui.i18n.AppStringsProduct
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.components.sheet.SheetDefaults as AppSheetDefaults
import com.babytracker.designsystem.theme.LocalAppColors

/**
 * 通用表单底部弹层 — 消除 5 个 XxxFormDialog 的 ModalBottomSheet 样板。
 *
 * 提供统一的容器、标题、保存按钮，中间 [content] 插槽由业务填充表单字段。
 * 调用方负责用 `if (show) { ... }` 控制可见性，不需要额外 `show` 参数。
 *
 * 用法：
 *   AppFormSheet(
 *       title = if (isEdit) AppStringsProduct.editFeeding else AppStringsProduct.recordFeeding,
 *       onDismiss = onDismiss,
 *       onSave = { onSave(buildEntity()) },
 *       saveText = if (isEdit) AppStringsProduct.updateLabel else AppStrings.save,
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
    saveText: String = AppStrings.save,
    saveEnabled: Boolean = true,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor: Color = AppSheetDefaults.containerColor(),
    contentColor: Color = AppSheetDefaults.contentColor(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier
                .padding(horizontal = spacing.md)
                .padding(bottom = spacing.xl)
                .verticalScroll(rememberScrollState()),
        ) {
            // 标题
            Text(
                title,
                style = LocalAppTypography.current.headlineSmall,
                color = c.textPrimary,
            )
            Spacer(Modifier.height(spacing.md))

            // 业务表单字段
            content()

            // 保存按钮（几何/配色全部由 AppButton 令牌派生）
            Spacer(Modifier.height(spacing.lg))
            AppButton(
                label = saveText,
                onClick = onSave,
                enabled = saveEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
