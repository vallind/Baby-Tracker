package com.example.myapp.ui.health

import com.example.myapp.data.room.HealthProfileEntity

data class HealthUiState(
    val loading: Boolean = false,
    val profile: HealthProfileEntity? = null,
    val error: String? = null
)
