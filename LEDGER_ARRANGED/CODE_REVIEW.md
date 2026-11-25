# Ledger code review

Reviewed: `src` in `LEDGER_ARRANGED` (9 Java files, about 3,600 lines). This folder is not a git repository, so this review covers the program as it stands, not a branch diff.

The program is a desktop student money tracker. It is correct enough to demo: login, record this month, save after every change, and reopen the same user. The risky spots are editing an old expense, the sidebar highlight, budget trimming that is not saved, and passwords stored in clear text.

## How one run works

1. `FinancialTracker.main` starts on the Swing event thread and opens `LoginDialog`.
2. Login reads `ledger-users.txt` from the user home folder. First launch creates `student` / `1234` and `admin` / `admin`.
3. A matching username and password calls `openTracker`. That builds `FinancialTracker`.
4. The window loads `LedgerData\<hex-username>.properties`. The hex name is the lowercased username, so `Student` and `student` share one file.
5. `createContents` builds five cards: Dashboard, Expenses, Allowance, Savings Goals, Reports. The sidebar only swaps which card is visible.
6. A button changes a list or a map, then `changed()` sets `unsaved`, writes the file, and calls `refreshAll()`.

Closing the window saves if something is still unsaved. If the save fails, the window stays open.

## Where the money math lives

One month does not inherit the previous month. January's leftover does not become February's allowance.

```text
balance = allowance + extra income - expenses - savings deposits + savings returned
```

That formula is `LedgerData.balance`. The four Dashboard cards come from `DashboardTotals`, which only calls methods on `LedgerData`.

| Piece | Stored in | What it means |
| --- | --- | --- |
| Allowance | `HashMap<YearMonth, Double>` | Money you set for that month |
| Extra income | `ArrayList<MoneyEntry>` | Dated income that is not the allowance |
| Expenses | `ArrayList<Expense>` | Dated purchases |
| Savings deposits | `ArrayList<MoneyEntry>` | Money moved into a goal, dated |
| Savings returned | `ArrayList<MoneyEntry>` | Money put back when a goal is reset or deleted |
| Goal progress | `ArrayList<Goal>` | Name, target, amount still saved, deadline |
| Category plan | `HashMap<YearMonth, HashMap<String, Double>>` | Planned amount per category for that month |
| Undo | `Stack<Expense>` | Last deleted expense is the first one Undo restores |

A goal's `saved` field is the running total still inside that goal. The deposit list is the history. Deleting a goal does not rewrite old months. It adds a "savings returned" row dated today, so the current month's balance goes back up.

`LEFT PER DAY` is the current balance divided by the days left in the month, including today. A past month shows 0. A negative balance is treated as 0 for that card.

## What each class is for

Read the small classes first. They are the nouns. `FinancialTracker` is the window that uses them.

| Class | Job |
| --- | --- |
| `MoneyEntry` | One dated amount: income, a deposit, or money returned from a goal. Fields cannot change after creation. |
| `Slice` | One chart row: a name and a total. |
| `Goal` | One savings goal. `percentSaved()` is saved / target, capped at 100. |
| `ReportBar` | A colored rectangle. Width is value / max, clamped between 0 and 1. |
| `Expense` | One purchase. `matches` is the search. `bubbleSort` orders the rows the table shows. |
| `DashboardTotals` | Adds up the four cards for one month. It stores nothing of its own. |
| `LedgerData` | Every list and map, plus save, load, reset, legacy import, and CSV export. |
| `LoginDialog` | Login and create-account. Accounts live in `ledger-users.txt`. |
| `FinancialTracker` | The frame, the five pages, validation, and when to save. About 2,900 lines. |

`Expense`, `Goal`, `Slice`, and `MoneyEntry` are package-visible on purpose. The window and `LedgerData` are in the same folder and read the fields directly. There are no getters.

### Data structures from class

- **ArrayList** holds expenses, goals, categories, income, deposits, and savings returns. Adding is `add`. Deleting an expense uses the object, not the row number, because the table may be sorted or filtered.
- **HashMap** looks up this month's allowance and this month's plan by category name. `planned()` always uses `YearMonth.now()`, so the Allowance page edits the real current month even if the Dashboard is showing an older month.
- **Stack** is the undo pile. Delete pushes. Undo pops. The pile is written into the save file, so Undo still works after a restart.

