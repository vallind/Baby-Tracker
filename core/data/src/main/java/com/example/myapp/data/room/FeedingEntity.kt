package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeding_records")
data class FeedingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long = 1,
    val type: String,
    val amount: Int,
    val unit: String = "ml",
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
