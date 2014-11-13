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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

public class Server extends Verticle {

    public final static int DEFAULT_PORT = 8080;

    public final static String DEFAULT_HOST = "localhost";

    public final static String DEFAULT_WEB_ROOT = "web";

    public final static String DEFAULT_DATA_ROOT = "data";

    public static final String ADMINISTRATOR_KEY_PATH = "administrator.key";
    public static final String ADMINISTRATOR_ACTIVATED_PATH = "administrator.activated";

    private Logger logger;

    private Path webRoot;

    private Path dataRoot;

    private IndexDatabase indexDatabase;

    private boolean showKeyCreationAlert = false;
    private String administratorKey = null;
    private String userKey = null;

    @Override
    public void start() {
        logger = getContainer().logger();
        webRoot = getWebRoot();
        dataRoot = getDataRoot();

        try {
            if (Files.exists(dataRoot) == false) Files.createDirectories(dataRoot);
        } catch (IOException e) {
            logger.error("Cannot start server data root isn't accessible.", e);
            return;
        }

        readKeys();

        // creates server
        HttpServer httpServer = vertx.createHttpServer();

        // creates admin key if needed
        if (administratorKey == null)  createAdminKey();

        // initializes index.
        try {
            long start = System.currentTimeMillis();
            indexDatabase = new IndexDatabase(dataRoot.resolve("index"), dataRoot.resolve("songs"));
            long end = System.currentTimeMillis();
            logger.info("Opened index in " + (end - start) + " milliseconds.");

        } catch (IOException e) {
            logger.error("Can't initialize index in "+ dataRoot.resolve("index"));
        }

        //installs matcher to server song
        RouteMatcher routeMatcher = createSongServerRoutMatcher();
        httpServer.requestHandler(routeMatcher);

        final int port = getPort();
        final String host = getHost();
        logger.info("Starting server on '"+ host +":"+ port +"'.");
        httpServer.listen(port, host);
    }

