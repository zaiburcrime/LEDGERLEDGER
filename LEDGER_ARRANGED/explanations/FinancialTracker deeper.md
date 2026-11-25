# FinancialTracker.java — a deeper explanation of the beginning

Read this next to `src/FinancialTracker.java`. The line numbers below match that file.

This class is the window. It does not invent a second copy of your money. It builds the buttons, reads what you type, and asks `LedgerData` to store the records.

---

# Part 1: The very beginning (imports and the class)

```java
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
```

Lines 1–68 are all imports. The snippet above is only a sample of them.

## What an import really does

An import does not run, and it does not create a window. It is a note to the compiler: "when I write the short name, I mean this full name."

`JFrame` is short for `javax.swing.JFrame`. `ArrayList` is short for `java.util.ArrayList`. Without the import, every use would have to be written as the long name. The class would still exist inside Java. Your file would just be harder to read.

Nothing in the import list is your data. Food, goals, and passwords are not imported. Those are created later, while the program is running.

## The groups, and why this file needs each one

| Lines | Package | What this file takes from it |
| --- | --- | --- |
| 1–12 | `java.awt` | Layouts (`BorderLayout`, `GridBagLayout`), `Color`, `Font`, `Dimension`, and `EventQueue` so the window is created on the correct thread. `ActionListener` is how a button reports a click. |
| 13–22 | `java.io` and `java.nio` | Reading and writing the user's `.properties` file. `Path` is the address of that file. |
| 24–29 | `java.text` and `java.time` | `DecimalFormat` prints money. `LocalDate` is one day. `YearMonth` is a whole month, which is how allowance and the dashboard are grouped. |
| 30–38 | `java.util` | `ArrayList`, `HashMap`, `Stack`, and `Properties` (the save-file format). |
| 40–68 | `javax.swing` | The visible pieces: window, button, label, text box, table, dropdown, popup message. |

`java.awt` is the older drawing toolkit. `javax.swing` is the toolkit that builds on it and supplies `JButton` and `JTable`. A Swing screen still needs `java.awt` for color, font, and layout.

## The class line

```java
public class FinancialTracker extends JFrame {
```

Line 97. Three words do three different jobs.

`class FinancialTracker` creates a blueprint named `FinancialTracker`. The file must be named `FinancialTracker.java`. Java matches the file name to the public class name.

`public` means another file is allowed to write `new FinancialTracker(...)`. `LoginDialog` does not construct it. `main` in this same file does, through `openTracker`.

`extends JFrame` means this blueprint starts as a copy of Java's ready-made window, then adds your fields and methods. A `JFrame` already knows `setTitle`, `setSize`, `setVisible`, and `add`. You never write those methods. You call them. One `FinancialTracker` object is one open window.

The `{` on that line opens the body. Every field and method below it, until the final `}` at the end of the file, belongs to this window.

Line 99, `serialVersionUID`, is a version number Swing wants on a window class. It does not store money and it does not change login. You can leave it alone.

The comment on lines 70–96 is for people. The program skips it. It tells you the three structures from class: ArrayList, HashMap, and Stack.

---

# Part 2: Colors, fonts, and the way numbers and dates are written

These lines are inside the class, outside every method. They are prepared once, and every method in the file can use the names.

```java
private static Color BG = new Color(232, 236, 242);
private static Color CARD = Color.WHITE;
private static Color INK = new Color(28, 32, 40);
private static Color MUTED = new Color(70, 80, 95);
private static final Color ACCENT = new Color(124, 92, 255);
private static final Color GREEN = new Color(52, 199, 89);
private static final Color RED = new Color(255, 99, 99);
private static final Color BLUE = new Color(120, 170, 255);
private static final Color SIDE = new Color(108, 78, 214);
private static final Color SIDE_ON = new Color(88, 62, 190);
private boolean darkMode = false;
private static Color BORDER = new Color(190, 198, 210);
```

## The words in front of the name

`private` means only `FinancialTracker.java` can use that name. Another class cannot write `FinancialTracker.BG`.

`static` means the color belongs to the class, and there is one shared `BG` even if you imagine two windows. This program opens one window, so you will not see a difference on screen. The word still matters: methods marked `static`, such as `main`, are allowed to mention `INK`. A static method has no window object yet.

