package com.lifeindex.app.data.repository

import com.lifeindex.app.data.model.SettingsResponse
import com.lifeindex.app.data.model.UpdateSettingsRequest
import com.lifeindex.app.data.remote.RetrofitClient
import retrofit2.Response

class UserRepository {

    private val apiService = RetrofitClient.api

    suspend fun getSettings(
        accessToken: String
    ): Response<SettingsResponse> {
        return apiService.getSettings(
            token = "Bearer $accessToken"
        )
    }

    suspend fun updateSettings(
        accessToken: String,
        request: UpdateSettingsRequest
    ): Response<SettingsResponse> {
        return apiService.updateSettings(
            token = "Bearer $accessToken",
            request = request
        )
    }
}