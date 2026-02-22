package com.example.bettingapp.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectAsState
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun ProfitTab(
    modifier: Modifier = Modifier,
    viewModel: ProfitViewModel = viewModel()
) {
    val profitData by viewModel.profitData.collectAsState(initial = emptyList())
    val summary by viewModel.profitSummary.collectAsState(initial = ProfitSummary())
    val selectedPeriod by viewModel.selectedPeriod.collectAsState(initial = ProfitPeriod.DAILY)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period selector
        PeriodSelector(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = { viewModel.selectPeriod(it) }
        )
        
        // Summary cards
        ProfitSummaryCards(summary = summary)
        
        // Profit chart/table
        ProfitDataTable(
            profitData = profitData,
            selectedPeriod = selectedPeriod
        )
        
        // Export/Share button
        ExportButton(
            onExport = { viewModel.exportProfitData() }
        )
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: ProfitPeriod,
    onPeriodSelected: (ProfitPeriod) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Profit Period",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfitPeriod.values().forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodSelected(period) },
                        label = { Text(period.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfitSummaryCards(summary: ProfitSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Profit Card
        SummaryCard(
            title = "Total Profit",
            value = summary.totalProfit,
            icon = Icons.Default.AttachMoney,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Commission Card
        SummaryCard(
            title = "Commission",
            value = summary.totalCommission,
            icon = Icons.Default.Percent,
            color = MaterialTheme.colorScheme.secondary
        )
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Payouts Card
        SummaryCard(
            title = "Total Payouts",
            value = summary.totalPayouts,
            icon = Icons.Default.Payments,
            color = MaterialTheme.colorScheme.tertiary
        )
        
        // Net Profit Card
        SummaryCard(
            title = "Net Profit",
            value = summary.netProfit,
            icon = Icons.Default.TrendingUp,
            color = if (summary.netProfit >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.weight(1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "$${String.format("%.2f", value)}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun ProfitDataTable(
    profitData: List<ProfitDataPoint>,
    selectedPeriod: ProfitPeriod
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Profit Details",
                style = MaterialTheme.typography.headlineSmall
            )
            
            if (profitData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No profit data available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Date",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "Sales",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                            Text(
                                text = "Payouts",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                            Text(
                                text = "Profit",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }
                    
                    items(profitData) { dataPoint ->
                        ProfitDataRow(
                            dataPoint = dataPoint,
                            period = selectedPeriod
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfitDataRow(
    dataPoint: ProfitDataPoint,
    period: ProfitPeriod
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date/Period
            Text(
                text = when (period) {
                    ProfitPeriod.DAILY -> dataPoint.date
                    ProfitPeriod.WEEKLY -> "Week ${dataPoint.date}"
                    ProfitPeriod.MONTHLY -> dataPoint.date
                    ProfitPeriod.YEARLY -> dataPoint.date
                },
                modifier = Modifier.weight(2f)
            )
            
            // Sales
            Text(
                text = "$${String.format("%.2f", dataPoint.sales)}",
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
            
            // Payouts
            Text(
                text = "$${String.format("%.2f", dataPoint.payouts)}",
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
            
            // Profit
            Text(
                text = "$${String.format("%.2f", dataPoint.profit)}",
                color = if (dataPoint.profit >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExportButton(
    onExport: () -> Unit
) {
    Button(
        onClick = onExport,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Export",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Export Profit Report")
    }
}

// Data classes
data class ProfitDataPoint(
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
    val averageDailyProfit: Double = 0.0,
    val bestDay: ProfitDataPoint? = null,
    val worstDay: ProfitDataPoint? = null
)

enum class ProfitPeriod(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

// ViewModel (simplified - would be more complex in real app)
class ProfitViewModel {
    val profitData: StateFlow<List<ProfitDataPoint>> = TODO("Implement with Room database")
    val profitSummary: StateFlow<ProfitSummary> = TODO("Implement with Room database")
    val selectedPeriod: StateFlow<ProfitPeriod> = TODO("Implement with mutable state")
    
    fun selectPeriod(period: ProfitPeriod) {
        // Update selected period and refresh data
    }
    
    fun exportProfitData() {
        // Export profit data to CSV or PDF
    }
}