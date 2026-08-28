package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET xp = xp + :points, totalHabitsCompleted = totalHabitsCompleted + 1 WHERE id = 1")
    suspend fun addXpAndCompletion(points: Int)

    @Query("UPDATE user_profile SET level = :level WHERE id = 1")
    suspend fun updateLevel(level: Int)

    @Query("UPDATE user_profile SET isPremium = :isPremium, subscriptionPlan = :plan, subscriptionExpiry = :expiry WHERE id = 1")
    suspend fun updateSubscription(isPremium: Boolean, plan: String, expiry: Long?)

    @Query("UPDATE user_profile SET isDarkMode = :isDark WHERE id = 1")
    suspend fun updateDarkMode(isDark: Boolean)
}
