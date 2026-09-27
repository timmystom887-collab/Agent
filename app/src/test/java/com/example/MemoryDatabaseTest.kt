package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.MemoryDao
import com.example.data.local.entity.Memory
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoryDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var memoryDao: MemoryDao
    private lateinit var repository: MemoryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = AppDatabase.createInMemory(context)
        memoryDao = database.memoryDao()
        repository = MemoryRepository(memoryDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveMemory() = runBlocking {
        val memory = Memory(
            title = "Neural Interface Directive",
            content = "Bridge the subconscious intuition with high-speed autonomous reasoning.",
            category = Memory.CATEGORY_CORE,
            tags = "neural, directive",
            importance = 5,
            isFavorite = true
        )

        val id = repository.insertMemory(memory)
        assertTrue(id > 0)

        val retrieved = repository.getMemoryByIdOnce(id)
        assertNotNull(retrieved)
        assertEquals("Neural Interface Directive", retrieved?.title)
        assertEquals(Memory.CATEGORY_CORE, retrieved?.category)
        assertTrue(retrieved?.isFavorite == true)
        assertEquals(5, retrieved?.importance)
    }

    @Test
    fun testCategoryFiltering() = runBlocking {
        val mem1 = Memory(title = "Task 1", content = "Do something", category = Memory.CATEGORY_TASK)
        val mem2 = Memory(title = "Insight 1", content = "Learned something", category = Memory.CATEGORY_INSIGHT)
        val mem3 = Memory(title = "Task 2", content = "Do another thing", category = Memory.CATEGORY_TASK)

        repository.insertMemories(listOf(mem1, mem2, mem3))

        val tasks = repository.getMemoriesByCategory(Memory.CATEGORY_TASK).first()
        assertEquals(2, tasks.size)
        assertTrue(tasks.all { it.category == Memory.CATEGORY_TASK })

        val insights = repository.getMemoriesByCategory(Memory.CATEGORY_INSIGHT).first()
        assertEquals(1, insights.size)
        assertEquals("Insight 1", insights.first().title)
    }

    @Test
    fun testSearchMemories() = runBlocking {
        val mem1 = Memory(title = "Matrix Source Code", content = "Look for the green rain", tags = "cipher, matrix")
        val mem2 = Memory(title = "Daily Routine", content = "Drink coffee and code", tags = "life")

        repository.insertMemories(listOf(mem1, mem2))

        val searchResult1 = repository.searchMemories("matrix").first()
        assertEquals(1, searchResult1.size)
        assertEquals("Matrix Source Code", searchResult1.first().title)

        val searchResult2 = repository.searchMemories("coffee").first()
        assertEquals(1, searchResult2.size)
        assertEquals("Daily Routine", searchResult2.first().title)

        val emptyResult = repository.searchMemories("nonexistent_term").first()
        assertTrue(emptyResult.isEmpty())
    }

    @Test
    fun testToggleFavoriteAndArchive() = runBlocking {
        val memory = Memory(
            title = "Key Secret",
            content = "Keep this safe",
            category = Memory.CATEGORY_CORE,
            isFavorite = false
        )
        val id = repository.insertMemory(memory)

        // Toggle favorite
        repository.toggleFavorite(id, currentFavorite = false)
        val updated = repository.getMemoryByIdOnce(id)
        assertTrue(updated?.isFavorite == true)

        // Verify it appears in favorite memories
        val favorites = repository.favoriteMemories.first()
        assertEquals(1, favorites.size)

        // Archive memory
        repository.archiveMemory(id)
        val activeMemories = repository.allActiveMemories.first()
        assertTrue(activeMemories.isEmpty())
    }

    @Test
    fun testDeleteMemory() = runBlocking {
        val memory = Memory(title = "Temporary Note", content = "To be deleted")
        val id = repository.insertMemory(memory)

        assertEquals(1, repository.activeMemoryCount.first())

        repository.deleteMemoryById(id)

        assertEquals(0, repository.activeMemoryCount.first())
        assertNull(repository.getMemoryByIdOnce(id))
    }
}
