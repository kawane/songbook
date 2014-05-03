package songbook.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by j5r on 01/05/2014.
 */
public class SongBookServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String servletPath = request.getServletPath();
        final String requestURI = request.getRequestURI();

        if ( requestURI.equals(servletPath)) {
            // serves index of song
            response.setContentType("text/html");
            response.getWriter().write(Templates.getSongIndex());
        } else {
            // serves given song
            final String songId = requestURI.substring(servletPath.length() + 1);
            if (Database.isValidId(songId) == false) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            response.setContentType("text/html");
            response.getWriter().write(Templates.getSongView(songId, songId));
        }
    }
}
