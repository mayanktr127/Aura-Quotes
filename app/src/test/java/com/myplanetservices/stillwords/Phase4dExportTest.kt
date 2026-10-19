package com.myplanetservices.stillwords

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.myplanetservices.stillwords.data.local.SeedQuotes
import com.myplanetservices.stillwords.data.model.CardAspectRatio
import com.myplanetservices.stillwords.data.model.CardStyle
import com.myplanetservices.stillwords.data.model.PhotoFilter
import com.myplanetservices.stillwords.data.model.QuoteFont
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.util.ImageExportUtil
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase4dExportTest {

    @Test
    fun exportThreeLongestQuotes_inAllThreeFormats_atFontScale1_0_and_1_3_withTwoFilters() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val backgroundRepo = BackgroundRepository(context)

        val allQuotes = SeedQuotes.loadQuotesFromAssets(context)
        val longestQuotes = allQuotes.sortedByDescending { it.text.length }.take(3)
        assertEquals(3, longestQuotes.size)

        val rootDir = if (File("docs").exists()) File(".") else File("..")
        val exportDir = File(rootDir, "docs/phase4d-exports").apply { mkdirs() }

        val formats = listOf(
            CardAspectRatio.SQUARE_1_1,
            CardAspectRatio.PORTRAIT_4_5,
            CardAspectRatio.STORY_9_16
        )

        val fontScales = listOf(1.0f, 1.3f)
        val testFilters = listOf(PhotoFilter.NOIR, PhotoFilter.GOLDEN)

        var exportCount = 0

        longestQuotes.forEachIndexed { quoteIdx, quote ->
            val photoPath = backgroundRepo.getBackgroundAssetPath(quote.id, quote.category)
            assertNotNull("Photo path for quote ${quote.id} should not be null", photoPath)

            formats.forEach { format ->
                fontScales.forEach { fontScale ->
                    testFilters.forEach { filter ->
                        val bitmap = ImageExportUtil.createQuoteBitmap(
                            context = context,
                            quoteText = quote.text,
                            author = quote.author,
                            category = quote.category,
                            cardStyle = CardStyle.NOIR_STILLWORDS,
                            quoteFont = QuoteFont.EDITORIAL_SERIF,
                            aspectRatio = format,
                            showWatermark = true,
                            photoAssetPath = photoPath,
                            photoFilter = filter,
                            fontScale = fontScale
                        )

                        assertNotNull(bitmap)
                        val expectedWidth = 1080
                        val expectedHeight = when (format) {
                            CardAspectRatio.SQUARE_1_1 -> 1080
                            CardAspectRatio.PORTRAIT_4_5 -> 1350
                            CardAspectRatio.STORY_9_16 -> 1920
                            CardAspectRatio.LANDSCAPE_16_9 -> 608
                        }
                        assertEquals(expectedWidth, bitmap.width)
                        assertEquals(expectedHeight, bitmap.height)

                        val formatLabel = when (format) {
                            CardAspectRatio.SQUARE_1_1 -> "1x1"
                            CardAspectRatio.PORTRAIT_4_5 -> "4x5"
                            CardAspectRatio.STORY_9_16 -> "9x16"
                            CardAspectRatio.LANDSCAPE_16_9 -> "16x9"
                        }
                        val scaleLabel = if (fontScale == 1.0f) "scale1_0" else "scale1_3"
                        val fileName = "quote${quoteIdx + 1}_${formatLabel}_${scaleLabel}_${filter.name.lowercase()}.png"
                        val outFile = File(exportDir, fileName)

                        FileOutputStream(outFile).use { fos ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                        }

                        assertTrue(outFile.exists())
                        assertTrue(outFile.length() > 0)
                        exportCount++
                    }
                }
            }
        }

        // 3 quotes * 3 formats * 2 fontScales * 2 filters = 36 exported images
        assertEquals(36, exportCount)
    }

    @Test
    fun verifyShareToWhatsAppIntent() {
        val quoteText = "Life is what happens when you're busy making other plans."
        val author = "Stillwords"
        val fakeUri = Uri.parse("content://com.myplanetservices.stillwords.fileprovider/shared_quotes/card.png")

        val whatsAppIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_STREAM, fakeUri)
            putExtra(Intent.EXTRA_TEXT, "“$quoteText” — $author\n\nShared from Stillwords")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        assertEquals(Intent.ACTION_SEND, whatsAppIntent.action)
        assertEquals("image/png", whatsAppIntent.type)
        assertEquals("com.whatsapp", whatsAppIntent.`package`)
        assertEquals(fakeUri, whatsAppIntent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
        assertTrue(whatsAppIntent.getStringExtra(Intent.EXTRA_TEXT)?.contains("Stillwords") == true)
    }
}
