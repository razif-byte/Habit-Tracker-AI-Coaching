package com.example.data.remote.feed

import android.util.Log
import com.example.data.local.dao.TrendingLinkDao
import com.example.data.local.entity.TrendingLinkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class FeedSource(
    val url: String,
    val category: String, // "SOCIAL", "AI", "UTILITY", "MALAY_KARAOKE"
    val platform: String, // "YOUTUBE", "SMULE", "PLAY_STORE", "WEB"
    val sourceName: String,
    val isJson: Boolean = false,
    val defaultIcon: String = "Link",
    val colorHex: String = "#6750A4"
)

data class FeedSyncResult(
    val isSuccess: Boolean,
    val totalFetched: Int,
    val newInserted: Int,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

object TrendingFeedNetworkUtility {
    private const val TAG = "FeedNetworkUtility"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 HabitCoach/2.0"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .retryOnConnectionFailure(true)
            .build()
    }

    // Configured live RSS and API endpoints for each category
    val feedSources = listOf(
        // 1. AI TRENDING FEEDS
        FeedSource(
            url = "https://news.google.com/rss/search?q=Artificial+Intelligence+apps+tools+models&hl=en-US&gl=US&ceid=US:en",
            category = "AI",
            platform = "WEB",
            sourceName = "Google News AI",
            defaultIcon = "AutoAwesome",
            colorHex = "#10A37F"
        ),
        FeedSource(
            url = "https://hn.algolia.com/api/v1/search?query=AI+tools+agents&tags=story&hitsPerPage=8",
            category = "AI",
            platform = "WEB",
            sourceName = "HackerNews AI",
            isJson = true,
            defaultIcon = "Psychology",
            colorHex = "#D97706"
        ),
        FeedSource(
            url = "https://www.reddit.com/r/ArtificialInteligence/hot.rss?limit=8",
            category = "AI",
            platform = "WEB",
            sourceName = "Reddit AI",
            defaultIcon = "SmartToy",
            colorHex = "#4F46E5"
        ),

        // 2. SOCIAL TRENDING FEEDS
        FeedSource(
            url = "https://news.google.com/rss/search?q=social+media+apps+trending+features&hl=en-US&gl=US&ceid=US:en",
            category = "SOCIAL",
            platform = "WEB",
            sourceName = "Google News Social",
            defaultIcon = "Explore",
            colorHex = "#0284C7"
        ),
        FeedSource(
            url = "https://www.reddit.com/r/technology/hot.rss?limit=8",
            category = "SOCIAL",
            platform = "WEB",
            sourceName = "Reddit Tech",
            defaultIcon = "Share",
            colorHex = "#FF4500"
        ),

        // 3. UTILITY & PRODUCTIVITY TRENDING FEEDS
        FeedSource(
            url = "https://news.google.com/rss/search?q=best+android+apps+productivity+utility+tools&hl=en-US&gl=US&ceid=US:en",
            category = "UTILITY",
            platform = "PLAY_STORE",
            sourceName = "Google News Utility",
            defaultIcon = "Widgets",
            colorHex = "#7C3AED"
        ),
        FeedSource(
            url = "https://hn.algolia.com/api/v1/search?query=productivity+tools+apps&tags=story&hitsPerPage=8",
            category = "UTILITY",
            platform = "WEB",
            sourceName = "HackerNews Productivity",
            isJson = true,
            defaultIcon = "Description",
            colorHex = "#059669"
        ),

        // 4. MALAY KARAOKE & MUSIC FEEDS
        FeedSource(
            url = "https://news.google.com/rss/search?q=karaoke+melayu+lagu+viral+lirik&hl=ms&gl=MY&ceid=MY:ms",
            category = "MALAY_KARAOKE",
            platform = "YOUTUBE",
            sourceName = "Google News Karaoke",
            defaultIcon = "Mic",
            colorHex = "#E91E63"
        ),
        FeedSource(
            url = "https://www.youtube.com/feeds/videos.xml?search_query=karaoke+melayu+lirik+minus+one",
            category = "MALAY_KARAOKE",
            platform = "YOUTUBE",
            sourceName = "YouTube Malay Karaoke",
            defaultIcon = "MusicNote",
            colorHex = "#FF0000"
        )
    )

    /**
     * Fetches raw string from HTTP URL with custom headers
     */
    suspend fun fetchUrlContent(url: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/rss+xml, application/atom+xml, application/json, text/xml, */*")
                .header("Accept-Language", "en-US,en;q=0.9,ms;q=0.8")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                Result.success(body)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch $url: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Parses JSON from Algolia HackerNews or Reddit JSON APIs
     */
    fun parseJsonApiFeed(jsonStr: String, source: FeedSource): List<FeedItem> {
        val items = mutableListOf<FeedItem>()
        if (jsonStr.isBlank()) return items

        try {
            val root = JSONObject(jsonStr)

            // 1. HackerNews Algolia Format
            if (root.has("hits")) {
                val hits = root.getJSONArray("hits")
                for (i in 0 until hits.length()) {
                    val hit = hits.getJSONObject(i)
                    val title = hit.optString("title", "").trim()
                    var url = hit.optString("url", "").trim()
                    if (url.isBlank()) {
                        val objectId = hit.optString("objectID", "")
                        if (objectId.isNotBlank()) {
                            url = "https://news.ycombinator.com/item?id=$objectId"
                        }
                    }
                    val points = hit.optInt("points", 0)
                    val comments = hit.optInt("num_comments", 0)
                    val author = hit.optString("author", "Tech Community")

                    if (title.isNotBlank() && url.isNotBlank()) {
                        items.add(
                            FeedItem(
                                title = RssXmlParser.cleanHtml(title),
                                description = "Populariti: $points undian, $comments komen • Oleh @$author",
                                link = url,
                                category = source.category,
                                platform = source.platform,
                                sourceName = source.sourceName,
                                iconName = source.defaultIcon,
                                colorHex = source.colorHex
                            )
                        )
                    }
                }
            }
            // 2. Reddit JSON format
            else if (root.has("data") && root.getJSONObject("data").has("children")) {
                val children = root.getJSONObject("data").getJSONArray("children")
                for (i in 0 until children.length()) {
                    val post = children.getJSONObject(i).getJSONObject("data")
                    val title = post.optString("title", "")
                    val url = post.optString("url", "")
                    val permalink = "https://www.reddit.com" + post.optString("permalink", "")
                    val finalUrl = if (url.startsWith("http")) url else permalink
                    val ups = post.optInt("ups", 0)
                    val subreddit = post.optString("subreddit_name_prefixed", "r/tech")

                    if (title.isNotBlank()) {
                        items.add(
                            FeedItem(
                                title = RssXmlParser.cleanHtml(title),
                                description = "$subreddit • $ups undian komuniti",
                                link = finalUrl,
                                category = source.category,
                                platform = source.platform,
                                sourceName = source.sourceName,
                                iconName = source.defaultIcon,
                                colorHex = source.colorHex
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON feed from ${source.sourceName}", e)
        }

        return items
    }

    /**
     * Fetches a single FeedSource and returns parsed FeedItems
     */
    suspend fun fetchFeed(source: FeedSource): List<FeedItem> {
        val result = fetchUrlContent(source.url)
        return if (result.isSuccess) {
            val content = result.getOrNull() ?: ""
            if (source.isJson) {
                parseJsonApiFeed(content, source)
            } else {
                RssXmlParser.parseFeed(
                    xmlContent = content,
                    category = source.category,
                    platform = source.platform,
                    sourceName = source.sourceName,
                    defaultIcon = source.defaultIcon,
                    colorHex = source.colorHex
                )
            }
        } else {
            emptyList()
        }
    }

    /**
     * Fetches all trending feeds across all categories or for a specific category
     */
    suspend fun fetchAllFeeds(targetCategory: String? = null): List<FeedItem> = withContext(Dispatchers.IO) {
        val sourcesToFetch = if (targetCategory == null || targetCategory == "ALL") {
            feedSources
        } else {
            feedSources.filter { it.category.equals(targetCategory, ignoreCase = true) }
        }

        val allItems = mutableListOf<FeedItem>()
        for (source in sourcesToFetch) {
            val items = fetchFeed(source)
            if (items.isNotEmpty()) {
                allItems.addAll(items)
            }
        }

        // If network failed to fetch any items (e.g. offline mode), provide verified high-quality fallback items
        if (allItems.isEmpty()) {
            val fallback = getOfflineCuratedFeedFallbacks(targetCategory)
            allItems.addAll(fallback)
        }

        allItems
    }

    /**
     * Fetches live feeds and synchronizes them directly into the Room database.
     * Prevents duplicate URLs while adding new trending discoveries.
     */
    suspend fun fetchAndSyncWithDatabase(
        trendingLinkDao: TrendingLinkDao,
        targetCategory: String? = null
    ): FeedSyncResult = withContext(Dispatchers.IO) {
        try {
            val existingUrls = trendingLinkDao.getAllUrls().toSet()
            val fetchedItems = fetchAllFeeds(targetCategory)

            var newCount = 0
            val entitiesToInsert = mutableListOf<TrendingLinkEntity>()

            for (item in fetchedItems) {
                if (!existingUrls.contains(item.link)) {
                    entitiesToInsert.add(item.toTrendingLinkEntity())
                    newCount++
                }
            }

            if (entitiesToInsert.isNotEmpty()) {
                trendingLinkDao.insertAll(entitiesToInsert)
            }

            FeedSyncResult(
                isSuccess = true,
                totalFetched = fetchedItems.size,
                newInserted = newCount,
                message = if (newCount > 0) {
                    "Berjaya memuat turun ${fetchedItems.size} pautan ($newCount baharu) dari suapan RSS/API!"
                } else {
                    "Suapan terkini telah dikemaskini. Semua pautan terkini sudah ada dalam senarai."
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing feeds with database", e)
            FeedSyncResult(
                isSuccess = false,
                totalFetched = 0,
                newInserted = 0,
                message = "Ralat memuat turun suapan: ${e.localizedMessage ?: "Sambungan internet terganggu"}"
            )
        }
    }

    /**
     * High quality fallback curated items if device is offline or network is unreachable
     */
    fun getOfflineCuratedFeedFallbacks(category: String?): List<FeedItem> {
        val allFallbacks = listOf(
            // AI
            FeedItem(
                title = "Claude 3.5 Sonnet & Artifacts",
                description = "Model kecerdasan buatan terbaik untuk koding dan analisis saintifik berstruktur.",
                link = "https://claude.ai",
                category = "AI",
                platform = "WEB",
                sourceName = "Anthropic AI Feed",
                iconName = "AutoAwesome",
                colorHex = "#D97706"
            ),
            FeedItem(
                title = "Google DeepMind Gemini 1.5 Pro",
                description = "Konteks tetingkap 2 juta token dan keupayaan penalaran multimodal tinggi.",
                link = "https://gemini.google.com",
                category = "AI",
                platform = "PLAY_STORE",
                packageName = "com.google.android.apps.bard",
                sourceName = "Google AI Feed",
                iconName = "Psychology",
                colorHex = "#4285F4"
            ),
            FeedItem(
                title = "Perplexity AI: Search Reimagined",
                description = "Carian web masa nyata berpandukan sitasi sumber yang tepat dan telus.",
                link = "https://www.perplexity.ai",
                category = "AI",
                platform = "PLAY_STORE",
                packageName = "ai.perplexity.app.android",
                sourceName = "AI Search Feed",
                iconName = "Search",
                colorHex = "#0D9488"
            ),

            // Social
            FeedItem(
                title = "Threads by Instagram: Top Discussions",
                description = "Komuniti perbincangan teks moden dan perkongsian idea pantas.",
                link = "https://www.threads.net",
                category = "SOCIAL",
                platform = "PLAY_STORE",
                packageName = "com.instagram.barcelona",
                sourceName = "Social Trending Feed",
                iconName = "Forum",
                colorHex = "#101010"
            ),
            FeedItem(
                title = "TikTok Viral Sound Trends",
                description = "Trend audio dan video kreatif paling berpengaruh minggu ini.",
                link = "https://www.tiktok.com",
                category = "SOCIAL",
                platform = "PLAY_STORE",
                packageName = "com.zhiliaoapp.musically",
                sourceName = "Social Video Feed",
                iconName = "Share",
                colorHex = "#FE2C55"
            ),

            // Utility
            FeedItem(
                title = "Notion Calendar & Task Workspaces",
                description = "Pengurusan masa serba canggih untuk rutin harian dan perancangan projek.",
                link = "https://www.notion.so",
                category = "UTILITY",
                platform = "PLAY_STORE",
                packageName = "notion.id",
                sourceName = "Productivity Feed",
                iconName = "Description",
                colorHex = "#000000"
            ),
            FeedItem(
                title = "Forest Focus Timer",
                description = "Disiplin fokus tanpa gangguan telefon dengan pokok maya yang membesar.",
                link = "https://www.forestapp.cc",
                category = "UTILITY",
                platform = "PLAY_STORE",
                packageName = "cc.forestapp",
                sourceName = "Productivity Feed",
                iconName = "Park",
                colorHex = "#15803D"
            ),

            // Malay Karaoke
            FeedItem(
                title = "Karaoke Melayu: Bunga Angkasa (Iklim) Lirik HD",
                description = "Versi karaoke minus one rock kapak malar segar dengan audio berkualiti tinggi.",
                link = "https://www.youtube.com/results?search_query=bunga+angkasa+iklim+karaoke+minus+one",
                category = "MALAY_KARAOKE",
                platform = "YOUTUBE",
                packageName = "com.google.android.youtube",
                sourceName = "YouTube Music Feed",
                iconName = "Mic",
                colorHex = "#FF0000"
            ),
            FeedItem(
                title = "Smule Duet: Tiara (Kris) Karaoke Challenge",
                description = "Sertai ribuan penyanyi menyanyikan lagu Tiara secara duet di aplikasi Smule.",
                link = "https://www.smule.com/search?q=tiara+kris",
                category = "MALAY_KARAOKE",
                platform = "SMULE",
                packageName = "com.smule.singandroid",
                sourceName = "Smule Community Feed",
                iconName = "Headphones",
                colorHex = "#325FFF"
            )
        )

        return if (category == null || category == "ALL") {
            allFallbacks
        } else {
            allFallbacks.filter { it.category.equals(category, ignoreCase = true) }
        }
    }
}
