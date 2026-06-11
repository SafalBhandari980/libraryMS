import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * FineCalculator
 * Calculates overdue fines for the LibraryMs borrowing system.
 *
 * Fine rule: Rs 10 per day overdue.
 * Due date  = borrowedDate + loanPeriodDays (default: 14 days).
 */
public class FineCalculator {

    // ---------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------
    public static final int   FINE_PER_DAY_RS  = 10;
    public static final int   DEFAULT_LOAN_DAYS = 14;
    public static final String DATE_FORMAT       = "yyyy-MM-dd"; // adjust to match your DB format

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(DATE_FORMAT);

    // ---------------------------------------------------------------
    // Core calculation
    // ---------------------------------------------------------------

    /**
     * Calculates the fine for a single borrowing record.
     *
     * @param borrowedDateStr  Date the book was borrowed (e.g. "2025-05-01")
     * @param returnedDateStr  Date the book was actually returned, or null/""
     *                         if the book has not been returned yet (uses today).
     * @param loanPeriodDays   Number of days the member is allowed to keep the book.
     * @return                 FineResult containing days overdue and fine amount.
     */
    public static FineResult calculate(String borrowedDateStr,
                                       String returnedDateStr,
                                       int    loanPeriodDays) {

        LocalDate borrowedDate = LocalDate.parse(borrowedDateStr, FORMATTER);
        LocalDate dueDate      = borrowedDate.plusDays(loanPeriodDays);

        // If no return date is provided, compare against today (still active)
        LocalDate compareDate = (returnedDateStr == null || returnedDateStr.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(returnedDateStr, FORMATTER);

        long daysOverdue = ChronoUnit.DAYS.between(dueDate, compareDate);
        long fineAmount  = (daysOverdue > 0) ? daysOverdue * FINE_PER_DAY_RS : 0;

        return new FineResult(
                borrowedDate,
                dueDate,
                compareDate,
                Math.max(daysOverdue, 0),
                fineAmount,
                returnedDateStr == null || returnedDateStr.isBlank()   // isActive
        );
    }

    /**
     * Convenience overload using the default loan period (14 days).
     */
    public static FineResult calculate(String borrowedDateStr,
                                       String returnedDateStr) {
        return calculate(borrowedDateStr, returnedDateStr, DEFAULT_LOAN_DAYS);
    }

    // ---------------------------------------------------------------
    // Result container
    // ---------------------------------------------------------------

    /**
     * Holds the outcome of a fine calculation.
     */
    public static class FineResult {

        public final LocalDate borrowedDate;
        public final LocalDate dueDate;
        public final LocalDate compareDate;   // returned date OR today
        public final long      daysOverdue;
        public final long      fineAmountRs;
        public final boolean   isActive;      // true = book not yet returned

        public FineResult(LocalDate borrowedDate,
                          LocalDate dueDate,
                          LocalDate compareDate,
                          long      daysOverdue,
                          long      fineAmountRs,
                          boolean   isActive) {

            this.borrowedDate  = borrowedDate;
            this.dueDate       = dueDate;
            this.compareDate   = compareDate;
            this.daysOverdue   = daysOverdue;
            this.fineAmountRs  = fineAmountRs;
            this.isActive      = isActive;
        }

        /** Human-readable fine string for display in UI labels. */
        public String fineDisplayText() {
            if (fineAmountRs == 0) return "Rs 0";
            return "Rs " + fineAmountRs;
        }

        /** Status label text matching the UI's ActiveStatus / OverdueStatus / ReturnedStatus labels. */
        public String statusText() {
            if (isActive && daysOverdue > 0) return "Overdue";
            if (isActive)                    return "Active";
            return "Returned";
        }

        @Override
        public String toString() {
            return String.format(
                "Borrowed: %s | Due: %s | %s: %s | Days overdue: %d | Fine: %s | Status: %s",
                borrowedDate, dueDate,
                isActive ? "Today" : "Returned", compareDate,
                daysOverdue, fineDisplayText(), statusText()
            );
        }
    }
}
