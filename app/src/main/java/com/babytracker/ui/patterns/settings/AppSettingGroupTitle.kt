package com.babytracker.ui.patterns.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 设置分组标题 — 设置页卡片组的 section 标题（labelMedium 次级色）。
 *
 * 统一 feature/settings 的 SettingsSectionTitle 与 feature/ai 的 AiSettingsSectionTitle：
 * 两者仅差顶部留白（AI 版 top=lg），用 showTopSpacing 参数表达——
 * 默认 false 即 SettingsSectionTitle 形态，true 时顶部追加 spacing.lg。
 *
 * 用法：
 *   AppSettingGroupTitle("宝宝与家庭")                          // 紧跟间距 Spacer 后
 *   AppSettingGroupTitle("模型与生成", showTopSpacing = true)   // 组间需要大间隔
 */
@Composable
fun AppSettingGroupTitle(
    title: String,
    modifier: Modifier = Modifier,
    showTopSpacing: Boolean = false,
) {
    val spacing = LocalAppSpacing.current
    Text(
        title,
        style = LocalAppTypography.current.labelMedium.copy(fontSize = SettingItemDefaults.groupTitleFontSize()),
        color = SettingItemDefaults.groupTitleColor(),
        modifier = modifier.padding(
            top = if (showTopSpacing) spacing.lg else spacing.none,
            bottom = spacing.sm,
        ),
    )
}