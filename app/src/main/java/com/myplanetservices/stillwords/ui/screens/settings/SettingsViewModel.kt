package com.myplanetservices.stillwords.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myplanetservices.stillwords.data.preferences.UserPreferences
import com.myplanetservices.stillwords.data.preferences.UserPreferencesRepository
import com.myplanetservices.stillwords.data.repository.QuoteRepository
import com.myplanetservices.stillwords.notification.DailyReminderManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val quoteRepository: QuoteRepository,
    private val reminderManager: DailyReminderManager
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val availableCategories: StateFlow<List<String>> = quoteRepository.allCategoriesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(
                "Motivation", "Confidence", "Gratitude", "Mindfulness", "Relationships",
                "Resilience", "Creativity", "Growth", "Discipline", "Wisdom", "Peace", "Success"
            )
        )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.setDailyReminder(enabled, hour, minute)
            if (enabled) {
                reminderManager.scheduleDailyReminder(hour, minute)
            } else {
                reminderManager.cancelDailyReminder()
            }
        }
    }

    fun setQuoteTextSize(size: String) {
        viewModelScope.launch {
            preferencesRepository.setQuoteTextSize(size)
        }
    }

    fun setSelectedCategories(categories: Set<String>) {
        viewModelScope.launch {
            preferencesRepository.setSelectedCategories(categories)
        }
    }

    fun toggleCategoryInterest(category: String) {
        viewModelScope.launch {
            val current = preferences.value.selectedCategories.toMutableSet()
            if (current.contains(category)) {
                if (current.size > 1) {
                    current.remove(category)
                }
            } else {
                current.add(category)
            }
            preferencesRepository.setSelectedCategories(current)
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            val qotd = quoteRepository.allQuotesFlow.stateIn(viewModelScope).value.firstOrNull()
            if (qotd != null) {
                reminderManager.showDailyInspirationNotification(qotd)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            quoteRepository.clearHistory()
        }
    }

    fun resetPreferences() {
        viewModelScope.launch {
            preferencesRepository.resetPreferences()
        }
    }

    class Factory(
        private val preferencesRepository: UserPreferencesRepository,
        private val quoteRepository: QuoteRepository,
        private val reminderManager: DailyReminderManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferencesRepository, quoteRepository, reminderManager) as T
        }
    }
}

