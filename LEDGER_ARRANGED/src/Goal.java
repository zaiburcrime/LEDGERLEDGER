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
