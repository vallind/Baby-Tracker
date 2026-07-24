package com.babytracker.designsystem.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.babytracker.core.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ThemeController(private val settingsStore: SettingsStore) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var currentTheme by mutableStateOf(AppTheme.pure)
        private set

    init {
        scope.launch {
            settingsStore.settings.collectLatest { settings ->
                currentTheme = resolve(settings.appearance.themeName)
            }
        }
    }

    fun switchTheme(name: String) {
        currentTheme = resolve(name)
        scope.launch(Dispatchers.IO) {
            settingsStore.update { current ->
                current.copy(appearance = current.appearance.copy(themeName = name))
            }
        }
    }

    private fun resolve(name: String): AppTheme =
        AppTheme.all.firstOrNull { it.name == name } ?: AppTheme.pure
}
