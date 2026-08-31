package com.babytracker.ui.patterns.records

import com.babytracker.designsystem.i18n.AppStrings
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 日期导航标签（「今天 · 8月23日」式）— 纯函数，非 Composable。
 *
 * 去重背景：尿布 / 喂养 / 睡眠 / 生长四个记录页此前各维护一份逐字符相同的
 * remember 内联实现（M月d日 格式化 + 今天/昨天/明天 前缀，三分支完整一致），
 * 本函数将四份复制粘贴收敛为单一实现，产出文本直接喂给 [DateNavCapsule] 的
 * dateLabel。文案走既有 AppStrings key（today / yesterday / tomorrow），无新增字符串。
 *
 * 来源：feature/diaper/DiaperListScreen、feature/feeding/FeedingListScreen、
 * feature/sleep/SleepListScreen、feature/growth/GrowthScreen 的原私有实现。
 */
fun appDateNavLabel(date: LocalDate, today: LocalDate): String {
    val md = date.format(DateTimeFormatter.ofPattern("M月d日"))
    return when {
        date == today -> "${AppStrings.today} · $md"
        date == today.minusDays(1) -> "${AppStrings.yesterday} · $md"
        date == today.plusDays(1) -> "${AppStrings.tomorrow} · $md"
        else -> md
    }
}