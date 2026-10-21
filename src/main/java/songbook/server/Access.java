package songbook.server;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Deque;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

/**
 * Who is asking. Reading is public; writing is for the administrator, who
 * comes from the shared login (users, through Caddy) or, as a fallback, from
 * the local administrator key. This class is the only place that reads the
 * identity of a request: checking the users JWT here later (decision 0016 in
 * the knowledge base) would change nothing else.
 */
final class Access {

	/** The person behind a request; {@link #ANONYMOUS} for a reader. */
	record Identity(String name, boolean admin) {
		static final Identity ANONYMOUS = new Identity(null, false);

		boolean authenticated() {
			return name != null;
		}
	}

	static final HttpString REMOTE_USER = new HttpString("Remote-User");
	static final HttpString REMOTE_GROUPS = new HttpString("Remote-Groups");
	static final String ADMIN_GROUP = "admin";

	/** Session of the key login: an expiry and its signature, never the key. */
	static final String SESSION_COOKIE = "songbook-session";
	/** Shows the edit buttons on the public pages; grants nothing. */
	static final String DISPLAY_COOKIE = "songbook-admin";
	/** Older versions stored the key itself in this cookie: removed on sight. */
	static final String LEGACY_COOKIE = "SessionKey";

	static final int SESSION_DAYS = 30;

	private static final String HMAC = "HmacSHA256";

	private final String usersUrl;

	private final String adminKey;

	/**
	 * @param usersUrl address of the shared login (https://users.llgmusic.net),
	 *                 or null: the Remote-User and Remote-Groups headers are then
	 *                 ignored. When set, the proxy in front of songbook must
	 *                 remove them from every client request.
	 * @param adminKey the administrator key when the key login is enabled, or
	 *                 null
	 */
	Access(String usersUrl, String adminKey) {
		this.usersUrl = usersUrl;
		this.adminKey = adminKey;
	}

