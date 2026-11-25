# MoneyEntry.java

This tab explains only `src/MoneyEntry.java`.

In the Explorer, open the `explanations` folder and click another name to open that class in its own tab. Read `00 How the program works.md` first if you have not yet.

## What this class is

`MoneyEntry` is one amount with a date and a short description. Ledger uses the same small class for three different lists:

- extra income, such as a gift
- a deposit into a savings goal
- money returned when a goal is reset or deleted

## How it works

The fields are `final`, so after `new MoneyEntry(...)` the date, description, and amount do not get replaced. A change is done by putting a different `MoneyEntry` into the list. `LedgerData` stores these in ArrayLists and adds up the ones that fall in the month you are viewing.

## The code

```java
import java.time.LocalDate;

/**
 * Extra income (money that is not the monthly allowance).
 *
 * DATA STRUCTURE: stored in an ArrayList inside LedgerData.
 */
public class MoneyEntry {
    final LocalDate date;
    final String description;
    final double amount;

    MoneyEntry(LocalDate date, String description, double amount) {
        this.date = date;
        this.description = description;
        this.amount = amount;
    }
}
```

## Walk through every line

| Line | What it means |
| --- | --- |
| 1 | Import Java's date type. |
| 3–7 | Comment. These objects live in lists inside `LedgerData`. |
| 8 | Start the class. |
| 9 | The day of the record. |
| 10 | The label, such as "birthday gift" or the goal name. |
| 11 | The money. |
| 13 | Constructor. It runs on `new MoneyEntry(date, description, amount)`. |
| 14–16 | Copy the three values into this object. |
| 17–18 | End of the constructor and the class. |
