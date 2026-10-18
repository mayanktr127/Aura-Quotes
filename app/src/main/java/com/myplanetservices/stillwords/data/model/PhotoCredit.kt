package com.myplanetservices.stillwords.data.model

import android.content.Context
import org.json.JSONObject

data class PhotoCreditItem(
    val file: String,
    val originalFile: String,
    val theme: String,
    val photographer: String,
    val sourceUrl: String,
    val isAi: Boolean = false
)

data class AiSourceItem(
    val file: String,
    val originalFile: String = "",
    val tool: String = "",
    val date: String = ""
)

data class PhotoCreditsData(
    val statement: String,
    val credits: List<PhotoCreditItem>,
    val aiSources: List<AiSourceItem> = emptyList()
) {
    val hasAiEntries: Boolean
        get() = credits.any { it.isAi } || aiSources.isNotEmpty()
}

object PhotoCreditsLoader {
    private var cachedData: PhotoCreditsData? = null

    fun load(context: Context): PhotoCreditsData {
        cachedData?.let { return it }

        return try {
            val jsonString = context.assets.open("credits.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val statement = root.optString("statement", "")
            val creditsArray = root.optJSONArray("credits")
            val list = mutableListOf<PhotoCreditItem>()

            if (creditsArray != null) {
                for (i in 0 until creditsArray.length()) {
                    val obj = creditsArray.getJSONObject(i)
                    list.add(
                        PhotoCreditItem(
                            file = obj.getString("file"),
                            originalFile = obj.optString("originalFile", ""),
                            theme = obj.getString("theme"),
                            photographer = obj.getString("photographer"),
                            sourceUrl = obj.getString("sourceUrl"),
                            isAi = obj.optBoolean("isAi", false)
                        )
                    )
                }
            }

            val aiSourcesArray = root.optJSONArray("aiSources")
            val aiSourcesList = mutableListOf<AiSourceItem>()
            if (aiSourcesArray != null) {
                for (i in 0 until aiSourcesArray.length()) {
                    val obj = aiSourcesArray.getJSONObject(i)
                    aiSourcesList.add(
                        AiSourceItem(
                            file = obj.getString("file"),
                            originalFile = obj.optString("originalFile", ""),
                            tool = obj.optString("tool", ""),
                            date = obj.optString("date", "")
                        )
                    )
                }
            }

            val data = PhotoCreditsData(
                statement = statement,
                credits = list,
                aiSources = aiSourcesList
            )
            cachedData = data
            data
        } catch (e: Exception) {
            PhotoCreditsData("", emptyList(), emptyList())
        }
    }
}
