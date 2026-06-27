package com.babytracker.designsystem.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.babytracker.designsystem.i18n.AppStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 撤销 Snackbar 辅助 — 消除 6+ 处重复的 showSnackbar + ActionPerformed 样板
 *
 * 对标 Palette FeedbackDisplay 组件体系。
 *
 * 用法：
 *   val scope = rememberCoroutineScope()
 *   val snackbar = remember { AppSnackbar(snackbarHostState, scope) }
 *   snackbar.showUndo(onUndo = { repo.insert(record) })
 */
class AppSnackbar(
    private val hostState: SnackbarHostState,
    private val scope: CoroutineScope,
) {
    /**
     * 显示"已删除，可撤销" Snackbar
     *
     * @param onUndo 点击撤销时执行的回调（如重新插入记录）
     */
    fun showUndo(onUndo: () -> Unit) {
        scope.launch {
            val result = hostState.showSnackbar(
                message = AppStrings.deleted,
                actionLabel = AppStrings.undo,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndo()
            }
        }
    }
}
