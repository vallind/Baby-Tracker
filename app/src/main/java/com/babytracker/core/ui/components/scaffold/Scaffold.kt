package com.babytracker.core.ui.components.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.babytracker.core.ui.components.LocalScaffoldBackdrop
import io.elyon.kmp.blur.layerBackdrop
import io.elyon.kmp.blur.rememberLayerBackdrop
import io.elyon.kmp.basic.Scaffold
import io.elyon.kmp.theme.ElyonTheme

@Composable
fun AppScaffold(
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    fab: @Composable (() -> Unit)? = null,
    snackbarHost: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    // 顶栏全出血：内容延伸到顶栏下面，滚动时产生真毛玻璃效果
    edgeToEdgeTop: Boolean = false,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    // 页面内容捕获为 backdrop，供底部导航毛玻璃使用（过渡期实现，迁移完成后由 Elyon Scaffold 接管）
    val containerColor = ElyonTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(containerColor)
        drawContent()
    }
    // Provider 放在 Scaffold 外层：bottomBar/topBar 插槽与内容都能读到 backdrop
    CompositionLocalProvider(LocalScaffoldBackdrop provides backdrop) {
        Scaffold(
            topBar = topBar ?: {},
            bottomBar = bottomBar ?: {},
            floatingActionButton = fab ?: {},
            snackbarHost = snackbarHost ?: {},
            modifier = modifier,
            containerColor = containerColor,
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                // 底栏存在时内容不垫底栏高度；列表屏开启 edgeToEdgeTop 时顶栏同理，
                // backdrop 才能录到栏后内容，毛玻璃才有可见效果（参照 Elyon 示例）。
                content(
                    PaddingValues(
                        top = if (topBar != null && edgeToEdgeTop) 0.dp else padding.calculateTopPadding(),
                        bottom = if (bottomBar != null) 0.dp else padding.calculateBottomPadding(),
                        start = padding.calculateStartPadding(LocalLayoutDirection.current),
                        end = padding.calculateEndPadding(LocalLayoutDirection.current),
                    ),
                )
            }
        }
    }
}
