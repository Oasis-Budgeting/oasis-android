package com.oasis.budgeting.data.repository

import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.remote.ApiService

class DebtRepository(private val apiService: ApiService) {

    suspend fun getDebts(): Result<List<Debt>> {
        return try {
            val response = apiService.getDebts()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load debts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDebt(request: CreateDebtRequest): Result<Debt> {
        return try {
            val response = apiService.createDebt(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create debt"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDebt(id: Int, request: UpdateDebtRequest): Result<Debt> {
        return try {
            val response = apiService.updateDebt(id, request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update debt"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDebt(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteDebt(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete debt"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStrategies(): Result<DebtStrategies> {
        return try {
            val response = apiService.getDebtStrategies()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load debt strategies"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
