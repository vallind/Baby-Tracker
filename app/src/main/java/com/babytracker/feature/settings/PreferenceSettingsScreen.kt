package com.babytracker.feature.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.cardgroup.AppCardGroup
import com.babytracker.designsystem.components.chip.AppChip
import com.babytracker.designsystem.components.divider.AppDivider
import com.babytracker.designsystem.components.scaffold.SubPageScaffold
import com.babytracker.designsystem.components.settingitem.AppSettingItem
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppSpacing

/**
 * 使用偏好 — 纯 UI 渲染层（Batch 4 自 SettingsMenuScreen 拆出）。
 * 主题/密度状态与弹层宿主由 Route 注入（themePicker/densityPicker 槽位）；
 * 弹层显隐留 Screen。
 */
@Composable
fun PreferenceSettingsScreen(
    themeName: String,
    densityLabel: String,
    onBack: () -> Unit,
    onOpenAiSettings: () -> Unit,
    themePicker: @Composable (onDismiss: () -> Unit) -> Unit,
    densityPicker: @Composable (onDismiss: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    var showThemePicker by remember { mutableStateOf(false) }
    var showDensityPicker by remember { mutableStateOf(false) }

    SubPageScaffold(
        title = "使用偏好",
        onBack = onBack,
    ) {
        AppCardGroup {
            AppSettingItem(
                emoji = "🎨",
                label = "主题模式",
                subtitle = themeName,
                onClick = { showThemePicker = true },
            )
            AppDivider(horizontalInset = spacing.md)
            AppSettingItem(
                emoji = "📐",
                label = AppStrings.densityLabel,
                subtitle = densityLabel,
                onClick = { showDensityPicker = true },
            )
            AppDivider(horizontalInset = spacing.md)
            AppSettingItem(
                emoji = "✨",
                label = AppStrings.aiSettings,
                subtitle = "模型、宝宝数据与回答偏好",
                onClick = onOpenAiSettings,
            )
        }
        Spacer(Modifier.height(spacing.md))
    }

    if (showThemePicker) {
        themePicker { showThemePicker = false }
    }

    if (showDensityPicker) {
        densityPicker { showDensityPicker = false }
    }
}