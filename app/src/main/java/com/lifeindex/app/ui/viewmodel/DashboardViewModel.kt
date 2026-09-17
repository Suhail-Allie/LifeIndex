package com.lifeindex.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.data.model.DashboardResponse
import com.lifeindex.app.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val dashboard: DashboardResponse? = null,
    val errorMessage: String? = null
)

class DashboardViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = DashboardRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    fun loadDashboard() {
        val token = sessionManager.getAccessToken()

        if (token == null) {
            _uiState.value = DashboardUiState(
                errorMessage = "You are not signed in"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = DashboardUiState(
                isLoading = true
            )

            try {
                val response = repository.getDashboard(token)

                if (response.isSuccessful) {
                    _uiState.value = DashboardUiState(
                        dashboard = response.body()
                    )
                } else {
                    _uiState.value = DashboardUiState(
                        errorMessage = "Unable to load dashboard"
                    )
                }
            } catch (error: Exception) {
                _uiState.value = DashboardUiState(
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }
}