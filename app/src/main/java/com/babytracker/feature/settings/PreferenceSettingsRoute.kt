package com.babytracker.feature.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.babytracker.core.settings.DensityController
import com.babytracker.core.settings.ThemeController
import com.babytracker.navigation.AiSettings
import org.koin.compose.koinInject

/**
 * 使用偏好路由（组合根）— 装配 Theme/Density Controller 并把弹层作为槽位注入，
 * Screen 只收展示状态与回调（主题弹层本身在 SettingsSheets.kt，纯组件）。
 */
@Composable
fun PreferenceSettingsRoute(navController: NavController) {
    val themeCtrl: ThemeController = koinInject()
    val densityCtrl: DensityController = koinInject()

    PreferenceSettingsScreen(
        themeName = themeCtrl.currentTheme.displayName(),
        densityLabel = densityCtrl.currentDensity.label,
        onBack = { navController.popBackStack() },
        onOpenAiSettings = { navController.navigate(AiSettings) },
        themePicker = { onDismiss -> ThemePickerSheet(themeCtrl, onDismiss) },
        densityPicker = { onDismiss -> DensityPickerSheet(densityCtrl, onDismiss) },
    )
}

private fun com.babytracker.designsystem.theme.AppTheme.displayName(): String = when (name) {
    "pure" -> "纯净蓝"
    "aurora" -> "极光紫"
    "warm" -> "暖阳粉"
    "sunny" -> "阳光黄"
    "night" -> "暗夜深"
    "morandi" -> "莫兰迪"
    else -> "跟随系统"
}