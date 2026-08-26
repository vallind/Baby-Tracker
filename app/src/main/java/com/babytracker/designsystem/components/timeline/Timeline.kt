package com.babytracker.designsystem.components.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.components.timeline.TimelineDefaults as AppTimelineDefaults
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 时间线条目数据模型。
 *
 * @param time 已格式化的时间文案（由调用方格式化，组件不做日期运算）
 * @param highlighted 高亮节点（当前/最新一条），圆点放大并转主色
 */
@Immutable
data class AppTimelineItem(
    val title: String,
    val time: String,
    val supportingText: String? = null,
    val highlighted: Boolean = false,
)

/**
 * 时间线 —— P1 组件（timeline/home 记录流形态）。
 *
 * Composition 轴：条目数据声明式传入；节点列（线+点）与内容列结构固定，
 *   首行无上段线、末行无下段线；高亮节点的放大档走 TimelineTokens.activeDotSize。
 * 颜色全部令牌化：line/dot/activeDot 三色 + 文字走 AppColors。
 *
 * 用法：
 *   AppTimeline(
 *       items = records.map {
 *           AppTimelineItem(title = it.label, time = it.timeText, highlighted = it.isLatest)
 *       },
 *   )
 */
@Composable
fun AppTimeline(
    items: List<AppTimelineItem>,
    modifier: Modifier = Modifier,
    onItemClick: ((Int) -> Unit)? = null,
) {
    Column(modifier) {
        items.forEachIndexed { index, item ->
            TimelineRow(
                item = item,
                isFirst = index == 0,
                isLast = index == items.lastIndex,
                clickable = onItemClick != null,
                onClick = onItemClick?.let { cb -> { cb(index) } },
            )
        }
    }
}

@Composable
private fun TimelineRow(
    item: AppTimelineItem,
    isFirst: Boolean,
    isLast: Boolean,
    clickable: Boolean,
    onClick: (() -> Unit)?,
) {
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    val maxDot = AppTimelineDefaults.activeDotSize()

    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .then(if (clickable) Modifier.clickable(role = Role.Button, onClick = onClick!!) else Modifier),
    ) {
        // ── 节点列：上段线 + 圆点 + 下段线 ──
        Column(
            modifier = Modifier
                .width(maxDot)
                .fillMaxHeight()
                .padding(horizontal = LocalAppSpacing.current.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Segment(visible = !isFirst, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(if (item.highlighted) maxDot else AppTimelineDefaults.dotSize())
                    .clip(CircleShape)
                    .background(
                        when {
                            item.highlighted -> AppTimelineDefaults.activeDotColor()
                            else -> colors.surfaceMuted
                        }
                    )
                    .border(
                        width = AppTimelineDefaults.lineWidth(),
                        color = if (item.highlighted) {
                            AppTimelineDefaults.activeDotColor()
                        } else {
                            AppTimelineDefaults.dotColor()
                        },
                        shape = CircleShape,
                    ),
            )
            Segment(visible = !isLast, modifier = Modifier.weight(1f))
        }

        // ── 内容列：标题 / 时间 / 补充 ──
        Column(
            modifier = Modifier
                .padding(start = LocalAppSpacing.current.md)
                .padding(vertical = LocalAppSpacing.current.sm),
        ) {
            Text(
                item.title,
                style = typography.bodyMedium,
                fontWeight = if (item.highlighted) FontWeight.SemiBold else FontWeight.Normal,
                color = if (item.highlighted) colors.textPrimary else colors.textSecondary,
                maxLines = 1,
            )
            Text(
                item.time,
                style = typography.labelMedium,
                color = colors.textSecondary,
            )
            if (item.supportingText != null) {
                Text(
                    item.supportingText,
                    style = typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = LocalAppSpacing.current.xs),
                )
            }
        }
    }
}

/** 竖向连接线段：不可见时占位不绘制（保持圆点垂直居中） */
@Composable
private fun Segment(visible: Boolean, modifier: Modifier) {
    if (visible) {
        Box(
            modifier
                .width(AppTimelineDefaults.lineWidth())
                .background(AppTimelineDefaults.lineColor())
        )
    } else {
        Box(modifier.width(AppTimelineDefaults.lineWidth()))
    }
}
