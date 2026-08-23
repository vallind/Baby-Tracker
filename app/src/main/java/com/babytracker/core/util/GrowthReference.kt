package com.babytracker.core.util

import com.babytracker.core.domain.model.GrowthType
import java.util.Locale

/**
 * 生长参考区间（近似 WHO 标准）— 按整月龄锚点线性插值。
 *
 * 锚点取自 WHO 男孩中位数的常用近似值（0/6/12/18/24/36 月）；
 * 区间 = 中位值 ± 自然波动带（身高 6% / 体重 12% / 头围 4%）。
 * 用途：生长页「x月龄参考 y-y cm」副行与超出提示；仅作参考，不替代医嘱。
 *
 * 2.1 起取代写死的 normalRanges（原固定在 12 月龄附近一组值，不随宝宝月龄变化）。
 */
object GrowthReference {
    data class Range(val min: Double, val max: Double)

    // （月龄, 中位值）锚点
    private val heightAnchors = listOf(0 to 50.0, 6 to 67.0, 12 to 76.0, 18 to 82.0, 24 to 87.0, 36 to 96.0)
    private val weightAnchors = listOf(0 to 3.3, 6 to 8.0, 12 to 9.9, 18 to 11.3, 24 to 12.5, 36 to 14.8)
    private val headAnchors = listOf(0 to 34.5, 6 to 43.5, 12 to 46.5, 18 to 47.8, 24 to 48.5, 36 to 49.5)

    /** 指定月龄的参考区间；月龄越界（负数或超过锚点范围）返回 null */
    fun range(ageMonths: Int, type: GrowthType): Range? {
        val m = median(ageMonths, anchors(type)) ?: return null
        val band = when (type) {
            GrowthType.HEIGHT -> 0.06
            GrowthType.WEIGHT -> 0.12
            GrowthType.HEAD -> 0.04
        }
        return Range(m * (1 - band), m * (1 + band))
    }

    /** 参考区间展示文本，如「12月龄参考 71.4-80.6 cm」 */
    fun rangeText(ageMonths: Int, type: GrowthType, unit: String): String {
        val r = range(ageMonths, type) ?: return ""
        return String.format(Locale.US, "%d月龄参考 %.1f-%.1f %s", ageMonths, r.min, r.max, unit)
    }

    private fun anchors(type: GrowthType): List<Pair<Int, Double>> = when (type) {
        GrowthType.HEIGHT -> heightAnchors
        GrowthType.WEIGHT -> weightAnchors
        GrowthType.HEAD -> headAnchors
    }

    private fun median(ageMonths: Int, anchors: List<Pair<Int, Double>>): Double? {
        if (ageMonths < 0) return null
        val a = anchors.lastOrNull { it.first <= ageMonths } ?: return null
        val b = anchors.firstOrNull { it.first >= ageMonths } ?: return a.second
        if (a.first == b.first) return a.second
        val t = (ageMonths - a.first).toDouble() / (b.first - a.first)
        return a.second + (b.second - a.second) * t
    }
}
