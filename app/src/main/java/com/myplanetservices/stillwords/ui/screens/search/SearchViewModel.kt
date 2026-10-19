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

enum class SearchFilter(val label: String) {
    ALL("All"),
    QUOTES("Quotes"),
    TOPICS("Topics"),
    AUTHORS("Authors")
}

data class SearchUiState(
    val query: String = "",
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val results: List<Quote> = emptyList(),
    val matchingTopics: List<String> = emptyList(),
    val suggestions: List<String> = listOf(
        "Happiness", "Gratitude", "Life", "Creativity", "Wisdom"
    ),
    val isSearching: Boolean = false
) {
    val totalQuotesCount: Int get() = results.size
    val totalTopicsCount: Int get() = matchingTopics.size
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val repository: QuoteRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(SearchFilter.ALL)

    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        _selectedFilter,
        repository.allQuotesFlow
    ) { query, filter, allQuotes ->
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            SearchUiState(
                query = query,
                selectedFilter = filter,
                results = emptyList(),
                matchingTopics = emptyList(),
                isSearching = false
            )
        } else {
            val allCategories = allQuotes.map { it.category }.distinct().sorted()
            val matchedTopics = if (filter == SearchFilter.ALL || filter == SearchFilter.TOPICS) {
                allCategories.filter { it.contains(trimmed, ignoreCase = true) }
            } else {
                emptyList()
            }

            val filteredQuotes = allQuotes.filter { quote ->
                when (filter) {
                    SearchFilter.ALL ->
                        quote.text.contains(trimmed, ignoreCase = true) ||
                                quote.author.contains(trimmed, ignoreCase = true) ||
                                quote.category.contains(trimmed, ignoreCase = true) ||
                                quote.tags.any { it.contains(trimmed, ignoreCase = true) }
                    SearchFilter.QUOTES ->
                        quote.text.contains(trimmed, ignoreCase = true)
                    SearchFilter.TOPICS ->
                        quote.category.contains(trimmed, ignoreCase = true) ||
                                quote.tags.any { it.contains(trimmed, ignoreCase = true) }
                    SearchFilter.AUTHORS ->
                        quote.author.contains(trimmed, ignoreCase = true)
                }
            }

            SearchUiState(
                query = query,
                selectedFilter = filter,
                results = filteredQuotes,
                matchingTopics = matchedTopics,
                isSearching = true
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onFilterSelected(filter: SearchFilter) {
        _selectedFilter.value = filter
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
