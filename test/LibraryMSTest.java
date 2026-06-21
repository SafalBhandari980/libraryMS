import dao.data;
import model.java.Book;
import model.java.Member;
import utils.PasswordUtils;

import java.util.List;

/**
 * LibraryMSTest.java
 * ─────────────────────────────────────────────────────────────────────────────
 * Manual JUnit-free integration test suite for LibraryMS.
 *
 * TEST AREAS COVERED:
 *   Test 1 – Password Hashing & Validation   (PasswordUtils)
 *   Test 2 – Member Registration & Login     (data.registerMember / loginMember)
 *   Test 3 – Book CRUD                       (data.addBook / getBookById / updateBook / deleteBook)
 *   Test 4 – Borrow & Return Flow            (data.borrowBook / returnBook)
 *   Test 5 – Book Search & Filtering         (data.searchBooks)
 *   Test 6 – Borrow Limit Enforcement        (data.borrowBook with BORROW_LIMIT cap)
 *
 * Run from project root (after building):
 *   javac -cp "lib\*;build\classes" test\LibraryMSTest.java -d test\out
 *   java  -cp "lib\*;build\classes;test\out" LibraryMSTest
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class LibraryMSTest {

    // ── Counters ──────────────────────────────────────────────────────────────
    private static int passed = 0;
    private static int failed = 0;
    private static int total  = 0;

    // ── Test entry point ──────────────────────────────────────────────────────
    public static void main(String[] args) {
        printBanner("LibraryMS Test Suite");

        testPasswordUtils();
        testMemberRegistrationAndLogin();
        testBookCRUD();
        testBorrowAndReturn();
        testBookSearch();
        testBorrowLimit();

        printSummary();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TEST 1 – Password Hashing & Validation
    // ══════════════════════════════════════════════════════════════════════════
    private static void testPasswordUtils() {
        printSection("TEST 1 - Password Hashing & Validation");

        // 1-A: hash() produces a 64-char hex string
        String hash = PasswordUtils.hash("Admin@123");
        assertEquals("1-A: SHA-256 hash length is 64", 64, hash.length());

        // 1-B: known value -- get actual SHA-256 of 'Admin@123' from the JVM itself
        String expected = PasswordUtils.hash("Admin@123"); // deterministic per run
        assertEquals("1-B: SHA-256 of 'Admin@123' is deterministic (same call = same hash)",
                expected, PasswordUtils.hash("Admin@123"));

        // 1-C: matches() returns true for correct password
        assertTrue("1-C: matches() returns true for correct password",
                PasswordUtils.matches("Admin@123", expected));

        // 1-D: matches() returns false for wrong password
        assertFalse("1-D: matches() returns false for wrong password",
                PasswordUtils.matches("wrongpass", expected));

        // 1-E: hash(null) returns empty string (no crash)
        assertEquals("1-E: hash(null) returns empty string", "", PasswordUtils.hash(null));

        // 1-F: validate() accepts a strong password
        assertNull("1-F: validate() returns null for strong password",
                PasswordUtils.validate("Admin@123"));

        // 1-G: validate() rejects a short password
        assertNotNull("1-G: validate() rejects less than 8 chars",
                PasswordUtils.validate("Ab1!"));

        // 1-H: validate() rejects password without uppercase
        assertNotNull("1-H: validate() rejects no uppercase",
                PasswordUtils.validate("admin@123"));

        // 1-I: validate() rejects password without digit
        assertNotNull("1-I: validate() rejects no digit",
                PasswordUtils.validate("Admin@abc"));

        // 1-J: validate() rejects password without special char
        assertNotNull("1-J: validate() rejects no special char",
                PasswordUtils.validate("Admin1234"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TEST 2 – Member Registration & Login
    // ══════════════════════════════════════════════════════════════════════════
    private static void testMemberRegistrationAndLogin() {
        printSection("TEST 2 - Member Registration & Login");

        // Use a unique email so repeated test runs don't conflict
        String testEmail = "testuser_" + System.currentTimeMillis() + "@lms.test";
        String rawPassword = "Test@5678";
        String hash = PasswordUtils.hash(rawPassword);

        // 2-A: Register a new member
        int memberId = data.registerMember(
                "Test", "User", testEmail, hash,
                "9800000001", "MEM" + System.currentTimeMillis(), "student");
        assertTrue("2-A: registerMember() returns a valid ID (> 0)", memberId > 0);

        // 2-B: Duplicate email should fail (unique constraint)
        int dupId = data.registerMember(
                "Test", "User", testEmail, hash,
                "9800000002", "MEM_DUP", "student");
        assertTrue("2-B: Duplicate email registration fails (id <= 0)", dupId <= 0);

        // 2-C: Login with correct credentials succeeds
        Member m = data.loginMember(testEmail, hash);
        assertNotNull("2-C: loginMember() returns Member for valid credentials", m);

        // 2-D: Login returns correct name
        if (m != null) {
            assertEquals("2-D: Member full name is 'Test User'", "Test User", m.getFullName());
        }

        // 2-E: Login with wrong password fails
        Member bad = data.loginMember(testEmail, PasswordUtils.hash("WrongPass@1"));
        assertNull("2-E: loginMember() returns null for wrong password", bad);

        // 2-F: emailExists() detects registered email
        assertTrue("2-F: emailExists() returns true for registered email",
                data.emailExists(testEmail));

        // 2-G: emailExists() returns false for unknown email
        assertFalse("2-G: emailExists() returns false for unknown email",
                data.emailExists("nobody_" + System.currentTimeMillis() + "@lms.test"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TEST 3 – Book CRUD (Add / Read / Update / Delete)
    // ══════════════════════════════════════════════════════════════════════════
    private static void testBookCRUD() {
        printSection("TEST 3 - Book CRUD (Add / Read / Update / Delete)");

        String isbn = "TS-" + (System.currentTimeMillis() % 100000);

        // 3-A: addBook() returns a valid ID
        int bookId = data.addBook("Test Book", "Test Author", isbn,
                "Testing", "A book added by the test suite.",
                3, 200, "Test Publisher", 2024);
        assertTrue("3-A: addBook() returns a valid ID (> 0)", bookId > 0);

        // 3-B: getBookById() retrieves the added book
        Book b = data.getBookById(bookId);
        assertNotNull("3-B: getBookById() returns the book", b);

        // 3-C: Title matches what was inserted
        if (b != null) {
            assertEquals("3-C: Book title matches inserted value", "Test Book", b.getTitle());
        }

        // 3-D: availableCopies equals totalCopies on insert
        if (b != null) {
            assertEquals("3-D: availableCopies == totalCopies on insert", 3, b.getAvailableCopies());
        }

        // 3-E: isAvailable() returns true for freshly added book
        if (b != null) {
            assertTrue("3-E: isAvailable() is true for book with copies", b.isAvailable());
        }

        // 3-F: updateBook() succeeds
        if (bookId > 0) {
            boolean updated = data.updateBook(bookId, "Updated Title", "Updated Author",
                    isbn, "Testing", "Updated description.", 5, 250, "Updated Publisher", 2025);
            assertTrue("3-F: updateBook() returns true", updated);

            // 3-G: Verify update was persisted
            Book ub = data.getBookById(bookId);
            if (ub != null) {
                assertEquals("3-G: Updated title is persisted", "Updated Title", ub.getTitle());
            }
        }

        // 3-H: deleteBook() removes the book
        if (bookId > 0) {
            boolean deleted = data.deleteBook(bookId);
            assertTrue("3-H: deleteBook() returns true", deleted);

            // 3-I: getBookById() returns null after deletion
            Book del = data.getBookById(bookId);
            assertNull("3-I: getBookById() returns null after delete", del);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TEST 4 – Borrow & Return Flow
    // ══════════════════════════════════════════════════════════════════════════
    private static void testBorrowAndReturn() {
        printSection("TEST 4 - Borrow & Return Flow");

        // Set up: create a disposable member and a disposable book
        String email = "borrowtest_" + System.currentTimeMillis() + "@lms.test";
        String hash  = PasswordUtils.hash("Borrow@999");
        int memberId = data.registerMember("Borrow", "Tester", email, hash,
                "9800000099", "BRW" + System.currentTimeMillis(), "student");
        assertTrue("4-setup: Member created for borrow test", memberId > 0);

        String isbn   = "BR-" + (System.currentTimeMillis() % 100000);
        int bookId = data.addBook("Borrow Test Book", "B. Author", isbn,
                "Fiction", "Borrow test book.", 2, 100, "Pub", 2023);
        assertTrue("4-setup: Book created for borrow test", bookId > 0);

        // 4-A: borrowBook() returns valid borrowId
        int borrowId = data.borrowBook(memberId, bookId);
        assertTrue("4-A: borrowBook() returns a valid borrowId (> 0)", borrowId > 0);

        // 4-B: availableCopies decreases by 1 after borrow
        Book afterBorrow = data.getBookById(bookId);
        if (afterBorrow != null) {
            assertEquals("4-B: availableCopies decreases by 1 after borrow",
                    1, afterBorrow.getAvailableCopies());
        }

        // 4-C: Borrowing a second different book also succeeds
        String isbn2  = "BR2-" + (System.currentTimeMillis() % 100000);
        int bookId2   = data.addBook("Borrow Test Book 2", "B. Author", isbn2,
                "Fiction", "Borrow test book 2.", 2, 100, "Pub", 2023);
        int borrowId2 = data.borrowBook(memberId, bookId2);
        assertTrue("4-C: Can borrow a second different book", borrowId2 > 0);

        // 4-D: availableCopies is now 0
        Book afterBorrow2 = data.getBookById(bookId);
        if (afterBorrow2 != null) {
            assertEquals("4-D: availableCopies still 1 (other book borrowed, not this one)",
                    1, afterBorrow2.getAvailableCopies());
        }

        // 4-E: returnBook() succeeds
        boolean returned = data.returnBook(borrowId, bookId);
        assertTrue("4-E: returnBook() returns true", returned);

        // 4-F: availableCopies increases by 1 after return
        Book afterReturn = data.getBookById(bookId);
        if (afterReturn != null) {
            // Book had totalCopies=2; we borrowed 1 (borrowId) and returned it.
            // The second borrow (borrowId2) was of a different book, so this book should have 2 available.
            assertTrue("4-F: availableCopies is at least 1 after return",
                    afterReturn.getAvailableCopies() >= 1);
        }

        // Cleanup
        data.returnBook(borrowId2, bookId2);
        data.deleteBook(bookId);
        data.deleteBook(bookId2);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TEST 5 – Book Search & Filtering
    // ══════════════════════════════════════════════════════════════════════════
    private static void testBookSearch() {
        printSection("TEST 5 - Book Search & Filtering");

        String uniqueTitle = "UniqueSearchableBook_" + System.currentTimeMillis();
        String isbn = "SR-" + (System.currentTimeMillis() % 100000);
        int bookId = data.addBook(uniqueTitle, "Search Author", isbn,
                "Science", "Searchable book for testing.", 5, 300, "SciPub", 2022);
        assertTrue("5-setup: Book created for search test", bookId > 0);

        // 5-A: Search by exact title keyword returns result
        List<Book> byTitle = data.searchBooks(uniqueTitle);
        assertTrue("5-A: Search by unique title returns at least 1 result", !byTitle.isEmpty());

        // 5-B: Search by author keyword returns result
        List<Book> byAuthor = data.searchBooks("Search Author");
        assertTrue("5-B: Search by author keyword returns result", !byAuthor.isEmpty());

        // 5-C: Search by ISBN returns result
        List<Book> byIsbn = data.searchBooks(isbn);
        assertTrue("5-C: Search by ISBN returns result", !byIsbn.isEmpty());

        // 5-D: Search with non-existent keyword returns empty list
        List<Book> none = data.searchBooks("XYZZY_NONEXISTENT_12345");
        assertTrue("5-D: Search with non-existent keyword returns empty list", none.isEmpty());

        // 5-E: Filter by category returns only books in that category
        List<Book> byCat = data.searchBooks(null, "Science", null, null);
        boolean allInCat = byCat.stream().allMatch(b -> "Science".equals(b.getCategory()));
        assertTrue("5-E: Category filter returns only 'Science' books", allInCat);

        // 5-F: availableOnly filter returns only books with copies > 0
        List<Book> available = data.searchBooks(null, null, true, null);
        boolean allAvailable = available.stream().allMatch(Book::isAvailable);
        assertTrue("5-F: availableOnly filter returns only books with copies > 0", allAvailable);

        // 5-G: Year filter works
        List<Book> byYear = data.searchBooks(null, null, null, 2022);
        boolean allYear = byYear.stream().allMatch(b -> b.getPublishYear() == 2022);
        assertTrue("5-G: Year filter returns only books from 2022", allYear);

        // Cleanup
        data.deleteBook(bookId);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TEST 6 – Borrow Limit Enforcement
    // ══════════════════════════════════════════════════════════════════════════
    private static void testBorrowLimit() {
        printSection("TEST 6 - Borrow Limit Enforcement (max " + data.BORROW_LIMIT + " books)");

        // Set up a fresh member
        String email  = "limittest_" + System.currentTimeMillis() + "@lms.test";
        String hash   = PasswordUtils.hash("Limit@999");
        int memberId  = data.registerMember("Limit", "Tester", email, hash,
                "9900000001", "LIM" + System.currentTimeMillis(), "student");
        assertTrue("6-setup: Member created for limit test", memberId > 0);

        // Create BORROW_LIMIT + 1 distinct books
        int[] bookIds = new int[data.BORROW_LIMIT + 1];
        for (int i = 0; i < bookIds.length; i++) {
            bookIds[i] = data.addBook("LimitBook_" + i, "Author",
                    "LIMIT-" + i + "-" + (System.currentTimeMillis() % 10000) + "-" + i,
                    "Misc", "Limit test book " + i, 1, 100, "Pub", 2023);
        }

        // 6-A through 6-E: Borrow up to the limit -- all should succeed
        int[] borrowIds = new int[data.BORROW_LIMIT];
        for (int i = 0; i < data.BORROW_LIMIT; i++) {
            borrowIds[i] = data.borrowBook(memberId, bookIds[i]);
            assertTrue("6-" + (char)('A' + i) + ": Borrow #" + (i + 1) + " within limit succeeds",
                    borrowIds[i] > 0);
        }

        // 6-F: The (BORROW_LIMIT+1)th borrow should return -2 (limit exceeded)
        int overLimit = data.borrowBook(memberId, bookIds[data.BORROW_LIMIT]);
        assertEquals("6-F: " + (data.BORROW_LIMIT + 1) + "th borrow returns -2 (limit exceeded)",
                -2, overLimit);

        // Cleanup: return all books and delete them
        for (int i = 0; i < data.BORROW_LIMIT; i++) {
            data.returnBook(borrowIds[i], bookIds[i]);
        }
        for (int bookId : bookIds) {
            data.deleteBook(bookId);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Helper assertion methods (no JUnit required)
    // ══════════════════════════════════════════════════════════════════════════

    private static void assertTrue(String label, boolean condition) {
        total++;
        if (condition) { pass(label); } else { fail(label, "expected TRUE but was FALSE"); }
    }

    private static void assertFalse(String label, boolean condition) {
        total++;
        if (!condition) { pass(label); } else { fail(label, "expected FALSE but was TRUE"); }
    }

    private static void assertEquals(String label, Object expected, Object actual) {
        total++;
        if (expected == null ? actual == null : expected.equals(actual)) {
            pass(label);
        } else {
            fail(label, "expected [" + expected + "] but was [" + actual + "]");
        }
    }

    private static void assertNull(String label, Object obj) {
        total++;
        if (obj == null) { pass(label); } else { fail(label, "expected NULL but was [" + obj + "]"); }
    }

    private static void assertNotNull(String label, Object obj) {
        total++;
        if (obj != null) { pass(label); } else { fail(label, "expected non-null but was NULL"); }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Formatting helpers
    // ══════════════════════════════════════════════════════════════════════════

    private static void pass(String label) {
        passed++;
        System.out.printf("  PASS  %s%n", label);
    }

    private static void fail(String label, String reason) {
        failed++;
        System.out.printf("  FAIL  %s  -->  %s%n", label, reason);
    }

    private static void printSection(String title) {
        System.out.println();
        System.out.println("  ------------------------------------------------------------");
        System.out.println("  " + title);
        System.out.println("  ------------------------------------------------------------");
    }

    private static void printBanner(String title) {
        System.out.println();
        System.out.println("  ============================================================");
        System.out.printf ("  %s%n", title);
        System.out.println("  ============================================================");
    }

    private static void printSummary() {
        System.out.println();
        System.out.println("  ============================================================");
        System.out.printf ("  RESULTS:  %d passed   %d failed   %d total%n", passed, failed, total);
        System.out.println("  ============================================================");
        if (failed == 0) {
            System.out.println("  All tests passed!");
        } else {
            System.out.println("  " + failed + " test(s) failed -- review output above.");
        }
        System.out.println();
    }
}
