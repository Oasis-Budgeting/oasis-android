package com.oasis.budgeting.data.repository

import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.remote.ApiService

class SubscriptionRepository(private val apiService: ApiService) {

    suspend fun getSubscriptions(): Result<List<Subscription>> {
        return try {
            val response = apiService.getSubscriptions()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load subscriptions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSubscription(request: CreateSubscriptionRequest): Result<Subscription> {
        return try {
            val response = apiService.createSubscription(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create subscription"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSubscription(id: Int, request: UpdateSubscriptionRequest): Result<Subscription> {
        return try {
            val response = apiService.updateSubscription(id, request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update subscription"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSubscription(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteSubscription(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete subscription"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUpcomingBills(days: Int? = null): Result<List<UpcomingBill>> {
        return try {
            val response = apiService.getUpcomingBills(days)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load upcoming bills"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
