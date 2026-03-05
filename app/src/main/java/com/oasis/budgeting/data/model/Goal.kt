package com.oasis.budgeting.data.model

import com.google.gson.annotations.SerializedName

data class Goal(
    val id: Int,
    val name: String,
    @SerializedName("target_amount") val targetAmount: Double,
    @SerializedName("current_amount") val currentAmount: Double,
    @SerializedName("target_date") val targetDate: String? = null,
    val category: String? = null,
    val notes: String? = null
)

data class CreateGoalRequest(
    val name: String,
    @SerializedName("target_amount") val targetAmount: Double,
    @SerializedName("current_amount") val currentAmount: Double = 0.0,
    @SerializedName("target_date") val targetDate: String? = null,
    val category: String? = null
)

data class UpdateGoalRequest(
    val name: String? = null,
    @SerializedName("target_amount") val targetAmount: Double? = null,
    @SerializedName("current_amount") val currentAmount: Double? = null,
    @SerializedName("target_date") val targetDate: String? = null,
    val category: String? = null
)

data class ContributeRequest(
    val amount: Double
)