`final` means the variable cannot be pointed at a different color later. `ACCENT`, `GREEN`, `RED`, `BLUE`, `SIDE`, and `SIDE_ON` are locked. `BG`, `CARD`, `INK`, `MUTED`, and `BORDER` are not `final`, so a later line is allowed to assign a new `Color` to those names. In this version the light colors stay as written here. `darkMode` starts as `false`, and `applyTheme` (line 2504) checks that flag when it paints text boxes.

## What the numbers mean

`new Color(232, 236, 242)` builds one color from red, green, and blue. Each number runs from 0 to 255.

- `232, 236, 242` is a light gray-blue. `BG` paints the area behind the pages (`contentPane.setBackground(BG)` in the constructor).
- `Color.WHITE` is a ready-made name for `255, 255, 255`. `CARD` is the white box behind a form or a card.
- `28, 32, 40` is near-black. `INK` is ordinary writing. `main` also tells Swing that labels and text boxes should use `INK`, so the look-and-feel does not replace your text with a faint color.
- `70, 80, 95` is a softer gray. `MUTED` is for hints and secondary labels, so they look quieter than a title.
- `124, 92, 255` is purple. `ACCENT` is used for the important number, such as the allowance on the dashboard.
- `GREEN` and `RED` mark a good amount and money spent. The spent card uses `RED`.
- `BLUE` is the extra chart color.
- `SIDE` (`108, 78, 214`) fills the sidebar. `SIDE_ON` (`88, 62, 190`) is the darker purple `paintSidebar` uses for the selected page button.
- `BORDER` is the light line around boxes.

A color object only describes a mix of red, green, and blue. It does not paint anything by itself. A later line such as `setBackground(SIDE)` is what puts the color on a panel.

## Fonts

```java
private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 20);
private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 13);
private static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 12);
private static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 13);
private static final Font BIG_FONT = new Font("SansSerif", Font.BOLD, 22);
```

`new Font` needs three facts: the family, the style, and the size in points.

`"SansSerif"` is a family Java promises on every computer. The school PCs may not have Helvetica installed, so the code does not ask for it. Java substitutes a real plain font that the computer does have.

`Font.BOLD` is thick. `Font.PLAIN` is regular. The last number is the size: 22 for the big money figures (`BIG_FONT`), 20 for page titles, 13 for labels and buttons, 12 for the small notes (`SMALL_FONT`).

These five objects are created when the class loads. Buttons later call `setFont(BOLD_FONT)` instead of building a new font on every button.

## How money and dates become text

```java
private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00",
        java.text.DecimalFormatSymbols.getInstance(Locale.US));
```

`MONEY` is not a color. It is a rule for printing a number. The pattern `#,##0.00` means "put a comma between thousands, and always show two decimal places." `Locale.US` means the comma is the thousands mark and the dot is the decimal mark.

`MONEY.format(1250.5)` produces the text `1,250.50`. The pattern does not include the peso sign. When a table row is built, the code adds it in front: `"₱" + MONEY.format(expense.amount)`.

```java
private static final DateTimeFormatter GOAL_DATE = DateTimeFormatter
        .ofPattern("MM/dd/uuuu", Locale.US)
        .withResolverStyle(java.time.format.ResolverStyle.STRICT);
private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter
        .ofPattern("MM/uuuu", Locale.US)
        .withResolverStyle(java.time.format.ResolverStyle.STRICT);
```

`GOAL_DATE` turns a day into `MM/DD/YYYY`, for example `11/24/2026`. `uuuu` is the year. `STRICT` refuses an impossible day such as February 31, so a bad deadline cannot sneak through as a different date.

`MONTH_FORMAT` turns a month into `MM/YYYY`, which is the text in the dashboard and report dropdowns.

## The starting categories

```java
public static final String[] DEFAULT_CATEGORIES = {
    "Food", "Transport", "School", "Load", "Other"
};
```

This is a fixed array of five names. `public` is deliberate: `LedgerData` reads this array when a new ledger is created and copies the names into its category `ArrayList`. A user can add more categories later. Those extra names live in the list, and this array stays the original five.

