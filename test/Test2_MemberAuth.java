import dao.data;
import model.java.Member;
import utils.PasswordUtils;

/**
 * Test2_MemberAuth.java
 * ─────────────────────────────────────────────────────────────────────────────
 * Tests member registration, login, duplicate-email rejection, and email lookup.
 * ─────────────────────────────────────────────────────────────────────────────
 * Run standalone:
 *   javac -cp "lib\*;build\classes" test\Test2_MemberAuth.java -d test\out
 *   java  -cp "lib\*;build\classes;test\out" Test2_MemberAuth
 */
public class Test2_MemberAuth {

    static int passed = 0, failed = 0, total = 0;

    public static void main(String[] args) {
        printSection("TEST 2 - Member Registration & Login");
        run();
        printSummary();
    }

    public static int[] run() {
        passed = 0; failed = 0; total = 0;

        // Use timestamp-based email so repeated runs don't conflict
        String testEmail = "auth_" + System.currentTimeMillis() + "@lms.test";
        String hash = PasswordUtils.hash("Test@5678");

        // 2-A: Register a brand-new member
        int memberId = data.registerMember(
                "Test", "User", testEmail, hash,
                "9800000001", "MEM" + System.currentTimeMillis(), "student");
        assertTrue("2-A: registerMember() returns a valid ID (> 0)", memberId > 0);

        // 2-B: Duplicate email must fail (unique constraint in DB)
        int dupId = data.registerMember(
                "Test", "User", testEmail, hash,
                "9800000002", "MEM_DUP", "student");
        assertTrue("2-B: Duplicate email registration fails (id <= 0)", dupId <= 0);

        // 2-C: Login with correct credentials returns a Member object
        Member m = data.loginMember(testEmail, hash);
        assertNotNull("2-C: loginMember() returns Member for valid credentials", m);

        // 2-D: Member full name is assembled correctly
        if (m != null)
            assertEquals("2-D: Member full name is 'Test User'", "Test User", m.getFullName());

        // 2-E: Login with wrong password returns null
        Member bad = data.loginMember(testEmail, PasswordUtils.hash("WrongPass@1"));
        assertNull("2-E: loginMember() returns null for wrong password", bad);

        // 2-F: emailExists() detects the registered email
        assertTrue("2-F: emailExists() returns true for registered email",
                data.emailExists(testEmail));

        // 2-G: emailExists() returns false for an unknown address
        assertFalse("2-G: emailExists() returns false for unknown email",
                data.emailExists("nobody_" + System.currentTimeMillis() + "@lms.test"));

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
