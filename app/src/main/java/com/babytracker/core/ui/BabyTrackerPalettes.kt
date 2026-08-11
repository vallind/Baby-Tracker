package com.babytracker.core.ui

import androidx.compose.ui.graphics.Color
import io.elyon.kmp.theme.Colors
import io.elyon.kmp.theme.darkColorScheme
import io.elyon.kmp.theme.lightColorScheme

/**
 * 旧 6 套主题的固定 Elyon 色板。
 *
 * 数值沿用旧 designsystem ThemeColors.derive 的派生规则：各主题只保留
 * primary/background/card/accent 差异项，其余按主色混合派生。从 Monet 自动
 * 色板切回固定色板，恢复原来的主题观感。
 */
object BabyTrackerPalettes {
    val pure: Colors = light(
        primary = Color(0xFF4285F4),
        background = Color(0xFFF0F4FE),
    )

    val aurora: Colors = light(
        primary = Color(0xFF7C6CF0),
        background = Color(0xFFF8F5FF),
        accent = Color(0xFFFCD34D),
    )

    val warm: Colors = light(
        primary = Color(0xFFFF8A80),
        background = Color(0xFFFFFBF7),
    )

    val sunny: Colors = light(
        primary = Color(0xFFF5A623),
        background = Color(0xFFFFFAF0),
        accent = Color(0xFFE67A2E),
    )

    val night: Colors = dark(
        primary = Color(0xFF5C6BC0),
        background = Color(0xFF12121F),
        card = Color(0xFF1E1E32),
    )

    val morandi: Colors = light(
        primary = Color(0xFFB0BEC5),
        background = Color(0xFFFAFAFA),
        accent = Color(0xFFD0A878),
    )
}

private fun light(
    primary: Color,
    background: Color,
    accent: Color = Color(0xFFFFA500),
    card: Color = Color.White,
): Colors {
    val white = Color.White
    val primaryLight = primary.mix(white, 0.88f)
    val textPrimary = Color(0xFF09090B)
    val textSecondary = Color(0xFF71717A)
    val border = primary.mix(white, 0.85f).desaturate(0.5f)
    val purple = Color(0xFFA78BFA)
    val cyan = Color(0xFF4DD0E1)
    val danger = Color(0xFFEF4444)
    return lightColorScheme().copy(
        primary = primary,
        onPrimary = white,
        primaryVariant = primaryLight,
        onPrimaryVariant = primary,
        primaryContainer = primaryLight,
        onPrimaryContainer = primary,
        secondary = purple,
        onSecondary = white,
        secondaryContainer = purple.mix(white, 0.88f),
        onSecondaryContainer = purple,
        secondaryContainerVariant = purple.mix(white, 0.88f),
        onSecondaryContainerVariant = purple,
        tertiaryContainer = cyan.mix(white, 0.88f),
        onTertiaryContainer = cyan,
        tertiaryContainerVariant = cyan.mix(white, 0.88f),
        background = background,
        onBackground = textPrimary,
        onBackgroundVariant = textSecondary,
        surface = card,
        onSurface = textPrimary,
        surfaceVariant = border,
        onSurfaceSecondary = textSecondary,
        onSurfaceVariantSummary = textSecondary,
        onSurfaceVariantActions = textSecondary,
        disabledOnSurface = Color(0xFFC7C7CC),
        surfaceContainer = card,
        onSurfaceContainer = textPrimary,
        onSurfaceContainerVariant = textSecondary,
        surfaceContainerHigh = border,
        onSurfaceContainerHigh = textSecondary,
        surfaceContainerHighest = Color(0xFFF4F4F5),
        onSurfaceContainerHighest = textPrimary,
        outline = border,
        dividerLine = border,
        error = danger,
        onError = white,
        errorContainer = accent.mix(white, 0.85f),
        onErrorContainer = accent,
        sliderKeyPoint = primary,
        sliderKeyPointForeground = border,
        sliderBackground = primary.mix(card, 0.8f),
    )
}

private fun dark(
    primary: Color,
    background: Color,
    card: Color,
    accent: Color = Color(0xFFFFA500),
): Colors {
    val white = Color.White
    val black = Color.Black
    val primaryLight = primary.mix(white, 0.88f)
    val textPrimary = white
    val textSecondary = Color(0xFF8E8E93)
    val border = Color(0xFF2A2A3E)
    val purple = Color(0xFFA78BFA)
    val cyan = Color(0xFF4DD0E1)
    val danger = Color(0xFFE57373)
    return darkColorScheme().copy(
        primary = primary,
        onPrimary = card,
        primaryVariant = primaryLight,
        onPrimaryVariant = primary,
        primaryContainer = primaryLight,
        onPrimaryContainer = primary,
        secondary = purple,
        onSecondary = card,
        secondaryContainer = purple.mix(card, 0.88f),
        onSecondaryContainer = purple,
        secondaryContainerVariant = purple.mix(card, 0.88f),
        onSecondaryContainerVariant = purple,
        tertiaryContainer = cyan.mix(card, 0.88f),
        onTertiaryContainer = cyan,
        tertiaryContainerVariant = cyan.mix(card, 0.88f),
        background = background,
        onBackground = textPrimary,
        onBackgroundVariant = textSecondary,
        surface = card,
        onSurface = textPrimary,
        surfaceVariant = border,
        onSurfaceSecondary = textSecondary,
        onSurfaceVariantSummary = textSecondary,
        onSurfaceVariantActions = textSecondary,
        disabledOnSurface = Color(0xFF4A4A55),
        surfaceContainer = card,
        onSurfaceContainer = textPrimary,
        onSurfaceContainerVariant = textSecondary,
        surfaceContainerHigh = Color(0xFF252540),
        onSurfaceContainerHigh = textSecondary,
        surfaceContainerHighest = Color(0xFF2E2E50),
        onSurfaceContainerHighest = textPrimary,
        outline = border,
        dividerLine = border,
        error = danger,
        onError = card,
        errorContainer = accent.mix(black, 0.85f),
        onErrorContainer = accent,
        windowDimming = black.copy(alpha = 0.6f),
        sliderKeyPoint = primary,
        sliderKeyPointForeground = Color(0xFF252540),
        sliderBackground = primary.mix(card, 0.8f),
    )
}

private fun Color.mix(other: Color, weight: Float): Color = Color(
    red * (1 - weight) + other.red * weight,
    green * (1 - weight) + other.green * weight,
    blue * (1 - weight) + other.blue * weight,
    alpha = 1f,
)

private fun Color.desaturate(factor: Float): Color {
    val avg = (red + green + blue) / 3f
    return Color(
        red * (1 - factor) + avg * factor,
        green * (1 - factor) + avg * factor,
        blue * (1 - factor) + avg * factor,
        alpha = alpha,
    )
}