---

# Part 3: Connecting the window to the data

```java
private final LedgerData data = new LedgerData();
private final ArrayList<Expense> expenses = data.expenses;
private final ArrayList<Goal> goals = data.goals;
private final ArrayList<String> categories = data.categories;
private final Stack<Expense> deleted = data.deleted;
private final ArrayList<Expense> shownExpenses = new ArrayList<Expense>();
```

## When these lines run

They are field initializers. Java runs them at the moment `new FinancialTracker(username)` starts, before the first line inside the constructor. So the data box already exists before the file is loaded and before any button is built.

`new LedgerData()` makes one object. That object owns the real lists. Its constructor also copies `DEFAULT_CATEGORIES` into `categories`.

## Two names, one list

`expenses` and `data.expenses` are two names for the same `ArrayList` object.

Picture one notebook. `data` is holding the notebook. The line `expenses = data.expenses` writes a second label on that same notebook. It does not photocopy the pages.

So this line:

```java
expenses.add(expense);
```

adds the purchase to the list inside `LedgerData`. When `data.save(...)` later walks `data.expenses`, the new purchase is there, because it was never stored anywhere else.

`final` on `expenses` locks the name. The code cannot later do `expenses = new ArrayList<>()`. Adding, removing, and editing objects inside the list is still allowed. `final` protects the connection, and the list itself can still change.

The same connection is used for `goals`, `categories`, and `deleted`.

## `planned()` is a method, not a fifth list

```java
private HashMap<String, Double> planned() {
    return data.plan(YearMonth.now());
}
```

A call to `planned()` asks `LedgerData` for the budget map of the current month. The key is a category name. The value is the amount you planned. If that month has never had a plan, `LedgerData.plan` creates an empty `HashMap` and remembers it.

The window does not keep its own `HashMap` field. Each call looks the month up again. That is why a saved plan survives: it lives in `data.plans`, and `save` writes that map.

## The undo stack

`deleted` is a `Stack`. Delete does `deleted.push(expense)`. Undo does `deleted.pop()`.

Stack order is last in, first out. Delete "Lunch", then delete "Fare". The first Undo puts "Fare" back. The next Undo puts "Lunch" back. The same stack object is `data.deleted`, so the undo pile is saved in the file too (`writeExpenses(p, "undo", deleted)` inside `LedgerData`).

## The visible list is a different list

```java
private final ArrayList<Expense> shownExpenses = new ArrayList<Expense>();
```

This one is a new, empty list. It is not `data.expenses`.

`rebuildExpenseTable` (line 1608) does this:

1. Walk the real `expenses` list.
2. Keep only the rows in the chosen month that also match the search, the category filter, and the date range.
3. Bubble-sort that smaller list.
4. Clear `shownExpenses`, then add those same objects in the sorted order, and add matching rows to the `JTable`.

The table and `shownExpenses` stay in the same order. If you click row 0, the code does `shownExpenses.get(0)`. That object is one of the real expenses, but it might not be `expenses.get(0)`, because search and sort changed the order on screen.

`shownExpenses` is wiped and rebuilt often. Deleting from it would not be enough. Delete removes the object from `expenses`, which is the list that gets saved.

## The three small fields under the lists

```java
private String username = "";
private Path dataFile;
private boolean unsaved;
```

`username` starts as empty text. The constructor's first job is `this.username = username`, storing the name that login just accepted.

`dataFile` has no value until the constructor computes it. `dataPath` lower-cases the name, turns each byte into two hex characters, and builds `home\LedgerData\<those characters>.properties`. `student` and `Student` become the same file. The hex step also keeps a strange character in a name from becoming a dangerous path.

`unsaved` is a `boolean`, so it starts as `false`. `changed()` sets it to `true`, then calls `saveData()`. A successful save sets it back to `false`. If you click the X while a save has failed, `unsaved` is still true and `saveData()` fails again, so the window stays open instead of throwing the records away.

---

# Part 4: Declaring the pieces of the window

```java
private JPanel contentPane;
private JPanel sidebar;
private JTextField descriptionField;
private JTextField amountField;
private JTable expenseTable;
private JButton addExpenseButton;
```

