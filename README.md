# Smart Personal Finance & Wealth Management Hub (Unified 3-Portal Java Application)

An Object-Oriented Java Wealth Management Platform that combines three core personal finance portals into a unified desktop application:
1. **Portal 1: Personal Expense Tracker & Overspending Anomaly Detector**
2. **Portal 2: Subscription & Recurring Bill Renewal Assistant**
3. **Portal 3: Cryptocurrency & Stock Investment Risk Analyzer**

---

## 🌟 Key Features Across 3 Portals

### 📊 Portal 1: Expense Tracker & Budget Alerts
- Log income sources and expenses across categories (Salary, Food, Housing, Utilities, Entertainment).
- Define monthly budget limits ($1,500 default). Triggers custom `BudgetBreachException` warning when an expense breaches budget limits.
- Real-time financial health summary (Total Income, Total Expenses, Net Savings, Budget Status).

### 🔄 Portal 2: Subscription & Recurring Bill Assistant
- Auto-track recurring monthly services (Netflix, Spotify, ChatGPT Plus, Gym).
- Displays days remaining until next renewal.
- Highlights **Urgent Renewal Warnings** for subscriptions due within 5 days.
- Calculates total monthly ($116.97) and annual ($1,403.64) subscription commitments.

### ⚡ Portal 3: Crypto & Stock Investment Risk Analyzer
- Manage crypto assets (BTC, ETH, SOL, PEPE) and tech stocks (NVDA).
- Real-time calculation of total portfolio value, total investment cost, and Net Profit/Loss ($+1,770.25).
- Evaluates overall portfolio risk level (**LOW**, **MEDIUM**, **HIGH**) based on asset volatility distribution.

---

## 📁 Project Structure

```
smart-finance-hub/
├── .vscode/
│   └── settings.json          # VS Code settings
├── transactions.csv           # Persistent expense/income data
├── subscriptions.csv          # Persistent subscription data
├── investments.csv            # Persistent crypto & stock holdings
├── config.txt                 # Persistent budget limits
├── README.md                  # Project documentation
└── src/
    └── com/
        └── finance/
            ├── Main.java                        # Launcher Entry Point
            ├── model/                           # Domain Models
            │   ├── Category.java                # Categories Enum
            │   ├── Transaction.java             # Expense/Income Model
            │   ├── Subscription.java            # Subscription Model
            │   ├── CryptoAsset.java             # Investment Model
            │   └── PortfolioRisk.java           # Risk Level Enum
            ├── exceptions/                      # Custom Exceptions
            │   ├── BudgetBreachException.java
            │   └── InvalidAssetException.java
            ├── repository/                      # CSV File Persistence Layer
            │   └── FileRepository.java
            ├── service/                         # Business Logic Services
            │   ├── ExpenseService.java
            │   ├── SubscriptionService.java
            │   └── CryptoService.java
            └── ui/                              # Presentation Layer
                └── SwingApp.java                # 3-Portal Desktop Application GUI
```

---

## 🚀 How to Run in VS Code

1. Open **VS Code**.
2. Go to **File -> Open Folder...** and select `C:\Users\glnar\.gemini\antigravity\scratch\smart-finance-hub`.
3. Open `src/com/finance/Main.java`.
4. Press **F5** or run in VS Code Terminal (**Ctrl + `**):
   ```bash
   javac -d bin -sourcepath src src/com/finance/Main.java
   java -cp bin com.finance.Main
   ```
