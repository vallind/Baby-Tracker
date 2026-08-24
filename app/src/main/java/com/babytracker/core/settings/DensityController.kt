package com.babytracker.core.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.babytracker.designsystem.theme.AppDensity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 界面密度控制器 — App 层状态（仿 ThemeController，订阅设置流并暴露当前密度），
 * 不属于 Design System。依赖方向：core.settings → designsystem（AppDensity 类型）。
 */
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