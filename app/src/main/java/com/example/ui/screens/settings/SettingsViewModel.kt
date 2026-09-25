package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.QuoteRepository
import com.example.notification.DailyReminderManager
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

    val availableCategories = listOf(
        "Motivation", "Mindfulness", "Wisdom", "Courage", "Peace",
        "Gratitude", "Growth", "Discipline", "Self-Love", "Success",
        "Creativity", "Love", "Friendship", "Leadership", "Fitness", "Study"
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.setDailyReminder(enabled, hour, minute)
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
