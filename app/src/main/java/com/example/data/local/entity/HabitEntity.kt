package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val category: String = "Health", // Health, Productivity, Mindfulness, Fitness, Learning, Finance
    val targetCount: Int = 1,
    val unit: String = "times", // times, mins, glasses, pages, steps
    val reminderTime: String = "08:00", // HH:mm
    val frequencyDays: String = "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
    val iconName: String = "FitnessCenter",
    val colorHex: String = "#10B981",
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0
)
