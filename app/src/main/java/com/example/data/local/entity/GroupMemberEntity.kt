package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_members")
data class GroupMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val groupId: Long,
    val userName: String,
    val avatarInitials: String,
    val avatarColorHex: String = "#6750A4",
    val currentStreak: Int = 3,
    val habitsCompletedToday: Int = 2,
    val habitsTargetToday: Int = 3,
    val isCurrentUser: Boolean = false,
    val statusEmoji: String = "🔥",
    val statusText: String = "Crushing daily habits!",
    val xpWeekly: Int = 320
)
