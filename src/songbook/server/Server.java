package songbook.server;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;
import songbook.index.SongIndex;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server extends Verticle {

    public final static int DEFAULT_PORT = 8080;
    public final static String DEFAULT_HOST = "localhost";
    public final static String DEFAULT_WEB_ROOT = "web";

    public static final String SONG_PATH = "/songs/";

    private Path webRoot;

    private Database database;

    private void handler(HttpServerRequest request) {
        final String path = request.path();
        final HttpServerResponse response = request.response();
        Logger log = container.logger();
        if (path.equals("/songs")) {
            // lists all songs
            SongIndex allSongs = database.getAllSongIndex();
            response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
            response.end(Templates.showSongIndex(allSongs));

        } else if (path.startsWith("/songs/")) {
            // shows chosen song
            final String subPath = path.substring(SONG_PATH.length());
            final String id = QueryStringDecoder.decodeComponent(subPath);
            response.setChunked(true);
            response.write(Templates.getHeader(id + " - My SongBook"));
            response.write(Templates.getNavigation());
            try {
                response.write(database.readHtmlSong(id));
            } catch (IOException e) {
                response.write("Error loading song");
                log.error("Failed to read", e);
                response.setStatusCode(404);
            }
            response.write(Templates.getFooter(null));
            response.end();
        } else {
            final String fileName = path.equals("/") ? "index.html" : path;
            final Path localFilePath = Paths.get(webRoot.toString(), fileName).normalize();
            String type = "text/plain";
            if (fileName.endsWith(".js")) {
                type = "application/javascript";
            } else if (fileName.endsWith(".css")) {
                type = "text/css";
            } else if (fileName.endsWith(".html")) {
                type = "text/html";
            }
            response.putHeader(HttpHeaders.CONTENT_TYPE, type);
            response.sendFile(localFilePath.toString());
        }
    }


    @Override
    public void start() {
        webRoot = getWebRoot();
        database = new Database(vertx);

        final HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(this::handler);
        httpServer.listen(getPort(), getHost());
    }

    private Path getWebRoot() {
        final String webRoot = System.getenv("WEB_ROOT");
        return Paths.get(webRoot == null ? DEFAULT_WEB_ROOT : webRoot);
    }

    private int getPort() {
        final String portString = System.getenv("PORT");
        int port = DEFAULT_PORT;
        if ( portString != null ) {
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                // doesn't matter;
            }
        }
        return port;
    }

    private String getHost() {
        final String host = System.getenv("HOST");
        return host == null ? DEFAULT_HOST : host;
    }
}