## Declaring is not building

These lines tell Java the names and the types. They do not create the buttons. After this section runs, `descriptionField` is `null`, which means "no object yet."

The real `new JTextField()` happens later, inside `createExpenseForm` and the other `create...` methods, which the constructor calls from `createContents()` (line 328).

If `addExpense` called `descriptionField.getText()` before that build step, the program would crash with `NullPointerException`. That is why several refresh methods start with a guard such as `if (expenseTableModel == null) return;`. During startup, a refresh can run before every page has been built.

## Why they are fields, not local variables

A local variable inside `createExpenseForm` would disappear when that method returns. `addExpense` is a different method, called later, when the user clicks. It can see `descriptionField` only because the field belongs to the object and stays alive for the whole time the window is open.

Every method in this class shares these fields. That is also why a field used on one page must be given a clear name (`goalNameField`, not another `descriptionField`).

## The shell of the window

| Field | Role |
| --- | --- |
| `contentPane` | The panel that fills the window. North is the header. Center is the sidebar plus the page. |
| `sidebar` | The purple panel of page buttons. |
| `contentArea` | The panel that holds the five pages. |
| `cardLayout` | The layout that stacks those five pages and shows one. `showPage("Expenses")` brings the card that was stored under the name `"Expenses"`. The name must match the string used in `contentArea.add(..., "Expenses")`. |
| `navButtons` | The five sidebar buttons, so `showPage` can recolor the one that matches the open page. |
| `pageTitleLabel`, `pageSubtitleLabel` | The title at the top. `pageBlurb` supplies the short sentence under it. |

## Dashboard fields

`allowanceValueLabel`, `spentValueLabel`, `leftValueLabel`, and `perDayValueLabel` are the four big numbers. `refreshDashboard` replaces their text. It does not create new labels.

`summaryArea` is the paragraph under the cards. `dashboardMonthBox` is the month dropdown. `dashboardMonth` is the month the cards are adding up, which can be an older month than today. `fillingDashboardMonths` is a guard: while the program is filling that dropdown, the dropdown fires a "the user changed it" event. The flag tells the listener to ignore those events until the fill is finished.

## Expense fields

`descriptionField`, `amountField`, `categoryBox`, and `dateSpinner` are the form. `addExpenseButton` adds a row. While you are editing, that button is hidden and `saveExpenseButton` plus `cancelExpenseButton` are shown.

`searchField` and `sortBox` do not change the saved list. They change what `rebuildExpenseTable` copies into `shownExpenses`.

`expenseTable` is the grid you see. `expenseTableModel` is the data behind that grid: rows and columns of text. The model is not the `ArrayList`. The list is the record. The model is the text on screen.

`editingExpense` remembers the actual `Expense` object being edited. It does not remember a row number, because a sort would make that number point at a different purchase. `null` means you are not editing.

`expenseMonth`, `expenseFrom`, `expenseTo`, and `expenseCategory` are the filters. A `null` filter means "do not restrict that part." `dateFloor` is the earliest day the spinner allows. It starts as today, and a 30-second timer moves it forward if the app stays open past midnight.

## Goal fields

`goalNameField`, `goalTargetField`, and `goalDeadlineField` are the form. `editingGoal` is an `int`, not an object. `-1` means "not editing." A number `0` or more is the position in the `goals` list. This is a different choice from `editingExpense`, which keeps the object itself.

`depositField` is the amount you add into the selected goal.

## Allowance fields

`allowanceField` is this month's allowance. `planCategoryBox` and `planAmountField` set one category's plan. `planTable` shows planned, spent, and what is left. `totalBudgetValueLabel` is the sum of the plans. The plans themselves are not stored in these fields. They are stored in the `HashMap` that `planned()` returns.

## Report fields

`categoryBarsPanel` and `dailyBarsPanel` are empty panels at first. `rebuildReports` removes old bars and adds a `ReportBar` for each category and each day. `reportMonthBox` picks the month. `fillingReportMonths` is the same kind of guard as `fillingDashboardMonths`.

---

# Part 5: How those empty names become a running window

Declaring the fields does nothing visible. `main` is what starts the program.

