package com.lifeindex.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.data.model.CreateTrackerRequest
import com.lifeindex.app.data.model.FieldValueRequest
import com.lifeindex.app.data.model.TrackerData
import com.lifeindex.app.data.model.UpdateTrackerFieldsRequest
import com.lifeindex.app.data.repository.FieldRepository
import com.lifeindex.app.data.repository.TrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class TrackerDetailUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isArchiving: Boolean = false,
    val tracker: TrackerData? = null,
    val fields: List<com.lifeindex.app.data.model.TrackerFieldData> = emptyList(),
    val message: String? = null,
    val errorMessage: String? = null,
    val deleted: Boolean = false,
    val archived: Boolean = false
)

class TrackerDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val fieldRepository = FieldRepository()
    private val trackerRepository = TrackerRepository(application)
    private val sessionManager = SessionManager(application)
    private val _uiState = MutableStateFlow(TrackerDetailUiState())
    val uiState: StateFlow<TrackerDetailUiState> = _uiState

    fun loadTracker(trackerId: String) {
        val token = sessionManager.getAccessToken()
        val userId = sessionManager.getUserId()
        if (token == null || userId == null) {
            _uiState.value = TrackerDetailUiState(errorMessage = "You are not signed in")
            return
        }

        viewModelScope.launch {
            trackerRepository.observeTracker(userId, trackerId).collect { tracker ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    tracker = tracker,
                    errorMessage = if (tracker == null) "This tracker is not available in the offline cache yet." else null
                )
            }
        }
        viewModelScope.launch {
            try {
                trackerRepository.refreshTracker(userId, token, trackerId)
                val fieldsResponse = fieldRepository.getTrackerFields(token, trackerId)
                if (fieldsResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        fields = fieldsResponse.body()?.fields ?: emptyList()
                    )
                }
            } catch (_: Exception) {
                // The tracker itself remains available from Room while offline.
            }
        }
    }

    fun saveTracker(trackerId: String, title: String, trackerType: String, status: String, priority: String, notes: String) {
        val userId = sessionManager.getUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val updated = trackerRepository.updateTracker(
                    userId,
                    trackerId,
                    CreateTrackerRequest(
                        title = title,
                        trackerType = trackerType,
                        status = status,
                        priority = priority,
                        notes = notes
                    )
                )
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    tracker = updated,
                    message = "Saved on this device. Waiting to sync."
                )
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "Unable to save tracker"
                )
            }
        }
    }

    fun saveFields(trackerId: String, fields: List<FieldValueRequest>) {
        val token = sessionManager.getAccessToken() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, message = null, errorMessage = null)
            try {
                val response = fieldRepository.updateTrackerFields(token, trackerId, UpdateTrackerFieldsRequest(fields))
                _uiState.value = if (response.isSuccessful) {
                    _uiState.value.copy(isSaving = false, message = "Tracker fields saved successfully")
                } else {
                    _uiState.value.copy(isSaving = false, errorMessage = "Unable to save tracker fields while offline")
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Custom field changes require a connection; tracker details remain saved locally."
                )
            }
        }
    }

    fun archiveTracker(trackerId: String) {
        val userId = sessionManager.getUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isArchiving = true)
            val archived = trackerRepository.archiveTracker(userId, trackerId)
            _uiState.value = _uiState.value.copy(
                isArchiving = false,
                archived = archived,
                message = if (archived) "Archived locally. Waiting to sync." else null,
                errorMessage = if (archived) null else "Unable to archive tracker"
            )
        }
    }

    fun deleteTracker(trackerId: String) {
        val userId = sessionManager.getUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            val deleted = trackerRepository.deleteTracker(userId, trackerId)
            _uiState.value = _uiState.value.copy(
                isDeleting = false,
                deleted = deleted,
                errorMessage = if (deleted) null else "Unable to delete tracker"
            )
        }
    }

    fun useServerVersion(trackerId: String) {
        val userId = sessionManager.getUserId() ?: return
        viewModelScope.launch {
            if (trackerRepository.useServerVersion(userId, trackerId)) {
                _uiState.value = _uiState.value.copy(message = "Server version restored on this device.")
            }
        }
    }

    fun keepLocalVersion(trackerId: String) {
        val userId = sessionManager.getUserId() ?: return
        viewModelScope.launch {
            if (trackerRepository.keepLocalVersion(userId, trackerId)) {
                _uiState.value = _uiState.value.copy(message = "Your local copy has been queued to sync over the server version.")
            }
        }
    }
}