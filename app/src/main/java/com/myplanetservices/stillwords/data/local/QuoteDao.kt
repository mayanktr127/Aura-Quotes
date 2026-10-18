package com.myplanetservices.stillwords.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    // --- Curated Quotes ---
    @Query("SELECT * FROM quotes ORDER BY isFeatured DESC, id ASC")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE id = :id LIMIT 1")
    suspend fun getQuoteById(id: String): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE category = :category ORDER BY id ASC")
    fun getQuotesByCategory(category: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE moods LIKE '%' || :mood || '%' ORDER BY id ASC")
    fun getQuotesByMood(mood: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE situations LIKE '%' || :situation || '%' ORDER BY id ASC")
    fun getQuotesBySituation(situation: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE author = :author ORDER BY id ASC")
    fun getQuotesByAuthor(author: String): Flow<List<QuoteEntity>>

    @Query("""
        SELECT * FROM quotes 
        WHERE text LIKE '%' || :query || '%' 
           OR author LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%'
        ORDER BY id ASC
    """)
    fun searchQuotes(query: String): Flow<List<QuoteEntity>>

    @Query("SELECT DISTINCT category FROM quotes ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT author FROM quotes ORDER BY author ASC")
    fun getAllAuthors(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun getQuoteCount(): Int

    @Query("SELECT * FROM quotes ORDER BY id ASC")
    suspend fun getAllQuotesList(): List<QuoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewQuotes(quotes: List<QuoteEntity>)

    // --- Favorites ---
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE quoteId = :quoteId)")
    fun isFavorite(quoteId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE quoteId = :quoteId")
    suspend fun deleteFavorite(quoteId: String)

    // --- Collections ---
    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT COUNT(*) FROM collections")
    suspend fun getCollectionCount(): Int

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    suspend fun getCollectionById(id: Long): CollectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Update
    suspend fun updateCollection(collection: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollection(id: Long)

    @Query("DELETE FROM collection_quotes WHERE collectionId = :collectionId")
    suspend fun deleteCollectionQuotes(collectionId: Long)

    // Collection Quotes Cross Ref
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addQuoteToCollection(crossRef: CollectionQuoteCrossRef)

    @Query("DELETE FROM collection_quotes WHERE collectionId = :collectionId AND quoteId = :quoteId")
    suspend fun removeQuoteFromCollection(collectionId: Long, quoteId: String)

    @Query("SELECT quoteId FROM collection_quotes WHERE collectionId = :collectionId ORDER BY addedAt DESC")
    fun getQuoteIdsForCollection(collectionId: Long): Flow<List<String>>

    @Query("SELECT collectionId FROM collection_quotes WHERE quoteId = :quoteId")
    fun getCollectionIdsForQuote(quoteId: String): Flow<List<Long>>

    // --- Reflections ---
    @Query("SELECT * FROM reflections WHERE quoteId = :quoteId LIMIT 1")
    fun getReflectionForQuote(quoteId: String): Flow<ReflectionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflection(reflection: ReflectionEntity)

    @Query("DELETE FROM reflections WHERE quoteId = :quoteId")
    suspend fun deleteReflection(quoteId: String)

    // --- History ---
    @Query("SELECT * FROM quote_history ORDER BY viewedAt DESC LIMIT 50")
    fun getHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM quote_history")
    suspend fun clearHistory()

    // --- User Quotes ---
    @Query("SELECT * FROM user_quotes ORDER BY createdAt DESC")
    fun getAllUserQuotes(): Flow<List<UserQuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserQuote(userQuote: UserQuoteEntity): Long

    @Update
    suspend fun updateUserQuote(userQuote: UserQuoteEntity)

    @Query("DELETE FROM user_quotes WHERE id = :id")
    suspend fun deleteUserQuote(id: Long)
}

