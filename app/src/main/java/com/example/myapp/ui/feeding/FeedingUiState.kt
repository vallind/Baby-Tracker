package com.example.myapp.ui.feeding

import com.example.myapp.data.room.FeedingEntity

data class FeedingUiState(
    val loading: Boolean = false,
    val records: List<FeedingEntity> = emptyList(),
    val error: String? = null
)
