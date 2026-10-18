package com.myplanetservices.auraquotes

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.myplanetservices.auraquotes.data.local.AuraDatabase
import com.myplanetservices.auraquotes.data.local.SeedQuotes
import com.myplanetservices.auraquotes.data.repository.QuoteRepository
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

    private lateinit var database: AuraDatabase
    private lateinit var repository: QuoteRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AuraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = QuoteRepository(database.quoteDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun readAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Aura Quotes", appName)
    }

    @Test
    fun seedQuotesPopulateProperly() = runBlocking {
        val dao = database.quoteDao()
        dao.insertQuotes(SeedQuotes.getInitialQuotes())
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
        val dao = database.quoteDao()
        dao.insertQuotes(SeedQuotes.getInitialQuotes())

        val testQuoteId = "q_1"
        repository.toggleFavorite(testQuoteId)
        val isFavAfterAdd = dao.isFavorite(testQuoteId).first()
        assertTrue(isFavAfterAdd)

        repository.toggleFavorite(testQuoteId)
        val isFavAfterRemove = dao.isFavorite(testQuoteId).first()
        assertTrue(!isFavAfterRemove)
    }
}
