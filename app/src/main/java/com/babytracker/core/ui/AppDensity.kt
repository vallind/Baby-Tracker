package com.babytracker.core.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 界面密度三档（应用设置，非设计系统令牌）。
 */
enum class AppDensity(val key: String, val label: String) {
    Compact("compact", "紧凑"),
    Comfortable("comfortable", "舒适"),
    Large("large", "宽松"),
    ;

    companion object {
        fun fromKey(key: String): AppDensity =
            entries.firstOrNull { it.key == key } ?: Comfortable
    }
}

/** 密度档令牌：间距缩放系数 + 控件高度调整量 */
@Immutable
data class AppDensityTokens(
    val spacingScale: Float,
    val controlHeightDelta: Dp,
)

val AppDensity.tokens: AppDensityTokens
    get() = when (this) {
        AppDensity.Compact -> AppDensityTokens(spacingScale = 0.85f, controlHeightDelta = (-8).dp)
        AppDensity.Comfortable -> AppDensityTokens(spacingScale = 1.0f, controlHeightDelta = 0.dp)
        AppDensity.Large -> AppDensityTokens(spacingScale = 1.15f, controlHeightDelta = 8.dp)
    }
