package com.example.bettingapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "vouchers")
data class VoucherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "raw_text")
    val rawText: String,
    
    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,
    
    @ColumnInfo(name = "entry_count")
    val entryCount: Int,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "is_forwarded")
    val isForwarded: Boolean = false,
    
    @ColumnInfo(name = "forwarded_at")
    val forwardedAt: Long? = null,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null
)