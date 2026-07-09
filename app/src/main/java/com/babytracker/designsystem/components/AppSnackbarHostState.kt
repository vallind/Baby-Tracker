package com.babytracker.designsystem.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AppSnackbarHostState {
    private val snackbarHostState = SnackbarHostState()

    val hostState: SnackbarHostState get() = snackbarHostState

    fun showUndo(scope: CoroutineScope, message: String, onUndo: () -> Unit) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "撤销",
                duration = SnackbarDuration.Short,
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onUndo()
            }
        }
    }

    fun showSuccess(scope: CoroutineScope, message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    fun showError(scope: CoroutineScope, message: String, onRetry: (() -> Unit)? = null) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (onRetry != null) "重试" else null,
                duration = SnackbarDuration.Long,
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onRetry?.invoke()
            }
        }
    }
}
