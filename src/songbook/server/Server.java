package songbook.server;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import org.vertx.java.platform.Verticle;
import songbook.index.HtmlIndexer;
import songbook.index.IndexDatabase;
import songbook.index.SongUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Server extends Verticle {

    public final static int DEFAULT_PORT = 8080;

    public final static String DEFAULT_HOST = "localhost";

    public final static String DEFAULT_WEB_ROOT = "./web";

    public final static String DEFAULT_DATA_ROOT = "data";

    private Logger logger;

    private Path webRoot;

    private Path dataRoot;

    private IndexDatabase indexDatabase;

    private String administratorKey = null;

    private String userKey = null;

    @Override
    public void start() {
        logger = getContainer().logger();
        readKeys();
        try {
            webRoot = getWebRoot();

            dataRoot = getDataRoot();

            indexDatabase = new IndexDatabase(dataRoot.resolve("index"));

            indexDatabase.analyzeSongs(dataRoot.resolve("songs"));

            HttpServer httpServer = vertx.createHttpServer();
            RouteMatcher routeMatcher = new RouteMatcher();
            Logger log = container.logger();

            Handler<HttpServerRequest> searchHandler = (request) -> {
                if (checkDeniedAccess(request, false)) return;


                // Serve all songs
                HttpServerResponse response = request.response();
                response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");

                String query = request.params().get("query");
                response.setChunked(true);
                String title = "My SongBook";
                if (query != null && !query.isEmpty()) {
                    title = query + " - " + title;
                }

                String key = getRequestKey(request);
                response.write(Templates.getHeader(key, title));
                response.write(Templates.getNavigation(key));
                try {
                    indexDatabase.search(key, query, request);
                } catch (ParseException e) {
                    e.printStackTrace();
                    response.write("Wrong Query Syntax");
                } catch (IOException e) {
                    e.printStackTrace();
                    response.write("Internal Error");
                }
                response.write(Templates.getFooter(key, null));
                response.end();
            };
            routeMatcher.get("/search/:query", searchHandler);
            routeMatcher.get("/search", searchHandler);
            routeMatcher.get("/", searchHandler);

            routeMatcher.get("/songs/:id", (request) -> {
                if (checkDeniedAccess(request, false)) return;
                boolean admin = isAdministrator(getRequestKey(request));

                // Serves song
                HttpServerResponse response = request.response();
                response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");

                String id = QueryStringDecoder.decodeComponent(request.params().get("id"));
                response.setChunked(true);

                String key = getRequestKey(request);
                response.write(Templates.getHeader(key, id + " - My SongBook"));
                response.write(Templates.getNavigation(key));
                readHtmlSong(id, (e) -> {
                    if (e.succeeded()) {
                        response.write(e.result());
                        log.trace("Serve Song " + id);
                    } else {
                        response.write("Error loading song " + id);
                        log.error("Failed to read song " + id, e.cause());
                        response.setStatusCode(404);
                    }
                    response.write(Templates.getFooter(key, admin ? "songbook.installEditionModeActivation()" : null));
                    response.end();
                });
            });

            routeMatcher.post("/songs", (request) -> {
                if (checkDeniedAccess(request, true)) return;

                HttpServerResponse response = request.response();
                request.bodyHandler((body) -> {
                    String songData = body.toString();
                    Document document;
                    try {
                        HtmlIndexer songIndexer = new HtmlIndexer();
                        document = songIndexer.indexSong(songData);

                        Path filePath = Files.createTempFile(dataRoot.resolve("songs"), "", ".html").toAbsolutePath();
                        String id = SongUtil.getId(filePath.getFileName().toString());
                        document.add(new StringField("id", id, Field.Store.YES));
                        indexDatabase.addOrUpdateDocument(document);

                        vertx.fileSystem().writeFile(filePath.toString(), body, (ar) -> {
                            if (ar.succeeded()) {
                                response.end(id);
                            } else {
                                log.error("Failed to create song", ar.cause());
                                response.setStatusCode(500);
                                response.end();
                                try {
                                    Files.deleteIfExists(filePath);
                                } catch (IOException e) {
                                    log.warn("Can't delete file", e);
                                }
                            }
                        });
                    } catch (Exception e) {
                        log.error("Error indexing song", e);
                        response.setStatusCode(500);
                        response.end();
                    }
                });
            });

            routeMatcher.put("/songs/:id", (request) -> {
                if (checkDeniedAccess(request, true)) return;

                HttpServerResponse response = request.response();
                String id = request.params().get("id");
                String fileName = decodeUrl(id) + ".html";
                Path filePath = dataRoot.resolve("songs").resolve(fileName).toAbsolutePath();
                request.bodyHandler((body) -> {
                    String songData = body.toString();
                    Document document;
                    try {
                        HtmlIndexer songIndexer = new HtmlIndexer();
                        document = songIndexer.indexSong(songData);
                        document.add(new StringField("id", SongUtil.getId(filePath.getFileName().toString()), Field.Store.YES));
                        indexDatabase.addOrUpdateDocument(document);

                        vertx.fileSystem().writeFile(filePath.toString(), body, (ar) -> {
                            if (ar.succeeded()) {
                                response.end(id);
                            } else {
                                log.error("Failed to create song", ar.cause());
                                response.setStatusCode(500);
                                response.end();
                                try {
                                    Files.deleteIfExists(filePath);
                                } catch (IOException e) {
                                    log.warn("Can't delete file", e);
                                }
                            }
                        });
                    } catch (Exception e) {
                        // TODO write error to client
                        log.error("Error indexing song", e);
                        response.setStatusCode(500);
                        response.end();
                    }
                });
            });

            routeMatcher.noMatch((request) -> {
                //if (checkDeniedAccess(request, false)) return;

                // Serve Files
                HttpServerResponse response = request.response();
                String path = request.path();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readHtmlSong(String id, Handler<AsyncResult<String>> handler) {
        DefaultFutureResult<String> event = new DefaultFutureResult<>();
        event.setHandler(handler);
        try {
            ConcurrentSharedMap<Object, String> songs = vertx.sharedData().getMap("songs");
            String song = songs.get(id);
            if (song != null) {
                event.setResult(song);
            } else {
                final Path songPath = dataRoot.resolve("songs").resolve(id + IndexDatabase.SONG_EXTENSION).toAbsolutePath();
                vertx.fileSystem().readFile(songPath.toString(), (e) -> {
                    if (e.succeeded()) {
                        String songFromFile = e.result().toString();
                        songs.put(id, songFromFile);
                        event.setResult(songFromFile);
                    } else {
                        event.setFailure(e.cause());
                    }
                });

            }
        } catch (Throwable e) {
            event.setFailure(e);
        }
    }

    private Path getWebRoot() {
        final String webRoot = System.getenv("WEB_ROOT");
        return Paths.get(webRoot == null ? DEFAULT_WEB_ROOT : webRoot);
    }

    private static Path getDataRoot() {
        final String dataRoot = System.getenv("DATA_ROOT");
        return Paths.get(dataRoot == null ? DEFAULT_DATA_ROOT : dataRoot);
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

    /** Searches for keys on server to initialize administratorKey and userKey. */
    private void readKeys() {
        try {
            final Path administratorKeyPath = getDataRoot().resolve("administrator.key");
            if (Files.exists(administratorKeyPath)) {
                final List<String> allLines = Files.readAllLines(administratorKeyPath);
                if (allLines.isEmpty() == false) {
                    administratorKey = allLines.get(allLines.size() - 1);
                }
            }
        } catch (IOException e) {
            logger.error("Could not read administrator key", e);
        }

        try {
            final Path userKeyPath = getDataRoot().resolve("user.key");
            if (Files.exists(userKeyPath)) {
                final List<String> allLines = Files.readAllLines(userKeyPath);
                if (allLines.isEmpty() == false) {
                    userKey = allLines.get(allLines.size() - 1);
                }
            }
        } catch (IOException e) {
            logger.error("Could not read user key", e);
        }
    }

    /** Extracts key from query */
    private String getRequestKey(HttpServerRequest request) {
        final String query = request.query();
        if (query != null) {
            final String attribute = "key=";
            final int keyIndex = query.indexOf(attribute);
            if (keyIndex >= 0) {
                final int andIndex = query.indexOf('&', keyIndex);
                return query.substring(keyIndex+attribute.length(), andIndex >= 0 ? andIndex : query.length());
            }
        }
        return null;
    }

    /** Checks if key allows to be administrator */
    private boolean isAdministrator(String requestKey) {
        return administratorKey == null || administratorKey.equals(requestKey);
    }

    /** Checks if key allows to be user */
    private boolean isUser(String requestKey) {
        return userKey== null || userKey.equals(requestKey);
    }

    /** Checks if request need to be denied, returns true if access is denied. */
    private boolean checkDeniedAccess(HttpServerRequest request, boolean needAdmin) {
        String key = getRequestKey(request);
        if (isAdministrator(key)) return false;
        if (isUser(key) && needAdmin==false) return false;

        forbiddenAccess(request);
        return true;
    }

    private void forbiddenAccess(HttpServerRequest request) {
        HttpServerResponse response = request.response();
        response.setStatusCode(403);
        response.end("Access Forbidden '" + request.path() + "'");
    }


    private static String decodeUrl(String id) {
        try {
            return URLDecoder.decode(id, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // Do nothing but logging
            e.printStackTrace();
        }
        return id;
    }
}
