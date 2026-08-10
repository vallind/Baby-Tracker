package com.babytracker.core.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import io.elyon.kmp.blur.LayerBackdrop

/**
 * AppScaffold 捕获的页面 backdrop，供底部导航毛玻璃读取。
 * 非 AppScaffold 场景（无 backdrop）时为 null，BottomNavBar 自动回退纯色。
 */
val LocalScaffoldBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }
