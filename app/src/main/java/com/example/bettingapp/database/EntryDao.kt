package com.example.bettingapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEntry(entry: EntryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEntries(entries: List<EntryEntity>)
    
    @Update
    suspend fun updateEntry(entry: EntryEntity)
    
    @Delete
    suspend fun deleteEntry(entry: EntryEntity)
    
    @Query("DELETE FROM entries WHERE voucher_id = :voucherId")
    suspend fun deleteEntriesByVoucherId(voucherId: Long)
    
    @Query("SELECT * FROM entries WHERE voucher_id = :voucherId ORDER BY id")
    fun getEntriesByVoucherId(voucherId: Long): Flow<List<EntryEntity>>
    
    @Query("SELECT * FROM entries WHERE number = :number AND bet_type = :betType")
    fun getEntriesByNumberAndType(number: String, betType: BetType): Flow<List<EntryEntity>>
    
    // Real-time calculation queries
    @Query("""
        SELECT number, SUM(amount) as total_amount 
        FROM entries 
        WHERE voucher_id IN (SELECT id FROM vouchers WHERE is_forwarded = 0)
        GROUP BY number
        ORDER BY total_amount DESC
    """)
    fun getGroupedPendingEntries(): Flow<List<GroupedEntry>>
    
    @Query("""
        SELECT number, bet_type, SUM(amount) as total_amount 
        FROM entries 
        WHERE voucher_id IN (SELECT id FROM vouchers WHERE is_forwarded = 0)
        GROUP BY number, bet_type
    """)
    fun getGroupedEntriesByType(): Flow<List<GroupedEntryWithType>>
    
    @Query("""
        SELECT 
            SUM(amount) as total_sales,
            SUM(potential_payout) as total_potential_payout
        FROM entries 
        WHERE voucher_id IN (SELECT id FROM vouchers WHERE is_forwarded = 0)
    """)
    suspend fun getTotalSalesAndPayout(): SalesAndPayout?
}

data class GroupedEntry(
    val number: String,
    val total_amount: Double
)

data class GroupedEntryWithType(
    val number: String,
    val bet_type: BetType,
    val total_amount: Double
)

data class SalesAndPayout(
    val total_sales: Double?,
    val total_potential_payout: Double?
)