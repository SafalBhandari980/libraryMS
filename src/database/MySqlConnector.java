/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class MySqlConnector implements db {

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "libraryms";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    // ── Singleton connection (reused across the whole session) ───────────────
    private static Connection sharedConn = null;

    /**
     * Opens (or reuses) a singleton MySQL connection.
     * Following the Fixly / Class39 pattern: opens fresh per-DAO-call is fine too,
     * but sharing one connection is lighter for a desktop app.
     */
    @Override
    public Connection openConnection() {
        try {
            if (sharedConn == null || sharedConn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                sharedConn = DriverManager.getConnection(
                        DB_URL + DB_NAME
                        + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                        DB_USER, DB_PASS);
                System.out.println("LibraryMS DB: Connection opened.");
            }
            return sharedConn;
        } catch (Exception e) {
            System.err.println("LibraryMS DB: openConnection error – " + e.getMessage());
            return null;
        }
    }

    /**
     * Closes the supplied connection (or the singleton if conn is null).
     */
    @Override
    public void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("LibraryMS DB: Connection closed.");
            }
        } catch (Exception e) {
            System.err.println("LibraryMS DB: closeConnection error – " + e.getMessage());
        }
    }

    /** Static convenience: close the shared singleton (called on shutdown). */
    public static void closeSharedConnection() {
        try {
            if (sharedConn != null && !sharedConn.isClosed()) {
                sharedConn.close();
                System.out.println("LibraryMS DB: Shared connection closed.");
            }
        } catch (Exception e) {
            System.err.println("LibraryMS DB: closeSharedConnection error – " + e.getMessage());
        }
    }

    /**
     * Executes a raw SQL SELECT query (no parameters).
     * Use preparedQuery() for parameterised queries.
     */
    @Override
    public ResultSet runQuery(Connection conn, String query) {
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        } catch (Exception e) {
            System.err.println("LibraryMS DB: runQuery error – " + e.getMessage());
            return null;
        }
    }

    /**
     * Executes a raw SQL INSERT / UPDATE / DELETE (no parameters).
     * Returns rows affected, or -1 on error.
     */
    @Override
    public int executeUpdate(Connection conn, String query) {
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeUpdate(query);
        } catch (Exception e) {
            System.err.println("LibraryMS DB: executeUpdate error – " + e.getMessage());
            return -1;
        }
    }

    /**
     * Executes a parameterised SELECT query.
     * Parameters are set positionally (String only for simplicity; extend as needed).
     */
    public ResultSet preparedQuery(Connection conn, String sql, Object... params) {
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Integer) pst.setInt(i + 1, (Integer) params[i]);
                else if (params[i] instanceof Double) pst.setDouble(i + 1, (Double) params[i]);
                else pst.setString(i + 1, params[i] == null ? null : params[i].toString());
            }
            return pst.executeQuery();
        } catch (Exception e) {
            System.err.println("LibraryMS DB: preparedQuery error – " + e.getMessage());
            return null;
        }
    }

    /**
     * Executes a parameterised INSERT / UPDATE / DELETE.
     * Returns rows affected, or -1 on error.
     */
    public int preparedUpdate(Connection conn, String sql, Object... params) {
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Integer) pst.setInt(i + 1, (Integer) params[i]);
                else if (params[i] instanceof Double) pst.setDouble(i + 1, (Double) params[i]);
                else pst.setString(i + 1, params[i] == null ? null : params[i].toString());
            }
            return pst.executeUpdate();
        } catch (Exception e) {
            System.err.println("LibraryMS DB: preparedUpdate error – " + e.getMessage());
            return -1;
        }
    }

    /**
     * Executes a parameterised INSERT and returns the auto-generated key.
     * Returns -1 on failure.
     */
    public int preparedInsertGetKey(Connection conn, String sql, Object... params) {
        try {
            PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Integer) pst.setInt(i + 1, (Integer) params[i]);
                else if (params[i] instanceof Double) pst.setDouble(i + 1, (Double) params[i]);
                else pst.setString(i + 1, params[i] == null ? null : params[i].toString());
            }
            pst.executeUpdate();
            ResultSet keys = pst.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        
    }
}
