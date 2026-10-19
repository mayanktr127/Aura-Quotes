package com.myplanetservices.stillwords.ui.screens.read

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myplanetservices.stillwords.data.model.Quote
import com.myplanetservices.stillwords.data.preferences.UserPreferencesRepository
import com.myplanetservices.stillwords.data.repository.QuoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReadUiState(
    val quotes: List<Quote> = emptyList(),
    val initialIndex: Int = 0,
    val showSwipeHint: Boolean = true,
    val isLoading: Boolean = true
)

class ReadViewModel(
    private val initialQuoteId: String?,
    private val categories: List<String>?,
    val quoteRepository: QuoteRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val recordedHistoryQuotes = mutableSetOf<String>()

    val uiState: StateFlow<ReadUiState> = combine(
        quoteRepository.allQuotesFlow,
        preferencesRepository.userPreferencesFlow
    ) { allQuotes, userPrefs ->
        val filteredQuotes = if (!categories.isNullOrEmpty()) {
            val catSet = categories.map { it.lowercase() }.toSet()
            allQuotes.filter { it.category.lowercase() in catSet }
        } else {
            allQuotes
        }

        val targetQuotes = if (initialQuoteId != null && filteredQuotes.none { it.id == initialQuoteId }) {
            val specialQuote = allQuotes.find { it.id == initialQuoteId }
            if (specialQuote != null) listOf(specialQuote) + filteredQuotes else filteredQuotes
        } else {
            filteredQuotes
        }

        val initialIdx = if (initialQuoteId != null) {
            targetQuotes.indexOfFirst { it.id == initialQuoteId }.coerceAtLeast(0)
        } else 0

        ReadUiState(
            quotes = targetQuotes,
            initialIndex = initialIdx,
            showSwipeHint = userPrefs.readSwipeHintCount < 3,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReadUiState()
    )

    fun toggleFavorite(quoteId: String) {
        viewModelScope.launch {
            quoteRepository.toggleFavorite(quoteId)
        }
    }

    fun onPageSettled(page: Int) {
        val quotes = uiState.value.quotes
        if (page in quotes.indices) {
            val quote = quotes[page]
            if (recordedHistoryQuotes.add(quote.id)) {
                viewModelScope.launch {
                    quoteRepository.recordHistory(quote.id)
                }
            }
            if (page > 0 && uiState.value.showSwipeHint) {
                viewModelScope.launch {
                    preferencesRepository.incrementReadSwipeHintCount()
                }
            }
        }
    }

    fun dismissSwipeHint() {
        viewModelScope.launch {
            preferencesRepository.incrementReadSwipeHintCount()
        }
    }

    class Factory(
        private val initialQuoteId: String?,
        private val categories: List<String>?,
        private val quoteRepository: QuoteRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReadViewModel(
                initialQuoteId,
                categories,
                quoteRepository,
                preferencesRepository
            ) as T
        }
    }
}
