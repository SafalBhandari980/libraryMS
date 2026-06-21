import utils.PasswordUtils;

/**
 * Test1_PasswordUtils.java
 * ─────────────────────────────────────────────────────────────────────────────
 * Tests password hashing, matching, and validation rules in PasswordUtils.
 * ─────────────────────────────────────────────────────────────────────────────
 * Run standalone:
 *   javac -cp "lib\*;build\classes" test\Test1_PasswordUtils.java -d test\out
 *   java  -cp "lib\*;build\classes;test\out" Test1_PasswordUtils
 */
public class Test1_PasswordUtils {

    static int passed = 0, failed = 0, total = 0;

    public static void main(String[] args) {
        printSection("TEST 1 - Password Hashing & Validation");
        run();
        printSummary();
    }

    /** Called by TestRunner to accumulate results across all tests. */
    public static int[] run() {
        passed = 0; failed = 0; total = 0;

        String hash = PasswordUtils.hash("Admin@123");

        // 1-A: hash produces a 64-char hex string
        assertEquals("1-A: SHA-256 hash length is 64", 64, hash.length());

        // 1-B: SHA-256 is deterministic (same input = same output every call)
        String expected = PasswordUtils.hash("Admin@123");
        assertEquals("1-B: SHA-256 of 'Admin@123' is deterministic", expected, hash);

        // 1-C: matches() returns true for correct password
        assertTrue("1-C: matches() returns true for correct password",
                PasswordUtils.matches("Admin@123", expected));

        // 1-D: matches() returns false for wrong password
        assertFalse("1-D: matches() returns false for wrong password",
                PasswordUtils.matches("wrongpass", expected));

        // 1-E: hash(null) returns empty string without crashing
        assertEquals("1-E: hash(null) returns empty string", "", PasswordUtils.hash(null));

        // 1-F: validate() accepts a strong password
        assertNull("1-F: validate() returns null for strong password",
                PasswordUtils.validate("Admin@123"));

        // 1-G: validate() rejects password shorter than 8 chars
        assertNotNull("1-G: validate() rejects less than 8 chars",
                PasswordUtils.validate("Ab1!"));

        // 1-H: validate() rejects password without uppercase letter
        assertNotNull("1-H: validate() rejects no uppercase",
                PasswordUtils.validate("admin@123"));

        // 1-I: validate() rejects password without a digit
        assertNotNull("1-I: validate() rejects no digit",
                PasswordUtils.validate("Admin@abc"));

        // 1-J: validate() rejects password without a special character
        assertNotNull("1-J: validate() rejects no special char",
                PasswordUtils.validate("Admin1234"));

        return new int[]{passed, failed, total};
    }

    // ── Assertion helpers ──────────────────────────────────────────────────────
    static void assertTrue(String label, boolean condition) {
        total++;
        if (condition) pass(label); else fail(label, "expected TRUE but was FALSE");
    }
    static void assertFalse(String label, boolean condition) {
        total++;
        if (!condition) pass(label); else fail(label, "expected FALSE but was TRUE");
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
