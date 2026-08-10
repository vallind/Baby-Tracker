package com.babytracker.designsystem.components.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.CircularProgressIndicator
import io.elyon.kmp.basic.LinearProgressIndicator
import io.elyon.kmp.basic.ProgressIndicatorDefaults
import io.elyon.kmp.theme.ElyonTheme

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
    height: Dp = 6.dp,
    trackColor: Color = ElyonTheme.colorScheme.secondaryContainer,
    indicatorColor: Color = ElyonTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress ?: 0f,
        animationSpec = tween(durationMillis = 300),
    )

    LinearProgressIndicator(
        modifier = modifier,
        progress = if (progress == null) null else animatedProgress,
        height = height,
        colors = ProgressIndicatorDefaults.progressIndicatorColors(
            foregroundColor = indicatorColor,
            backgroundColor = trackColor,
        ),
    )
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
    size: Dp = 30.dp,
    strokeWidth: Dp = 4.dp,
    trackColor: Color = ElyonTheme.colorScheme.secondaryContainer,
    indicatorColor: Color = ElyonTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress ?: 0f,
        animationSpec = tween(durationMillis = 300),
    )

    CircularProgressIndicator(
        modifier = modifier,
        progress = if (progress == null) null else animatedProgress,
        colors = ProgressIndicatorDefaults.progressIndicatorColors(
            foregroundColor = indicatorColor,
            backgroundColor = trackColor,
        ),
        strokeWidth = strokeWidth,
        size = size,
    )
}
