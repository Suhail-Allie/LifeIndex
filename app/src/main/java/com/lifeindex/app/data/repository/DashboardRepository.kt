package com.lifeindex.app.data.repository

import com.lifeindex.app.data.model.DashboardResponse
import com.lifeindex.app.data.remote.RetrofitClient
import retrofit2.Response

class DashboardRepository {

    private val apiService = RetrofitClient.api

    suspend fun getDashboard(
        accessToken: String
    ): Response<DashboardResponse> {
        return apiService.getDashboard(
            token = "Bearer $accessToken"
        )
    }
}