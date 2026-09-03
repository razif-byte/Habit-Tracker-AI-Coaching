package com.example.data.remote.feed

import com.example.data.local.entity.TrendingLinkEntity

data class FeedItem(
    val title: String,
    val description: String,
    val link: String,
    val pubDate: String? = null,
    val category: String, // "SOCIAL", "AI", "UTILITY", "MALAY_KARAOKE"
    val platform: String = "WEB", // "YOUTUBE", "SMULE", "PLAY_STORE", "WEB"
    val sourceName: String = "",
    val packageName: String = "",
    val iconName: String = "Link",
    val colorHex: String = "#6750A4"
) {
    fun toTrendingLinkEntity(): TrendingLinkEntity {
        return TrendingLinkEntity(
            title = title.trim(),
            subtitle = if (description.isNotBlank()) description.trim() else "Trending dari $sourceName",
            url = link.trim(),
            category = category,
            platform = platform,
            packageName = packageName,
            iconName = iconName,
            colorHex = colorHex,
            isCustom = false
        )
    }
}
