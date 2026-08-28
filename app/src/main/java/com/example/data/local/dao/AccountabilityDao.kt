package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AccountabilityGroupEntity
import com.example.data.local.entity.GroupMemberEntity
import com.example.data.local.entity.GroupMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountabilityDao {
    @Query("SELECT * FROM accountability_groups ORDER BY id ASC")
    fun getAllGroups(): Flow<List<AccountabilityGroupEntity>>

    @Query("SELECT * FROM accountability_groups WHERE id = :groupId LIMIT 1")
    fun getGroupById(groupId: Long): Flow<AccountabilityGroupEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: AccountabilityGroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<AccountabilityGroupEntity>)

    @Update
    suspend fun updateGroup(group: AccountabilityGroupEntity)

    // Members
    @Query("SELECT * FROM group_members WHERE groupId = :groupId ORDER BY habitsCompletedToday DESC, currentStreak DESC")
    fun getMembersForGroup(groupId: Long): Flow<List<GroupMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<GroupMemberEntity>)

    @Query("UPDATE group_members SET habitsCompletedToday = :count WHERE isCurrentUser = 1 AND groupId = :groupId")
    suspend fun updateCurrentUserProgress(groupId: Long, count: Int)

    // Messages
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    fun getMessagesForGroup(groupId: Long): Flow<List<GroupMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: GroupMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<GroupMessageEntity>)
}
