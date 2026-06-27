package com.babytracker.designsystem.components.recordcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.theme.LocalThemeColors

/**
 * 记录卡片 — 滑动删除+点击编辑一体化组件
 *
 * 内部复合 SwipeToDismissBox + Card，红色背景与卡片尺寸天然一致，杜绝溢出。
 *
 * 优先级模型：
 *   显式参数 > RecordCardDefaults > CardDefaults > 组件令牌
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun RecordCard(
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = RecordCardDefaults.cornerRadius(),
    containerColor: Color = LocalThemeColors.current.card,
    elevation: Dp = RecordCardDefaults.elevation(),
    innerPadding: Dp = RecordCardDefaults.innerPadding(),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
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

    Box(modifier = modifier.clip(shape)) {
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
            content = {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .shadow(elevation, shape)
                        .clickable(onClick = onClick),
                    shape = shape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                ) {
                    Row(
                        Modifier.padding(innerPadding),
                        verticalAlignment = verticalAlignment,
                        content = content,
                    )
                }
            },
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
