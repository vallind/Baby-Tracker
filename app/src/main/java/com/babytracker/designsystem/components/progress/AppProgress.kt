package com.babytracker.designsystem.components.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * 线性进度条 — 对标 Palette Progress，消费 AppComponentTokens.progress。
 *
 * 用法：
 *   AppLinearProgress(progress = 0.6f)  // 60% 进度
 *   AppLinearProgress(progress = null)  // 不确定进度（indeterminate）
 */
@Composable
fun AppLinearProgress(
    progress: Float?,  // 0f ~ 1f，null 表示不确定
    height: Dp = ProgressDefaults.height(),
    trackColor: Color = ProgressDefaults.trackColor(),
    indicatorColor: Color = ProgressDefaults.indicatorColor(),
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress ?: 0f,
        animationSpec = tween(durationMillis = 300),
    )

    if (progress == null) {
        LinearProgressIndicator(
            modifier = modifier
                .clip(RoundedCornerShape(height / 2))
                .then(Modifier.size(
                    width = Dp.Unspecified,
                    height = height,
                )),
            color = indicatorColor,
            trackColor = trackColor,
        )
    } else {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = modifier
                .clip(RoundedCornerShape(height / 2))
                .then(Modifier.size(
                    width = Dp.Unspecified,
                    height = height,
                )),
            color = indicatorColor,
            trackColor = trackColor,
        )
    }
}

/**
 * 圆形进度指示器 — 对标 Palette CircularProgress，消费 AppComponentTokens.progress。
 *
 * 用法：
 *   AppCircularProgress()                     // 不确定
 *   AppCircularProgress(progress = 0.75f)     // 75% 进度
 */
@Composable
fun AppCircularProgress(
    progress: Float? = null,  // 0f ~ 1f，null 表示不确定
    size: Dp = ProgressDefaults.circularSize(),
    strokeWidth: Dp = ProgressDefaults.strokeWidth(),
    trackColor: Color = ProgressDefaults.trackColor(),
    indicatorColor: Color = ProgressDefaults.indicatorColor(),
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress ?: 0f,
        animationSpec = tween(durationMillis = 300),
    )

    if (progress == null) {
        CircularProgressIndicator(
            modifier = modifier.size(size),
            color = indicatorColor,
            strokeWidth = strokeWidth,
            trackColor = trackColor,
        )
    } else {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = modifier.size(size),
            color = indicatorColor,
            strokeWidth = strokeWidth,
            trackColor = trackColor,
        )
    }
}
