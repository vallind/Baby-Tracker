package com.babytracker.designsystem.components.snackbar

import com.babytracker.i18n.AppStrings
import io.elyon.kmp.basic.SnackbarDuration
import io.elyon.kmp.basic.SnackbarHostState
import io.elyon.kmp.basic.SnackbarResult

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
    /** 显示无需操作的短提示。 */
    suspend fun showMessage(message: String) {
        hostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short,
        )
    }

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
}
