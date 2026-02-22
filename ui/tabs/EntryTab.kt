package com.example.bettingapp.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bettingapp.parsing.BetParser
import com.example.bettingapp.database.EntryEntity
import com.example.bettingapp.database.BetType

@Composable
fun EntryTab(
    modifier: Modifier = Modifier,
    viewModel: EntryViewModel = viewModel()
) {
    var rawText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Input section
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enter Bets",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Text(
                    text = "Format examples:\n• Direct: 123=100*80\n• Rolled: 123r50",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = rawText,
                    onValueChange = {
                        rawText = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Bet entries (one per line)") },
                    placeholder = { Text("123=100*80\n456r50") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text
                    ),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!)
                        } else {
                            Text("${rawText.lines().size} lines")
                        }
                    },
                    maxLines = 10
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            rawText = ""
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                    
                    Button(
                        onClick = {
                            if (rawText.isBlank()) {
                                errorMessage = "Please enter some bets"
                                return@Button
                            }
                            
                            isSubmitting = true
                            viewModel.parseAndSubmit(rawText) { success, message ->
                                isSubmitting = false
                                if (success) {
                                    rawText = ""
                                    errorMessage = null
                                } else {
                                    errorMessage = message
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting && rawText.isNotBlank()
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing...")
                        } else {
                            Icon(Icons.Default.Add, contentDescription = "Submit")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Submit")
                        }
                    }
                }
            }
        }
        
        // Live preview section
        val previewEntries by viewModel.previewEntries.collectAsState(emptyList())
        
        if (previewEntries.isNotEmpty()) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Live Preview",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(previewEntries) { entry ->
                            EntryPreviewItem(entry = entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryPreviewItem(entry: EntryEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (entry.betType) {
                BetType.DIRECT -> MaterialTheme.colorScheme.primaryContainer
                BetType.ROLLED -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = entry.number,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = entry.betType.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", entry.amount)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Payout: $${String.format("%.2f", entry.potentialPayout)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ViewModel for EntryTab
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EntryViewModel(
    private val repository: com.example.bettingapp.repository.BettingRepository,
    private val betParser: BetParser
) : ViewModel() {
    
    private val _previewEntries = MutableStateFlow<List<EntryEntity>>(emptyList())
    val previewEntries: StateFlow<List<EntryEntity>> = _previewEntries
    
    fun parseAndSubmit(rawText: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = betParser.parseInput(rawText)
                
                if (!result.isValid) {
                    callback(false, result.errorMessage)
                    return@launch
                }
                
                // Show preview
                _previewEntries.value = result.entries
                
                // Create voucher
                val voucherId = repository.insertVoucher(
                    com.example.bettingapp.database.VoucherEntity(
                        rawText = rawText,
                        totalAmount = result.totalAmount,
                        entryCount = result.entries.size
                    )
                )
                
                // Update entries with voucher ID
                val entriesWithVoucherId = result.entries.map { it.copy(voucherId = voucherId) }
                
                // Insert entries
                repository.insertEntries(entriesWithVoucherId)
                
                callback(true, null)
                
                // Clear preview after delay
                launch {
                    kotlinx.coroutines.delay(3000)
                    _previewEntries.value = emptyList()
                }
                
            } catch (e: Exception) {
                callback(false, "Error: ${e.message}")
            }
        }
    }
}