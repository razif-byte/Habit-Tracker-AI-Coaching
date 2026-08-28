package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE dateString = :dateString")
    fun getLogsForDate(dateString: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY dateString DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString LIMIT 1")
    suspend fun getLog(habitId: Long, dateString: String): HabitLogEntity?

    @Query("SELECT * FROM habit_logs WHERE dateString >= :startDateString ORDER BY dateString ASC")
    fun getLogsSinceDate(startDateString: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY dateString DESC")
    fun getAllLogs(): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteLog(habitId: Long, dateString: String)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)

    @Query("SELECT COUNT(*) FROM habit_logs WHERE isCompleted = 1")
    suspend fun getTotalCompletedLogsCount(): Int
}
