package com.example.bettingapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)
    
    @Update
    suspend fun updateSettings(settings: SettingsEntity)
    
    @Query("UPDATE settings SET commissionPercentage = :commission WHERE id = 1")
    suspend fun updateCommission(commission: Double)
    
    @Query("UPDATE settings SET dailyLimit = :limit WHERE id = 1")
    suspend fun updateDailyLimit(limit: Double)
    
    @Query("UPDATE settings SET weeklyLimit = :limit WHERE id = 1")
    suspend fun updateWeeklyLimit(limit: Double)
    
    @Query("UPDATE settings SET monthlyLimit = :limit WHERE id = 1")
    suspend fun updateMonthlyLimit(limit: Double)
    
    @Query("UPDATE settings SET directMultiplier = :multiplier WHERE id = 1")
    suspend fun updateDirectMultiplier(multiplier: Double)
    
    @Query("UPDATE settings SET rolledMultiplier = :multiplier WHERE id = 1")
    suspend fun updateRolledMultiplier(multiplier: Double)
}