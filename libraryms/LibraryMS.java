/*
 * LibraryMS.java  (libraryms package)
 * ─────────────────────────────────────────────────────────────────────
 * Application entry point.
 *
 * Starts the GUI on the Swing Event Dispatch Thread and opens the
 * Login page.  Registers a shutdown hook to close the DB connection
 * cleanly when the JVM exits.
 * ─────────────────────────────────────────────────────────────────────
 */
package libraryms;

import database.MySqlConnector;
import controller.control;
import view.LMS_Login_Page;

public class LibraryMS {

    public static void main(String[] args) {

        // Close the DB connection cleanly when the JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MySqlConnector.closeSharedConnection();
            System.out.println("LibraryMS: shutdown complete.");
        }));

        // Launch the Swing GUI on the Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() -> {

            // Set Nimbus look-and-feel (matches existing UI)
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info :
                        javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                // Fall back to the system default
            }

            // Open the login page and attach its controller
            LMS_Login_Page loginPage = new LMS_Login_Page();
            new control(loginPage);
            loginPage.setVisible(true);
        });
    }
}
