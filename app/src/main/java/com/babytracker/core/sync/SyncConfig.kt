package com.babytracker.core.sync

import java.util.concurrent.TimeUnit
import kotlinx.serialization.Serializable

@Serializable
data class SyncConfig(
    val autoSync: Boolean = true,
    val syncDelay: SyncDelay = SyncDelay.SECONDS_2,
    val bgInterval: BgInterval = BgInterval.OFF,
    val wifiOnly: Boolean = false,
    val syncOnExit: Boolean = false,
)

@Serializable
enum class SyncDelay(val label: String, val millis: Long) {
    IMMEDIATE("立即同步", 0L),
    SECONDS_2("2 秒", 2000L),
    SECONDS_5("5 秒", 5000L),
    SECONDS_10("10 秒", 10000L),
    SECONDS_30("30 秒", 30000L),
}

@Serializable
enum class BgInterval(val label: String, val periodMillis: Long) {
    OFF("关闭", 0L),
    MINUTES_30("每 30 分钟", TimeUnit.MINUTES.toMillis(30)),
    HOURS_1("每 1 小时", TimeUnit.HOURS.toMillis(1)),
    HOURS_2("每 2 小时", TimeUnit.HOURS.toMillis(2)),
}
