# Slice.java

This tab explains only `src/Slice.java`.

In the Explorer, open the `explanations` folder and click another name to open that class in its own tab. Read `00 How the program works.md` first if you have not yet.

## What this class is

`Slice` is one piece of a report. It is only a name and a total. Example: name `Food`, amount `250`, or name `11/24/2026`, amount `80`.

## How it works

The Reports page walks the expenses, adds them into slices, sorts the category slices, then draws one `ReportBar` for each slice. `Slice` itself does not draw and does not sort.

## The code

```java
/**
 * One slice of a report (a name and a total).
 *
 * DATA STRUCTURE: report code builds an ArrayList of Slice,
 * one slice per category or per day, then draws a bar for each.
 */
public class Slice {
    String name;
    double amount;

    Slice(String name, double amount) {
        this.name = name;
        this.amount = amount;
    }
}
```

## Walk through every line

| Line | What it means |
| --- | --- |
| 1–6 | Comment. Reports build a list of slices, then one bar per slice. |
| 7 | Start the class. |
| 8 | The label on the bar. |
| 9 | How much that bar represents. |
| 11–14 | Constructor. Store the name and the amount that were passed in. |
| 15 | End of the class. |
