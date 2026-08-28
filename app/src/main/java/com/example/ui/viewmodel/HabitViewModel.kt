package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.remote.AICoachingRepository
import com.example.data.repository.HabitRepository
import com.example.notification.HabitNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MilestoneCelebrationData(
    val title: String,
    val streakDays: Int,
    val xpEarned: Int,
    val badgeTitle: String? = null
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = HabitRepository(database)
    private val aiRepository = AICoachingRepository()
    private val notificationManager = HabitNotificationManager(application)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = dateFormat.format(Date())

    val allHabits: StateFlow<List<HabitEntity>> = repository.allActiveHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val badges: StateFlow<List<BadgeEntity>> = repository.allBadges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<HabitLogEntity>> = repository.allLogs
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

    // Map of today's completed habit logs: habitId -> HabitLogEntity
    val todayLogsMap: StateFlow<Map<Long, HabitLogEntity>> = repository.getLogsForDate(todayDateString)
        .combine(allHabits) { logs, _ ->
            logs.associateBy { it.habitId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
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
                        badgeTitle = if (newStreak == 7) "7-Day Champion" else if (newStreak == 21) "21-Day Habit Transformer" else "Momentum Booster"
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
            _snackbarMessage.value = "Habit '${title.trim()}' created! 🚀"
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

    fun sendChatMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isGeneratingAI.value) return

        viewModelScope.launch {
            _isGeneratingAI.value = true
            // Save user message to database
            repository.sendChatMessage(trimmed, sender = "user")

            val active = allHabits.value
            val profile = userProfile.value
            val history = chatMessages.value

            val reply = aiRepository.getCoachChatResponse(
                userMessage = trimmed,
                recentHistory = history,
                activeHabits = active,
                userGoal = profile?.primaryGoal ?: "Consistency"
            )

            // Save AI reply to database
            repository.sendChatMessage(reply, sender = "coach")
            _isGeneratingAI.value = false
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
}
