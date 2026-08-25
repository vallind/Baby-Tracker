package com.babytracker.feature.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.divider.AppDivider
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing

// ═══════════════════════════════════════════════════════════
//  Settings 共享 UI 组件（自 SettingsScreen.kt 拆出，Batch 4）
//  设置行家族已收编进 designsystem：SettingsRow → AppSettingItem、
//  SettingsSectionTitle → AppSettingGroupTitle、SettingsMenuScaffold → SubPageScaffold、
//  ThemeDots → AppColorDots；AiSwitchRow/AiChoiceSetting → AppSettingSwitchItem/AppSettingChoiceItem。
//  仅剩 SettingsDivider 待下一批处理。
// ═══════════════════════════════════════════════════════════

@Composable
fun SettingsDivider() {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    AppDivider(
        color = c.divider,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = spacing.md),
    )
}
