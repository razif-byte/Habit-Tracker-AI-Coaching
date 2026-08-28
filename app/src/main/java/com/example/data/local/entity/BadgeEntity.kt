package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey
    val badgeKey: String,
    val title: String,
    val description: String,
    val iconName: String,
    val xpReward: Int = 100,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progress: Float = 0f // 0.0 to 1.0
)
