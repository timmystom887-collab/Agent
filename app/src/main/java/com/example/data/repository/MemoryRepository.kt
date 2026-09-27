package com.example.data.repository

import com.example.data.local.dao.MemoryDao
import com.example.data.local.entity.Memory
import kotlinx.coroutines.flow.Flow

/**
 * Repository layer abstracting database operations for the Second-Brain Memory store.
 */
class MemoryRepository(private val memoryDao: MemoryDao) {

    val allActiveMemories: Flow<List<Memory>> = memoryDao.getAllActiveMemories()

    val favoriteMemories: Flow<List<Memory>> = memoryDao.getFavoriteMemories()

    val activeMemoryCount: Flow<Int> = memoryDao.getActiveMemoryCount()

    fun getMemoriesByCategory(category: String): Flow<List<Memory>> {
        return memoryDao.getMemoriesByCategory(category)
    }

    fun searchMemories(query: String): Flow<List<Memory>> {
        return memoryDao.searchMemories(query)
    }

    fun getMemoryById(id: Long): Flow<Memory?> {
        return memoryDao.getMemoryById(id)
    }

    suspend fun getMemoryByIdOnce(id: Long): Memory? {
        return memoryDao.getMemoryByIdOnce(id)
    }

    suspend fun insertMemory(memory: Memory): Long {
        return memoryDao.insertMemory(memory)
    }

    suspend fun insertMemories(memories: List<Memory>): List<Long> {
        return memoryDao.insertMemories(memories)
    }

    suspend fun updateMemory(memory: Memory) {
        memoryDao.updateMemory(memory.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        memoryDao.setFavorite(id, !currentFavorite)
    }

    suspend fun archiveMemory(id: Long) {
        memoryDao.setArchived(id, true)
    }

    suspend fun deleteMemory(memory: Memory) {
        memoryDao.deleteMemory(memory)
    }

    suspend fun deleteMemoryById(id: Long) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun clearAll() {
        memoryDao.clearAllMemories()
    }
}
