package com.example.myapp.ui.feeding

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.data.room.FeedingDao
import com.example.myapp.data.room.FeedingEntity
import com.example.myapp.domain.feeding.AddFeedingUseCase
import com.example.myapp.domain.feeding.DeleteFeedingUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FeedingViewModelTest {
    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private val dao: FeedingDao = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addEventCallsUseCase() = runTest(testDispatcher) {
        coEvery { dao.getAllByBabyFlow(any()) } returns MutableStateFlow(emptyList())
        coEvery { dao.insert(any()) } returns Unit
        val repository = FeedingRepository(dao)
        val addUseCase = AddFeedingUseCase(repository)
        val deleteUseCase = DeleteFeedingUseCase(repository)
        val viewModel = FeedingViewModel(repository, addUseCase, deleteUseCase)

        viewModel.onEvent(FeedingEvent.Add("WATER", 50, "ml", null))
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { dao.insert(any()) }
    }

    @Test
    fun recordsCollectedUpdatesUiState() = runTest(testDispatcher) {
        val testFlow = MutableStateFlow(
            listOf(FeedingEntity(id = 1, type = "BREAST_MILK", amount = 120, unit = "ml"))
        )
        coEvery { dao.getAllByBabyFlow(any()) } returns testFlow
        coEvery { dao.insert(any()) } returns Unit
        val repository = FeedingRepository(dao)
        val addUseCase = AddFeedingUseCase(repository)
        val deleteUseCase = DeleteFeedingUseCase(repository)
        val viewModel = FeedingViewModel(repository, addUseCase, deleteUseCase)

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.first()
        assertEquals(false, state.loading)
        assertEquals(1, state.records.size)
        assertEquals(120, state.records[0].amount)
    }
}
