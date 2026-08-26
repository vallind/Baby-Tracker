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
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 通用底部导航栏（纯 UI 组件，零业务依赖）—— 超级参照组件：Selection / Navigation。
 *
 * Selection 选择轴：选中判定与药丸指示器颜色全部令牌化；M3 NavigationBarItem 自带
 *   "已选中"选中态语义，配合指示器即完整选择表达；
 * Navigation 导航轴：徽章计数（含 99+ 溢出格式走 AppStrings）承载消息等导航提示。
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
    // 悬浮胶囊容器：圆角 + 暖阴影，页面底从胶囊四周透出（几何全部读 BottomBarTokens）
    val pillShape = RoundedCornerShape(BottomBarDefaults.pillRadius())
    val shadowColor = LocalAppColors.current.shadow

    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = BottomBarDefaults.outerPaddingHorizontal(),
                vertical = BottomBarDefaults.outerPaddingVertical(),
            ),
    ) {
        // 外层容器负责胶囊裁切 + 暖阴影；内部为 M3 NavigationBar（无 shape 参数，lessons #21）
        Box(
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = BottomBarDefaults.shadowElevation(),
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
                                BadgedBox(badge = {
                                    Badge {
                                        Text(
                                            if (item.badgeCount > 99) {
                                                AppStrings.badgeOverflowMax
                                            } else {
                                                item.badgeCount.toString()
                                            }
                                        )
                                    }
                                }) {
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