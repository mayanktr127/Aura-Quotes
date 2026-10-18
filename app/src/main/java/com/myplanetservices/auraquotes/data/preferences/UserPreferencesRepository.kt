package com.myplanetservices.auraquotes.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val onboardingCompleted: Boolean = false,
    val selectedCategories: Set<String> = setOf("Motivation", "Mindfulness", "Wisdom", "Growth"),
    val dailyReminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 30,
    val defaultQuoteStyle: String = "NOIR_AURA",
    val defaultAspectRatio: String = "PORTRAIT_4_5"
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SELECTED_CATEGORIES = stringSetPreferencesKey("selected_categories")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val DEFAULT_QUOTE_STYLE = stringPreferencesKey("default_quote_style")
        val DEFAULT_ASPECT_RATIO = stringPreferencesKey("default_aspect_ratio")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
                onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
                selectedCategories = preferences[PreferencesKeys.SELECTED_CATEGORIES]
                    ?: setOf("Motivation", "Mindfulness", "Wisdom", "Growth"),
                dailyReminderEnabled = preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: false,
                reminderHour = preferences[PreferencesKeys.REMINDER_HOUR] ?: 8,
                reminderMinute = preferences[PreferencesKeys.REMINDER_MINUTE] ?: 30,
                defaultQuoteStyle = preferences[PreferencesKeys.DEFAULT_QUOTE_STYLE] ?: "NOIR_AURA",
                defaultAspectRatio = preferences[PreferencesKeys.DEFAULT_ASPECT_RATIO] ?: "PORTRAIT_4_5"
            )
        }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setSelectedCategories(categories: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_CATEGORIES] = categories
        }
    }

    suspend fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] = enabled
            preferences[PreferencesKeys.REMINDER_HOUR] = hour
            preferences[PreferencesKeys.REMINDER_MINUTE] = minute
        }
    }

    suspend fun setDefaultQuoteStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_QUOTE_STYLE] = style
        }
    }

    suspend fun resetPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
