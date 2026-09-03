package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TrendingLinkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrendingLinkDao {

    @Query("SELECT * FROM trending_links ORDER BY isFavorite DESC, clickCount DESC, id ASC")
    fun getAllLinks(): Flow<List<TrendingLinkEntity>>

    @Query("SELECT * FROM trending_links WHERE category = :category ORDER BY isFavorite DESC, clickCount DESC, id ASC")
    fun getLinksByCategory(category: String): Flow<List<TrendingLinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: TrendingLinkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<TrendingLinkEntity>)

    @Update
    suspend fun updateLink(link: TrendingLinkEntity)

    @Delete
    suspend fun deleteLink(link: TrendingLinkEntity)

    @Query("UPDATE trending_links SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE trending_links SET clickCount = clickCount + 1 WHERE id = :id")
    suspend fun incrementClickCount(id: Long)

    @Query("SELECT * FROM trending_links WHERE url = :url LIMIT 1")
    suspend fun getLinkByUrl(url: String): TrendingLinkEntity?

    @Query("SELECT url FROM trending_links")
    suspend fun getAllUrls(): List<String>

    @Query("SELECT COUNT(*) FROM trending_links")
    suspend fun getCount(): Int
}
