package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AccountabilityDao
import com.example.data.local.dao.BadgeDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.HabitLogDao
import com.example.data.local.dao.MoodLogDao
import com.example.data.local.dao.TrendingLinkDao
import com.example.data.local.dao.UserProfileDao
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
import com.example.data.repository.TrendingRepository
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
        UserProfileEntity::class,
        MoodLogEntity::class,
        AccountabilityGroupEntity::class,
        GroupMemberEntity::class,
        GroupMessageEntity::class,
        TrendingLinkEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun chatDao(): ChatDao
    abstract fun badgeDao(): BadgeDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun moodLogDao(): MoodLogDao
    abstract fun accountabilityDao(): AccountabilityDao
    abstract fun trendingLinkDao(): TrendingLinkDao

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
                    .fallbackToDestructiveMigration()
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
            val moodLogDao = database.moodLogDao()
            val accountabilityDao = database.accountabilityDao()

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
                    xp = 780,
                    level = 3,
                    totalHabitsCompleted = 24,
                    longestOverallStreak = 7,
                    notificationsEnabled = true,
                    morningReminderTime = "07:00",
                    eveningReviewTime = "21:30",
                    isDarkMode = true
                )
            )

            // 2. Initial Badges (rich set for gamification milestones)
            val badges = listOf(
                BadgeEntity(
                    badgeKey = "first_habit",
                    title = "First Spark",
                    description = "Created and tracked your first positive habit",
                    iconName = "Bolt",
                    xpReward = 50,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 86400000L * 7,
                    progress = 1.0f
                ),
                BadgeEntity(
                    badgeKey = "streak_3",
                    title = "3-Day Momentum",
                    description = "Maintained a streak for 3 consecutive days",
                    iconName = "LocalFireDepartment",
                    xpReward = 100,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 86400000L * 4,
                    progress = 1.0f
                ),
                BadgeEntity(
                    badgeKey = "streak_7",
                    title = "7-Day Champion",
                    description = "Locked in a 7-day unbroken habit cycle",
                    iconName = "EmojiEvents",
                    xpReward = 250,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 86400000L * 1,
                    progress = 1.0f
                ),
                BadgeEntity(
                    badgeKey = "streak_14",
                    title = "Fortnight Master",
                    description = "Unstoppable discipline for 14 continuous days",
                    iconName = "MilitaryTech",
                    xpReward = 350,
                    isUnlocked = false,
                    unlockedAt = null,
                    progress = 0.50f
                ),
                BadgeEntity(
                    badgeKey = "streak_21",
                    title = "21-Day Habit Transformer",
                    description = "Rewired neural pathways through 21 days of persistence",
                    iconName = "Psychology",
                    xpReward = 500,
                    isUnlocked = false,
                    unlockedAt = null,
                    progress = 0.33f
                ),
                BadgeEntity(
                    badgeKey = "streak_30",
                    title = "30-Day Legend",
                    description = "Iron willpower sustaining a full monthly cycle",
                    iconName = "WorkspacePremium",
                    xpReward = 750,
                    isUnlocked = false,
                    unlockedAt = null,
                    progress = 0.23f
                ),
                BadgeEntity(
                    badgeKey = "mood_master",
                    title = "Mindful Explorer",
                    description = "Logged 7 daily mood check-ins for emotional awareness",
                    iconName = "Mood",
                    xpReward = 200,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 86400000L * 2,
                    progress = 1.0f
                ),
                BadgeEntity(
                    badgeKey = "squad_leader",
                    title = "Social Catalyst",
                    description = "Joined an accountability squad & cheered fellow peers",
                    iconName = "Groups",
                    xpReward = 200,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 86400000L * 3,
                    progress = 1.0f
                ),
                BadgeEntity(
                    badgeKey = "century_club",
                    title = "Century Titan (100 Check-ins)",
                    description = "Achieved 100 total habit check-ins",
                    iconName = "Diamond",
                    xpReward = 1000,
                    isUnlocked = false,
                    unlockedAt = null,
                    progress = 0.24f
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
                    currentStreak = 7,
                    bestStreak = 7,
                    totalCompletions = 14
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
                    currentStreak = 5,
                    bestStreak = 6,
                    totalCompletions = 10
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
                    colorHex = "#6750A4",
                    currentStreak = 4,
                    bestStreak = 5,
                    totalCompletions = 8
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
                    colorHex = "#625B71",
                    currentStreak = 3,
                    bestStreak = 4,
                    totalCompletions = 6
                )
            )
            habitDao.insertAll(initialHabits)

            // 4. Sample Recent Logs
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            
            // Today's date
            val todayStr = dateFormat.format(cal.time)
            habitLogDao.insertOrUpdateLog(HabitLogEntity(habitId = 1L, dateString = todayStr, count = 8, isCompleted = true))
            habitLogDao.insertOrUpdateLog(HabitLogEntity(habitId = 2L, dateString = todayStr, count = 30, isCompleted = true))

            // Past 14 days of logs for rich analytics and mood correlation
            for (i in 1..14) {
                cal.time = Date()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val pastDateStr = dateFormat.format(cal.time)
                
                // Habit 1 (Water) completed almost every day
                if (i != 8) {
                    habitLogDao.insertOrUpdateLog(HabitLogEntity(habitId = 1L, dateString = pastDateStr, count = 8, isCompleted = true))
                }
                
                // Habit 2 (Workout) completed most days except weekends (i=6, 7, 13, 14)
                if (i !in listOf(6, 7, 13)) {
                    habitLogDao.insertOrUpdateLog(HabitLogEntity(habitId = 2L, dateString = pastDateStr, count = 30, isCompleted = true))
                }
                
                // Habit 3 (Reading) completed 10 of 14 days
                if (i !in listOf(4, 9, 10, 14)) {
                    habitLogDao.insertOrUpdateLog(HabitLogEntity(habitId = 3L, dateString = pastDateStr, count = 15, isCompleted = true))
                }

                // Habit 4 (Meditation)
                if (i in listOf(1, 2, 3, 5, 7, 11, 12)) {
                    habitLogDao.insertOrUpdateLog(HabitLogEntity(habitId = 4L, dateString = pastDateStr, count = 10, isCompleted = true))
                }
            }

            // 5. Seed Historical Mood Logs (to power mood correlation analytics)
            val moodHistory = listOf(
                MoodLogEntity(dateString = todayStr, moodScore = 5, moodLabel = "Energized", moodEmoji = "⚡", energyLevel = 5, stressLevel = 1, tags = "Focused, Productive", notes = "Felt great after morning workout & water!"),
                MoodLogEntity(dateString = getDateOffset(dateFormat, -1), moodScore = 4, moodLabel = "Great", moodEmoji = "😊", energyLevel = 4, stressLevel = 2, tags = "Calm, Motivated", notes = "Solid execution across all routines."),
                MoodLogEntity(dateString = getDateOffset(dateFormat, -2), moodScore = 5, moodLabel = "Fantastic", moodEmoji = "🤩", energyLevel = 5, stressLevel = 1, tags = "High Flow, Inspired", notes = "Meditation set a calm tone."),
                MoodLogEntity(dateString = getDateOffset(dateFormat, -3), moodScore = 4, moodLabel = "Good", moodEmoji = "🙂", energyLevel = 4, stressLevel = 2, tags = "Steady, Focused", notes = "Work was busy but stayed hydrated."),
                MoodLogEntity(dateString = getDateOffset(dateFormat, -4), moodScore = 3, moodLabel = "Neutral", moodEmoji = "😐", energyLevel = 3, stressLevel = 3, tags = "Tired, Busy", notes = "Missed evening reading due to late meeting."),
                MoodLogEntity(dateString = getDateOffset(dateFormat, -5), moodScore = 5, moodLabel = "Energized", moodEmoji = "⚡", energyLevel = 5, stressLevel = 1, tags = "Active, Joyful", notes = "Great cardio and 100% habit day!"),
                MoodLogEntity(dateString = getDateOffset(dateFormat, -6), moodScore = 2, moodLabel = "Low Energy", moodEmoji = "😔", energyLevel = 2, stressLevel = 4, tags = "Overwhelmed, Sluggish", notes = "Sunday slump, skipped morning exercise."),
                MoodLogEntity(dateString = getDateOffset(dateFormat, -7), moodScore = 3, moodLabel = "Okay", moodEmoji = "😐", energyLevel = 3, stressLevel = 3, tags = "Resting", notes = "Weekend recovery.")
            )
            moodHistory.forEach { moodLogDao.insertOrUpdateMood(it) }

            // 6. Seed Accountability Groups & Squads
            val groups = listOf(
                AccountabilityGroupEntity(
                    id = 1L,
                    name = "5 AM Morning Titans",
                    description = "Rise early, hydrate, workout, and win the first 2 hours of the day.",
                    iconName = "Bolt",
                    category = "Productivity",
                    inviteCode = "TITAN-5AM",
                    memberCount = 5,
                    targetDailyCompletions = 15,
                    isUserJoined = true,
                    streak = 6
                ),
                AccountabilityGroupEntity(
                    id = 2L,
                    name = "Daily Mindful Tribe",
                    description = "Daily meditation, reading, and mental well-being check-ins.",
                    iconName = "SelfImprovement",
                    category = "Mindfulness",
                    inviteCode = "ZEN-FLOW",
                    memberCount = 4,
                    targetDailyCompletions = 12,
                    isUserJoined = true,
                    streak = 4
                ),
                AccountabilityGroupEntity(
                    id = 3L,
                    name = "Iron Consistency Club",
                    description = "No zero days. Fitness, clean habits, and unbroken streaks.",
                    iconName = "FitnessCenter",
                    category = "Fitness",
                    inviteCode = "IRON-HABIT",
                    memberCount = 6,
                    targetDailyCompletions = 18,
                    isUserJoined = false,
                    streak = 9
                )
            )
            accountabilityDao.insertGroups(groups)

            // 7. Seed Group Members
            val membersGroup1 = listOf(
                GroupMemberEntity(groupId = 1L, userName = "Alex Rivera (You)", avatarInitials = "AR", avatarColorHex = "#6750A4", currentStreak = 7, habitsCompletedToday = 2, habitsTargetToday = 3, isCurrentUser = true, statusEmoji = "⚡", statusText = "Finished workout & hydration!", xpWeekly = 450),
                GroupMemberEntity(groupId = 1L, userName = "Elena Chen", avatarInitials = "EC", avatarColorHex = "#10B981", currentStreak = 9, habitsCompletedToday = 3, habitsTargetToday = 3, isCurrentUser = false, statusEmoji = "🔥", statusText = "100% daily loop closed early!", xpWeekly = 580),
                GroupMemberEntity(groupId = 1L, userName = "Marcus Vance", avatarInitials = "MV", avatarColorHex = "#F59E0B", currentStreak = 6, habitsCompletedToday = 2, habitsTargetToday = 3, isCurrentUser = false, statusEmoji = "🏃", statusText = "Running 5km before work", xpWeekly = 410),
                GroupMemberEntity(groupId = 1L, userName = "Sophia Patel", avatarInitials = "SP", avatarColorHex = "#06B6D4", currentStreak = 4, habitsCompletedToday = 1, habitsTargetToday = 2, isCurrentUser = false, statusEmoji = "💧", statusText = "Hydration goal in progress", xpWeekly = 340),
                GroupMemberEntity(groupId = 1L, userName = "Liam O'Connor", avatarInitials = "LO", avatarColorHex = "#8B5CF6", currentStreak = 5, habitsCompletedToday = 2, habitsTargetToday = 3, isCurrentUser = false, statusEmoji = "💪", statusText = "Day 5 of morning gym routine", xpWeekly = 390)
            )
            accountabilityDao.insertMembers(membersGroup1)

            val membersGroup2 = listOf(
                GroupMemberEntity(groupId = 2L, userName = "Alex Rivera (You)", avatarInitials = "AR", avatarColorHex = "#6750A4", currentStreak = 4, habitsCompletedToday = 1, habitsTargetToday = 2, isCurrentUser = true, statusEmoji = "🧘", statusText = "Mindful breathing done", xpWeekly = 350),
                GroupMemberEntity(groupId = 2L, userName = "Chloe Dubois", avatarInitials = "CD", avatarColorHex = "#EC4899", currentStreak = 8, habitsCompletedToday = 2, habitsTargetToday = 2, isCurrentUser = false, statusEmoji = "📖", statusText = "Read 25 pages of Atomic Habits", xpWeekly = 520),
                GroupMemberEntity(groupId = 2L, userName = "David Kim", avatarInitials = "DK", avatarColorHex = "#3B82F6", currentStreak = 3, habitsCompletedToday = 1, habitsTargetToday = 2, isCurrentUser = false, statusEmoji = "🍵", statusText = "Green tea & reflection time", xpWeekly = 280),
                GroupMemberEntity(groupId = 2L, userName = "Zara Ahmed", avatarInitials = "ZA", avatarColorHex = "#10B981", currentStreak = 6, habitsCompletedToday = 2, habitsTargetToday = 2, isCurrentUser = false, statusEmoji = "✨", statusText = "Calm morning gratitude practice", xpWeekly = 430)
            )
            accountabilityDao.insertMembers(membersGroup2)

            // 8. Seed Group Messages
            val messagesGroup1 = listOf(
                GroupMessageEntity(groupId = 1L, senderName = "Elena Chen", senderAvatar = "EC", isCurrentUser = false, message = "Good morning squad! 5:00 AM check-in, water down and stretching done. 🌅", messageType = "chat", timestamp = System.currentTimeMillis() - 1000 * 60 * 180),
                GroupMessageEntity(groupId = 1L, senderName = "Marcus Vance", senderAvatar = "MV", isCurrentUser = false, message = "Crushed morning 5km run! Let's keep this collective streak alive! 🔥", messageType = "milestone", timestamp = System.currentTimeMillis() - 1000 * 60 * 120),
                GroupMessageEntity(groupId = 1L, senderName = "Alex Rivera", senderAvatar = "AR", isCurrentUser = true, message = "Finished workout and logged 2L water! Momentum is high today.", messageType = "chat", timestamp = System.currentTimeMillis() - 1000 * 60 * 60),
                GroupMessageEntity(groupId = 1L, senderName = "Elena Chen", senderAvatar = "EC", isCurrentUser = false, message = "High-fived Alex! Keep crushing it! ✋", messageType = "cheer", timestamp = System.currentTimeMillis() - 1000 * 60 * 45)
            )
            accountabilityDao.insertMessages(messagesGroup1)

            // 9. Initial Welcome Chat from AI Coach
            chatDao.insertMessage(
                ChatMessageEntity(
                    sender = "coach",
                    message = "Hello Alex! I am Coach Zenith, your AI Habit & Behavioral Mentor. 🎯\n\nI correlate your daily habits with mood patterns, diagnose drop-off vulnerabilities, and coach your accountability squads. How can I empower your routines today?",
                    suggestionType = "daily_motivate"
                )
            )

            // 10. Curated Trending Apps & Malay Karaoke Links
            val trendingLinkDao = database.trendingLinkDao()
            trendingLinkDao.insertAll(TrendingRepository.getCuratedTrendingLinks())
        }

        private fun getDateOffset(format: SimpleDateFormat, offsetDays: Int): String {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, offsetDays)
            return format.format(cal.time)
        }
    }
}
