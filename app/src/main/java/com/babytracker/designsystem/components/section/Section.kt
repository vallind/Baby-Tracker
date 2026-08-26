package com.babytracker.designsystem.components.section

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.section.SectionHeaderDefaults as AppSectionHeaderDefaults
import com.babytracker.designsystem.i18n.AppStrings

/**
 * 列表项密度档位（超级参照组件的 Density 轴）：
 *   Compact  紧凑行（48dp 基准），适合设置分组内的次级条目
 *   Regular  常规行（64dp 基准），适合记录列表主条目
 */
enum class ListItemDensity { Compact, Regular }

/**
 * 分区标题 — 对标 Palette LayoutTokens，页面中的分区标题 + 可选操作链接
 *
 * 用法：
 *   SectionHeader(title = "最近记录", actionText = "查看全部", onAction = { ... })
 */
@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = AppSectionHeaderDefaults.titleSize(), fontWeight = FontWeight.Bold, color = AppSectionHeaderDefaults.titleColor())
        Spacer(Modifier.weight(1f))
        if (actionText != null && onAction != null) {
            Text(
                actionText,
                fontSize = AppSectionHeaderDefaults.subtitleSize(),
                color = AppSectionHeaderDefaults.actionColor(),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
        trailingContent?.invoke()
    }
}

/**
 * 标准列表项 —— 超级参照组件：Slot / Density / Composition。
 *
 * Slot 槽位轴：leading / headline / supporting / trailing 四槽，全部可空可选；
 * Density 密度轴：Compact / Regular 两档，行高与内边距由 ListItemTokens 驱动；
 * Composition 组合语义：可点击行声明 Button 角色，选中态补 stateDescription 朗读"已选中"。
 *
 * 用法：
 *   AppListItem(
 *       leadingContent = { Text("🍼") },
 *       headlineContent = { Text("喂养") },
 *       supportingContent = { Text("今天 5 次") },
 *       trailingContent = { Text("14:30") },
 *       onClick = { ... },
 *   )
 */
@Composable
fun AppListItem(
    headlineContent: @Composable () -> Unit,
    leadingContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    selected: Boolean = false,
    density: ListItemDensity = ListItemDensity.Regular,
    minHeight: Dp = ListItemDefaults.minHeight(density),
    horizontalPadding: Dp = ListItemDefaults.horizontalPadding(),
    dividerAlpha: Float = ListItemDefaults.dividerAlpha(),
    showDivider: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .background(if (selected) ListItemDefaults.selectedContainerColor() else Color.Transparent)
                // D 批状态轴：禁用降透明且不可点（disabledAlpha 与按钮/卡片同语义）
                .alpha(if (enabled) 1f else ListItemDefaults.disabledAlpha())
                .then(
                    if (onClick != null && enabled) {
                        Modifier.clickable(role = Role.Button, onClick = onClick)
                    } else {
                        Modifier
                    }
                )
                .semantics {
                    // Composition 参照：选中态不改变结构，用 stateDescription 补充朗读
                    if (selected) stateDescription = AppStrings.selected
                }
                .padding(
                    horizontal = horizontalPadding,
                    vertical = ListItemDefaults.verticalPadding(),
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(Modifier.width(ListItemDefaults.itemGap()))
            }
            Column(Modifier.weight(1f)) {
                headlineContent()
                if (supportingContent != null) {
                    supportingContent()
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
        if (showDivider) {
            HorizontalDivider(
                color = ListItemDefaults.dividerColor().copy(alpha = dividerAlpha),
                thickness = ListItemDefaults.dividerThickness(),
            )
        }
    }
}
