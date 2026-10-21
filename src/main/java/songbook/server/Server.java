package songbook.server;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.util.AttachmentKey;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import io.undertow.util.URLUtils;
import songbook.song.IndexDatabase;
import songbook.song.SongDatabase;
import songbook.song.SongUtils;

public class Server {

	public final static int DEFAULT_PORT = 8080;

	public final static String DEFAULT_HOST = "localhost";

	public final static String DEFAULT_WEB_ROOT = "web";

	public final static String DEFAULT_DATA_ROOT = "data";

	public static final String ADMINISTRATOR_KEY_PATH = "administrator.key";

	public static final String MIME_TEXT_HTML = "text/html";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String MIME_TEXT_SONG = "text/song";

	public static final AttachmentKey<Access.Identity> IDENTITY = AttachmentKey.create(Access.Identity.class);

	private Logger logger;

	private SongDatabase songDb;

	private IndexDatabase indexDb;

	private Access access;

	public void start() {
		logger = Logger.getLogger("Songbook");

		Path dataRoot = getDataRoot();
		Templates.setTemplatesPath(getWebRoot().resolve("templates"));

		try {
			if (!Files.exists(dataRoot))
				Files.createDirectories(dataRoot);
		} catch (IOException e) {
			error("Cannot start server data root isn't accessible.", e);
			return;
		}

		// Who may write: the shared login (USERS_URL) and/or the local key
		// (ADMIN_KEY_LOGIN=true), see Access
		String usersUrl = Access.usersUrl(System.getenv("USERS_URL"));
		if (System.getenv("USERS_URL") != null && usersUrl == null) {
			logger.severe("USERS_URL ignored: an http(s) address without path is expected");
		}
		String administratorKey = null;
		if ("true".equalsIgnoreCase(System.getenv("ADMIN_KEY_LOGIN"))) {
			administratorKey = readAdminKey();
			if (administratorKey == null)
				administratorKey = createAdminKey();
		}
		if (Files.exists(dataRoot.resolve("user.key"))) {
			logger.warning("user.key is no longer read: reading is public");
		}
		access = new Access(usersUrl, administratorKey);
		if (usersUrl == null && !access.keyLogin()) {
			info("Editing disabled: set USERS_URL (shared login) or ADMIN_KEY_LOGIN=true (administrator key)");
		} else {
			info("Editing through " + (usersUrl != null ? "the shared login at " + usersUrl : "")
					+ (usersUrl != null && access.keyLogin() ? " and " : "")
					+ (access.keyLogin() ? "the administrator key" : ""));
		}

		try {
			// initializes songDb
			songDb = new SongDatabase(getSongsPath());
		} catch (IOException e) {
			error("Can't initialize songs database in " + getSongsPath(), e);
		}

		Path index = getDataRoot().resolve("index");
		try {
			// initializes index.
			indexDb = new IndexDatabase(index, songDb);
		} catch (IOException e) {
			error("Can't initialize index in " + index, e);
		}

		// creates server
		Undertow undertow = createServer(pathTemplateHandler());
		undertow.start();
	}

	/**
	 * Create And initialize undertow and handlers stack
	 * 
	 * @param appHandler application handler to use for this server
	 * @return
	 */
	protected Undertow createServer(HttpHandler appHandler) {
		// Fourth Handler Session
		HttpHandler sessionHandler = sessionHandler(appHandler);
		// Third Handler exception
		HttpHandler exceptionHandler = exceptionHandler(sessionHandler);
		// Second Handler log
		HttpHandler logHandler = log(exceptionHandler);
		// Health, before the log (the Docker HEALTHCHECK calls it every 30s)
		// and before the session (public even when a user key is set)
		HttpHandler healthHandler = healthHandler(logHandler);
		// First Handler GracefulShutdown
		GracefulShutdownHandler gracefulShutdownHandler = Handlers.gracefulShutdown(healthHandler);

		var builder = Undertow.builder();
		final int port = getPort();
		final String host = getHost();
		builder.addHttpListener(port, host);
		builder.setHandler(gracefulShutdownHandler);

		info("Listens on '" + host + ":" + port + "'");

		return builder.build();
	}

