package com.babytracker.feature.settings

import com.babytracker.designsystem.i18n.AppStrings

// ═══════════════════════════════════════════════════════════
//  主题显示名 — 单一来源
//  原 SettingsSheets.kt 与 PreferenceSettingsRoute.kt 各维护一份
//  seed 名 → 中文显示名映射，现收敛于此；文案 key 在 AppStrings。
//  主题本体与颜色以 designsystem 的 AppTheme 为权威，本文件只补显示名。
// ═══════════════════════════════════════════════════════════

/** AppTheme seed 名 → 用户可见显示名（未知名回退"跟随系统"） */
internal fun appThemeDisplayName(name: String): String = when (name) {
    "pure" -> AppStrings.themeNamePure
    "aurora" -> AppStrings.themeNameAurora
    "warm" -> AppStrings.themeNameWarm
    "sunny" -> AppStrings.themeNameSunny
    "night" -> AppStrings.themeNameNight
    "morandi" -> AppStrings.themeNameMorandi
    else -> AppStrings.themeFollowSystem
}
