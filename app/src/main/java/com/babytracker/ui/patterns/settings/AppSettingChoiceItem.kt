package com.babytracker.ui.patterns.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.components.chip.AppChip
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.ui.i18n.AppStringsProduct
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 胶囊单选设置行 — 标题/副标题 + 可横向滚动的 AppChip 单选组。
 *
 * 自 feature/ai 的 AiChoiceSetting 收编，视觉与语义保持一致：
 * options 为 (id, 显示文案) 列表，selectedId 匹配的胶囊高亮；
 * interactive = false 时胶囊仅展示不响应（如"当前模型能力"只读行）。
 * options 为空时展示配置不可用占位（沿用迁移前文案，保持原行为）。
 *
 * 用法：
 *   AppSettingChoiceItem(
 *       emoji = "💭", label = "思考模式",
 *       options = listOf("auto" to "自动", "on" to "开启"),
 *       selectedId = "auto", onSelect = { ... },
 *   )
 */
@Composable
fun AppSettingChoiceItem(
    emoji: String,
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    interactive: Boolean = true,
) {
    val spacing = LocalAppSpacing.current
    Column(modifier.fillMaxWidth().padding(horizontal = spacing.md, vertical = spacing.sm)) {
        Text(
            text = "$emoji  $label",
            style = LocalAppTypography.current.bodyLarge.copy(fontSize = SettingItemDefaults.choiceLabelFontSize()),
            color = SettingItemDefaults.titleColor(),
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = LocalAppTypography.current.bodyMedium.copy(
                    fontSize = SettingItemDefaults.choiceSubtitleFontSize(),
                ),
                color = SettingItemDefaults.choiceSubtitleColor(),
                modifier = Modifier.padding(top = spacing.xs),
            )
        }
        Spacer(Modifier.height(spacing.sm))
        if (options.isEmpty()) {
            Text(
                text = AppStringsProduct.aiConfigUnavailable,
                style = LocalAppTypography.current.bodyMedium.copy(
                    fontSize = SettingItemDefaults.choiceSubtitleFontSize(),
                ),
                color = SettingItemDefaults.choiceSubtitleColor(),
            )
        } else {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                options.forEach { (id, optionLabel) ->
                    val selected = id == selectedId
                    AppChip(
                        label = optionLabel,
                        onClick = { onSelect(id) },
                        enabled = interactive,
                        selected = selected,
                    )
                }
            }
        }
    }
}