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
import com.lifeindex.app.ui.localizedTrackerType
import com.lifeindex.app.ui.localizedTrackerTag
import androidx.compose.ui.res.stringResource
import com.lifeindex.app.R

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
                    text = stringResource(R.string.trackers_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.trackers_subtitle),
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
                        text = stringResource(R.string.create_tracker),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                        },
                        label = {
                            Text(stringResource(R.string.name))
                        },
                        placeholder = {
                            Text(stringResource(R.string.example_passport))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = stringResource(R.string.type),
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
                                        when (type) {
                                            "ITEM" -> stringResource(R.string.type_item)
                                            "PROCESS" -> stringResource(R.string.type_process)
                                            else -> stringResource(R.string.type_recurring)
                                        }
                                    )
                                }
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.use_template),
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
                                stringResource(R.string.creating)
                            else
                                stringResource(R.string.create_tracker_button)
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
                    text = stringResource(R.string.your_trackers),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    label = {
                        Text(stringResource(R.string.search))
                    },
                    placeholder = {
                        Text(stringResource(R.string.search_by_tracker))
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
                            Text(stringResource(R.string.all))
                        }
                    )

                    FilterChip(
                        selected = selectedFilter == "ITEM",
                        onClick = {
                            selectedFilter = "ITEM"
                        },
                        label = {
                            Text(stringResource(R.string.items))
                        }
                    )

                    FilterChip(
                        selected = selectedFilter == "PROCESS",
                        onClick = {
                            selectedFilter = "PROCESS"
                        },
                        label = {
                            Text(stringResource(R.string.processes))
                        }
                    )

                    FilterChip(
                        selected = selectedFilter == "RECURRING",
                        onClick = {
                            selectedFilter = "RECURRING"
                        },
                        label = {
                            Text(stringResource(R.string.recurring))
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
                            text = stringResource(R.string.no_trackers_found),
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = if (
                                searchText.isNotBlank() ||
                                selectedFilter != "ALL"
                            ) {
                                stringResource(R.string.change_search_filter)
                            } else {
                                stringResource(R.string.create_first_tracker)
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
                            text = when (tracker.sync_state) {
                                "PENDING" -> stringResource(R.string.waiting_to_sync)
                                "CONFLICT" -> stringResource(R.string.sync_conflict)
                                else -> stringResource(R.string.synced)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (tracker.sync_state == "CONFLICT") MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text =
                                "${localizedTrackerType(tracker.tracker_type)}  •  ${localizedTrackerTag(tracker.status ?: "ACTIVE")}",
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
                            Text(stringResource(R.string.open_tracker))
                        }
                    }
                }
            }
        }
    }
}