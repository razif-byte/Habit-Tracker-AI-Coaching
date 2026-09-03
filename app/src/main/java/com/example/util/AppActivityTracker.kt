package com.example.util

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.util.Calendar

data class DetectedAppUsage(
    val packageName: String,
    val appName: String,
    val category: String, // "Social", "AI Tools", "Utility", "Karaoke & Media", "Productivity", "Other"
    val totalTimeInForegroundMs: Long,
    val lastTimeUsedMs: Long,
    val iconName: String = "Apps"
) {
    val durationMinutes: Long get() = totalTimeInForegroundMs / (1000 * 60)
    val durationFormatted: String get() {
        val minutes = durationMinutes
        val hours = minutes / 60
        val remMin = minutes % 60
        return if (hours > 0) "${hours}j ${remMin}m" else "${minutes}m"
    }
}

data class DailyActivitySummary(
    val totalScreenTimeMinutes: Long,
    val socialTimeMinutes: Long,
    val aiTimeMinutes: Long,
    val utilityTimeMinutes: Long,
    val entertainmentTimeMinutes: Long,
    val topApps: List<DetectedAppUsage>,
    val isPermissionGranted: Boolean
)

object AppActivityTracker {

    fun getTodayAppUsage(context: Context): DailyActivitySummary {
        val isGranted = AppIntegrationHelper.isUsageAccessPermissionGranted(context)
        if (!isGranted) {
            // Return informative mock / baseline breakdown with permission indicator
            return DailyActivitySummary(
                totalScreenTimeMinutes = 185,
                socialTimeMinutes = 80,
                aiTimeMinutes = 45,
                utilityTimeMinutes = 35,
                entertainmentTimeMinutes = 25,
                topApps = listOf(
                    DetectedAppUsage("com.zhiliaoapp.musically", "TikTok", "Social", 45 * 60 * 1000L, System.currentTimeMillis()),
                    DetectedAppUsage("com.openai.chatgpt", "ChatGPT", "AI Tools", 30 * 60 * 1000L, System.currentTimeMillis()),
                    DetectedAppUsage("com.google.android.youtube", "YouTube (Karaoke)", "Karaoke & Media", 25 * 60 * 1000L, System.currentTimeMillis()),
                    DetectedAppUsage("com.instagram.android", "Instagram", "Social", 20 * 60 * 1000L, System.currentTimeMillis()),
                    DetectedAppUsage("com.google.android.apps.bard", "Google Gemini", "AI Tools", 15 * 60 * 1000L, System.currentTimeMillis()),
                    DetectedAppUsage("notion.id", "Notion", "Utility", 20 * 60 * 1000L, System.currentTimeMillis())
                ),
                isPermissionGranted = false
            )
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return DailyActivitySummary(0, 0, 0, 0, 0, emptyList(), false)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val statsList: List<UsageStats> = try {
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val pm = context.packageManager
        val detectedList = mutableListOf<DetectedAppUsage>()

        for (stats in statsList) {
            if (stats.totalTimeInForeground > 60 * 1000L) { // at least 1 minute
                val pkg = stats.packageName
                if (pkg == "android" || pkg.startsWith("com.android.systemui") || pkg == context.packageName) {
                    continue
                }

                val appName = try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    cleanPackageToName(pkg)
                }

                val category = categorizePackage(pkg, appName)
                detectedList.add(
                    DetectedAppUsage(
                        packageName = pkg,
                        appName = appName,
                        category = category,
                        totalTimeInForegroundMs = stats.totalTimeInForeground,
                        lastTimeUsedMs = stats.lastTimeUsed
                    )
                )
            }
        }

        val sortedApps = detectedList.sortedByDescending { it.totalTimeInForegroundMs }
        val totalScreenMinutes = sortedApps.sumOf { it.durationMinutes }
        val socialMinutes = sortedApps.filter { it.category == "Social" }.sumOf { it.durationMinutes }
        val aiMinutes = sortedApps.filter { it.category == "AI Tools" }.sumOf { it.durationMinutes }
        val utilityMinutes = sortedApps.filter { it.category == "Utility" || it.category == "Productivity" }.sumOf { it.durationMinutes }
        val entertainmentMinutes = sortedApps.filter { it.category == "Karaoke & Media" }.sumOf { it.durationMinutes }

        return DailyActivitySummary(
            totalScreenTimeMinutes = totalScreenMinutes,
            socialTimeMinutes = socialMinutes,
            aiTimeMinutes = aiMinutes,
            utilityTimeMinutes = utilityMinutes,
            entertainmentTimeMinutes = entertainmentMinutes,
            topApps = sortedApps.take(15),
            isPermissionGranted = true
        )
    }

    private fun cleanPackageToName(pkg: String): String {
        val last = pkg.substringAfterLast('.')
        return last.replaceFirstChar { it.uppercase() }
    }

    private fun categorizePackage(pkg: String, name: String): String {
        val lowerPkg = pkg.lowercase()
        val lowerName = name.lowercase()

        return when {
            lowerPkg.contains("instagram") || lowerPkg.contains("tiktok") || lowerPkg.contains("musically") ||
            lowerPkg.contains("twitter") || lowerPkg.contains("facebook") || lowerPkg.contains("whatsapp") ||
            lowerPkg.contains("telegram") || lowerPkg.contains("reddit") || lowerPkg.contains("discord") ||
            lowerPkg.contains("threads") || lowerPkg.contains("bereal") || lowerPkg.contains("snapchat") -> "Social"

            lowerPkg.contains("openai") || lowerPkg.contains("chatgpt") || lowerPkg.contains("bard") ||
            lowerPkg.contains("gemini") || lowerPkg.contains("claude") || lowerPkg.contains("copilot") ||
            lowerPkg.contains("perplexity") || lowerPkg.contains("poe") || lowerPkg.contains("deepseek") ||
            lowerName.contains("gpt") || lowerName.contains("ai") -> "AI Tools"

            lowerPkg.contains("youtube") || lowerPkg.contains("smule") || lowerPkg.contains("spotify") ||
            lowerPkg.contains("netflix") || lowerPkg.contains("karaoke") || lowerPkg.contains("sing") -> "Karaoke & Media"

            lowerPkg.contains("notion") || lowerPkg.contains("canva") || lowerPkg.contains("camscanner") ||
            lowerPkg.contains("duolingo") || lowerPkg.contains("forest") || lowerPkg.contains("docs") ||
            lowerPkg.contains("drive") || lowerPkg.contains("shazam") || lowerPkg.contains("habit") -> "Utility"

            else -> "Productivity"
        }
    }
}
