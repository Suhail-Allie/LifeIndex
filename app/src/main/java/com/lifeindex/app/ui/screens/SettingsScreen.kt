package com.lifeindex.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifeindex.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsState()

    var theme by remember { mutableStateOf("SYSTEM") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var defaultReminder by remember { mutableStateOf("1440") }

    var themeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        settingsViewModel.loadSettings()
    }

    LaunchedEffect(uiState.settings) {
        uiState.settings?.let { settings ->
            theme = settings.theme
            notificationsEnabled = settings.notifications_enabled
            defaultReminder =
                settings.default_reminder_minutes.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 20.dp,
                vertical = 22.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Control how LifeIndex works for you.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SettingsCard(
            title = "Appearance"
        ) {

            Text(
                text = "Theme",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            ExposedDropdownMenuBox(
                expanded = themeExpanded,
                onExpandedChange = {
                    themeExpanded = !themeExpanded
                }
            ) {

                OutlinedTextField(
                    value = theme
                        .lowercase()
                        .replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text("App theme")
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults
                            .TrailingIcon(themeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = themeExpanded,
                    onDismissRequest = {
                        themeExpanded = false
                    }
                ) {
                    listOf(
                        "SYSTEM",
                        "LIGHT",
                        "DARK"
                    ).forEach { option ->

                        DropdownMenuItem(
                            text = {
                                Text(
                                    option.lowercase()
                                        .replaceFirstChar {
                                            it.uppercase()
                                        }
                                )
                            },
                            onClick = {
                                theme = option
                                themeExpanded = false
                            }
                        )
                    }
                }
            }
        }

        SettingsCard(
            title = "Notifications"
        ) {

            Text(
                text = "Reminder notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Allow LifeIndex to use reminder notifications.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Switch(
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                }
            )
        }

        SettingsCard(
            title = "Reminders"
        ) {

            OutlinedTextField(
                value = defaultReminder,
                onValueChange = {
                    defaultReminder =
                        it.filter { character ->
                            character.isDigit()
                        }
                },
                label = {
                    Text("Default reminder")
                },
                suffix = {
                    Text("minutes")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "1440 minutes is one day before.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                settingsViewModel.updateSettings(
                    theme = theme,
                    notificationsEnabled = notificationsEnabled,
                    defaultReminderMinutes =
                        defaultReminder.toIntOrNull() ?: 1440
                )
            },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text(
                if (uiState.isSaving)
                    "Saving..."
                else
                    "Save Settings"
            )
        }

        Button(
            onClick = {
                settingsViewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text("Log Out")
        }

        Text(
            text = "LifeIndex • Track anything. Find everything.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            content()
        }
    }
}