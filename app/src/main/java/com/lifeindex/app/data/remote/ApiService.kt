package com.lifeindex.app.data.remote

import com.lifeindex.app.data.model.AuthResponse
import com.lifeindex.app.data.model.LoginRequest
import com.lifeindex.app.data.model.RegisterRequest
import com.lifeindex.app.data.model.RefreshTokenRequest
import com.lifeindex.app.data.model.MessageResponse
import com.lifeindex.app.data.model.CreateTrackerRequest
import com.lifeindex.app.data.model.TrackerResponse
import com.lifeindex.app.data.model.TrackersResponse
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.lifeindex.app.data.model.SettingsResponse
import com.lifeindex.app.data.model.UpdateSettingsRequest
import retrofit2.http.PATCH
import retrofit2.http.Header
import com.lifeindex.app.data.model.TemplateFieldsResponse
import com.lifeindex.app.data.model.TemplatesResponse
import com.lifeindex.app.data.model.TrackerFieldsResponse
import com.lifeindex.app.data.model.UpdateTrackerFieldsRequest
import com.lifeindex.app.data.model.UpdateTrackerFieldsResponse
import com.lifeindex.app.data.model.DashboardResponse
import com.lifeindex.app.data.model.SearchResponse
import retrofit2.http.Query
import retrofit2.Call



data class HealthResponse(
    val status: String,
    val service: String
)

interface ApiService {

    @GET("health")
    suspend fun getHealth(): Response<HealthResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @GET("users/me/settings")
    suspend fun getSettings(
        @Header("Authorization") token: String
    ): Response<SettingsResponse>

    @PATCH("users/me/settings")
    suspend fun updateSettings(
        @Header("Authorization") token: String,
        @Body request: UpdateSettingsRequest
    ): Response<SettingsResponse>

    @POST("auth/logout")
    suspend fun logout(
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: RefreshTokenRequest
    ): Response<MessageResponse>

    @GET("trackers")
    suspend fun getTrackers(
        @Header("Authorization") token: String
    ): Response<TrackersResponse>

    @POST("trackers")
    suspend fun createTracker(
        @Header("Authorization") token: String,
        @Body request: CreateTrackerRequest
    ): Response<TrackerResponse>

    @DELETE("trackers/{id}")
    suspend fun deleteTracker(
        @Header("Authorization") token: String,
        @Path("id") trackerId: String
    ): Response<MessageResponse>

    @GET("templates")
    suspend fun getTemplates(): Response<TemplatesResponse>

    @GET("templates/{id}/fields")
    suspend fun getTemplateFields(
        @Path("id") templateId: String
    ): Response<TemplateFieldsResponse>

    @GET("trackers/{id}/fields")
    suspend fun getTrackerFields(
        @Header("Authorization") token: String,
        @Path("id") trackerId: String
    ): Response<TrackerFieldsResponse>

    @PATCH("trackers/{id}/fields")
    suspend fun updateTrackerFields(
        @Header("Authorization") token: String,
        @Path("id") trackerId: String,
        @Body request: UpdateTrackerFieldsRequest
    ): Response<UpdateTrackerFieldsResponse>

    @GET("dashboard")
    suspend fun getDashboard(
        @Header("Authorization") token: String
    ): Response<DashboardResponse>

    @GET("search/trackers")
    suspend fun searchTrackers(
        @Header("Authorization") token: String,
        @Query("query") query: String? = null,
        @Query("trackerType") trackerType: String? = null,
        @Query("status") status: String? = null,
        @Query("categoryId") categoryId: String? = null
    ): Response<SearchResponse>

    @GET("trackers/{id}")
    suspend fun getTrackerById(
        @Header("Authorization") token: String,
        @Path("id") trackerId: String
    ): Response<TrackerResponse>

    @PATCH("trackers/{id}")
    suspend fun updateTracker(
        @Header("Authorization") token: String,
        @Path("id") trackerId: String,
        @Body request: CreateTrackerRequest
    ): Response<TrackerResponse>

    @PATCH("trackers/{id}/archive")
    suspend fun archiveTracker(
        @Header("Authorization") token: String,
        @Path("id") trackerId: String
    ): Response<TrackerResponse>

    @POST("auth/refresh")
    fun refresh(
        @Body request: RefreshTokenRequest
    ): Call<AuthResponse>
}