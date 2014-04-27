package songbook.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by j5r on 26/04/2014.
 */
public class Database {

    public static final String getMySQLAccess() {
        final String mysqlAccess = System.getProperty("mysql.access");
        return mysqlAccess == null ? "localhost:3306/songbook?user=root" : mysqlAccess;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" +  getMySQLAccess());
    }

}
