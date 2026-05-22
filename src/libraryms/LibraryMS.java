/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package libraryms;

import database.db;
import database.MySqlConnector;
import java.sql.Connection;

/**
 *
 * @author acer
 */
public class LibraryMS {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        db dbConnection=new MySqlConnector();
        Connection conn=dbConnection.openConnection();
        dbConnection.closeConnection(conn);
    }
    
}
