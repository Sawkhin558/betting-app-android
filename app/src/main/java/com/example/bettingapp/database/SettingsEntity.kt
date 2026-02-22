package com.example.bettingapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1, // Single row
    val commissionPercentage: Double = 5.0,
    val dailyLimit: Double = 10000.0,
    val weeklyLimit: Double = 50000.0,
    val monthlyLimit: Double = 200000.0,
    val directMultiplier: Double = 80.0,
    val rolledMultiplier: Double = 500.0,
    val updatedAt: Long = System.currentTimeMillis()
)