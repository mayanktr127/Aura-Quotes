package com.example.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuthorInfo
import com.example.data.model.Quote
import com.example.data.repository.QuoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExploreUiState(
    val selectedFilterType: FilterType? = null,
    val selectedFilterValue: String? = null,
    val filteredQuotes: List<Quote> = emptyList(),
    val categories: List<String> = emptyList(),
    val moods: List<String> = listOf(
        "Motivated", "Calm", "Happy", "Healing", "Confident",
        "Thoughtful", "Determined", "Hopeful", "Peace", "Tired"
    ),
    val moments: List<String> = listOf(
        "Morning", "Before Work", "Study Time", "Workout",
        "Difficult Day", "Need Confidence", "Need Focus", "Before Sleep", "Starting Over"
    ),
    val authors: List<AuthorInfo> = emptyList(),
    val trendingQuotes: List<Quote> = emptyList()
)

enum class FilterType {
    CATEGORY, MOOD, MOMENT, AUTHOR
}

class ExploreViewModel(
    private val repository: QuoteRepository
) : ViewModel() {

    private val _selectedFilterType = MutableStateFlow<FilterType?>(null)
    private val _selectedFilterValue = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ExploreUiState> = combine(
        _selectedFilterType,
        _selectedFilterValue,
        repository.allQuotesFlow,
        repository.authorsDirectoryFlow
    ) { filterType, filterValue, allQuotes, authors ->
        val filtered = when {
            filterType == FilterType.CATEGORY && filterValue != null ->
                allQuotes.filter { it.category.equals(filterValue, ignoreCase = true) }
            filterType == FilterType.MOOD && filterValue != null ->
                allQuotes.filter { it.moods.any { m -> m.equals(filterValue, ignoreCase = true) } }
            filterType == FilterType.MOMENT && filterValue != null ->
                allQuotes.filter { it.situations.any { s -> s.equals(filterValue, ignoreCase = true) } }
            filterType == FilterType.AUTHOR && filterValue != null ->
                allQuotes.filter { it.author.equals(filterValue, ignoreCase = true) }
            else -> emptyList()
        }

        val allCategories = allQuotes.map { it.category }.distinct().sorted()

        ExploreUiState(
            selectedFilterType = filterType,
            selectedFilterValue = filterValue,
            filteredQuotes = filtered,
            categories = allCategories,
            authors = authors,
            trendingQuotes = allQuotes.filter { it.isFeatured }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExploreUiState()
    )

    fun selectCategory(category: String) {
        _selectedFilterType.value = FilterType.CATEGORY
        _selectedFilterValue.value = category
    }

    fun selectMood(mood: String) {
        _selectedFilterType.value = FilterType.MOOD
        _selectedFilterValue.value = mood
    }

    fun selectMoment(moment: String) {
        _selectedFilterType.value = FilterType.MOMENT
        _selectedFilterValue.value = moment
    }

    fun selectAuthor(author: String) {
        _selectedFilterType.value = FilterType.AUTHOR
        _selectedFilterValue.value = author
    }

    fun clearFilter() {
        _selectedFilterType.value = null
        _selectedFilterValue.value = null
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
            return ExploreViewModel(repository) as T
        }
    }
}
