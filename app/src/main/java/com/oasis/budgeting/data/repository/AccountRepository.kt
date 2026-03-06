package com.oasis.budgeting.data.repository

import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.remote.ApiService

class AccountRepository(private val apiService: ApiService) {

    suspend fun getAccounts(): Result<List<Account>> {
        return try {
            val response = apiService.getAccounts()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load accounts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAccount(name: String, type: String, balance: Double, onBudget: Boolean = true): Result<Account> {
        return try {
            val response = apiService.createAccount(CreateAccountRequest(name, type, balance, onBudget))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAccount(id: Int, request: UpdateAccountRequest): Result<Account> {
        return try {
            val response = apiService.updateAccount(id, request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteAccount(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reconcileAccount(id: Int, statementBalance: Double, statementDate: String): Result<Unit> {
        return try {
            val response = apiService.reconcileAccount(id, ReconcileRequest(statementBalance, statementDate))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to reconcile account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
