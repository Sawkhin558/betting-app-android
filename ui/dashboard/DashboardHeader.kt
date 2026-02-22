package com.example.bettingapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardHeader(
    totalSales: Double,
    dailyLimit: Double,
    commissionPercentage: Double,
    commissionAmount: Double,
    maxPayout: Double,
    netProfit: Double,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            TotalSalesCard(
                totalSales = totalSales,
                limit = dailyLimit,
                modifier = Modifier.width(180.dp)
            )
        }
        
        item {
            CommissionCard(
                commissionPercentage = commissionPercentage,
                commissionAmount = commissionAmount,
                modifier = Modifier.width(180.dp)
            )
        }
        
        item {
            MaxPayoutCard(
                maxPayout = maxPayout,
                modifier = Modifier.width(180.dp)
            )
        }
        
        item {
            NetProfitCard(
                netProfit = netProfit,
                modifier = Modifier.width(180.dp)
            )
        }
    }
}