package com.babytracker.feature.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.navigation.AppBottomBar
import org.koin.androidx.compose.koinViewModel

/**
 * 统计页路由（组合根）— 只做 DI 装配与导航映射。
 * 当前宝宝解析与数据加载在 ViewModel（init 内 snapshotFlow 监听 currentBabyId 自动加载）；
 * 本路由不 koinInject 业务对象、不解析宝宝；底部导航槽位在此注入。
 */
@Composable
fun StatsRoute(navController: NavController) {
    val viewModel: StatsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    StatsScreen(
        state = state,
        bottomBar = { AppBottomBar(navController) },
        onSelectPeriod = viewModel::selectPeriod,
        onGoBack = viewModel::goBack,
        onGoForward = viewModel::goForward,
        onRetry = viewModel::retry,
    )
}