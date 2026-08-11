package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        Wilaya::class,
        Property::class,
        Favorite::class,
        VisitRequest::class,
        Conversation::class,
        Message::class,
        SavedSearch::class,
        Report::class,
        VerificationDoc::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KriDarDatabase : RoomDatabase() {

    abstract fun propertyDao(): PropertyDao
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun visitDao(): VisitDao
    abstract fun messageDao(): MessageDao
    abstract fun savedSearchDao(): SavedSearchDao
    abstract fun reportDao(): ReportDao
    abstract fun verificationDao(): VerificationDao

    companion object {
        @Volatile
        private var INSTANCE: KriDarDatabase? = null

        fun getInstance(context: Context): KriDarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KriDarDatabase::class.java,
                    "kridar_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
