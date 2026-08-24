package com.babytracker.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.babytracker.BabyTrackerApp
import com.babytracker.navigation.LogViewer
import org.koin.androidx.compose.koinViewModel

/**
 * 帮助与关于路由（组合根）— 装配 SettingsViewModel；
 * 日志抓取开关的全局副作用（appLogTree.enabled）在此映射。
 */
@Composable
fun SupportSettingsRoute(navController: NavController) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val logCaptureEnabled = settings.diagnostics.logCaptureEnabled
    val versionName = androidx.compose.runtime.remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }

    SupportSettingsScreen(
        logCaptureEnabled = logCaptureEnabled,
        versionName = versionName,
        onBack = { navController.popBackStack() },
        onToggleLogCapture = { enabled ->
            settingsViewModel.updateSettings { current ->
                current.copy(
                    diagnostics = current.diagnostics.copy(
                        logCaptureEnabled = enabled,
                    ),
                )
            }
            val app = context.applicationContext as BabyTrackerApp
            app.appLogTree.enabled = enabled
        },
        onOpenLogViewer = { navController.navigate(LogViewer) },
    )
}