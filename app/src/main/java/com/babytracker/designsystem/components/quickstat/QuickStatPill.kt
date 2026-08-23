package com.babytracker.designsystem.components.quickstat

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.babytracker.designsystem.components.quickstat.QuickStatPillDefaults

/**
 * 统计胶囊 — 渐变卡/主色卡上的「大数值 + 单位 + 副标签」统计格。
 *
 * 内容色由调用方注入（如 onPrimary 白字版），数值/单位/标签透明度走令牌，
 * 今日概览卡与尿布/睡眠汇总卡共用同一形态（曾出现 2 处各自实现）。
 *
 * 用法：
 *   QuickStatPill(value = "8", label = "喂养次数", unit = "次", contentColor = c.onPrimary, modifier = Modifier.weight(1f))
 */
@Composable
fun QuickStatPill(
    value: String,
    label: String,
    unit: String? = null,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                fontSize = QuickStatPillDefaults.valueFontSize(),
                fontWeight = QuickStatPillDefaults.valueFontWeight(),
                color = contentColor.copy(alpha = QuickStatPillDefaults.valueAlpha()),
            )
            if (!unit.isNullOrBlank()) {
                Spacer(Modifier.width(2.dp))
                Text(
                    unit,
                    fontSize = QuickStatPillDefaults.unitFontSize(),
                    color = contentColor.copy(alpha = QuickStatPillDefaults.unitAlpha()),
                )
            }
        }
        Spacer(Modifier.height(QuickStatPillDefaults.innerSpacing()))
        Text(
            label,
            fontSize = QuickStatPillDefaults.labelFontSize(),
            color = contentColor.copy(alpha = QuickStatPillDefaults.labelAlpha()),
        )
    }
}
