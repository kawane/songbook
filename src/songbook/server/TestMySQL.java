package songbook.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by j5r on 26/04/2014.
 */
public class TestMySQL {

    public static final String getMySQLAccess() {
        final String mysqlAccess = System.getProperty("mysql.access");
        return mysqlAccess == null ? "localhost:3306/songbook?user=root" : mysqlAccess;
    }

    public static String testConnection() {
        try {
            final Connection connection = DriverManager.getConnection("jdbc:mysql://" +  getMySQLAccess());
            return connection.getMetaData().toString();
        } catch (SQLException ex) {
            return ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
    }

    public static void main(String[] args) {
        System.out.println(testConnection());
    }

}
