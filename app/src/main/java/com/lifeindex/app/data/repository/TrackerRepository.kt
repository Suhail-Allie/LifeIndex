package com.lifeindex.app.data.repository

import com.lifeindex.app.data.model.CreateTrackerRequest
import com.lifeindex.app.data.model.MessageResponse
import com.lifeindex.app.data.model.TrackerResponse
import com.lifeindex.app.data.model.TrackersResponse
import com.lifeindex.app.data.remote.RetrofitClient
import retrofit2.Response

class TrackerRepository {

    private val apiService = RetrofitClient.api

    suspend fun getTrackers(
        accessToken: String
    ): Response<TrackersResponse> {
        return apiService.getTrackers(
            token = "Bearer $accessToken"
        )
    }

    suspend fun createTracker(
        accessToken: String,
        request: CreateTrackerRequest
    ): Response<TrackerResponse> {
        return apiService.createTracker(
            token = "Bearer $accessToken",
            request = request
        )
    }

    suspend fun getTrackerById(
        accessToken: String,
        trackerId: String
    ): Response<TrackerResponse> {
        return apiService.getTrackerById(
            token = "Bearer $accessToken",
            trackerId = trackerId
        )
    }

    suspend fun updateTracker(
        accessToken: String,
        trackerId: String,
        request: CreateTrackerRequest
    ): Response<TrackerResponse> {
        return apiService.updateTracker(
            token = "Bearer $accessToken",
            trackerId = trackerId,
            request = request
        )
    }

    suspend fun deleteTracker(
        accessToken: String,
        trackerId: String
    ): Response<MessageResponse> {
        return apiService.deleteTracker(
            token = "Bearer $accessToken",
            trackerId = trackerId
        )
    }

    suspend fun archiveTracker(
        accessToken: String,
        trackerId: String
    ): Response<TrackerResponse> {
        return apiService.archiveTracker(
            token = "Bearer $accessToken",
            trackerId = trackerId
        )
    }
}