package com.myplanetservices.stillwords.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val author: String,
    val category: String,
    val tags: String, // Comma-separated tags
    val moods: String, // Comma-separated moods
    val situations: String, // Comma-separated moments/situations
    val explanation: String = "",
    val source: String = "",
    val language: String = "en",
    val isFeatured: Boolean = false,
    val defaultStyle: String = "NOIR_STILLWORDS",
    val isOriginal: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val quoteId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconName: String = "Bookmark",
    val colorHex: String = "#E5A93C",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "collection_quotes",
    primaryKeys = ["collectionId", "quoteId"]
)
data class CollectionQuoteCrossRef(
    val collectionId: Long,
    val quoteId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reflections")
data class ReflectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quoteId: String,
    val note: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quote_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quoteId: String,
    val viewedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_quotes")
data class UserQuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val author: String,
    val category: String,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

