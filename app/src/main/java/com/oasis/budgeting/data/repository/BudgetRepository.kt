package com.oasis.budgeting.data.repository

import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.remote.ApiService

class BudgetRepository(private val apiService: ApiService) {

    suspend fun getBudget(month: String): Result<BudgetResponse> {
        return try {
            val response = apiService.getBudget(month)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load budget"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSummary(month: String): Result<BudgetSummary> {
        return try {
            val response = apiService.getBudgetSummary(month)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load budget summary"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignBudget(month: String, categoryId: Int, assigned: Double): Result<BudgetCategory> {
        return try {
            val response = apiService.assignBudget(month, categoryId, AssignBudgetRequest(assigned))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to assign budget"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
