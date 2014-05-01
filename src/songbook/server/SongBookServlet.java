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

        final String servletPath = request.getServletPath();
        final String requestURI = request.getRequestURI();
        if (servletPath.equals(requestURI)) {
            Database.executeWithConnection((connection, statement) -> {
                statement.execute("SELECT id, name FROM Songs");
                final ResultSet resultSet = statement.getResultSet();

                out.println("<ul>");
                while (resultSet.next()) {
                    out.println("<li><a href='" + servletPath + "/" + resultSet.getString(1) + "'>" + resultSet.getString(2) + "</a></li>");
                }
                out.println("</ul>");
            });

        } else {
            // presents the request song
            final String songId = requestURI.substring(servletPath.length() + 1);
            if (Database.isValidId(songId)) {
                Database.executeWithConnection((connection, statement) -> {
                    statement.execute("SELECT name, author, contents FROM Songs WHERE id='" + songId + "'");

                    final ResultSet resultSet = statement.getResultSet();
                    if (resultSet.next()) {
                        // one song found, prints it.
                        out.println("<h2>" + resultSet.getString(1) + "</h2>");
                        out.println("<i>" + resultSet.getString(2) + "</i>");
                        out.println("<pre>" + resultSet.getString(3) + "</pre>");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                });
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        }
        out.println("</body>");
        out.println("</html>");
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String servletPath = request.getServletPath();
        final String requestURI = request.getRequestURI();

        if ( servletPath.equals(requestURI)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final String songId = requestURI.substring(servletPath.length() + 1);
        if (Database.isValidId(songId) == false) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("text/html");

        final PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>Songbook</title></head>");
        out.println("<body>");

        Database.executeWithConnection((connection, statement) -> {
            final String encoding = request.getCharacterEncoding();
            final String contents = StreamUtils.getContents(request.getInputStream(), encoding);
            statement.execute("UPDATE Songs SET contents='" + contents + "' WHERE id='" + songId + "'");
            out.println("<h2>Song updated.</h2>");
        });

        out.println("</body>");
        out.println("</html>");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ( request.getServletPath().equals(request.getRequestURI()) == false ) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("text/html");

        final PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>Songbook</title></head>");
        out.println("<body>");

        Database.executeWithConnection((connection, statement) -> {
            final String contents = StreamUtils.getContents(request.getInputStream(), request.getCharacterEncoding());
            statement.execute("INSERT Songs (name, author, creator, contents) values (" +
                        "'name'," +
                        "'author'," +
                        "'0'," +
                        "'"+ contents +"'," +
                    ")");
        });

        out.println("</body>");
        out.println("</html>");
    }
}