	/**
	 * Serves GET /api/health (see {@link Health}), passes anything else on.
	 * 
	 * @param next
	 * @return
	 */
	protected HttpHandler healthHandler(HttpHandler next) {
		return exchange -> {
			if (!"/api/health".equals(exchange.getRequestPath()) || !Methods.GET.equals(exchange.getRequestMethod())) {
				next.handleRequest(exchange);
				return;
			}
			Health.Report report = Health.check(songDb, indexDb, getSongsPath(), getDataRoot());
			exchange.setStatusCode(report.statusCode());
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json; charset=utf-8");
			exchange.getResponseHeaders().put(Headers.CACHE_CONTROL, "no-store");
			exchange.getResponseSender().send(report.json());
		};
	}

	/**
	 * Log All requests
	 * 
	 * @param next
	 * @return
	 */
	protected HttpHandler log(HttpHandler next) {
		return (exchange) -> {
			long start = System.currentTimeMillis();
			next.handleRequest(exchange);
			long end = System.currentTimeMillis();
			long time = end - start < 0 ? 0 : end - start;
			info("[" + exchange.getRequestMethod() + "]" + exchange.getRequestURI() + " in " + time + " ms");
		};
	}

	/**
	 * Handles exceptions (including ServerException)
	 * 
	 * @param next
	 * @return
	 */
	protected HttpHandler exceptionHandler(HttpHandler next) {
		ExceptionHandler exceptionHandler = Handlers.exceptionHandler(next);
		exceptionHandler.addExceptionHandler(ServerException.class, (exchange) -> {
			Throwable exception = exchange.getAttachment(ExceptionHandler.THROWABLE);
			if (exception instanceof ServerException serverException) {
				serverException.serveError(getRole(exchange), exchange);
			} else {
				// TODO create real error message
				String message = exception.getMessage();
				exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
				exchange.getResponseSender().send(createMessage(message, isAskingForJson(exchange)));
			}
		});
		return exceptionHandler;
	}

	/**
	 * Attaches the identity of the request (see {@link Access}) and keeps the
	 * cookies in line with it. Reading is public: nothing is refused here.
	 * 
	 * @param next next Handler in the stack
	 * @return
	 */
	protected HttpHandler sessionHandler(HttpHandler next) {
		return exchange -> {
			Access.Identity identity = access.currentUser(exchange);
			exchange.putAttachment(IDENTITY, identity);
			access.updateCookies(exchange, identity);
			next.handleRequest(exchange);
		};
	}

	private HttpHandler pathTemplateHandler() {
		HttpHandler fallThrough = Handlers.resource(new FileResourceManager(getWebRoot().toFile(), 1024));

		//// To Update ////
		PathTemplateHandler pathHandler = new PathTemplateHandler(fallThrough);

		pathHandler.add("/", this::homePage); // Home Page

		pathHandler.add("/view/{id}", this::viewSongPage);
		pathHandler.add("/edit/{id}", adminAccess(this::editSongPage));
		pathHandler.add("/delete/{id}", adminAccess(this::deleteSongPage));
		pathHandler.add("/new", adminAccess(this::editSongPage));

		pathHandler.add("/search/{query}", this::searchPage);
		pathHandler.add("/search", this::searchPage);

		pathHandler.add("/artists/{artist}", this::songsByArtistPage);
		pathHandler.add("/artists", this::listArtistPage);

		pathHandler.add("/songs/{id}", this::restSong);

		pathHandler.add("/consoleApi", this::consoleApiPage);

		pathHandler.add("/signin", this::signinPage);
		pathHandler.add("/signout", this::signout);
		pathHandler.add("/admin/{section}/{command}", adminAccess(this::adminCommand));
		pathHandler.add("/admin", adminAccess(this::adminPage));

		return pathHandler;
	}

	private void homePage(HttpServerExchange exchange) throws Exception {
		searchPage(exchange);
	}

	private void viewSongPage(HttpServerExchange exchange) throws Exception {
		getSong(exchange);
	}

