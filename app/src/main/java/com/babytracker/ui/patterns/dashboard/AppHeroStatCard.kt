package com.babytracker.ui.patterns.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.babytracker.ui.i18n.AppStringsProduct

/**
 * 渐变统计大卡 — 「渐变底 + 可选 emoji 徽章标题头 + 自由内容槽」的 hero 形态复合组件。
 *
 * 收编自三处同构实现：首页今日概览（无 emoji 头）、尿布日汇总（青渐变 + 🧷 头）、
 * 夜间睡眠卡（紫渐变 + 🌙 头）。渐变 Brush 与内容色由调用方注入（如
 * `Gradients.overviewCard(c)` + `c.onPrimary`），本组件不持有领域语义；
 * 内容槽自由组合大数字文本与 QuickStatPill 行，头部徽章底为 contentColor × iconContainerAlpha。
 *
 * 页边距（horizontal padding 等）留在调用方 modifier，与既有调用点习惯一致。
 *
 * 用法：
 *   AppHeroStatCard(
 *       gradient = Gradients.diaperSummary(c), contentColor = c.onTertiary,
 *       emoji = "🧷", title = AppStringsProduct.diaperToday,
 *   ) {
 *       Text("3 次", style = typography.headlineMedium, fontWeight = FontWeight.Bold, color = c.onTertiary)
 *       Row { QuickStatPill(...) }
 *   }
 */
@Composable
fun AppHeroStatCard(
    modifier: Modifier = Modifier,
    gradient: Brush,
    contentColor: Color,
    emoji: String? = null,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HeroStatCardDefaults.cornerRadius()))
            .background(gradient),
    ) {
        Column(Modifier.padding(HeroStatCardDefaults.innerPadding())) {
            if (!emoji.isNullOrEmpty() || !title.isNullOrEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (emoji != null) {
                        Box(
                            Modifier
                                .size(HeroStatCardDefaults.badgeSize())
                                .clip(RoundedCornerShape(HeroStatCardDefaults.badgeCornerRadius()))
                                .background(contentColor.copy(alpha = HeroStatCardDefaults.iconContainerAlpha())),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(emoji, style = HeroStatCardDefaults.emojiStyle())
                        }
                    }
                    if (title != null) {
                        if (emoji != null) {
                            Spacer(Modifier.width(HeroStatCardDefaults.headerGap()))
                        }
                        Text(
                            title,
                            style = HeroStatCardDefaults.titleStyle(),
                            color = contentColor.copy(alpha = HeroStatCardDefaults.titleAlpha()),
                        )
                    }
                }
                Spacer(Modifier.height(HeroStatCardDefaults.contentGap()))
            }
            content()
        }
    }
}