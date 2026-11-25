# Expense.java

This tab explains only `src/Expense.java`.

In the Explorer, open the `explanations` folder and click another name to open that class in its own tab. Read `00 How the program works.md` first if you have not yet.

## What this class is

`Expense` is one thing you bought. It holds the date, what it was, the category, and the amount.

The same file also contains two class methods, which means they belong to `Expense` in general, not to one purchase:

- `matches` — does this expense contain the search text?
- `bubbleSort` — put a list of expenses in order by swapping neighbors.

## How it works

The Expenses page creates an object with `new Expense(date, description, category, amount)` and adds it to the ArrayList. The table is only a view of that list.

When you type in the search box, the program asks `Expense.matches` for each row. When you change the sort dropdown, it calls `Expense.bubbleSort`. Mode 0 is newest first, 1 is oldest first, 2 is biggest amount first, 3 is smallest amount first, and 4 is description A to Z.

## The code

```java
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * One expense record.
 *
 * DATA STRUCTURE: these objects are stored in an ArrayList
 * (see LedgerData.expenses). The table is just a view of that list.
 */
public class Expense {
    LocalDate date;
    String description;
    String category;
    double amount;

    Expense(LocalDate date, String description, String category, double amount) {
        this.date = date;
        this.description = description;
        this.category = category;
        this.amount = amount;
    }

    /**
     * DATA STRUCTURE: linear search on one Expense.
     * Returns true when the search box text is in the description,
     * category, date, or amount.
     */
    public static boolean matches(Expense expense, String search) {
        if (expense.description.toLowerCase(Locale.ROOT).contains(search)) {
            return true;
        }
        if (expense.category.toLowerCase(Locale.ROOT).contains(search)) {
            return true;
        }
        String shownDate = expense.date.format(DateTimeFormatter.ofPattern("MM/dd/uuuu"));
        return expense.date.toString().contains(search)
                || shownDate.contains(search)
                || String.format(Locale.ROOT, "%.2f", expense.amount).contains(search);
    }

    /**
     * DATA STRUCTURE: bubble sort on an ArrayList of Expense.
     * This is the sorting algorithm from class, not Collections.sort.
     * mode 0 = newest first, 1 = oldest first, 2 = amount high to low,
     * 3 = amount low to high, 4 = description A-Z.
     */
    public static void bubbleSort(ArrayList<Expense> list, int mode) {
        for (int i = 0; i < list.size() - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < list.size() - 1 - i; j++) {
                Expense first = list.get(j);
                Expense second = list.get(j + 1);
                boolean swap = false;

                if (mode == 0) {
                    swap = first.date.isBefore(second.date);
                } else if (mode == 1) {
                    swap = first.date.isAfter(second.date);
                } else if (mode == 2) {
                    swap = first.amount < second.amount;
                } else if (mode == 3) {
                    swap = first.amount > second.amount;
                } else {
                    swap = first.description.compareToIgnoreCase(second.description) > 0;
                }

                if (swap) {
                    // swap two neighbors in the ArrayList
                    list.set(j, second);
                    list.set(j + 1, first);
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }
}
```

## Walk through the code

**Lines 1–4.** Imports. `LocalDate` is a calendar date. `DateTimeFormatter` turns a date into text. `ArrayList` is the list bubble sort receives. `Locale` keeps lower-case and decimals the same on every computer.

**Lines 6–11.** A comment. Java ignores it.

**Lines 12–23.** The class and its four fields. The constructor copies the four values into `this` object.

**Lines 30–41. Search.**

- Line 31: if the description contains the search text, return `true` immediately.
- Lines 34–36: try the category the same way.
- Line 37: build the date the way the table shows it, `MM/dd/yyyy`.
- Lines 38–40: also match the ISO date (`2026-11-24`), the shown date, or the amount written with two decimals (`10.50`). `||` means "or".

**Lines 49–78. Bubble sort.**

The outer loop makes one pass after another. The inner loop compares item `j` with the next item. If they are in the wrong order, lines 71–72 swap them. `swapped` remembers whether this pass moved anything. If a full pass swaps nothing, line 76 stops early because the list is finished.

The `if / else if` chain picks the comparison from `mode`. For dates, `isBefore` and `isAfter` decide the order. For amounts, `<` and `>` decide. For names, `compareToIgnoreCase` compares the words without caring about capital letters.
