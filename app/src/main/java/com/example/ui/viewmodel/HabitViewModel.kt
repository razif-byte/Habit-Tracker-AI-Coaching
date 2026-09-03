package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountabilityGroupEntity
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.GroupMemberEntity
import com.example.data.local.entity.GroupMessageEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.MoodLogEntity
import com.example.data.local.entity.TrendingLinkEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.remote.AICoachingRepository
import com.example.data.remote.AIEngineType
import com.example.data.repository.HabitRepository
import com.example.data.repository.TrendingRepository
import com.example.notification.HabitNotificationManager
import com.example.service.OverlayBubbleService
import com.example.util.AppActivityTracker
import com.example.util.AppIntegrationHelper
import com.example.util.CoachPersona
import com.example.util.DailyActivitySummary
import com.example.util.GeminiLiveVoiceManager
import com.example.util.LiveVoiceState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MilestoneCelebrationData(
    val title: String,
    val streakDays: Int,
    val xpEarned: Int,
    val badgeTitle: String? = null
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val initials: String,
    val avatarColorHex: String,
    val weeklyXp: Int,
    val currentStreak: Int,
    val isCurrentUser: Boolean,
    val league: String = "Gold", // Diamond, Platinum, Gold, Silver, Bronze
    val change: String = "+1"
)

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = HabitRepository(database)
    private val trendingRepository = TrendingRepository(database.trendingLinkDao())
    private val aiRepository = AICoachingRepository()
    private val notificationManager = HabitNotificationManager(application)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = dateFormat.format(Date())

    val allHabits: StateFlow<List<HabitEntity>> = repository.allActiveHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Trending Links & Apps State
    val allTrendingLinks: StateFlow<List<TrendingLinkEntity>> = trendingRepository.allLinks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTrendingCategory = MutableStateFlow("ALL")
    val selectedTrendingCategory: StateFlow<String> = _selectedTrendingCategory.asStateFlow()

    private val _trendingSearchQuery = MutableStateFlow("")
    val trendingSearchQuery: StateFlow<String> = _trendingSearchQuery.asStateFlow()

    private val _isFeedRefreshing = MutableStateFlow(false)
    val isFeedRefreshing: StateFlow<Boolean> = _isFeedRefreshing.asStateFlow()

    private val _lastFeedSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastFeedSyncTime: StateFlow<Long> = _lastFeedSyncTime.asStateFlow()

    private val _feedSyncStatus = MutableStateFlow<String?>(null)
    val feedSyncStatus: StateFlow<String?> = _feedSyncStatus.asStateFlow()

    val filteredTrendingLinks: StateFlow<List<TrendingLinkEntity>> = combine(
        allTrendingLinks,
        _selectedTrendingCategory,
        _trendingSearchQuery
    ) { links, cat, query ->
        links.filter { item ->
            val matchCategory = if (cat == "ALL") true else item.category.equals(cat, ignoreCase = true)
            val matchQuery = if (query.isBlank()) true else {
                item.title.contains(query, ignoreCase = true) ||
                item.subtitle.contains(query, ignoreCase = true)
            }
            matchCategory && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Activity & System Overlay State
    private val _dailyActivitySummary = MutableStateFlow<DailyActivitySummary?>(null)
    val dailyActivitySummary: StateFlow<DailyActivitySummary?> = _dailyActivitySummary.asStateFlow()

    private val _isOverlayActive = MutableStateFlow(OverlayBubbleService.isRunning)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    val badges: StateFlow<List<BadgeEntity>> = repository.allBadges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<HabitLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMoodLogs: StateFlow<List<MoodLogEntity>> = repository.allMoodLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMood: StateFlow<MoodLogEntity?> = repository.getTodayMood(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Social Accountability Groups
    val allGroups: StateFlow<List<AccountabilityGroupEntity>> = repository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedGroupId = MutableStateFlow<Long>(1L)
    val selectedGroupId: StateFlow<Long> = _selectedGroupId.asStateFlow()

    val currentGroupMembers: StateFlow<List<GroupMemberEntity>> = _selectedGroupId
        .flatMapLatest { groupId -> repository.getGroupMembers(groupId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentGroupMessages: StateFlow<List<GroupMessageEntity>> = _selectedGroupId
        .flatMapLatest { groupId -> repository.getGroupMessages(groupId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isGeneratingAI = MutableStateFlow(false)
    val isGeneratingAI: StateFlow<Boolean> = _isGeneratingAI.asStateFlow()

    private val _aiAnalysisReport = MutableStateFlow<String?>(null)
    val aiAnalysisReport: StateFlow<String?> = _aiAnalysisReport.asStateFlow()

    private val _goalBreakdownResult = MutableStateFlow<String?>(null)
    val goalBreakdownResult: StateFlow<String?> = _goalBreakdownResult.asStateFlow()

    private val _activeMilestone = MutableStateFlow<MilestoneCelebrationData?>(null)
    val activeMilestone: StateFlow<MilestoneCelebrationData?> = _activeMilestone.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // --- AI Engine & Gemini Live Voice Integration ---
    private val sharedPrefs = application.getSharedPreferences("ai_coach_prefs", Context.MODE_PRIVATE)

    private val _selectedAIEngine = MutableStateFlow(
        AIEngineType.fromId(sharedPrefs.getString("selected_engine", AIEngineType.GEMINI_FLASH.id))
    )
    val selectedAIEngine: StateFlow<AIEngineType> = _selectedAIEngine.asStateFlow()

    private val _customOpenAiKey = MutableStateFlow(sharedPrefs.getString("openai_key", "") ?: "")
    val customOpenAiKey: StateFlow<String> = _customOpenAiKey.asStateFlow()

    private val _customGeminiKey = MutableStateFlow(sharedPrefs.getString("gemini_key", "") ?: "")
    val customGeminiKey: StateFlow<String> = _customGeminiKey.asStateFlow()

    val liveVoiceManager = GeminiLiveVoiceManager(application)
    val liveVoiceState: StateFlow<LiveVoiceState> = liveVoiceManager.voiceState
    val liveAudioRms: StateFlow<Float> = liveVoiceManager.audioRmsLevel
    val liveInterimTranscript: StateFlow<String> = liveVoiceManager.interimTranscript
    val selectedPersona: StateFlow<CoachPersona> = liveVoiceManager.selectedPersona

    private val _isLiveSessionOpen = MutableStateFlow(false)
    val isLiveSessionOpen: StateFlow<Boolean> = _isLiveSessionOpen.asStateFlow()

    init {
        viewModelScope.launch {
            trendingRepository.checkAndSeedInitialTrendingLinks()
        }
        refreshAppActivityUsage()

        liveVoiceManager.onSpeechRecognized = { spokenText ->
            sendChatMessage(spokenText, engineOverride = AIEngineType.GEMINI_LIVE)
        }
        liveVoiceManager.onSpeechError = { errorMsg ->
            _snackbarMessage.value = errorMsg
        }
    }

    // Map of today's completed habit logs: habitId -> HabitLogEntity
    val todayLogsMap: StateFlow<Map<Long, HabitLogEntity>> = repository.getLogsForDate(todayDateString)
        .combine(allHabits) { logs, _ ->
            logs.associateBy { it.habitId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedGroup(groupId: Long) {
        _selectedGroupId.value = groupId
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun dismissMilestone() {
        _activeMilestone.value = null
    }

    fun toggleHabit(habit: HabitEntity) {
        viewModelScope.launch {
            val wasCompleted = repository.toggleHabitCompletion(habit, todayDateString)
            if (wasCompleted) {
                val newStreak = habit.currentStreak + 1
                _snackbarMessage.value = "Great job! Completed '${habit.title}' (+50 XP)"
                
                // Milestone alerts on key streak intervals
                if (newStreak in listOf(3, 7, 14, 21, 30, 60, 100)) {
                    val celebration = MilestoneCelebrationData(
                        title = habit.title,
                        streakDays = newStreak,
                        xpEarned = 100,
                        badgeTitle = when (newStreak) {
                            3 -> "3-Day Momentum"
                            7 -> "7-Day Champion"
                            14 -> "Fortnight Master"
                            21 -> "21-Day Habit Transformer"
                            30 -> "30-Day Legend"
                            else -> "Momentum Booster"
                        }
                    )
                    _activeMilestone.value = celebration
                    notificationManager.sendMilestoneCelebration(habit.title, newStreak, 100)
                }
            } else {
                _snackbarMessage.value = "Unchecked '${habit.title}'"
            }
        }
    }

    fun addHabit(
        title: String,
        description: String,
        category: String,
        targetCount: Int,
        unit: String,
        reminderTime: String,
        frequencyDays: String,
        iconName: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            val profile = userProfile.value
            val activeCount = allHabits.value.size
            if (profile?.isPremium != true && activeCount >= 5) {
                _snackbarMessage.value = "Free limit reached (5 habits). Upgrade to Pro for unlimited habits!"
                return@launch
            }

            val newHabit = HabitEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                targetCount = if (targetCount > 0) targetCount else 1,
                unit = unit,
                reminderTime = reminderTime,
                frequencyDays = frequencyDays,
                iconName = iconName,
                colorHex = colorHex
            )
            repository.insertHabit(newHabit)
            _snackbarMessage.value = "Habit '${title.trim()}' created! 🚀 (+50 XP)"
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            _snackbarMessage.value = "Habit updated!"
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            _snackbarMessage.value = "Habit deleted"
        }
    }

    // Mood Tracking & Correlation
    fun logDailyMood(
        moodScore: Int,
        moodLabel: String,
        moodEmoji: String,
        energyLevel: Int,
        stressLevel: Int,
        tags: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.logMood(
                dateString = todayDateString,
                moodScore = moodScore,
                moodLabel = moodLabel,
                moodEmoji = moodEmoji,
                energyLevel = energyLevel,
                stressLevel = stressLevel,
                tags = tags,
                notes = notes
            )
            _snackbarMessage.value = "Mood logged: $moodEmoji $moodLabel (+30 XP) ✨"
        }
    }

    // Social Accountability Actions
    fun sendGroupChatMessage(messageText: String) {
        val trimmed = messageText.trim()
        if (trimmed.isBlank()) return
        val currentGroup = _selectedGroupId.value
        val user = userProfile.value
        viewModelScope.launch {
            repository.sendGroupMessage(
                groupId = currentGroup,
                senderName = user?.name ?: "Alex Rivera",
                senderAvatar = (user?.name ?: "Alex").take(2).uppercase(),
                message = trimmed,
                messageType = "chat",
                isCurrentUser = true
            )
            _snackbarMessage.value = "Message sent to squad! (+10 XP)"
        }
    }

    fun sendCheerToPeer(memberName: String, cheerEmoji: String, cheerPhrase: String) {
        val currentGroup = _selectedGroupId.value
        val user = userProfile.value
        viewModelScope.launch {
            val messageText = "${user?.name ?: "Alex"} sent a cheer to $memberName: $cheerPhrase $cheerEmoji"
            repository.sendGroupMessage(
                groupId = currentGroup,
                senderName = user?.name ?: "Alex Rivera",
                senderAvatar = (user?.name ?: "Alex").take(2).uppercase(),
                message = messageText,
                messageType = "cheer",
                isCurrentUser = true
            )
            _snackbarMessage.value = "Cheer sent to $memberName! $cheerEmoji"
        }
    }

    fun joinAccountabilityGroup(code: String) {
        val trimmed = code.trim().uppercase()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            repository.joinGroupByCode(trimmed)
            _snackbarMessage.value = "Joined squad with code $trimmed! 🤝"
        }
    }

    fun createAccountabilityGroup(name: String, description: String, category: String, code: String) {
        if (name.isBlank() || code.isBlank()) return
        viewModelScope.launch {
            val id = repository.createGroup(name, description, category, code)
            _selectedGroupId.value = id
            _snackbarMessage.value = "Created squad '$name'! 🎉"
        }
    }

    fun setAIEngine(engine: AIEngineType) {
        _selectedAIEngine.value = engine
        sharedPrefs.edit().putString("selected_engine", engine.id).apply()
        _snackbarMessage.value = "Enjin AI ditukar kepada ${engine.displayName}"
    }

    fun saveApiKeys(openAiKey: String, geminiKey: String) {
        _customOpenAiKey.value = openAiKey.trim()
        _customGeminiKey.value = geminiKey.trim()
        sharedPrefs.edit()
            .putString("openai_key", openAiKey.trim())
            .putString("gemini_key", geminiKey.trim())
            .apply()
        _snackbarMessage.value = "Tetapan Kunci API dikemas kini!"
    }

    fun setPersona(persona: CoachPersona) {
        liveVoiceManager.setPersona(persona)
        _snackbarMessage.value = "Personaliti suara: ${persona.title}"
    }

    fun openLiveSession() {
        _isLiveSessionOpen.value = true
        _selectedAIEngine.value = AIEngineType.GEMINI_LIVE
    }

    fun closeLiveSession() {
        _isLiveSessionOpen.value = false
        liveVoiceManager.stopListening()
        liveVoiceManager.stopSpeaking()
    }

    fun startListening() {
        liveVoiceManager.startListening()
    }

    fun stopListening() {
        liveVoiceManager.stopListening()
    }

    fun speakCoachMessage(text: String) {
        liveVoiceManager.speak(text)
    }

    fun stopSpeaking() {
        liveVoiceManager.stopSpeaking()
    }

    fun sendChatMessage(text: String, engineOverride: AIEngineType? = null) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isGeneratingAI.value) return

        val activeEngine = engineOverride ?: _selectedAIEngine.value

        viewModelScope.launch {
            _isGeneratingAI.value = true
            repository.sendChatMessage(trimmed, sender = "user", suggestionType = "user")

            val active = allHabits.value
            val profile = userProfile.value
            val history = chatMessages.value

            val reply = aiRepository.getCoachChatResponse(
                userMessage = trimmed,
                recentHistory = history,
                activeHabits = active,
                userGoal = profile?.primaryGoal ?: "Consistency",
                engine = activeEngine,
                customGeminiKey = _customGeminiKey.value,
                customOpenAiKey = _customOpenAiKey.value
            )

            repository.sendChatMessage(reply, sender = "coach", suggestionType = activeEngine.id)
            _isGeneratingAI.value = false

            // Automatically speak reply aloud if in Gemini Live mode or Live Session is active
            if (activeEngine == AIEngineType.GEMINI_LIVE || _isLiveSessionOpen.value) {
                liveVoiceManager.speak(reply)
            }
        }
    }

    fun requestDeepAIAnalysis() {
        viewModelScope.launch {
            _isGeneratingAI.value = true
            val active = allHabits.value
            val total = userProfile.value?.totalHabitsCompleted ?: 10
            val name = userProfile.value?.name ?: "User"

            val report = aiRepository.generateDeepHabitAnalysis(active, total, name)
            _aiAnalysisReport.value = report
            _isGeneratingAI.value = false
        }
    }

    fun requestGoalBreakdown(goal: String) {
        if (goal.isBlank()) return
        viewModelScope.launch {
            _isGeneratingAI.value = true
            val result = aiRepository.generateGoalBreakdown(goal)
            _goalBreakdownResult.value = result
            _isGeneratingAI.value = false
        }
    }

    fun testPushReminder(habitTitle: String, reminderTime: String) {
        notificationManager.sendHabitReminder(habitTitle, reminderTime)
        _snackbarMessage.value = "Notification sent for '$habitTitle'!"
    }

    fun testMilestoneAlert() {
        val sampleHabit = allHabits.value.firstOrNull()?.title ?: "Morning Routine"
        val streak = 7
        _activeMilestone.value = MilestoneCelebrationData(
            title = sampleHabit,
            streakDays = streak,
            xpEarned = 250,
            badgeTitle = "7-Day Champion"
        )
        notificationManager.sendMilestoneCelebration(sampleHabit, streak, 250)
    }

    fun updateSubscription(isPremium: Boolean, plan: String) {
        viewModelScope.launch {
            val expiry = if (isPremium) System.currentTimeMillis() + 30L * 86400000L else null
            repository.updateSubscription(isPremium, plan, expiry)
            _snackbarMessage.value = if (isPremium) "🌟 Welcome to Habit Coach Pro!" else "Subscription updated"
        }
    }

    fun updateProfile(name: String, age: Int, primaryGoal: String, experienceLevel: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val updated = current.copy(
                name = name,
                age = age,
                primaryGoal = primaryGoal,
                experienceLevel = experienceLevel
            )
            repository.updateProfile(updated)
            _snackbarMessage.value = "Profile updated!"
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            repository.setDarkMode(!current.isDarkMode)
        }
    }

    // --- Trending Links & Apps Functions ---
    fun setTrendingCategory(category: String) {
        _selectedTrendingCategory.value = category
    }

    fun setTrendingSearchQuery(query: String) {
        _trendingSearchQuery.value = query
    }

    fun refreshTrendingFeeds(category: String? = null) {
        if (_isFeedRefreshing.value) return
        viewModelScope.launch {
            _isFeedRefreshing.value = true
            _feedSyncStatus.value = "Memuat turun suapan RSS & API..."
            try {
                val targetCat = category ?: _selectedTrendingCategory.value
                val result = trendingRepository.fetchAndSyncFeeds(targetCat)
                _lastFeedSyncTime.value = result.timestamp
                _feedSyncStatus.value = result.message
                _snackbarMessage.value = result.message
            } catch (e: Exception) {
                _feedSyncStatus.value = "Gagal memuat turun: ${e.message}"
            } finally {
                _isFeedRefreshing.value = false
            }
        }
    }

    fun addTrendingLink(
        title: String,
        subtitle: String,
        url: String,
        category: String,
        platform: String,
        packageName: String = ""
    ) {
        if (title.isBlank() || url.isBlank()) {
            _snackbarMessage.value = "Sila masukkan tajuk dan pautan URL"
            return
        }
        viewModelScope.launch {
            val newLink = TrendingLinkEntity(
                title = title.trim(),
                subtitle = subtitle.trim(),
                url = url.trim(),
                category = category,
                platform = platform,
                packageName = packageName.trim(),
                isCustom = true
            )
            trendingRepository.insertLink(newLink)
            _snackbarMessage.value = "Pautan '$title' berjaya ditambah!"
        }
    }

    fun toggleFavoriteTrending(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            trendingRepository.updateFavorite(id, isFavorite)
        }
    }

    fun recordTrendingClick(id: Long) {
        viewModelScope.launch {
            trendingRepository.recordClick(id)
        }
    }

    fun deleteTrendingLink(link: TrendingLinkEntity) {
        viewModelScope.launch {
            trendingRepository.deleteLink(link)
            _snackbarMessage.value = "Pautan dipadam"
        }
    }

    fun createHabitFromTrending(title: String, category: String, iconName: String) {
        viewModelScope.launch {
            val habit = HabitEntity(
                title = title,
                description = "Tabiat berasaskan inspirasi trending",
                category = category,
                targetCount = 1,
                unit = "kali",
                reminderTime = "20:00",
                iconName = iconName,
                colorHex = "#6750A4"
            )
            repository.insertHabit(habit)
            _snackbarMessage.value = "Tabiat '$title' berjaya ditambah ke senarai harian!"
        }
    }

    // --- App Activity Monitoring & System Overlay ---
    fun refreshAppActivityUsage() {
        viewModelScope.launch {
            val summary = AppActivityTracker.getTodayAppUsage(getApplication())
            _dailyActivitySummary.value = summary
            _isOverlayActive.value = OverlayBubbleService.isRunning
        }
    }

    fun toggleOverlayService(enable: Boolean) {
        AppIntegrationHelper.toggleOverlayService(getApplication(), enable)
        _isOverlayActive.value = enable
        _snackbarMessage.value = if (enable) "Floating Overlay diaktifkan!" else "Floating Overlay ditutup"
    }

    override fun onCleared() {
        super.onCleared()
        liveVoiceManager.cleanup()
    }
}
