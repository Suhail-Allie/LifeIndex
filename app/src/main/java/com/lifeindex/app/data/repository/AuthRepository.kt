package com.lifeindex.app.data.repository

import com.lifeindex.app.data.model.AuthResponse
import com.lifeindex.app.data.model.LoginRequest
import com.lifeindex.app.data.model.RegisterRequest
import com.lifeindex.app.data.remote.ApiService
import com.lifeindex.app.data.remote.RetrofitClient
import retrofit2.Response
import com.lifeindex.app.data.model.MessageResponse
import com.lifeindex.app.data.model.RefreshTokenRequest

class AuthRepository(
    private val apiService: ApiService = RetrofitClient.api
) {

    suspend fun register(
        displayName: String,
        email: String,
        password: String
    ): Response<AuthResponse> {
        return apiService.register(
            RegisterRequest(
                displayName = displayName,
                email = email,
                password = password
            )
        )
    }

    suspend fun login(
        email: String,
        password: String
    ): Response<AuthResponse> {
        return apiService.login(
            LoginRequest(
                email = email,
                password = password
            )
        )
    }

    suspend fun logout(
        refreshToken: String
    ): Response<MessageResponse> {
        return apiService.logout(
            request = RefreshTokenRequest(refreshToken)
        )
    }
}