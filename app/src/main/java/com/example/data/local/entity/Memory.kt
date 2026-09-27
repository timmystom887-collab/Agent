package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Memory entity representing an autonomous 'second-brain' memory entry.
 * Stores semantic notes, observations, agent knowledge, and user-stored contexts.
 */
@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = CATEGORY_GENERAL,
    val tags: String = "",
    val importance: Int = 1, // 1 (Normal) to 5 (Critical)
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val CATEGORY_GENERAL = "General"
        const val CATEGORY_INSIGHT = "Insight"
        const val CATEGORY_TASK = "Task"
        const val CATEGORY_CONTEXT = "Context"
        const val CATEGORY_CORE = "Core"
        
        val ALL_CATEGORIES = listOf(
            CATEGORY_GENERAL,
            CATEGORY_INSIGHT,
            CATEGORY_TASK,
            CATEGORY_CONTEXT,
            CATEGORY_CORE
        )
    }
}
