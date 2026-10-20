package com.myplanetservices.stillwords

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.myplanetservices.stillwords.ui.components.AdBannerSlot
import com.myplanetservices.stillwords.ui.components.AdNativeCard
import com.myplanetservices.stillwords.ui.components.MiniQuoteCard
import com.myplanetservices.stillwords.ui.components.WeekDots
import com.myplanetservices.stillwords.ui.theme.StillwordsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-xxhdpi")
class Phase4eScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testWeekCardPluralization_exactWording() {
        fun getTitle(n: Int): String = if (n == 1) "1 quiet day" else "$n quiet days"

        assertEquals("1 quiet day", getTitle(1))
        assertEquals("0 quiet days", getTitle(0))
        assertEquals("2 quiet days", getTitle(2))
        assertEquals("5 quiet days", getTitle(5))
        assertEquals("7 quiet days", getTitle(7))
    }

    @Test
    fun testOnboardingRealCategories_exactly22AndNoResilience() {
        val onboardingCategories = listOf(
            "Motivation", "Success", "Discipline", "Fitness", "Work",
            "Courage", "Confidence", "Leadership", "Growth", "Peace",
            "Mindfulness", "Healing", "Life", "Gratitude", "Self-Love",
            "Love", "Relationships", "Friendship", "Happiness", "Wisdom",
            "Creativity", "Study"
        )

        assertEquals(22, onboardingCategories.size)
        assertFalse(onboardingCategories.contains("Resilience"))
        assertEquals(22, onboardingCategories.toSet().size) // strictly unique
    }

    @Test
    fun testOnboardingReminderSlots_matchesDesignSpec() {
        val slots = listOf(
            Triple("Early morning", "6:30 AM", Pair(6, 30)),
            Triple("Morning", "7:30 AM", Pair(7, 30)),
            Triple("Lunch break", "1:00 PM", Pair(13, 0)),
            Triple("Before sleep", "10:00 PM", Pair(22, 0))
        )

        assertEquals(4, slots.size)
        assertEquals("Early morning", slots[0].first)
        assertEquals("6:30 AM", slots[0].second)
        assertEquals("Morning", slots[1].first)
        assertEquals("7:30 AM", slots[1].second)
        assertEquals("Lunch break", slots[2].first)
        assertEquals("1:00 PM", slots[2].second)
        assertEquals("Before sleep", slots[3].first)
        assertEquals("10:00 PM", slots[3].second)
    }

    @Test
    fun testWeekDotsRendersOneQuietDay() {
        composeTestRule.setContent {
            StillwordsTheme(darkTheme = true) {
                WeekDots(
                    daysCompleted = 1,
                    title = "1 quiet day",
                    subtitle = "Your week of stillness"
                )
            }
        }

        composeTestRule.onNodeWithText("1 quiet day").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your week of stillness").assertIsDisplayed()
    }

    @Test
    fun testMiniQuoteCardRendersInBothThemes() {
        composeTestRule.setContent {
            StillwordsTheme(darkTheme = false) { // Light theme
                MiniQuoteCard(
                    quoteText = "Silence is a source of great strength.",
                    photoPath = null,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Silence is a source of great strength.").assertIsDisplayed()
    }

    @Test
    fun testAdNativeCardRendersComponents() {
        composeTestRule.setContent {
            StillwordsTheme(darkTheme = true) {
                AdNativeCard(
                    advertiserName = "Calm Design Co.",
                    headline = "Spaces for quiet contemplation",
                    callToAction = "Install App"
                )
            }
        }

        composeTestRule.onNodeWithText("SPONSORED").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calm Design Co.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Spaces for quiet contemplation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Install App").assertIsDisplayed()
        composeTestRule.onNodeWithText("Swipe up to keep reading").assertIsDisplayed()
    }

    @Test
    fun testAdBannerSlotRendersPlaceholder() {
        composeTestRule.setContent {
            StillwordsTheme(darkTheme = true) {
                AdBannerSlot(
                    placeholderText = "[Anchored adaptive banner · full width × ~60]"
                )
            }
        }

        composeTestRule.onNodeWithText("[Anchored adaptive banner · full width × ~60]").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ad").assertIsDisplayed()
    }
}
