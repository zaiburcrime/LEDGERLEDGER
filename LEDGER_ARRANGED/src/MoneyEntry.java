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
