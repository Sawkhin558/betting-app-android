package com.example.bettingapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface VoucherDao {
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVoucher(voucher: VoucherEntity): Long
    
    @Update
    suspend fun updateVoucher(voucher: VoucherEntity)
    
    @Query("DELETE FROM vouchers WHERE id = :id")
    suspend fun deleteVoucher(id: Long)
    
    @Query("SELECT * FROM vouchers ORDER BY created_at DESC")
    fun getAllVouchers(): Flow<List<VoucherEntity>>
    
    @Query("SELECT * FROM vouchers WHERE id = :id")
    fun getVoucherById(id: Long): Flow<VoucherEntity?>
    
    @Query("SELECT * FROM vouchers WHERE is_forwarded = 0 ORDER BY created_at DESC")
    fun getPendingVouchers(): Flow<List<VoucherEntity>>
    
    @Query("SELECT * FROM vouchers WHERE is_forwarded = 1 ORDER BY forwarded_at DESC")
    fun getForwardedVouchers(): Flow<List<VoucherEntity>>
    
    @Query("UPDATE vouchers SET is_forwarded = 1, forwarded_at = :timestamp WHERE id = :id")
    suspend fun markAsForwarded(id: Long, timestamp: Long)
    
    @Query("SELECT SUM(total_amount) FROM vouchers WHERE is_forwarded = 0")
    suspend fun getTotalPendingAmount(): Double?
    
    @Transaction
    @Query("SELECT * FROM vouchers WHERE id = :voucherId")
    fun getVoucherWithEntries(voucherId: Long): Flow<VoucherWithEntries>
}

data class VoucherWithEntries(
    val voucher: VoucherEntity,
    val entries: List<EntryEntity>
)