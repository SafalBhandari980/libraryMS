/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

package libraryms;

import database.MySqlConnector;

public class LibraryMS {

    public static void main(String[] args) {
        // Close the DB connection cleanly when the JVM shuts down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MySqlConnector.closeConnection();
        }));

        // Launch the GUI on the Swing Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            // TODO: replace NewClass with your actual login JFrame class
            // new view.LoginForm().setVisible(true);
            System.out.println("LibraryMS started. Wire up your View here.");
        });
    }
}

