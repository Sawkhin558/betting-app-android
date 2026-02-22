package com.example.bettingapp.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectAsState
import kotlinx.coroutines.flow.StateFlow
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsTab(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState(initial = SettingsState())
    val backupStatus by viewModel.backupStatus.collectAsState(initial = BackupStatus.IDLE)
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Info Section
        item {
            AppInfoSection()
        }
        
        // Betting Settings Section
        item {
            BettingSettingsSection(
                settings = settings,
                onUpdateSettings = { key, value -> viewModel.updateSetting(key, value) }
            )
        }
        
        // Backup & Restore Section
        item {
            BackupRestoreSection(
                backupStatus = backupStatus,
                onBackup = { viewModel.createBackup() },
                onRestore = { viewModel.restoreBackup() },
                onExport = { viewModel.exportData() },
                onImport = { viewModel.importData() }
            )
        }
        
        // Clipboard Settings Section
        item {
            ClipboardSettingsSection(
                autoCopyEnabled = settings.autoCopyEnabled,
                copyFormat = settings.copyFormat,
                onAutoCopyChanged = { enabled -> viewModel.updateSetting("autoCopyEnabled", enabled) },
                onCopyFormatChanged = { format -> viewModel.updateSetting("copyFormat", format) }
            )
        }
        
        // Security Section
        item {
            SecuritySection(
                requirePin = settings.requirePin,
                autoLockMinutes = settings.autoLockMinutes,
                onRequirePinChanged = { enabled -> viewModel.updateSetting("requirePin", enabled) },
                onAutoLockChanged = { minutes -> viewModel.updateSetting("autoLockMinutes", minutes) }
            )
        }
        
        // About & Support Section
        item {
            AboutSupportSection()
        }
    }
}

@Composable
private fun AppInfoSection() {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App icon placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "App Icon",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                    )
                }
                
                Column {
                    Text(
                        text = "Betting Manager",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Divider()
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Database info
                InfoItem(
                    icon = Icons.Default.Storage,
                    title = "Database",
                    value = "5 tables"
                )
                
                // Last backup
                InfoItem(
                    icon = Icons.Default.Backup,
                    title = "Last Backup",
                    value = "2 hours ago"
                )
                
                // Storage used
                InfoItem(
                    icon = Icons.Default.DataUsage,
                    title = "Storage",
                    value = "12.5 MB"
                )
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun BettingSettingsSection(
    settings: SettingsState,
    onUpdateSettings: (String, Any) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Betting Settings",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Daily Limit
            SettingItem(
                icon = Icons.Default.Limit,
                title = "Daily Limit",
                description = "Maximum sales per day",
                value = "$${settings.dailyLimit}",
                action = {
                    var showDialog by remember { mutableStateOf(false) }
                    var newLimit by remember { mutableStateOf(settings.dailyLimit.toString()) }
                    
                    OutlinedButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text("Edit")
                    }
                    
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Set Daily Limit") },
                            text = {
                                OutlinedTextField(
                                    value = newLimit,
                                    onValueChange = { newLimit = it },
                                    label = { Text("Daily Limit ($)") },
                                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val limit = newLimit.toDoubleOrNull() ?: settings.dailyLimit
                                        onUpdateSettings("dailyLimit", limit)
                                        showDialog = false
                                    }
                                ) {
                                    Text("Save")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showDialog = false }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            )
            
            Divider()
            
            // Commission Percentage
            SettingItem(
                icon = Icons.Default.Percent,
                title = "Commission",
                description = "Commission percentage",
                value = "${settings.commissionPercentage}%",
                action = {
                    var showDialog by remember { mutableStateOf(false) }
                    var newCommission by remember { mutableStateOf(settings.commissionPercentage.toString()) }
                    
                    OutlinedButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text("Edit")
                    }
                    
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Set Commission") },
                            text = {
                                OutlinedTextField(
                                    value = newCommission,
                                    onValueChange = { newCommission = it },
                                    label = { Text("Commission (%)") },
                                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val commission = newCommission.toDoubleOrNull() ?: settings.commissionPercentage
                                        onUpdateSettings("commissionPercentage", commission)
                                        showDialog = false
                                    }
                                ) {
                                    Text("Save")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showDialog = false }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            )
            
            Divider()
            
            // Max Payout
            SettingItem(
                icon = Icons.Default.Payments,
                title = "Max Payout",
                description = "Maximum payout per bet",
                value = "$${settings.maxPayout}",
                action = {
                    var showDialog by remember { mutableStateOf(false) }
                    var newPayout by remember { mutableStateOf(settings.maxPayout.toString()) }
                    
                    OutlinedButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text("Edit")
                    }
                    
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Set Max Payout") },
                            text = {
                                OutlinedTextField(
                                    value = newPayout,
                                    onValueChange = { newPayout = it },
                                    label = { Text("Max Payout ($)") },
                                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val payout = newPayout.toDoubleOrNull() ?: settings.maxPayout
                                        onUpdateSettings("maxPayout", payout)
                                        showDialog = false
                                    }
                                ) {
                                    Text("Save")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showDialog = false }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    value: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )
            action()
        }
    }
}

