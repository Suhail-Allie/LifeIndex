package com.lifeindex.app.data.repository

import com.lifeindex.app.data.model.SearchResponse
import com.lifeindex.app.data.remote.RetrofitClient
import retrofit2.Response

class SearchRepository {

    private val apiService = RetrofitClient.api

    suspend fun searchTrackers(
        accessToken: String,
        query: String? = null,
        trackerType: String? = null,
        status: String? = null,
        categoryId: String? = null
    ): Response<SearchResponse> {
        return apiService.searchTrackers(
            token = "Bearer $accessToken",
            query = query,
            trackerType = trackerType,
            status = status,
            categoryId = categoryId
        )
    }
}