```java
public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
        public void run() {
            // set the look, then:
            LoginDialog login = new LoginDialog();
            login.setVisible(true);
            if (login.isLoggedIn()) {
                openTracker(login.getUsername());
            } else {
                System.exit(0);
            }
        }
    });
}
```

`main` is `static` because Java calls it before any window exists. There is no `FinancialTracker` object yet, so `main` cannot use `descriptionField`. It can use the static colors, which is why `UIManager.put("Label.foreground", INK)` is legal here.

`EventQueue.invokeLater` means "run this on Swing's thread." Swing is not safe to build from a random thread. The `Runnable` is a small object whose only job is the `run` method. Java calls `run` when the window thread is ready.

Inside `run`:

1. The cross-platform look is selected so the purple, white, and ink colors are not painted over.
2. `new LoginDialog()` builds the login popup. `setVisible(true)` shows it. The dialog is modal, so this line waits until the user logs in or closes it.
3. A successful login calls `openTracker`, which does `new FinancialTracker(username).setVisible(true)`.
4. Closing login without success runs `System.exit(0)`, which ends the program. No ledger file is opened.

## What the constructor does with the fields

`public FinancialTracker(String username)` is the method that runs for `new FinancialTracker(...)`.

In order:

1. Store the username.
2. Compute `dataFile`.
3. `data.load(dataFile, username)` fills the lists that `expenses` and `goals` already point at. A missing file leaves the lists empty. An older Ledger file asks before import. Cancel throws, and `openTracker` shows the error. The original file is left as it was.
4. Pick the dashboard month: this month if it has records, otherwise the latest month that has records.
5. `setTitle`, the size `1000` by `720`, and the close rule. The X button does not kill the program immediately. `DO_NOTHING_ON_CLOSE` plus the window listener means: save first, and call `dispose()` only if the save works or there was nothing unsaved.
6. Create `contentPane`, paint it `BG`, and call `createContents()`. That is the moment the `null` fields become real buttons, tables, and labels.
7. Start a timer every 30 seconds. If the date or the month has changed, refresh the screens.
8. Put the saved allowance into `allowanceField`, build the expense table, paint colors, and call `refreshAll()` so the cards match the loaded file.

After step 8 the window is shown, because `openTracker` calls `setVisible(true)` on the object the constructor just finished.

---

# Part 6: One expense, using the pieces from Parts 3 and 4

You type `Lunch`, `150`, category `Food`, and today's date, then click **Add expense**. The button was wired in `createExpenseForm` to call `addExpense` (line 1383).

`addExpense` reads the fields:

1. `descriptionField.getText().trim()` removes spaces at the ends. An empty description stops here with a popup, and nothing is saved.
2. `readAmount` turns `150` or `1,250.50` into a `double`. Zero or text that is not a number stops here.
3. `categoryBox.getSelectedItem()` is the chosen category. A missing choice stops here.
4. `validatedExpenseDate()` reads the spinner and allows only a day in the current month. A bad date returns `null`, and the method stops.

If all four checks pass:

```java
Expense expense = new Expense(date, description, category, amount);
expenses.add(expense);
```

`new Expense` makes one record. `expenses.add` puts that object into the shared `ArrayList` inside `LedgerData`.

Then the form is cleared, `rebuildExpenseTable` fills `shownExpenses` and the `JTable`, and `changed()` runs:

```java
private boolean changed() {
    unsaved = true;
    boolean saved = saveData();
    refreshAll();
    return saved;
}
```

`saveData` calls `data.save(dataFile)`, which writes every list, including this new expense, into the `.properties` file. `refreshAll` redraws the dashboard cards, the goal table, the plan table, and the report bars, because Lunch changed "money left" on every page that shows a total.

Delete uses the same path with one extra structure. The selected row is `shownExpenses.get(row)`, that object is removed from `expenses`, then `deleted.push(expense)` puts it on the stack, then `changed()` saves. Undo does `deleted.pop()`, adds that object back to `expenses`, and saves again.

That is the whole connection: the fields in Part 4 collect the click, the lists in Part 3 hold the record, and `changed()` writes the file and repaints every page.
