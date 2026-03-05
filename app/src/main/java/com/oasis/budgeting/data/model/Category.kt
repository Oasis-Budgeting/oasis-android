package com.oasis.budgeting.data.model

import com.google.gson.annotations.SerializedName

data class CategoryGroup(
    val id: Int,
    val name: String,
    val categories: List<Category> = emptyList()
)

data class Category(
    val id: Int,
    val name: String,
    @SerializedName("group_id") val groupId: Int
)

data class CreateCategoryGroupRequest(val name: String)

data class UpdateCategoryGroupRequest(val name: String)

data class CreateCategoryRequest(
    val name: String,
    @SerializedName("group_id") val groupId: Int
)

data class UpdateCategoryRequest(val name: String? = null)
