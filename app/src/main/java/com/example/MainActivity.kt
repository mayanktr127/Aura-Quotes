package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AuraDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.QuoteRepository
import com.example.notification.DailyReminderManager
import com.example.ui.navigation.AuraApp
import com.example.ui.theme.AuraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AuraDatabase.getInstance(applicationContext)
        val quoteRepository = QuoteRepository(database.quoteDao())
        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val reminderManager = DailyReminderManager(applicationContext)

        setContent {
            val userPrefs by preferencesRepository.userPreferencesFlow
                .collectAsStateWithLifecycle(initialValue = UserPreferences())

            val isDarkTheme = when (userPrefs.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            AuraTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuraApp(
                        quoteRepository = quoteRepository,
                        preferencesRepository = preferencesRepository,
                        reminderManager = reminderManager,
                        userPreferences = userPrefs
                    )
                }
            }
        }
    }
}
