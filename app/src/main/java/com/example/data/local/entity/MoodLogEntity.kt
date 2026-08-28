package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mood_logs",
    indices = [
        Index(value = ["dateString"], unique = true)
    ]
)
data class MoodLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val dateString: String, // YYYY-MM-DD
    val moodScore: Int, // 1 to 5 (1=Down, 2=Meh, 3=Neutral/OK, 4=Good, 5=Energized/Fantastic)
    val moodLabel: String,
    val moodEmoji: String,
    val energyLevel: Int = 3, // 1 to 5
    val stressLevel: Int = 2, // 1 to 5
    val tags: String = "Focused", // comma separated
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
