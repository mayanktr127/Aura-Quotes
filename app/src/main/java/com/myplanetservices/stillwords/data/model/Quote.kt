package com.myplanetservices.stillwords.data.model

data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val category: String,
    val tags: List<String> = emptyList(),
    val moods: List<String> = emptyList(),
    val situations: List<String> = emptyList(),
    val explanation: String = "",
    val source: String = "",
    val isFeatured: Boolean = false,
    val isOriginal: Boolean = true,
    val isUserCreated: Boolean = false,
    val isFavorite: Boolean = false,
    val reflection: String? = null,
    val translator: String? = null,
    val defaultStyle: CardStyle = CardStyle.NOIR_STILLWORDS,
    val createdAt: Long = System.currentTimeMillis()
)

data class QuoteCollection(
    val id: Long,
    val name: String,
    val description: String = "",
    val iconName: String = "Bookmark",
    val colorHex: String = "#E5A93C",
    val quoteCount: Int = 0,
    val previewQuotes: List<Quote> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class AuthorInfo(
    val name: String,
    val era: String = "",
    val role: String = "",
    val quoteCount: Int = 0
)

data class HistoryItem(
    val id: Long,
    val viewedAt: Long,
    val quote: Quote
)


