package com.example.bettingapp.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.bettingapp.database.EntryEntity
import com.example.bettingapp.database.VoucherEntity
import com.example.bettingapp.parsing.BetParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ClipboardManager(private val context: Context) {
    
    private val clipboardManager: ClipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * Copy text to clipboard
     */
    fun copyToClipboard(text: String, label: String = "Betting Data") {
        val clip = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clip)
    }
    
    /**
     * Copy parsed bets to clipboard in specified format
     */
    suspend fun copyParsedBets(
        bets: List<BetParser.ParsedBet>,
        format: CopyFormat = CopyFormat.JSON
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val text = when (format) {
                CopyFormat.JSON -> convertToJson(bets)
                CopyFormat.CSV -> convertToCsv(bets)
                CopyFormat.PLAIN_TEXT -> convertToPlainText(bets)
                is CopyFormat.CUSTOM -> convertToCustom(bets, format.template)
            }
            
            copyToClipboard(text, "Parsed Bets")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Copy entry data to clipboard
     */
    suspend fun copyEntryData(
        entries: List<EntryEntity>,
        format: CopyFormat = CopyFormat.JSON
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val text = when (format) {
                CopyFormat.JSON -> convertEntriesToJson(entries)
                CopyFormat.CSV -> convertEntriesToCsv(entries)
                CopyFormat.PLAIN_TEXT -> convertEntriesToPlainText(entries)
                is CopyFormat.CUSTOM -> convertEntriesToCustom(entries, format.template)
            }
            
            copyToClipboard(text, "Entry Data")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Copy voucher data to clipboard
     */
    suspend fun copyVoucherData(
        vouchers: List<VoucherEntity>,
        format: CopyFormat = CopyFormat.JSON
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val text = when (format) {
                CopyFormat.JSON -> convertVouchersToJson(vouchers)
                CopyFormat.CSV -> convertVouchersToCsv(vouchers)
                CopyFormat.PLAIN_TEXT -> convertVouchersToPlainText(vouchers)
                is CopyFormat.CUSTOM -> convertVouchersToCustom(vouchers, format.template)
            }
            
            copyToClipboard(text, "Voucher Data")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Copy profit report to clipboard
     */
    suspend fun copyProfitReport(
        profitData: List<ProfitData>,
        summary: ProfitSummary,
        format: CopyFormat = CopyFormat.CSV
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val text = when (format) {
                CopyFormat.JSON -> convertProfitToJson(profitData, summary)
                CopyFormat.CSV -> convertProfitToCsv(profitData, summary)
                CopyFormat.PLAIN_TEXT -> convertProfitToPlainText(profitData, summary)
                is CopyFormat.CUSTOM -> convertProfitToCustom(profitData, summary, format.template)
            }
            
            copyToClipboard(text, "Profit Report")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Check if clipboard has text
     */
    fun hasText(): Boolean {
        return clipboardManager.primaryClip?.let { clip ->
            clip.itemCount > 0 && clip.getItemAt(0).text != null
        } ?: false
    }
    
    /**
     * Get text from clipboard
     */
    fun getText(): String? {
        return clipboardManager.primaryClip?.let { clip ->
            if (clip.itemCount > 0) {
                clip.getItemAt(0).text?.toString()
            } else {
                null
            }
        }
    }
    
    /**
     * Clear clipboard
     */
    fun clearClipboard() {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""))
    }
    
    // Private conversion methods
    private fun convertToJson(bets: List<BetParser.ParsedBet>): String {
        val jsonArray = JSONArray()
        bets.forEach { bet ->
            val json = JSONObject().apply {
                put("number", bet.number)
                put("amount", bet.amount)
                put("odds", bet.odds)
                put("type", bet.type.name)
                put("timestamp", dateFormat.format(Date()))
            }
            jsonArray.put(json)
        }
        return jsonArray.toString(2) // Pretty print with 2-space indent
    }
    
    private fun convertToCsv(bets: List<BetParser.ParsedBet>): String {
        val header = "Number,Amount,Odds,Type,Timestamp\n"
        val rows = bets.joinToString("\n") { bet ->
            "${bet.number},${bet.amount},${bet.odds},${bet.type.name},${dateFormat.format(Date())}"
        }
        return header + rows
    }
    
    private fun convertToPlainText(bets: List<BetParser.ParsedBet>): String {
        return bets.joinToString("\n") { bet ->
            "${bet.number}=${bet.amount}*${bet.odds} (${bet.type.name})"
        }
    }
    
    private fun convertToCustom(bets: List<BetParser.ParsedBet>, template: String): String {
        return bets.joinToString("\n") { bet ->
            template
                .replace("{number}", bet.number.toString())
                .replace("{amount}", bet.amount.toString())
                .replace("{odds}", bet.odds.toString())
                .replace("{type}", bet.type.name)
                .replace("{timestamp}", dateFormat.format(Date()))
        }
    }
    
    private fun convertEntriesToJson(entries: List<EntryEntity>): String {
        val jsonArray = JSONArray()
        entries.forEach { entry ->
            val json = JSONObject().apply {
                put("id", entry.id)
                put("number", entry.number)
                put("amount", entry.amount)
                put("odds", entry.odds)
                put("type", entry.type.name)
                put("timestamp", dateFormat.format(entry.timestamp))
                put("voucherId", entry.voucherId)
            }
            jsonArray.put(json)
        }
        return jsonArray.toString(2)
    }
    
    private fun convertEntriesToCsv(entries: List<EntryEntity>): String {
        val header = "ID,Number,Amount,Odds,Type,Timestamp,VoucherID\n"
        val rows = entries.joinToString("\n") { entry ->
            "${entry.id},${entry.number},${entry.amount},${entry.odds},${entry.type.name},${dateFormat.format(entry.timestamp)},${entry.voucherId}"
        }
        return header + rows
    }
    
    private fun convertEntriesToPlainText(entries: List<EntryEntity>): String {
        return entries.joinToString("\n") { entry ->
            "ID: ${entry.id} | ${entry.number}=${entry.amount}*${entry.odds} (${entry.type.name}) | ${dateFormat.format(entry.timestamp)}"
        }
    }
    
    private fun convertEntriesToCustom(entries: List<EntryEntity>, template: String): String {
        return entries.joinToString("\n") { entry ->
            template
                .replace("{id}", entry.id.toString())
                .replace("{number}", entry.number.toString())
                .replace("{amount}", entry.amount.toString())
                .replace("{odds}", entry.odds.toString())
                .replace("{type}", entry.type.name)
                .replace("{timestamp}", dateFormat.format(entry.timestamp))
                .replace("{voucherId}", entry.voucherId?.toString() ?: "")
        }
    }
    
    private fun convertVouchersToJson(vouchers: List<VoucherEntity>): String {
        val jsonArray = JSONArray()
        vouchers.forEach { voucher ->
            val json = JSONObject().apply {
                put("id", voucher.id)
                put("customerName", voucher.customerName)
                put("totalAmount", voucher.totalAmount)
                put("commission", voucher.commission)
                put("payout", voucher.payout)
                put("status", voucher.status.name)
                put("createdAt", dateFormat.format(voucher.createdAt))
                put("settledAt", voucher.settledAt?.let { dateFormat.format(it) } ?: "")
            }
            jsonArray.put(json)
        }
        return jsonArray.toString(2)
    }
    
    private fun convertVouchersToCsv(vouchers: List<VoucherEntity>): String {
        val header = "ID,CustomerName,TotalAmount,Commission,Payout,Status,CreatedAt,SettledAt\n"
        val rows = vouchers.joinToString("\n") { voucher ->
            "${voucher.id},${voucher.customerName},${voucher.totalAmount},${voucher.commission},${voucher.payout},${voucher.status.name},${dateFormat.format(voucher.createdAt)},${voucher.settledAt?.let { dateFormat.format(it) } ?: ""}"
        }
        return header + rows
    }
    
    private fun convertVouchersToPlainText(vouchers: List<VoucherEntity>): String {
        return vouchers.joinToString("\n") { voucher ->
            "Voucher #${voucher.id}: ${voucher.customerName} | Total: $${voucher.totalAmount} | Payout: $${voucher.payout} | Status: ${voucher.status.name}"
        }
    }
    
    private fun convertVouchersToCustom(vouchers: List<VoucherEntity>, template: String): String {
        return vouchers.joinToString("\n") { voucher ->
            template
                .replace("{id}", voucher.id.toString())
                .replace("{customerName}", voucher.customerName)
                .replace("{totalAmount}", voucher.totalAmount.toString())
                .replace("{commission}", voucher.commission.toString())
                .replace("{payout}", voucher.payout.toString())
                .replace("{status}", voucher.status.name)
                .replace("{createdAt}", dateFormat.format(voucher.createdAt))
                .replace("{settledAt}", voucher.settledAt?.let { dateFormat.format(it) } ?: "")
        }
    }
    
    private fun convertProfitToJson(profitData: List<ProfitData>, summary: ProfitSummary): String {
        val json = JSONObject().apply {
            put("summary", JSONObject().apply {
                put("totalProfit", summary.totalProfit)
                put("totalCommission", summary.totalCommission)
                put("totalPayouts", summary.totalPayouts)
                put("netProfit", summary.netProfit)
                put("averageDailyProfit", summary.averageDailyProfit)
            })
            
            val dataArray = JSONArray()
            profitData.forEach { data ->
                dataArray.put(JSONObject().apply {
                    put("date", data.date)
                    put("sales", data.sales)
                    put("payouts", data.payouts)
                    put("profit", data.profit)
                    put("commission", data.commission)
                })
            }
            put("data", dataArray)
        }
        return json.toString(2)
    }
    
    private fun convertProfitToCsv(profitData: List<ProfitData>, summary: ProfitSummary): String {
        val header = "Date,Sales,Payouts,Profit,Commission\n"
        val rows = profitData.joinToString("\n") { data ->
            "${data.date},${data.sales},${data.payouts},${data.profit},${data.commission}"
        }
        
        val summarySection = """
            
            Summary:
            Total Profit,${summary.totalProfit}
            Total Commission,${summary.totalCommission}
            Total Payouts,${summary.totalPayouts}
            Net Profit,${summary.netProfit}
            Average Daily Profit,${summary.averageDailyProfit}
        """.trimIndent()
        
        return header + rows + summarySection
    }
    
    private fun convertProfitToPlainText(profitData: List<ProfitData>, summary: ProfitSummary): String {
        val dataSection = profitData.joinToString("\n") { data ->
            "${data.date}: Sales: $${data.sales} | Payouts: $${data.payouts} | Profit: $${data.profit}"
        }
        
        val summarySection = """
            
            SUMMARY:
            Total Profit: $${summary.totalProfit}
            Total Commission: $${summary.totalCommission}
            Total Payouts: $${summary.totalPayouts}
            Net Profit: $${summary.netProfit}
            Average Daily Profit: $${summary.averageDailyProfit}
        """.trimIndent()
        
        return dataSection + summarySection
    }
    
    private fun convertProfitToCustom(profitData: List<ProfitData>, summary: ProfitSummary, template: String): String {
        // For simplicity, just use the plain text format
        return convertProfitToPlainText(profitData, summary)
    }
}

// Data classes for clipboard operations
data class ProfitData(
    val date: String,
    val sales: Double,
    val payouts: Double,
    val profit: Double,
    val commission: Double = 0.0
)

data class ProfitSummary(
    val totalProfit: Double = 0.0,
    val totalCommission: Double = 0.0,
    val totalPayouts: Double = 0.0,
    val netProfit: Double = 0.0,
    val averageDailyProfit: Double = 0.0
)

// Copy format sealed class
sealed class CopyFormat {
    object JSON : CopyFormat()
    object CSV : CopyFormat()
    object PLAIN_TEXT : CopyFormat()
    data class CUSTOM(val template: String) : CopyFormat()
    
    companion object {
        fun fromString(format: String): CopyFormat {
            return when (format.uppercase()) {
                "JSON" -> JSON
                "CSV" -> CSV
                "PLAIN_TEXT", "TEXT" -> PLAIN_TEXT
                else -> CUSTOM(format)
            }
        }
    }
}

// Composable helper
@Composable
fun rememberClipboardManager(): ClipboardManager {
    val context = LocalContext.current
    return remember { ClipboardManager(context) }
}