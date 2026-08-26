package com.babytracker.designsystem.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.card.CardDefaults as AppCardDefaults
import com.babytracker.designsystem.components.skeleton.SkeletonBar
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing

/**
 * 卡片变体：
 *   Filled       平面色差卡（surfaceMuted 底，无阴影无描边）
 *   Elevated     surface + 暖阴影（历史全站默认观感，故为默认变体）
 *   Outlined     surface + 描边
 *   Transparent  透明容器（仅前景色有意义的场景，如嵌入渐变底）
 */
enum class CardVariant { Filled, Elevated, Outlined, Transparent }

/** 尺寸档位：仅控制内容边距（Compact→spacing.sm / Medium→令牌 / Large→spacing.lg），密度缩放自动生效 */
enum class CardSize { Compact, Medium, Large }

/**
 * 统一卡片基座 —— 超级参照组件：Composition / Surface（三轴样板，新组件以此为模板）。
 * 消费 AppComponentTokens.card
 *
 * 三轴模型：
 *   视觉轴：variant × size
 *   结构轴：header / content / footer 可选槽位（纵向排布，槽间距 spacing.sm）
 *   交互轴：onClick / onLongClick / enabled / selected / loading
 *
 * 用法：
 *   AppCard { Text("内容") }                                  // 默认 Elevated，兼容旧用法
 *   AppCard(variant = CardVariant.Outlined, size = CardSize.Compact) { ... }
 *   AppCard(header = { Text("标题") }, footer = { AppButton(...) }) { ... }
 *   AppCard(onClick = { ... }, selected = isSelected) { ... }
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    variant: CardVariant = CardVariant.Elevated,
    size: CardSize = CardSize.Medium,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    selected: Boolean = false,
    loading: Boolean = false,
    colors: CardColors = AppCardDefaults.colors(variant),
    modifier: Modifier = Modifier,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(AppCardDefaults.cornerRadius())
    val appColors = LocalAppColors.current
    // 变体解析值作为回落基线，再逐字段合并调用方覆盖组
    val resolved = AppCardDefaults.colors(variant)
    val containerColor =
        if (colors.containerColor == Color.Unspecified) resolved.containerColor else colors.containerColor
    val contentColor =
        if (colors.contentColor == Color.Unspecified) resolved.contentColor else colors.contentColor
    // 描边优先级：选中高亮 > 调用方覆盖 > Outlined 变体默认；其余变体无边框
    val borderColor: Color = when {
        selected -> AppCardDefaults.selectedBorderColor()
        colors.borderColor != Color.Unspecified -> colors.borderColor
        variant == CardVariant.Outlined -> resolved.borderColor
        else -> Color.Unspecified
    }
    val borderWidth: Dp = when {
        selected -> AppCardDefaults.selectedBorderWidth()
        colors.borderWidth != Dp.Unspecified -> colors.borderWidth
        variant == CardVariant.Outlined -> resolved.borderWidth
        else -> 0.dp
    }
    val elevationDp: Dp =
        if (colors.elevation == Dp.Unspecified) resolved.elevation else colors.elevation

    // 交互态：禁用降透明且不可点；加载中锁交互但保持正常观感
    val baseModifier = when {
        !enabled -> modifier.alpha(AppCardDefaults.disabledAlpha())
        onClick != null || onLongClick != null -> modifier.combinedClickable(
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick,
        )
        else -> modifier
    }
    // 暖棕调阴影（colors.shadow），与奶油底呼应；仅 Elevated 有高度
    val shadowedModifier = if (elevationDp > 0.dp) {
        baseModifier.shadow(
            elevation = elevationDp,
            shape = shape,
            ambientColor = appColors.shadow,
            spotColor = appColors.shadow,
        )
    } else {
        baseModifier
    }

    Card(
        modifier = shadowedModifier.then(
            if (selected) {
                // 选中态语义：不合并子节点（卡片内容丰富），用 stateDescription 补充朗读"已选中"
                Modifier.semantics { stateDescription = AppStrings.selected }
            } else {
                Modifier
            },
        ),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // M3 阴影关闭，统一走暖阴影
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = borderColor.takeIf { borderWidth > 0.dp }?.let { BorderStroke(borderWidth, it) },
    ) {
        Column(
            modifier = Modifier.padding(AppCardDefaults.contentPadding(size)),
            verticalArrangement = Arrangement.spacedBy(LocalAppSpacing.current.sm),
        ) {
            if (loading) {
                // 加载态：标准骨架占位替代全部槽位内容
                SkeletonBar(Modifier.fillMaxWidth(0.55f), height = 14.dp)
                SkeletonBar(Modifier.fillMaxWidth(0.85f))
                SkeletonBar(Modifier.fillMaxWidth(0.4f))
            } else {
                val scope = this
                header?.invoke(scope)
                content(scope)
                footer?.invoke(scope)
            }
        }
    }
}
