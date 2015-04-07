package songbook.server;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;
import songbook.index.IndexDatabase;
import songbook.index.SongUtils;
import songbook.index.SongDatabase;

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

	public static final String MIME_TEXT_HTML = "text/html";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String MIME_TEXT_SONG = "text/song";


	private Logger logger;

	private SongDatabase songDb;

	private IndexDatabase indexDb;

	private boolean showKeyCreationAlert = false;
	private String administratorKey = null;
	private String userKey = null;

	@Override
	public void start() {
		logger = getContainer().logger();

		Path dataRoot = getDataRoot();

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

		// initialize songDb
		songDb = new SongDatabase(vertx, getSongsPath());

		// initializes index.
		try {
			indexDb = new IndexDatabase(getDataRoot().resolve("index"), songDb);
		} catch (IOException e) {
			logger.error("Can't initialize index in " + dataRoot.resolve("index"), e);
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

	private void matchRequest(RouteMatcher routeMatcher) {
		routeMatcher.get("/", this::search); // Home Page
		routeMatcher.noMatch(this::serveFile);
		routeMatcher.get("/new", this::songForm);

		routeMatcher.get("/search/:query", this::search);
		routeMatcher.get("/search/", this::search);
		routeMatcher.get("/search", this::search);

		routeMatcher.get("/songs/:id", this::getSong);
		routeMatcher.post("/songs", this::createSong);
		routeMatcher.post("/songs/", this::createSong);
		routeMatcher.put("/songs/:id", this::modifySong);
		routeMatcher.delete("/songs/:id", this::deleteSong);

		routeMatcher.get("/admin/index/:command", this::adminIndex);
	}

	private void serveFile(HttpServerRequest request) {
		//if (checkDeniedAccess(request, false)) return;
		allowCrossOrigin(request);
		// Serve Files
		HttpServerResponse response = request.response();
		String path = request.path().equals("/") ? "index.html" : request.path().substring(1);
		Path localFilePath = getWebRoot().resolve(QueryStringDecoder.decodeComponent(path)).toAbsolutePath();
		logger.info("GET " + localFilePath);
		String type = "text/plain";
		if (path.endsWith(".js")) {
			type = "application/javascript";
		} else if (path.endsWith(".css")) {
			type = "text/css";
		} else if (path.endsWith(".html")) {
			type = "text/html";
		}

		response.putHeader(HttpHeaders.CONTENT_TYPE, type);
		response.sendFile(localFilePath.toString());
	}

	private void search(HttpServerRequest request) {
		if (checkDeniedAccess(request, false)) return;
		String key = request.params().get("key");

		// Serve all songs

		String query = QueryStringDecoder.decodeComponent(request.params().get("query"));
		String title = "My SongBook";
		if (query != null && !query.isEmpty()) {
			title = query + " - " + title;
		}

		HttpServerResponse response = request.response();
		try {
			String mimeType = MimeParser.bestMatch(request.headers().get(HttpHeaders.ACCEPT), MIME_TEXT_SONG, MIME_TEXT_PLAIN, MIME_TEXT_HTML);
			switch (mimeType) {
				case  MIME_TEXT_HTML:
					response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
					response.setChunked(true);
					response.write(Templates.getHeader(title));
					response.write(Templates.getNavigation(key));
					if (showKeyCreationAlert) response.write(Templates.getKeyCreationAlert(administratorKey, request.path()));
					indexDb.search(key, query, request, mimeType);
					response.write(Templates.getFooter());
					response.end();
					break;
				default:
					response.setChunked(true);
					indexDb.search(key, query, request, mimeType);
					response.end();
					break;

			}
		} catch (ParseException e) {
			e.printStackTrace();
			response.end("Wrong Query Syntax");
		} catch (IOException e) {
			e.printStackTrace();
			response.end("Internal Error");
		}

	}

	private void getSong(HttpServerRequest request) {
		allowCrossOrigin(request);
		if (checkDeniedAccess(request, false)) return;
		String id = request.params().get("id");

		// Serves song
		HttpServerResponse response = request.response();
		songDb.readSong(id, (handler) -> {
			if (handler.succeeded()) {
				String mimeType = MimeParser.bestMatch(request.headers().get(HttpHeaders.ACCEPT), MIME_TEXT_SONG, MIME_TEXT_PLAIN, MIME_TEXT_HTML);
				switch (mimeType) {
					case MIME_TEXT_HTML:
						response.putHeader(HttpHeaders.CONTENT_TYPE, mimeType);
						response.end(htmlSong(request.params().get("key"), id, handler.result(), request.path()));
						break;
					default:
					case MIME_TEXT_PLAIN:
					case MIME_TEXT_SONG:
						response.putHeader(HttpHeaders.CONTENT_TYPE, mimeType);
						response.end(handler.result());
						break;
				}
				logger.trace("Serve Song " + id);
			} else {
				response.write(Templates.alertSongDoesNotExist(id));
				logger.error("Failed to read song " + id, handler.cause());
				response.setStatusCode(404);
			}
		});
	}

	private String htmlSong(String key, String id, String songData, String path) {
		StringBuilder sb = new StringBuilder();
		sb.append(Templates.getHeader(id + " - My SongBook"));
		sb.append(Templates.getNavigation(key));
		if (showKeyCreationAlert) sb.append(Templates.getKeyCreationAlert(administratorKey, path));
		SongUtils.writeHtml(sb, songData);
		sb.append(Templates.getFooter());
		return sb.toString();
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
		final Path song = getWebRoot().resolve("NewSong.html");
		vertx.fileSystem().readFile(song.toString(), (e) -> {
			response.write(e.result());
			logger.trace("Serve Song 'New Song'");
			response.write(Templates.getFooter());
			response.end();
		});
	}

	private void createSong(HttpServerRequest request) {
		allowCrossOrigin(request);
		if (checkDeniedAccess(request, true)) return;
		request.bodyHandler((body) -> {
			HttpServerResponse response = request.response();
			String songData = body.toString();
			try {

				// indexes updated song
				Document document = SongUtils.indexSong(songData);
				String title = document.get("title");
				String artist = document.get("artist");

				if (title == null || title.isEmpty() || artist == null ) {
					response.setStatusCode(400);
					response.end("You must provide a title and an artist information");
					return;
				}
				String id = songDb.generateId(title, artist);
				// prepares new document
				document.add(new StringField("id", id, Field.Store.YES));
				indexDb.addOrUpdateDocument(document);

				// writes song to database
				songDb.writeSong(id, songData, (ar) -> {
					if (ar.succeeded()) {
						response.end(id);
					} else {
						logger.error("Failed to create the song", ar.cause());
						response.setStatusCode(500);
						response.end();
					}
				});



			} catch (Exception e) {
				logger.error("Error indexing song", e);
				response.setStatusCode(500);
				response.end();
			}
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
				Document document = SongUtils.indexSong(songData);

				String id = request.params().get("id");

				// Verify that song exists
				if (songDb.exists(id)) {
					// prepares new document
					document.add(new StringField("id", id, Field.Store.YES));
					indexDb.addOrUpdateDocument(document);

					// writes song to database
					songDb.writeSong(id, songData, (ar) -> {
						if (ar.succeeded()) {
							response.end(id);
						} else {
							logger.error("Failed to update the song", ar.cause());
							response.setStatusCode(500);
							response.end();
						}
					});
				} else {
					response.setStatusCode(400);
					response.end("The song doesn't exist and cannot be updated");
				}


			} catch (Exception e) {
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
			String id = request.params().get("id");

			// Verify that song exists
			if (songDb.exists(id)) {
				// removes file
				songDb.delete(id);

				// removes document from index
				indexDb.removeDocument(id);

				response.setStatusCode(200);

				response.end(id);
			} else {
				response.setStatusCode(400);
				response.end("The song doesn't exist and cannot be deleted");
			}
		} catch (Exception e) {
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
					long start = System.currentTimeMillis();
					songDb.clearCache();
					indexDb.analyzeSongs();

					long end = System.currentTimeMillis();
					logger.info("Opened index in " + (end - start) + " milliseconds.");
					response.write(Templates.alert("success", "Songs are re-indexed."));
					response.setStatusCode(200);
				} catch (IOException e) {
					logger.error("Can't initialize index in " + getDataRoot().resolve("index"), e);
					response.write(Templates.alert("danger", "An error occurred while indexing songs."));
					response.setStatusCode(500);
				}
				break;
			default:
				response.write(Templates.alert("danger", "Command '" + command + "' isn't supported for index."));
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

	private Path getSongsPath() {
		final String songRoot = System.getenv("SONGS_ROOT");
		return songRoot == null ? getDataRoot().resolve("songs") : Paths.get(songRoot);
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
		response.write(Templates.alert("danger", "Access Forbidden '" + request.path() + "'"));
		response.write(Templates.getFooter());
		response.end();
	}
}
