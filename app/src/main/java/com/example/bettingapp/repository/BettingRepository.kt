package com.example.bettingapp.repository

import com.example.bettingapp.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BettingRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val voucherDao: VoucherDao,
    private val entryDao: EntryDao,
    private val masterHistoryDao: MasterHistoryDao,
    private val clearedLimitDao: ClearedLimitDao
) {
    
    // Settings operations
    fun getSettings(): Flow<SettingsEntity?> = settingsDao.getSettings()
    
    suspend fun updateSettings(settings: SettingsEntity) = settingsDao.updateSettings(settings)
    
    // Voucher operations
    suspend fun insertVoucher(voucher: VoucherEntity): Long = voucherDao.insertVoucher(voucher)
    
    fun getAllVouchers(): Flow<List<VoucherEntity>> = voucherDao.getAllVouchers()
    
    fun getPendingVouchers(): Flow<List<VoucherEntity>> = voucherDao.getPendingVouchers()
    
    suspend fun deleteVoucher(id: Long) = voucherDao.deleteVoucher(id)
    
    suspend fun markVoucherAsForwarded(id: Long) {
        val timestamp = System.currentTimeMillis()
        voucherDao.markAsForwarded(id, timestamp)
        // Also create master history entry
        val voucher = voucherDao.getVoucherById(id)
        voucher.collect { v ->
            v?.let {
                masterHistoryDao.insertMasterHistory(
                    MasterHistoryEntity(
                        voucherId = id,
                        forwardedAmount = it.totalAmount
                    )
                )
            }
        }
    }
    
    // Entry operations
    suspend fun insertEntries(entries: List<EntryEntity>) = entryDao.insertEntries(entries)
    
    fun getEntriesByVoucherId(voucherId: Long): Flow<List<EntryEntity>> = 
        entryDao.getEntriesByVoucherId(voucherId)
    
    // Real-time calculations
    fun getGroupedPendingEntries(): Flow<List<GroupedEntry>> = 
        entryDao.getGroupedPendingEntries()
    
    suspend fun getTotalSalesAndPayout(): SalesAndPayout? = 
        entryDao.getTotalSalesAndPayout()
    
    // Master history operations
    fun getAllMasterHistory(): Flow<List<MasterHistoryEntity>> = 
        masterHistoryDao.getAllHistory()
    
    suspend fun reverseMasterHistory(id: Long, reason: String) {
        masterHistoryDao.markAsReversed(id, System.currentTimeMillis(), reason)
    }
    
    // Limit calculations
    suspend fun getTotalSales(): Double {
        val salesPayout = entryDao.getTotalSalesAndPayout()
        val totalSales = salesPayout?.total_sales ?: 0.0
        
        // Get cleared amounts for current periods
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        // Daily cleared
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dailyStart = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val dailyEnd = calendar.timeInMillis - 1
        
        val dailyCleared = clearedLimitDao.getTotalClearedInPeriod(
            PeriodType.DAILY, dailyStart, dailyEnd
        ) ?: 0.0
        
        // Apply limit caps from settings
        val settings = settingsDao.getSettings()
        var settingsValue: SettingsEntity? = null
        settings.collect { s -> settingsValue = s }
        
        return settingsValue?.let { settings ->
            val netSales = totalSales - dailyCleared
            netSales.coerceAtMost(settings.dailyLimit)
        } ?: totalSales
    }
    
    suspend fun getSelfReport(): Double {
        val totalSales = getTotalSales()
        val settings = settingsDao.getSettings()
        var settingsValue: SettingsEntity? = null
        settings.collect { s -> settingsValue = s }
        
        return settingsValue?.let { settings ->
            totalSales.coerceAtMost(settings.dailyLimit)
        } ?: totalSales
    }
    
    // Cleared limit operations
    suspend fun clearCurrentLimit(amount: Double, periodType: PeriodType) {
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()
        
        when (periodType) {
            PeriodType.DAILY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val end = calendar.timeInMillis - 1
                
                clearedLimitDao.insertClearedLimit(
                    ClearedLimitEntity(
                        periodType = PeriodType.DAILY,
                        periodStart = start,
                        periodEnd = end,
                        clearedAmount = amount
                    )
                )
            }
            PeriodType.WEEKLY -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                val end = calendar.timeInMillis - 1
                
                clearedLimitDao.insertClearedLimit(
                    ClearedLimitEntity(
                        periodType = PeriodType.WEEKLY,
                        periodStart = start,
                        periodEnd = end,
                        clearedAmount = amount
                    )
                )
            }
            PeriodType.MONTHLY -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                val end = calendar.timeInMillis - 1
                
                clearedLimitDao.insertClearedLimit(
                    ClearedLimitEntity(
                        periodType = PeriodType.MONTHLY,
                        periodStart = start,
                        periodEnd = end,
                        clearedAmount = amount
                    )
                )
            }
        }
    }
}