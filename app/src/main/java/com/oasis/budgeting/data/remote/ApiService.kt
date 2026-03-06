package com.oasis.budgeting.data.remote

import com.oasis.budgeting.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserResponse>

    // Accounts
    @GET("api/accounts")
    suspend fun getAccounts(): Response<List<Account>>

    @POST("api/accounts")
    suspend fun createAccount(@Body request: CreateAccountRequest): Response<Account>

    @PUT("api/accounts/{id}")
    suspend fun updateAccount(@Path("id") id: Int, @Body request: UpdateAccountRequest): Response<Account>

    @DELETE("api/accounts/{id}")
    suspend fun deleteAccount(@Path("id") id: Int): Response<Unit>

    @POST("api/accounts/{id}/reconcile")
    suspend fun reconcileAccount(@Path("id") id: Int, @Body request: ReconcileRequest): Response<Unit>

    // Category Groups
    @GET("api/category-groups")
    suspend fun getCategoryGroups(): Response<List<CategoryGroup>>

    @POST("api/category-groups")
    suspend fun createCategoryGroup(@Body request: CreateCategoryGroupRequest): Response<CategoryGroup>

    @PUT("api/category-groups/{id}")
    suspend fun updateCategoryGroup(@Path("id") id: Int, @Body request: UpdateCategoryGroupRequest): Response<CategoryGroup>

    @DELETE("api/category-groups/{id}")
    suspend fun deleteCategoryGroup(@Path("id") id: Int): Response<Unit>

    // Categories
    @POST("api/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<Category>

    @PUT("api/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body request: UpdateCategoryRequest): Response<Category>

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): Response<Unit>

    // Transactions
    @GET("api/transactions")
    suspend fun getTransactions(
        @Query("account_id") accountId: Int? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<TransactionsResponse>

    @POST("api/transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): Response<Transaction>

    @PUT("api/transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: Int, @Body request: UpdateTransactionRequest): Response<Transaction>

    @DELETE("api/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Int): Response<Unit>

    @GET("api/transactions/suggest-category")
    suspend fun suggestCategory(
        @Query("payee") payee: String,
        @Query("amount") amount: Double? = null
    ): Response<SuggestCategoryResponse>

    // Budget
    @GET("api/budget/{month}")
    suspend fun getBudget(@Path("month") month: String): Response<BudgetResponse>

    @GET("api/budget/summary/{month}")
    suspend fun getBudgetSummary(@Path("month") month: String): Response<BudgetSummary>

    @PUT("api/budget/{month}/{categoryId}")
    suspend fun assignBudget(
        @Path("month") month: String,
        @Path("categoryId") categoryId: Int,
        @Body request: AssignBudgetRequest
    ): Response<BudgetCategory>

    // Reports
    @GET("api/reports/spending-by-category")
    suspend fun getSpendingByCategory(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<List<SpendingByCategory>>

    @GET("api/reports/income-vs-expense")
    suspend fun getIncomeVsExpense(@Query("months") months: Int? = null): Response<List<IncomeVsExpense>>

    @GET("api/reports/net-worth")
    suspend fun getNetWorth(@Query("months") months: Int? = null): Response<List<NetWorth>>

    @GET("api/reports/budget-vs-actual/{month}")
    suspend fun getBudgetVsActual(@Path("month") month: String): Response<List<BudgetVsActual>>

    @GET("api/reports/spending-trend")
    suspend fun getSpendingTrend(@Query("months") months: Int? = null): Response<List<SpendingTrend>>

    // Settings
    @GET("api/settings")
    suspend fun getSettings(): Response<SettingsResponse>

    @PUT("api/settings")
    suspend fun updateSettings(@Body request: SettingsUpdateRequest): Response<SettingsResponse>

    // Subscriptions
    @GET("api/subscriptions")
    suspend fun getSubscriptions(): Response<List<Subscription>>

    @POST("api/subscriptions")
    suspend fun createSubscription(@Body request: CreateSubscriptionRequest): Response<Subscription>

    @PUT("api/subscriptions/{id}")
    suspend fun updateSubscription(@Path("id") id: Int, @Body request: UpdateSubscriptionRequest): Response<Subscription>

    @DELETE("api/subscriptions/{id}")
    suspend fun deleteSubscription(@Path("id") id: Int): Response<Unit>

    @GET("api/subscriptions/upcoming")
    suspend fun getUpcomingBills(@Query("days") days: Int? = null): Response<List<UpcomingBill>>

    // Goals
    @GET("api/goals")
    suspend fun getGoals(): Response<List<Goal>>

    @POST("api/goals")
    suspend fun createGoal(@Body request: CreateGoalRequest): Response<Goal>

    @PUT("api/goals/{id}")
    suspend fun updateGoal(@Path("id") id: Int, @Body request: UpdateGoalRequest): Response<Goal>

    @DELETE("api/goals/{id}")
    suspend fun deleteGoal(@Path("id") id: Int): Response<Unit>

    @POST("api/goals/{id}/contribute")
    suspend fun contributeToGoal(@Path("id") id: Int, @Body request: ContributeRequest): Response<Goal>

    // Debts
    @GET("api/debts")
    suspend fun getDebts(): Response<List<Debt>>

    @POST("api/debts")
    suspend fun createDebt(@Body request: CreateDebtRequest): Response<Debt>

    @PUT("api/debts/{id}")
    suspend fun updateDebt(@Path("id") id: Int, @Body request: UpdateDebtRequest): Response<Debt>

    @DELETE("api/debts/{id}")
    suspend fun deleteDebt(@Path("id") id: Int): Response<Unit>

    @GET("api/debts/strategies")
    suspend fun getDebtStrategies(): Response<DebtStrategies>

    // Investments
    @GET("api/investments")
    suspend fun getInvestments(): Response<List<Investment>>

    @POST("api/investments")
    suspend fun createInvestment(@Body request: CreateInvestmentRequest): Response<Investment>

    @PUT("api/investments/{id}")
    suspend fun updateInvestment(@Path("id") id: Int, @Body request: UpdateInvestmentRequest): Response<Investment>

    @DELETE("api/investments/{id}")
    suspend fun deleteInvestment(@Path("id") id: Int): Response<Unit>

    // Insights
    @GET("api/insights")
    suspend fun getInsights(): Response<List<Insight>>
}
