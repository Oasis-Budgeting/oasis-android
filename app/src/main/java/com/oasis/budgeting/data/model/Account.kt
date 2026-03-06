package com.oasis.budgeting.data.model

import com.google.gson.annotations.SerializedName

data class Account(
    val id: Int,
    val name: String,
    val type: String,
    val balance: Double,
    @SerializedName("on_budget") val onBudget: Boolean = true,
    @SerializedName("cleared_balance") val clearedBalance: Double = 0.0,
    @SerializedName("uncleared_balance") val unclearedBalance: Double = 0.0,
    val closed: Boolean = false,
    @SerializedName("is_credit_card_tracking") val isCreditCardTracking: Boolean = false
)

data class CreateAccountRequest(
    val name: String,
    val type: String,
    val balance: Double,
    @SerializedName("on_budget") val onBudget: Boolean = true
)

data class UpdateAccountRequest(
    val name: String? = null,
    val type: String? = null,
    val balance: Double? = null,
    @SerializedName("on_budget") val onBudget: Boolean? = null,
    val closed: Boolean? = null
)

data class ReconcileRequest(
    @SerializedName("statementBalance") val statementBalance: Double,
    @SerializedName("statementDate") val statementDate: String
)
