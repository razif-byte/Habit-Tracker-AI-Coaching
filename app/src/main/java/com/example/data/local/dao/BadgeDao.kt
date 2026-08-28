package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BadgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges")
    fun getAllBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badges WHERE isUnlocked = 1")
    fun getUnlockedBadges(): Flow<List<BadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(badges: List<BadgeEntity>)

    @Update
    suspend fun updateBadge(badge: BadgeEntity)

    @Query("UPDATE badges SET isUnlocked = 1, unlockedAt = :timestamp, progress = 1.0 WHERE badgeKey = :key AND isUnlocked = 0")
    suspend fun unlockBadge(key: String, timestamp: Long = System.currentTimeMillis()): Int
}
