package com.babytracker.feature.message

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.babytracker.navigation.AppBottomBar
import org.koin.androidx.compose.koinViewModel

/**
 * 消息中心路由：负责创建 ViewModel、注入底部导航栏回调，
 * 页面本身不感知导航细节。
 */
@Composable
fun MessageRoute(navController: NavController) {
    val viewModel: MessageViewModel = koinViewModel()
    MessageScreen(
        viewModel = viewModel,
        bottomBar = { AppBottomBar(navController) },
    )
}