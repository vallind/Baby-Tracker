package com.babytracker.core.util

import com.babytracker.core.domain.model.Vaccination
import com.babytracker.core.domain.model.VaccinationStatus

object VaccineSchedule {
    data class DefaultVaccine(val name: String, val dose: String, val monthAge: Int)

    val list = listOf(
        DefaultVaccine("卡介苗", "", 0),
        DefaultVaccine("乙肝疫苗", "第1剂", 0),
        DefaultVaccine("乙肝疫苗", "第2剂", 1),
        DefaultVaccine("脊灰疫苗", "第1剂", 2),
        DefaultVaccine("脊灰疫苗", "第2剂", 3),
        DefaultVaccine("百白破", "第1剂", 3),
        DefaultVaccine("脊灰疫苗", "第3剂", 4),
        DefaultVaccine("百白破", "第2剂", 4),
        DefaultVaccine("百白破", "第3剂", 5),
        DefaultVaccine("乙肝疫苗", "第3剂", 6),
        DefaultVaccine("A群流脑", "第1剂", 6),
        DefaultVaccine("麻腮风", "第1剂", 8),
        DefaultVaccine("乙脑减毒活", "第1剂", 8),
        DefaultVaccine("A群流脑", "第2剂", 9),
        DefaultVaccine("百白破", "第4剂", 18),
        DefaultVaccine("麻腮风", "第2剂", 18),
        DefaultVaccine("甲肝减毒活", "", 18),
        DefaultVaccine("乙脑减毒活", "第2剂", 24),
        DefaultVaccine("A+C群流脑", "第1剂", 36),
        DefaultVaccine("A+C群流脑", "第2剂", 72),
        DefaultVaccine("白破", "", 72),
    )

    fun createForBaby(babyId: Int, birthDate: String): List<Vaccination> {
        val birth = java.time.LocalDate.parse(birthDate)
        return list.map { v ->
            val scheduled = birth.plusMonths(v.monthAge.toLong()).toString()
            Vaccination(babyId = babyId, name = v.name, dose = v.dose.ifBlank { null }, scheduledDate = scheduled + "T00:00:00", status = VaccinationStatus.PENDING)
        }
    }
}