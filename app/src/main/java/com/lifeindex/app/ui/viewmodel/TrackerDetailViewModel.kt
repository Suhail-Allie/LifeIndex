package com.lifeindex.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.data.model.FieldValueRequest
import com.lifeindex.app.data.model.TrackerData
import com.lifeindex.app.data.model.UpdateTrackerFieldsRequest
import com.lifeindex.app.data.repository.FieldRepository
import com.lifeindex.app.data.repository.TrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

class TrackerDetailViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val fieldRepository = FieldRepository()
    private val trackerRepository = TrackerRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(TrackerDetailUiState())
    val uiState: StateFlow<TrackerDetailUiState> = _uiState

    fun loadTracker(trackerId: String) {
        val token = sessionManager.getAccessToken()

        if (token == null) {
            _uiState.value = TrackerDetailUiState(
                errorMessage = "You are not signed in"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = TrackerDetailUiState(
                isLoading = true
            )

            try {
                val trackerResponse =
                    trackerRepository.getTrackerById(token, trackerId)

                val fieldResponse =
                    fieldRepository.getTrackerFields(token, trackerId)

                if (
                    trackerResponse.isSuccessful &&
                    fieldResponse.isSuccessful
                ) {
                    _uiState.value = TrackerDetailUiState(
                        tracker = trackerResponse.body()?.tracker,
                        fields = fieldResponse.body()?.fields ?: emptyList()
                    )
                } else {
                    _uiState.value = TrackerDetailUiState(
                        errorMessage = "Unable to load tracker"
                    )
                }

            } catch (error: Exception) {
                _uiState.value = TrackerDetailUiState(
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }

    fun saveTracker(
        trackerId: String,
        title: String,
        trackerType: String,
        status: String,
        priority: String,
        notes: String
    ) {
        val token = sessionManager.getAccessToken() ?: return

        viewModelScope.launch {
            try {
                val request = com.lifeindex.app.data.model.CreateTrackerRequest(
                    title = title,
                    trackerType = trackerType,
                    status = status,
                    priority = priority,
                    notes = notes
                )

                val response = trackerRepository.updateTracker(
                    token,
                    trackerId,
                    request
                )

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        tracker = response.body()?.tracker,
                        message = "Tracker updated successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Unable to update tracker"
                    )
                }

            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }

    fun saveFields(
        trackerId: String,
        fields: List<FieldValueRequest>
    ) {
        val token = sessionManager.getAccessToken()

        if (token == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "You are not signed in"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                message = null,
                errorMessage = null
            )

            try {
                val response = fieldRepository.updateTrackerFields(
                    token,
                    trackerId,
                    UpdateTrackerFieldsRequest(fields)
                )

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        message = "Tracker fields saved successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Unable to save tracker fields"
                    )
                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }

    fun archiveTracker(trackerId: String) {
        val token = sessionManager.getAccessToken() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isArchiving = true
            )

            try {
                val response =
                    trackerRepository.archiveTracker(token, trackerId)

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isArchiving = false,
                        archived = true,
                        message = "Tracker archived successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isArchiving = false,
                        errorMessage = "Unable to archive tracker"
                    )
                }

            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isArchiving = false,
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }

    fun deleteTracker(trackerId: String) {
        val token = sessionManager.getAccessToken() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true
            )

            try {
                val response =
                    trackerRepository.deleteTracker(token, trackerId)

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deleted = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = "Unable to delete tracker"
                    )
                }

            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }
}