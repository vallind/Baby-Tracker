package com.example.myapp.ui.feeding

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.data.room.FeedingDao
import com.example.myapp.data.room.FeedingEntity
import com.example.myapp.domain.feeding.AddFeedingUseCase
import com.example.myapp.domain.feeding.DeleteFeedingUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class FeedingScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showsEmptyState() {
        val dao: FeedingDao = mockk()
        coEvery { dao.getAllByBabyFlow(any()) } returns MutableStateFlow(emptyList())
        val repo = FeedingRepository(dao)
        val vm = FeedingViewModel(repo, AddFeedingUseCase(repo), DeleteFeedingUseCase(repo))

        composeTestRule.setContent {
            FeedingScreen(viewModel = vm, onAddClick = {})
        }

        composeTestRule.onNodeWithText("暂无喂养记录").assertExists()
    }

    @Test
    fun showsRecords() {
        val dao: FeedingDao = mockk()
        coEvery { dao.getAllByBabyFlow(any()) } returns MutableStateFlow(
            listOf(FeedingEntity(id = 1, type = "BREAST_MILK", amount = 120, unit = "ml"))
        )
        val repo = FeedingRepository(dao)
        val vm = FeedingViewModel(repo, AddFeedingUseCase(repo), DeleteFeedingUseCase(repo))

        composeTestRule.setContent {
            FeedingScreen(viewModel = vm, onAddClick = {})
        }

        composeTestRule.onNodeWithText("120 ml").assertExists()
    }
}
