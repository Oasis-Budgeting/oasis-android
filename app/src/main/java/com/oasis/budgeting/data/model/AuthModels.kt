package com.oasis.budgeting.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val username: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class UserResponse(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    @SerializedName("created_at") val createdAt: String? = null
)
