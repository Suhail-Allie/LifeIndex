package com.lifeindex.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lifeindex.app.R

@Composable
fun localizedTrackerType(value: String): String = when (value.uppercase()) {
    "ITEM" -> stringResource(R.string.type_item)
    "PROCESS" -> stringResource(R.string.type_process)
    "RECURRING" -> stringResource(R.string.type_recurring)
    else -> value
}

@Composable
fun localizedTrackerTag(value: String): String = when (value.uppercase()) {
    "ACTIVE" -> stringResource(R.string.status_active)
    "COMPLETED" -> stringResource(R.string.status_completed)
    "IN_PROGRESS" -> stringResource(R.string.status_in_progress)
    "ON_HOLD" -> stringResource(R.string.status_on_hold)
    "ARCHIVED" -> stringResource(R.string.status_archived)
    "LOW" -> stringResource(R.string.priority_low)
    "NORMAL" -> stringResource(R.string.priority_normal)
    "HIGH" -> stringResource(R.string.priority_high)
    "URGENT" -> stringResource(R.string.priority_urgent)
    else -> value.lowercase().replaceFirstChar { it.uppercase() }
}
