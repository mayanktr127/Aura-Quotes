package com.myplanetservices.stillwords.ui.screens.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myplanetservices.stillwords.data.model.Quote
import com.myplanetservices.stillwords.data.model.QuoteCollection
import com.myplanetservices.stillwords.data.repository.QuoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CollectionDetailUiState(
    val collection: QuoteCollection? = null,
    val quotes: List<Quote> = emptyList(),
    val isLoading: Boolean = true
)

class CollectionDetailViewModel(
    private val collectionId: Long,
    private val repository: QuoteRepository
) : ViewModel() {

    val uiState: StateFlow<CollectionDetailUiState> = combine(
        repository.collectionsFlow,
        repository.getQuotesForCollection(collectionId)
    ) { collections, quotes ->
        val collection = collections.firstOrNull { it.id == collectionId }
        CollectionDetailUiState(
            collection = collection,
            quotes = quotes,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CollectionDetailUiState()
    )

    fun toggleFavorite(quoteId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(quoteId)
        }
    }

    fun removeQuoteFromCollection(quoteId: String) {
        viewModelScope.launch {
            repository.removeQuoteFromCollection(collectionId, quoteId)
        }
    }

    fun deleteCollection(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCollection(collectionId)
            onDeleted()
        }
    }

    class Factory(
        private val collectionId: Long,
        private val repository: QuoteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CollectionDetailViewModel(collectionId, repository) as T
        }
    }
}

