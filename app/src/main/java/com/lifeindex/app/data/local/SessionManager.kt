package com.lifeindex.app.data.local

import android.content.Context

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "lifeindex_session",
        Context.MODE_PRIVATE
    )

    fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String
    ) {
        preferences.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putString("user_id", userId)
            .apply()
    }

    fun getAccessToken(): String? {
        return preferences.getString("access_token", null)
    }

    fun getRefreshToken(): String? {
        return preferences.getString("refresh_token", null)
    }

    fun getUserId(): String? {
        return preferences.getString("user_id", null)
    }

    fun clearSession() {
        preferences.edit().clear().apply()
    }
}