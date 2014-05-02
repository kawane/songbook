package songbook.server;

import javax.servlet.ServletException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by j5r on 26/04/2014.
 */
public class Database {

    static {
        try {
            // The newInstance() call is a work around for some broken Java implementations
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }
    }

    public static final String getMySQLAccess() {
        final String mysqlAccess = System.getProperty("mysql.access");
        return mysqlAccess == null ? "localhost:3306/songbook?user=root" : mysqlAccess;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" +  getMySQLAccess());
    }

    public static void executeWithConnection(SqlFunction function) throws ServletException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = Database.getConnection();
            statement = connection.createStatement();
            function.accept(connection, statement);
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            try {
                if (connection != null) connection.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                throw new ServletException(e);
            }
        }
    }


    public static boolean isValidId(String id) {
        if (id==null) return false;
        for (int i=0; i<id.length(); i+=1) {
            if (Character.isDigit(id.charAt(i)) == false) return false;
        }
        return true;
    }

    @FunctionalInterface
    public interface SqlFunction {
        void accept(Connection connection, Statement statement) throws Exception;
    }
}
