package com.example.bettingapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "master_history")
data class MasterHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "voucher_id", index = true)
    val voucherId: Long,
    
    @ColumnInfo(name = "forwarded_amount")
    val forwardedAmount: Double,
    
    @ColumnInfo(name = "forwarded_at")
    val forwardedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "is_reversed")
    val isReversed: Boolean = false,
    
    @ColumnInfo(name = "reversed_at")
    val reversedAt: Long? = null,
    
    @ColumnInfo(name = "reversal_reason")
    val reversalReason: String? = null
)