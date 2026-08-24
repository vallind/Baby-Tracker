package com.babytracker.feature.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * 同步设置路由 — 只负责 DI 装配与导航映射。
 */
@Composable
fun SyncSettingsRoute(navController: NavController) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val syncViewModel: SyncViewModel = koinViewModel()
    SyncSettingsScreen(
        settingsViewModel = settingsViewModel,
        syncViewModel = syncViewModel,
        onBack = { navController.popBackStack() },
    )
}