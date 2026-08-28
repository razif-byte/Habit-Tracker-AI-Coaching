package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitRepository(private val database: AppDatabase) {

    private val habitDao = database.habitDao()
    private val habitLogDao = database.habitLogDao()
    private val badgeDao = database.badgeDao()
    private val userProfileDao = database.userProfileDao()
    private val chatDao = database.chatDao()

    val allActiveHabits: Flow<List<HabitEntity>> = habitDao.getAllActiveHabits()
    val allBadges: Flow<List<BadgeEntity>> = badgeDao.getAllBadges()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    val chatMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()
    val allLogs: Flow<List<HabitLogEntity>> = habitLogDao.getAllLogs()

    fun getLogsForDate(dateString: String): Flow<List<HabitLogEntity>> {
        return habitLogDao.getLogsForDate(dateString)
    }

    fun getLogsSinceDate(startDateString: String): Flow<List<HabitLogEntity>> {
        return habitLogDao.getLogsSinceDate(startDateString)
    }

    suspend fun insertHabit(habit: HabitEntity): Long {
        val id = habitDao.insertHabit(habit)
        // Check if first habit badge should be unlocked
        badgeDao.unlockBadge("first_habit")
        return id
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteHabitById(habitId)
        habitLogDao.deleteLogsForHabit(habitId)
    }

    suspend fun toggleHabitCompletion(
        habit: HabitEntity,
        dateString: String,
        targetCount: Int = habit.targetCount
    ): Boolean {
        val existingLog = habitLogDao.getLog(habit.id, dateString)
        val nowCompleted: Boolean

        if (existingLog != null && existingLog.isCompleted) {
            // Uncheck
            habitLogDao.deleteLog(habit.id, dateString)
            nowCompleted = false
        } else {
            // Complete
            habitLogDao.insertOrUpdateLog(
                HabitLogEntity(
                    habitId = habit.id,
                    dateString = dateString,
                    count = targetCount,
                    isCompleted = true,
                    completedAt = System.currentTimeMillis()
                )
            )
            nowCompleted = true
            // Award XP
            userProfileDao.addXpAndCompletion(50)
        }

        // Recalculate streak for this habit
        recalculateHabitStreak(habit.id)

        // Check and evaluate badges
        checkAndUnlockBadges()

        return nowCompleted
    }

    private suspend fun recalculateHabitStreak(habitId: Long) {
        val habit = habitDao.getHabitById(habitId) ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        var currentStreak = 0
        var checkDate = Date()
        calendar.time = checkDate

        // Check if completed today
        val todayStr = dateFormat.format(calendar.time)
        val todayLog = habitLogDao.getLog(habitId, todayStr)

        if (todayLog != null && todayLog.isCompleted) {
            currentStreak++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Check yesterday
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = dateFormat.format(calendar.time)
            val yesterdayLog = habitLogDao.getLog(habitId, yesterdayStr)
            if (yesterdayLog != null && yesterdayLog.isCompleted) {
                currentStreak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                currentStreak = 0
            }
        }

        if (currentStreak > 0) {
            while (true) {
                val dateStr = dateFormat.format(calendar.time)
                val log = habitLogDao.getLog(habitId, dateStr)
                if (log != null && log.isCompleted) {
                    currentStreak++
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
        }

        val bestStreak = maxOf(habit.bestStreak, currentStreak)
        val totalCompletions = habit.totalCompletions + (if (currentStreak > habit.currentStreak) 1 else 0)

        habitDao.updateStreakStats(habitId, currentStreak, bestStreak, totalCompletions)
    }

    suspend fun checkAndUnlockBadges() {
        val totalLogs = habitLogDao.getTotalCompletedLogsCount()
        val habits = database.habitDao().getHabitById(1L) // quick check
        val profile = userProfileDao.getUserProfileOnce()

        // Streak badges
        if (totalLogs >= 1) badgeDao.unlockBadge("first_habit")
        if (totalLogs >= 3) badgeDao.unlockBadge("streak_3")
        if (totalLogs >= 7) badgeDao.unlockBadge("streak_7")
        if (totalLogs >= 21) badgeDao.unlockBadge("streak_21")
        if (totalLogs >= 100) badgeDao.unlockBadge("century_club")

        // Level update based on XP
        if (profile != null) {
            val calculatedLevel = (profile.xp / 250) + 1
            if (calculatedLevel != profile.level) {
                userProfileDao.updateLevel(calculatedLevel)
            }
        }
    }

    suspend fun sendChatMessage(message: String, sender: String, suggestionType: String = "general"): Long {
        return chatDao.insertMessage(
            ChatMessageEntity(
                sender = sender,
                message = message,
                suggestionType = suggestionType
            )
        )
    }

    suspend fun clearChat() {
        chatDao.clearChatHistory()
    }

    suspend fun updateSubscription(isPremium: Boolean, plan: String, expiry: Long?) {
        userProfileDao.updateSubscription(isPremium, plan, expiry)
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        userProfileDao.updateProfile(profile)
    }

    suspend fun setDarkMode(isDark: Boolean) {
        userProfileDao.updateDarkMode(isDark)
    }
}
