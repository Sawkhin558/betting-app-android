package com.example.bettingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bettingapp.ui.dashboard.DashboardHeader
import com.example.bettingapp.ui.tabs.*
import com.example.bettingapp.database.SettingsEntity
import kotlinx.coroutines.flow.collectAsState
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            BettingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Entry", "Vouchers", "Master", "Report", "Profit", "Settings")
    
    // In a real app, these would come from ViewModel
    val dashboardViewModel: DashboardViewModel = viewModel()
    
    Column(modifier = modifier.fillMaxSize()) {
        // Dashboard header
        DashboardHeader(
            totalSales = dashboardViewModel.totalSales,
            dailyLimit = dashboardViewModel.dailyLimit,
            commissionPercentage = dashboardViewModel.commissionPercentage,
            commissionAmount = dashboardViewModel.commissionAmount,
            maxPayout = dashboardViewModel.maxPayout,
            netProfit = dashboardViewModel.netProfit,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Tab content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> EntryTab()
                1 -> VouchersTab()
                2 -> MasterHistoryTab()
                3 -> ReportTab()
                4 -> ProfitTab()
                5 -> SettingsTab()
            }
        }
        
        // Bottom navigation
        NavigationBar {
            tabs.forEachIndexed { index, title ->
                NavigationBarItem(
                    icon = {
                        // In a real app, use proper icons
                        Icon(
                            imageVector = when (index) {
                                0 -> androidx.compose.material.icons.Icons.Default.Edit
                                1 -> androidx.compose.material.icons.Icons.Default.List
                                2 -> androidx.compose.material.icons.Icons.Default.History
                                3 -> androidx.compose.material.icons.Icons.Default.BarChart
                                4 -> androidx.compose.material.icons.Icons.Default.AttachMoney
                                5 -> androidx.compose.material.icons.Icons.Default.Settings
                                else -> androidx.compose.material.icons.Icons.Default.Info
                            },
                            contentDescription = title
                        )
                    },
                    label = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }
    }
}

@Composable
fun BettingAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = when (isSystemInDarkTheme()) {
            true -> darkColorScheme()
            false -> lightColorScheme()
        },
        typography = Typography(),
        content = content
    )
}

// ViewModel for dashboard data
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: com.example.bettingapp.repository.BettingRepository
) : ViewModel() {
    
    private val _totalSales = MutableStateFlow(0.0)
    val totalSales: StateFlow<Double> = _totalSales
    
    private val _dailyLimit = MutableStateFlow(10000.0)
    val dailyLimit: StateFlow<Double> = _dailyLimit
    
    private val _commissionPercentage = MutableStateFlow(5.0)
    val commissionPercentage: StateFlow<Double> = _commissionPercentage
    
    private val _commissionAmount = MutableStateFlow(0.0)
    val commissionAmount: StateFlow<Double> = _commissionAmount
    
    private val _maxPayout = MutableStateFlow(0.0)
    val maxPayout: StateFlow<Double> = _maxPayout
    
    private val _netProfit = MutableStateFlow(0.0)
    val netProfit: StateFlow<Double> = _netProfit
    
    init {
        loadDashboardData()
        startPeriodicUpdates()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            // Load settings
            repository.getSettings().collect { settings ->
                settings?.let {
                    _dailyLimit.value = it.dailyLimit
                    _commissionPercentage.value = it.commissionPercentage
                }
            }
            
            // Load sales data
            repository.getTotalSales().also { sales ->
                _totalSales.value = sales
                _commissionAmount.value = sales * (_commissionPercentage.value / 100)
            }
            
            // Load payout data
            repository.getTotalSalesAndPayout().also { salesPayout ->
                _maxPayout.value = salesPayout?.total_potential_payout ?: 0.0
                _netProfit.value = (_totalSales.value - _commissionAmount.value) - (_maxPayout.value * 0.01) // Simplified
            }
        }
    }
    
    private fun startPeriodicUpdates() {
        viewModelScope.launch {
            // Update every 30 seconds
            while (true) {
                kotlinx.coroutines.delay(30000)
                loadDashboardData()
            }
        }
    }
    
    fun refresh() {
        loadDashboardData()
    }
}

// Color schemes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

private fun darkColorScheme() = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFBB86FC),
    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC6),
    tertiary = androidx.compose.ui.graphics.Color(0xFFCF6679)
)

private fun lightColorScheme() = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF6200EE),
    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC5),
    tertiary = androidx.compose.ui.graphics.Color(0xFF018786)
)