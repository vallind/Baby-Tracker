package com.babytracker.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class CareType {
    FEEDING, SLEEP, DIAPER, GROWTH, HEALTH, VACCINATION, REMINDER
}

@Immutable
data class CareTypeVisuals(
    val label: String,
    val emoji: String,
    val container: Color,
    val content: Color,
    val brush: Brush? = null,
)

interface CareTypePalette {
    fun of(type: CareType): CareTypeVisuals
}

class DefaultCareTypePalette(private val colors: AppColors) : CareTypePalette {
    override fun of(type: CareType): CareTypeVisuals = when (type) {
        CareType.FEEDING -> CareTypeVisuals("喂养", "🍼", colors.warning, colors.onSurface)
        CareType.SLEEP -> CareTypeVisuals(
            "睡眠", "🌙", colors.secondary, colors.onSecondary,
            brush = Brush.linearGradient(
                listOf(colors.secondary, colors.secondary.copy(alpha = 0.7f)),
                start = Offset.Zero, end = Offset.Infinite,
            ),
        )
        CareType.DIAPER -> CareTypeVisuals(
            "尿布", "🧷", colors.tertiary, colors.onSurface,
            brush = Brush.linearGradient(
                listOf(colors.tertiary, colors.tertiary.copy(alpha = 0.7f)),
                start = Offset.Zero, end = Offset.Infinite,
            ),
        )
        CareType.GROWTH -> CareTypeVisuals("生长", "📏", colors.success, colors.onSurface)
        CareType.HEALTH -> CareTypeVisuals("健康", "❤️", colors.primary, colors.onPrimary)
        CareType.VACCINATION -> CareTypeVisuals("疫苗", "💉", colors.info, colors.onSurface)
        CareType.REMINDER -> CareTypeVisuals("提醒", "⏰", colors.warning.copy(alpha = 0.12f), colors.warning)
    }
}

val LocalCareTypePalette = staticCompositionLocalOf<CareTypePalette> {
    DefaultCareTypePalette(AppColors.light())
}
