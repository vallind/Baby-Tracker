package com.babytracker.feature.health

import com.babytracker.core.domain.model.Vaccination
import com.babytracker.core.domain.model.VaccinationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthScreenTest {
    @Test
    fun `completed vaccination count includes only done records`() {
        val vaccinations = listOf(
            Vaccination(babyId = 1, name = "待接种", status = VaccinationStatus.PENDING),
            Vaccination(babyId = 1, name = "已接种", status = VaccinationStatus.DONE),
            Vaccination(babyId = 1, name = "已跳过", status = VaccinationStatus.SKIPPED),
        )

        assertEquals(1, completedVaccinationCount(vaccinations))
    }
}
