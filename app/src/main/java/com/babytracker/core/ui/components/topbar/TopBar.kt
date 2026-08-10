package com.babytracker.core.ui.components.topbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * 自动应用主题色（primaryContainer 背景），带返回箭头 + 标题。
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
    height: Dp = 56.dp,
    // elyon TopAppBar 使用自身字号体系，这两个参数仅保留签名兼容
    titleSize: TextUnit = 18.sp,
    titleWeight: FontWeight = FontWeight.SemiBold,
    backIconSize: Dp = 22.dp,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val backdrop = LocalScaffoldBackdrop.current
    val blurEnabled = BlurPolicy.isBlurSupported(
        runtimeSdk = Build.VERSION.SDK_INT,
        shaderSupported = isRuntimeShaderSupported(),
    ) && backdrop != null
    val containerColor = if (blurEnabled) Color.Transparent else ElyonTheme.colorScheme.primaryContainer
    val contentColor = ElyonTheme.colorScheme.onPrimaryContainer
    val blurColors = BlurDefaults.blurColors(
        blendColors = listOf(
            BlendColorEntry(color = ElyonTheme.colorScheme.surface.copy(alpha = 0.65f)),
        ),
    )
    TopAppBar(
        title = title,
        modifier = Modifier
            .height(height)
            .then(
                if (blurEnabled) {
                    Modifier.textureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
                        blurRadius = 32f,
                        colors = blurColors,
                    )
                } else {
                    Modifier
                },
            ),
        color = containerColor,
        titleColor = contentColor,
        navigationIcon = {
            if (showBack && onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = AppStrings.back,
                        modifier = Modifier.size(backIconSize),
                        tint = contentColor,
                    )
                }
            }
        },
        actions = { actions() },
    )
}
