/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import java.sql.*;

/**
 *
 * @author acer
 */
public class MySqlConnector implements db {

    @Override
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
    }catch(Exception e){
            System.out.println(e);
            }
        return null;
    }
    
}
