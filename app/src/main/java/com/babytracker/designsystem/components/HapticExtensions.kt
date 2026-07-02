package com.babytracker.designsystem.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * 在 Composable 中获取 HapticFeedback 实例。
 */
@Composable
fun rememberHaptic(): HapticFeedback = LocalHapticFeedback.current

/**
 * 长按删除 + 单击编辑 统一的 Modifier 扩展 — 内置触觉反馈。
 *
 * 用法：
 *   Card(
 *     Modifier.fillMaxWidth().longPressDeletable(
 *       haptic = haptic,
 *       onClick = { editingItem = item },
 *       onLongClick = { deletingItem = item }
 *     )
 *   )
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.longPressDeletable(
    haptic: HapticFeedback,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit,
): Modifier = this.combinedClickable(
    onClick = onClick,
    onLongClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onLongClick()
    },
)
