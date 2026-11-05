package com.lifeindex.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.data.model.CreateTrackerRequest
import com.lifeindex.app.data.model.TrackerData
import com.lifeindex.app.data.repository.TrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class TrackerUiState(
    val isLoading: Boolean = false,
    val trackers: List<TrackerData> = emptyList(),
    val isCreating: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TrackerRepository(application)
    private val sessionManager = SessionManager(application)
    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState

    fun loadTrackers() {
        val token = sessionManager.getAccessToken()
        val userId = sessionManager.getUserId()
        if (token == null || userId == null) {
            _uiState.value = TrackerUiState(errorMessage = "You are not signed in")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.observeTrackers(userId).collect { trackers ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    trackers = trackers,
                    errorMessage = null
                )
            }
        }
        viewModelScope.launch {
            try {
                repository.refreshTrackers(userId, token)
            } catch (_: Exception) {
                // Keep the Room cache available when the network is unavailable.
            }
        }
    }

    fun createTracker(title: String, trackerType: String, templateId: String?) {
        val userId = sessionManager.getUserId()
        if (userId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "You are not signed in")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, errorMessage = null, message = null)
            try {
                repository.createTracker(
                    userId,
                    CreateTrackerRequest(title = title, trackerType = trackerType, templateId = templateId)
                )
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    message = "Saved on this device. Sync will run when a connection is available."
                )
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    errorMessage = error.message ?: "Unable to save tracker locally"
                )
            }
        }
    }
}