import dao.data;
import model.java.Book;

/**
 * Test3_BookCRUD.java
 * ─────────────────────────────────────────────────────────────────────────────
 * Tests the full Create / Read / Update / Delete lifecycle for books.
 * All books created here are deleted at the end, so the DB stays clean.
 * ─────────────────────────────────────────────────────────────────────────────
 * Run standalone:
 *   javac -cp "lib\*;build\classes" test\Test3_BookCRUD.java -d test\out
 *   java  -cp "lib\*;build\classes;test\out" Test3_BookCRUD
 */
public class Test3_BookCRUD {

    static int passed = 0, failed = 0, total = 0;

    public static void main(String[] args) {
        printSection("TEST 3 - Book CRUD (Add / Read / Update / Delete)");
        run();
        printSummary();
    }

    public static int[] run() {
        passed = 0; failed = 0; total = 0;

        // Short ISBN that fits VARCHAR(20)
        String isbn = "TS-" + (System.currentTimeMillis() % 100000);

        // 3-A: addBook() must return a valid auto-generated ID
        int bookId = data.addBook("Test Book", "Test Author", isbn,
                "Testing", "A book added by the test suite.", 3, 200, "Test Publisher", 2024);
        assertTrue("3-A: addBook() returns a valid ID (> 0)", bookId > 0);

        // 3-B: getBookById() retrieves the just-inserted book
        Book b = data.getBookById(bookId);
        assertNotNull("3-B: getBookById() returns the book", b);

        // 3-C: Title stored matches what was inserted
        if (b != null)
            assertEquals("3-C: Book title matches inserted value", "Test Book", b.getTitle());

        // 3-D: On insert, availableCopies must equal totalCopies
        if (b != null)
            assertEquals("3-D: availableCopies == totalCopies on insert", 3, b.getAvailableCopies());

        // 3-E: isAvailable() must be true for a book that has copies
        if (b != null)
            assertTrue("3-E: isAvailable() is true for book with copies", b.isAvailable());

        // 3-F: updateBook() must return true on success
        if (bookId > 0) {
            boolean updated = data.updateBook(bookId, "Updated Title", "Updated Author",
                    isbn, "Testing", "Updated description.", 5, 250, "Updated Publisher", 2025);
            assertTrue("3-F: updateBook() returns true", updated);

            // 3-G: The new title must be persisted in the DB
            Book ub = data.getBookById(bookId);
            if (ub != null)
                assertEquals("3-G: Updated title is persisted in DB", "Updated Title", ub.getTitle());
        }

        // 3-H: deleteBook() must return true
        if (bookId > 0) {
            boolean deleted = data.deleteBook(bookId);
            assertTrue("3-H: deleteBook() returns true", deleted);

            // 3-I: Fetching a deleted book must return null
            Book del = data.getBookById(bookId);
            assertNull("3-I: getBookById() returns null after deletion", del);
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
    static void assertNull(String label, Object obj) {
        total++;
        if (obj == null) pass(label); else fail(label, "expected NULL but was [" + obj + "]");
    }
    static void assertNotNull(String label, Object obj) {
        total++;
        if (obj != null) pass(label); else fail(label, "expected non-null but was NULL");
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
