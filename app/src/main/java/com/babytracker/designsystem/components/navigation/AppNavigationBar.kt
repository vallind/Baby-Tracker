package com.babytracker.designsystem.components.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 通用底部导航栏（纯 UI 组件，零业务依赖）。
 *
 * 只负责视觉与交互：悬浮胶囊形态（左右留白 + 大圆角 + 柔和暖阴影）、
 * 选中项粉彩药丸指示器、徽章展示与无障碍语义（选中态/goBack 可聚焦）。
 *
 * 业务不进入本组件：Tab 数量、图标、标签、徽章数据、选中判定与点击后的
 * 导航行为全部由调用方（App 层 AppBottomBar）提供。
 *
 * 用法：
 *   AppNavigationBar(
 *       items = listOf(AppNavigationItem("首页", Icons.Outlined.Home)),
 *       selectedIndex = 0,
 *       onItemClick = { index -> ... },
 *   )
 */
data class AppNavigationItem(
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
)

@Composable
fun AppNavigationBar(
    items: List<AppNavigationItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 悬浮胶囊容器：圆角 + 暖阴影，页面底从胶囊四周透出
    val pillShape = RoundedCornerShape(28.dp)
    val shadowColor = LocalAppColors.current.shadow

    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        // 外层容器负责胶囊裁切 + 暖阴影；内部为 M3 NavigationBar（无 shape 参数，lessons #21）
        Box(
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = pillShape,
                    ambientColor = shadowColor,
                    spotColor = shadowColor,
                )
                .clip(pillShape),
        ) {
            NavigationBar(
                containerColor = BottomBarDefaults.containerColor(),
                contentColor = BottomBarDefaults.contentColor(),
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            if (item.badgeCount > 0) {
                                BadgedBox(badge = { Badge { Text(if (item.badgeCount > 99) "99+" else item.badgeCount.toString()) } }) {
                                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(BottomBarDefaults.iconSize()))
                                }
                            } else {
                                Icon(item.icon, contentDescription = null, modifier = Modifier.size(BottomBarDefaults.iconSize()))
                            }
                        },
                        label = { Text(item.label, style = LocalAppTypography.current.labelSmall) },
                        selected = index == selectedIndex,
                        onClick = { onItemClick(index) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BottomBarDefaults.selectedColor(),
                            selectedTextColor = BottomBarDefaults.selectedColor(),
                            unselectedIconColor = BottomBarDefaults.unselectedColor(),
                            unselectedTextColor = BottomBarDefaults.unselectedColor(),
                            indicatorColor = BottomBarDefaults.indicatorColor(),
                        ),
                    )
                }
            }
        }
    }
}