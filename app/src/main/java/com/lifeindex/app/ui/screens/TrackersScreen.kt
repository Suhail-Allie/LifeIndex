package com.lifeindex.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifeindex.app.data.model.TemplateData
import com.lifeindex.app.ui.viewmodel.TemplateViewModel
import com.lifeindex.app.ui.viewmodel.TrackerViewModel

@Composable
fun TrackersScreen(
    onTrackerClick: (String) -> Unit,
    trackerViewModel: TrackerViewModel = viewModel(),
    templateViewModel: TemplateViewModel = viewModel()
) {
    val trackerState by trackerViewModel.uiState.collectAsState()
    val templateState by templateViewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ITEM") }
    var selectedTemplate by remember { mutableStateOf<TemplateData?>(null) }

    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    LaunchedEffect(Unit) {
        trackerViewModel.loadTrackers()
        templateViewModel.loadTemplates()
    }

    val filteredTrackers = trackerState.trackers.filter { tracker ->

        val matchesSearch =
            searchText.isBlank() ||
                    tracker.title.contains(
                        searchText,
                        ignoreCase = true
                    )

        val matchesType =
            selectedFilter == "ALL" ||
                    tracker.tracker_type.equals(
                        selectedFilter,
                        ignoreCase = true
                    )

        matchesSearch && matchesType
    }

    LazyColumn(
        modifier = Modifier.padding(
            horizontal = 20.dp,
            vertical = 22.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Trackers",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Keep important parts of your life in one place.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {

                    Text(
                        text = "Create a tracker",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                        },
                        label = {
                            Text("Name")
                        },
                        placeholder = {
                            Text("e.g. Passport")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Type",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        listOf(
                            "ITEM",
                            "PROCESS",
                            "RECURRING"
                        ).forEach { type ->

                            FilterChip(
                                selected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                    selectedTemplate = null
                                },
                                label = {
                                    Text(
                                        type.lowercase()
                                            .replaceFirstChar {
                                                it.uppercase()
                                            }
                                    )
                                }
                            )
                        }
                    }

                    Text(
                        text = "Use a template",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        templateState.templates.forEach { template ->

                            FilterChip(
                                selected =
                                    selectedTemplate?.id == template.id,
                                onClick = {
                                    selectedTemplate = template
                                    selectedType = template.tracker_type
                                },
                                label = {
                                    Text(template.name)
                                }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            trackerViewModel.createTracker(
                                title.trim(),
                                selectedType,
                                selectedTemplate?.id
                            )

                            title = ""
                            selectedTemplate = null
                        },
                        enabled = title.isNotBlank() &&
                                !trackerState.isCreating,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text(
                            if (trackerState.isCreating)
                                "Creating..."
                            else
                                "Create Tracker"
                        )
                    }
                }
            }
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Your trackers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    label = {
                        Text("Search")
                    },
                    placeholder = {
                        Text("Search by tracker name")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {

                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = {
                            selectedFilter = "ALL"
                        },
                        label = {
                            Text("All")
                        }
                    )

                    FilterChip(
                        selected = selectedFilter == "ITEM",
                        onClick = {
                            selectedFilter = "ITEM"
                        },
                        label = {
                            Text("Items")
                        }
                    )

                    FilterChip(
                        selected = selectedFilter == "PROCESS",
                        onClick = {
                            selectedFilter = "PROCESS"
                        },
                        label = {
                            Text("Processes")
                        }
                    )

                    FilterChip(
                        selected = selectedFilter == "RECURRING",
                        onClick = {
                            selectedFilter = "RECURRING"
                        },
                        label = {
                            Text("Recurring")
                        }
                    )
                }
            }
        }

        if (trackerState.isLoading) {
            item {
                CircularProgressIndicator()
            }
        }

        trackerState.errorMessage?.let { error ->
            item {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (
            filteredTrackers.isEmpty() &&
            !trackerState.isLoading
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "No trackers found",
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = if (
                                searchText.isNotBlank() ||
                                selectedFilter != "ALL"
                            ) {
                                "Try changing your search or filter."
                            } else {
                                "Create your first tracker above."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {

            items(
                items = filteredTrackers,
                key = { it.id }
            ) { tracker ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text = tracker.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text =
                                "${tracker.tracker_type.lowercase()
                                    .replaceFirstChar {
                                        it.uppercase()
                                    }}  •  ${tracker.status ?: "ACTIVE"}",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )

                        TextButton(
                            onClick = {
                                onTrackerClick(tracker.id)
                            }
                        ) {
                            Text("Open tracker")
                        }
                    }
                }
            }
        }
    }
}