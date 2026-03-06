package com.oasis.budgeting.data.model

data class Insight(
    val type: String,
    val title: String,
    val message: String,
    val severity: String = "info"
)
