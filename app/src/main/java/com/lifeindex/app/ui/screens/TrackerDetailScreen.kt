package com.lifeindex.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifeindex.app.data.model.FieldValueRequest
import com.lifeindex.app.ui.viewmodel.TrackerDetailViewModel

@Composable
fun TrackerDetailScreen(
    trackerId: String,
    onBack: () -> Unit,
    trackerDetailViewModel: TrackerDetailViewModel = viewModel()
) {
    val uiState by trackerDetailViewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var trackerType by remember { mutableStateOf("ITEM") }
    var status by remember { mutableStateOf("ACTIVE") }
    var priority by remember { mutableStateOf("NORMAL") }
    var notes by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val textValues = remember {
        mutableStateMapOf<String, String>()
    }

    val booleanValues = remember {
        mutableStateMapOf<String, Boolean>()
    }

    LaunchedEffect(trackerId) {
        trackerDetailViewModel.loadTracker(trackerId)
    }

    LaunchedEffect(uiState.tracker) {
        uiState.tracker?.let { tracker ->
            title = tracker.title
            trackerType = tracker.tracker_type
            status = tracker.status
            priority = tracker.priority
            notes = tracker.notes ?: ""
        }
    }

    LaunchedEffect(uiState.fields) {
        uiState.fields.forEach { field ->

            when (field.field_type) {
                "TEXT" -> {
                    textValues[field.id] = field.text_value ?: ""
                }

                "DATE" -> {
                    textValues[field.id] =
                        field.date_value ?: ""
                }

                "BOOLEAN" -> {
                    booleanValues[field.id] =
                        field.boolean_value ?: false
                }
            }
        }
    }

    LaunchedEffect(uiState.deleted, uiState.archived) {
        if (uiState.deleted || uiState.archived) {
            onBack()
        }
    }

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            CircularProgressIndicator()
        }

        return
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Tracker Details")

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Title")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = trackerType,
            onValueChange = { trackerType = it },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Type")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = status,
            onValueChange = { status = it },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Status")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = priority,
            onValueChange = { priority = it },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Priority")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Notes")
            }
        )

        Button(
            onClick = {
                trackerDetailViewModel.saveTracker(
                    trackerId = trackerId,
                    title = title,
                    trackerType = trackerType,
                    status = status,
                    priority = priority,
                    notes = notes
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Tracker")
        }

        Text("Custom Fields")

        uiState.fields.forEach { field ->

            Text(
                text = buildString {
                    append(field.field_name)

                    if (field.is_required) {
                        append(" *")
                    }
                }
            )

            when (field.field_type) {

                "TEXT",
                "DATE" -> {
                    OutlinedTextField(
                        value = textValues[field.id] ?: "",
                        onValueChange = {
                            textValues[field.id] = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                "BOOLEAN" -> {
                    Switch(
                        checked = booleanValues[field.id] ?: false,
                        onCheckedChange = {
                            booleanValues[field.id] = it
                        }
                    )
                }

                else -> {
                    Text(
                        "Field type: ${field.field_type}"
                    )
                }
            }
        }

        Button(
            onClick = {
                val values = uiState.fields.map { field ->

                    when (field.field_type) {

                        "TEXT" -> FieldValueRequest(
                            fieldDefinitionId = field.id,
                            textValue = textValues[field.id]
                        )

                        "DATE" -> FieldValueRequest(
                            fieldDefinitionId = field.id,
                            dateValue = textValues[field.id]
                        )

                        "BOOLEAN" -> FieldValueRequest(
                            fieldDefinitionId = field.id,
                            booleanValue = booleanValues[field.id]
                        )

                        else -> FieldValueRequest(
                            fieldDefinitionId = field.id
                        )
                    }
                }

                trackerDetailViewModel.saveFields(
                    trackerId,
                    values
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator()
            } else {
                Text("Save Fields")
            }
        }

        Button(
            onClick = {
                trackerDetailViewModel.archiveTracker(
                    trackerId
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isArchiving
        ) {
            if (uiState.isArchiving) {
                CircularProgressIndicator()
            } else {
                Text("Archive Tracker")
            }
        }

        Button(
            onClick = {
                showDeleteDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isDeleting
        ) {
            Text("Delete Tracker")
        }

        uiState.message?.let {
            Text(it)
        }

        uiState.errorMessage?.let {
            Text(it)
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDeleting) {
                    showDeleteDialog = false
                }
            },
            title = {
                Text("Delete Tracker?")
            },
            text = {
                Text(
                    "This will permanently remove this tracker and its data."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        trackerDetailViewModel.deleteTracker(
                            trackerId
                        )
                    },
                    enabled = !uiState.isDeleting
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}