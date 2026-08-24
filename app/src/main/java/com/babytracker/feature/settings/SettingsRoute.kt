package com.babytracker.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.navigation.AppBottomBar
import com.babytracker.navigation.BabyManagement
import com.babytracker.navigation.Backup
import com.babytracker.navigation.DataSettings
import com.babytracker.navigation.Family
import com.babytracker.navigation.LogViewer
import com.babytracker.navigation.Login
import com.babytracker.navigation.PreferenceSettings
import com.babytracker.navigation.Reminder
import com.babytracker.navigation.SupportSettings
import org.koin.androidx.compose.koinViewModel

/**
 * 设置主页路由（组合根）— 只做 DI 装配与导航映射；
 * 登录态/宝宝等展示状态在 SettingsViewModel；Section 编排在 SettingsScreen。
 */
@Composable
fun SettingsRoute(navController: NavController) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()

    SettingsScreen(
        state = state,
        bottomBar = { AppBottomBar(navController) },
        onOpenUserAccount = {
            navController.navigate(if (state.isLoggedIn) Family else Login)
        },
        onOpenBabyManagement = { navController.navigate(BabyManagement) },
        onOpenReminder = { navController.navigate(Reminder) },
        onOpenBackup = { navController.navigate(Backup) },
        onOpenLogViewer = { navController.navigate(LogViewer) },
        onOpenPreference = { navController.navigate(PreferenceSettings) },
        onOpenData = { navController.navigate(DataSettings) },
        onOpenSupport = { navController.navigate(SupportSettings) },
        onLogout = viewModel::signOut,
        onSaveNickname = viewModel::setNickname,
    )
}