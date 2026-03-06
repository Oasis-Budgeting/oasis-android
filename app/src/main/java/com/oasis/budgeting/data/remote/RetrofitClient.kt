package com.oasis.budgeting.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @Volatile
    private var apiService: ApiService? = null

    @Volatile
    private var currentBaseUrl: String? = null

    fun getInstance(tokenManager: TokenManager): ApiService {
        val baseUrl = tokenManager.getServerUrl().trimEnd('/') + "/"
        if (apiService != null && currentBaseUrl == baseUrl) {
            return apiService!!
        }
        synchronized(this) {
            if (apiService != null && currentBaseUrl == baseUrl) {
                return apiService!!
            }
            currentBaseUrl = baseUrl
            apiService = createApiService(baseUrl, tokenManager)
            return apiService!!
        }
    }

    fun resetInstance() {
        synchronized(this) {
            apiService = null
            currentBaseUrl = null
        }
    }

    private fun createApiService(baseUrl: String, tokenManager: TokenManager): ApiService {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val token = tokenManager.getToken()
            val request = if (token != null) {
                original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                original
            }
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