Search builds a second list, then bubble-sorts that copy. The master `expenses` list stays in insert order. Sort modes match the combo box: 0 newest, 1 oldest, 2 amount high to low, 3 amount low to high, 4 description A to Z.

### Save file

`LedgerData.save` writes a Java `Properties` file in UTF-8. It writes to a temp file, then moves that file onto the real one, so a crash mid-save does not leave a half-written ledger. Load checks `version` 1 or 2. Version 2 is an older Ledger edition: the window asks before import and keeps an exact backup.

Accounts are a separate file, one `username;password` line each. The same temp-file move is used there.

## What is solid

- Money entry rejects blank text, zero, and amounts with more than two decimal places. `readAmount` accepts `1250` and `1,250.50`.
- Expense dates for a new row must fall inside the current month. The spinner blocks other months.
- A savings deposit cannot exceed money left this month, and cannot exceed what the goal still needs.
- Category plans cannot add up to more than allowance plus extra income.
- Reset requires the word `RESET` and writes a backup first. If the new save fails, memory is loaded back from that backup.
- CSV export quotes text and prefixes a quote when a cell starts with `=`, `+`, `@`, or `-`, so a description is not treated as a spreadsheet formula.
- Old PBKDF2 password lines still verify. `MessageDigest.isEqual` is used for the hash compare.
- The expense editor keeps the `Expense` object. Sorting the table cannot make Save write a different row.

## Issues

### Issue 1 -- Severity: bug

- File: `src/FinancialTracker.java:1428`
- Description: Editing an expense always forces the date into the current month. `startEditingExpense` calls `configureExpenseDate(expense.date, YearMonth.now())`, and `validatedExpenseDate` (line 2410) rejects any other month. Filters can show last month's rows. Saving a description change on one of those rows moves the purchase into this month. Cancel is safe. Save is not.
- Suggestion: When the row's month is not the current month, keep `expense.date` unless the user is allowed to pick a date in that same month. Do not clamp a historical date with `YearMonth.now()`.

### Issue 2 -- Severity: bug

- File: `src/FinancialTracker.java:511`
- Description: The selected sidebar button is cleared on startup. `showPage` paints the active button `new Color(70, 50, 150)`. `applyTheme` (line 2583) then paints every sidebar button `SIDE`, which is `new Color(108, 78, 214)`. `paintSidebar` (line 2487) tries to restore the active one by comparing to `SIDE_ON` (`new Color(88, 62, 190)`). That color is never the button's background, so every button is painted `SIDE`. After launch, no page looks selected until the user clicks a nav button.
- Suggestion: Use one pair of colors. Have `showPage` set `SIDE_ON` for the active button and `SIDE` for the others, and teach `paintSidebar` and `applyTheme` to keep that pair.

### Issue 3 -- Severity: bug

- File: `src/FinancialTracker.java:2126`
- Description: `refreshAll` calls `enforceBudgetCap`, which deletes or shrinks planned amounts and opens a warning dialog. It does not set `unsaved` and does not save. A file that is already over budget is trimmed in memory on every launch, the dialog appears, and closing the window drops the trim because `unsaved` is still false. The next launch does it again.
- Suggestion: If the cap changes any amount, call `saveData()`. Keep the dialog, but do not hide a data change inside a refresh method that is also used when switching pages.

### Issue 4 -- Severity: bug

- File: `src/LoginDialog.java:291`
- Description: New passwords are written in clear text: `username + ";" + password`. The first run also writes `student;1234` and `admin;admin` into `ledger-users.txt` in the home folder. `verify` can check an old `pbkdf2$` hash, and new accounts never create one. Anyone who can open that file can log in.
- Suggestion: Store only the PBKDF2 form that `verify` already understands. Keep the demo account if the class needs it, and say so on the login window (it already does).

### Issue 5 -- Severity: bug