	private void editSongPage(final HttpServerExchange exchange) throws Exception {
		if (!exchange.getRequestMethod().equals(Methods.GET)) {
			throw ServerException.METHOD_NOT_ALLOWED;
		}
		String id = getParameter(exchange, "id");
		if (id != null) {
			id = URLEncoder.encode(id, "utf-8");
		}

		// Serves song
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");

		var out = new StringBuilder();
		String role = getRole(exchange);

		if (id != null && !id.isEmpty()) {

			String songContents = songDb.getSongContents(id);
			if (songContents == null)
				throw new SongNotFoundException(id);
			String title = SongUtils.getTitle(songContents);
			Templates.header(out, "Edit - " + title + " - My SongBook", role);
			Templates.editSong(out, id, songContents, role, sessionKeeper());
			Templates.footer(out);

			exchange.getResponseSender().send(out.toString());

		} else {
			Templates.header(out, "Create Song - My SongBook", role);
			Templates.editSong(out, "", Templates.newSong(new StringBuilder()), role, sessionKeeper());
			Templates.footer(out);
			exchange.getResponseSender().send(out.toString());
		}
	}

	private void deleteSongPage(HttpServerExchange exchange) throws Exception {
		deleteSong(exchange);
	}

	private void searchPage(final HttpServerExchange exchange) throws Exception {
		if (!exchange.getRequestMethod().equals(Methods.GET)) {
			throw ServerException.METHOD_NOT_ALLOWED;
		}
		// Serve all songs
		String query = getParameter(exchange, "query");
		String title = "My SongBook";
		if (query != null && !query.isEmpty()) {
			title = query + " - " + title;
		}

		var out = new StringBuilder();
		String mimeType = MimeParser.bestMatch(getHeader(exchange, Headers.ACCEPT), MIME_TEXT_SONG, MIME_TEXT_PLAIN,
				MIME_TEXT_HTML);
		switch (mimeType) {
			case MIME_TEXT_HTML -> {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");

				String role = getRole(exchange);
				Templates.header(out, title, role);
				var result = new StringBuilder();
				indexDb.search(query, result, mimeType);
				Templates.search(out, result, role);

				Templates.footer(out);
			}
			default -> indexDb.search(query, out, mimeType);
		}
		exchange.getResponseSender().send(out.toString());
	}

	private void restSong(final HttpServerExchange exchange) throws Exception {
		switch (exchange.getRequestMethod().toString()) {
			case Methods.GET_STRING -> this.getSong(exchange);
			case Methods.POST_STRING -> adminAccess(this::createSong).handleRequest(exchange);
			case Methods.PUT_STRING -> adminAccess(this::modifySong).handleRequest(exchange);
			case Methods.DELETE_STRING -> adminAccess(this::deleteSong).handleRequest(exchange);
			default -> throw ServerException.METHOD_NOT_ALLOWED;
		}
	}

	private void getSong(final HttpServerExchange exchange) throws Exception {
		if (!exchange.getRequestMethod().equals(Methods.GET)) {
			throw ServerException.METHOD_NOT_ALLOWED;
		}
		String id = URLEncoder.encode(getParameter(exchange, "id"), "utf-8");

		// Serves song
		String songContents = songDb.getSongContents(id);
		if (songContents == null)
			throw new SongNotFoundException(id);

		String mimeType = MimeParser.bestMatch(getHeader(exchange, Headers.ACCEPT), MIME_TEXT_SONG, MIME_TEXT_PLAIN,
				MIME_TEXT_HTML);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, mimeType);
		switch (mimeType) {
			case MIME_TEXT_HTML -> exchange.getResponseSender().send(htmlSong(exchange, id, songContents));
			default -> exchange.getResponseSender().send(songContents);
		}

