package com.babytracker.designsystem.components.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.babytracker.designsystem.i18n.AppStrings

/**
 * 撤销 Snackbar 辅助 — 消除 7 处重复的 showSnackbar + ActionPerformed 样板
 *
 * 对标 Palette FeedbackDisplay 组件体系。
 *
 * 用法：
 *   scope.launch {
 *       repo.delete(record)
 *       snackbar.showUndo("已删除xxx记录") { repo.insert(record) }
 *   }
 */
class AppSnackbar(private val hostState: SnackbarHostState) {
    /**
     * 显示"已删除，可撤销" Snackbar。suspend 函数，调用方负责在协程中调用。
     *
     * @param message 自定义删除提示，默认使用 AppStrings.deleted
     * @param onUndo 点击撤销时执行的回调（如重新插入记录）
     */
    suspend fun showUndo(message: String = AppStrings.deleted, onUndo: suspend () -> Unit) {
        val result = hostState.showSnackbar(
            message = message,
            actionLabel = AppStrings.undo,
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) {
            onUndo()
        }
    }

    suspend fun showSuccess(message: String) {
        hostState.currentSnackbarData?.dismiss()
        hostState.showSnackbar(message, duration = SnackbarDuration.Short)
    }
}
