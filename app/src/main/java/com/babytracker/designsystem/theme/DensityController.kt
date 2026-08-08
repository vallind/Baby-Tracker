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

/** 界面密度控制器 — 仿 ThemeController，订阅设置流并暴露当前密度 */
class DensityController(private val settingsStore: SettingsStore) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var currentDensity by mutableStateOf(AppDensity.Comfortable)
        private set

    init {
        scope.launch {
            settingsStore.settings.collectLatest { settings ->
                currentDensity = AppDensity.fromKey(settings.appearance.density)
            }
        }
    }

    fun switchDensity(density: AppDensity) {
        currentDensity = density
        scope.launch(Dispatchers.IO) {
            settingsStore.update { current ->
                current.copy(appearance = current.appearance.copy(density = density.key))
            }
        }
    }
}
