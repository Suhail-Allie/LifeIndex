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
import com.lifeindex.app.data.local.AppLanguageManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.lifeindex.app.R

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
    var languageExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val selectedLanguageCode = AppLanguageManager.currentLanguage(context)
    val selectedLanguageName = when (selectedLanguageCode) {
        "af" -> "Afrikaans"
        "xh" -> "isiXhosa"
        else -> "English"
    }

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
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.settings_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SettingsCard(
            title = stringResource(R.string.language)
        ) {
            Text(
                text = stringResource(R.string.choose_language),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ExposedDropdownMenuBox(
                expanded = languageExpanded,
                onExpandedChange = { languageExpanded = !languageExpanded }
            ) {
                OutlinedTextField(
                    value = selectedLanguageName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.app_language)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(languageExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = languageExpanded,
                    onDismissRequest = { languageExpanded = false }
                ) {
                    listOf(
                        "en" to "English",
                        "af" to "Afrikaans",
                        "xh" to "isiXhosa"
                    ).forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.second) },
                            onClick = {
                                languageExpanded = false
                                AppLanguageManager.applyLanguage(context, option.first)
                            }
                        )
                    }
                }
            }
        }

        SettingsCard(
            title = stringResource(R.string.appearance)
        ) {

            Text(
                text = stringResource(R.string.theme),
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
                    value = when (theme) {
                        "SYSTEM" -> stringResource(R.string.system)
                        "LIGHT" -> stringResource(R.string.light)
                        else -> stringResource(R.string.dark)
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text(stringResource(R.string.app_theme))
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
                                    when (option) {
                                        "SYSTEM" -> stringResource(R.string.system)
                                        "LIGHT" -> stringResource(R.string.light)
                                        else -> stringResource(R.string.dark)
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
            title = stringResource(R.string.notifications)
        ) {

            Text(
                text = stringResource(R.string.reminder_notifications),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = stringResource(R.string.allow_notifications),
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
            title = stringResource(R.string.reminders)
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
                    Text(stringResource(R.string.default_reminder))
                },
                suffix = {
                    Text(stringResource(R.string.minutes))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.reminder_one_day_before),
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
                    stringResource(R.string.saving)
                else
                    stringResource(R.string.save_settings)
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
            Text(stringResource(R.string.log_out))
        }

        Text(
            text = stringResource(R.string.footer_tagline),
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