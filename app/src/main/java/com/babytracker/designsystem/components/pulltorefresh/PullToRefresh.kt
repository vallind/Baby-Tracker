package com.babytracker.designsystem.components.pulltorefresh

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 下拉刷新容器 —— P1 组件，封装 material3 1.4 PullToRefreshBox。
 *
 * Gesture/Physics 轴：下拉距离与释放阈值由 M3 内置物理提供，本组件不覆盖；
 * Motion 轴：指示器旋转与淡入走 M3 内置动效；
 * 颜色：指示器容器/内容色经 M3 colorScheme 默认值取色，而 BabyTrackerTheme
 *   已把自建令牌桥接进 colorScheme（theme 层单一事实来源），
 *   组件不再二次暴露颜色参数——避免 Token 泄漏。
 *
 * 用法：
 *   var refreshing by remember { mutableStateOf(false) }
 *   LaunchedEffect(refreshing) { if (refreshing) { sync(); refreshing = false } }
 *   AppPullToRefresh(isRefreshing = refreshing, onRefresh = { refreshing = true }) {
 *       LazyColumn { ... }
 *   }
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = state,
        contentAlignment = contentAlignment,
        modifier = modifier,
        content = content,
    )
}
