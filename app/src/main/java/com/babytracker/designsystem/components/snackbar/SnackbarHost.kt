package com.babytracker.designsystem.components.snackbar

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 全局提示宿主 — 对标 M3 SnackbarHost，消费 AppComponentTokens.snackbarHost。
 *
 * 用法：
 *   val hostState = remember { SnackbarHostState() }
 *   AppScaffold(snackbarHost = { AppSnackbarHost(hostState) }) { ... }
 *
 * 注意：M3 1.4.0 起 Snackbar 移除了 tonalElevation 参数（内部固定走
 * SnackbarTokens.ContainerElevation），故 elevation 令牌暂不注入组件，预留后续版本接入。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            Snackbar(
                snackbarData = data,
                containerColor = SnackbarHostDefaults.containerColor(),
                contentColor = SnackbarHostDefaults.contentColor(),
                shape = RoundedCornerShape(SnackbarHostDefaults.cornerRadius()),
            )
        },
    )
}
