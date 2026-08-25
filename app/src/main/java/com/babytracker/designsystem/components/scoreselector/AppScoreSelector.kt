package com.babytracker.designsystem.components.scoreselector

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.ScoreSelectorOptionColors
import com.babytracker.designsystem.theme.accentContent
import com.babytracker.designsystem.theme.tintContainer

/**
 * 评分选择器 — 图标+标题头行与横向等宽分档选择条的组合（自 feature/development 的私有
 * ScoreSelector 收编，G4 杂项二批）。
 *
 * 结构：头行（圆角色块图标 + 标题/可选副标题）+ 选项行（每档 weight(1f) 等宽胶囊，
 * 点击即选中，无持久多选语义）。
 *
 * 颜色轴：
 *   [useAccent] 切换头部图标块与选项缺省强调组（警示琥珀组 vs 品牌主色组）；
 *   [optionColors] 允许调用方按 key 为个别档位指定独立颜色对（如「落后=琥珀、正常=绿」
 *   的逐档配色），未覆盖的档位回落 useAccent 轴。选中档为实底容器 + 成对前景；
 *   未选中档以该档容器色为种子派生浅底档，前景取令牌 unselectedContent。
 *
 * 业务枚举不进本组件 API：调用方将取值域映射为 [options] 的 key:String（分数等数值
 * 同样字符串化），选中与回调均走 key。
 *
 * 用法：
 *   AppScoreSelector(
 *       title = "大运动", icon = Icons...,
 *       options = listOf("0" to "未观察", "1" to "落后", "2" to "正常", "3" to "超前"),
 *       selectedKey = "2",
 *       onSelect = { key -> ... },
 *   )
 */
@Composable
fun AppScoreSelector(
    title: String,
    icon: ImageVector,
    options: List<Pair<String, String>>,   // value(String 化的分数或 key) to label
    selectedKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    useAccent: Boolean = false,
    subtitle: String? = null,              // 头行副标题（随选中变化的说明文案由调用方组装）
    optionColors: Map<String, ScoreSelectorOptionColors> = emptyMap(), // 按 key 覆盖逐档配色
) {
    val tokens = AppScoreSelectorDefaults.tokens()
    val c = LocalAppColors.current
    // 头部图标块种子色：useAccent 轴取警示组，默认取品牌组（容器浅底 / 前景强调成对派生）
    val tileScale = AppColorScale.fromSeed(
        if (useAccent) tokens.accent.selectedContainer else tokens.neutral.selectedContainer,
    )
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(tokens.iconTileSize)
                    .clip(RoundedCornerShape(tokens.iconTileCornerRadius))
                    .background(tileScale.tintContainer(c)),
                contentAlignment = Alignment.Center,
            ) {
                // 图标取强调前景档，与浅底形成层次
                Icon(
                    icon,
                    contentDescription = title,
                    tint = tileScale.accentContent(c),
                    modifier = Modifier.size(tokens.iconSize),
                )
            }
            Spacer(Modifier.width(tokens.headerGap))
            Column {
                Text(title, style = tokens.titleStyle, color = tokens.titleColor)
                if (subtitle != null) {
                    Text(subtitle, style = tokens.subtitleStyle, color = tokens.subtitleColor)
                }
            }
        }
        Spacer(Modifier.height(tokens.optionsTopGap))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.optionGap)) {
            options.forEach { (value, label) ->
                val isSelected = value == selectedKey
                val colors = optionColors[value]
                    ?: if (useAccent) tokens.accent else tokens.neutral
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(tokens.optionCornerRadius))
                        // 选中=整档实底；未选中=该档容器色作种子的浅底档（替代 alpha 叠加）
                        .background(
                            if (isSelected) {
                                colors.selectedContainer
                            } else {
                                AppColorScale.fromSeed(colors.selectedContainer).tintContainer(c)
                            },
                        )
                        .clickable { onSelect(value) }
                        .padding(vertical = tokens.optionVerticalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = tokens.optionTextStyle,
                        fontWeight = if (isSelected) tokens.selectedOptionFontWeight else tokens.optionFontWeight,
                        color = if (isSelected) colors.selectedContent else tokens.unselectedContent,
                    )
                }
            }
        }
    }
}
