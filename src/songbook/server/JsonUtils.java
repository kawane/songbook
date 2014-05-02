package songbook.server;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.function.Consumer;

/**
 * Created by j5r on 02/05/2014.
 */
public class JsonUtils {

    /** Executes given function with a controlled JsonGenerator stream on response writer */
    public static void executeWithGenerator(HttpServletResponse response, ServletConsumer<JsonGenerator> function) throws ServletException {
        JsonGenerator generator = null;
        try {
            response.setContentType("application/json");
            generator = new JsonFactory().createGenerator(response.getWriter());
            function.accept(generator);
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            try {
                if (generator != null) generator.close();
            } catch (IOException e) {
                throw new ServletException(e);
            }
        }
    }

    @FunctionalInterface
    public interface ServletConsumer<T> {
        void accept(T parameter) throws ServletException;
    }
}

