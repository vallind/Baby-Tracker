package com.babytracker.designsystem.components.keyvaluerow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

/**
 * 键值行 — 信息展示行：左侧 label / 右侧 value，值下可选 caption 副行（全站统一规格）。
 *
 * 来源：自 feature/settings/BabyProfileScreen.kt 的私有 InfoRow / GrowthValueRow 收编，
 * 两种形态均按源码保真：
 *  - caption == null（源 InfoRow 形态）：固定 [KeyValueRowDefaults.rowHeight] 定高，
 *    value 用次要色（valueColor）；
 *  - caption != null（源 GrowthValueRow 形态）：以 rowHeight 为最小行高、随副行撑高，
 *    value 用强调色（valueEmphasizedColor），caption 走小号三级色（如测量日期）。
 *
 * 用法：
 *   AppKeyValueRow("出生日期", "2024-01-01")
 *   AppKeyValueRow("当前身高", "72cm", caption = "2025-06-01 测量")
 */
@Composable
fun AppKeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (caption == null) {
                    Modifier.height(KeyValueRowDefaults.rowHeight())
                } else {
                    Modifier.heightIn(min = KeyValueRowDefaults.rowHeight())
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = KeyValueRowDefaults.labelTextStyle(),
            color = KeyValueRowDefaults.labelColor(),
        )
        if (caption == null) {
            Text(
                value,
                style = KeyValueRowDefaults.valueTextStyle(),
                color = KeyValueRowDefaults.valueColor(),
                textAlign = TextAlign.End,
            )
        } else {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    value,
                    style = KeyValueRowDefaults.valueTextStyle(),
                    color = KeyValueRowDefaults.valueEmphasizedColor(),
                )
                Text(
                    caption,
                    style = KeyValueRowDefaults.captionTextStyle(),
                    color = KeyValueRowDefaults.captionColor(),
                )
            }
        }
    }
}
