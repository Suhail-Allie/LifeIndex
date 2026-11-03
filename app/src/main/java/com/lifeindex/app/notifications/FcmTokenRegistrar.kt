package com.lifeindex.app.notifications

import android.content.Context
import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.data.model.DeviceRegistrationRequest
import com.lifeindex.app.data.remote.RetrofitClient

object FcmTokenRegistrar {
    suspend fun register(
        context: Context,
        accessToken: String,
        fcmToken: String
    ) {
        RetrofitClient.initialize(context.applicationContext)

        val response = RetrofitClient.api.registerDevice(
            token = "Bearer $accessToken",
            request = DeviceRegistrationRequest(fcmToken)
        )

        if (!response.isSuccessful) {
            throw IllegalStateException(
                "FCM device registration failed with HTTP ${response.code()}"
            )
        }
    }

    suspend fun registerForSavedSession(context: Context, fcmToken: String) {
        val appContext = context.applicationContext
        val accessToken = SessionManager(appContext).getAccessToken() ?: return
        register(appContext, accessToken, fcmToken)
    }
}
