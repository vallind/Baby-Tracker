package com.babytracker.core.ui.components.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.babytracker.core.ui.components.LocalScaffoldBackdrop
import com.babytracker.core.ui.components.appBlur
import io.elyon.kmp.basic.Snackbar
import io.elyon.kmp.basic.SnackbarDefaults
import io.elyon.kmp.basic.SnackbarHost
import io.elyon.kmp.basic.SnackbarHostState
import io.elyon.kmp.theme.ElyonTheme

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
@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val backdrop = LocalScaffoldBackdrop.current
    SnackbarHost(
        state = hostState,
        modifier = modifier,
        content = { data ->
            Snackbar(
                data = data,
                modifier = Modifier.appBlur(backdrop = backdrop, radius = 24f, tintAlpha = 0.85f),
                colors = SnackbarDefaults.snackbarColors(
                    containerColor = Color.Transparent,
                    contentColor = ElyonTheme.colorScheme.onSurface,
                    actionContentColor = ElyonTheme.colorScheme.onPrimary,
                    actionContainerColor = ElyonTheme.colorScheme.primary,
                ),
            )
        },
    )
}
