package com.lifeindex.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.data.model.SettingsData
import com.lifeindex.app.data.model.UpdateSettingsRequest
import com.lifeindex.app.data.repository.UserRepository
import com.lifeindex.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isLoggingOut: Boolean = false,
    val settings: SettingsData? = null,
    val message: String? = null,
    val errorMessage: String? = null,
    val logoutSuccess: Boolean = false
)

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun loadSettings() {
        val token = sessionManager.getAccessToken()

        if (token == null) {
            _uiState.value = SettingsUiState(
                errorMessage = "You are not signed in"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = SettingsUiState(isLoading = true)

            try {
                val response = userRepository.getSettings(token)

                if (response.isSuccessful) {
                    _uiState.value = SettingsUiState(
                        settings = response.body()?.settings
                    )
                } else {
                    _uiState.value = SettingsUiState(
                        errorMessage = "Unable to load settings"
                    )
                }
            } catch (error: Exception) {
                _uiState.value = SettingsUiState(
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }

    fun updateSettings(
        theme: String,
        notificationsEnabled: Boolean,
        defaultReminderMinutes: Int
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
                errorMessage = null,
                message = null
            )

            try {
                val request = UpdateSettingsRequest(
                    theme = theme,
                    notificationsEnabled = notificationsEnabled,
                    defaultReminderMinutes = defaultReminderMinutes
                )

                val response = userRepository.updateSettings(
                    token,
                    request
                )

                if (response.isSuccessful) {
                    _uiState.value = SettingsUiState(
                        settings = response.body()?.settings,
                        message = "Settings saved successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Unable to save settings"
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

    fun logout() {
        val refreshToken = sessionManager.getRefreshToken()

        if (refreshToken == null) {
            sessionManager.clearSession()

            _uiState.value = _uiState.value.copy(
                logoutSuccess = true
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoggingOut = true,
                errorMessage = null
            )

            try {
                val response = authRepository.logout(refreshToken)

                sessionManager.clearSession()

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoggingOut = false,
                        logoutSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoggingOut = false,
                        logoutSuccess = true
                    )
                }

            } catch (error: Exception) {
                sessionManager.clearSession()

                _uiState.value = _uiState.value.copy(
                    isLoggingOut = false,
                    logoutSuccess = true
                )
            }
        }
    }
}