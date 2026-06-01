package com.example.myapp.ui.growth

import com.example.myapp.data.room.GrowthEntity

data class GrowthUiState(
    val loading: Boolean = false,
    val records: List<GrowthEntity> = emptyList(),
    val error: String? = null
)
