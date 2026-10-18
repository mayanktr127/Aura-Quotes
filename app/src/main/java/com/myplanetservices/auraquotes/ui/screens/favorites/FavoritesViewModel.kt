package com.myplanetservices.auraquotes.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myplanetservices.auraquotes.data.model.Quote
import com.myplanetservices.auraquotes.data.model.QuoteCollection
import com.myplanetservices.auraquotes.data.repository.QuoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val selectedTab: Int = 0, // 0: Saved Quotes, 1: Collections, 2: History
    val savedQuotes: List<Quote> = emptyList(),
    val collections: List<QuoteCollection> = emptyList(),
    val historyQuotes: List<Quote> = emptyList(),
    val isCreateCollectionDialogOpen: Boolean = false,
    val isClearHistoryDialogOpen: Boolean = false,
    val selectedCollection: QuoteCollection? = null,
    val collectionQuotes: List<Quote> = emptyList()
)

private data class FavoritesDialogState(
    val selectedTab: Int = 0,
    val isCreateDialogOpen: Boolean = false,
    val isClearHistoryDialogOpen: Boolean = false,
    val selectedCollectionId: Long? = null
)

class FavoritesViewModel(
    private val repository: QuoteRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    private val _isCreateDialogOpen = MutableStateFlow(false)
    private val _isClearHistoryDialogOpen = MutableStateFlow(false)
    private val _selectedCollectionId = MutableStateFlow<Long?>(null)

    private val _dialogState: Flow<FavoritesDialogState> = combine(
        _selectedTab,
        _isCreateDialogOpen,
        _isClearHistoryDialogOpen,
        _selectedCollectionId
    ) { tab, isCreateOpen, isClearOpen, colId ->
        FavoritesDialogState(tab, isCreateOpen, isClearOpen, colId)
    }

    val uiState: StateFlow<FavoritesUiState> = combine(
        _dialogState,
        repository.favoriteQuotesFlow,
        repository.collectionsFlow,
        repository.historyQuotesFlow
    ) { dialogs: FavoritesDialogState, saved: List<Quote>, collections: List<QuoteCollection>, history: List<Quote> ->
        val selectedCol = collections.firstOrNull { it.id == dialogs.selectedCollectionId }
        val colQuotes = if (dialogs.selectedCollectionId != null) {
            repository.getQuotesForCollection(dialogs.selectedCollectionId).firstOrNull() ?: emptyList()
        } else emptyList()

        FavoritesUiState(
            selectedTab = dialogs.selectedTab,
            savedQuotes = saved,
            collections = collections,
            historyQuotes = history,
            isCreateCollectionDialogOpen = dialogs.isCreateDialogOpen,
            isClearHistoryDialogOpen = dialogs.isClearHistoryDialogOpen,
            selectedCollection = selectedCol,
            collectionQuotes = colQuotes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FavoritesUiState()
    )

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
        if (tabIndex != 1) {
            _selectedCollectionId.value = null
        }
    }

    fun toggleFavorite(quoteId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(quoteId)
        }
    }

    fun openCollection(collectionId: Long) {
        _selectedCollectionId.value = collectionId
    }

    fun closeCollection() {
        _selectedCollectionId.value = null
    }

    fun setCreateCollectionDialogOpen(open: Boolean) {
        _isCreateDialogOpen.value = open
    }

    fun createCollection(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createCollection(name.trim(), description.trim())
            _isCreateDialogOpen.value = false
        }
    }

    fun deleteCollection(collectionId: Long) {
        viewModelScope.launch {
            repository.deleteCollection(collectionId)
            _selectedCollectionId.value = null
        }
    }

    fun removeQuoteFromCollection(collectionId: Long, quoteId: String) {
        viewModelScope.launch {
            repository.removeQuoteFromCollection(collectionId, quoteId)
        }
    }

    fun setClearHistoryDialogOpen(open: Boolean) {
        _isClearHistoryDialogOpen.value = open
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _isClearHistoryDialogOpen.value = false
        }
    }

    class Factory(private val repository: QuoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoritesViewModel(repository) as T
        }
    }
}
