package com.babytracker.core.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.babytracker.designsystem.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 主题控制器 — App 层状态（读 SettingsStore、暴露当前 AppTheme），
 * 不属于 Design System（DS 只提供纯令牌与组件，不依赖设置存储）。
 * 依赖方向：core.settings → designsystem（AppTheme 类型），DS 永不反向依赖。
 */
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