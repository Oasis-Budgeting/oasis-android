package com.oasis.budgeting.data.repository

import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.remote.ApiService

class TransactionRepository(private val apiService: ApiService) {

    suspend fun getTransactions(
        accountId: Int? = null,
        categoryId: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        search: String? = null,
        page: Int? = null,
        limit: Int? = null
    ): Result<TransactionsResponse> {
        return try {
            val response = apiService.getTransactions(accountId, categoryId, startDate, endDate, search, page, limit)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load transactions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction> {
        return try {
            val response = apiService.createTransaction(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create transaction"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(id: Int, request: UpdateTransactionRequest): Result<Transaction> {
        return try {
            val response = apiService.updateTransaction(id, request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update transaction"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteTransaction(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete transaction"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun suggestCategory(payee: String, amount: Double? = null): Result<SuggestCategoryResponse> {
        return try {
            val response = apiService.suggestCategory(payee, amount)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get category suggestion"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
