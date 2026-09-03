package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trending_links")
data class TrendingLinkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val subtitle: String = "",
    val url: String,
    val category: String, // "SOCIAL", "AI", "UTILITY", "MALAY_KARAOKE"
    val platform: String = "WEB", // "YOUTUBE", "SMULE", "PLAY_STORE", "WEB"
    val packageName: String = "", // e.g. "com.google.android.youtube", "com.smule.singandroid"
    val iconName: String = "Link",
    val colorHex: String = "#6750A4",
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val clickCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
