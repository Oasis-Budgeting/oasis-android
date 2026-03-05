# Oasis Budgeting - Android

An Android companion app for [Oasis](https://github.com/Oasis-Budgeting/oasis), a self-hosted envelope budgeting application. This app connects to the Oasis web server for all data read and write operations.

## Features

- **Authentication** – Register, login, JWT-based session management
- **Dashboard** – Overview of accounts, budget summary, recent transactions, and insights
- **Accounts** – Create, edit, and delete checking, savings, and credit card accounts with balance tracking
- **Transactions** – Full transaction management with search, filtering, and pagination
- **Budget** – Monthly envelope budgeting with category groups, assigned/activity/available tracking
- **Reports** – Spending by category, income vs. expense, net worth, budget vs. actual, spending trends
- **Goals** – Savings goals with progress tracking and contributions
- **Debts** – Debt tracking with balances, interest rates, and payoff strategies
- **Subscriptions** – Recurring subscription and bill management
- **Investments** – Investment portfolio tracking with current value and gain/loss
- **Settings** – Currency, locale, theme configuration, and server URL management

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material 3
- **Architecture:** MVVM with Repository pattern
- **Networking:** Retrofit 2 + OkHttp
- **Local Storage:** DataStore Preferences (for auth token and settings)
- **Navigation:** Navigation Compose
- **Async:** Kotlin Coroutines + StateFlow

## Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2) or newer
- JDK 17
- Android SDK with API 35

### Server Setup

The app connects to the Oasis web server. By default it points to `http://192.168.0.105:3003`. You can change the server URL in **Settings** after logging in.

Make sure your Oasis server is running and accessible from the Android device/emulator on the same network.

### Build & Run

1. Clone this repository
2. Open in Android Studio
3. Sync Gradle
4. Run on a device or emulator (API 26+)

### Configuration

The server URL is stored locally and can be changed at any time from the Settings screen. The auth token is stored securely using Android DataStore.

## Project Structure

```
app/src/main/java/com/oasis/budgeting/
├── MainActivity.kt
├── data/
│   ├── model/          # Data classes for API request/response
│   ├── remote/         # Retrofit API service, client, token manager
│   └── repository/     # Repository layer for each domain
└── ui/
    ├── navigation/     # Nav graph and bottom navigation
    ├── screens/        # Composable screens
    ├── theme/          # Material 3 theme, colors, typography
    └── viewmodel/      # ViewModels for each screen
```

## API Endpoints

The app communicates with the Oasis backend server via REST API:

| Feature | Endpoints |
|---------|-----------|
| Auth | `POST /api/auth/login`, `POST /api/auth/register`, `GET /api/auth/me` |
| Accounts | `GET/POST /api/accounts`, `PUT/DELETE /api/accounts/{id}`, `POST /api/accounts/{id}/reconcile` |
| Categories | `GET/POST /api/category-groups`, `POST /api/categories` |
| Transactions | `GET/POST /api/transactions`, `PUT/DELETE /api/transactions/{id}` |
| Budget | `GET /api/budget/{month}`, `GET /api/budget/summary/{month}`, `PUT /api/budget/{month}/{categoryId}` |
| Reports | `GET /api/reports/spending-by-category`, `income-vs-expense`, `net-worth`, `budget-vs-actual/{month}`, `spending-trend` |
| Subscriptions | `GET/POST /api/subscriptions`, `PUT/DELETE /api/subscriptions/{id}` |
| Goals | `GET/POST /api/goals`, `PUT/DELETE /api/goals/{id}`, `POST /api/goals/{id}/contribute` |
| Debts | `GET/POST /api/debts`, `PUT/DELETE /api/debts/{id}`, `GET /api/debts/strategies` |
| Investments | `GET/POST /api/investments`, `PUT/DELETE /api/investments/{id}` |
| Insights | `GET /api/insights` |
| Settings | `GET/PUT /api/settings` |

## License

ISC / MIT