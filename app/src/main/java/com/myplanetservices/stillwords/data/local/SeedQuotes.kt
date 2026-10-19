package com.myplanetservices.stillwords.data.local

import android.content.Context
import org.json.JSONArray

object SeedQuotes {

    private var translatorMapCache: Map<String, String>? = null

    fun getTranslatorMap(context: Context): Map<String, String> {
        translatorMapCache?.let { return it }
        return try {
            val jsonString = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val map = HashMap<String, String>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val tr = obj.optString("translator", "")
                if (tr.isNotBlank()) {
                    map[id] = tr
                }
            }
            translatorMapCache = map
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Loads the initial curated quotes catalog from app/src/main/assets/quotes.json.
     * All quotes have been audited to comply with AGENTS.md rules.
     */
    fun loadQuotesFromAssets(context: Context): List<QuoteEntity> {
        return try {
            val jsonString = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val list = ArrayList<QuoteEntity>(jsonArray.length())
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    QuoteEntity(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        author = obj.optString("author", "Stillwords"),
                        category = obj.getString("category"),
                        tags = obj.optString("tags", ""),
                        moods = obj.optString("moods", ""),
                        situations = obj.optString("situations", ""),
                        explanation = obj.optString("explanation", ""),
                        source = obj.optString("source", ""),
                        language = obj.optString("language", "en"),
                        isFeatured = obj.optBoolean("isFeatured", false),
                        defaultStyle = obj.optString("defaultStyle", "NOIR_STILLWORDS"),
                        isOriginal = obj.optBoolean("isOriginal", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getInitialCollections(): List<CollectionEntity> = listOf(
        CollectionEntity(
            id = 1,
            name = "Morning Stillness",
            description = "Gentle wisdom to begin your day with clarity",
            iconName = "WbSunny",
            colorHex = "#E5A93C"
        ),
        CollectionEntity(
            id = 2,
            name = "Deep Motivation",
            description = "Fuel for discipline, focus, and unstoppable drive",
            iconName = "Bolt",
            colorHex = "#D48B38"
        ),
        CollectionEntity(
            id = 3,
            name = "Inner Peace & Calm",
            description = "Words that slow down the heart and quiet the noise",
            iconName = "Spa",
            colorHex = "#5A6953"
        ),
        CollectionEntity(
            id = 4,
            name = "Courage & Self-Trust",
            description = "Reminders of your resilience and natural potential",
            iconName = "Shield",
            colorHex = "#8B6FA8"
        )
    )

    fun getInitialCollectionQuotes(): List<CollectionQuoteCrossRef> = listOf(
        CollectionQuoteCrossRef(collectionId = 1, quoteId = "q_1"),
        CollectionQuoteCrossRef(collectionId = 1, quoteId = "q_6"),
        CollectionQuoteCrossRef(collectionId = 1, quoteId = "q_46"),
        CollectionQuoteCrossRef(collectionId = 2, quoteId = "q_2"),
        CollectionQuoteCrossRef(collectionId = 2, quoteId = "q_7"),
        CollectionQuoteCrossRef(collectionId = 2, quoteId = "q_27"),
        CollectionQuoteCrossRef(collectionId = 3, quoteId = "q_8"),
        CollectionQuoteCrossRef(collectionId = 3, quoteId = "q_18"),
        CollectionQuoteCrossRef(collectionId = 3, quoteId = "q_21"),
        CollectionQuoteCrossRef(collectionId = 4, quoteId = "q_4"),
        CollectionQuoteCrossRef(collectionId = 4, quoteId = "q_11"),
        CollectionQuoteCrossRef(collectionId = 4, quoteId = "q_50")
    )
}
