package com.babytracker.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.navigation.Screen

@Composable
fun RequireBaby(
    babyId: Long,
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (babyId == 0L) {
        EmptyState(
            emoji = "🍼",
            title = "还没有添加宝宝",
            subtitle = "点击下方按钮，开始记录宝宝成长的每一个瞬间",
            actionText = "添加宝宝",
            onAction = { navController.navigate(Screen.BabyManagement.route) },
            modifier = modifier,
        )
    } else {
        content()
    }
}
