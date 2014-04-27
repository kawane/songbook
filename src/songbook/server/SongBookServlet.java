package songbook.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by LF5 on 23/04/2014.
 */
public class SongBookServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>Songbook</title></head>");
        out.println("<body>");
        try {
            final Connection connection = Database.getConnection();
            final Statement statement = connection.createStatement();
            final boolean execute = statement.execute("SELECT t.* FROM songbook.Songs t", new String[]{"id", "name"});

            final ResultSet resultSet = statement.getResultSet();

            out.println("<ul>");
            while ( resultSet.next() ) {
                out.println("<li>" + resultSet.getString(2) + "</li>");
            }
            out.println("</ul>");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        out.println("</body>");
        out.println("</html>");
    }

}
