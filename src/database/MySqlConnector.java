/*
 * MySqlConnector.java  (database package)
 * ─────────────────────────────────────────────────────────────────────
 * Implements the db interface and provides all low-level JDBC helpers.
 *
 * Design notes:
 *   • openConnection() creates a FRESH connection every time it is called.
 *     This avoids the "stale connection" crash that occurs when a singleton
 *     connection is left idle longer than MySQL's 8-hour wait_timeout.
 *   • Each DAO method must call closeConnection() in a finally block.
 *   • Use preparedQuery() / preparedUpdate() for all user-supplied values
 *     to prevent SQL injection.
 * ─────────────────────────────────────────────────────────────────────
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class MySqlConnector implements db {

    // ── Database configuration ────────────────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "libraryms";           // ← fixed (was "lybraryms")
    private static final String DB_USER = "root";                 // change if needed
    private static final String DB_PASS = "123456789";            // change if needed
    // ─────────────────────────────────────────────────────────────────────

    /** Full JDBC URL including options that avoid common pitfalls. */
    private static final String JDBC_URL =
            DB_URL + DB_NAME
            + "?useSSL=false"
            + "&serverTimezone=UTC"
            + "&allowPublicKeyRetrieval=true"
            + "&autoReconnect=true"
            + "&connectTimeout=5000"
            + "&socketTimeout=30000";

    // ─────────────────────────────────────────────────────────────────────
    //  db interface implementation
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Opens a fresh MySQL connection every time.
     * Returns null and prints an error if the connection fails —
     * callers should always null-check the returned value.
     */
    @Override
    public Connection openConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("[LibraryMS DB] MySQL JDBC driver not found. "
                + "Add mysql-connector-java.jar to the project libraries.");
            return null;
        } catch (Exception e) {
            System.err.println("[LibraryMS DB] openConnection failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Closes the supplied connection safely.
     * Silently ignores null or already-closed connections.
     */
    @Override
    public void closeConnection(Connection conn) {
        if (conn == null) return;
        try {
            if (!conn.isClosed()) conn.close();
        } catch (Exception e) {
            System.err.println("[LibraryMS DB] closeConnection error: " + e.getMessage());
        }
    }

    /**
     * Executes a raw (non-parameterised) SELECT.
     * Prefer preparedQuery() for any query that includes user input.
     */
    @Override
    public ResultSet runQuery(Connection conn, String query) {
        if (conn == null) { System.err.println("[LibraryMS DB] runQuery: null connection"); return null; }
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        } catch (Exception e) {
            System.err.println("[LibraryMS DB] runQuery error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Executes a raw (non-parameterised) INSERT / UPDATE / DELETE.
     * Returns rows affected, or -1 on error.
     * Prefer preparedUpdate() for any query that includes user input.
     */
    @Override
    public int executeUpdate(Connection conn, String query) {
        if (conn == null) { System.err.println("[LibraryMS DB] executeUpdate: null connection"); return -1; }
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeUpdate(query);
        } catch (Exception e) {
            System.err.println("[LibraryMS DB] executeUpdate error: " + e.getMessage());
            return -1;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Parameterised helpers (safe from SQL injection)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Executes a parameterised SELECT query.
     * Positional parameters are bound by type (Integer, Double, or String).
     * Returns null on error.
     */
    public ResultSet preparedQuery(Connection conn, String sql, Object... params) {
        if (conn == null) { System.err.println("[LibraryMS DB] preparedQuery: null connection"); return null; }
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            bindParams(pst, params);
            return pst.executeQuery();
        } catch (Exception e) {
            System.err.println("[LibraryMS DB] preparedQuery error: " + e.getMessage()
                + " | SQL: " + sql);
            return null;
        }
    }

    /**
     * Executes a parameterised INSERT / UPDATE / DELETE.
     * Returns rows affected, or -1 on error.
     */
    public int preparedUpdate(Connection conn, String sql, Object... params) {
        if (conn == null) { System.err.println("[LibraryMS DB] preparedUpdate: null connection"); return -1; }
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            bindParams(pst, params);
            return pst.executeUpdate();
        } catch (Exception e) {
            System.err.println("[LibraryMS DB] preparedUpdate error: " + e.getMessage()
                + " | SQL: " + sql);
            return -1;
        }
    }

    /**
     * Executes a parameterised INSERT and returns the auto-generated key.
     * Returns -1 on failure.
     */
    public int preparedInsertGetKey(Connection conn, String sql, Object... params) {
        if (conn == null) { System.err.println("[LibraryMS DB] preparedInsertGetKey: null connection"); return -1; }
        try {
            PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            bindParams(pst, params);
            pst.executeUpdate();
            ResultSet keys = pst.getGeneratedKeys();
            if (keys != null && keys.next()) return keys.getInt(1);
        } catch (Exception e) {
            System.err.println("[LibraryMS DB] preparedInsertGetKey error: " + e.getMessage()
                + " | SQL: " + sql);
        }
        return -1;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Private helper
    // ─────────────────────────────────────────────────────────────────────

    /** Binds varargs parameters to a PreparedStatement by type. */
    private void bindParams(PreparedStatement pst, Object... params) throws Exception {
        for (int i = 0; i < params.length; i++) {
            if      (params[i] == null)               pst.setNull(i + 1, java.sql.Types.NULL);
            else if (params[i] instanceof Integer)    pst.setInt(i + 1, (Integer) params[i]);
            else if (params[i] instanceof Long)       pst.setLong(i + 1, (Long) params[i]);
            else if (params[i] instanceof Double)     pst.setDouble(i + 1, (Double) params[i]);
            else if (params[i] instanceof Boolean)    pst.setBoolean(i + 1, (Boolean) params[i]);
            else if (params[i] instanceof java.sql.Date) pst.setDate(i + 1, (java.sql.Date) params[i]);
            else                                      pst.setString(i + 1, params[i].toString());
        }
    }
}
