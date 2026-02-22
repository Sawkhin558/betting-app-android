package com.example.bettingapp.utils

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.bettingapp.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context) {
    
    private val json = Json { prettyPrint = true }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val backupDir by lazy {
        File(context.filesDir, "backups").apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * Create a complete backup of all database data
     */
    suspend fun createBackup(
        entryDao: EntryDao,
        voucherDao: VoucherDao,
        masterHistoryDao: MasterHistoryDao,
        settingsDao: SettingsDao,
        clearedLimitDao: ClearedLimitDao
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            // Collect all data
            val backupData = BackupData(
                entries = entryDao.getAllEntries(),
                vouchers = voucherDao.getAllVouchers(),
                masterHistory = masterHistoryDao.getAllHistory(),
                settings = settingsDao.getSettings(),
                clearedLimits = clearedLimitDao.getAllClearedLimits(),
                backupTimestamp = System.currentTimeMillis()
            )
            
            // Convert to JSON
            val jsonData = json.encodeToString(backupData)
            
            // Create backup file
            val timestamp = dateFormat.format(Date())
            val backupFile = File(backupDir, "backup_$timestamp.json")
            
            backupFile.writeText(jsonData)
            
            // Create compressed version
            val zipFile = File(backupDir, "backup_$timestamp.zip")
            createZipArchive(backupFile, zipFile)
            
            // Clean up JSON file
            backupFile.delete()
            
            BackupResult.Success(
                file = zipFile,
                timestamp = backupData.backupTimestamp,
                entryCount = backupData.entries.size,
                voucherCount = backupData.vouchers.size
            )
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Restore from backup file
     */
    suspend fun restoreBackup(
        backupFile: File,
        entryDao: EntryDao,
        voucherDao: VoucherDao,
        masterHistoryDao: MasterHistoryDao,
        settingsDao: SettingsDao,
        clearedLimitDao: ClearedLimitDao
    ): RestoreResult = withContext(Dispatchers.IO) {
        try {
            // Extract backup data
            val jsonData = if (backupFile.extension == "zip") {
                extractZipArchive(backupFile)
            } else {
                backupFile.readText()
            }
            
            // Parse backup data
            val backupData = json.decodeFromString<BackupData>(jsonData)
            
            // Clear existing data
            entryDao.deleteAllEntries()
            voucherDao.deleteAllVouchers()
            masterHistoryDao.deleteAllHistory()
            clearedLimitDao.deleteAllClearedLimits()
            
            // Restore data
            backupData.entries.forEach { entryDao.insertEntry(it) }
            backupData.vouchers.forEach { voucherDao.insertVoucher(it) }
            backupData.masterHistory.forEach { masterHistoryDao.insertHistory(it) }
            backupData.clearedLimits.forEach { clearedLimitDao.insertClearedLimit(it) }
            
            // Restore settings (only if not empty)
            if (backupData.settings.isNotEmpty()) {
                settingsDao.deleteAllSettings()
                backupData.settings.forEach { settingsDao.insertSettings(it) }
            }
            
            RestoreResult.Success(
                timestamp = backupData.backupTimestamp,
                entryCount = backupData.entries.size,
                voucherCount = backupData.vouchers.size,
                restoredItems = backupData.entries.size + backupData.vouchers.size + 
                               backupData.masterHistory.size + backupData.clearedLimits.size
            )
        } catch (e: Exception) {
            RestoreResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Export data to CSV format
     */
    suspend fun exportToCsv(
        entryDao: EntryDao,
        voucherDao: VoucherDao,
        outputDir: File = backupDir
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val timestamp = dateFormat.format(Date())
            
            // Export entries
            val entries = entryDao.getAllEntries()
            val entriesFile = File(outputDir, "entries_$timestamp.csv")
            exportEntriesToCsv(entries, entriesFile)
            
            // Export vouchers
            val vouchers = voucherDao.getAllVouchers()
            val vouchersFile = File(outputDir, "vouchers_$timestamp.csv")
            exportVouchersToCsv(vouchers, vouchersFile)
            
            // Export master history
            val history = masterHistoryDao.getAllHistory()
            val historyFile = File(outputDir, "history_$timestamp.csv")
            exportHistoryToCsv(history, historyFile)
            
            ExportResult.Success(
                entriesFile = entriesFile,
                vouchersFile = vouchersFile,
                historyFile = historyFile,
                entryCount = entries.size,
                voucherCount = vouchers.size,
                historyCount = history.size
            )
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Import data from CSV files
     */
    suspend fun importFromCsv(
        entriesFile: File? = null,
        vouchersFile: File? = null,
        historyFile: File? = null,
        entryDao: EntryDao,
        voucherDao: VoucherDao,
        masterHistoryDao: MasterHistoryDao
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            var importedEntries = 0
            var importedVouchers = 0
            var importedHistory = 0
            
            // Import entries
            entriesFile?.let { file ->
                val entries = importEntriesFromCsv(file)
                entries.forEach { entryDao.insertEntry(it) }
                importedEntries = entries.size
            }
            
            // Import vouchers
            vouchersFile?.let { file ->
                val vouchers = importVouchersFromCsv(file)
                vouchers.forEach { voucherDao.insertVoucher(it) }
                importedVouchers = vouchers.size
            }
            
            // Import history
            historyFile?.let { file ->
                val history = importHistoryFromCsv(file)
                history.forEach { masterHistoryDao.insertHistory(it) }
                importedHistory = history.size
            }
            
            ImportResult.Success(
                importedEntries = importedEntries,
                importedVouchers = importedVouchers,
                importedHistory = importedHistory
            )
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Get list of available backups
     */
    fun getAvailableBackups(): List<BackupInfo> {
        return backupDir.listFiles { file ->
            file.name.startsWith("backup_") && (file.extension == "json" || file.extension == "zip")
        }?.map { file ->
            BackupInfo(
                file = file,
                size = file.length(),
                timestamp = extractTimestampFromFilename(file.name),
                type = if (file.extension == "zip") BackupType.COMPRESSED else BackupType.JSON
            )
        }?.sortedByDescending { it.timestamp } ?: emptyList()
    }
    
    /**
     * Delete old backups (keep only last N)
     */
    fun cleanupOldBackups(keepLast: Int = 10) {
        val backups = getAvailableBackups()
        if (backups.size > keepLast) {
            backups.drop(keepLast).forEach { it.file.delete() }
        }
    }
    
    // Private helper methods
    private fun createZipArchive(sourceFile: File, zipFile: File) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            val entry = ZipEntry(sourceFile.name)
            zos.putNextEntry(entry)
            sourceFile.inputStream().copyTo(zos)
            zos.closeEntry()
        }
    }
    
    private fun extractZipArchive(zipFile: File): String {
        val tempDir = File(context.cacheDir, "backup_extract")
        if (!tempDir.exists()) tempDir.mkdirs()
        
        val jsonFile = File(tempDir, "backup.json")
        
        java.util.zip.ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".json")) {
                    FileOutputStream(jsonFile).use { fos ->
                        zis.copyTo(fos)
                    }
                    break
                }
                entry = zis.nextEntry
            }
        }
        
        return jsonFile.readText()
    }
    
    private fun exportEntriesToCsv(entries: List<EntryEntity>, outputFile: File) {
        val header = "ID,Number,Amount,Odds,Type,Timestamp,VoucherID\n"
        val rows = entries.joinToString("\n") { entry ->
            "${entry.id},${entry.number},${entry.amount},${entry.odds},${entry.type.name},${entry.timestamp},${entry.voucherId ?: ""}"
        }
        outputFile.writeText(header + rows)
    }
    
    private fun exportVouchersToCsv(vouchers: List<VoucherEntity>, outputFile: File) {
        val header = "ID,CustomerName,TotalAmount,Commission,Payout,Status,CreatedAt,SettledAt\n"
        val rows = vouchers.joinToString("\n") { voucher ->
            "${voucher.id},${voucher.customerName},${voucher.totalAmount},${voucher.commission},${voucher.payout},${voucher.status.name},${voucher.createdAt},${voucher.settledAt ?: ""}"
        }
        outputFile.writeText(header + rows)
    }
    
    private fun exportHistoryToCsv(history: List<MasterHistoryEntity>, outputFile: File) {
        val header = "ID,Number,Amount,Odds,Type,Timestamp\n"
        val rows = history.joinToString("\n") { h ->
            "${h.id},${h.number},${h.amount},${h.odds},${h.type.name},${h.timestamp}"
        }
        outputFile.writeText(header + rows)
    }
    
    private fun importEntriesFromCsv(file: File): List<EntryEntity> {
        val lines = file.readLines()
        if (lines.size < 2) return emptyList()
        
        return lines.drop(1).mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size >= 6) {
                EntryEntity(
                    id = 0, // Auto-generated by Room
                    number = parts[1].toInt(),
                    amount = parts[2].toDouble(),
                    odds = parts[3].toDouble(),
                    type = BetType.valueOf(parts[4]),
                    timestamp = parts[5].toLong(),
                    voucherId = if (parts.size > 6 && parts[6].isNotBlank()) parts[6].toLong() else null
                )
            } else {
                null
            }
        }
    }
    
    private fun importVouchersFromCsv(file: File): List<VoucherEntity> {
        val lines = file.readLines()
        if (lines.size < 2) return emptyList()
        
        return lines.drop(1).mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size >= 8) {
                VoucherEntity(
                    id = 0, // Auto-generated by Room
                    customerName = parts[1],
                    totalAmount = parts[2].toDouble(),
                    commission = parts[3].toDouble(),
                    payout = parts[4].toDouble(),
                    status = VoucherStatus.valueOf(parts[5]),
                    createdAt = parts[6].toLong(),
                    settledAt = if (parts[7].isNotBlank()) parts[7].toLong() else null
                )
            } else {
                null
            }
        }
    }
    
    private fun importHistoryFromCsv(file: File): List<MasterHistoryEntity> {
        val lines = file.readLines()
        if (lines.size < 2) return emptyList()
        
        return lines.drop(1).mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size >= 6) {
                MasterHistoryEntity(
                    id = 0, // Auto-generated by Room
                    number = parts[1].toInt(),
                    amount = parts[2].toDouble(),
                    odds = parts[3].toDouble(),
                    type = BetType.valueOf(parts[4]),
                    timestamp = parts[5].toLong()
                )
            } else {
                null
            }
        }
    }
    
    private fun extractTimestampFromFilename(filename: String): Long {
        val pattern = "backup_(\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2})".toRegex()
        val match = pattern.find(filename)
        return match?.groupValues?.get(1)?.let { dateString ->
            try {
                dateFormat.parse(dateString)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        } ?: 0L
    }
}

