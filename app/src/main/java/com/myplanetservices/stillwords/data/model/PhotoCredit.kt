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

data class PhotoCreditsData(
    val statement: String,
    val credits: List<PhotoCreditItem>
)

object PhotoCreditsLoader {
    private var cachedData: PhotoCreditsData? = null

    fun load(context: Context): PhotoCreditsData {
        cachedData?.let { return it }

        return try {
            val jsonString = context.assets.open("credits.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val statement = root.optString("statement", "Some backgrounds were created with AI tools.")
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
            val data = PhotoCreditsData(statement = statement, credits = list)
            cachedData = data
            data
        } catch (e: Exception) {
            PhotoCreditsData("Some backgrounds were created with AI tools.", emptyList())
        }
    }
}
