package com.oasis.budgeting.data.repository

import com.oasis.budgeting.data.model.Insight
import com.oasis.budgeting.data.remote.ApiService

class InsightRepository(private val apiService: ApiService) {

    suspend fun getInsights(): Result<List<Insight>> {
        return try {
            val response = apiService.getInsights()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load insights"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
