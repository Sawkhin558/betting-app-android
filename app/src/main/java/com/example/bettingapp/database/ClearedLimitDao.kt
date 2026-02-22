package com.example.bettingapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClearedLimitDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClearedLimit(limit: ClearedLimitEntity)
    
    @Query("SELECT * FROM cleared_limits WHERE period_type = :periodType AND period_start <= :timestamp AND period_end >= :timestamp")
    fun getClearedLimitForPeriod(periodType: PeriodType, timestamp: Long): Flow<ClearedLimitEntity?>
    
    @Query("SELECT * FROM cleared_limits WHERE period_type = :periodType ORDER BY period_start DESC")
    fun getClearedLimitsByType(periodType: PeriodType): Flow<List<ClearedLimitEntity>>
    
    @Query("SELECT SUM(cleared_amount) FROM cleared_limits WHERE period_type = :periodType AND period_start >= :startTimestamp AND period_end <= :endTimestamp")
    suspend fun getTotalClearedInPeriod(periodType: PeriodType, startTimestamp: Long, endTimestamp: Long): Double?
    
    @Query("DELETE FROM cleared_limits WHERE period_type = :periodType AND period_start = :start AND period_end = :end")
    suspend fun deleteClearedLimit(periodType: PeriodType, start: Long, end: Long)
}