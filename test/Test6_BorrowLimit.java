import dao.data;
import utils.PasswordUtils;

/**
 * Test6_BorrowLimit.java
 * ─────────────────────────────────────────────────────────────────────────────
 * Tests that the system enforces a maximum borrow limit per member:
 *   • Borrowing up to BORROW_LIMIT books all succeed
 *   • The (BORROW_LIMIT + 1)th borrow is rejected with return code -2
 * All data created here is cleaned up at the end.
 * ─────────────────────────────────────────────────────────────────────────────
 * Run standalone:
 *   javac -cp "lib\*;build\classes" test\Test6_BorrowLimit.java -d test\out
 *   java  -cp "lib\*;build\classes;test\out" Test6_BorrowLimit
 */
public class Test6_BorrowLimit {

    static int passed = 0, failed = 0, total = 0;

    public static void main(String[] args) {
        printSection("TEST 6 - Borrow Limit Enforcement (max " + data.BORROW_LIMIT + " books)");
        run();
        printSummary();
    }

    public static int[] run() {
        passed = 0; failed = 0; total = 0;

        // ── Setup: fresh member for this test ──────────────────────────────────
        String email  = "limit_" + System.currentTimeMillis() + "@lms.test";
        String hash   = PasswordUtils.hash("Limit@999");
        int memberId  = data.registerMember("Limit", "Tester", email, hash,
                "9900000001", "LIM" + System.currentTimeMillis(), "student");
        assertTrue("6-setup: Member created for limit test", memberId > 0);

        // ── Setup: create BORROW_LIMIT + 1 distinct books ─────────────────────
        int total_books = data.BORROW_LIMIT + 1;
        int[] bookIds   = new int[total_books];
        long ts = System.currentTimeMillis() % 10000;
        for (int i = 0; i < total_books; i++) {
            bookIds[i] = data.addBook("LimitBook_" + i, "Author",
                    "LM-" + ts + "-" + i,
                    "Misc", "Limit test book " + i, 1, 100, "Pub", 2023);
        }

        // 6-A through 6-E: The first BORROW_LIMIT borrows should all succeed
        int[] borrowIds = new int[data.BORROW_LIMIT];
        for (int i = 0; i < data.BORROW_LIMIT; i++) {
            borrowIds[i] = data.borrowBook(memberId, bookIds[i]);
            assertTrue("6-" + (char)('A' + i) + ": Borrow #" + (i + 1) + " within limit succeeds",
                    borrowIds[i] > 0);
        }

        // 6-F: The (BORROW_LIMIT + 1)th borrow must be rejected with -2
        int overLimit = data.borrowBook(memberId, bookIds[data.BORROW_LIMIT]);
        assertEquals("6-F: " + (data.BORROW_LIMIT + 1) +
                        "th borrow returns -2 (limit exceeded)", -2, overLimit);

        // ── Cleanup: return all books, then delete them ───────────────────────
        for (int i = 0; i < data.BORROW_LIMIT; i++) {
            data.returnBook(borrowIds[i], bookIds[i]);
        }
        for (int bookId : bookIds) {
            data.deleteBook(bookId);
        }

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
