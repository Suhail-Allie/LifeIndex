package com.lifeindex.app.data.remote

import android.content.Context
import com.lifeindex.app.data.local.SessionManager
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://lifeindex-api.onrender.com/api/v1/"

    private lateinit var refreshApi: ApiService
    private lateinit var authenticatedApi: ApiService

    val api: ApiService
        get() {
            check(::authenticatedApi.isInitialized) {
                "RetrofitClient has not been initialized."
            }

            return authenticatedApi
        }

    fun initialize(context: Context) {

        if (::authenticatedApi.isInitialized) {
            return
        }

        val applicationContext = context.applicationContext

        val sessionManager =
            SessionManager(applicationContext)

        /*
         * Separate Retrofit instance used for token refresh.
         * It does not use the authenticator itself, which prevents
         * an infinite refresh loop.
         */
        refreshApi =
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()
                .create(ApiService::class.java)

        val client =
            OkHttpClient.Builder()
                .authenticator(
                    TokenAuthenticator(
                        sessionManager = sessionManager,
                        refreshApi = refreshApi
                    )
                )
                .build()

        authenticatedApi =
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()
                .create(ApiService::class.java)
    }
}