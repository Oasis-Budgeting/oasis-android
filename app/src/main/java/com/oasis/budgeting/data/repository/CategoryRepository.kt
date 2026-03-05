package com.oasis.budgeting.data.repository

import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.remote.ApiService

class CategoryRepository(private val apiService: ApiService) {

    suspend fun getCategoryGroups(): Result<List<CategoryGroup>> {
        return try {
            val response = apiService.getCategoryGroups()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load categories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCategoryGroup(name: String): Result<CategoryGroup> {
        return try {
            val response = apiService.createCategoryGroup(CreateCategoryGroupRequest(name))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create category group"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategoryGroup(id: Int, name: String): Result<CategoryGroup> {
        return try {
            val response = apiService.updateCategoryGroup(id, UpdateCategoryGroupRequest(name))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update category group"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategoryGroup(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteCategoryGroup(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete category group"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCategory(name: String, groupId: Int): Result<Category> {
        return try {
            val response = apiService.createCategory(CreateCategoryRequest(name, groupId))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create category"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategory(id: Int, name: String): Result<Category> {
        return try {
            val response = apiService.updateCategory(id, UpdateCategoryRequest(name))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update category"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteCategory(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete category"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
