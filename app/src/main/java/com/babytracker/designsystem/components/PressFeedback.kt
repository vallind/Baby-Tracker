package com.babytracker.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

/**
 * 按压缩放反馈 — 按下缩至 0.97、松手弹簧回弹（Linear 风格微反馈）。
 *
 * 不消费指针事件，与 ripple 水波共存。用法：
 *   Modifier.pressScale().clickable { ... }
 */
@Composable
fun Modifier.pressScale(
    pressedScale: Float = 0.97f,
    pressDurationMillis: Int = 80,
    releaseSpring: SpringSpec<Float> = spring(dampingRatio = 0.7f, stiffness = 500f),
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    this
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown()
                scope.launch { scale.animateTo(pressedScale, tween(pressDurationMillis)) }
                waitForUpOrCancellation()
                scope.launch { scale.animateTo(1f, releaseSpring) }
            }
        }
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
}
