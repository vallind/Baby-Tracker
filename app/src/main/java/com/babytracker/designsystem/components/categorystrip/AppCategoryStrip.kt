package com.babytracker.designsystem.components.categorystrip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.card.CardColors
import com.babytracker.designsystem.theme.CategoryStripAccentColors
import com.babytracker.designsystem.theme.LocalAppSpacing

/** 分类条的单个 tab 规格：[key] 为稳定标识（选中轴/回调值），业务枚举由调用方映射为 String */
data class AppCategoryTab(
    val key: String,
    val emoji: String,
    val label: String,
    val badgeCount: Int,
)

/** 角标计数封顶显示约定（数值格式，非文案） */
private const val BADGE_MAX_COUNT = 99
private const val BADGE_OVERFLOW_LABEL = "99+"

/**
 * 分类统计条 — emoji+标签+未读角标的分类卡横条（自 feature/message 的私有
 * CategoryOverviewBar 收编，G4 杂项二批）。
 *
 * 结构：整行横向等分（每卡 weight(1f)）的可点卡片；选中卡切换为该分类强调组实底，
 * 标签加粗并改用强调前景；未读数 > 0 时尾部显示圆形角标（未选中=危险红实底，
 * 选中=强调前景半透明底），计数超 [BADGE_MAX_COUNT] 封顶显示。
 *
 * 颜色轴：默认强调组走令牌 accent（品牌主色组）；个别分类需要独立强调色时，
 * 调用方经 [tabAccents] 按 key 覆盖（如服务通知用次要色组）。
 *
 * 用法：
 *   AppCategoryStrip(
 *       tabs = listOf(AppCategoryTab("interaction", "💬", "互动消息", 3), ...),
 *       selectedKey = selectedType,
 *       onSelect = { key -> ... },
 *   )
 */
@Composable
fun AppCategoryStrip(
    tabs: List<AppCategoryTab>,
    selectedKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    tabAccents: Map<String, CategoryStripAccentColors> = emptyMap(), // 按 key 覆盖逐分类强调组
) {
    val tokens = AppCategoryStripDefaults.tokens()
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = LocalAppSpacing.current.md, vertical = tokens.rowVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(tokens.cardSpacing),
    ) {
        tabs.forEach { tab ->
            val selected = tab.key == selectedKey
            val accent = tabAccents[tab.key] ?: tokens.accent
            AppCard(
                modifier = Modifier.weight(1f),
                onClick = { onSelect(tab.key) },
                colors = CardColors(
                    containerColor = if (selected) accent.container else Color.Unspecified,
                ),
            ) {
                Row(
                    Modifier
                        .padding(
                            horizontal = tokens.innerHorizontalPadding,
                            vertical = tokens.innerVerticalPadding,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(tab.emoji, style = tokens.emojiStyle)
                        Spacer(Modifier.height(tokens.emojiLabelGap))
                        Text(
                            tab.label,
                            style = tokens.labelTextStyle,
                            fontWeight = if (selected) tokens.selectedLabelFontWeight else tokens.labelFontWeight,
                            color = if (selected) accent.content else tokens.labelColor,
                        )
                    }
                    if (tab.badgeCount > 0) {
                        Box(
                            Modifier
                                .size(tokens.badgeSize)
                                .clip(CircleShape)
                                .background(
                                    if (selected) {
                                        accent.content.copy(alpha = tokens.selectedBadgeContainerAlpha)
                                    } else {
                                        tokens.badgeContainerColor
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                if (tab.badgeCount > BADGE_MAX_COUNT) BADGE_OVERFLOW_LABEL else tab.badgeCount.toString(),
                                style = tokens.badgeTextStyle,
                                fontWeight = tokens.badgeFontWeight,
                                color = if (selected) accent.content else tokens.badgeContentColor,
                            )
                        }
                    }
                }
            }
        }
    }
}
