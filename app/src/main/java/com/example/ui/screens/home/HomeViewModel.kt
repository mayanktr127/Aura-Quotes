package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CardStyle
import com.example.data.model.Quote
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.QuoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val quoteOfTheDay: Quote? = null,
    val heroStyle: CardStyle = CardStyle.NOIR_AURA,
    val selectedMood: String = "All",
    val moodQuotes: List<Quote> = emptyList(),
    val forYouQuotes: List<Quote> = emptyList(),
    val popularQuotes: List<Quote> = emptyList(),
    val greeting: String = "Good morning",
    val subGreeting: String = "Find something meaningful today.",
    val moods: List<String> = listOf("All", "Mindset", "Motivated", "Calm", "Healing", "Growth", "Confidence", "Focused", "Peace", "Hopeful"),
    val categories: List<String> = listOf("Motivation", "Wisdom", "Mindfulness", "Courage", "Peace", "Gratitude", "Discipline", "Self-Love"),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: QuoteRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _heroStyle = MutableStateFlow(CardStyle.NOIR_AURA)
    private val _selectedMood = MutableStateFlow("All")

    private val greeting: String
        get() {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when {
                hour in 5..11 -> "Good morning"
                hour in 12..16 -> "Good afternoon"
                hour in 17..21 -> "Good evening"
                else -> "Quiet night"
            }
        }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.quoteOfTheDayFlow,
        _heroStyle,
        _selectedMood,
        repository.popularQuotesFlow,
        preferencesRepository.userPreferencesFlow
    ) { qotd, heroStyle, selectedMood, popular, prefs ->
        val moodQuotes = if (selectedMood == "All") {
            repository.allQuotesFlow.firstOrNull() ?: emptyList()
        } else {
            repository.getQuotesByMood(selectedMood).firstOrNull() ?: emptyList()
        }
        val forYou = repository.getForYouQuotes(prefs.selectedCategories).firstOrNull() ?: emptyList()

        HomeUiState(
            quoteOfTheDay = qotd,
            heroStyle = heroStyle,
            selectedMood = selectedMood,
            moodQuotes = moodQuotes.take(10),
            forYouQuotes = forYou.take(8),
            popularQuotes = popular.take(8),
            greeting = greeting,
            subGreeting = "Find something meaningful today."
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(greeting = greeting)
    )

    fun onMoodSelected(mood: String) {
        _selectedMood.value = mood
    }

    fun onHeroStyleSelected(style: CardStyle) {
        _heroStyle.value = style
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

    class Factory(
        private val repository: QuoteRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository, preferencesRepository) as T
        }
    }
}
