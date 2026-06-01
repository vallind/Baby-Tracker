package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "growth_records")
data class GrowthEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long = 1,
    val height: Float,
    val weight: Float,
    val headCircumference: Float = 0f,
    val recordDate: Long = System.currentTimeMillis()
)
