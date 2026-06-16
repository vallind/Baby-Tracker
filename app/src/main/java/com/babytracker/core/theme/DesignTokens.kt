package com.babytracker.core.theme

import androidx.compose.ui.graphics.Color

object DT {
    val pageMargin = 20
    val cardGap = 16
    val cardRadius = 8
    val buttonRadius = 8
    val inputRadius = 8
    val iconSize = 22
    val iconBgSize = 40
    val appBarHeight = 56
}

data class ThemeColors(
    val primary: Color,
    val primaryLight: Color,
    val bg: Color,
    val card: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val divider: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val pink: Color,
    val blue: Color,
    val green: Color,
    val yellow: Color,
    val purple: Color,
    val cyan: Color,
    val tagBg: Color,
    val tagText: Color,
)

data class AppTheme(
    val name: String,
    val colors: ThemeColors,
) {
    companion object {
        val pure = AppTheme("pure", ThemeColors(
            primary = Color(0xFF2563EB),
            primaryLight = Color(0xFFEFF6FF),
            bg = Color(0xFFFFFFFF),
            card = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFE4E4E7),
            textPrimary = Color(0xFF09090B),
            textSecondary = Color(0xFF71717A),
            textHint = Color(0xFFC7C7CC),
            divider = Color(0xFFE4E4E7),
            success = Color(0xFF16A34A),
            warning = Color(0xFFF59E0B),
            danger = Color(0xFFEF4444),
            pink = Color(0xFFF43F5E),
            blue = Color(0xFF3B82F6),
            green = Color(0xFF22C55E),
            yellow = Color(0xFFEAB308),
            purple = Color(0xFFA855F7),
            cyan = Color(0xFF06B6D4),
            tagBg = Color(0xFFFEF2F2),
            tagText = Color(0xFFEF4444),
        ))

        val aurora = AppTheme("aurora", ThemeColors(
            primary = Color(0xFF7C6CF0),
            primaryLight = Color(0xFFE8E0FF),
            bg = Color(0xFFFFFFFF),
            card = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFE4E4E7),
            textPrimary = Color(0xFF09090B),
            textSecondary = Color(0xFF71717A),
            textHint = Color(0xFFC7C7CC),
            divider = Color(0xFFE4E4E7),
            success = Color(0xFF6EE7B7),
            warning = Color(0xFFFCD34D),
            danger = Color(0xFFF87171),
            pink = Color(0xFFC4B5FD),
            blue = Color(0xFF7C6CF0),
            green = Color(0xFF6EE7B7),
            yellow = Color(0xFFFCD34D),
            purple = Color(0xFFA78BFA),
            cyan = Color(0xFF67E8F9),
            tagBg = Color(0xFFF3E8FF),
            tagText = Color(0xFF8B5CF6),
        ))

        val warm = AppTheme("warm", ThemeColors(
            primary = Color(0xFFFF8A80),
            primaryLight = Color(0xFFFFE8E0),
            bg = Color(0xFFFFFBF7),
            card = Color(0xFFFFFBF7),
            cardBorder = Color(0xFFE8E0E0),
            textPrimary = Color(0xFF09090B),
            textSecondary = Color(0xFF71717A),
            textHint = Color(0xFFC7C7CC),
            divider = Color(0xFFE8E0E0),
            success = Color(0xFF81C784),
            warning = Color(0xFFFFB74D),
            danger = Color(0xFFE57373),
            pink = Color(0xFFFF8A9E),
            blue = Color(0xFF90CAF9),
            green = Color(0xFF81C784),
            yellow = Color(0xFFFFE082),
            purple = Color(0xFFCE93D8),
            cyan = Color(0xFF80DEEA),
            tagBg = Color(0xFFFFE8E0),
            tagText = Color(0xFFE07060),
        ))

        val sunny = AppTheme("sunny", ThemeColors(
            primary = Color(0xFFF5A623),
            primaryLight = Color(0xFFFFF3E0),
            bg = Color(0xFFFFFAF0),
            card = Color(0xFFFFFDF5),
            cardBorder = Color(0xFFF0E8D8),
            textPrimary = Color(0xFF09090B),
            textSecondary = Color(0xFF71717A),
            textHint = Color(0xFFC7C7CC),
            divider = Color(0xFFF0E8D8),
            success = Color(0xFF7CB342),
            warning = Color(0xFFFFB300),
            danger = Color(0xFFE57373),
            pink = Color(0xFFFFAB91),
            blue = Color(0xFF90CAF9),
            green = Color(0xFFAED581),
            yellow = Color(0xFFFFD54F),
            purple = Color(0xFFCE93D8),
            cyan = Color(0xFF80DEEA),
            tagBg = Color(0xFFFFF0E0),
            tagText = Color(0xFFE67A2E),
        ))

        val night = AppTheme("night", ThemeColors(
            primary = Color(0xFF5C6BC0),
            primaryLight = Color(0xFF1A2744),
            bg = Color(0xFF12121F),
            card = Color(0xFF1E1E32),
            cardBorder = Color(0xFF2A2A3E),
            textPrimary = Color.White,
            textSecondary = Color(0xFF8E8E93),
            textHint = Color(0xFF666680),
            divider = Color(0xFF2A2A3E),
            success = Color(0xFF4DB6AC),
            warning = Color(0xFFFFB74D),
            danger = Color(0xFFE57373),
            pink = Color(0xFFE57373),
            blue = Color(0xFF5C6BC0),
            green = Color(0xFF4DB6AC),
            yellow = Color(0xFFFFB74D),
            purple = Color(0xFF9575CD),
            cyan = Color(0xFF4DD0E1),
            tagBg = Color(0xFF3A2020),
            tagText = Color(0xFFE57373),
        ))

        val morandi = AppTheme("morandi", ThemeColors(
            primary = Color(0xFFB0BEC5),
            primaryLight = Color(0xFFECEFF1),
            bg = Color(0xFFFAFAFA),
            card = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFE8E8E8),
            textPrimary = Color(0xFF09090B),
            textSecondary = Color(0xFF71717A),
            textHint = Color(0xFFC7C7CC),
            divider = Color(0xFFE8E8E8),
            success = Color(0xFFA5D6A7),
            warning = Color(0xFFFFCC80),
            danger = Color(0xFFEF9A9A),
            pink = Color(0xFFE0B0B0),
            blue = Color(0xFFA0B0C0),
            green = Color(0xFFA0C0A0),
            yellow = Color(0xFFD0C0A0),
            purple = Color(0xFFC0B0D0),
            cyan = Color(0xFFA0D0D0),
            tagBg = Color(0xFFF5E8E8),
            tagText = Color(0xFFC09090),
        ))

        val all = listOf(pure, aurora, warm, sunny, night, morandi)
    }
}
