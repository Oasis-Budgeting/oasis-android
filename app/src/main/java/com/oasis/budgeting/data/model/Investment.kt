package com.oasis.budgeting.data.model

import com.google.gson.annotations.SerializedName

data class Investment(
    val id: Int,
    val name: String,
    val type: String,
    @SerializedName("current_value") val currentValue: Double,
    @SerializedName("cost_basis") val costBasis: Double = 0.0,
    val shares: Double? = null,
    val ticker: String? = null
)

data class CreateInvestmentRequest(
    val name: String,
    val type: String,
    @SerializedName("current_value") val currentValue: Double,
    @SerializedName("cost_basis") val costBasis: Double = 0.0,
    val shares: Double? = null,
    val ticker: String? = null
)

data class UpdateInvestmentRequest(
    val name: String? = null,
    val type: String? = null,
    @SerializedName("current_value") val currentValue: Double? = null,
    @SerializedName("cost_basis") val costBasis: Double? = null,
    val shares: Double? = null,
    val ticker: String? = null
)
