/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
<<<<<<< HEAD
/*
 * db.java  (database package)
 * ─────────────────────────────────────────────────────────────────
 * Low-level SQL executor. Called ONLY by DAO classes (data.java).
 * Never call this directly from Controller or View.
 *
 * Pattern:
 *   View → Controller → DAO → db → MySqlConnector → MySQL
 * ─────────────────────────────────────────────────────────────────
 */
=======
>>>>>>> 2adfde3a6e4f6784954be4a208096dec2415f42d
package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class db {

    /**
     * Executes a SELECT query and returns a ResultSet.
     *
     * IMPORTANT: The caller must close the ResultSet when done.
     * Use try-with-resources in the DAO:
     *   try (ResultSet rs = db.executeQuery(sql, param)) { ... }
     *
     * @param sql    the SQL SELECT string with ? placeholders
     * @param params values to bind to each ?
     * @return ResultSet containing the query results
     * @throws SQLException if the query fails
     */
    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = MySqlConnector.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        bindParams(stmt, params);
        return stmt.executeQuery();
    }

    /**
     * Executes an INSERT / UPDATE / DELETE.
     *
     * @param sql    the SQL string with ? placeholders
     * @param params values to bind to each ?
     * @return number of rows affected
     * @throws SQLException if the update fails
     */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = MySqlConnector.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindParams(stmt, params);
            return stmt.executeUpdate();
        }
    }

    /**
     * Executes an INSERT and returns the auto-generated primary key.
     *
     * @param sql    the SQL INSERT string with ? placeholders
     * @param params values to bind to each ?
     * @return generated key as int, or -1 if none was generated
     * @throws SQLException if the insert fails
     */
    public static int executeInsertGetKey(String sql, Object... params) throws SQLException {
        Connection conn = MySqlConnector.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            bindParams(stmt, params);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    // ── Private helper: binds Object[] params to a PreparedStatement ─────────
    private static void bindParams(PreparedStatement stmt, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 2adfde3a6e4f6784954be4a208096dec2415f42d
