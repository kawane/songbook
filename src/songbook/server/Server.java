package songbook.server;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.platform.Verticle;
import songbook.index.Song;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Server extends Verticle {

    private final static String WEB_DIRECTORY = "web";
    public static final String SONG_PATH = "/songs/";

    private void handler(HttpServerRequest request) {
        final String path = request.path();
        final HttpServerResponse response = request.response();
        if (path.equals("/songs")) {
            // lists all songs
            final Stream<Song> allSongs = Database.getAllSongs();
            response.end(Templates.getSongIndex(allSongs));

        } else if (path.startsWith("/songs/")) {
            // shows chosen song
            final String subPath = path.substring(SONG_PATH.length());
            final String id = QueryStringDecoder.decodeComponent(subPath);
            try {
                final Song song = Database.getSong(id);
                if ( song != null ) {
                    final Buffer buffer = new Buffer();
                    buffer.appendString(Templates.getSongView(song));
                    response.end(buffer);
                } else {
                    response.setStatusCode(404);
                }
            } catch (IOException e) {
                response.setStatusCode(404);
            } finally {
                response.close();
            }
        } else {
            final String fileName = path.equals("/") ? "index.html" : path;
            final Path localFilePath = Paths.get(WEB_DIRECTORY, fileName).normalize();
            if (localFilePath.startsWith(WEB_DIRECTORY)) {
                response.sendFile(localFilePath.toString());
            } else {
                response.setStatusCode(404);
                response.close();
            }
        }
    }

    @Override
    public void start() {
        final HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(this::handler);
        httpServer.listen(8080);
    }
}
