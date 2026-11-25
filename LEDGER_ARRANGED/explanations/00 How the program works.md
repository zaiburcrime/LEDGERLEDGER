# How the program works

This tab is the story of one run. It is not a Java file. Open the other files in this folder when you want one class at a time. Each of those tabs explains that `.java` file, then shows its code.

1. Java starts in `FinancialTracker.main`.
2. The login window (`LoginDialog`) reads `ledger-users.txt`. Practice login: `student` / `1234`.
3. After a correct login, `LedgerData` loads that user's file from `LedgerData\<user-id>.properties`.
4. The window shows five pages: Dashboard, Expenses, Allowance, Savings Goals, Reports.
5. A button changes a list or a map, then `changed()` saves the file and refreshes the screens.

Money for one month:

```
balance = allowance + extra income - expenses - savings deposits + savings returned
```

Three data structures from class:

- **ArrayList** — a list that can grow. Expenses, goals, categories, income.
- **HashMap** — look up a value by a name. Allowance per month, and the plan per category.
- **Stack** — a pile. The last deleted expense is the first one Undo puts back.

| File | One-sentence job |
| --- | --- |
| `Goal.java` | One savings goal: name, target, amount saved, deadline. |
| `Expense.java` | One purchase, plus search and bubble sort. |
| `MoneyEntry.java` | One dated amount: income, a deposit, or money returned from a goal. |
| `Slice.java` | One piece of a chart: a name and a total. |
| `ReportBar.java` | Draws one colored bar on the Reports page. |
| `DashboardTotals.java` | Adds up the four numbers on the Dashboard cards. |
| `LedgerData.java` | All of one user's records, plus save, load, and CSV export. |
| `LoginDialog.java` | The login window and the account file. |
| `FinancialTracker.java` | The main window, the buttons, and the rules. |
