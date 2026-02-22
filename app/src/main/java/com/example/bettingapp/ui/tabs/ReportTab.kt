package com.example.bettingapp.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bettingapp.database.GroupedEntry
import java.util.*

@Composable
fun ReportTab(
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel()
) {
    val selfReport by viewModel.selfReport.collectAsState(0.0)
    val masterReport by viewModel.masterReport.collectAsState(0.0)
    val groupedEntries by viewModel.groupedEntries.collectAsState(emptyList())
    var selectedFilter by remember { mutableStateOf(ReportFilter.ALL) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reports",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Icon(
                Icons.Default.Summarize,
                contentDescription = "Reports",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = "Self Report",
                value = selfReport,
                icon = Icons.Default.BarChart,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary
            )
            
            SummaryCard(
                title = "Master Report",
                value = masterReport,
                icon = Icons.Default.BarChart,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        // Filter row
        Card {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Filter by:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                FilterChip(
                    selected = selectedFilter == ReportFilter.ALL,
                    onClick = { selectedFilter = ReportFilter.ALL },
                    label = { Text("All") }
                )
                
                FilterChip(
                    selected = selectedFilter == ReportFilter.TOP_10,
                    onClick = { selectedFilter = ReportFilter.TOP_10 },
                    label = { Text("Top 10") }
                )
                
                FilterChip(
                    selected = selectedFilter == ReportFilter.HIGH_RISK,
                    onClick = { selectedFilter = ReportFilter.HIGH_RISK },
                    label = { Text("High Risk") }
                )
            }
        }
        
        // Grouped entries table
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Number Analysis",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    IconButton(
                        onClick = { /* TODO: Print/export */ }
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Table header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Number",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Filter entries
                val filteredEntries = when (selectedFilter) {
                    ReportFilter.ALL -> groupedEntries
                    ReportFilter.TOP_10 -> groupedEntries.take(10)
                    ReportFilter.HIGH_RISK -> groupedEntries.filter { it.total_amount > 1000 }
                }
                
                if (filteredEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No data available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredEntries) { entry ->
                            EntryRow(entry = entry)
                        }
                    }
                }
            }
        }
        
        // Additional stats
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Total Numbers",
                        value = groupedEntries.size.toString()
                    )
                    
                    StatItem(
                        label = "Total Amount",
                        value = "$${String.format("%.2f", groupedEntries.sumOf { it.total_amount })}"
                    )
                    
                    StatItem(
                        label = "Avg per Number",
                        value = "$${String.format("%.2f", if (groupedEntries.isNotEmpty()) groupedEntries.sumOf { it.total_amount } / groupedEntries.size else 0.0)}"
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$${String.format("%.2f", value)}",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Daily limit applied",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EntryRow(entry: GroupedEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = entry.number,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "$${String.format("%.2f", entry.total_amount)}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
    
    Divider(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

enum class ReportFilter {
    ALL, TOP_10, HIGH_RISK
}

// ViewModel for ReportTab
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReportViewModel(
    private val repository: com.example.bettingapp.repository.BettingRepository
) : ViewModel() {
    
    private val _selfReport = MutableStateFlow(0.0)
    val selfReport: StateFlow<Double> = _selfReport
    
    private val _masterReport = MutableStateFlow(0.0)
    val masterReport: StateFlow<Double> = _masterReport
    
    private val _groupedEntries = MutableStateFlow<List<GroupedEntry>>(emptyList())
    val groupedEntries: StateFlow<List<GroupedEntry>> = _groupedEntries
    
    init {
        loadReports()
        loadGroupedEntries()
    }
    
    private fun loadReports() {
        viewModelScope.launch {
            // Load self report
            repository.getSelfReport().also { report ->
                _selfReport.value = report
            }
            
            // Load master report (simplified - in real app, this would come from master)
            repository.getTotalSales().also { sales ->
                _masterReport.value = sales * 0.95 // Assuming 5% commission for master
            }
        }
    }
    
    private fun loadGroupedEntries() {
        viewModelScope.launch {
            repository.getGroupedPendingEntries().collect { entries ->
                _groupedEntries.value = entries
            }
        }
    }
    
    fun refresh() {
        loadReports()
        loadGroupedEntries()
    }
}