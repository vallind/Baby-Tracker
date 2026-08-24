package com.babytracker.feature.message

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.navigation.AppBottomBar
import org.koin.androidx.compose.koinViewModel

/**
 * 消息中心路由（组合根）：只负责 DI 装配、ViewModel 创建与导航映射，
 * 页面状态收集后交给 [MessageScreen] 纯 UI 渲染，底部导航槽位在此注入。
 */
@Composable
fun MessageRoute(navController: NavController) {
    val viewModel: MessageViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    MessageScreen(
        state = state,
        onMarkAllRead = viewModel::markAllRead,
        onMarkRead = viewModel::markRead,
        onDelete = viewModel::delete,
        bottomBar = { AppBottomBar(navController) },
    )
}