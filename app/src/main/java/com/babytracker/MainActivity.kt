package com.babytracker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.babytracker.core.ui.BabyTrackerElyonTheme
import com.babytracker.core.ui.ElyonThemeResolver
import com.babytracker.core.ui.ThemeController
import com.babytracker.navigation.AppNavigation
import io.elyon.kmp.theme.ElyonTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val themeController: ThemeController by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeName = themeController.currentThemeName

            BabyTrackerElyonTheme(themeName) {
                val darkTheme = ElyonThemeResolver.isDark(themeName)
                val scheme = ElyonTheme.colorScheme
                // 状态栏颜色跟随 primaryContainer（所有页面顶部区域统一使用此颜色）
                val statusBarColor = if (darkTheme) scheme.surface else scheme.primaryContainer

                // 系统级：状态栏图标颜色（浅/深），始终跟随主题
                if (Build.VERSION.SDK_INT >= 21) {
                    SideEffect {
                        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !darkTheme
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // ── 页面内容（底层）──
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavigation()
                    }
                    // ── 状态栏底色块（上层）─────────────────
                    // enableEdgeToEdge 让系统状态栏透明；
                    // 此色块作为 Compose 层的视觉背景，确保所有页面
                    // 状态栏颜色一致跟随主题变化（不依赖 window.statusBarColor）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsTopHeight(WindowInsets.statusBars)
                            .background(statusBarColor),
                    )
                }
            }
        }
    }
}
