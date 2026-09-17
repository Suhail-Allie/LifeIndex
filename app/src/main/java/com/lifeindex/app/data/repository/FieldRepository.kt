package com.lifeindex.app.data.repository

import com.lifeindex.app.data.model.TrackerFieldsResponse
import com.lifeindex.app.data.model.UpdateTrackerFieldsRequest
import com.lifeindex.app.data.model.UpdateTrackerFieldsResponse
import com.lifeindex.app.data.remote.RetrofitClient
import retrofit2.Response

class FieldRepository {

    private val apiService = RetrofitClient.api

    suspend fun getTrackerFields(
        accessToken: String,
        trackerId: String
    ): Response<TrackerFieldsResponse> {
        return apiService.getTrackerFields(
            token = "Bearer $accessToken",
            trackerId = trackerId
        )
    }

    suspend fun updateTrackerFields(
        accessToken: String,
        trackerId: String,
        request: UpdateTrackerFieldsRequest
    ): Response<UpdateTrackerFieldsResponse> {
        return apiService.updateTrackerFields(
            token = "Bearer $accessToken",
            trackerId = trackerId,
            request = request
        )
    }
}