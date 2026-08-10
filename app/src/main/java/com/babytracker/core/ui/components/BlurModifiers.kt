package com.babytracker.core.ui.components

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import com.babytracker.core.ui.BlurPolicy
import io.elyon.kmp.blur.BlendColorEntry
import io.elyon.kmp.blur.BlurDefaults
import io.elyon.kmp.blur.LayerBackdrop
import io.elyon.kmp.blur.isRuntimeShaderSupported
import io.elyon.kmp.blur.textureBlur
import io.elyon.kmp.theme.ElyonTheme

/**
 * 毛玻璃统一修饰符：API 33+ 且存在 Scaffold backdrop 时叠加 textureBlur，
 * 组件自身背景应传 Color.Transparent；不支持时原样返回，保证可读性。
 */
@Composable
fun Modifier.appBlur(
    backdrop: LayerBackdrop? = LocalScaffoldBackdrop.current,
    radius: Float = 32f,
    tintAlpha: Float = 0.6f,
): Modifier {
    val enabled = BlurPolicy.isBlurSupported(
        runtimeSdk = Build.VERSION.SDK_INT,
        shaderSupported = isRuntimeShaderSupported(),
    ) && backdrop != null
    return if (enabled) {
        then(
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = RectangleShape,
                blurRadius = radius,
                colors = BlurDefaults.blurColors(
                    blendColors = listOf(
                        BlendColorEntry(color = ElyonTheme.colorScheme.surface.copy(alpha = tintAlpha)),
                    ),
                ),
            ),
        )
    } else {
        this
    }
}
