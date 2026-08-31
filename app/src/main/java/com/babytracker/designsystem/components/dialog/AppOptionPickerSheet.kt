package com.babytracker.designsystem.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.components.section.AppListItem
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography



/**
 * 单选选项底部弹层 — 表单族通用组件（G4 收敛）。
 *
 * 对照 TimelineScreen 的"选择记录类型"弹层抽平：底部弹层 + 标题 + 单选列表 +
 * 选中态高亮。容器复用 [AppBottomSheet]（SheetDefaults 令牌），列表行复用
 * [AppListItem]（selected 高亮即选中态）。
 *
 * 点击选项后先回调 [onSelect] 再回调 [onDismiss]，与原调用点"关闭弹层→执行动作"
 * 的顺序一致；调用方无需在 onSelect 里重复关弹层。
 *
 * 用法：
 *   AppOptionPickerSheet(
 *       title = AppStrings.pleaseSelect,
 *       options = listOf("a" to AppStrings.save, ...),
 *       selectedKey = null,
 *       onSelect = { key -> when (key) { "feeding" -> showAddFeeding = true; ... } },
 *       onDismiss = { showTypePicker = false },
 *   )
 */
@Composable
fun AppOptionPickerSheet(
    title: String,
    options: List<Pair<String, String>>, // key to label
    selectedKey: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current

    AppBottomSheet(show = true, onDismiss = onDismiss) {
        Column(modifier = modifier.padding(horizontal = spacing.md, vertical = spacing.sm)) {
            Text(
                title,
                style = typography.headlineMedium,
                modifier = Modifier.padding(bottom = spacing.md),
            )
            options.forEach { (key, label) ->
                AppListItem(
                    headlineContent = {
                        Text(label, style = typography.bodyLarge)
                    },
                    selected = selectedKey == key,
                    onClick = {
                        onSelect(key)
                        onDismiss()
                    },
                )
            }
            Spacer(Modifier.height(spacing.lg))
        }
    }
}
