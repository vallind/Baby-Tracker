package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_records")
data class SleepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long = 1,
    val startTime: Long,
    val endTime: Long,
    val type: String = "NAP"
)
