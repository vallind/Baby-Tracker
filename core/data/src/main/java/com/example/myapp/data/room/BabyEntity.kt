package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "babies")
data class BabyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gender: String,
    val birthday: Long,
    val avatar: String? = null,
    val birthHeight: Float = 0f,
    val birthWeight: Float = 0f,
    val note: String? = null
)
