package com.oasis.budgeting.data.model

import com.google.gson.annotations.SerializedName

data class Debt(
    val id: Int,
    val name: String,
    val balance: Double,
    @SerializedName("interest_rate") val interestRate: Double,
    @SerializedName("minimum_payment") val minimumPayment: Double,
    val type: String = "other"
)

data class CreateDebtRequest(
    val name: String,
    val balance: Double,
    @SerializedName("interest_rate") val interestRate: Double,
    @SerializedName("minimum_payment") val minimumPayment: Double,
    val type: String = "other"
)

data class UpdateDebtRequest(
    val name: String? = null,
    val balance: Double? = null,
    @SerializedName("interest_rate") val interestRate: Double? = null,
    @SerializedName("minimum_payment") val minimumPayment: Double? = null,
    val type: String? = null
)

data class DebtStrategies(
    val avalanche: List<DebtPayoffStep> = emptyList(),
    val snowball: List<DebtPayoffStep> = emptyList(),
    @SerializedName("total_interest_avalanche") val totalInterestAvalanche: Double = 0.0,
    @SerializedName("total_interest_snowball") val totalInterestSnowball: Double = 0.0,
    @SerializedName("payoff_months_avalanche") val payoffMonthsAvalanche: Int = 0,
    @SerializedName("payoff_months_snowball") val payoffMonthsSnowball: Int = 0
)

data class DebtPayoffStep(
    @SerializedName("debt_id") val debtId: Int,
    @SerializedName("debt_name") val debtName: String,
    val order: Int,
    val balance: Double,
    @SerializedName("interest_rate") val interestRate: Double
)
