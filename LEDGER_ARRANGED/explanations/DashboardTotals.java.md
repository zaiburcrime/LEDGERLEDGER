# DashboardTotals.java

This tab explains only `src/DashboardTotals.java`.

In the Explorer, open the `explanations` folder and click another name to open that class in its own tab. Read `00 How the program works.md` first if you have not yet.

## What this class is

`DashboardTotals` is a calculator, not a storage box. It does not keep its own copy of your records. The Dashboard asks it, "for this month, what are the card numbers?"

## How it works

The constructor reads allowance, extra income, savings deposits, spending, balance, and the expense count from `LedgerData`. Then it counts the days left in the month.

- A past month has 0 days left.
- The current month counts today through the last day.
- A future month counts every day of that month.

Daily budget is the balance divided by the days left. A negative balance is treated as 0 for that division. The result is rounded to centavos.

## The code

```java
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Numbers shown on the Dashboard cards.
 *
 * This class does not store the records. It only adds up what is already
 * in LedgerData:
 *   ArrayList  - expenses, goals, extra income
 *   HashMap    - planned budget per category
 * The window asks for a new DashboardTotals whenever the month changes.
 */
public class DashboardTotals {
    final double allowance, income, savings, spent, balance, dailyBudget;
    final int remainingDays, expenseCount;

    DashboardTotals(LedgerData data, YearMonth month, LocalDate today) {
        allowance = data.allowance(month);
        income = data.extraIncome(month);
        savings = data.savings(month);
        spent = data.spent(month, null);
        balance = data.balance(month);
        expenseCount = data.expenseCount(month);
        YearMonth current = YearMonth.from(today);
        remainingDays = month.isBefore(current) ? 0 : month.equals(current)
                ? month.lengthOfMonth() - today.getDayOfMonth() + 1 : month.lengthOfMonth();
        dailyBudget = remainingDays == 0 ? 0 : LedgerData.cents(Math.max(0, balance) / remainingDays);
    }
}
```

## Walk through the code

| Line | What it means |
| --- | --- |
| 1–2 | Import date types. `YearMonth` is a month such as November 2026, without a day. |
| 4–12 | Comment. The numbers come from lists and maps that already live in `LedgerData`. |
| 13 | Start the class. |
| 14 | The six money results the cards and the summary can read. |
| 15 | Days remaining, and how many expenses are in the month. |
| 17 | Constructor. It needs the data, the month on screen, and today's date. |
| 18–23 | Copy each total out of `LedgerData`. `spent(month, null)` means every category, because `null` means "no category filter." |
| 24 | The month that contains today. |
| 25–26 | Choose the days-left rule described above. `? :` is a short if/else. |
| 27 | If no days remain, daily budget is 0. Otherwise divide. `Math.max(0, balance)` blocks a negative daily number. `cents` rounds to two decimals. |
| 28–29 | End of the constructor and the class. |
