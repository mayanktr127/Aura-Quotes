package com.myplanetservices.stillwords

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.myplanetservices.stillwords.data.local.StillwordsDatabase
import com.myplanetservices.stillwords.data.preferences.UserPreferencesRepository
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.data.repository.QuoteRepository
import com.myplanetservices.stillwords.notification.DailyReminderManager
import com.myplanetservices.stillwords.ui.components.AdBannerSlot
import com.myplanetservices.stillwords.ui.components.AdNativeCard
import com.myplanetservices.stillwords.ui.screens.credits.PhotoCreditsScreen
import com.myplanetservices.stillwords.ui.screens.designer.QuoteDesignerScreen
import com.myplanetservices.stillwords.ui.screens.designer.QuoteDesignerViewModel
import com.myplanetservices.stillwords.ui.screens.explore.ExploreScreen
import com.myplanetservices.stillwords.ui.screens.explore.ExploreViewModel
import com.myplanetservices.stillwords.ui.screens.favorites.FavoritesScreen
import com.myplanetservices.stillwords.ui.screens.favorites.FavoritesViewModel
import com.myplanetservices.stillwords.ui.screens.home.HomeScreen
import com.myplanetservices.stillwords.ui.screens.home.HomeViewModel
import com.myplanetservices.stillwords.ui.screens.onboarding.OnboardingScreen
import com.myplanetservices.stillwords.ui.screens.settings.SettingsScreen
import com.myplanetservices.stillwords.ui.screens.settings.SettingsViewModel
import com.myplanetservices.stillwords.ui.theme.StillwordsTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-xxhdpi")
class GenerateScreenshotsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var database: StillwordsDatabase
    private lateinit var quoteRepository: QuoteRepository
    private lateinit var backgroundRepository: BackgroundRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var reminderManager: DailyReminderManager
    private lateinit var outputDir: File

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        userPreferencesRepository = UserPreferencesRepository(context)

        database = Room.inMemoryDatabaseBuilder(context, StillwordsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        quoteRepository = QuoteRepository(context, database.quoteDao(), testScope)
        backgroundRepository = BackgroundRepository(context)
        reminderManager = DailyReminderManager(context)

        outputDir = File("../docs/images/screens")
        outputDir.mkdirs()
    }

    @After
    fun tearDown() {
        // In-memory database is GC'd with test process; omitting close prevents
        // async ViewModel flow collectors from throwing closed-connection exceptions during teardown.
    }

    private fun capture(filename: String, content: @Composable () -> Unit) {
        composeTestRule.setContent(content)
        composeTestRule.waitForIdle()
        val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        val target = File(outputDir, filename)
        FileOutputStream(target).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        println("Saved screenshot: ${target.canonicalPath}")
    }

    @Test
    fun capture_01_welcome_dark() {
        capture("01_welcome_dark.png") {
            StillwordsTheme(darkTheme = true) {
                OnboardingScreen(userPreferencesRepository, onComplete = {}, initialStep = 0)
            }
        }
    }

    @Test
    fun capture_01_welcome_light() {
        capture("01_welcome_light.png") {
            StillwordsTheme(darkTheme = false) {
                OnboardingScreen(userPreferencesRepository, onComplete = {}, initialStep = 0)
            }
        }
    }

    @Test
    fun capture_02_onboarding_topics_dark() {
        capture("02_onboarding_topics_dark.png") {
            StillwordsTheme(darkTheme = true) {
                OnboardingScreen(userPreferencesRepository, onComplete = {}, initialStep = 1)
            }
        }
    }

    @Test
    fun capture_02_onboarding_topics_light() {
        capture("02_onboarding_topics_light.png") {
            StillwordsTheme(darkTheme = false) {
                OnboardingScreen(userPreferencesRepository, onComplete = {}, initialStep = 1)
            }
        }
    }

    @Test
    fun capture_03_onboarding_reminder_dark() {
        capture("03_onboarding_reminder_dark.png") {
            StillwordsTheme(darkTheme = true) {
                OnboardingScreen(userPreferencesRepository, onComplete = {}, initialStep = 2)
            }
        }
    }

    @Test
    fun capture_03_onboarding_reminder_light() {
        capture("03_onboarding_reminder_light.png") {
            StillwordsTheme(darkTheme = false) {
                OnboardingScreen(userPreferencesRepository, onComplete = {}, initialStep = 2)
            }
        }
    }

    @Test
    fun capture_04_settings_dark() {
        val settingsViewModel = SettingsViewModel(userPreferencesRepository, quoteRepository, reminderManager)
        capture("04_settings_dark.png") {
            StillwordsTheme(darkTheme = true) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = {},
                    onOpenOnboarding = {},
                    onNavigateToPhotoCredits = {}
                )
            }
        }
    }

    @Test
    fun capture_04_settings_light() {
        val settingsViewModel = SettingsViewModel(userPreferencesRepository, quoteRepository, reminderManager)
        capture("04_settings_light.png") {
            StillwordsTheme(darkTheme = false) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = {},
                    onOpenOnboarding = {},
                    onNavigateToPhotoCredits = {}
                )
            }
        }
    }

    @Test
    fun capture_05_credits_dark() {
        capture("05_credits_dark.png") {
            StillwordsTheme(darkTheme = true) {
                PhotoCreditsScreen(onNavigateBack = {})
            }
        }
    }

    @Test
    fun capture_05_credits_light() {
        capture("05_credits_light.png") {
            StillwordsTheme(darkTheme = false) {
                PhotoCreditsScreen(onNavigateBack = {})
            }
        }
    }

    @Test
    fun capture_06_today_dark() {
        val homeViewModel = HomeViewModel(quoteRepository, userPreferencesRepository)
        capture("06_today_dark.png") {
            StillwordsTheme(darkTheme = true) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToRead = { _, _ -> },
                    onNavigateToDetail = {},
                    onNavigateToDesigner = {},
                    onNavigateToSearch = {},
                    onNavigateToSettings = {},
                    onNavigateToCategory = {}
                )
            }
        }
    }

    @Test
    fun capture_06_today_light() {
        val homeViewModel = HomeViewModel(quoteRepository, userPreferencesRepository)
        capture("06_today_light.png") {
            StillwordsTheme(darkTheme = false) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToRead = { _, _ -> },
                    onNavigateToDetail = {},
                    onNavigateToDesigner = {},
                    onNavigateToSearch = {},
                    onNavigateToSettings = {},
                    onNavigateToCategory = {}
                )
            }
        }
    }

    @Test
    fun capture_07_ad_native_card_dark() {
        capture("07_ad_native_card_dark.png") {
            StillwordsTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    AdNativeCard(
                        advertiserName = "Calm Design Co.",
                        headline = "Spaces for quiet contemplation",
                        callToAction = "Install App"
                    )
                }
            }
        }
    }

    @Test
    fun capture_07_ad_native_card_light() {
        capture("07_ad_native_card_light.png") {
            StillwordsTheme(darkTheme = false) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    AdNativeCard(
                        advertiserName = "Calm Design Co.",
                        headline = "Spaces for quiet contemplation",
                        callToAction = "Install App"
                    )
                }
            }
        }
    }

    @Test
    fun capture_08_ad_banner_slot_dark() {
        capture("08_ad_banner_slot_dark.png") {
            StillwordsTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    AdBannerSlot()
                }
            }
        }
    }

    @Test
    fun capture_08_ad_banner_slot_light() {
        capture("08_ad_banner_slot_light.png") {
            StillwordsTheme(darkTheme = false) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    AdBannerSlot()
                }
            }
        }
    }

    @Test
    fun capture_09_explore_dark() {
        val exploreViewModel = ExploreViewModel(quoteRepository)
        capture("09_explore_dark.png") {
            StillwordsTheme(darkTheme = true) {
                ExploreScreen(
                    viewModel = exploreViewModel,
                    onNavigateToDetail = {},
                    onNavigateToDesigner = {},
                    onNavigateToSearch = {},
                    onNavigateToRead = { _, _ -> },
                    onNavigateToFavorites = {}
                )
            }
        }
    }

    @Test
    fun capture_09_explore_light() {
        val exploreViewModel = ExploreViewModel(quoteRepository)
        capture("09_explore_light.png") {
            StillwordsTheme(darkTheme = false) {
                ExploreScreen(
                    viewModel = exploreViewModel,
                    onNavigateToDetail = {},
                    onNavigateToDesigner = {},
                    onNavigateToSearch = {},
                    onNavigateToRead = { _, _ -> },
                    onNavigateToFavorites = {}
                )
            }
        }
    }

    @Test
    fun capture_10_saved_dark() {
        val favoritesViewModel = FavoritesViewModel(quoteRepository)
        capture("10_saved_dark.png") {
            StillwordsTheme(darkTheme = true) {
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onNavigateToDetail = {},
                    onNavigateToDesigner = {},
                    onNavigateToCollection = {},
                    onNavigateToRead = { _, _ -> }
                )
            }
        }
    }

    @Test
    fun capture_10_saved_light() {
        val favoritesViewModel = FavoritesViewModel(quoteRepository)
        capture("10_saved_light.png") {
            StillwordsTheme(darkTheme = false) {
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onNavigateToDetail = {},
                    onNavigateToDesigner = {},
                    onNavigateToCollection = {},
                    onNavigateToRead = { _, _ -> }
                )
            }
        }
    }

    @Test
    fun capture_11_create_dark() {
        val designerViewModel = QuoteDesignerViewModel(null, quoteRepository, backgroundRepository, userPreferencesRepository)
        capture("11_create_dark.png") {
            StillwordsTheme(darkTheme = true) {
                QuoteDesignerScreen(
                    viewModel = designerViewModel,
                    onNavigateBack = {}
                )
            }
        }
    }

    @Test
    fun capture_11_create_light() {
        val designerViewModel = QuoteDesignerViewModel(null, quoteRepository, backgroundRepository, userPreferencesRepository)
        capture("11_create_light.png") {
            StillwordsTheme(darkTheme = false) {
                QuoteDesignerScreen(
                    viewModel = designerViewModel,
                    onNavigateBack = {}
                )
            }
        }
    }
}
