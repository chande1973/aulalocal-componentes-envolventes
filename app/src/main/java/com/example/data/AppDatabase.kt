package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfile::class,
        LocalSection::class,
        SyncedStudent::class,
        UnitProgress::class,
        AttendanceRecord::class,
        InboxMessage::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classroomDao(): ClassroomDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "classroom_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    AppLogger.e("AppDatabase", "Error initializing database", e)
                    throw e
                }
            }
        }
    }
}
