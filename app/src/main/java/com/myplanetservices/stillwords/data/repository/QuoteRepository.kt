package com.myplanetservices.stillwords.data.repository

import android.content.Context
import com.myplanetservices.stillwords.data.local.*
import com.myplanetservices.stillwords.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class QuoteRepository(
    private val context: Context,
    private val quoteDao: QuoteDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    init {
        scope.launch {
            seedOrUpdateDatabase()
        }
    }

    private suspend fun seedOrUpdateDatabase() {
        val assetQuotes = SeedQuotes.loadQuotesFromAssets(context)
        if (assetQuotes.isNotEmpty()) {
            quoteDao.insertNewQuotes(assetQuotes)
        }
        val collectionCount = quoteDao.getCollectionCount()
        if (collectionCount == 0) {
            val collections = SeedQuotes.getInitialCollections()
            for (col in collections) {
                quoteDao.insertCollection(col)
            }
            val crossRefs = SeedQuotes.getInitialCollectionQuotes()
            for (ref in crossRefs) {
                quoteDao.addQuoteToCollection(ref)
            }
        }
    }

    // Mapping helper
    fun toQuote(
        entity: QuoteEntity,
        isFav: Boolean = false,
        reflectionText: String? = null
    ): Quote {
        val style = try {
            CardStyle.valueOf(entity.defaultStyle)
        } catch (e: Exception) {
            CardStyle.NOIR_STILLWORDS
        }
        return Quote(
            id = entity.id,
            text = entity.text,
            author = entity.author,
            category = entity.category,
            tags = entity.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            moods = entity.moods.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            situations = entity.situations.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            explanation = entity.explanation,
            source = entity.source,
            isFeatured = entity.isFeatured,
            isOriginal = entity.isOriginal,
            isUserCreated = false,
            isFavorite = isFav,
            reflection = reflectionText,
            defaultStyle = style,
            createdAt = entity.createdAt
        )
    }

    private fun userQuoteToQuote(
        entity: UserQuoteEntity,
        isFav: Boolean = false
    ): Quote {
        return Quote(
            id = "user_${entity.id}",
            text = entity.text,
            author = if (entity.author.isBlank()) "You" else entity.author,
            category = if (entity.category.isBlank()) "Personal" else entity.category,
            tags = entity.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            moods = listOf("Thoughtful"),
            situations = listOf("Personal Reflection"),
            explanation = "Created by you in Stillwords",
            source = "Personal note",
            isFeatured = false,
            isUserCreated = true,
            isFavorite = isFav,
            defaultStyle = CardStyle.WARM_LINEN,
            createdAt = entity.createdAt
        )
    }

    // All quotes combined with favorite statuses
    val allQuotesFlow: Flow<List<Quote>> = combine(
        quoteDao.getAllQuotes(),
        quoteDao.getAllFavorites(),
        quoteDao.getAllUserQuotes()
    ) { quotes, favorites, userQuotes ->
        val favSet = favorites.map { it.quoteId }.toSet()
        val mappedCurated = quotes.map { toQuote(it, favSet.contains(it.id)) }
        val mappedUser = userQuotes.map { userQuoteToQuote(it, favSet.contains("user_${it.id}")) }
        mappedUser + mappedCurated
    }

    // Favorite quotes flow
    val favoriteQuotesFlow: Flow<List<Quote>> = combine(
        allQuotesFlow,
        quoteDao.getAllFavorites()
    ) { allQuotes, favorites ->
        val favMap = favorites.associateBy { it.quoteId }
        allQuotes.filter { favMap.containsKey(it.id) }
            .sortedByDescending { favMap[it.id]?.createdAt ?: 0L }
    }

    // Get Quote by ID
    suspend fun getQuoteById(id: String): Quote? {
        if (id.startsWith("user_")) {
            val userQuoteId = id.removePrefix("user_").toLongOrNull() ?: return null
            val userQuotes = quoteDao.getAllUserQuotes().firstOrNull() ?: emptyList()
            val found = userQuotes.firstOrNull { it.id == userQuoteId } ?: return null
            val isFav = quoteDao.isFavorite(id).firstOrNull() ?: false
            return userQuoteToQuote(found, isFav)
        }
        val entity = quoteDao.getQuoteById(id) ?: return null
        val isFav = quoteDao.isFavorite(id).firstOrNull() ?: false
        val reflection = quoteDao.getReflectionForQuote(id).firstOrNull()
        return toQuote(entity, isFav, reflection?.note)
    }

    // Quote of the day (deterministic based on calendar day)
    val quoteOfTheDayFlow: Flow<Quote?> = allQuotesFlow.map { quotes ->
        if (quotes.isEmpty()) null
        else {
            val calendar = Calendar.getInstance()
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val year = calendar.get(Calendar.YEAR)
            val seedIndex = (dayOfYear + year * 7) % quotes.size
            quotes.getOrNull(seedIndex) ?: quotes.firstOrNull()
        }
    }

    // Curated For You quotes based on selected categories
    fun getForYouQuotes(selectedCategories: Set<String>): Flow<List<Quote>> {
        return allQuotesFlow.map { quotes ->
            if (selectedCategories.isEmpty()) {
                quotes.take(15)
            } else {
                quotes.filter { quote ->
                    selectedCategories.any { category ->
                        quote.category.equals(category, ignoreCase = true) ||
                                quote.tags.any { it.equals(category, ignoreCase = true) }
                    }
                }.ifEmpty { quotes.take(15) }
            }
        }
    }

    // Popular quotes flow
    val popularQuotesFlow: Flow<List<Quote>> = allQuotesFlow.map { quotes ->
        quotes.filter { it.isFeatured }.take(12)
    }

    // Search quotes
    fun searchQuotes(query: String): Flow<List<Quote>> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return flowOf(emptyList())
        return allQuotesFlow.map { quotes ->
            quotes.filter { quote ->
                quote.text.contains(cleanQuery, ignoreCase = true) ||
                        quote.author.contains(cleanQuery, ignoreCase = true) ||
                        quote.category.contains(cleanQuery, ignoreCase = true) ||
                        quote.tags.any { it.contains(cleanQuery, ignoreCase = true) } ||
                        quote.moods.any { it.contains(cleanQuery, ignoreCase = true) } ||
                        quote.situations.any { it.contains(cleanQuery, ignoreCase = true) }
            }
        }
    }

    // Filter by mood
    fun getQuotesByMood(mood: String): Flow<List<Quote>> {
        if (mood.equals("All", ignoreCase = true)) return allQuotesFlow
        return allQuotesFlow.map { quotes ->
            quotes.filter { quote ->
                quote.moods.any { it.equals(mood, ignoreCase = true) } ||
                (mood.equals("Need encouragement", ignoreCase = true) &&
                    (quote.situations.any { it.contains("difficult", ignoreCase = true) || it.contains("confidence", ignoreCase = true) || it.contains("starting", ignoreCase = true) } ||
                     quote.tags.any { it.contains("courage", ignoreCase = true) || it.contains("perseverance", ignoreCase = true) || it.contains("strength", ignoreCase = true) || it.contains("mindset", ignoreCase = true) } ||
                     quote.category.equals("Confidence", ignoreCase = true) || quote.category.equals("Resilience", ignoreCase = true) || quote.category.equals("Motivation", ignoreCase = true)))
            }
        }
    }

    // Filter by category
    fun getQuotesByCategory(category: String): Flow<List<Quote>> {
        return allQuotesFlow.map { quotes ->
            quotes.filter { quote ->
                quote.category.equals(category, ignoreCase = true)
            }
        }
    }

    // Filter by situation/moment
    fun getQuotesBySituation(situation: String): Flow<List<Quote>> {
        return allQuotesFlow.map { quotes ->
            quotes.filter { quote ->
                quote.situations.any { it.equals(situation, ignoreCase = true) }
            }
        }
    }

    // Filter by author
    fun getQuotesByAuthor(author: String): Flow<List<Quote>> {
        return allQuotesFlow.map { quotes ->
            quotes.filter { quote ->
                quote.author.equals(author, ignoreCase = true)
            }
        }
    }

    // Get related quotes for a quote
    fun getRelatedQuotes(quote: Quote): Flow<List<Quote>> {
        return allQuotesFlow.map { all ->
            all.filter { other ->
                other.id != quote.id && (
                        other.category.equals(quote.category, ignoreCase = true) ||
                                other.author.equals(quote.author, ignoreCase = true) ||
                                other.tags.any { tag -> quote.tags.contains(tag) }
                        )
            }.take(8)
        }
    }

    // Authors directory
    val authorsDirectoryFlow: Flow<List<AuthorInfo>> = allQuotesFlow.map { quotes ->
        quotes.groupBy { it.author }
            .map { (author, list) ->
                AuthorInfo(
                    name = author,
                    role = list.firstOrNull()?.source ?: "Philosopher & Thinker",
                    quoteCount = list.size
                )
            }
            .sortedByDescending { it.quoteCount }
    }

    // All categories
    val allCategoriesFlow: Flow<List<String>> = quoteDao.getAllCategories()

    // Favorite toggle
    suspend fun toggleFavorite(quoteId: String) {
        val isFav = quoteDao.isFavorite(quoteId).first()
        if (isFav) {
            quoteDao.deleteFavorite(quoteId)
        } else {
            quoteDao.insertFavorite(FavoriteEntity(quoteId = quoteId))
        }
    }

    // Collections
    val collectionsFlow: Flow<List<QuoteCollection>> = combine(
        quoteDao.getAllCollections(),
        allQuotesFlow
    ) { collections, quotes ->
        val quoteMap = quotes.associateBy { it.id }
        collections.map { col ->
            val quoteIds = quoteDao.getQuoteIdsForCollection(col.id).firstOrNull() ?: emptyList()
            val colQuotes = quoteIds.mapNotNull { quoteMap[it] }
            QuoteCollection(
                id = col.id,
                name = col.name,
                description = col.description,
                iconName = col.iconName,
                colorHex = col.colorHex,
                quoteCount = colQuotes.size,
                previewQuotes = colQuotes.take(3),
                createdAt = col.createdAt
            )
        }
    }

    fun getQuotesForCollection(collectionId: Long): Flow<List<Quote>> {
        return combine(
            quoteDao.getQuoteIdsForCollection(collectionId),
            allQuotesFlow
        ) { quoteIds, quotes ->
            val quoteMap = quotes.associateBy { it.id }
            quoteIds.mapNotNull { quoteMap[it] }
        }
    }

    suspend fun createCollection(name: String, description: String = "", colorHex: String = "#E5A93C"): Long {
        return quoteDao.insertCollection(
            CollectionEntity(
                name = name,
                description = description,
                colorHex = colorHex
            )
        )
    }

    suspend fun updateCollection(collection: CollectionEntity) {
        quoteDao.updateCollection(collection)
    }

    suspend fun deleteCollection(collectionId: Long) {
        quoteDao.deleteCollectionQuotes(collectionId)
        quoteDao.deleteCollection(collectionId)
    }

    suspend fun addQuoteToCollection(collectionId: Long, quoteId: String) {
        quoteDao.addQuoteToCollection(CollectionQuoteCrossRef(collectionId, quoteId))
    }

    suspend fun removeQuoteFromCollection(collectionId: Long, quoteId: String) {
        quoteDao.removeQuoteFromCollection(collectionId, quoteId)
    }

    fun getCollectionIdsForQuote(quoteId: String): Flow<List<Long>> {
        return quoteDao.getCollectionIdsForQuote(quoteId)
    }

    // Reflections
    fun getReflection(quoteId: String): Flow<String?> {
        return quoteDao.getReflectionForQuote(quoteId).map { it?.note }
    }

    suspend fun saveReflection(quoteId: String, note: String) {
        if (note.isBlank()) {
            quoteDao.deleteReflection(quoteId)
        } else {
            quoteDao.insertReflection(
                ReflectionEntity(
                    quoteId = quoteId,
                    note = note.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // History
    val historyQuotesFlow: Flow<List<Quote>> = combine(
        quoteDao.getHistory(),
        allQuotesFlow
    ) { historyEntries, quotes ->
        val quoteMap = quotes.associateBy { it.id }
        historyEntries.mapNotNull { quoteMap[it.quoteId] }
    }

    suspend fun recordHistory(quoteId: String) {
        quoteDao.insertHistory(HistoryEntity(quoteId = quoteId))
    }

    suspend fun clearHistory() {
        quoteDao.clearHistory()
    }

    // User created quotes
    suspend fun createUserQuote(text: String, author: String, category: String, tags: String): Long {
        return quoteDao.insertUserQuote(
            UserQuoteEntity(
                text = text.trim(),
                author = author.trim().ifEmpty { "You" },
                category = category.trim().ifEmpty { "Personal" },
                tags = tags.trim()
            )
        )
    }

    suspend fun deleteUserQuote(id: Long) {
        quoteDao.deleteUserQuote(id)
    }
}

