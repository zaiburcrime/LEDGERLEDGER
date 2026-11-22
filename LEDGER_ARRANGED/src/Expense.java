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
