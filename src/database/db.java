/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;
import java.sql.Connection;
import java.sql.ResultSet;

/**
 *
 * @author acer
 */
public interface db {
    Connection openConnection();
    void closeConnection();
    ResultSet runQuery();
    int executeUpdate();

    public void closeConnection(Connection conn);
    
}
