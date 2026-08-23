package com.babytracker.designsystem.components.recorddetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.recorddetail.RecordDetailSheetDefaults
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 记录详情弹层 — 全站记录卡「单击=详情」契约的统一承载。
 *
 * 展示记录全部字段（只读）+ 主按钮「编辑」+ 次按钮「删除」；
 * 删除走内置确认弹层（与触屏左滑删除路径的确认行为一致，lessons #16）。
 *
 * 用法：
 *   RecordDetailSheet(
 *       show = showDetail,
 *       title = "母乳喂养",
 *       emoji = "🤱", tint = c.danger,
 *       fields = listOf("时间" to "08:15", "时长" to "15 分钟"),
 *       onEdit = { showForm = true; showDetail = false },
 *       onDelete = { repo.delete(f) },
 *       onDismiss = { showDetail = false },
 *   )
 */
@Composable
fun RecordDetailSheet(
    show: Boolean,
    title: String,
    fields: List<Pair<String, String>>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    emoji: String? = null,
    tint: Color? = null,
    deleteText: String = AppStrings.delete,
    editText: String = AppStrings.edit,
) {
    val typography = LocalAppTypography.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AppBottomSheet(show = show, onDismiss = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            if (emoji != null && tint != null) {
                AppEmojiBadge(emoji = emoji, tint = tint)
                Spacer(Modifier.height(12.dp))
            }
            Text(
                title,
                style = typography.titleLarge,
                fontWeight = RecordDetailSheetDefaults.titleWeight(),
                color = com.babytracker.designsystem.theme.LocalAppColors.current.textPrimary,
            )
            Spacer(Modifier.height(16.dp))
            fields.forEach { (label, value) ->
                Row(
                    Modifier.fillMaxWidth(),
                ) {
                    Text(
                        label,
                        style = typography.bodyMedium,
                        color = RecordDetailSheetDefaults.labelColor(),
                        modifier = Modifier.width(RecordDetailSheetDefaults.labelWidth()),
                    )
                    Text(
                        value,
                        style = typography.bodyMedium,
                        color = RecordDetailSheetDefaults.valueColor(),
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(RecordDetailSheetDefaults.rowSpacing()))
            }
            Spacer(Modifier.height(8.dp))
            AppButton(
                label = editText,
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            AppButton(
                variant = ButtonVariant.Secondary,
                label = deleteText,
                contentColor = RecordDetailSheetDefaults.deleteColor(),
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    AppConfirmDialog(
        show = showDeleteConfirm,
        title = AppStrings.confirmDelete,
        message = AppStrings.confirmDeleteMessage,
        onConfirm = {
            showDeleteConfirm = false
            onDismiss()
            onDelete()
        },
        onDismiss = { showDeleteConfirm = false },
    )
}
