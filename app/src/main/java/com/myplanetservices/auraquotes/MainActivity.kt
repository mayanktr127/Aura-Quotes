package com.myplanetservices.auraquotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myplanetservices.auraquotes.data.local.AuraDatabase
import com.myplanetservices.auraquotes.data.preferences.UserPreferences
import com.myplanetservices.auraquotes.data.preferences.UserPreferencesRepository
import com.myplanetservices.auraquotes.data.repository.QuoteRepository
import com.myplanetservices.auraquotes.notification.DailyReminderManager
import com.myplanetservices.auraquotes.ui.navigation.AuraApp
import com.myplanetservices.auraquotes.ui.theme.AuraTheme
import com.myplanetservices.auraquotes.util.InAppUpdateHelper

class MainActivity : ComponentActivity() {

    private var inAppUpdateHelper: InAppUpdateHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AuraDatabase.getInstance(applicationContext)
        val quoteRepository = QuoteRepository(database.quoteDao())
        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val reminderManager = DailyReminderManager(applicationContext)

        val isUpdateDownloaded = mutableStateOf(false)
        inAppUpdateHelper = InAppUpdateHelper(this) {
            isUpdateDownloaded.value = true
        }.also { it.start() }

        setContent {
            val userPrefs by preferencesRepository.userPreferencesFlow
                .collectAsStateWithLifecycle(initialValue = UserPreferences())

            val isDarkTheme = when (userPrefs.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            val snackbarHostState = remember { SnackbarHostState() }
            val updateDownloaded by isUpdateDownloaded

            LaunchedEffect(updateDownloaded) {
                if (updateDownloaded) {
                    val result = snackbarHostState.showSnackbar(
                        message = "An update has just been downloaded.",
                        actionLabel = "Restart",
                        duration = SnackbarDuration.Indefinite
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        inAppUpdateHelper?.completeUpdate()
                    }
                }
            }

            AuraTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuraApp(
                        quoteRepository = quoteRepository,
                        preferencesRepository = preferencesRepository,
                        reminderManager = reminderManager,
                        userPreferences = userPrefs,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdateHelper?.unregister()
    }
}
