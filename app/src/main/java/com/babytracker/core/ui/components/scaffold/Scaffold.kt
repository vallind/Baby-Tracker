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
                // 底栏存在时，内容不垫底栏高度：列表内容滚动时延伸到栏后面，
                // backdrop 才能录到栏后内容，毛玻璃才有可见效果（参照 Elyon 示例）。
                // 顶栏保持原 padding，避免首行内容被顶栏永久遮挡。
                content(
                    PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = if (bottomBar != null) 0.dp else padding.calculateBottomPadding(),
                        start = padding.calculateStartPadding(LocalLayoutDirection.current),
                        end = padding.calculateEndPadding(LocalLayoutDirection.current),
                    ),
                )
            }
        }
    }
}
