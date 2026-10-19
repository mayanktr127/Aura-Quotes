package com.myplanetservices.stillwords

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ApplicationProvider
import com.myplanetservices.stillwords.data.local.SeedQuotes
import com.myplanetservices.stillwords.ui.components.AutoShrinkQuoteText
import com.myplanetservices.stillwords.ui.components.PhotoQuoteCard
import com.myplanetservices.stillwords.ui.theme.StillwordsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LongQuoteDisplayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verifyThreeLongestQuotesOnTodayCard_at360dp_fontScale1_3() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val allQuotes = SeedQuotes.loadQuotesFromAssets(context)
        val longestQuotes = allQuotes.sortedByDescending { it.text.length }.take(3)
        assertEquals(3, longestQuotes.size)

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 2.5f, fontScale = 1.3f)
            ) {
                StillwordsTheme(darkTheme = true) {
                    Column(
                        modifier = Modifier
                            .width(360.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        longestQuotes.forEach { quote ->
                            PhotoQuoteCard(
                                quoteText = quote.text,
                                author = quote.author,
                                category = quote.category,
                                photoPath = null,
                                isFavorite = false,
                                onFavoriteToggle = {},
                                onShare = {},
                                onDownload = {},
                                onEdit = {},
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun verifyThreeLongestQuotesOnReadScreen_at360dp_fontScale1_3() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val allQuotes = SeedQuotes.loadQuotesFromAssets(context)
        val longestQuotes = allQuotes.sortedByDescending { it.text.length }.take(3)
        assertEquals(3, longestQuotes.size)

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 2.5f, fontScale = 1.3f)
            ) {
                StillwordsTheme(darkTheme = true) {
                    Column(
                        modifier = Modifier
                            .width(360.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        longestQuotes.forEach { quote ->
                            AutoShrinkQuoteText(
                                text = quote.text,
                                initialFontSize = 34.sp,
                                minFontSize = 16.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun verifyThreeLongestQuotesOnQuoteDetail_at360dp_fontScale1_3() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val allQuotes = SeedQuotes.loadQuotesFromAssets(context)
        val longestQuotes = allQuotes.sortedByDescending { it.text.length }.take(3)
        assertEquals(3, longestQuotes.size)

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 2.5f, fontScale = 1.3f)
            ) {
                StillwordsTheme(darkTheme = true) {
                    Column(
                        modifier = Modifier
                            .width(360.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        longestQuotes.forEach { quote ->
                            AutoShrinkQuoteText(
                                text = quote.text,
                                initialFontSize = 32.sp,
                                minFontSize = 16.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}
