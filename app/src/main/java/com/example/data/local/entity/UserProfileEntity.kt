package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Alex Rivera",
    val email: String = "alex.rivera@example.com",
    val age: Int = 27,
    val primaryGoal: String = "Build peak physical stamina & morning consistency",
    val experienceLevel: String = "Intermediate", // Beginner, Intermediate, Habit Master
    val isPremium: Boolean = false,
    val subscriptionPlan: String = "FREE", // FREE, MONTHLY, YEARLY
    val subscriptionExpiry: Long? = null,
    val xp: Int = 150,
    val level: Int = 1,
    val totalHabitsCompleted: Int = 14,
    val longestOverallStreak: Int = 5,
    val notificationsEnabled: Boolean = true,
    val morningReminderTime: String = "07:30",
    val eveningReviewTime: String = "21:00",
    val isDarkMode: Boolean = true
)
