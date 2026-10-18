package com.myplanetservices.stillwords

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.myplanetservices.stillwords.data.local.StillwordsDatabase
import com.myplanetservices.stillwords.data.local.SeedQuotes
import com.myplanetservices.stillwords.data.repository.QuoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: StillwordsDatabase
    private lateinit var repository: QuoteRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, StillwordsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = QuoteRepository(context, database.quoteDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun readAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Stillwords", appName)
    }

    @Test
    fun seedQuotesPopulateProperly() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dao = database.quoteDao()
        val quotesToInsert = SeedQuotes.loadQuotesFromAssets(context)
        dao.insertQuotes(quotesToInsert)
        val count = dao.getQuoteCount()
        assertTrue(count >= 50)

        val quotes = repository.allQuotesFlow.first()
        assertTrue(quotes.isNotEmpty())

        val firstQuote = quotes.first()
        assertNotNull(firstQuote.text)
        assertNotNull(firstQuote.author)
    }

    @Test
    fun favoriteToggleWorks() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dao = database.quoteDao()
        val quotesToInsert = SeedQuotes.loadQuotesFromAssets(context)
        dao.insertQuotes(quotesToInsert)

        val testQuoteId = "q_1"
        repository.toggleFavorite(testQuoteId)
        val isFavAfterAdd = dao.isFavorite(testQuoteId).first()
        assertTrue(isFavAfterAdd)

        repository.toggleFavorite(testQuoteId)
        val isFavAfterRemove = dao.isFavorite(testQuoteId).first()
        assertTrue(!isFavAfterRemove)
    }

    @Test
    fun photoCreditsContainNoAiEntries() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val data = com.myplanetservices.stillwords.data.model.PhotoCreditsLoader.load(context)
        assertTrue(data.credits.isNotEmpty())
        assertTrue(!data.hasAiEntries)
        assertTrue(data.statement.isEmpty())
    }

    @Test
    fun longestQuotesExportBitmapsSuccessfully() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val quotes = SeedQuotes.loadQuotesFromAssets(context)
        val longestQuotes = quotes.sortedByDescending { it.text.length }.take(3)
        assertEquals(3, longestQuotes.size)

        val bgRepo = com.myplanetservices.stillwords.data.repository.BackgroundRepository(context)

        for (q in longestQuotes) {
            val photoPath = bgRepo.getBackgroundAssetPath(q.id, q.category)
            for (ratio in com.myplanetservices.stillwords.data.model.CardAspectRatio.entries) {
                val bitmap = com.myplanetservices.stillwords.util.ImageExportUtil.createQuoteBitmap(
                    context = context,
                    quoteText = q.text,
                    author = q.author,
                    category = q.category,
                    cardStyle = com.myplanetservices.stillwords.data.model.CardStyle.NOIR_STILLWORDS,
                    quoteFont = com.myplanetservices.stillwords.data.model.QuoteFont.EDITORIAL_SERIF,
                    aspectRatio = ratio,
                    photoAssetPath = photoPath
                )
                assertNotNull(bitmap)
                assertTrue(bitmap.width > 0)
                assertTrue(bitmap.height > 0)
                bitmap.recycle()
            }
        }
    }
}

