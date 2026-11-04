package com.lifeindex.app.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.data.repository.AuthRepository
import com.lifeindex.app.notifications.FcmTokenRegistrar
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
                val response = repository.register(displayName, email, password)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    val accessToken = authResponse?.accessToken
                    val refreshToken = authResponse?.refreshToken
                    val userId = authResponse?.user?.id

                    if (accessToken != null && refreshToken != null && userId != null) {
                        sessionManager.saveSession(accessToken, refreshToken, userId)
                        registerPushToken(accessToken)
                        _uiState.value = AuthUiState(isSuccess = true)
                    } else {
                        _uiState.value = AuthUiState(
                            errorMessage = "Registration response was incomplete"
                        )
                    }
                } else {
                    _uiState.value = AuthUiState(errorMessage = "Registration failed")
                }
            } catch (error: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                val response = repository.login(email, password)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    val accessToken = authResponse?.accessToken
                    val refreshToken = authResponse?.refreshToken
                    val userId = authResponse?.user?.id

                    if (accessToken != null && refreshToken != null && userId != null) {
                        sessionManager.saveSession(accessToken, refreshToken, userId)
                        registerPushToken(accessToken)
                        _uiState.value = AuthUiState(isSuccess = true)
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

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                val response = repository.loginWithGoogle(idToken)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    val accessToken = authResponse?.accessToken
                    val refreshToken = authResponse?.refreshToken
                    val userId = authResponse?.user?.id

                    if (accessToken != null && refreshToken != null && userId != null) {
                        sessionManager.saveSession(accessToken, refreshToken, userId)
                        registerPushToken(accessToken)
                        _uiState.value = AuthUiState(isSuccess = true)
                    } else {
                        _uiState.value = AuthUiState(
                            errorMessage = "Google sign-in response was incomplete"
                        )
                    }
                } else {
                    _uiState.value = AuthUiState(
                        errorMessage = "Google sign-in failed (${response.code()}). Check the account and try again."
                    )
                }
            } catch (error: Exception) {
                Log.e("LifeIndexAuth", "Google sign-in failed", error)
                _uiState.value = AuthUiState(
                    errorMessage = "Unable to complete Google sign-in. Please try again."
                )
            }
        }
    }

    // Register the current installation after either registration or login.
    // FCM token retrieval is asynchronous and must not block authentication.
    private fun registerPushToken(accessToken: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("LifeIndexFCM", "Unable to obtain FCM token", task.exception)
                    return@addOnCompleteListener
                }

                val fcmToken = task.result ?: return@addOnCompleteListener
                viewModelScope.launch {
                    try {
                        FcmTokenRegistrar.register(
                            getApplication<Application>(),
                            accessToken,
                            fcmToken
                        )
                        Log.d("LifeIndexFCM", "Device registered for push notifications")
                    } catch (error: Exception) {
                        Log.w("LifeIndexFCM", "Device registration will need retry", error)
                    }
                }
            }
    }

    fun clearState() {
        _uiState.value = AuthUiState()
    }
}
