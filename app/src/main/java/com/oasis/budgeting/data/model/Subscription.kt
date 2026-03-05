package com.oasis.budgeting.data.model

import com.google.gson.annotations.SerializedName

data class Subscription(
    val id: Int,
    val name: String,
    val amount: Double,
    val frequency: String,
    @SerializedName("next_date") val nextDate: String,
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("account_id") val accountId: Int? = null,
    val active: Boolean = true,
    @SerializedName("category_name") val categoryName: String? = null
)

data class CreateSubscriptionRequest(
    val name: String,
    val amount: Double,
    val frequency: String,
    @SerializedName("next_date") val nextDate: String,
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("account_id") val accountId: Int? = null,
    val active: Boolean = true
)

data class UpdateSubscriptionRequest(
    val name: String? = null,
    val amount: Double? = null,
    val frequency: String? = null,
    @SerializedName("next_date") val nextDate: String? = null,
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("account_id") val accountId: Int? = null,
    val active: Boolean? = null
)

data class UpcomingBill(
    val id: Int,
    val name: String,
    val amount: Double,
    @SerializedName("next_date") val nextDate: String,
    val frequency: String,
    @SerializedName("days_until") val daysUntil: Int = 0
)
