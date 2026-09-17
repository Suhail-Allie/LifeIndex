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
import kotlinx.coroutines.launch

data class TrackerUiState(
    val isLoading: Boolean = false,
    val trackers: List<TrackerData> = emptyList(),
    val isCreating: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

class TrackerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = TrackerRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState

    fun loadTrackers() {
        val token = sessionManager.getAccessToken()

        if (token == null) {
            _uiState.value = TrackerUiState(
                errorMessage = "You are not signed in"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = TrackerUiState(isLoading = true)

            try {
                val response = repository.getTrackers(token)

                if (response.isSuccessful) {
                    _uiState.value = TrackerUiState(
                        trackers = response.body()?.trackers ?: emptyList()
                    )
                } else {
                    _uiState.value = TrackerUiState(
                        errorMessage = "Unable to load trackers"
                    )
                }
            } catch (error: Exception) {
                _uiState.value = TrackerUiState(
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }

    fun createTracker(
        title: String,
        trackerType: String,
        templateId: String?
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
                isCreating = true,
                errorMessage = null,
                message = null
            )

            try {
                val request = CreateTrackerRequest(
                    title = title,
                    trackerType = trackerType,
                    templateId = templateId
                )

                val response = repository.createTracker(
                    token,
                    request
                )

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        message = "Tracker created successfully"
                    )

                    loadTrackers()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = "Unable to create tracker"
                    )
                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }
}