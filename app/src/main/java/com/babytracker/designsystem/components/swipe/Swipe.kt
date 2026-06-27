package com.babytracker.designsystem.components.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.theme.DT

/**
 * ---------- 滑动删除容器（Material 3 SwipeToDismissBox）----------
 *
 * 向左滑动 → 红色背景 + 删除图标，松手弹出确认弹窗。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    var showConfirm by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showConfirm = true
                false
            } else {
                false
            }
        }
    )

    Box(
        modifier = modifier.clip(RoundedCornerShape(DT.cardRadius.dp))
    ) {
        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.fillMaxSize(),
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE53935))
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

    AppConfirmDialog(
        show = showConfirm,
        onConfirm = {
            showConfirm = false
            onDelete()
        },
        onDismiss = { showConfirm = false },
    )
}
=======
    AppConfirmDialog(
        show = showConfirm,
        onConfirm = {
            showConfirm = false
            onDelete()
>>>>>>> re:app/src/main/java/com/babytracker/designsystem/components/SwipeContainers.kt
        },
        onDismiss = { showConfirm = false },
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

    Box(
        modifier = modifier.clip(RoundedCornerShape(DT.cardRadius.dp))
    ) {
        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.fillMaxSize(),
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = false,
            backgroundContent = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1976D2))
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
}

/**
 * ---------- 滑动删除+编辑容器 ----------
 *
 * 向右滑动 → 蓝色编辑背景；向左滑动 → 红色删除背景（松手弹出确认弹窗）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToEditDeleteContainer(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    var showConfirm by remember { mutableStateOf(false) }
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
                    showConfirm = true
                    false
                }
                else -> false
            }
        }
    )

    if (consumed) return

    Box(
        modifier = modifier.clip(RoundedCornerShape(DT.cardRadius.dp))
    ) {
        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.fillMaxSize(),
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
        backgroundContent = {
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1976D2))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑", tint = Color.White)
                }
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE53935))
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

    AppConfirmDialog(
        show = showConfirm,
        onConfirm = {
            showConfirm = false
            onDelete()
        },
        onDismiss = { showConfirm = false },
    )
}
