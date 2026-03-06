package com.oasis.budgeting.data.model

import com.google.gson.annotations.SerializedName

data class Transaction(
    val id: Int,
    @SerializedName("account_id") val accountId: Int,
    val date: String,
    val payee: String,
    val amount: Double,
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    val memo: String? = null,
    val cleared: Boolean = false,
    val reconciled: Boolean = false,
    @SerializedName("is_transfer") val isTransfer: Boolean = false,
    @SerializedName("transfer_id") val transferId: Int? = null,
    @SerializedName("account_name") val accountName: String? = null
)

data class TransactionsResponse(
    val transactions: List<Transaction>,
    val pagination: Pagination
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    @SerializedName("total_pages") val totalPages: Int
)

data class CreateTransactionRequest(
    @SerializedName("account_id") val accountId: Int,
    val date: String,
    val payee: String,
    val amount: Double,
    @SerializedName("category_id") val categoryId: Int? = null,
    val memo: String? = null,
    val cleared: Boolean = false
)

data class UpdateTransactionRequest(
    @SerializedName("account_id") val accountId: Int? = null,
    val date: String? = null,
    val payee: String? = null,
    val amount: Double? = null,
    @SerializedName("category_id") val categoryId: Int? = null,
    val memo: String? = null,
    val cleared: Boolean? = null
)

data class SuggestCategoryResponse(
    @SerializedName("category_id") val categoryId: Int?,
    @SerializedName("category_name") val categoryName: String?
)