// Data classes
@Serializable
data class BackupData(
    val entries: List<EntryEntity>,
    val vouchers: List<VoucherEntity>,
    val masterHistory: List<MasterHistoryEntity>,
    val settings: List<SettingsEntity>,
    val clearedLimits: List<ClearedLimitEntity>,
    val backupTimestamp: Long
)

data class BackupInfo(
    val file: File,
    val size: Long,
    val timestamp: Long,
    val type: BackupType
)

enum class BackupType {
    JSON, COMPRESSED
}

// Result classes
sealed class BackupResult {
    data class Success(
        val file: File,
        val timestamp: Long,
        val entryCount: Int,
        val voucherCount: Int
    ) : BackupResult()
    
    data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(
        val timestamp: Long,
        val entryCount: Int,
        val voucherCount: Int,
        val restoredItems: Int
    ) : RestoreResult()
    
    data class Error(val message: String) : RestoreResult()
}

sealed class ExportResult {
    data class Success(
        val entriesFile: File,
        val vouchersFile: File,
        val historyFile: File,
        val entryCount: Int,
        val voucherCount: Int,
        val historyCount: Int
    ) : ExportResult()
    
    data class Error(val message: String) : ExportResult()
}

sealed class ImportResult {
    data class Success(
        val importedEntries: Int,
        val importedVouchers: Int,
        val importedHistory: Int
    ) : ImportResult()
    
    data class Error(val message: String) : ImportResult()
}

// Composable helper
@Composable
fun rememberBackupManager(): BackupManager {
    val context = LocalContext.current
    return remember { BackupManager(context) }
}