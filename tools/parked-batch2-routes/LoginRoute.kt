package com.babytracker.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * 登录路由 — 只负责 DI 装配与导航映射（登录成功自动返回）。
 */
@Composable
fun LoginRoute(navController: NavController) {
    val viewModel: LoginViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) navController.popBackStack()
    }

    LoginScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() },
    )
}