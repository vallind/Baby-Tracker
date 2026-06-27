package com.babytracker.designsystem.theme

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ThemeController(private val sp: SharedPreferences) {
    var currentTheme by mutableStateOf(load())
        private set

    private fun load(): AppTheme {
        val name = sp.getString(KEY_THEME, "pure") ?: "pure"
        return AppTheme.all.firstOrNull { it.name == name } ?: AppTheme.pure
    }

    fun switchTheme(name: String) {
        val theme = AppTheme.all.firstOrNull { it.name == name } ?: AppTheme.pure
        currentTheme = theme
        sp.edit().putString(KEY_THEME, name).apply()
    }

    companion object {
        private const val KEY_THEME = "theme_name"
    }
}
