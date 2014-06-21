package songbook.server;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;
import songbook.index.SongIndex;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Server extends Verticle {

    public final static int DEFAULT_PORT = 8080;
    public final static String DEFAULT_HOST = "localhost";
    public final static String DEFAULT_WEB_ROOT = "./web";

    public static final String SONG_PATH = "/songs/";

    private Path webRoot;

    private Database database;

    @Override
    public void start() {
        webRoot = getWebRoot();
        database = new Database(vertx);

        final HttpServer httpServer = vertx.createHttpServer();
        RouteMatcher routeMatcher = new RouteMatcher();
        Logger log = container.logger();

        routeMatcher.get("/search/:query", (req) -> {
            // Serve all songs
            HttpServerResponse response = req.response();
            String query = req.params().get("query");

        });

        routeMatcher.get("/songs", (req) -> {
            // Serve all songs
            HttpServerResponse response = req.response();
            SongIndex allSongs = database.getAllSongIndex();
            response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
            response.end(Templates.showSongIndex(allSongs));
        });

        routeMatcher.get("/songs/:id", (req) -> {
            // Serve song
            HttpServerResponse response = req.response();
            response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");

            String id = QueryStringDecoder.decodeComponent(req.params().get("id"));
            response.setChunked(true);
            response.write(Templates.getHeader(id + " - My SongBook"));
            response.write(Templates.getNavigation());
            database.readHtmlSong(id, (e) -> {
                if (e.succeeded()) {
                    response.write(e.result());
                    log.trace("Serve Song " + id);
                } else {
                    response.write("Error loading song " + id);
                    log.error("Failed to read song " + id, e.cause());
                    response.setStatusCode(404);
                }
                response.write(Templates.getFooter(null));
                response.end();
            });
        });

        routeMatcher.noMatch((req) -> {
            // Serve Files
            HttpServerResponse response = req.response();
            String path = req.path();
            String fileName = path.equals("/") ? "index.html" : path;
            Path localFilePath = Paths.get(webRoot.toString(), QueryStringDecoder.decodeComponent(fileName)).toAbsolutePath();
            log.info("GET " + localFilePath);
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
        });


        httpServer.requestHandler(routeMatcher);
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
