package com.lifeindex.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun register(
        displayName: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                val response = repository.register(
                    displayName,
                    email,
                    password
                )

                if (response.isSuccessful) {
                    val authResponse = response.body()

                    val accessToken = authResponse?.accessToken
                    val refreshToken = authResponse?.refreshToken
                    val userId = authResponse?.user?.id

                    if (
                        accessToken != null &&
                        refreshToken != null &&
                        userId != null
                    ) {
                        sessionManager.saveSession(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            userId = userId
                        )

                        _uiState.value = AuthUiState(
                            isSuccess = true
                        )
                    } else {
                        _uiState.value = AuthUiState(
                            errorMessage = "Registration response was incomplete"
                        )
                    }
                } else {
                    _uiState.value = AuthUiState(
                        errorMessage = "Registration failed"
                    )
                }
            } catch (error: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }

    fun login(
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                val response = repository.login(
                    email,
                    password
                )

                if (response.isSuccessful) {
                    val authResponse = response.body()

                    val accessToken = authResponse?.accessToken
                    val refreshToken = authResponse?.refreshToken
                    val userId = authResponse?.user?.id

                    if (
                        accessToken != null &&
                        refreshToken != null &&
                        userId != null
                    ) {
                        sessionManager.saveSession(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            userId = userId
                        )

                        _uiState.value = AuthUiState(
                            isSuccess = true
                        )
                    } else {
                        _uiState.value = AuthUiState(
                            errorMessage = "Login response was incomplete"
                        )
                    }
                } else {
                    _uiState.value = AuthUiState(
                        errorMessage = "Invalid email or password"
                    )
                }
            } catch (error: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState()
    }
}