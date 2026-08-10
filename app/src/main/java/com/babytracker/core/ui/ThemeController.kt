package com.babytracker.core.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.babytracker.core.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 主题控制器：订阅设置流并暴露当前主题名，切换时持久化。
 * 主题名到 Elyon 参数的映射见 ElyonThemeResolver。
 */
class ThemeController(private val settingsStore: SettingsStore) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var currentThemeName by mutableStateOf("pure")
        private set

    init {
        scope.launch {
            settingsStore.settings.collectLatest { settings ->
                currentThemeName = settings.appearance.themeName
            }
        }
    }

    fun switchTheme(name: String) {
        currentThemeName = name
        scope.launch(Dispatchers.IO) {
            settingsStore.update { current ->
                current.copy(appearance = current.appearance.copy(themeName = name))
            }
        }
    }
}
