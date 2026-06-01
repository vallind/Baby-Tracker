package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_profiles")
data class HealthProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long = 1,
    val allergies: String? = null,
    val medicalHistory: String? = null,
    val doctorNotes: String? = null
)