    private void createAdminKey() {
        // creates administrator key when it's null
        long timestamp = System.currentTimeMillis();
        String timestampString = Long.toHexString(timestamp);
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(timestampString.getBytes(), 0, timestampString.length());
            administratorKey =  new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            administratorKey = timestampString;
        }
        logger.info("Created administrator key: '" + administratorKey + "'.");
        writeKeys();
    }

    private RouteMatcher createSongServerRoutMatcher() {
        RouteMatcher routeMatcher = new RouteMatcher();
        Handler<HttpServerRequest> searchHandler = createSearchHandler();
        routeMatcher.get("/", searchHandler);
        routeMatcher.get("/search/:query", searchHandler);
        routeMatcher.get("/search/", searchHandler);
        routeMatcher.get("/search", searchHandler);


        routeMatcher.get("/songs/:id", createGetSongHandler());

        routeMatcher.get("/new", createNewSongHandler());
        routeMatcher.put("/songs/:id", createPutSongHandler());
        routeMatcher.delete("/songs/:id", createDeleteSongHandler());

        routeMatcher.noMatch(createGetFileHandler());
        return routeMatcher;
    }

    private void allowCrossOrigin(HttpServerRequest request) {
        String origin = request.headers().get("Origin");
        if (origin != null) {
            HttpServerResponse response = request.response();
            response.putHeader("Access-Control-Allow-Origin", origin);
        }
    }

    private Handler<HttpServerRequest> createGetFileHandler() {
        return (request) -> {
            //if (checkDeniedAccess(request, false)) return;
            allowCrossOrigin(request);
            // Serve Files
            HttpServerResponse response = request.response();
            String path = request.path();
            String fileName = path.equals("/") ? "index.html" : path;
            Path localFilePath = Paths.get(webRoot.toString(), QueryStringDecoder.decodeComponent(fileName)).toAbsolutePath();
            logger.info("GET " + localFilePath);
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
        };
    }

    private Handler<HttpServerRequest> createPutSongHandler() {
        return (request) -> {
            allowCrossOrigin(request);
            if (checkDeniedAccess(request, true)) return;
            request.bodyHandler((body) -> {
                HttpServerResponse response = request.response();
                String songData = body.toString();
                try {

                    // indexes updated song
                    HtmlIndexer songIndexer = new HtmlIndexer();
                    Document document = songIndexer.indexSong(songData);

                    // constructs song info
                    String oldTitle = getSongIdFromUrl(request.headers().get("Referer"));
                    String newTitle = decodeUrl(request.params().get("id"));

                    // if title changed
                    if (oldTitle != null && newTitle.equals(oldTitle) == false) {
                        Path filePath = getSongPath(oldTitle);
                        vertx.fileSystem().delete(filePath.toString(), (ar) -> {/* do nothing */});

                        indexDatabase.removeDocument(oldTitle);
                    }

                    // prepares new document
                    document.add(new StringField("id", newTitle, Field.Store.YES));
                    indexDatabase.addOrUpdateDocument(document);

                    // removes song from vert.x cache (using old title)
                    ConcurrentSharedMap<Object, String> songs = vertx.sharedData().getMap("songs");
                    songs.remove(oldTitle);

                    // writes file to disk
                    Path filePath = getSongPath(newTitle);
                    vertx.fileSystem().writeFile(filePath.toString(), body, (ar) -> {
                        if (ar.succeeded()) {
                            response.end(newTitle);
                        } else {
                            logger.error("Failed to update the song", ar.cause());
                            response.setStatusCode(500);
                            response.end();

                            try {
                                Files.deleteIfExists(filePath);
                            } catch (IOException e) {
                                logger.warn("Can't delete file", e);
                            }
                        }
                    });
                } catch (Exception e) {
                    // TODO write error to client
                    logger.error("Error indexing song", e);
                    response.setStatusCode(500);
                    response.end();
                }
            });
        };
    }

    private Handler<HttpServerRequest> createDeleteSongHandler() {
        return (request) -> {
            allowCrossOrigin(request);
            if (checkDeniedAccess(request, true)) return;

            HttpServerResponse response = request.response();
            try {

                String id = request.params().get("id");
                String oldTitle = decodeUrl(id);

                // removes file
                Path filePath = getSongPath(oldTitle);
                vertx.fileSystem().delete(filePath.toString(), (ar) -> {/* do nothing */});

                // removes document
                indexDatabase.removeDocument(oldTitle);

                // removes song from vert.x cache (using old title)
                ConcurrentSharedMap<Object, String> songs = vertx.sharedData().getMap("songs");
                songs.remove(oldTitle);

                response.setStatusCode(200);
                response.end();

            } catch (Exception e) {
                // TODO write error to client
                logger.error("Error removing song", e);
                response.setStatusCode(500);
                response.end();
            }
        };
    }

    private Path getSongPath(String title) {
        return dataRoot.resolve("songs").resolve(title+ IndexDatabase.SONG_EXTENSION).toAbsolutePath();
    }

    private Handler<HttpServerRequest> createGetSongHandler() {
        return (request) -> {
                allowCrossOrigin(request);
                if (checkDeniedAccess(request, false)) return;
                String key = request.params().get("key");
                boolean admin = isAdministrator(key);

                // Serves song
                HttpServerResponse response = request.response();
                response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");

                String id = QueryStringDecoder.decodeComponent(request.params().get("id"));
                response.setChunked(true);

                response.write(Templates.getHeader(id + " - My SongBook"));
                response.write(Templates.getNavigation(key));
                if (showKeyCreationAlert) response.write(Templates.getKeyCreationAlert(administratorKey, request.path()));
                readHtmlSong(id, (e) -> {
                    if (e.succeeded()) {
                        response.write(e.result());
                        logger.trace("Serve Song " + id);
                    } else {
                        response.write(Templates.alertSongDoesntExist(id));
                        logger.error("Failed to read song " + id, e.cause());
                        response.setStatusCode(404);
                    }
                    response.write(Templates.getFooter());
                    response.end();
                });
            };
    }

    public static String decodeUriComponent(String s) {
        if (s == null) {
            return null;
        }
        try {
            return URLDecoder.decode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }


    private Handler<HttpServerRequest> createNewSongHandler() {
        return (request) -> {
                if (checkDeniedAccess(request, true)) return;
                boolean admin = isAdministrator(getRequestKey(request));

                // Serves song
                HttpServerResponse response = request.response();
                response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");

                response.setChunked(true);

                String key = getRequestKey(request);
                response.write(Templates.getHeader("New Song - My SongBook"));
                response.write(Templates.getNavigation(key));
                if (showKeyCreationAlert) response.write(Templates.getKeyCreationAlert(administratorKey, request.path()));
                final Path song = webRoot.resolve("js/NewSong.html");
                vertx.fileSystem().readFile(song.toString(), (e) -> {
                    response.write(e.result());
                    logger.trace("Serve Song 'New Song'");
                    response.write(Templates.getFooter());
                    response.end();
                });
            };
    }

    private Handler<HttpServerRequest> createSearchHandler() {
        return (request) -> {
                if (checkDeniedAccess(request, false)) return;
                String key = request.params().get("key");
                boolean admin = isAdministrator(key);

                // Serve all songs
                HttpServerResponse response = request.response();
                response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");

                String query = decodeUriComponent(request.params().get("query"));
                response.setChunked(true);
                String title = "My SongBook";
                if (query != null && !query.isEmpty()) {
                    title = query + " - " + title;
                }

                response.write(Templates.getHeader(title));
                response.write(Templates.getNavigation(key));
                if (showKeyCreationAlert) response.write(Templates.getKeyCreationAlert(administratorKey, request.path()));

                try {
                    indexDatabase.search(key, query, request);
                } catch (ParseException e) {
                    e.printStackTrace();
                    response.write("Wrong Query Syntax");
                } catch (IOException e) {
                    e.printStackTrace();
                    response.write("Internal Error");
                }
                response.write(Templates.getFooter());
                response.end();
            };
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
                final Path songPath = getSongPath(id);
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
        String host = System.getenv("HOST");
        if (host == null) host = System.getenv("HOSTNAME");
        return host == null ? DEFAULT_HOST : host;
    }

    /** Searches for keys on server to initialize administratorKey and userKey. */
    private void readKeys() {
        try {
            final Path administratorKeyPath = getDataRoot().resolve(ADMINISTRATOR_KEY_PATH);
            if (Files.exists(administratorKeyPath)) {
                final List<String> allLines = Files.readAllLines(administratorKeyPath);
                if (allLines.isEmpty() == false) {
                    administratorKey = allLines.get(allLines.size() - 1);

                    showKeyCreationAlert = Files.exists(getDataRoot().resolve(ADMINISTRATOR_ACTIVATED_PATH)) == false;
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

    /** Writes administratorKey and userKey to file system. */
    private void writeKeys() {
        if (administratorKey != null) {
            try {
                final Path administratorKeyPath = getDataRoot().resolve(ADMINISTRATOR_KEY_PATH);
                Files.write(administratorKeyPath, Collections.singleton(administratorKey));
                showKeyCreationAlert = true;

                final Path administratorActivatedPath = getDataRoot().resolve(ADMINISTRATOR_ACTIVATED_PATH);
                if (Files.exists(administratorActivatedPath)) {
                    Files.delete(administratorActivatedPath);
                }
            } catch (IOException e) {
                logger.error("Could not write administrator key", e);
            }
        }

        if (userKey != null) {
            try {
                final Path userKeyPath = getDataRoot().resolve("user.key");
                Files.write(userKeyPath, Collections.singleton(userKey));
                showKeyCreationAlert = true;
            } catch (IOException e) {
                logger.error("Could not write user key", e);
            }
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
        String key = request.params().get("key");
        if (isAdministrator(key)) {
            // gets administrator key, remove alert (if present)
            if (showKeyCreationAlert) {
                showKeyCreationAlert = false;
                try {
                    Files.createFile(getDataRoot().resolve(ADMINISTRATOR_ACTIVATED_PATH));
                } catch (IOException e) {
                    logger.error("Can't create file '"+ ADMINISTRATOR_ACTIVATED_PATH +"'", e);
                }
            }
            return false;
        }
        if (isUser(key) && needAdmin==false) return false;

        forbiddenAccess(request);
        return true;
    }

    private void forbiddenAccess(HttpServerRequest request) {
        HttpServerResponse response = request.response();
        response.setStatusCode(403);
        response.end("Access Forbidden '" + request.path() + "'");
    }

    public String getSongIdFromUrl(String url) {
        if (url == null) return null;
        String path = "/songs/";
        int songPathIndex = url.indexOf(path);
        if (songPathIndex < 0) return null;
        int paramIndex = url.indexOf('?', songPathIndex);
        int endIndex = paramIndex < 0 ? url.length() : paramIndex;
        return decodeUrl(url.substring(songPathIndex + path.length(), endIndex));
    }

    public String decodeUrl(String id) {
        try {
            return URLDecoder.decode(id, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // Do nothing but logging
            logger.error("Decoding error", e);
            return id;
        }
    }
}
