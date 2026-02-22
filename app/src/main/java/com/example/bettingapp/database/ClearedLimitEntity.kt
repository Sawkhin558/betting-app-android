package com.example.bettingapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "cleared_limits")
data class ClearedLimitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "period_type")
    val periodType: PeriodType,
    
    @ColumnInfo(name = "period_start")
    val periodStart: Long,
    
    @ColumnInfo(name = "period_end")
    val periodEnd: Long,
    
    @ColumnInfo(name = "cleared_amount")
    val clearedAmount: Double,
    
    @ColumnInfo(name = "cleared_at")
    val clearedAt: Long = System.currentTimeMillis()
)

enum class PeriodType {
    DAILY,
    WEEKLY,
    MONTHLY
}