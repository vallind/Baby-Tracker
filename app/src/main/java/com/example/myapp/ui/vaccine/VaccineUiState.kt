package com.example.myapp.ui.vaccine

import com.example.myapp.data.room.VaccineEntity

data class VaccineUiState(
    val loading: Boolean = false,
    val vaccines: List<VaccineEntity> = emptyList(),
    val error: String? = null
)
