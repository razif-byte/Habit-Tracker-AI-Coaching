package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MoodLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodLogDao {
    @Query("SELECT * FROM mood_logs ORDER BY dateString DESC")
    fun getAllMoodLogs(): Flow<List<MoodLogEntity>>

    @Query("SELECT * FROM mood_logs WHERE dateString = :dateString LIMIT 1")
    fun getMoodForDate(dateString: String): Flow<MoodLogEntity?>

    @Query("SELECT * FROM mood_logs WHERE dateString = :dateString LIMIT 1")
    suspend fun getMoodForDateOnce(dateString: String): MoodLogEntity?

    @Query("SELECT * FROM mood_logs ORDER BY dateString DESC LIMIT :limit")
    fun getRecentMoodLogs(limit: Int): Flow<List<MoodLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMood(moodLog: MoodLogEntity): Long

    @Query("SELECT AVG(moodScore) FROM mood_logs")
    suspend fun getAverageMoodScore(): Float?

    @Query("SELECT COUNT(*) FROM mood_logs")
    suspend fun getTotalMoodEntries(): Int
}
