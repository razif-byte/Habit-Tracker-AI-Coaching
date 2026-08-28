package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sender: String, // "user" or "coach"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestionType: String = "general" // general, streak_rescue, routine_stack, daily_motivate, milestone
)
