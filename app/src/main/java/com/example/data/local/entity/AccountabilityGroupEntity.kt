package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accountability_groups")
data class AccountabilityGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String,
    val iconName: String = "Groups",
    val category: String = "General",
    val inviteCode: String,
    val memberCount: Int = 4,
    val targetDailyCompletions: Int = 12,
    val isUserJoined: Boolean = true,
    val streak: Int = 5,
    val createdAt: Long = System.currentTimeMillis()
)
