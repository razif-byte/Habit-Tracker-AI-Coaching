package com.example.data.repository

import com.example.data.local.dao.TrendingLinkDao
import com.example.data.local.entity.TrendingLinkEntity
import com.example.data.remote.feed.FeedSyncResult
import com.example.data.remote.feed.TrendingFeedNetworkUtility
import kotlinx.coroutines.flow.Flow

class TrendingRepository(private val trendingLinkDao: TrendingLinkDao) {

    val allLinks: Flow<List<TrendingLinkEntity>> = trendingLinkDao.getAllLinks()

    fun getLinksByCategory(category: String): Flow<List<TrendingLinkEntity>> {
        return trendingLinkDao.getLinksByCategory(category)
    }

    suspend fun insertLink(link: TrendingLinkEntity): Long {
        return trendingLinkDao.insertLink(link)
    }

    suspend fun updateFavorite(id: Long, isFavorite: Boolean) {
        trendingLinkDao.updateFavorite(id, isFavorite)
    }

    suspend fun recordClick(id: Long) {
        trendingLinkDao.incrementClickCount(id)
    }

    suspend fun deleteLink(link: TrendingLinkEntity) {
        trendingLinkDao.deleteLink(link)
    }

    suspend fun checkAndSeedInitialTrendingLinks() {
        if (trendingLinkDao.getCount() == 0) {
            val initialList = getCuratedTrendingLinks()
            trendingLinkDao.insertAll(initialList)
        }
    }

    suspend fun fetchAndSyncFeeds(category: String? = null): FeedSyncResult {
        return TrendingFeedNetworkUtility.fetchAndSyncWithDatabase(trendingLinkDao, category)
    }

