package com.example.bettingapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        SettingsEntity::class,
        VoucherEntity::class,
        EntryEntity::class,
        MasterHistoryEntity::class,
        ClearedLimitEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun settingsDao(): SettingsDao
    abstract fun voucherDao(): VoucherDao
    abstract fun entryDao(): EntryDao
    abstract fun masterHistoryDao(): MasterHistoryDao
    abstract fun clearedLimitDao(): ClearedLimitDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "betting_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    // Add TypeConverters if needed for complex types
}