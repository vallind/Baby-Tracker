package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaccines")
data class VaccineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long = 1,
    val name: String,
    val dose: Int,
    val plannedDate: Long,
    val completedDate: Long? = null,
    val status: String = "PENDING"
)
