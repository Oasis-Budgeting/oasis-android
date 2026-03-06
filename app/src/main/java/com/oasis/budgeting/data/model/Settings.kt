package com.oasis.budgeting.data.model

data class SettingsResponse(
    val currency: String = "USD",
    val locale: String = "en-US",
    val theme: String = "dark",
    val dateFormat: String = "YYYY-MM-DD"
)

data class SettingsUpdateRequest(
    val currency: String? = null,
    val locale: String? = null,
    val theme: String? = null,
    val dateFormat: String? = null
)
