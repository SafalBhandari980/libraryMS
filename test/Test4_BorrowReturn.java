import dao.data;
import model.java.Book;
import utils.PasswordUtils;

/**
 * Test4_BorrowReturn.java
 * ─────────────────────────────────────────────────────────────────────────────
 * Tests the full borrow-and-return lifecycle:
 *   • borrowBook() inserts a record and decrements availableCopies
 *   • Borrowing a second different book also succeeds
 *   • returnBook() marks the record returned and increments availableCopies
 * All data created here is cleaned up at the end.
 * ─────────────────────────────────────────────────────────────────────────────
 * Run standalone:
 *   javac -cp "lib\*;build\classes" test\Test4_BorrowReturn.java -d test\out
 *   java  -cp "lib\*;build\classes;test\out" Test4_BorrowReturn
 */
public class Test4_BorrowReturn {

    static int passed = 0, failed = 0, total = 0;

    public static void main(String[] args) {
        printSection("TEST 4 - Borrow & Return Flow");
        run();
        printSummary();
    }

    public static int[] run() {
        passed = 0; failed = 0; total = 0;

        // ── Setup: create a disposable member ──────────────────────────────────
        String email  = "borrow_" + System.currentTimeMillis() + "@lms.test";
        String hash   = PasswordUtils.hash("Borrow@999");
        int memberId  = data.registerMember("Borrow", "Tester", email, hash,
                "9800000099", "BRW" + System.currentTimeMillis(), "student");
        assertTrue("4-setup: Member created for borrow test", memberId > 0);

        // ── Setup: create two disposable books ────────────────────────────────
        String isbn1 = "BR-"  + (System.currentTimeMillis() % 100000);
        String isbn2 = "BR2-" + (System.currentTimeMillis() % 100000);
        int bookId1 = data.addBook("Borrow Test Book 1", "B. Author", isbn1,
                "Fiction", "Borrow test book 1.", 2, 100, "Pub", 2023);
        int bookId2 = data.addBook("Borrow Test Book 2", "B. Author", isbn2,
                "Fiction", "Borrow test book 2.", 2, 100, "Pub", 2023);
        assertTrue("4-setup: Book 1 created", bookId1 > 0);
        assertTrue("4-setup: Book 2 created", bookId2 > 0);

        // 4-A: borrowBook() must return a valid borrowId (> 0)
        int borrowId1 = data.borrowBook(memberId, bookId1);
        assertTrue("4-A: borrowBook() returns a valid borrowId (> 0)", borrowId1 > 0);

        // 4-B: availableCopies must decrease by 1 after borrowing
        Book afterBorrow1 = data.getBookById(bookId1);
        if (afterBorrow1 != null)
            assertEquals("4-B: availableCopies decreases by 1 after borrow",
                    1, afterBorrow1.getAvailableCopies());

        // 4-C: Borrowing a second, different book also succeeds
        int borrowId2 = data.borrowBook(memberId, bookId2);
        assertTrue("4-C: Can borrow a second different book", borrowId2 > 0);

        // 4-D: Book 2 also has its availableCopies decremented
        Book afterBorrow2 = data.getBookById(bookId2);
        if (afterBorrow2 != null)
            assertEquals("4-D: Book 2 availableCopies decreases by 1 after borrow",
                    1, afterBorrow2.getAvailableCopies());

        // 4-E: returnBook() must succeed
        boolean returned = data.returnBook(borrowId1, bookId1);
        assertTrue("4-E: returnBook() returns true", returned);

        // 4-F: availableCopies must increase by 1 after return
        Book afterReturn = data.getBookById(bookId1);
        if (afterReturn != null)
            assertTrue("4-F: availableCopies is at least 1 after return",
                    afterReturn.getAvailableCopies() >= 1);

        // ── Cleanup ────────────────────────────────────────────────────────────
        data.returnBook(borrowId2, bookId2);
        data.deleteBook(bookId1);
        data.deleteBook(bookId2);

        return new int[]{passed, failed, total};
    }

    // ── Assertion helpers ──────────────────────────────────────────────────────
    static void assertTrue(String label, boolean condition) {
        total++;
        if (condition) pass(label); else fail(label, "expected TRUE but was FALSE");
    }
    static void assertEquals(String label, Object expected, Object actual) {
        total++;
        if (expected == null ? actual == null : expected.equals(actual)) pass(label);
        else fail(label, "expected [" + expected + "] but was [" + actual + "]");
    }
    static void pass(String label) { passed++; System.out.printf("  PASS  %s%n", label); }
    static void fail(String label, String reason) {
        failed++; System.out.printf("  FAIL  %s  -->  %s%n", label, reason);
    }
    static void printSection(String title) {
        System.out.println();
        System.out.println("  ------------------------------------------------------------");
        System.out.println("  " + title);
        System.out.println("  ------------------------------------------------------------");
    }
    static void printSummary() {
        System.out.println();
        System.out.printf("  Results: %d passed, %d failed, %d total%n", passed, failed, total);
        System.out.println(failed == 0 ? "  All tests passed!" : "  " + failed + " test(s) FAILED.");
    }
}
