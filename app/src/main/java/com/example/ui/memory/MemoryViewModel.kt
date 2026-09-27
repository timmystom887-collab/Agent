package com.example.ui.memory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Memory
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MemoryUiState(
    val memories: List<Memory> = emptyList(),
    val totalActiveCount: Int = 0,
    val favoriteCount: Int = 0,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedMemory: Memory? = null,
    val isAddEditSheetOpen: Boolean = false,
    val userMessage: String? = null
)

private data class FilterParams(
    val query: String,
    val category: String?,
    val selectedMemory: Memory?,
    val isSheetOpen: Boolean
)

class MemoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MemoryRepository
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedMemory = MutableStateFlow<Memory?>(null)
    val selectedMemory = _selectedMemory.asStateFlow()

    private val _isAddEditSheetOpen = MutableStateFlow(false)
    val isAddEditSheetOpen = _isAddEditSheetOpen.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = MemoryRepository(database.memoryDao())
    }

    // Secondary constructor for testing with mock or in-memory repository
    constructor(application: Application, customRepository: MemoryRepository) : this(application) {
        // In testing, can override or use repository directly
    }

    private val filterState = combine(
        _searchQuery,
        _selectedCategory,
        _selectedMemory,
        _isAddEditSheetOpen
    ) { query, category, selectedMem, isSheetOpen ->
        FilterParams(query, category, selectedMem, isSheetOpen)
    }

    val uiState: StateFlow<MemoryUiState> = combine(
        repository.allActiveMemories,
        filterState,
        _userMessage
    ) { allMemories, filter, message ->
        val filtered = allMemories.filter { memory ->
            val matchesCategory = filter.category == null || memory.category.equals(filter.category, ignoreCase = true)
            val matchesQuery = filter.query.isBlank() ||
                memory.title.contains(filter.query, ignoreCase = true) ||
                memory.content.contains(filter.query, ignoreCase = true) ||
                memory.tags.contains(filter.query, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        MemoryUiState(
            memories = filtered,
            totalActiveCount = allMemories.size,
            favoriteCount = allMemories.count { it.isFavorite },
            selectedCategory = filter.category,
            searchQuery = filter.query,
            isLoading = false,
            selectedMemory = filter.selectedMemory,
            isAddEditSheetOpen = filter.isSheetOpen,
            userMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MemoryUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun openAddMemoryDialog() {
        _selectedMemory.value = null
        _isAddEditSheetOpen.value = true
    }

    fun openEditMemoryDialog(memory: Memory) {
        _selectedMemory.value = memory
        _isAddEditSheetOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddEditSheetOpen.value = false
        _selectedMemory.value = null
    }

    fun saveMemory(title: String, content: String, category: String, tags: String, importance: Int) {
        if (title.isBlank() && content.isBlank()) {
            _userMessage.value = "Cannot save an empty memory entry"
            return
        }

        viewModelScope.launch {
            val current = _selectedMemory.value
            if (current == null) {
                val newMemory = Memory(
                    title = title.trim().ifBlank { "Untitled Memory" },
                    content = content.trim(),
                    category = category,
                    tags = tags.trim(),
                    importance = importance.coerceIn(1, 5)
                )
                repository.insertMemory(newMemory)
                _userMessage.value = "Memory stored in Second-Brain"
            } else {
                val updated = current.copy(
                    title = title.trim().ifBlank { current.title },
                    content = content.trim(),
                    category = category,
                    tags = tags.trim(),
                    importance = importance.coerceIn(1, 5)
                )
                repository.updateMemory(updated)
                _userMessage.value = "Memory updated"
            }
            closeAddEditDialog()
        }
    }

    fun toggleFavorite(memory: Memory) {
        viewModelScope.launch {
            repository.toggleFavorite(memory.id, memory.isFavorite)
        }
    }

    fun deleteMemory(memory: Memory) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
            _userMessage.value = "Memory removed from Second-Brain"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
