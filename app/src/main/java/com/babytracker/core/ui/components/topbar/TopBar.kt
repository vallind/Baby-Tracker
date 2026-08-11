package com.babytracker.core.ui.components.topbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import android.os.Build
import com.babytracker.core.ui.BlurPolicy
import com.babytracker.core.ui.components.LocalScaffoldBackdrop
import com.babytracker.i18n.AppStrings
import io.elyon.kmp.basic.Icon
import io.elyon.kmp.basic.IconButton
import io.elyon.kmp.basic.TopAppBar
import io.elyon.kmp.blur.BlendColorEntry
import io.elyon.kmp.blur.BlurDefaults
import io.elyon.kmp.blur.isRuntimeShaderSupported
import io.elyon.kmp.blur.textureBlur
import io.elyon.kmp.theme.ElyonTheme

/**
 * 统一导航栏组件 — 对标 Palette AppBar 组件，消费 AppComponentTokens.appBar
 *
 * 非毛玻璃时使用 Elyon 默认 surface 背景与 onSurface 标题，
 * 尺寸/字号全部走 Elyon TopAppBar 默认值。
 *
 * 用法：
 *   Scaffold(topBar = { AppTopBar(title = "喂养记录", onBack = { navController.popBackStack() }) }) { ... }
 *   // 不需要返回按钮时
 *   AppTopBar(title = "首页", showBack = false)
 */
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    showBack: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val backdrop = LocalScaffoldBackdrop.current
    val blurEnabled = BlurPolicy.isBlurSupported(
        runtimeSdk = Build.VERSION.SDK_INT,
        shaderSupported = isRuntimeShaderSupported(),
    ) && backdrop != null
    val containerColor = if (blurEnabled) Color.Transparent else ElyonTheme.colorScheme.surface
    val contentColor = ElyonTheme.colorScheme.onSurface
    val blurColors = BlurDefaults.blurColors(
        blendColors = listOf(
            BlendColorEntry(color = ElyonTheme.colorScheme.surface.copy(alpha = 0.65f)),
        ),
    )
    TopAppBar(
        title = title,
        modifier = if (blurEnabled) {
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = RectangleShape,
                blurRadius = 32f,
                colors = blurColors,
            )
        } else {
            Modifier
        },
        color = containerColor,
        titleColor = contentColor,
        navigationIcon = {
            if (showBack && onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = AppStrings.back,
                        tint = contentColor,
                    )
                }
            }
        },
        actions = { actions() },
    )
}
