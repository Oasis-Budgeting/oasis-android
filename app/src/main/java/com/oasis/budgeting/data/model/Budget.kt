package com.oasis.budgeting.data.model

import com.google.gson.annotations.SerializedName

data class BudgetResponse(
    val month: String,
    val groups: List<BudgetGroup>
)

data class BudgetGroup(
    val id: Int,
    val name: String,
    val categories: List<BudgetCategory>,
    val assigned: Double = 0.0,
    val activity: Double = 0.0,
    val available: Double = 0.0
)

data class BudgetCategory(
    val id: Int,
    val name: String,
    val assigned: Double = 0.0,
    val activity: Double = 0.0,
    val available: Double = 0.0,
    @SerializedName("group_id") val groupId: Int = 0
)

data class BudgetSummary(
    val month: String,
    @SerializedName("total_budgeted") val totalBudgeted: Double = 0.0,
    @SerializedName("total_activity") val totalActivity: Double = 0.0,
    @SerializedName("total_available") val totalAvailable: Double = 0.0,
    @SerializedName("total_income") val totalIncome: Double = 0.0,
    @SerializedName("overspent") val overspent: Double = 0.0,
    @SerializedName("to_be_budgeted") val toBeBudgeted: Double = 0.0
)

data class AssignBudgetRequest(
    val assigned: Double
)
