package com.babytracker.designsystem.components.chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.ui.i18n.AppStringsProduct

/**
 * 表单类型选择 chip 行 — 表单族通用组件（G4 收敛）。
 *
 * 六个记录表单（喂养/睡眠/尿布/生长/健康/疫苗）同构的"key→label 选项胶囊行"：
 * 等宽横排、单选高亮。内部复用 [AppFilterChip] 交互胶囊形态，
 * M3 FilterChip 自带 Role/selected 选中语义（与 SegmentedControl 的读屏约定同风格）。
 *
 * 行内不自带外边距：与表单字段的上下间距由调用方用 modifier/Spacer 控制。
 *
 * 用法：
 *   AppOptionChipRow(
 *       options = listOf("wet" to AppStringsProduct.diaperOptionWet, ...),
 *       selectedKey = selectedType,
 *       onSelect = { selectedType = it },
 *       modifier = Modifier.padding(bottom = spacing.md),
 *   )
 */
@Composable
fun AppOptionChipRow(
    options: List<Pair<String, String>>, // key to label
    selectedKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        options.forEach { (key, label) ->
            AppFilterChip(
                selected = selectedKey == key,
                onClick = { onSelect(key) },
                label = label,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
