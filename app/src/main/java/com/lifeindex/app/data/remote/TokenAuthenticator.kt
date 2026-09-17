package com.lifeindex.app.data.remote

import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.data.model.AuthResponse
import com.lifeindex.app.data.model.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val refreshApi: ApiService
) : Authenticator {

    private val lock = Any()

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {

        // Prevent endless authentication retry loops.
        if (responseCount(response) >= 3) {
            return null
        }

        val request = response.request()
        val path = request.url().encodedPath()

        // Do not attempt refresh for authentication endpoints.
        if (
            path.contains("/auth/login") ||
            path.contains("/auth/register") ||
            path.contains("/auth/refresh") ||
            path.contains("/auth/logout")
        ) {
            return null
        }

        synchronized(lock) {

            val currentAccessToken =
                sessionManager.getAccessToken()

            val failedAccessToken =
                request
                    .header("Authorization")
                    ?.removePrefix("Bearer ")
                    ?.trim()

            /*
             * Another request may already have refreshed
             * the token while this request was waiting.
             */
            if (
                currentAccessToken != null &&
                currentAccessToken != failedAccessToken
            ) {
                return request
                    .newBuilder()
                    .header(
                        "Authorization",
                        "Bearer $currentAccessToken"
                    )
                    .build()
            }

            val refreshToken =
                sessionManager.getRefreshToken()
                    ?: return null

            return try {

                val refreshResponse =
                    refreshApi
                        .refresh(
                            RefreshTokenRequest(refreshToken)
                        )
                        .execute()

                if (!refreshResponse.isSuccessful) {
                    sessionManager.clearSession()
                    return null
                }

                val authResponse: AuthResponse =
                    refreshResponse.body()
                        ?: return null

                val newAccessToken =
                    authResponse.accessToken
                        ?: return null

                val newRefreshToken =
                    authResponse.refreshToken
                        ?: refreshToken

                val userId =
                    sessionManager.getUserId()
                        ?: authResponse.user?.id
                        ?: return null

                sessionManager.saveSession(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken,
                    userId = userId
                )

                request
                    .newBuilder()
                    .header(
                        "Authorization",
                        "Bearer $newAccessToken"
                    )
                    .build()

            } catch (exception: Exception) {

                sessionManager.clearSession()
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {

        var count = 1
        var previous = response.priorResponse()

        while (previous != null) {
            count++
            previous = previous.priorResponse()
        }

        return count
    }
}