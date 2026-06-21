/**
 * TestRunner.java
 * ─────────────────────────────────────────────────────────────────────────────
 * Master test runner for LibraryMS.
 * Executes all 6 individual test classes and prints a combined summary.
 *
 * Individual test files:
 *   Test1_PasswordUtils.java  – Password hashing & validation
 *   Test2_MemberAuth.java     – Member registration & login
 *   Test3_BookCRUD.java       – Book Add / Read / Update / Delete
 *   Test4_BorrowReturn.java   – Borrow & return lifecycle
 *   Test5_BookSearch.java     – Book search & filtering
 *   Test6_BorrowLimit.java    – Borrow limit enforcement
 *
 * Build & run (from project root, after Clean and Build in NetBeans):
 *   javac -cp "lib\*;build\classes" test\*.java -d test\out
 *   java  -cp "lib\*;build\classes;test\out" TestRunner
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println();
        System.out.println("  ============================================================");
        System.out.println("  LibraryMS – Full Test Suite");
        System.out.println("  ============================================================");

        int totalPassed = 0;
        int totalFailed = 0;
        int totalTests  = 0;

        // ── Run each test class, collect results ──────────────────────────────
        int[] r1 = runSafe("Test1_PasswordUtils",  () -> Test1_PasswordUtils.run());
        int[] r2 = runSafe("Test2_MemberAuth",      () -> Test2_MemberAuth.run());
        int[] r3 = runSafe("Test3_BookCRUD",        () -> Test3_BookCRUD.run());
        int[] r4 = runSafe("Test4_BorrowReturn",    () -> Test4_BorrowReturn.run());
        int[] r5 = runSafe("Test5_BookSearch",      () -> Test5_BookSearch.run());
        int[] r6 = runSafe("Test6_BorrowLimit",     () -> Test6_BorrowLimit.run());

        // ── Aggregate ─────────────────────────────────────────────────────────
        int[][] all = {r1, r2, r3, r4, r5, r6};
        String[] names = {
            "Test1_PasswordUtils ",
            "Test2_MemberAuth    ",
            "Test3_BookCRUD      ",
            "Test4_BorrowReturn  ",
            "Test5_BookSearch    ",
            "Test6_BorrowLimit   "
        };

        System.out.println();
        System.out.println("  ============================================================");
        System.out.println("  SUMMARY");
        System.out.println("  ============================================================");
        System.out.printf("  %-24s  %6s  %6s  %6s%n", "Test Class", "Passed", "Failed", "Total");
        System.out.println("  --------------------------------------------------------");
        for (int i = 0; i < all.length; i++) {
            int p = all[i][0], f = all[i][1], t = all[i][2];
            totalPassed += p;
            totalFailed += f;
            totalTests  += t;
            String status = f == 0 ? "OK" : "FAIL";
            System.out.printf("  %-24s  %6d  %6d  %6d   [%s]%n",
                    names[i], p, f, t, status);
        }
        System.out.println("  --------------------------------------------------------");
        System.out.printf("  %-24s  %6d  %6d  %6d%n", "TOTAL", totalPassed, totalFailed, totalTests);
        System.out.println("  ============================================================");
        System.out.println();
        if (totalFailed == 0) {
            System.out.println("  All " + totalTests + " tests PASSED!");
        } else {
            System.out.println("  " + totalFailed + " test(s) FAILED out of " + totalTests + ".");
        }
        System.out.println();
    }

    /** Runs a test supplier, catches any unexpected exception so other tests still run. */
    private static int[] runSafe(String name, java.util.function.Supplier<int[]> testFn) {
        try {
            return testFn.get();
        } catch (Exception e) {
            System.out.printf("%n  [ERROR] %s threw an unexpected exception: %s%n",
                    name, e.getMessage());
            e.printStackTrace();
            return new int[]{0, 1, 1};
        }
    }
}
