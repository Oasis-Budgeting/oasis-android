package com.oasis.budgeting.data.model

import com.google.gson.annotations.SerializedName

data class SpendingByCategory(
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("category_name") val categoryName: String,
    val amount: Double,
    val percentage: Double = 0.0
)

data class IncomeVsExpense(
    val month: String,
    val income: Double,
    val expenses: Double,
    val net: Double = 0.0
)

data class NetWorth(
    val month: String,
    val assets: Double,
    val liabilities: Double,
    @SerializedName("net_worth") val netWorth: Double
)

data class BudgetVsActual(
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("category_name") val categoryName: String,
    val budgeted: Double,
    val actual: Double,
    val difference: Double = 0.0
)

data class SpendingTrend(
    val month: String,
    val amount: Double,
    @SerializedName("category_name") val categoryName: String? = null
)
