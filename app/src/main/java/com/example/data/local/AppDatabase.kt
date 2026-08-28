package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BadgeDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.HabitLogDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        ChatMessageEntity::class,
        BadgeEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun chatDao(): ChatDao
    abstract fun badgeDao(): BadgeDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_coach_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val userProfileDao = database.userProfileDao()
            val habitDao = database.habitDao()
            val habitLogDao = database.habitLogDao()
            val badgeDao = database.badgeDao()
            val chatDao = database.chatDao()

            // 1. Initial User Profile
            userProfileDao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    name = "Alex Rivera",
                    email = "alex.rivera@example.com",
                    age = 27,
                    primaryGoal = "Master morning routines & daily fitness",
                    experienceLevel = "Intermediate",
                    isPremium = false,
                    subscriptionPlan = "FREE",
                    xp = 350,
                    level = 2,
                    totalHabitsCompleted = 18,
                    longestOverallStreak = 6,
                    notificationsEnabled = true,
                    morningReminderTime = "07:00",
                    eveningReviewTime = "21:30",
                    isDarkMode = true
                )
            )

            // 2. Initial Badges
            val badges = listOf(
                BadgeEntity(
                    badgeKey = "first_habit",
                    title = "First Spark",
                    description = "Created and tracked your first positive habit",
                    iconName = "Bolt",
                    xpReward = 50,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 86400000L * 5,
                    progress = 1.0f
                ),
                BadgeEntity(
                    badgeKey = "streak_3",
                    title = "3-Day Momentum",
                    description = "Maintained a streak for 3 consecutive days",
                    iconName = "LocalFireDepartment",
                    xpReward = 100,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 86400000L * 2,
                    progress = 1.0f
                ),
                BadgeEntity(
                    badgeKey = "streak_7",
                    title = "7-Day Champion",
                    description = "Locked in a 7-day unbroken habit cycle",
                    iconName = "EmojiEvents",
                    xpReward = 250,
                    isUnlocked = false,
                    unlockedAt = null,
                    progress = 0.85f
                ),
                BadgeEntity(
                    badgeKey = "streak_21",
                    title = "21-Day Habit Transformer",
                    description = "Rewired neural pathways through 21 days of persistence",
                    iconName = "Psychology",
                    xpReward = 500,
                    isUnlocked = false,
                    unlockedAt = null,
                    progress = 0.28f
                ),
                BadgeEntity(
                    badgeKey = "ai_explorer",
                    title = "AI Coaching Apprentice",
                    description = "Completed an AI coaching consultation & goal breakdown",
                    iconName = "SmartToy",
                    xpReward = 150,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 86400000L,
                    progress = 1.0f
                ),
                BadgeEntity(
                    badgeKey = "perfect_week",
                    title = "Flawless Execution",
                    description = "Completed all daily habits 7 days in a row",
                    iconName = "Verified",
                    xpReward = 400,
                    isUnlocked = false,
                    unlockedAt = null,
                    progress = 0.6f
                ),
                BadgeEntity(
                    badgeKey = "century_club",
                    title = "Century Titan (100 Check-ins)",
                    description = "Achieved 100 total habit check-ins",
                    iconName = "Diamond",
                    xpReward = 1000,
                    isUnlocked = false,
                    unlockedAt = null,
                    progress = 0.18f
                )
            )
            badgeDao.insertAll(badges)

            // 3. Initial Sample Habits
            val initialHabits = listOf(
                HabitEntity(
                    id = 1L,
                    title = "Drink 2L Water",
                    description = "Hydrate properly throughout the workday for energy",
                    category = "Health",
                    targetCount = 8,
                    unit = "glasses",
                    reminderTime = "08:30",
                    frequencyDays = "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
                    iconName = "LocalDrink",
                    colorHex = "#06B6D4",
                    currentStreak = 6,
                    bestStreak = 6,
                    totalCompletions = 12
                ),
                HabitEntity(
                    id = 2L,
                    title = "Morning Workout",
                    description = "30-min HIIT or resistance training session",
                    category = "Fitness",
                    targetCount = 30,
                    unit = "mins",
                    reminderTime = "07:00",
                    frequencyDays = "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
                    iconName = "FitnessCenter",
                    colorHex = "#10B981",
                    currentStreak = 4,
                    bestStreak = 5,
                    totalCompletions = 8
                ),
                HabitEntity(
                    id = 3L,
                    title = "Read 15 Pages",
                    description = "Deep reading on non-fiction or skill development",
                    category = "Learning",
                    targetCount = 15,
                    unit = "pages",
                    reminderTime = "21:00",
                    frequencyDays = "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
                    iconName = "MenuBook",
                    colorHex = "#8B5CF6",
                    currentStreak = 3,
                    bestStreak = 4,
                    totalCompletions = 6
                ),
                HabitEntity(
                    id = 4L,
                    title = "Mindful Meditation",
                    description = "10 minutes diaphragmatic breathing & mental reset",
                    category = "Mindfulness",
                    targetCount = 10,
                    unit = "mins",
                    reminderTime = "07:45",
                    frequencyDays = "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
                    iconName = "SelfImprovement",
                    colorHex = "#6366F1",
                    currentStreak = 2,
                    bestStreak = 3,
                    totalCompletions = 5
                )
            )
            habitDao.insertAll(initialHabits)

            // 4. Sample Recent Logs (for the past few days to showcase charts)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            
            // Today's date
            val todayStr = dateFormat.format(cal.time)
            habitLogDao.insertOrUpdateLog(HabitLogEntity(habitId = 1L, dateString = todayStr, count = 8, isCompleted = true))
            habitLogDao.insertOrUpdateLog(HabitLogEntity(habitId = 2L, dateString = todayStr, count = 30, isCompleted = true))

            // Past 6 days
            for (i in 1..6) {
                cal.time = Date()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val pastDateStr = dateFormat.format(cal.time)
                
                // Habit 1 was completed all 6 past days
                habitLogDao.insertOrUpdateLog(HabitLogEntity(habitId = 1L, dateString = pastDateStr, count = 8, isCompleted = true))
                
                // Habit 2 completed 3 of past days
                if (i in listOf(1, 2, 3)) {
                    habitLogDao.insertOrUpdateLog(HabitLogEntity(habitId = 2L, dateString = pastDateStr, count = 30, isCompleted = true))
                }
                
                // Habit 3 completed 2 of past days
                if (i in listOf(1, 2)) {
                    habitLogDao.insertOrUpdateLog(HabitLogEntity(habitId = 3L, dateString = pastDateStr, count = 15, isCompleted = true))
                }
            }

            // 5. Initial Welcome Chat from AI Coach
            chatDao.insertMessage(
                ChatMessageEntity(
                    sender = "coach",
                    message = "Hello Alex! I am Coach Zenith, your AI Habit & Behavior Mentor. 🎯\n\nI analyze your consistency patterns, troubleshoot missed streaks, and help you stack atomic habits that actually stick. How can I empower your routine today?",
                    suggestionType = "daily_motivate"
                )
            )
        }
    }
}