@Composable
private fun BackupRestoreSection(
    backupStatus: BackupStatus,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Status indicator
            when (backupStatus) {
                BackupStatus.BACKING_UP -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text("Creating backup...")
                    }
                }
                BackupStatus.RESTORING -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text("Restoring backup...")
                    }
                }
                else -> {
                    // Show backup info
                    Text(
                        text = "Last backup: Today, 14:30",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBackup,
                    modifier = Modifier.weight(1f),
                    enabled = backupStatus == BackupStatus.IDLE
                ) {
                    Icon(Icons.Default.Backup, contentDescription = "Backup")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup Now")
                }
                
                Button(
                    onClick = onRestore,
                    modifier = Modifier.weight(1f),
                    enabled = backupStatus == BackupStatus.IDLE,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Restore, contentDescription = "Restore")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore")
                }
            }
            
            Divider()
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExport,
                    modifier = Modifier.weight(1f),
                    enabled = backupStatus == BackupStatus.IDLE
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Export")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export CSV")
                }
                
                OutlinedButton(
                    onClick = onImport,
                    modifier = Modifier.weight(1f),
                    enabled = backupStatus == BackupStatus.IDLE,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Upload, contentDescription = "Import")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import CSV")
                }
            }
        }
    }
}

@Composable
private fun ClipboardSettingsSection(
    autoCopyEnabled: Boolean,
    copyFormat: String,
    onAutoCopyChanged: (Boolean) -> Unit,
    onCopyFormatChanged: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Clipboard Settings",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Auto-copy toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Auto-copy to Clipboard",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "Automatically copy parsed bets to clipboard",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = autoCopyEnabled,
                    onCheckedChange = onAutoCopyChanged
                )
            }
            
            Divider()
            
            // Copy format selection
            Text(
                text = "Copy Format",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            
            val formats = listOf("JSON", "CSV", "Plain Text", "Custom")
            var selectedFormat by remember { mutableStateOf(copyFormat) }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                formats.forEach { format ->
                    FilterChip(
                        selected = selectedFormat == format,
                        onClick = {
                            selectedFormat = format
                            onCopyFormatChanged(format)
                        },
                        label = { Text(format) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            if (selectedFormat == "Custom") {
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* Update custom format */ },
                    label = { Text("Custom Format") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., {number}={amount}*{odds}") }
                )
            }
        }
    }
}

@Composable
private fun SecuritySection(
    requirePin: Boolean,
    autoLockMinutes: Int,
    onRequirePinChanged: (Boolean) -> Unit,
    onAutoLockChanged: (Int) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Security",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // PIN protection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Require PIN",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "Require PIN to access app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(

                    checked = requirePin,
                    onCheckedChange = onRequirePinChanged
                )
            }
            
            Divider()
            
            // Auto-lock
            Text(
                text = "Auto-lock",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            
            val lockOptions = listOf(1, 5, 15, 30, 60)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lockOptions.forEach { minutes ->
                    FilterChip(
                        selected = autoLockMinutes == minutes,
                        onClick = { onAutoLockChanged(minutes) },
                        label = { Text("${minutes} min") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSupportSection() {
    val context = LocalContext.current
    
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "About & Support",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Support email
            SettingItem(
                icon = Icons.Default.Email,
                title = "Support Email",
                description = "Contact support team",
                value = "support@bettingapp.com",
                action = {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@bettingapp.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Betting App Support")
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Email")
                    }
                }
            )
            
            Divider()
            
            // Privacy Policy
            SettingItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                description = "View our privacy policy",
                value = "",
                action = {
                    Button(
                        onClick = {
                            // Open privacy policy
                        }
                    ) {
                        Text("View")
                    }
                }
            )
            
            Divider()
            
            // App version
            SettingItem(
                icon = Icons.Default.Info,
                title = "App Version",
                description = "Current version information",
                value = "1.0.0",
                action = {
                    Button(
                        onClick = {
                            // Check for updates
                        }
                    ) {
                        Text("Check Update")
                    }
                }
            )
            
            Divider()
            
            // Reset app
            Button(
                onClick = { /* Reset app data */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Reset")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Data")
            }
        }
    }
}

// Data classes
data class SettingsState(
    val dailyLimit: Double = 10000.0,
    val commissionPercentage: Double = 5.0,
    val maxPayout: Double = 5000.0,
    val autoCopyEnabled: Boolean = true,
    val copyFormat: String = "JSON",
    val requirePin: Boolean = false,
    val autoLockMinutes: Int = 5,
    val backupFrequency: BackupFrequency = BackupFrequency.DAILY,
    val theme: AppTheme = AppTheme.SYSTEM
)

enum class BackupStatus {
    IDLE, BACKING_UP, RESTORING, ERROR, SUCCESS
}

enum class BackupFrequency {
    DAILY, WEEKLY, MONTHLY, MANUAL
}

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

// ViewModel (simplified)
class SettingsViewModel {
    val settings: StateFlow<SettingsState> = TODO("Implement with Room database")
    val backupStatus: StateFlow<BackupStatus> = TODO("Implement with mutable state")
    
    fun updateSetting(key: String, value: Any) {
        // Update setting in database
    }
    
    fun createBackup() {
        // Create backup file
    }
    
    fun restoreBackup() {
        // Restore from backup
    }
    
    fun exportData() {
        // Export data to CSV
    }
    
    fun importData() {
        // Import data from CSV
    }
}
