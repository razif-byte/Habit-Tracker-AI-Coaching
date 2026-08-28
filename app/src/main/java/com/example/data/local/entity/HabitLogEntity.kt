package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_logs",
    indices = [
        Index(value = ["habitId", "dateString"], unique = true)
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val habitId: Long,
    val dateString: String, // format: YYYY-MM-DD
    val count: Int = 1,
    val isCompleted: Boolean = true,
    val notes: String = "",
    val completedAt: Long = System.currentTimeMillis()
)
