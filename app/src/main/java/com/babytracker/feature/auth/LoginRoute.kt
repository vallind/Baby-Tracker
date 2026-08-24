package com.babytracker.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * 登录/注册路由（组合根）：只负责 DI 装配、ViewModel 创建与导航映射，
 * 登录成功弹栈返回与顶栏返回均由回调映射到导航，UI 渲染委托给 [LoginScreen]。
 */
@Composable
fun LoginRoute(navController: NavController) {
    val viewModel: LoginViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginScreen(
        state = uiState,
        onAccountChange = viewModel::onAccountChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        onToggleMode = viewModel::toggleMode,
        onLoginSuccess = { navController.popBackStack() },
        onBack = { navController.popBackStack() },
    )
}