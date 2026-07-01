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
    val shimmerColors = listOf(
        shimmerColor1,
        shimmerColor2,
        shimmerColor1,
    )
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = AppSkeletonDefaults.shimmerDurationMs(), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value),
    )

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
