package com.lifeindex.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.data.model.TrackerData
import com.lifeindex.app.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val isLoading: Boolean = false,
    val trackers: List<TrackerData> = emptyList(),
    val errorMessage: String? = null
)

class SearchViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = SearchRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    fun search(
        query: String? = null,
        trackerType: String? = null,
        status: String? = null,
        categoryId: String? = null
    ) {
        val token = sessionManager.getAccessToken()

        if (token == null) {
            _uiState.value = SearchUiState(
                errorMessage = "You are not signed in"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = SearchUiState(
                isLoading = true
            )

            try {
                val response = repository.searchTrackers(
                    accessToken = token,
                    query = query,
                    trackerType = trackerType,
                    status = status,
                    categoryId = categoryId
                )

                if (response.isSuccessful) {
                    _uiState.value = SearchUiState(
                        trackers = response.body()?.trackers ?: emptyList()
                    )
                } else {
                    _uiState.value = SearchUiState(
                        errorMessage = "Unable to search trackers"
                    )
                }
            } catch (error: Exception) {
                _uiState.value = SearchUiState(
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }
}