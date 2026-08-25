package com.babytracker.designsystem.components.inlinebanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 内联横幅默认视觉 — 全部经 [LocalAppComponentTokens] 的 inlineBanner 令牌组取值。
 * 禁止直读核心颜色令牌或硬编码色值（审计规则 DefaultsHardcodedColor / DefaultsImportsLocalAppColors）。
 */
object InlineBannerDefaults {

    /** severity 对应的浅底容器色（语义色阶 tintContainer 档） */
    @Composable
    fun containerColor(severity: AppBannerSeverity): Color {
        val tokens = LocalAppComponentTokens.current.inlineBanner
        return when (severity) {
            AppBannerSeverity.Info -> tokens.info.containerColor
            AppBannerSeverity.Warning -> tokens.warning.containerColor
            AppBannerSeverity.Error -> tokens.error.containerColor
        }
    }

    /** severity 对应的强调前景色（语义色阶 accentContent 档） */
    @Composable
    fun contentColor(severity: AppBannerSeverity): Color {
        val tokens = LocalAppComponentTokens.current.inlineBanner
        return when (severity) {
            AppBannerSeverity.Info -> tokens.info.contentColor
            AppBannerSeverity.Warning -> tokens.warning.contentColor
            AppBannerSeverity.Error -> tokens.error.contentColor
        }
    }

    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.inlineBanner.cornerRadius

    @Composable fun horizontalPadding(): Dp = LocalAppComponentTokens.current.inlineBanner.horizontalPadding

    @Composable fun verticalPadding(): Dp = LocalAppComponentTokens.current.inlineBanner.verticalPadding

    /** 动作内容色（尾部 Ghost 文字动作前景；与 ButtonTokens.textContentColor 同源派生） */
    @Composable fun actionContentColor(): Color = LocalAppComponentTokens.current.inlineBanner.actionContentColor
}
