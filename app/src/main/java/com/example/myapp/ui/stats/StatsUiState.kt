package com.example.myapp.ui.stats

data class StatsUiState(
    val loading: Boolean = false,
    val feedingCount: Int = 0,
    val sleepHours: Float = 0f,
    val growthTrend: List<Float> = emptyList()
)
