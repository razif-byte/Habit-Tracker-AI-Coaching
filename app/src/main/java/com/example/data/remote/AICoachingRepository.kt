package com.example.data.remote

import com.example.BuildConfig
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.HabitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AICoachingRepository(
    private val apiService: GeminiApiService = RetrofitClient.geminiService
) {

    private val systemPrompt = """
        You are Coach Zenith, an elite behavioral scientist, habit architect, and empathetic AI coach.
        You specialize in BJ Fogg's Tiny Habits, James Clear's Atomic Habits, and neuroplasticity-based habit retention.
        Your tone is encouraging, razor-sharp, practical, and highly actionable.
        Always provide structured, clear advice (using bullet points and bold headers when helpful).
        When the user mentions feeling lazy or losing a streak, never shame them; provide a 2-Minute Rule reset.
        Keep responses concise (150-250 words) so they are easy to read on mobile.
    """.trimIndent()

    suspend fun getCoachChatResponse(
        userMessage: String,
        recentHistory: List<ChatMessageEntity>,
        activeHabits: List<HabitEntity>,
        userGoal: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateOfflineFallbackReply(userMessage, activeHabits, userGoal)
        }

        try {
            val habitSummary = if (activeHabits.isNotEmpty()) {
                "User's Active Habits:\n" + activeHabits.joinToString("\n") { 
                    "- ${it.title} (${it.category}, Current Streak: ${it.currentStreak} days, Target: ${it.targetCount} ${it.unit})" 
                }
            } else {
                "User has no active habits yet."
            }

            val contextNote = "User Primary Goal: $userGoal\n$habitSummary\n\nUser Question: $userMessage"

            val contents = mutableListOf<GeminiContent>()
            // Include up to 4 past conversation turns
            val historySlice = recentHistory.takeLast(4)
            for (msg in historySlice) {
                val role = if (msg.sender == "user") "user" else "model"
                contents.add(GeminiContent(parts = listOf(GeminiPart(text = msg.message)), role = role))
            }
            contents.add(GeminiContent(parts = listOf(GeminiPart(text = contextNote)), role = "user"))

            val request = GeminiRequest(
                contents = contents,
                generationConfig = GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 800),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
            )

            val response = apiService.generateContent(apiKey, request)
            val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!candidateText.isNullOrBlank()) {
                candidateText.trim()
            } else {
                generateOfflineFallbackReply(userMessage, activeHabits, userGoal)
            }
        } catch (e: Exception) {
            generateOfflineFallbackReply(userMessage, activeHabits, userGoal)
        }
    }

    suspend fun generateDeepHabitAnalysis(
        activeHabits: List<HabitEntity>,
        totalCompletions: Int,
        userName: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            Analyze the following habit routine for user '$userName':
            Total all-time completions: $totalCompletions
            Habits:
            ${activeHabits.joinToString("\n") { "- ${it.title} [${it.category}]: ${it.currentStreak} day streak (Best: ${it.bestStreak})" }}
            
            Provide a 3-part breakdown:
            1. **Strength Pattern**: What's working well.
            2. **Friction Risk**: Potential bottleneck or missed consistency area.
            3. **Habit Stacking Recommendation**: How to anchor these habits together in an unbroken chain (e.g. After X, I will do Y).
        """.trimIndent()

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateOfflineAnalysis(activeHabits, totalCompletions, userName)
        }

        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)), role = "user")),
                generationConfig = GeminiGenerationConfig(temperature = 0.6f, maxOutputTokens = 600),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
            )
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: generateOfflineAnalysis(activeHabits, totalCompletions, userName)
        } catch (e: Exception) {
            generateOfflineAnalysis(activeHabits, totalCompletions, userName)
        }
    }

    suspend fun generateGoalBreakdown(goal: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            The user wants to achieve this goal: "$goal".
            Break this down into 3 concrete, micro-habits using the 2-Minute Rule.
            Format clearly with:
            - Habit Name
            - Recommended Frequency (e.g. Daily, 3x/week)
            - Micro-Starter version (first 2 minutes)
            - How to level it up once consistent
        """.trimIndent()

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateOfflineGoalBreakdown(goal)
        }

        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)), role = "user")),
                generationConfig = GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 600),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
            )
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: generateOfflineGoalBreakdown(goal)
        } catch (e: Exception) {
            generateOfflineGoalBreakdown(goal)
        }
    }

    // High-fidelity fallback expert coaching logic
    private fun generateOfflineFallbackReply(
        query: String,
        activeHabits: List<HabitEntity>,
        userGoal: String
    ): String {
        val lower = query.lowercase()
        return when {
            lower.contains("procrastinat") || lower.contains("lazy") || lower.contains("hard") || lower.contains("unmotivated") -> {
                "💡 **The 2-Minute Reset Protocol**\n\nWhen motivation dips, your prefrontal cortex is experiencing decision friction. Scale your habit down to something impossible to fail:\n\n• If your workout feels daunting, commit only to putting on your shoes and doing 5 pushups.\n• If reading feels heavy, open the book for just 1 page.\n\n*Rule of thumb:* A 2-minute habit done consistently beats a 60-minute habit done once a month."
            }
            lower.contains("streak") || lower.contains("lost") || lower.contains("missed") -> {
                "🔥 **The Never-Miss-Twice Rule**\n\nMissing one day is an accident; missing two days is the start of a new, negative habit.\n\nToday, do the absolute minimum version of your missed habit before sleep. Reclaiming momentum today preserves your identity as a consistent person!"
            }
            lower.contains("stack") || lower.contains("routine") || lower.contains("morning") -> {
                val firstHabit = activeHabits.firstOrNull()?.title ?: "Morning Coffee"
                val secondHabit = activeHabits.getOrNull(1)?.title ?: "Meditation"
                "🔗 **Habit Stacking Formula**\n\nAnchor new behaviors onto existing automated rituals:\n\n1. **Trigger:** Immediately after I finish `$firstHabit`...\n2. **Action:** I will immediately do `$secondHabit`.\n3. **Reward:** Give yourself a quick fist pump or positive mental checkmark!\n\nThis uses existing neural pathways to carry the cognitive load."
            }
            else -> {
                "🎯 **Consistency Strategy for '$userGoal'**\n\n1. **Environment Design:** Make the cues for your good habits obvious and remove friction beforehand (e.g. fill your water bottle the night before).\n2. **Track Without Judgment:** Focus on showing up every single day, even for 60 seconds.\n3. **Identity Shift:** Every check-in is a vote for the person you are becoming!"
            }
        }
    }

    private fun generateOfflineAnalysis(
        activeHabits: List<HabitEntity>,
        totalCompletions: Int,
        userName: String
    ): String {
        val avgStreak = if (activeHabits.isNotEmpty()) activeHabits.map { it.currentStreak }.average().toInt() else 0
        return """
            📊 **AI Behavioral Analysis for $userName**
            
            • **Strength Pattern:** You have built solid momentum with an average active streak of $avgStreak days across your current habits. Total lifetime check-ins: $totalCompletions!
            
            • **Friction Risk:** Evening habits tend to have higher drop-off rates due to willpower depletion. Ensure reminders are set at least 1 hour before you unwind.
            
            • **Habit Stacking Plan:**
              1. **Morning Stack:** Wake up ➔ Hydrate (Drink 2L Water) ➔ 10-min Physical Movement.
              2. **Evening Stack:** Dim lights ➔ 15 Pages Reading ➔ Quick Habit Check-in Review.
        """.trimIndent()
    }

    private fun generateOfflineGoalBreakdown(goal: String): String {
        return """
            🚀 **Atomic Habit Blueprint for: "$goal"**
            
            1. **Habit 1: The Micro-Ignition (Daily)**
               • *Micro-starter:* Spend exactly 2 minutes initiating the core action every morning.
               • *Level up:* Expand to 15-20 minutes once you have a 7-day unbroken streak.
               
            2. **Habit 2: Environment Prep (Daily - Evening)**
               • *Micro-starter:* Lay out your tools/materials the night before.
               • *Level up:* Create a dedicated sacred workspace or zone.
               
            3. **Habit 3: The Reflection Check-in (Weekly)**
               • *Micro-starter:* Review your weekly consistency score every Sunday for 3 minutes.
        """.trimIndent()
    }
}