    companion object {
        fun getCuratedTrendingLinks(): List<TrendingLinkEntity> {
            return listOf(
                // 1. SOCIAL APPS
                TrendingLinkEntity(
                    title = "TikTok",
                    subtitle = "Trending videos, creativity & short-form discovery",
                    url = "https://www.tiktok.com",
                    category = "SOCIAL",
                    platform = "PLAY_STORE",
                    packageName = "com.zhiliaoapp.musically",
                    iconName = "Share",
                    colorHex = "#FE2C55",
                    isFavorite = true
                ),
                TrendingLinkEntity(
                    title = "Instagram",
                    subtitle = "Stories, reels, visual social connections",
                    url = "https://www.instagram.com",
                    category = "SOCIAL",
                    platform = "PLAY_STORE",
                    packageName = "com.instagram.android",
                    iconName = "PhotoCamera",
                    colorHex = "#E1306C",
                    isFavorite = true
                ),
                TrendingLinkEntity(
                    title = "WhatsApp",
                    subtitle = "Private messaging, community channels & squad calls",
                    url = "https://web.whatsapp.com",
                    category = "SOCIAL",
                    platform = "PLAY_STORE",
                    packageName = "com.whatsapp",
                    iconName = "Chat",
                    colorHex = "#25D366",
                    isFavorite = true
                ),
                TrendingLinkEntity(
                    title = "Telegram",
                    subtitle = "Fast cloud messaging, channels & habit study squads",
                    url = "https://web.telegram.org",
                    category = "SOCIAL",
                    platform = "PLAY_STORE",
                    packageName = "org.telegram.messenger",
                    iconName = "Send",
                    colorHex = "#229ED9"
                ),
                TrendingLinkEntity(
                    title = "Threads",
                    subtitle = "Text-based social discussion & ideas by Meta",
                    url = "https://www.threads.net",
                    category = "SOCIAL",
                    platform = "PLAY_STORE",
                    packageName = "com.instagram.barcelona",
                    iconName = "Forum",
                    colorHex = "#101010"
                ),
                TrendingLinkEntity(
                    title = "X (Twitter)",
                    subtitle = "Real-time news, trending topics & global tech discourse",
                    url = "https://twitter.com",
                    category = "SOCIAL",
                    platform = "PLAY_STORE",
                    packageName = "com.twitter.android",
                    iconName = "Tag",
                    colorHex = "#1DA1F2"
                ),
                TrendingLinkEntity(
                    title = "Discord",
                    subtitle = "Voice, text & communities for productivity & gaming",
                    url = "https://discord.com",
                    category = "SOCIAL",
                    platform = "PLAY_STORE",
                    packageName = "com.discord",
                    iconName = "Headset",
                    colorHex = "#5865F2"
                ),

                // 2. AI APPS
                TrendingLinkEntity(
                    title = "ChatGPT (OpenAI)",
                    subtitle = "Conversational intelligence, coaching & reasoning",
                    url = "https://chat.openai.com",
                    category = "AI",
                    platform = "PLAY_STORE",
                    packageName = "com.openai.chatgpt",
                    iconName = "Psychology",
                    colorHex = "#10A37F",
                    isFavorite = true
                ),
                TrendingLinkEntity(
                    title = "Google Gemini",
                    subtitle = "Multimodal AI assistant with DeepMind reasoning",
                    url = "https://gemini.google.com",
                    category = "AI",
                    platform = "PLAY_STORE",
                    packageName = "com.google.android.apps.bard",
                    iconName = "AutoAwesome",
                    colorHex = "#4285F4",
                    isFavorite = true
                ),
                TrendingLinkEntity(
                    title = "Claude (Anthropic)",
                    subtitle = "Thoughtful, nuanced AI writing & deep analysis",
                    url = "https://claude.ai",
                    category = "AI",
                    platform = "WEB",
                    packageName = "com.anthropic.claude",
                    iconName = "Lightbulb",
                    colorHex = "#D97706"
                ),
                TrendingLinkEntity(
                    title = "Perplexity AI",
                    subtitle = "AI search engine with real-time web citations",
                    url = "https://www.perplexity.ai",
                    category = "AI",
                    platform = "PLAY_STORE",
                    packageName = "ai.perplexity.app.android",
                    iconName = "Search",
                    colorHex = "#0D9488"
                ),
                TrendingLinkEntity(
                    title = "Microsoft Copilot",
                    subtitle = "Everyday AI companion with GPT-4 and DALL-E 3",
                    url = "https://copilot.microsoft.com",
                    category = "AI",
                    platform = "PLAY_STORE",
                    packageName = "com.microsoft.copilot",
                    iconName = "Assistant",
                    colorHex = "#0284C7"
                ),
                TrendingLinkEntity(
                    title = "DeepSeek AI",
                    subtitle = "Open-source reasoning & coding AI models",
                    url = "https://chat.deepseek.com",
                    category = "AI",
                    platform = "WEB",
                    packageName = "com.deepseek.chat",
                    iconName = "Terminal",
                    colorHex = "#4F46E5"
                ),
                TrendingLinkEntity(
                    title = "Poe - Fast AI Chat",
                    subtitle = "Multi-bot AI platform with Claude, GPT & customized bots",
                    url = "https://poe.com",
                    category = "AI",
                    platform = "PLAY_STORE",
                    packageName = "com.poe.android",
                    iconName = "SmartToy",
                    colorHex = "#8B5CF6"
                ),

                // 3. UTILITY APPS
                TrendingLinkEntity(
                    title = "Notion",
                    subtitle = "Connected workspace for notes, tasks & life planning",
                    url = "https://www.notion.so",
                    category = "UTILITY",
                    platform = "PLAY_STORE",
                    packageName = "notion.id",
                    iconName = "Description",
                    colorHex = "#000000",
                    isFavorite = true
                ),
                TrendingLinkEntity(
                    title = "Forest: Stay Focused",
                    subtitle = "Gamified pomodoro timer: plant trees while focusing",
                    url = "https://www.forestapp.cc",
                    category = "UTILITY",
                    platform = "PLAY_STORE",
                    packageName = "cc.forestapp",
                    iconName = "Park",
                    colorHex = "#15803D",
                    isFavorite = true
                ),
                TrendingLinkEntity(
                    title = "Google Drive",
                    subtitle = "Secure cloud file backup & collaborative workspaces",
                    url = "https://drive.google.com",
                    category = "UTILITY",
                    platform = "PLAY_STORE",
                    packageName = "com.google.android.apps.docs",
                    iconName = "Cloud",
                    colorHex = "#1E88E5"
                ),
                TrendingLinkEntity(
                    title = "Duolingo",
                    subtitle = "Language learning with fun gamified daily habits",
                    url = "https://www.duolingo.com",
                    category = "UTILITY",
                    platform = "PLAY_STORE",
                    packageName = "com.duolingo",
                    iconName = "School",
                    colorHex = "#58CC02"
                ),
                TrendingLinkEntity(
                    title = "Canva",
                    subtitle = "Visual design, presentations & habit trackers template",
                    url = "https://www.canva.com",
                    category = "UTILITY",
                    platform = "PLAY_STORE",
                    packageName = "com.canva.editor",
                    iconName = "Palette",
                    colorHex = "#7D2AE8"
                ),
                TrendingLinkEntity(
                    title = "CamScanner",
                    subtitle = "High-definition mobile document scanning & OCR",
                    url = "https://www.camscanner.com",
                    category = "UTILITY",
                    platform = "PLAY_STORE",
                    packageName = "com.intsig.camscanner",
                    iconName = "Scanner",
                    colorHex = "#008080"
                ),

                // 4. MALAY KARAOKE VIDEO TRENDING (YOUTUBE / SMULE)
                TrendingLinkEntity(
                    title = "Bunga Angkasa - Iklim",
                    subtitle = "Karaoke Melayu Klasik Slow Rock • Lirik Penuh & Instrumental",
                    url = "https://www.youtube.com/results?search_query=bunga+angkasa+iklim+karaoke+lirik",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "Mic",
                    colorHex = "#FF0000",
                    isFavorite = true
                ),
                TrendingLinkEntity(
                    title = "Suci Dalam Debu - Iklim",
                    subtitle = "Lagu Malar Segar Terbaik • Versi Karaoke YouTube & Smule",
                    url = "https://www.youtube.com/results?search_query=suci+dalam+debu+iklim+karaoke+lirik",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "MusicNote",
                    colorHex = "#FF0000",
                    isFavorite = true
                ),
                TrendingLinkEntity(
                    title = "Tiara - Kris",
                    subtitle = "Lagu Rock Romantik Viral • YouTube & Smule Karaoke Duet",
                    url = "https://www.youtube.com/results?search_query=tiara+kris+karaoke+lirik+minus+one",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "Mic",
                    colorHex = "#FF0000",
                    isFavorite = true
                ),
                TrendingLinkEntity(
                    title = "Janji Manismu - Aishah",
                    subtitle = "Vokal Emas Juara • Minus One Instrumental & Smule Duet",
                    url = "https://www.youtube.com/results?search_query=janji+manismu+aishah+karaoke+lirik",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "Star",
                    colorHex = "#FF0000",
                    isFavorite = true
                ),
                TrendingLinkEntity(
                    title = "Smule: Sing Bunga Angkasa / Melayu Hits",
                    subtitle = "Aplikasi Smule Karaoke Sosial • Nyanyi Duet & Rakaman Suara",
                    url = "https://www.smule.com/search?q=karaoke+melayu",
                    category = "MALAY_KARAOKE",
                    platform = "SMULE",
                    packageName = "com.smule.singandroid",
                    iconName = "GraphicEq",
                    colorHex = "#325FFF",
                    isFavorite = true
                ),
                TrendingLinkEntity(
                    title = "Isabella - Search",
                    subtitle = "Rock Kapak Legenda Nusantara • Minus One Karaoke Instrumental",
                    url = "https://www.youtube.com/results?search_query=isabella+search+karaoke+lirik",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "MusicVideo",
                    colorHex = "#FF0000"
                ),
                TrendingLinkEntity(
                    title = "Sandaran Kasih - Fauziah Latiff / Slam",
                    subtitle = "Lagu Balada Menggamit Memori • Karaoke YouTube & Smule",
                    url = "https://www.youtube.com/results?search_query=sandaran+kasih+karaoke+lirik",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "Mic",
                    colorHex = "#FF0000"
                ),
                TrendingLinkEntity(
                    title = "Ghazal Untuk Rabiah - Jamal & M. Nasir",
                    subtitle = "Duet Puisi Melayu Ikonik • Instrumental Lirik",
                    url = "https://www.youtube.com/results?search_query=ghazal+untuk+rabiah+karaoke+lirik",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "RecordVoiceOver",
                    colorHex = "#FF0000"
                ),
                TrendingLinkEntity(
                    title = "Hati Kama - Siti Nurhaliza & Noraniza Idris",
                    subtitle = "Irama Malaysia Klasik Bertemu Dangdut • Versi Duet Karaoke",
                    url = "https://www.youtube.com/results?search_query=hati+kama+siti+nurhaliza+karaoke+lirik",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "QueueMusic",
                    colorHex = "#FF0000"
                ),
                TrendingLinkEntity(
                    title = "Biar Menjadi Kenangan - Reza & Siti Nurhaliza",
                    subtitle = "Lagu Duet Paling Popular di Smule & YouTube",
                    url = "https://www.youtube.com/results?search_query=biar+menjadi+kenangan+karaoke+lirik",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "LibraryMusic",
                    colorHex = "#FF0000"
                ),
                TrendingLinkEntity(
                    title = "Gerimis Mengundang - Slam",
                    subtitle = "Hits Populariti Serantau Malaysia & Indonesia • Karaoke Lirik",
                    url = "https://www.youtube.com/results?search_query=gerimis+mengundang+slam+karaoke+lirik",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "MusicNote",
                    colorHex = "#FF0000"
                ),
                TrendingLinkEntity(
                    title = "Menahan Rindu - Wany Hasrita",
                    subtitle = "Balada Moden Trending • YouTube & Smule Duet",
                    url = "https://www.youtube.com/results?search_query=menahan+rindu+wany+hasrita+karaoke+lirik",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "Mic",
                    colorHex = "#FF0000"
                ),
                TrendingLinkEntity(
                    title = "Tergantung Sepi - Haqiem Rusli",
                    subtitle = "Vokal Emosi Pop Terkini • Minus One Instrumental",
                    url = "https://www.youtube.com/results?search_query=tergantung+sepi+haqiem+rusli+karaoke+lirik",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "VolumeUp",
                    colorHex = "#FF0000"
                ),
                TrendingLinkEntity(
                    title = "Cindai - Siti Nurhaliza",
                    subtitle = "Lagu Tradisional Terbaik Sepanjang Zaman • Karaoke Minus One",
                    url = "https://www.youtube.com/results?search_query=cindai+siti+nurhaliza+karaoke+lirik",
                    category = "MALAY_KARAOKE",
                    platform = "YOUTUBE",
                    packageName = "com.google.android.youtube",
                    iconName = "Star",
                    colorHex = "#FF0000"
                ),
                TrendingLinkEntity(
                    title = "Smule Melayu Duet Community",
                    subtitle = "Terokai ribuan rakaman duet lagu Melayu di platform Smule",
                    url = "https://www.smule.com/search?q=malay+karaoke",
                    category = "MALAY_KARAOKE",
                    platform = "SMULE",
                    packageName = "com.smule.singandroid",
                    iconName = "Headphones",
                    colorHex = "#325FFF"
                )
            )
        }
    }
}
