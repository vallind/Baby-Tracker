package com.babytracker.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

val LocalThemeColors = compositionLocalOf { AppTheme.pure.colors }

val BabyTrackerTypography = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, lineHeight = 64.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontSize = 45.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold),
    displaySmall = TextStyle(fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
)

val BabyTrackerShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

fun AppTheme.toColorScheme(): androidx.compose.material3.ColorScheme {
    val c = colors
    val useDark = name == "night"
    val scheme = if (useDark) darkColorScheme(
        primary = c.primary,
        onPrimary = c.card,
        primaryContainer = c.primaryLight,
        onPrimaryContainer = c.primary,
        secondary = c.purple,
        onSecondary = c.card,
        secondaryContainer = c.purple.copy(alpha = 0.12f),
        tertiary = c.cyan,
        tertiaryContainer = c.cyan.copy(alpha = 0.12f),
        background = c.bg,
        onBackground = c.textPrimary,
        surface = c.card,
        onSurface = c.textPrimary,
        surfaceVariant = c.divider,
        onSurfaceVariant = c.textSecondary,
        outline = c.cardBorder,
        outlineVariant = c.cardBorder.copy(alpha = 0.5f),
        error = c.danger,
        onError = c.card,
        errorContainer = c.tagBg,
        scrim = Color.Black.copy(alpha = 0.32f),
    ) else lightColorScheme(
        primary = c.primary,
        onPrimary = c.card,
        primaryContainer = c.primaryLight,
        onPrimaryContainer = c.primary,
        secondary = c.purple,
        onSecondary = c.card,
        secondaryContainer = c.purple.copy(alpha = 0.12f),
        tertiary = c.cyan,
        tertiaryContainer = c.cyan.copy(alpha = 0.12f),
        background = c.bg,
        onBackground = c.textPrimary,
        surface = c.card,
        onSurface = c.textPrimary,
        surfaceVariant = c.divider,
        onSurfaceVariant = c.textSecondary,
        outline = c.cardBorder,
        outlineVariant = c.cardBorder.copy(alpha = 0.5f),
        error = c.danger,
        onError = c.card,
        errorContainer = c.tagBg,
        scrim = Color.Black.copy(alpha = 0.32f),
    )
    return scheme
}

@Composable
fun BabyTrackerTheme(
    theme: AppTheme = AppTheme.pure,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = theme.toColorScheme()
    val darkTheme = theme.name == "night"

    SideEffect {
        val activity = context as? Activity
        if (activity != null && Build.VERSION.SDK_INT >= 21) {
            val window = activity.window
            window.statusBarColor = if (darkTheme) colorScheme.surface.toArgb() else colorScheme.primary.copy(alpha = 0.05f).toArgb()
            WindowCompat.getInsetsController(window, activity.window.decorView).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val mergedColors = theme.colors.copy(
        primary = colorScheme.primary,
        primaryLight = colorScheme.primaryContainer,
        bg = colorScheme.background,
        card = colorScheme.surface,
        cardBorder = colorScheme.outlineVariant,
        textPrimary = colorScheme.onSurface,
        textSecondary = colorScheme.onSurfaceVariant,
        textHint = colorScheme.outline,
        divider = colorScheme.surfaceVariant,
        success = colorScheme.tertiary,
        danger = colorScheme.error,
        blue = colorScheme.primary,
        green = colorScheme.tertiary,
        purple = colorScheme.secondary,
        cyan = colorScheme.tertiary,
        tagBg = colorScheme.errorContainer,
        tagText = colorScheme.onErrorContainer,
    )

    CompositionLocalProvider(LocalThemeColors provides mergedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BabyTrackerTypography,
            shapes = BabyTrackerShapes,
            content = content,
        )
    }
}
