package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_messages")
data class GroupMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val groupId: Long,
    val senderName: String,
    val senderAvatar: String,
    val isCurrentUser: Boolean = false,
    val message: String,
    val messageType: String = "chat", // "chat", "cheer", "milestone", "system"
    val timestamp: Long = System.currentTimeMillis()
)
