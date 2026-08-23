package com.babytracker.feature.common

import androidx.compose.ui.graphics.Color
import com.babytracker.core.domain.model.DiaperType
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.designsystem.theme.AppColors

/**
 * 记录类型视觉映射（2.1 收敛）— 全站唯一一份「类型 → emoji + 分区色」映射。
 *
 * 分区色纪律（docs/design-system.md）：
 *   喂养：母乳=珊瑚 danger / 配方=珊瑚深档 shade600 / 辅食=琥珀 / 饮水=青
 *   尿布：小便=青 tertiary / 大便=琥珀 warning / 混合=双色徽章（各页通过 twoTone 组合）
 *
 * 曾有三份各自实现（HomeScreen/FeedingListScreen/TimelineScreen），此处收敛，
 * 避免一处配色调整需要改多处。
 */
fun feedingTone(type: FeedingType, c: AppColors): Pair<String, Color> = when (type) {
    FeedingType.BREAST -> "🤱" to c.danger                 // 母乳=珊瑚
    FeedingType.FORMULA -> "🍼" to c.dangerScale.shade600  // 配方=珊瑚深档（同系，不用蓝）
    FeedingType.FOOD -> "🥣" to c.warning                  // 辅食=琥珀
    FeedingType.WATER -> "🥤" to c.tertiary                // 饮水=青
}

fun diaperTone(type: DiaperType, c: AppColors): Pair<String, Color> = when (type) {
    DiaperType.WET -> "💧" to c.tertiary
    DiaperType.POOP -> "💩" to c.warning
    DiaperType.BOTH -> "🔄" to c.tertiary
}
