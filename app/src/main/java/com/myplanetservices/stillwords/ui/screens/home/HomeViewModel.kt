package com.myplanetservices.stillwords.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myplanetservices.stillwords.data.model.CardStyle
import com.myplanetservices.stillwords.data.model.Quote
import com.myplanetservices.stillwords.data.preferences.UserPreferencesRepository
import com.myplanetservices.stillwords.data.repository.QuoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val quoteOfTheDay: Quote? = null,
    val heroStyle: CardStyle = CardStyle.NOIR_STILLWORDS,
    val selectedMood: String = "All",
    val moodQuotes: List<Quote> = emptyList(),
    val forYouQuotes: List<Quote> = emptyList(),
    val popularQuotes: List<Quote> = emptyList(),
    val greeting: String = "Good morning",
    val subGreeting: String = "Find something meaningful today.",
    val moods: List<String> = listOf("All", "Calm", "Focused", "Hopeful", "Need encouragement", "Motivated", "Growth", "Confidence", "Gratitude"),
    val categories: List<String> = listOf("Motivation", "Confidence", "Gratitude", "Mindfulness", "Relationships", "Resilience", "Creativity", "Growth", "Wisdom"),
    val quietDaysThisWeek: Int = 1,
    val weekDayStatuses: List<Boolean> = List(7) { false },
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: QuoteRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _heroStyle = MutableStateFlow(CardStyle.NOIR_STILLWORDS)
    private val _selectedMood = MutableStateFlow("All")

    init {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        viewModelScope.launch {
            preferencesRepository.recordAppOpened(todayStr)
        }
    }

    private val greeting: String
        get() {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when {
                hour in 5..11 -> "Good morning"
                hour in 12..16 -> "Good afternoon"
                else -> "Good evening"
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

        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = (dayOfWeek - Calendar.MONDAY + 7) % 7
        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val weekStatuses = (0..6).map {
            val dateStr = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, 1)
            dateStr in prefs.openedDates
        }
        val quietDays = weekStatuses.count { it }.coerceAtLeast(1)

        HomeUiState(
            quoteOfTheDay = qotd,
            heroStyle = heroStyle,
            selectedMood = selectedMood,
            moodQuotes = moodQuotes.take(10),
            forYouQuotes = forYou.take(8),
            popularQuotes = popular.take(8),
            greeting = greeting,
            subGreeting = "Find something meaningful today.",
            quietDaysThisWeek = quietDays,
            weekDayStatuses = weekStatuses
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

