import dao.data;
import model.java.Book;

import java.util.List;

/**
 * Test5_BookSearch.java
 * ─────────────────────────────────────────────────────────────────────────────
 * Tests the book-search feature with various filter combinations:
 *   • Keyword search by title, author, ISBN
 *   • Non-existent keyword returns empty list
 *   • Category filter, availability filter, publish-year filter
 * ─────────────────────────────────────────────────────────────────────────────
 * Run standalone:
 *   javac -cp "lib\*;build\classes" test\Test5_BookSearch.java -d test\out
 *   java  -cp "lib\*;build\classes;test\out" Test5_BookSearch
 */
public class Test5_BookSearch {

    static int passed = 0, failed = 0, total = 0;

    public static void main(String[] args) {
        printSection("TEST 5 - Book Search & Filtering");
        run();
        printSummary();
    }

    public static int[] run() {
        passed = 0; failed = 0; total = 0;

        // ── Setup: insert a uniquely-named book for search ─────────────────────
        String uniqueTitle = "UniqueSearchBook_" + System.currentTimeMillis();
        String isbn        = "SR-" + (System.currentTimeMillis() % 100000);
        int bookId = data.addBook(uniqueTitle, "Search Author", isbn,
                "Science", "Searchable test book.", 5, 300, "SciPub", 2022);
        assertTrue("5-setup: Book created for search test", bookId > 0);

        // 5-A: Search by unique title keyword returns at least 1 result
        List<Book> byTitle = data.searchBooks(uniqueTitle);
        assertTrue("5-A: Search by unique title returns >= 1 result", !byTitle.isEmpty());

        // 5-B: Search by author keyword returns at least 1 result
        List<Book> byAuthor = data.searchBooks("Search Author");
        assertTrue("5-B: Search by author keyword returns result", !byAuthor.isEmpty());

        // 5-C: Search by ISBN returns at least 1 result
        List<Book> byIsbn = data.searchBooks(isbn);
        assertTrue("5-C: Search by ISBN returns result", !byIsbn.isEmpty());

        // 5-D: Search with a non-existent keyword returns an empty list
        List<Book> none = data.searchBooks("XYZZY_NONEXISTENT_12345");
        assertTrue("5-D: Non-existent keyword returns empty list", none.isEmpty());

        // 5-E: Category filter returns ONLY books in that category
        List<Book> byCat = data.searchBooks(null, "Science", null, null);
        boolean allInCat = byCat.stream().allMatch(b -> "Science".equals(b.getCategory()));
        assertTrue("5-E: Category filter returns only 'Science' books", allInCat);

        // 5-F: availableOnly filter returns ONLY books with copies > 0
        List<Book> available = data.searchBooks(null, null, true, null);
        boolean allAvail = available.stream().allMatch(Book::isAvailable);
        assertTrue("5-F: availableOnly filter returns only books with copies > 0", allAvail);

        // 5-G: Year filter returns ONLY books published in that year
        List<Book> byYear = data.searchBooks(null, null, null, 2022);
        boolean allYear = byYear.stream().allMatch(b -> b.getPublishYear() == 2022);
        assertTrue("5-G: Year filter returns only books from year 2022", allYear);

        // ── Cleanup ────────────────────────────────────────────────────────────
        data.deleteBook(bookId);

        return new int[]{passed, failed, total};
    }

    // ── Assertion helpers ──────────────────────────────────────────────────────
    static void assertTrue(String label, boolean condition) {
        total++;
        if (condition) pass(label); else fail(label, "expected TRUE but was FALSE");
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
