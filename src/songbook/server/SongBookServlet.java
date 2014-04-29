package songbook.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by LF5 on 23/04/2014.
 */
public class SongBookServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        final PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>Songbook</title></head>");
        out.println("<body>");
        try {
            final Connection connection = Database.getConnection();
            final Statement statement = connection.createStatement();

            final String servletPath = request.getServletPath();
            final String requestURI = request.getRequestURI();
            if ( servletPath.equals(requestURI) ) {
                // presents all songs
                statement.execute("SELECT id, name FROM songbook.Songs");

                final ResultSet resultSet = statement.getResultSet();

                out.println("<ul>");
                while ( resultSet.next() ) {
                    out.println("<li><a href='"+ servletPath + "/" + resultSet.getString(1) +"'>" + resultSet.getString(2) + "</a></li>");
                }
                out.println("</ul>");

            } else {
                // presents the request song
                final String songId = requestURI.substring(servletPath.length() + 1);
                if ( Database.isValidId(songId) ) {
                    statement.execute("SELECT name, author, contents FROM songbook.Songs WHERE id='" + songId + "'");

                    final ResultSet resultSet = statement.getResultSet();
                    if ( resultSet.next() ) {
                        // one song found, prints it.
                        out.println("<h2>" + resultSet.getString(1) + "</h2>");
                        out.println("<i>" + resultSet.getString(2) + "</i>");
                        out.println("<pre>" + resultSet.getString(3) + "</pre>");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }

                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }
        out.println("</body>");
        out.println("</html>");
    }

}
