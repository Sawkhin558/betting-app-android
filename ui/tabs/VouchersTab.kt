package com.example.bettingapp.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bettingapp.database.VoucherEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VouchersTab(
    modifier: Modifier = Modifier,
    viewModel: VouchersViewModel = viewModel()
) {
    val vouchers by viewModel.vouchers.collectAsState(emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedVoucherId by remember { mutableStateOf<Long?>(null) }
    var showForwardDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vouchers",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Total: ${vouchers.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Vouchers list
        if (vouchers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "No vouchers",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No vouchers yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Create vouchers in the Entry tab",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vouchers) { voucher ->
                    VoucherCard(
                        voucher = voucher,
                        onEdit = { /* TODO: Implement edit */ },
                        onDelete = {
                            selectedVoucherId = voucher.id
                            showDeleteDialog = true
                        },
                        onForward = {
                            selectedVoucherId = voucher.id
                            showForwardDialog = true
                        }
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Voucher") },
            text = { Text("Are you sure you want to delete this voucher? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedVoucherId?.let { viewModel.deleteVoucher(it) }
                        showDeleteDialog = false
                        selectedVoucherId = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedVoucherId = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Forward confirmation dialog
    if (showForwardDialog) {
        AlertDialog(
            onDismissRequest = { showForwardDialog = false },
            title = { Text("Forward to Master") },
            text = { Text("Are you sure you want to forward this voucher to Master?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedVoucherId?.let { viewModel.forwardVoucher(it) }
                        showForwardDialog = false
                        selectedVoucherId = null
                    }
                ) {
                    Text("Forward")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showForwardDialog = false
                        selectedVoucherId = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun VoucherCard(
    voucher: VoucherEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onForward: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Voucher #${voucher.id}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = formatDate(voucher.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Badge(
                    containerColor = if (voucher.isForwarded) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    contentColor = if (voucher.isForwarded) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                ) {
                    Text(
                        text = if (voucher.isForwarded) "Forwarded" else "Pending",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", voucher.totalAmount)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Entries",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${voucher.entryCount}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Preview of raw text
            if (voucher.rawText.isNotBlank()) {
                Text(
                    text = voucher.rawText.take(50) + if (voucher.rawText.length > 50) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!voucher.isForwarded) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    
                    IconButton(
                        onClick = onForward,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Forward, contentDescription = "Forward")
                    }
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return formatter.format(date)
}

// ViewModel for VouchersTab
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VouchersViewModel(
    private val repository: com.example.bettingapp.repository.BettingRepository
) : ViewModel() {
    
    private val _vouchers = MutableStateFlow<List<VoucherEntity>>(emptyList())
    val vouchers: StateFlow<List<VoucherEntity>> = _vouchers
    
    init {
        loadVouchers()
    }
    
    private fun loadVouchers() {
        viewModelScope.launch {
            repository.getAllVouchers().collect { vouchersList ->
                _vouchers.value = vouchersList
            }
        }
    }
    
    fun deleteVoucher(id: Long) {
        viewModelScope.launch {
            repository.deleteVoucher(id)
        }
    }
    
    fun forwardVoucher(id: Long) {
        viewModelScope.launch {
            repository.markVoucherAsForwarded(id)
        }
    }
}