package com.babytracker.core.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateUtils {
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val timeSecFmt = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val dateTimeFmt = DateTimeFormatter.ofPattern("MM-dd HH:mm")

    fun formatDate(dt: LocalDateTime) = dt.format(dateFmt)
    fun formatTime(dt: LocalDateTime) = dt.format(timeFmt)
    fun formatTimeSeconds(dt: LocalDateTime) = dt.format(timeSecFmt)
    fun formatDateTime(dt: LocalDateTime) = dt.format(dateTimeFmt)
    fun parseDate(s: String) = LocalDate.parse(s, dateFmt)
    fun parseDateTime(s: String) = LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME)

    /**
     * 安全解析 ISO 日期时间字符串。失败返回 null。
     * 用于 UI 层避免散落 try-catch。
     */
    fun safeParseDateTime(s: String?): LocalDateTime? {
        if (s.isNullOrBlank()) return null
        return try { LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME) } catch (_: DateTimeParseException) { null } catch (_: Exception) { null }
    }

    fun relativeDate(dateStr: String): String {
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        return when (dateStr) {
            today -> "今天"
            yesterday -> "昨天"
            else -> {
                val date = try { LocalDate.parse(dateStr, dateFmt) } catch (_: Exception) { return dateStr }
                date.format(DateTimeFormatter.ofPattern("M月d日"))
            }
        }
    }

    /**
     * 安全解析日期字符串（yyyy-MM-dd 或 ISO_DATE_TIME）。失败返回 null。
     */
    fun safeParseDate(s: String?): LocalDate? {
        if (s.isNullOrBlank()) return null
        return try {
            if (s.length >= 10) LocalDate.parse(s.take(10), dateFmt) else null
        } catch (_: DateTimeParseException) { null } catch (_: Exception) { null }
    }

    fun monthAge(birthDate: LocalDate): String {
        val now = LocalDate.now()
        val p = Period.between(birthDate, now)
        val months = p.years * 12 + p.months
        return if (months < 0) "0个月"
        else if (months >= 12) "${months / 12}岁${months % 12}个月"
        else "${months}个月"
    }

    fun durationText(minutes: Int): String {
        if (minutes < 60) return "${minutes}分钟"
        val h = minutes / 60; val m = minutes % 60
        return if (m > 0) "${h}h${m}min" else "${h}h"
    }

    fun durationFullText(seconds: Long): String {
        if (seconds < 60) return "${seconds}秒"
        if (seconds < 3600) {
            val m = seconds / 60; val s = seconds % 60
            return if (s > 0) "${m}分${s}秒" else "${m}分钟"
        }
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return buildString {
            append("${h}h")
            if (m > 0) append("${m}min")
            if (s > 0) append("${s}s")
        }
    }

    fun durationToTotalSeconds(start: LocalDateTime, end: LocalDateTime): Long =
        Duration.between(start, end).seconds

    fun feedingTypeLabel(type: String) = when (type) {
        "breast" -> "母乳"; "formula" -> "配方奶"; "food" -> "辅食"; "water" -> "饮水"; else -> type
    }

    fun growthTypeLabel(type: String) = when (type) {
        "weight" -> "体重"; "height" -> "身高"; "head" -> "头围"; else -> type
    }

    fun diaperTypeLabel(type: String) = when (type) {
        "wet" -> "小便"; "poop" -> "大便"; "both" -> "大小便"; else -> type
    }
}
