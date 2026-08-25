package com.babytracker.designsystem.components.errorstate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.i18n.AppStrings

/**
 * 错误状态枚举：驱动默认文案与图标（PResult 形态，status 给默认、参数留逃生口）。
 */
enum class ErrorStatus(val emoji: String) {
    /** 通用加载失败 */
    Generic("⚠️"),

    /** 网络不可用 */
    Network("📡"),

    /** 内容不存在（深链失效/记录已删除） */
    NotFound("🔍"),
}

/**
 * 统一错误状态组件 —— 页面级数据块加载失败的标准展示（E 批内容状态编排）。
 *
 * 四态书写约定中的 Error 态唯一入口；内部组合 EmptyState 复用其令牌体系与读屏语义。
 * 操作失败/表单校验等轻提示不使用本组件（走输入框 errorMessage / snackbar 通道）。
 *
 * 用法：
 *   AppErrorState(message = state.errorMessage, onRetry = onRetry)
 *   AppErrorState(status = ErrorStatus.NotFound)
 *   AppErrorState(title = AppStrings.errorStatsTitle, message = msg, retryLabel = AppStrings.reload, onRetry = onRetry)
 */
@Composable
fun AppErrorState(
    modifier: Modifier = Modifier,
    status: ErrorStatus = ErrorStatus.Generic,
    title: String? = null,
    message: String? = null,
    onRetry: (() -> Unit)? = null,
    retryLabel: String = AppStrings.retry,
) {
    val defaultTitle = when (status) {
        ErrorStatus.Generic -> AppStrings.errorGenericTitle
        ErrorStatus.Network -> AppStrings.errorNetworkTitle
        ErrorStatus.NotFound -> AppStrings.errorNotFoundTitle
    }
    val defaultHint = when (status) {
        ErrorStatus.Generic -> AppStrings.errorGenericHint
        ErrorStatus.Network -> AppStrings.errorNetworkHint
        ErrorStatus.NotFound -> AppStrings.errorNotFoundHint
    }
    EmptyState(
        emoji = status.emoji,
        title = title ?: defaultTitle,
        subtitle = message ?: defaultHint,
        actionText = if (onRetry != null) retryLabel else null,
        onAction = onRetry,
        modifier = modifier,
    )
}
