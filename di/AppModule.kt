package com.example.bettingapp.di

import android.content.Context
import com.example.bettingapp.database.AppDatabase
import com.example.bettingapp.parsing.BetParser
import com.example.bettingapp.repository.BettingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideSettingsDao(database: AppDatabase) = database.settingsDao()
    
    @Provides
    @Singleton
    fun provideVoucherDao(database: AppDatabase) = database.voucherDao()
    
    @Provides
    @Singleton
    fun provideEntryDao(database: AppDatabase) = database.entryDao()
    
    @Provides
    @Singleton
    fun provideMasterHistoryDao(database: AppDatabase) = database.masterHistoryDao()
    
    @Provides
    @Singleton
    fun provideClearedLimitDao(database: AppDatabase) = database.clearedLimitDao()
    
    @Provides
    @Singleton
    fun provideBetParser(): BetParser {
        return BetParser(directMultiplier = 80.0, rolledMultiplier = 500.0)
    }
    
    @Provides
    @Singleton
    fun provideBettingRepository(
        settingsDao: com.example.bettingapp.database.SettingsDao,
        voucherDao: com.example.bettingapp.database.VoucherDao,
        entryDao: com.example.bettingapp.database.EntryDao,
        masterHistoryDao: com.example.bettingapp.database.MasterHistoryDao,
        clearedLimitDao: com.example.bettingapp.database.ClearedLimitDao
    ): BettingRepository {
        return BettingRepository(
            settingsDao = settingsDao,
            voucherDao = voucherDao,
            entryDao = entryDao,
            masterHistoryDao = masterHistoryDao,
            clearedLimitDao = clearedLimitDao
        )
    }
}