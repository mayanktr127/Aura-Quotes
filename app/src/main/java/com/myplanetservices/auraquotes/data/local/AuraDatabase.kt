package com.myplanetservices.auraquotes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        QuoteEntity::class,
        FavoriteEntity::class,
        CollectionEntity::class,
        CollectionQuoteCrossRef::class,
        ReflectionEntity::class,
        HistoryEntity::class,
        UserQuoteEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: AuraDatabase? = null

        fun getInstance(context: Context): AuraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuraDatabase::class.java,
                    "aura_quotes.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
