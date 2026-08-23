package com.babytracker.designsystem.components.statcell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

/**
 * 统计格 — 数值 + 单位 + 标签（可选 emoji）的纵向格子，全站统一规格。
 *
 * 收敛此前三处各写一份的统计格（首页概览/尿布汇总/睡眠详情）：
 * 数值统一强调色 headlineSmall，标签统一 labelMedium 次级色。
 *
 * 用法：
 *   Row { StatCell("5", "次", "母乳", Modifier.weight(1f)); StatCell("320", "ml", "配方奶", ...) }
 */
@Composable
fun StatCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    emoji: String? = null,
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (emoji != null) {
            Text(emoji, fontSize = StatCellDefaults.emojiFontSize())
            Spacer(Modifier.height(StatCellDefaults.innerSpacing()))
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                fontSize = StatCellDefaults.valueFontSize(),
                fontWeight = StatCellDefaults.valueFontWeight(),
                color = StatCellDefaults.valueColor(),
                textAlign = TextAlign.Center,
            )
            if (!unit.isNullOrBlank()) {
                Spacer(Modifier.width(StatCellDefaults.innerSpacing() / 2))
                Text(unit, fontSize = StatCellDefaults.unitFontSize(), color = StatCellDefaults.unitColor())
            }
        }
        Spacer(Modifier.height(StatCellDefaults.innerSpacing()))
        Text(label, fontSize = StatCellDefaults.labelFontSize(), color = StatCellDefaults.labelColor())
    }
}
