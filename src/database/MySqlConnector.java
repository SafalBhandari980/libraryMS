/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
/**
 *
 * @author acer
 */
public class MySqlConnector extends db {

    static Connection getConnection() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public static void closeConnection() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public Connection openConnection() {
        try{
            String username ="safal";
            String password ="1234567";
            String database ="Classwork1";
            Connection connection;
            connection = DriverManager.getConnection(
                           "jdbc:mysql://localhost:3306/" + database,username,password
            );
            if (connection == null){
                System.out.println("Connection Null");
            }
            else{
                System.out.println("Connectuion success");
            }
            return connection;
    }catch(SQLException e){
            System.out.println(e);
            }
        return null;
    }
    public void closeConnection(Connection conn) {
        try{
            if(conn != null && !conn.isClosed() ){
                conn.close();
                System.out.println("Connection close");
            }

        }catch(SQLException e){

            System.out.println(e);
        }

    }
    public ResultSet runQuery(Connection conn, String query) {
       try{
           Statement stmp = conn.createStatement();
           ResultSet result = stmp.executeQuery(query);
           return result;

       }catch (Exception e){
           System.out.println(e);
           return null;
       }
    }

    public int excecuteUpdate(Connection conn, String query) {
      try{
          Statement stmp = conn.createStatement();
          int result = stmp.executeUpdate(query);
          return result;
          
      }catch(SQLException e){
          System.out.println(e);
          return -1;
      }
    }
    
}
