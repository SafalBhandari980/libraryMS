/*
 * db.java  (database package)
 * Interface defining the database contract for LibraryMS.
 * Pattern used by Fixly and Class39 projects.
 */
package database;

import java.sql.Connection;
import java.sql.ResultSet;

public interface db {
    Connection openConnection();
    void closeConnection(Connection conn);
    ResultSet runQuery(Connection conn, String query);
    int executeUpdate(Connection conn, String query);
}
