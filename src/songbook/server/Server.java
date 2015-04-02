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
import java.math.BigInteger;
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
        if (administratorKey == null) createAdminKey();

        // initializes index.
        try {
            initializeIndex(false);
        } catch (IOException e) {
            logger.error("Can't initialize index in " + dataRoot.resolve("index"));
        }

        //installs matcher to server song
        RouteMatcher routeMatcher = new RouteMatcher();
        matchRequest(routeMatcher);
        httpServer.requestHandler(routeMatcher);

        final int port = getPort();
        final String host = getHost();
        logger.info("Starting server on '" + host + ":" + port + "'.");
        httpServer.listen(port, host);
    }

    private void initializeIndex(boolean forceReindex) throws IOException {
        long start = System.currentTimeMillis();
        Path songs = dataRoot.resolve("songs");
        if (Files.notExists(songs)) Files.createDirectories(songs);
        indexDatabase = new IndexDatabase(dataRoot.resolve("index"), songs, forceReindex);
        long end = System.currentTimeMillis();
        logger.info("Opened index in " + (end - start) + " milliseconds.");
    }

    private void matchRequest(RouteMatcher routeMatcher) {
        routeMatcher.get("/",this::search); // Home Page
        routeMatcher.noMatch(this:: serveFile);
        routeMatcher.get("/new", this::songForm);

        routeMatcher.get("/search/:query", this::search);
        routeMatcher.get("/search/", this::search);
        routeMatcher.get("/search", this::search);

        routeMatcher.get("/songs/:title", this::getSong);
        routeMatcher.put("/songs/:title", this::modifySong);
        routeMatcher.delete("/songs/:title", this::deleteSong);

        routeMatcher.get("/admin/index/:command", this::adminIndex);
    }

    private void serveFile(HttpServerRequest request) {
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
    }

    private Path getSongPath(String title) {
        String filename = SongUtil.getIdFromTitle(title) + IndexDatabase.SONG_EXTENSION ;
        return dataRoot.resolve("songs").resolve(filename).toAbsolutePath();
    }

    public String getSongTitle(HttpServerRequest request) {
        return QueryStringDecoder.decodeComponent(request.params().get("title"));
    }

    public String getRefererSongTitle(HttpServerRequest request) {
        String url = request.headers().get("Referer");
        if (url == null) return null;
        String path = "/songs/";
        int songPathIndex = url.indexOf(path);
        if (songPathIndex < 0) return null;
        int paramIndex = url.indexOf('?', songPathIndex);
        int endIndex = paramIndex < 0 ? url.length() : paramIndex;
        return QueryStringDecoder.decodeComponent(url.substring(songPathIndex + path.length(), endIndex));

    }

    public void readHtmlSong(String title, Handler<AsyncResult<String>> handler) {
        String id = SongUtil.getIdFromTitle(title);

        DefaultFutureResult<String> event = new DefaultFutureResult<>();
        event.setHandler(handler);
        try {
            ConcurrentSharedMap<Object, String> songs = vertx.sharedData().getMap("songs");
            String song = songs.get(id);
            if (song != null) {
                event.setResult(song);
            } else {
                final Path songPath = getSongPath(title);
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

    private void search(HttpServerRequest request) {
        if (checkDeniedAccess(request, false)) return;
        String key = request.params().get("key");

        // Serve all songs
        HttpServerResponse response = request.response();
        response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");

        String query = QueryStringDecoder.decodeComponent(request.params().get("query"));
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
    }

    private void getSong(HttpServerRequest request) {
        allowCrossOrigin(request);
        if (checkDeniedAccess(request, false)) return;
        String key = request.params().get("key");

        // Serves song
        HttpServerResponse response = request.response();
        response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");

        String title = getSongTitle(request);
        response.setChunked(true);

        response.write(Templates.getHeader(title + " - My SongBook"));
        response.write(Templates.getNavigation(key));
        if (showKeyCreationAlert) response.write(Templates.getKeyCreationAlert(administratorKey, request.path()));
        readHtmlSong(title, (e) -> {
            if (e.succeeded()) {
                response.write(e.result());
                logger.trace("Serve Song " + title);
            } else {
                response.write(Templates.alertSongDoesNotExist(title));
                logger.error("Failed to read song " + title, e.cause());
                response.setStatusCode(404);
            }
            response.write(Templates.getFooter());
            response.end();
        });
    }

    private void songForm(HttpServerRequest request) {
        if (checkDeniedAccess(request, true)) return;
        String key = request.params().get("key");

        // Serves song
        HttpServerResponse response = request.response();
        response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");

        response.setChunked(true);

        response.write(Templates.getHeader("New Song - My SongBook"));
        response.write(Templates.getNavigation(key));
        if (showKeyCreationAlert) response.write(Templates.getKeyCreationAlert(administratorKey, request.path()));
        final Path song = webRoot.resolve("NewSong.html");
        vertx.fileSystem().readFile(song.toString(), (e) -> {
            response.write(e.result());
            logger.trace("Serve Song 'New Song'");
            response.write(Templates.getFooter());
            response.end();
        });
    }

    private void modifySong(HttpServerRequest request) {
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
                String oldTitle = getRefererSongTitle(request);
                String newTitle = getSongTitle(request);
                String newId = SongUtil.getIdFromTitle(newTitle);

                // if title changed
                if (oldTitle != null && newTitle.equals(oldTitle) == false) {
                    Path filePath = getSongPath(oldTitle);
                    vertx.fileSystem().delete(filePath.toString(), (ar) -> {/* do nothing */});

                    String oldId = SongUtil.getIdFromTitle(oldTitle);
                    indexDatabase.removeDocument(oldId);

                    // removes song from vert.x cache (using old title)
                    ConcurrentSharedMap<Object, String> songs = vertx.sharedData().getMap("songs");
                    songs.remove(oldId);
                }

                // prepares new document
                document.add(new StringField("id", newId, Field.Store.YES));
                indexDatabase.addOrUpdateDocument(document);


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
    }

    private void deleteSong(HttpServerRequest request) {
        allowCrossOrigin(request);
        if (checkDeniedAccess(request, true)) return;

        HttpServerResponse response = request.response();
        try {

            String title = getSongTitle(request);
            String id = SongUtil.getIdFromTitle(title);

            // removes file
            Path filePath = getSongPath(title);
            vertx.fileSystem().delete(filePath.toString(), (ar) -> {/* do nothing */});

            // removes document
            indexDatabase.removeDocument(id);

            // removes song from vert.x cache (using old title)
            ConcurrentSharedMap<Object, String> songs = vertx.sharedData().getMap("songs");
            songs.remove(id);

            response.setStatusCode(200);
            response.end();

        } catch (Exception e) {
            // TODO write error to client
            logger.error("Error removing song", e);
            response.setStatusCode(500);
            response.end();
        }
    }

    private void adminIndex(HttpServerRequest request) {
        if (checkDeniedAccess(request, true)) return;
        String key = request.params().get("key");


        HttpServerResponse response = request.response();
        response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");

        response.setChunked(true);

        response.write(Templates.getHeader("Administration - My SongBook"));
        response.write(Templates.getNavigation(key));

        String command = QueryStringDecoder.decodeComponent(request.params().get("command"));
        switch (command) {
            case "reset":
                try {
                    initializeIndex(true);
                    response.write(Templates.alert("success", "Songs are re-indexed."));
                    response.setStatusCode(200);
                } catch (IOException e) {
                    logger.error("Can't initialize index in " + dataRoot.resolve("index"));
                    response.write(Templates.alert("danger", "An error occurred while indexing songs."));
                    response.setStatusCode(500);
                }
                break;
            default:
                response.write(Templates.alert("danger", "Command '"+ command +"' isn't supported for index."));
                response.setStatusCode(500);
                break;
        }
        response.write(Templates.getFooter());
        response.end();
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
        if (portString != null) {
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


    // Security

    private void allowCrossOrigin(HttpServerRequest request) {
        String origin = request.headers().get("Origin");
        if (origin != null) {
            HttpServerResponse response = request.response();
            response.putHeader("Access-Control-Allow-Origin", origin);
        }
    }

    private void createAdminKey() {
        // creates administrator key when it's null
        long timestamp = System.currentTimeMillis();
        String timestampString = Long.toHexString(timestamp);
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(timestampString.getBytes(), 0, timestampString.length());
            administratorKey = new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            administratorKey = timestampString;
        }
        logger.info("Created administrator key: '" + administratorKey + "'.");
        writeKeys();
    }

    /**
     * Searches for keys on server to initialize administratorKey and userKey.
     */
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

    /**
     * Writes administratorKey and userKey to file system.
     */
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

    /**
     * Checks if key allows to be administrator
     */
    private boolean isAdministrator(String requestKey) {
        return administratorKey == null || administratorKey.equals(requestKey);
    }

    /**
     * Checks if key allows to be user
     */
    private boolean isUser(String requestKey) {
        return userKey == null || userKey.equals(requestKey);
    }

    /**
     * Checks if request need to be denied, returns true if access is denied.
     */
    private boolean checkDeniedAccess(HttpServerRequest request, boolean needAdmin) {
        String key = request.params().get("key");
        if (isAdministrator(key)) {
            // gets administrator key, remove alert (if present)
            if (showKeyCreationAlert) {
                showKeyCreationAlert = false;
                try {
                    Files.createFile(getDataRoot().resolve(ADMINISTRATOR_ACTIVATED_PATH));
                } catch (IOException e) {
                    logger.error("Can't create file '" + ADMINISTRATOR_ACTIVATED_PATH + "'", e);
                }
            }
            return false;
        }
        if (isUser(key) && needAdmin == false) return false;

        forbiddenAccess(request);
        return true;
    }

    private void forbiddenAccess(HttpServerRequest request) {
        HttpServerResponse response = request.response();
        response.setStatusCode(403);
        response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");

        response.setChunked(true);

        response.write(Templates.getHeader("Forbidden - My SongBook"));
        response.write(Templates.getNavigation(null));
        response.write(Templates.alert("danger","Access Forbidden '" + request.path() + "'"));
        response.write(Templates.getFooter());
        response.end();
    }
}
