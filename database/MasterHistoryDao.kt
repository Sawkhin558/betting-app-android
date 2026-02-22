package com.example.bettingapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMasterHistory(history: MasterHistoryEntity): Long
    
    @Update
    suspend fun updateMasterHistory(history: MasterHistoryEntity)
    
    @Query("SELECT * FROM master_history ORDER BY forwarded_at DESC")
    fun getAllHistory(): Flow<List<MasterHistoryEntity>>
    
    @Query("SELECT * FROM master_history WHERE voucher_id = :voucherId")
    fun getHistoryByVoucherId(voucherId: Long): Flow<MasterHistoryEntity?>
    
    @Query("SELECT * FROM master_history WHERE is_reversed = 0 ORDER BY forwarded_at DESC")
    fun getActiveHistory(): Flow<List<MasterHistoryEntity>>
    
    @Query("SELECT * FROM master_history WHERE is_reversed = 1 ORDER BY reversed_at DESC")
    fun getReversedHistory(): Flow<List<MasterHistoryEntity>>
    
    @Query("UPDATE master_history SET is_reversed = 1, reversed_at = :timestamp, reversal_reason = :reason WHERE id = :id")
    suspend fun markAsReversed(id: Long, timestamp: Long, reason: String)
    
    @Query("SELECT SUM(forwarded_amount) FROM master_history WHERE is_reversed = 0")
    suspend fun getTotalForwardedAmount(): Double?
    
    @Query("""
        SELECT SUM(forwarded_amount) 
        FROM master_history 
        WHERE is_reversed = 0 
        AND forwarded_at >= :startTimestamp 
        AND forwarded_at <= :endTimestamp
    """)
    suspend fun getForwardedAmountInPeriod(startTimestamp: Long, endTimestamp: Long): Double?
}