		logger.info("Serve Song " + id);
	}

	private String htmlSong(HttpServerExchange exchange, String id, String songData) {
		var out = new StringBuilder();
		// Todo use a songmark object to extract title and then generate html
		String title = SongUtils.getTitle(songData);
		String role = getRole(exchange);
		Templates.header(out, title + " - My SongBook", role);
		Templates.viewSong(out, id, SongUtils.writeHtml(new StringBuilder(), songData), role);

		Templates.footer(out);
		return out.toString();
	}

	private void createSong(final HttpServerExchange exchange) throws Exception {
		String songData = ChannelUtil.getStringContents(exchange.getRequestChannel());

		// indexes updated song
		var document = SongUtils.indexSong(songData);
		String title = document.get("title");
		String artist = document.get("artist");

		if (title == null || title.isEmpty() || artist == null) {
			throw new MissingArgumentsException("title", "artist");
		}

		String id = songDb.generateId(title, artist);
		// prepares new document
		document.add(new StringField("id", id, Field.Store.YES));
		indexDb.addOrUpdateDocument(document);

		WritableByteChannel songChannel = songDb.writeChannelForSong(id);
		if (songChannel == null)
			throw new ServerException(500, "Can't write song");

		ChannelUtil.writeStringContents(songData, songChannel);

		exchange.getResponseSender().send(id);
	}

	private void modifySong(final HttpServerExchange exchange) throws Exception {
		String songData = ChannelUtil.getStringContents(exchange.getRequestChannel());

		// indexes updated song
		var document = SongUtils.indexSong(songData);

		String id = URLEncoder.encode(getParameter(exchange, "id"), "utf-8");

		// Verify that song exists
		if (!songDb.exists(id))
			throw ServerException.NOT_FOUND;

		// prepares new document
		document.add(new StringField("id", id, Field.Store.YES));
		indexDb.addOrUpdateDocument(document);

		WritableByteChannel songChannel = songDb.writeChannelForSong(id);
		if (songChannel == null)
			throw new ServerException(500, "Can't write song");

		ChannelUtil.writeStringContents(songData, songChannel);

		exchange.getResponseSender().send(id);
	}

	private void deleteSong(final HttpServerExchange exchange) throws Exception {
		if (!exchange.getRequestMethod().equals(Methods.DELETE)) {
			throw ServerException.METHOD_NOT_ALLOWED;
		}
		String id = URLEncoder.encode(getParameter(exchange, "id"), "utf-8");

		// Verify that song exists
		if (!songDb.exists(id))
			throw ServerException.NOT_FOUND;

		// removes file
		songDb.delete(id);

		String title = indexDb.getTitle(id);

		// removes document from index
		indexDb.removeDocument(id);

		String mimeType = MimeParser.bestMatch(getHeader(exchange, Headers.ACCEPT), MIME_TEXT_SONG, MIME_TEXT_PLAIN,
				MIME_TEXT_HTML);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, mimeType);
		switch (mimeType) {
			case MIME_TEXT_HTML -> {
				var out = new StringBuilder();
				Templates.header(out, "My SongBook", getRole(exchange));
				// show home page with message
				Templates.alertSongRemovedSuccessfully(out, title == null ? id : title);
				Templates.footer(out);
				exchange.getResponseSender().send(out.toString());
			}
			default -> exchange.getResponseSender().send(id);
		}
	}

	private void songsByArtistPage(final HttpServerExchange exchange) throws Exception {
		if (!exchange.getRequestMethod().equals(Methods.GET)) {
			throw ServerException.METHOD_NOT_ALLOWED;
		}
		String artist = getParameter(exchange, "artist");
		var out = new StringBuilder();
		String role = getRole(exchange);
		Templates.header(out, "Artists", role);
		var result = new StringBuilder();
		indexDb.songsByArtist(artist, result, MIME_TEXT_HTML);
		Templates.search(out, result, role);

		Templates.footer(out);

		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, MIME_TEXT_HTML);
		exchange.getResponseSender().send(out.toString());
	}

	private void listArtistPage(final HttpServerExchange exchange) throws Exception {
		if (!exchange.getRequestMethod().equals(Methods.GET)) {
			throw ServerException.METHOD_NOT_ALLOWED;
		}
		var out = new StringBuilder();
		String role = getRole(exchange);
		Templates.header(out, "Artists", role);
		var result = new StringBuilder();
		indexDb.listArtists(result, MIME_TEXT_HTML);
		Templates.search(out, result, role);

		Templates.footer(out);

		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, MIME_TEXT_HTML);
		exchange.getResponseSender().send(out.toString());
	}

	private void consoleApiPage(final HttpServerExchange exchange) throws ServerException {
		if (!exchange.getRequestMethod().equals(Methods.GET)) {
			throw ServerException.METHOD_NOT_ALLOWED;
		}
		var out = new StringBuilder();
		Templates.header(out, "Song Console Api", getRole(exchange));
		Templates.consoleApi(out);
		Templates.footer(out);

		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, MIME_TEXT_HTML);
		exchange.getResponseSender().send(out.toString());
	}

	/**
	 * The sign-in form of the administrator key, when ADMIN_KEY_LOGIN is on.
	 * Otherwise the administrator signs in through the shared login, which
	 * Caddy puts in front of /admin. The key only ever travels in the body of a
	 * POST, never in an address.
	 */
	private void signinPage(final HttpServerExchange exchange) throws Exception {
		boolean post = exchange.getRequestMethod().equals(Methods.POST);
		if (!post && !exchange.getRequestMethod().equals(Methods.GET)) {
			throw ServerException.METHOD_NOT_ALLOWED;
		}
		if (!access.keyLogin()) {
			if (post) {
				throw ServerException.NOT_FOUND;
			}
			redirect(exchange, "/admin");
			return;
		}
		String message = "";
		if (post) {
			String key = formValue(ChannelUtil.getStringContents(exchange.getRequestChannel()), "key");
			if (access.signIn(exchange, key)) {
				redirect(exchange, "/");
				return;
			}
			exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
			message = "<p class=\"field-error\" role=\"alert\">Wrong key.</p>";
		} else if (identity(exchange).admin()) {
			redirect(exchange, "/");
			return;
		}
		var out = new StringBuilder();
		Templates.header(out, "Sign in - My SongBook", getRole(exchange));
		Templates.signin(out, message);
		Templates.footer(out);

		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, MIME_TEXT_HTML);
		exchange.getResponseHeaders().put(Headers.CACHE_CONTROL, "no-store");
		exchange.getResponseSender().send(out.toString());
	}

	/**
	 * Ends the songbook session (key login) and hides the edit buttons, then
	 * ends the shared session at users when there is one.
	 */
	private void signout(final HttpServerExchange exchange) throws ServerException {
		if (!exchange.getRequestMethod().equals(Methods.POST)) {
			throw ServerException.METHOD_NOT_ALLOWED;
		}
		access.signOut(exchange);
		redirect(exchange, access.usersUrl() != null ? access.usersUrl() + "/api/logout" : "/");
	}

	private static void redirect(HttpServerExchange exchange, String location) {
		exchange.setStatusCode(StatusCodes.SEE_OTHER);
		exchange.getResponseHeaders().put(Headers.LOCATION, location);
		exchange.endExchange();
	}

	/** Reads a field of an application/x-www-form-urlencoded body. */
	private static String formValue(String body, String name) {
		for (String pair : body.split("&")) {
			int equals = pair.indexOf('=');
			if (equals > 0 && name.equals(URLDecoder.decode(pair.substring(0, equals), StandardCharsets.UTF_8))) {
				return URLDecoder.decode(pair.substring(equals + 1), StandardCharsets.UTF_8);
			}
		}
		return null;
	}

	/**
	 * The users session keeper, on the edit pages: an edit can outlast the 15
	 * min access token. Its banner is off, edit.js shows its own message.
	 */
	private String sessionKeeper() {
		return access.usersUrl() == null ? ""
				: "<script defer data-no-banner src=\"" + access.usersUrl() + "/session-keeper.js\"></script>";
	}

	private void adminPage(final HttpServerExchange exchange) throws ServerException {
		if (!exchange.getRequestMethod().equals(Methods.GET)) {
			throw ServerException.METHOD_NOT_ALLOWED;
		}
		var out = new StringBuilder();
		Templates.header(out, "SongBook Admin Page", getRole(exchange));
		Templates.admin(out);
		Templates.footer(out);

		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, MIME_TEXT_HTML);
		exchange.getResponseSender().send(out.toString());
	}

	private void adminCommand(final HttpServerExchange exchange) throws Exception {
		// Commands change the server state: POST only (a link or a prefetch
		// must not reindex)
		if (!exchange.getRequestMethod().equals(Methods.POST)) {
			throw ServerException.METHOD_NOT_ALLOWED;
		}
		var out = new StringBuilder();

		Templates.header(out, "Administration - My SongBook", getRole(exchange));

		String section = getParameter(exchange, "section");
		String command = getParameter(exchange, "command");
		switch (section) {
			case "index" -> {
				switch (command) {
					case "reset" -> {
						try {
							long start = System.currentTimeMillis();
							songDb.clearCache();
							indexDb.analyzeSongs();

							long end = System.currentTimeMillis();
							logger.info("Opened index in " + (end - start) + " milliseconds.");
							Templates.alertSongReindexed(out);
							Templates.admin(out);
						} catch (IOException e) {
							error("Can't initialize index in " + getDataRoot().resolve("index"), e);
							Templates.alertIndexingError(out);
							Templates.admin(out);
						}
					}
					default -> {
						Templates.alertCommandNotSupported(out);
						Templates.admin(out);
					}
				}
			}
			default -> throw ServerException.BAD_REQUEST;
		}
		Templates.footer(out);

		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, MIME_TEXT_HTML);
		exchange.getResponseSender().send(out.toString());
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
		if (host == null)
			host = System.getenv("HOSTNAME");
		return host == null ? DEFAULT_HOST : host;
	}

	// Security
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	/**
	 * Creates the administrator key of the key login. The key is never logged
	 * nor shown: it is read in the data directory.
	 */
	private String createAdminKey() {
		// 128 bits of randomness, hex-encoded
		byte[] bytes = new byte[16];
		SECURE_RANDOM.nextBytes(bytes);
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			sb.append(Character.forDigit((b >> 4) & 0xF, 16));
			sb.append(Character.forDigit(b & 0xF, 16));
		}
		String key = sb.toString();
		try {
			Files.write(getDataRoot().resolve(ADMINISTRATOR_KEY_PATH), List.of(key));
			info("Administrator key created in the data directory (" + ADMINISTRATOR_KEY_PATH + ")");
		} catch (IOException e) {
			error("Could not write administrator key", e);
		}
		return key;
	}

	/**
	 * Reads the administrator key of the key login, or null.
	 */
	private String readAdminKey() {
		try {
			final Path administratorKeyPath = getDataRoot().resolve(ADMINISTRATOR_KEY_PATH);
			if (Files.exists(administratorKeyPath)) {
				final List<String> allLines = Files.readAllLines(administratorKeyPath);
				for (int i = allLines.size() - 1; i >= 0; i--) {
					if (!allLines.get(i).isBlank()) {
						return allLines.get(i).strip();
					}
				}
			}
		} catch (IOException e) {
			error("Could not read administrator key", e);
		}
		return null;
	}

	private Access.Identity identity(HttpServerExchange exchange) {
		Access.Identity identity = exchange.getAttachment(IDENTITY);
		return identity == null ? Access.Identity.ANONYMOUS : identity;
	}

	/** "admin" shows the edit buttons; the access itself is checked by {@link #adminAccess}. */
	private String getRole(HttpServerExchange exchange) {
		return access != null && access.showsAdmin(exchange, identity(exchange)) ? "admin" : "user";
	}

	protected boolean isAskingForJson(HttpServerExchange exchange) {
		HeaderValues values = exchange.getRequestHeaders().get(Headers.ACCEPT);
		return values != null && values.getFirst().contains("application/json");
	}

	protected String createMessage(String message, boolean json) {
		return json ? """
				{ "message": "%s"}""".formatted(message) : message;
	}

	protected String getHeader(HttpServerExchange exchange, HttpString header) {
		Deque<String> deque = exchange.getRequestHeaders().get(header);
		return deque == null ? null : deque.element();
	}

	protected String getParameter(HttpServerExchange exchange, String parameter) {
		StringBuilder sb = new StringBuilder();
		Deque<String> deque = exchange.getQueryParameters().get(parameter);
		if (deque != null) {
			URLUtils.decode(deque.element(), "utf-8", true, sb);
			return sb.toString();
		}
		return null;
	}

	private HttpHandler adminAccess(HttpHandler handler) {
		return exchange -> {
			Access.Identity identity = identity(exchange);
			if (identity.admin()) {
				handler.handleRequest(exchange);
			} else {
				// 401: nobody signed in; 403: signed in, but not as administrator
				throw new ServerException(identity.authenticated() ? StatusCodes.FORBIDDEN : StatusCodes.UNAUTHORIZED);
			}
		};
	}

	private HttpHandler methodFilterHandler(HttpHandler handler, HttpString method, HttpHandler next) {
		return exchange -> {
			if (method.equals(exchange.getRequestMethod())) {
				handler.handleRequest(exchange);
			} else if (next != null) {
				next.handleRequest(exchange);
			} else {
				throw new ServerException(StatusCodes.METHOD_NOT_ALLOWED);
			}
		};
	}

	private void info(String message) {
		logger.log(Level.INFO, message);
	}

	private void error(String message, IOException e) {
		logger.log(Level.SEVERE, message, e);
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}

}
