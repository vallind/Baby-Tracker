package com.babytracker.core.ui

/**
 * 毛玻璃启用策略。
 *
 * elyon-blur 的 Android 实现要求 API 33+（RenderEffect）；
 * textureBlur 还需要运行时着色器支持，不支持时必须回退为纯色，避免页面不可见。
 */
object BlurPolicy {
    const val MIN_BLUR_SDK = 33

    fun isBlurSupported(runtimeSdk: Int, shaderSupported: Boolean): Boolean =
        runtimeSdk >= MIN_BLUR_SDK && shaderSupported
}
