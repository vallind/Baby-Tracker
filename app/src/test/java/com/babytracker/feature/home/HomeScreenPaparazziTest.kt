package com.babytracker.feature.home

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.domain.model.DiaperType
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import com.babytracker.designsystem.theme.BabyTrackerTheme
import org.junit.Rule
import org.junit.Test

/**
 * HomeScreen Paparazzi 截图测试（Batch 2 可测试性样板）。
 *
 * 拆分 Route/Screen 后的直接收益：Screen 纯 UI 化，测试无需 NavHost / Koin / Room / Supabase，
 * 直接喂 fake state 渲染 —— 这正是评审「Screen 可测试性」验收点。
 */
class HomeScreenPaparazziTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun `首页有数据渲染`() {
        paparazzi.snapshot(name = "home_light") {
            BabyTrackerTheme {
                HomeScreen(
                    state = HomeUiState(
                        feedCount = 3,
                        breastFeedCount = 1,
                        formulaCount = 2,
                        formulaTotalMl = 120,
                        sleepHours = "5小时30分",
                        diaperCount = 4,
                        recentItems = listOf<Any>(
                            Feeding(id = 1, babyId = 1, type = FeedingType.BREAST, durationMin = 15, timestamp = "2026-08-24T08:00:00"),
                            Sleep(id = 1, babyId = 1, type = SleepType.NAP, startTime = "2026-08-24T10:00:00", endTime = "2026-08-24T11:30:00"),
                            Diaper(id = 1, babyId = 1, type = DiaperType.WET, timestamp = "2026-08-24T12:00:00"),
                        ),
                        loading = false,
                    ),
                    baby = Baby(id = 1, name = "小豆丁", gender = "boy", birthDate = "2025-08-01"),
                    onSeeAll = {},
                )
            }
        }
    }
}