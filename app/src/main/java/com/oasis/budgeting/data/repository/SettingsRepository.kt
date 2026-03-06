package com.oasis.budgeting.data.repository

import com.oasis.budgeting.data.model.SettingsResponse
import com.oasis.budgeting.data.model.SettingsUpdateRequest
import com.oasis.budgeting.data.remote.ApiService
import com.oasis.budgeting.data.remote.TokenManager

class SettingsRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun getSettings(): Result<SettingsResponse> {
        return try {
            val response = apiService.getSettings()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSettings(request: SettingsUpdateRequest): Result<SettingsResponse> {
        return try {
            val response = apiService.updateSettings(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServerUrl(): String = tokenManager.getServerUrl()

    suspend fun updateServerUrl(url: String) {
        tokenManager.saveServerUrl(url)
    }
}
