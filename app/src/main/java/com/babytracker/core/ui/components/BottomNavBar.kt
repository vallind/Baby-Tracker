package com.babytracker.core.ui.components

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.babytracker.core.data.repository.MessageRepository
import com.babytracker.core.ui.BlurPolicy
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.navigation.Navigator
import com.babytracker.navigation.Route
import io.elyon.kmp.basic.Badge
import io.elyon.kmp.basic.NavigationBar
import io.elyon.kmp.basic.NavigationBarItem
import io.elyon.kmp.basic.Text
import io.elyon.kmp.blur.BlendColorEntry
import io.elyon.kmp.blur.BlurDefaults
import io.elyon.kmp.blur.isRuntimeShaderSupported
import io.elyon.kmp.blur.textureBlur
import io.elyon.kmp.theme.ElyonTheme
import org.koin.compose.koinInject

/**
 * Elyon 底部导航栏（5 Tab），支持毛玻璃：
 * - blur 启用条件：API 33+ 且运行时着色器可用（BlurPolicy），且有 Scaffold backdrop；
 * - 启用时导航栏背景透明 + textureBlur，内容层由 AppScaffold 的 layerBackdrop 捕获；
 * - 不支持时回退为 Elyon surface 纯色，保证可读性。
 */
@Composable
fun BottomNavBar(
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val messageRepo: MessageRepository = koinInject()
    val unreadCount by messageRepo.watchUnreadCount().collectAsState(initial = 0)
    val currentRoute = navigator.current()
    val backdrop = LocalScaffoldBackdrop.current
    val blurEnabled = BlurPolicy.isBlurSupported(
        runtimeSdk = Build.VERSION.SDK_INT,
        shaderSupported = isRuntimeShaderSupported(),
    ) && backdrop != null
    val blurColors = BlurDefaults.blurColors(
        blendColors = listOf(
            BlendColorEntry(color = ElyonTheme.colorScheme.surface.copy(alpha = 0.78f)),
        ),
    )

    val tabs = listOf(
        BottomTab(AppStrings.home, Icons.Outlined.Home, Route.Home, badgeCount = 0),
        BottomTab(AppStrings.records, Icons.AutoMirrored.Outlined.List, Route.Timeline, badgeCount = 0),
        BottomTab(AppStrings.stats, Icons.Outlined.BarChart, Route.Stats, badgeCount = 0),
        BottomTab(AppStrings.messages, Icons.AutoMirrored.Outlined.Message, Route.Message, badgeCount = unreadCount),
        BottomTab(AppStrings.profile, Icons.Outlined.Person, Route.Settings, badgeCount = 0),
    )

    NavigationBar(
        modifier = modifier.then(
            if (blurEnabled) {
                Modifier.textureBlur(
                    backdrop = backdrop,
                    shape = RectangleShape,
                    blurRadius = 24f,
                    colors = blurColors,
                )
            } else {
                Modifier
            },
        ),
        color = if (blurEnabled) Color.Transparent else ElyonTheme.colorScheme.surface,
        showDivider = false,
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { navigator.switchTab(tab.route) },
                icon = tab.icon,
                label = tab.label,
                badge = if (tab.badgeCount > 0) {
                    {
                        Badge {
                            Text(
                                if (tab.badgeCount > 99) "99+" else tab.badgeCount.toString(),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                } else {
                    null
                },
            )
        }
    }
}

private data class BottomTab(
    val label: String,
    val icon: ImageVector,
    val route: Route,
    val badgeCount: Int,
)
