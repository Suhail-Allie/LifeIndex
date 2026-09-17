package com.lifeindex.app.data.remote

import android.content.Context
import com.lifeindex.app.data.local.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "http://10.0.2.2:3000/api/v1/"

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

        val applicationContext =
            context.applicationContext

        val sessionManager =
            SessionManager(applicationContext)

        /*
         * This Retrofit instance intentionally does not use
         * the authenticator because it is responsible for
         * refreshing the token itself.
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