package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.model.*

@Database(
    entities = [
        Hotel::class,
        User::class,
        Audit::class,
        AuditAnswer::class,
        CorrectiveAction::class,
        QuickHazard::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hotelDao(): HotelDao
    abstract fun userDao(): UserDao
    abstract fun auditDao(): AuditDao
    abstract fun auditAnswerDao(): AuditAnswerDao
    abstract fun correctiveActionDao(): CorrectiveActionDao
    abstract fun quickHazardDao(): QuickHazardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rewaya_quality_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
