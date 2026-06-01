package com.example.myapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemePreference(private val context: Context) {
    companion object {
        private val THEME_MODE = intPreferencesKey("theme_mode")
    }

    val themeMode: Flow<Int> = context.themeStore.data.map { prefs ->
        prefs[THEME_MODE] ?: 0
    }

    suspend fun setThemeMode(mode: Int) {
        context.themeStore.edit { prefs -> prefs[THEME_MODE] = mode }
    }
}
