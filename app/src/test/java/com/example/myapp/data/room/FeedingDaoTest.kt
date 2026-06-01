package com.example.myapp.data.room

import com.example.myapp.data.repository.FeedingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedingDaoTest {
    private val dao: FeedingDao = mockk()
    private val repository = FeedingRepository(dao)

    @Test
    fun insertAndQueryFeeding() = runBlocking {
        val recordsFlow = MutableStateFlow(emptyList<FeedingEntity>())
        val entity = FeedingEntity(type = "BREAST_MILK", amount = 120, unit = "ml")
        coEvery { dao.insert(entity) } returns Unit
        coEvery { dao.getAllByBabyFlow(any()) } returns recordsFlow

        recordsFlow.value = listOf(entity)
        val records = repository.getAllByBaby().first()
        assertEquals(1, records.size)
        assertEquals(120, records[0].amount)
        assertEquals("BREAST_MILK", records[0].type)
        coVerify { dao.getAllByBabyFlow(any()) }
    }

    @Test
    fun insertAndDeleteFeeding() = runBlocking {
        val recordsFlow = MutableStateFlow(emptyList<FeedingEntity>())
        val entity = FeedingEntity(type = "FORMULA", amount = 180, unit = "ml")
        coEvery { dao.insert(entity) } returns Unit
        coEvery { dao.delete(entity) } returns Unit
        coEvery { dao.getAllByBabyFlow(any()) } returns recordsFlow

        recordsFlow.value = listOf(entity)
        assertEquals(1, repository.getAllByBaby().first().size)
        recordsFlow.value = emptyList()
        assertEquals(0, repository.getAllByBaby().first().size)
    }

    @Test
    fun queryByBabyId() = runBlocking {
        val entity1 = FeedingEntity(babyId = 1, type = "BREAST_MILK", amount = 100, unit = "ml")
        val entity2 = FeedingEntity(babyId = 2, type = "FORMULA", amount = 200, unit = "ml")
        coEvery { dao.getAllByBabyFlow(babyId = 1) } returns MutableStateFlow(listOf(entity1))
        coEvery { dao.getAllByBabyFlow(babyId = 2) } returns MutableStateFlow(listOf(entity2))

        val baby1Records = repository.getAllByBaby(babyId = 1).first()
        assertEquals(1, baby1Records.size)
        assertEquals("BREAST_MILK", baby1Records[0].type)
    }
}
