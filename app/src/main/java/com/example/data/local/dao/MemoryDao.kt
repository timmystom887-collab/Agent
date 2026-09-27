package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Memory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for 'memories' table in Room Database.
 */
@Dao
interface MemoryDao {

    @Query("SELECT * FROM memories WHERE isArchived = 0 ORDER BY isFavorite DESC, importance DESC, updatedAt DESC")
    fun getAllActiveMemories(): Flow<List<Memory>>

    @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
    fun getAllMemories(): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE category = :category AND isArchived = 0 ORDER BY importance DESC, updatedAt DESC")
    fun getMemoriesByCategory(category: String): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE isFavorite = 1 AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getFavoriteMemories(): Flow<List<Memory>>

    @Query("""
        SELECT * FROM memories 
        WHERE isArchived = 0 AND (
            title LIKE '%' || :query || '%' OR 
            content LIKE '%' || :query || '%' OR 
            tags LIKE '%' || :query || '%'
        )
        ORDER BY importance DESC, updatedAt DESC
    """)
    fun searchMemories(query: String): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE id = :id LIMIT 1")
    fun getMemoryById(id: Long): Flow<Memory?>

    @Query("SELECT * FROM memories WHERE id = :id LIMIT 1")
    suspend fun getMemoryByIdOnce(id: Long): Memory?

    @Query("SELECT COUNT(*) FROM memories WHERE isArchived = 0")
    fun getActiveMemoryCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<Memory>): List<Long>

    @Update
    suspend fun updateMemory(memory: Memory)

    @Delete
    suspend fun deleteMemory(memory: Memory)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("UPDATE memories SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE memories SET isArchived = :isArchived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()
}
