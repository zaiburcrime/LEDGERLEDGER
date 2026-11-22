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
