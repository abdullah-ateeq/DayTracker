package com.example.daytracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(entities = [EventEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "day_tracker.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default starter events so user has an immediate delightful experience
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).eventDao()
                            dao.insertEvents(getInitialSampleEvents())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        fun getInitialSampleEvents(): List<EventEntity> {
            val today = LocalDate.now()
            return listOf(
                EventEntity(
                    title = "Started DayTracker Project",
                    date = today.minusDays(14),
                    description = "Building a clean, offline Android event day counter with Jetpack Compose.",
                    category = "Work",
                    iconName = "Rocket",
                    colorHex = 0xFF0EA5E9
                ),
                EventEntity(
                    title = "Graduation Day",
                    date = today.minusYears(2).minusMonths(4).minusDays(10),
                    description = "Bachelor of Science in Computer Science with Honors.",
                    category = "Education",
                    iconName = "School",
                    colorHex = 0xFF10B981
                ),
                EventEntity(
                    title = "First Job Offer",
                    date = today.minusYears(1).minusMonths(6),
                    description = "Joined tech team as an Android Software Engineer.",
                    category = "Work",
                    iconName = "Work",
                    colorHex = 0xFF6366F1
                ),
                EventEntity(
                    title = "Trip to Japan",
                    date = today.minusMonths(8).minusDays(5),
                    description = "Visited Kyoto temples and Tokyo anime districts.",
                    category = "Travel",
                    iconName = "Flight",
                    colorHex = 0xFFF59E0B
                )
            )
        }
    }
}
