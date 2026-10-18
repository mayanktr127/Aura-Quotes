package com.myplanetservices.stillwords.data.local

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
abstract class StillwordsDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: StillwordsDatabase? = null

        fun getInstance(context: Context): StillwordsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StillwordsDatabase::class.java,
                    "stillwords.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

