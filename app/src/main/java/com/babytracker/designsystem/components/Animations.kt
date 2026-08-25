package com.babytracker.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.babytracker.designsystem.theme.LocalAppMotion
import kotlinx.coroutines.delay

/**
 * 列表项错峰入场动画 — 淡入 + 上滑
 *
 * 用法：
 *   items.forEachIndexed { i, item ->
 *       AnimatedListItem(index = i) {
 *           MyItemRow(item)
 *       }
 *   }
 */
@Composable
fun AnimatedListItem(
    index: Int,
    content: @Composable () -> Unit,
) {
    val motion = LocalAppMotion.current
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 25L)
        visible.value = true
    }
    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(animationSpec = tween(motion.duration.medium, easing = motion.easing.standard)) +
            slideInVertically(
                animationSpec = tween(motion.duration.medium, easing = motion.easing.standard),
                initialOffsetY = { it / 4 },
            ),
    ) {
        content()
    }
}

/**
 * 数字滚动动画
 *
 * 用法：
 *   val animated by animateNumber(target = feedCount)
 *   Text(animated.toString())
 */
@Composable
fun animateNumber(target: Int): Int {
    val motion = LocalAppMotion.current
    val animated by animateIntAsState(
        targetValue = target,
        animationSpec = tween(motion.duration.long, easing = motion.easing.standard),
        label = "number",
    )
    return animated
}
