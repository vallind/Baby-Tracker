package com.example.myapp.ui.sleep

import com.example.myapp.data.room.SleepEntity

data class SleepUiState(
    val loading: Boolean = false,
    val records: List<SleepEntity> = emptyList(),
    val error: String? = null
)
