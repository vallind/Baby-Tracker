package com.babytracker.designsystem.components.settingitem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 设置行默认值 — 从组件令牌（AppComponentTokens.settingItem）读取。
 *
 * 徽章尺寸/圆角/字号、标题与副标题字号颜色、尾部图标规格均由令牌下发，
 * 本文件禁止硬编码颜色或直读 LocalAppColors。
 */
object SettingItemDefaults {
    @Composable fun badgeSize(): Dp = LocalAppComponentTokens.current.settingItem.badgeSize
    @Composable fun badgeCornerRadius(): Dp = LocalAppComponentTokens.current.settingItem.badgeCornerRadius
    @Composable fun badgeFontSize(): TextUnit = LocalAppComponentTokens.current.settingItem.badgeFontSize
    @Composable fun badgeContainerColor(): Color = LocalAppComponentTokens.current.settingItem.badgeContainerColor
    @Composable fun titleFontSize(): TextUnit = LocalAppComponentTokens.current.settingItem.titleFontSize
    @Composable fun titleColor(): Color = LocalAppComponentTokens.current.settingItem.titleColor
    @Composable fun subtitleFontSize(): TextUnit = LocalAppComponentTokens.current.settingItem.subtitleFontSize
    @Composable fun subtitleColor(): Color = LocalAppComponentTokens.current.settingItem.subtitleColor
    @Composable fun trailingIconSize(): Dp = LocalAppComponentTokens.current.settingItem.trailingIconSize
    @Composable fun chevronTint(): Color = LocalAppComponentTokens.current.settingItem.chevronTint
    @Composable fun choiceLabelFontSize(): TextUnit = LocalAppComponentTokens.current.settingItem.choiceLabelFontSize
    @Composable fun choiceSubtitleFontSize(): TextUnit = LocalAppComponentTokens.current.settingItem.choiceSubtitleFontSize
    @Composable fun choiceSubtitleColor(): Color = LocalAppComponentTokens.current.settingItem.choiceSubtitleColor
    @Composable fun groupTitleFontSize(): TextUnit = LocalAppComponentTokens.current.settingItem.groupTitleFontSize
    @Composable fun groupTitleColor(): Color = LocalAppComponentTokens.current.settingItem.groupTitleColor
}
