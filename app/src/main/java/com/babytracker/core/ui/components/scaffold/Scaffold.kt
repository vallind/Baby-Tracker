package com.babytracker.core.ui.components.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
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
                content(padding)
            }
        }
    }
}
