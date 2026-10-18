package com.myplanetservices.auraquotes.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.myplanetservices.auraquotes.data.local.AuraDatabase
import com.myplanetservices.auraquotes.data.repository.QuoteRepository
import java.util.Calendar

class DailyReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AuraDatabase.getInstance(appContext)
            val quoteDao = database.quoteDao()
            val quotes = quoteDao.getAllQuotesList()
            if (quotes.isNotEmpty()) {
                val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                val entity = quotes[dayOfYear % quotes.size]
                val repository = QuoteRepository(quoteDao)
                val quote = repository.toQuote(entity)
                DailyReminderManager(appContext).showDailyInspirationNotification(quote)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