	/**
	 * Reads USERS_URL: an http(s) address without path, or nothing. An invalid
	 * value is refused (null) rather than half trusted.
	 */
	static String usersUrl(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			URI uri = new URI(value.strip());
			boolean http = "https".equals(uri.getScheme()) || "http".equals(uri.getScheme());
			boolean bare = uri.getRawQuery() == null && uri.getRawFragment() == null && uri.getRawUserInfo() == null
					&& (uri.getRawPath() == null || uri.getRawPath().isEmpty() || uri.getRawPath().equals("/"));
			if (!http || uri.getHost() == null || !bare) {
				return null;
			}
			return uri.getScheme() + "://" + uri.getRawAuthority();
		} catch (Exception e) {
			return null;
		}
	}

	String usersUrl() {
		return usersUrl;
	}

	boolean keyLogin() {
		return adminKey != null;
	}

	Identity currentUser(HttpServerExchange exchange) {
		if (usersUrl != null) {
			String user = header(exchange, REMOTE_USER);
			if (user != null && !user.isBlank()) {
				String groups = header(exchange, REMOTE_GROUPS);
				boolean admin = groups != null && Arrays.stream(groups.split(","))
						.anyMatch(group -> ADMIN_GROUP.equals(group.strip()));
				return new Identity(user.strip(), admin);
			}
		}
		if (adminKey != null && validSession(cookie(exchange, SESSION_COOKIE))) {
			return new Identity(ADMIN_GROUP, true);
		}
		return Identity.ANONYMOUS;
	}

	/** The edit buttons are shown to the administrator, or after a visit as one. */
	boolean showsAdmin(HttpServerExchange exchange, Identity identity) {
		return identity.admin() || "1".equals(cookie(exchange, DISPLAY_COOKIE));
	}

	/**
	 * Keeps the display cookie in line with the identity and removes the
	 * cookie of the older versions, which held the key itself.
	 */
	void updateCookies(HttpServerExchange exchange, Identity identity) {
		boolean displayed = "1".equals(cookie(exchange, DISPLAY_COOKIE));
		if (identity.admin() && !displayed) {
			exchange.setResponseCookie(cookie(DISPLAY_COOKIE, "1", SESSION_DAYS * 24 * 3600, "Lax"));
		} else if (identity.authenticated() && !identity.admin() && displayed) {
			exchange.setResponseCookie(cookie(DISPLAY_COOKIE, "", 0, "Lax"));
		}
		if (cookie(exchange, LEGACY_COOKIE) != null) {
			exchange.setResponseCookie(cookie(LEGACY_COOKIE, "", 0, "Lax"));
		}
	}

	/**
	 * Checks a key typed in the sign-in form and opens the session. The
	 * comparison takes the same time whatever the key.
	 */
	boolean signIn(HttpServerExchange exchange, String key) {
		if (adminKey == null || key == null) {
			return false;
		}
		boolean valid = MessageDigest.isEqual(adminKey.getBytes(StandardCharsets.UTF_8),
				key.strip().getBytes(StandardCharsets.UTF_8));
		if (valid) {
			long expiry = System.currentTimeMillis() / 1000 + SESSION_DAYS * 24 * 3600L;
			exchange.setResponseCookie(cookie(SESSION_COOKIE, expiry + "." + sign(expiry), SESSION_DAYS * 24 * 3600, "Strict"));
			exchange.setResponseCookie(cookie(DISPLAY_COOKIE, "1", SESSION_DAYS * 24 * 3600, "Lax"));
		}
		return valid;
	}

	void signOut(HttpServerExchange exchange) {
		exchange.setResponseCookie(cookie(SESSION_COOKIE, "", 0, "Strict"));
		exchange.setResponseCookie(cookie(DISPLAY_COOKIE, "", 0, "Lax"));
	}

	private boolean validSession(String value) {
		if (value == null) {
			return false;
		}
		int dot = value.indexOf('.');
		if (dot <= 0) {
			return false;
		}
		long expiry;
		try {
			expiry = Long.parseLong(value.substring(0, dot));
		} catch (NumberFormatException e) {
			return false;
		}
		if (expiry < System.currentTimeMillis() / 1000) {
			return false;
		}
		return MessageDigest.isEqual(sign(expiry).getBytes(StandardCharsets.US_ASCII),
				value.substring(dot + 1).getBytes(StandardCharsets.US_ASCII));
	}

	/** The session is signed with the key: a new key ends every session. */
	private String sign(long expiry) {
		try {
			Mac mac = Mac.getInstance(HMAC);
			mac.init(new SecretKeySpec(adminKey.getBytes(StandardCharsets.UTF_8), HMAC));
			byte[] signature = mac.doFinal(("songbook-session:" + expiry).getBytes(StandardCharsets.US_ASCII));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Cookie cookie(String name, String value, int maxAge, String sameSite) {
		Cookie cookie = new CookieImpl(name, value);
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setSameSiteMode(sameSite);
		return cookie;
	}

	private static String header(HttpServerExchange exchange, HttpString name) {
		Deque<String> values = exchange.getRequestHeaders().get(name);
		return values == null ? null : values.peekFirst();
	}

	/**
	 * Reads a request cookie. The header is parsed by hand: Undertow collects
	 * cookies into a set where same-name cookies are equal, and a browser can
	 * send a stale duplicate at a narrower path next to the current one: for
	 * the session, a valid value wins over the others.
	 */
	private String cookie(HttpServerExchange exchange, String name) {
		Deque<String> headers = exchange.getRequestHeaders().get(Headers.COOKIE);
		if (headers == null) {
			return null;
		}
		String found = null;
		for (String header : headers) {
			for (String pair : header.split(";")) {
				int equals = pair.indexOf('=');
				if (equals == -1 || !name.equals(pair.substring(0, equals).strip())) {
					continue;
				}
				String value = pair.substring(equals + 1).strip();
				if (found == null || (SESSION_COOKIE.equals(name) && adminKey != null && validSession(value))) {
					found = value;
				}
			}
		}
		return found;
	}
}
