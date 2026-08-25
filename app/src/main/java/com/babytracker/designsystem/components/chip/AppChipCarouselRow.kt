package com.babytracker.designsystem.components.chip

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.theme.LocalAppSpacing

/** 横向滚动单选 chip 行的选项规格：[key] 为稳定标识（选中轴/回调值），[label] 为展示文案 */
data class AppChipSpec(
    val key: String,
    val label: String,
)

/**
 * 横向滚动单选 chip 行 — 选项宽度自适应 + 溢出横滚的「轮播」选择条（G4 聊天族收编）。
 *
 * 与 [AppOptionChipRow] 的分工：那是等宽（weight(1f)）横排、不滚动，适合固定少量表单档位；
 * 本组件不自设宽度、整行 horizontalScroll 溢出可滚，适合数量不定、文案长度不齐的选择集
 * （如模型列表）。选中高亮复用 [AppChip] 的 selected 轴。
 *
 * 行内自带 chip 间距（spacing.sm），不带页面外边距；行内不自带选中语义朗读之外的额外状态。
 *
 * 用法：
 *   AppChipCarouselRow(
 *       options = models.map { AppChipSpec(key = it.id, label = it.name) },
 *       selectedKey = selectedId,
 *       onSelect = { selectedId = it },
 *   )
 */
@Composable
fun AppChipCarouselRow(
    options: List<AppChipSpec>,
    selectedKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        options.forEach { option ->
            AppChip(
                label = option.label,
                onClick = { onSelect(option.key) },
                selected = option.key == selectedKey,
            )
        }
    }
}
