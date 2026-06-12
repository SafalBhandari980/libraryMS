/*
 * LibraryMS.java  (libraryms package)
 * ─────────────────────────────────────────────────────────────────────
 * Application entry point.
 *
 * Changes in this version:
 *   • Calls data.updateOverdueStatuses() on startup so overdue borrowings
 *     are marked and fines calculated before the first screen opens.
 *   • Registers a JVM shutdown hook that closes the DB connection cleanly.
 *   • Wraps startup in a try/catch so a DB connection failure shows a
 *     user-friendly dialog instead of a raw stack trace.
 * ─────────────────────────────────────────────────────────────────────
 */

package libraryms;

import database.MySqlConnector;
import javax.swing.JOptionPane;

public class LibraryMS {

    public static void main(String[] args) {

        // ── Step 1: Update overdue statuses before the UI opens ──────────
        // Run on the main thread (before Swing) so the DB work is done
        // by the time the first screen appears.
        try {
            System.out.println("[LibraryMS] Checking overdue borrowings...");
            dao.data.updateOverdueStatuses();
        } catch (Exception e) {
            // Non-fatal — the UI can still open even if this fails
            System.err.println("[LibraryMS] Could not update overdue statuses: " + e.getMessage());
        }

        // ── Step 2: Register JVM shutdown hook ──────────────────────────
        // Ensures the DB connection pool is closed gracefully on app exit.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[LibraryMS] Shutting down...");
        }, "LibraryMS-Shutdown"));

        // ── Step 3: Launch the GUI on the Swing Event Dispatch Thread ───
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // Set system look-and-feel for native appearance
                try {
                    javax.swing.UIManager.setLookAndFeel(
                        javax.swing.UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                    // Gracefully fall back to default Swing L&F
                }

                view.LMS_Login_Page loginView = new view.LMS_Login_Page();
                new controller.control(loginView);
                loginView.setVisible(true);
                System.out.println("[LibraryMS] Application started.");

            } catch (Exception e) {
                System.err.println("[LibraryMS] Fatal startup error: " + e.getMessage());
                JOptionPane.showMessageDialog(null,
                    "Failed to start LibraryMS:\n" + e.getMessage()
                    + "\n\nPlease check that MySQL is running and the database 'libraryms' exists.",
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