- File: `src/FinancialTracker.java:117`
- Description: Dark mode is not wired up. `darkMode` stays `false`, and there is no `toggleDarkMode` method. The comment on line 105 says dark colors are the default and that light mode swaps them in `toggleDarkMode()`. `README.md` lists "Dark mode (whole window)". The palette in use is the light one (`BG` is `232, 236, 242`). `applyTheme` has dark branches that never run.
- Suggestion: Add a header toggle that flips `darkMode` and the static `BG`, `CARD`, `INK`, `MUTED`, and `BORDER` colors, then calls `applyTheme`. Or remove the claim from the README and the stale comment until the button exists.

### Issue 6 -- Severity: suggestion

- File: `src/LedgerData.java:83`
- Description: Each month stands alone. Money left in March is not the opening balance of April. The import dialog mentions this. The Dashboard does not. A user can read a full "money left" card and think the cash is still in the wallet next month.
- Suggestion: Add one line on the Dashboard when viewing a past month, such as "This balance does not carry into the next month." Add carry-over only if the course wants a running wallet.

### Issue 7 -- Severity: suggestion

- File: `src/LedgerData.java:215`
- Description: `load` refuses a file whose `owner` property belongs to someone else. `save` never writes `owner`. Files created by this version have no owner, so that check does not protect them. The per-user file name is the real separation.
- Suggestion: `put(p, "owner", username)` in `save`, and pass the signed-in name into `save`.

### Issue 8 -- Severity: suggestion

- File: `src/Expense.java:40`
- Description: Search compares the amount to `%.2f` (`1000.00`). The table shows `₱1,000.00`. Typing the amount the way it appears in the table does not match.
- Suggestion: Also compare against `MONEY.format(expense.amount)` with the commas removed from the search text, or strip commas before `contains`.

### Issue 9 -- Severity: suggestion

- File: `src/FinancialTracker.java:2150`
- Description: "Available funds used" is `(spent + savings deposits) / (allowance + extra income + savings returned)`. After a goal is deleted, the deposit still counts as used and the return also enlarges the denominator. The percentage no longer matches the balance line above it.
- Suggestion: Use `(spent + savings - savings returned) / (allowance + extra income)` when the denominator is positive.

### Issue 10 -- Severity: suggestion

- File: `src/Goal.java:14`
- Description: `Goal` calls `FinancialTracker.storedDeadline` from its constructor. The small data class cannot be understood or reused without the 2,900-line window.
- Suggestion: Parse the deadline in `LedgerData` or in a tiny date helper, and pass the stored ISO string into `Goal`.

### Issue 11 -- Severity: nit

- File: `src/FinancialTracker.java:151`
- Description: The comment says the category list starts with six defaults. `DEFAULT_CATEGORIES` has five: Food, Transport, School, Load, Other.
- Suggestion: Change the comment to five.

### Issue 12 -- Severity: nit

- File: `src/LoginDialog.java:280`
- Description: The error says `Could not read users.txt`. The file is `ledger-users.txt` (`userFile()`, line 34).
- Suggestion: Use the real file name in the message.

### Issue 13 -- Severity: nit

- File: `src/FinancialTracker.java:2169`
- Description: `perDayValueLabel` turns red when `perDay < 0`. `DashboardTotals` clamps that number with `Math.max(0, balance)`, so the red branch never runs. A negative month still shows `₱0.00` per day while "Money left" is red and negative.
- Suggestion: Show the negative daily figure, or set the per-day label to "—" when the balance is below zero.

## Counts

- 5 bugs
- 5 suggestions
- 3 nits

## A path through the code

1. `FinancialTracker.main` and the constructor (about lines 253–345).
2. `LoginDialog.tryLogin`, `createAccount`, `loadUsers`, `saveUsers`.
3. `LedgerData.balance`, `save`, and `load`.
4. `addExpense`, `rebuildExpenseTable`, `deleteExpense`, `undoDelete`.
5. `depositToGoal` and `LedgerData.resetGoal`.
6. `saveAllowance`, `savePlanForCategory`, `refreshDashboard`.
7. `rebuildReports`, `sortedCategoryTotals`, `dailyTotals`.

The class-by-class notes already in `explanations/` match this map. This file is the review of how those pieces behave together.
