package com.lifeindex.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifeindex.app.R
import com.lifeindex.app.data.model.AttachmentData
import com.lifeindex.app.ui.viewmodel.DocumentsViewModel
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    documentsViewModel: DocumentsViewModel = viewModel()
) {
    val uiState by documentsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var trackerMenuExpanded by remember { mutableStateOf(false) }
    var selectedTrackerId by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<AttachmentData?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && selectedTrackerId.isNotBlank()) {
            documentsViewModel.uploadAttachment(context, selectedTrackerId, uri)
        }
    }

    LaunchedEffect(Unit) {
        documentsViewModel.load()
    }

    LaunchedEffect(uiState.trackers) {
        if (selectedTrackerId.isBlank()) {
            selectedTrackerId = uiState.trackers.firstOrNull()?.id.orEmpty()
        }
    }

    val selectedTracker = uiState.trackers.firstOrNull { it.id == selectedTrackerId }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = stringResource(R.string.documents_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.documents_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.attachments_upload_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = trackerMenuExpanded,
                    onExpandedChange = { trackerMenuExpanded = !trackerMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTracker?.title ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.attachments_choose_tracker)) },
                        placeholder = { Text(stringResource(R.string.attachments_choose_tracker)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(trackerMenuExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = uiState.trackers.isNotEmpty()
                    )
                    ExposedDropdownMenu(
                        expanded = trackerMenuExpanded,
                        onDismissRequest = { trackerMenuExpanded = false }
                    ) {
                        uiState.trackers.forEach { tracker ->
                            DropdownMenuItem(
                                text = { Text(tracker.title) },
                                onClick = {
                                    selectedTrackerId = tracker.id
                                    trackerMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                if (uiState.trackers.isEmpty() && !uiState.isLoading) {
                    Text(
                        text = stringResource(R.string.attachments_no_trackers),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        filePicker.launch(arrayOf("application/pdf", "image/jpeg", "image/png"))
                    },
                    enabled = selectedTracker != null && !uiState.isUploading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (uiState.isUploading) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.attachments_uploading),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else {
                        Text(stringResource(R.string.attachments_choose_file))
                    }
                }
            }
        }

        uiState.messageCode?.let { code ->
            val message = when (code) {
                "UPLOADED" -> stringResource(R.string.attachments_uploaded)
                "DELETED" -> stringResource(R.string.attachments_deleted)
                else -> ""
            }
            if (message.isNotBlank()) {
                Text(message, color = MaterialTheme.colorScheme.primary)
            }
        }

        uiState.errorCode?.let { code ->
            val message = when (code) {
                "AUTH" -> stringResource(R.string.attachments_generic_error)
                "FILE_TOO_LARGE" -> stringResource(R.string.attachments_file_too_large)
                "UNSUPPORTED_TYPE" -> stringResource(R.string.attachments_invalid_type)
                "TRACKER_REQUIRED" -> stringResource(R.string.attachments_choose_tracker)
                "LOAD" -> stringResource(R.string.attachments_load_error)
                else -> stringResource(R.string.attachments_generic_error)
            }
            Text(message, color = MaterialTheme.colorScheme.error)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.attachments_list_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { documentsViewModel.load() }) {
                Text(stringResource(R.string.attachments_refresh))
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.attachments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.attachments_empty),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.attachments_empty_help),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            uiState.attachments.forEach { attachment ->
                AttachmentCard(
                    attachment = attachment,
                    onDownload = {
                        documentsViewModel.downloadAttachment(attachment.id) { downloadUrl ->
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                )
                            } catch (_: Exception) {
                                // The signed link remains available for a retry from this screen.
                            }
                        }
                    },
                    onDelete = { pendingDelete = attachment }
                )
            }
        }
    }

    pendingDelete?.let { attachment ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.attachments_delete_title)) },
            text = { Text(stringResource(R.string.attachments_delete_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        documentsViewModel.deleteAttachment(attachment.id)
                        pendingDelete = null
                    }
                ) {
                    Text(stringResource(R.string.attachments_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.attachments_cancel))
                }
            }
        )
    }
}

@Composable
private fun AttachmentCard(
    attachment: AttachmentData,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = attachment.file_name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.attachments_linked_to, attachment.tracker_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${formatAttachmentSize(attachment.file_size)} â€¢ ${attachment.mime_type}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDownload) {
                    Text(stringResource(R.string.attachments_download))
                }
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.attachments_delete))
                }
            }
        }
    }
}

private fun formatAttachmentSize(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> String.format(Locale.getDefault(), "%.0f KB", bytes / 1024.0)
    else -> "$bytes B"
}