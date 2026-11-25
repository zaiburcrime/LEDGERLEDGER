# Goal.java

This tab explains only `src/Goal.java`.

In the Explorer, open the `explanations` folder and click another name to open that class in its own tab. Read `00 How the program works.md` first if you have not yet.

## What this class is

`Goal` is one savings goal. Example: name "New shoes", target 2000, saved 500, deadline May 31, 2028.

The class does not draw the table and it does not save the file. It only holds the four facts and can answer "what percent is saved?"

All goals live together in an ArrayList called `goals` inside `LedgerData`. Adding a goal is `goals.add(...)`. Deleting a goal removes it from that list.

## How it works

When the Savings Goals page clicks **Add goal**, `FinancialTracker` runs `new Goal(name, target, 0, deadline)`. The constructor stores the name, target, and saved amount. The deadline text is cleaned by `storedDeadline` so old files and new files use the same date format.

`percentSaved()` divides saved by target and turns that into a whole number from 0 to 100. The table shows that number with a `%` sign. If the target is 0, it returns 0 instead of dividing by zero.

## The code

```java
/**
 * One savings goal.
 *
 * DATA STRUCTURE: goals are stored in an ArrayList (LedgerData.goals).
 * Adding a goal is goals.add(...). Deleting uses the list index.
 */
public class Goal {
    String name;
    double target;
    double saved;
    String deadline;

    Goal(String name, double target, double saved, String deadline) {
        this.name = name;
        this.target = target;
        this.saved = saved;
        this.deadline = FinancialTracker.storedDeadline(deadline).toString();
    }

    /** How much of the target is already saved, from 0 to 100. */
    public int percentSaved() {
        if (target <= 0) return 0;
        return (int) Math.min(100, Math.round(saved / target * 100));
    }
}
```

## Walk through every line

| Line | What it means |
| --- | --- |
| 1–6 | A comment for humans. Java skips it. It says goals are stored in `LedgerData.goals`. |
| 7 | `public class Goal` starts the blueprint. |
| 8 | `name` is the goal's title. `String` means text. |
| 9 | `target` is the amount you want. `double` is a number with a decimal point. |
| 10 | `saved` is how much is already in the goal. |
| 11 | `deadline` is the due date stored as text, such as `2028-05-31`. |
| 13 | The constructor runs when the program writes `new Goal(...)`. The four values in the parentheses are what the caller passes in. |
| 14 | `this.name = name` copies the passed-in name into this goal. `this` means "this object." |
| 15 | Store the target. |
| 16 | Store the amount already saved. A new goal passes `0`. |
| 17 | Turn the deadline into a real date, then back into standard text, and store that text. |
| 20–21 | `percentSaved` gives back an `int`, a whole number. |
| 22 | If the target is 0, stop and return 0. |
| 23 | `saved / target * 100` is the percent. `Math.round` rounds it. `Math.min(100, ...)` stops it from going above 100. `(int)` drops the decimal. |
| 24–25 | Close the method and the class. |

Example: target 1000, saved 250. The result is 25, and the table shows `25%`.
