package songbook.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by j5r on 26/04/2014.
 */
public class TestMySQL {

    public static String testConnection() {
        try {
            final Connection connection = DriverManager.getConnection("jdbc:mysql://" + getMySQLHost() + ":" + getMySQLPort() + "/songbook?user=adminqKH3YN4&password=xtT-T2Lijk4W");
            return connection.getMetaData().toString();
        } catch (SQLException ex) {
            return ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
    }

    public static void main(String[] args) {
        System.out.println(testConnection());
    }

    private static String getMySQLPort() {
        final String port = System.getenv("OPENSHIFT_MYSQL_DB_PORT");
        return port == null ? "8080" : port;
    }

    private static String getMySQLHost() {
        final String host = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
        return host == null ? "localhost" : host;
    }
}
