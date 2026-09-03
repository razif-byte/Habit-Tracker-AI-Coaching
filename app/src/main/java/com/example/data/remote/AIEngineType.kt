package com.example.data.remote

enum class AIEngineType(
    val id: String,
    val displayName: String,
    val providerName: String,
    val modelTag: String,
    val badgeLabel: String,
    val description: String
) {
    GEMINI_FLASH(
        id = "gemini_flash",
        displayName = "Gemini 3.5 Flash",
        providerName = "Google DeepMind",
        modelTag = "gemini-3.5-flash",
        badgeLabel = "Gemini 3.5",
        description = "Analisis tingkah laku pantas, neuroplastisiti & pembinaan tabiat atomik."
    ),
    CHATGPT_4O(
        id = "chatgpt_4o",
        displayName = "ChatGPT (GPT-4o)",
        providerName = "OpenAI",
        modelTag = "gpt-4o",
        badgeLabel = "ChatGPT-4o",
        description = "Penaakulan mendalam, pelan strategi produktiviti komprehensif & cadangan holistik."
    ),
    CHATGPT_MINI(
        id = "chatgpt_mini",
        displayName = "ChatGPT 4o-Mini",
        providerName = "OpenAI",
        modelTag = "gpt-4o-mini",
        badgeLabel = "GPT-4o Mini",
        description = "Maklum balas pantas, bimbingan ultra ringan dan pemulihan momentum harian."
    ),
    GEMINI_LIVE(
        id = "gemini_live",
        displayName = "Gemini Live",
        providerName = "Google AI Voice",
        modelTag = "gemini-live-audio",
        badgeLabel = "Gemini Live 🎙️",
        description = "Perbualan suara dua hala interaktif masa nyata dengan sintesis audio langsung."
    );

    companion object {
        fun fromId(id: String?): AIEngineType {
            return entries.firstOrNull { it.id == id } ?: GEMINI_FLASH
        }
    }
}
