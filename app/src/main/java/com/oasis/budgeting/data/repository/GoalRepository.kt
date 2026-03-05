package com.oasis.budgeting.data.repository

import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.remote.ApiService

class GoalRepository(private val apiService: ApiService) {

    suspend fun getGoals(): Result<List<Goal>> {
        return try {
            val response = apiService.getGoals()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load goals"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGoal(request: CreateGoalRequest): Result<Goal> {
        return try {
            val response = apiService.createGoal(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create goal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGoal(id: Int, request: UpdateGoalRequest): Result<Goal> {
        return try {
            val response = apiService.updateGoal(id, request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update goal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGoal(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteGoal(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete goal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun contribute(id: Int, amount: Double): Result<Goal> {
        return try {
            val response = apiService.contributeToGoal(id, ContributeRequest(amount))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to contribute to goal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
