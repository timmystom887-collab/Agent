package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.MemoryDao
import com.example.data.local.entity.Memory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database configuration for the application's Second-Brain memory store.
 */
@Database(
    entities = [Memory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "second_brain_memory.db"

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabasePrepopulateCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * For testing purposes, builds an in-memory database instance.
         */
        fun createInMemory(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
            .allowMainThreadQueries()
            .build()
        }
    }

    private class DatabasePrepopulateCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    prepopulateDatabase(database.memoryDao())
                }
            }
        }

        private suspend fun prepopulateDatabase(dao: MemoryDao) {
            val initialMemories = listOf(
                Memory(
                    title = "System Directive",
                    content = "Operate with absolute precision. Safeguard user context and execute autonomous tasks with continuous verification.",
                    category = Memory.CATEGORY_CORE,
                    tags = "directive, core, matrix",
                    importance = 5,
                    isFavorite = true
                ),
                Memory(
                    title = "Autonomous Swarm Protocol",
                    content = "When decomposed tasks exceed single-thread capacity, spawn specialized sub-agents with DAG dependency management.",
                    category = Memory.CATEGORY_INSIGHT,
                    tags = "swarm, multi-agent, architecture",
                    importance = 4,
                    isFavorite = true
                ),
                Memory(
                    title = "Telephony IVR Heuristics",
                    content = "DTMF tones must be matched to spoken numeric prompts. Monitor call state transitions to handle automated phone trees.",
                    category = Memory.CATEGORY_TASK,
                    tags = "ivr, telephony, automation",
                    importance = 3,
                    isFavorite = false
                ),
                Memory(
                    title = "3-Tier Memory Architecture",
                    content = "Working Memory (Context) -> Episodic Memory (Room SQLite Vector) -> Semantic Knowledge Vault (Encrypted MasterKey).",
                    category = Memory.CATEGORY_CONTEXT,
                    tags = "memory, letta, memgpt",
                    importance = 4,
                    isFavorite = false
                )
            )
            dao.insertMemories(initialMemories)
        }
    }
}
