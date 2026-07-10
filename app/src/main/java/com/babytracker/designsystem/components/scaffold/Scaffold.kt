package com.babytracker.designsystem.components.scaffold

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.components.scaffold.ScaffoldDefaults as AppScaffoldDefaults

@Composable
fun AppScaffold(
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    fab: @Composable (() -> Unit)? = null,
    snackbarHost: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = AppScaffoldDefaults.containerColor(),
        topBar = topBar ?: {},
        bottomBar = bottomBar ?: {},
        floatingActionButton = fab ?: {},
        snackbarHost = snackbarHost ?: {},
        modifier = modifier,
        content = content,
    )
}
