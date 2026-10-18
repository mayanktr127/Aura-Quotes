package com.myplanetservices.stillwords.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myplanetservices.stillwords.data.model.Quote
import com.myplanetservices.stillwords.data.repository.QuoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<Quote> = emptyList(),
    val suggestions: List<String> = listOf(
        "Motivation", "Mindset", "Peace", "Courage", "Discipline",
        "Marcus Aurelius", "Rumi", "Wisdom", "Growth", "Gratitude"
    ),
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val repository: QuoteRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = _query
        .debounce(250)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchQuotes(query)
            }
        }.combine(_query) { results, query ->
            SearchUiState(
                query = query,
                results = results,
                isSearching = query.isNotBlank()
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchUiState()
        )

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onSuggestionSelected(suggestion: String) {
        _query.value = suggestion
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun toggleFavorite(quoteId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(quoteId)
        }
    }

    fun recordHistory(quoteId: String) {
        viewModelScope.launch {
            repository.recordHistory(quoteId)
        }
    }

    class Factory(private val repository: QuoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(repository) as T
        }
    }
}

