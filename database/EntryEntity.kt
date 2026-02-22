package com.example.bettingapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(
    tableName = "entries",
    foreignKeys = [
        ForeignKey(
            entity = VoucherEntity::class,
            parentColumns = ["id"],
            childColumns = ["voucher_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "voucher_id", index = true)
    val voucherId: Long,
    
    @ColumnInfo(name = "number")
    val number: String, // 3-digit number
    
    @ColumnInfo(name = "bet_type")
    val betType: BetType,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "payout_multiplier")
    val payoutMultiplier: Double,
    
    @ColumnInfo(name = "potential_payout")
    val potentialPayout: Double
)

enum class BetType {
    DIRECT, // e.g., 123=100*50
    ROLLED  // e.g., 123r50
}