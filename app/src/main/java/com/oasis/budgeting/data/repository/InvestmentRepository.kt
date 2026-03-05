package com.oasis.budgeting.data.repository

import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.remote.ApiService

class InvestmentRepository(private val apiService: ApiService) {

    suspend fun getInvestments(): Result<List<Investment>> {
        return try {
            val response = apiService.getInvestments()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load investments"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createInvestment(request: CreateInvestmentRequest): Result<Investment> {
        return try {
            val response = apiService.createInvestment(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create investment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInvestment(id: Int, request: UpdateInvestmentRequest): Result<Investment> {
        return try {
            val response = apiService.updateInvestment(id, request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update investment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteInvestment(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteInvestment(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete investment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
