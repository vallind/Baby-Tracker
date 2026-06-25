package com.babytracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.babytracker.core.theme.DT

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

/**
 * ---------- 滑动删除容器（Material 3 SwipeToDismissBox）----------
 *
 * 向左滑动 → 红色背景 + 删除图标，松手即删并回调 [onDelete]。
 * 内部处理 dismiss 状态，避免 Room 重新 emit 造成视觉回弹。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    var dismissed by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                dismissed = true
                onDelete()
                true
            } else {
                false
            }
        }
    )

    // —— 被"消费"后直接移除整个 SwipeToDismissBox，等 Room 刷新后自然无此项 ——
    if (dismissed) return

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE53935), RoundedCornerShape(DT.cardRadius.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.White,
                )
            }
        },
        content = content,
    )
}

/**
 * ---------- 滑动编辑容器（Material 3 SwipeToDismissBox）----------
 *
 * 向右滑动 → 蓝色背景 + 编辑图标，松手即回调 [onEdit]。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToEditContainer(
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    var consumed by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                consumed = true
                onEdit()
                true
            } else {
                false
            }
        }
    )

    if (consumed) return

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1976D2), RoundedCornerShape(DT.cardRadius.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "编辑",
                    tint = Color.White,
                )
            }
        },
        content = content,
    )
}

/**
 * ---------- 滑动删除+编辑容器 ----------
 *
 * 向右滑动 → 蓝色编辑背景；向左滑动 → 红色删除背景。
 * [onEdit] / [onDelete] 在松手达到阈值时回调。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToEditDeleteContainer(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    var consumed by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    consumed = true
                    onEdit()
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    consumed = true
                    onDelete()
                    true
                }
                else -> false
            }
        }
    )

    if (consumed) return

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            // 根据当前滑动方向显示不同的背景色
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                // 向右滑：编辑（蓝色）
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1976D2), RoundedCornerShape(DT.cardRadius.dp))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑", tint = Color.White)
                }
            } else {
                // 向左滑：删除（红色）
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE53935), RoundedCornerShape(DT.cardRadius.dp))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.White)
                }
            }
        },
        content = content,
    )
}
