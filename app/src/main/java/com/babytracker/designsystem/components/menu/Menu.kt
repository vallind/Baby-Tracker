package com.babytracker.designsystem.components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.babytracker.designsystem.components.menu.MenuDefaults as AppMenuDefaults
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 菜单项数据模型 —— key 泛型承载调用方的标识类型（与 AppOptionChipRow 的 key→label 同风格）。
 *
 * destructive 为语义标记：文字与图标转 danger 色，交互仍可点（删除类动作需二次确认由调用方负责）。
 */
@Immutable
data class AppMenuItem<T>(
    val key: T,
    val label: String,
    val leadingIcon: ImageVector? = null,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
)

/**
 * 通用菜单 —— 消费 AppComponentTokens.menu（P0 五件套之一，解锁 SplitButton/ContextMenu 等）。
 *
 * 定位说明：Dropdown / ContextMenu / Popup 共用本组件（收敛蓝图：placement 不是独立轴，
 * 锚定方式由调用方的 Modifier 决定——普通点击锚定即下拉菜单，onLongClick 锚定即上下文菜单）。
 * 浮层容器色走主题桥接的 M3 surface；tonal 层次读 MenuTokens.elevation。
 *
 * 用法：
 *   var expanded by remember { mutableStateOf(false) }
 *   Box(Modifier.clickable { expanded = true }) {
 *       Text("操作")
 *       AppMenu(
 *           expanded = expanded,
 *           items = listOf(
 *               AppMenuItem("edit", "编辑", Icons.Default.Edit),
 *               AppMenuItem("delete", "删除", destructive = true),
 *           ),
 *           onItemClick = { key -> when (key) { "edit" -> ...; "delete" -> ... } },
 *           onDismiss = { expanded = false },
 *       )
 *   }
 *
 * @param selectedKey 高亮项 key（当前生效值），背景读 MenuTokens.selectedBgColor
 */
@Composable
fun <T> AppMenu(
    expanded: Boolean,
    items: List<AppMenuItem<T>>,
    onItemClick: (T) -> Unit,
    onDismiss: () -> Unit,
    selectedKey: T? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = AppMenuDefaults.cornerRadius(),
        tonalElevation = AppMenuDefaults.elevation(),
        modifier = modifier.defaultMinSize(minWidth = AppMenuDefaults.minWidth()),
    ) {
        items.forEach { item ->
            val selected = item.key == selectedKey
            val contentColor = when {
                !item.enabled -> colors.textDisabled
                item.destructive -> colors.danger
                else -> colors.textPrimary
            }
            DropdownMenuItem(
                text = {
                    Text(
                        item.label,
                        style = typography.bodyMedium,
                        color = contentColor,
                    )
                },
                leadingIcon = item.leadingIcon?.let { icon ->
                    {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = contentColor,
                        )
                    }
                },
                enabled = item.enabled,
                onClick = {
                    onDismiss()
                    onItemClick(item.key)
                },
                modifier = Modifier
                    .height(AppMenuDefaults.itemHeight())
                    .then(
                        if (selected) {
                            Modifier.background(AppMenuDefaults.selectedBgColor())
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = AppMenuDefaults.horizontalPadding()),
            )
        }
    }
}
