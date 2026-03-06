package com.oasis.budgeting.data.repository

import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.remote.ApiService

class ReportRepository(private val apiService: ApiService) {

    suspend fun getSpendingByCategory(startDate: String? = null, endDate: String? = null): Result<List<SpendingByCategory>> {
        return try {
            val response = apiService.getSpendingByCategory(startDate, endDate)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load spending report"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIncomeVsExpense(months: Int? = null): Result<List<IncomeVsExpense>> {
        return try {
            val response = apiService.getIncomeVsExpense(months)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load income vs expense report"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNetWorth(months: Int? = null): Result<List<NetWorth>> {
        return try {
            val response = apiService.getNetWorth(months)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load net worth report"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBudgetVsActual(month: String): Result<List<BudgetVsActual>> {
        return try {
            val response = apiService.getBudgetVsActual(month)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load budget vs actual report"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSpendingTrend(months: Int? = null): Result<List<SpendingTrend>> {
        return try {
            val response = apiService.getSpendingTrend(months)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load spending trend"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
