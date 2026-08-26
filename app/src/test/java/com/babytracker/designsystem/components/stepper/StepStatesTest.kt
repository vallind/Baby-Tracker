package com.babytracker.designsystem.components.stepper

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * AppStepper 三态状态机纯逻辑测试。
 * 约定：currentStep 之前全部 Completed，当前位 Current，其后 Upcoming；越界自动钳制。
 */
class StepStatesTest {

    @Test
    fun `first step yields current then upcoming`() {
        assertEquals(
            listOf(AppStepState.Current, AppStepState.Upcoming, AppStepState.Upcoming),
            stepStates(3, 0),
        )
    }

    @Test
    fun `middle step splits completed and upcoming`() {
        assertEquals(
            listOf(AppStepState.Completed, AppStepState.Current, AppStepState.Upcoming),
            stepStates(3, 1),
        )
    }

    @Test
    fun `last step marks everything before as completed`() {
        assertEquals(
            listOf(AppStepState.Completed, AppStepState.Completed, AppStepState.Current),
            stepStates(3, 2),
        )
    }

    @Test
    fun `negative index clamps to first`() {
        assertEquals(AppStepState.Current, stepStates(3, -5).first())
    }

    @Test
    fun `overflow index clamps to last`() {
        assertEquals(AppStepState.Current, stepStates(3, 99).last())
        assertEquals(AppStepState.Completed, stepStates(3, 99).first())
    }
}
