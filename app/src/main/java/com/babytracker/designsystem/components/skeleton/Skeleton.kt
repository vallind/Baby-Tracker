package com.babytracker.designsystem.components.skeleton

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.skeleton.SkeletonDefaults as AppSkeletonDefaults

@Composable
fun SkeletonLoader(
    itemCount: Int = 5,
    itemHeight: Int = 72,
    modifier: Modifier = Modifier,
    shimmerColor1: androidx.compose.ui.graphics.Color = AppSkeletonDefaults.shimmerColor1(),
    shimmerColor2: androidx.compose.ui.graphics.Color = AppSkeletonDefaults.shimmerColor2(),
    cornerRadius: Dp = AppSkeletonDefaults.cornerRadius(),
    avatarSize: Dp = AppSkeletonDefaults.avatarSize(),
) {
    val brush = rememberShimmerBrush(shimmerColor1, shimmerColor2)

    Column(
        modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(itemCount) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(itemHeight.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(avatarSize)
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(brush),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.6f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(brush),
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth(0.35f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(brush),
                    )
                }
                Box(
                    Modifier
                        .width(40.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(brush),
                )
            }
        }
    }
}

/** 骨架 shimmer 画刷（无限位移动画），骨架条与骨架列表共用 */
@Composable
private fun rememberShimmerBrush(
    shimmerColor1: Color,
    shimmerColor2: Color,
): Brush {
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = AppSkeletonDefaults.shimmerDurationMs(), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    return Brush.linearGradient(
        colors = listOf(shimmerColor1, shimmerColor2, shimmerColor1),
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value),
    )
}

/**
 * 骨架条原语：shimmer 底 + 圆角的矩形占位。
 * 供 AppCard 加载态等场景复用；宽度由调用方 modifier 控制（如 fillMaxWidth(0.6f)）。
 */
@Composable
fun SkeletonBar(
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    cornerRadius: Dp = AppSkeletonDefaults.cornerRadius(),
) {
    val brush = rememberShimmerBrush(
        shimmerColor1 = AppSkeletonDefaults.shimmerColor1(),
        shimmerColor2 = AppSkeletonDefaults.shimmerColor2(),
    )
    androidx.compose.foundation.layout.Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush),
    )
}
