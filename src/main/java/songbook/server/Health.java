package songbook.server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import songbook.song.IndexDatabase;
import songbook.song.SongDatabase;

/**
 * Health of the server, by the contract shared by the mesnos services
 * ({@code ecosysteme/sante.md} in mesnos/carnet.mesnos.ovh): {@code GET /api/health}
 * answers 200 "ok" or "degraded", 503 "down", with the list of checks.
 * <p>
 * Vital: the song database and the search index (search is the home page).
 * Not vital: writing to the data and song folders (reading works, editing fails).
 * <p>
 * The route is public: reasons are short sentences, never paths or raw errors.
 */
public class Health {

	public record Check(String name, boolean ok, boolean critical, String reason) {
	}

	public record Report(int statusCode, String json) {
	}

	/** The commit, passed at image build time (APP_VERSION). */
	static String version() {
		String version = System.getenv("APP_VERSION");
		return version == null || version.isBlank() ? "dev" : version;
	}

	public static Report check(SongDatabase songDb, IndexDatabase indexDb, Path songsPath, Path dataRoot) {
		return report(List.of(
				checkSongs(songDb, songsPath),
				checkIndex(indexDb),
				checkWritable(songsPath, dataRoot)));
	}

	static Check checkSongs(SongDatabase songDb, Path songsPath) {
		if (songDb == null) {
			return new Check("songs", false, true, "the song database failed to initialize (see the logs)");
		}
		if (!Files.isDirectory(songsPath) || !Files.isReadable(songsPath)) {
			return new Check("songs", false, true, "the song folder is missing or not readable");
		}
		return new Check("songs", true, true, null);
	}

	static Check checkIndex(IndexDatabase indexDb) {
		if (indexDb == null) {
			return new Check("index", false, true,
					"the search index failed to open, it may be corrupt: remove the index folder and restart, it is rebuilt from the songs (see the logs)");
		}
		try {
			indexDb.checkReadable();
		} catch (Exception e) {
			return new Check("index", false, true, "the search index cannot be read (see the logs)");
		}
		return new Check("index", true, true, null);
	}

	static Check checkWritable(Path songsPath, Path dataRoot) {
		for (Path dir : List.of(songsPath, dataRoot)) {
			if (!dirWritable(dir)) {
				String what = dir.equals(songsPath) ? "song" : "data";
				return new Check("data-writable", false, false,
						"the " + what + " folder is not writable: editing songs will fail");
			}
		}
		return new Check("data-writable", true, false, null);
	}

	/** Files.isWritable is not enough on read-only mounts: create and delete a probe. */
	static boolean dirWritable(Path dir) {
		Path probe = dir.resolve(".write-probe-" + UUID.randomUUID());
		try {
			Files.createFile(probe);
			Files.delete(probe);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	static Report report(List<Check> checks) {
		boolean down = checks.stream().anyMatch(c -> !c.ok() && c.critical());
		boolean degraded = checks.stream().anyMatch(c -> !c.ok());
		String status = down ? "down" : degraded ? "degraded" : "ok";

		StringBuilder json = new StringBuilder();
		json.append("{\"status\":\"").append(status).append("\",\"version\":\"").append(escape(version()))
				.append("\",\"checks\":[");
		for (int i = 0; i < checks.size(); i++) {
			Check check = checks.get(i);
			if (i > 0) {
				json.append(',');
			}
			json.append("{\"name\":\"").append(escape(check.name())).append("\",\"ok\":").append(check.ok())
					.append(",\"critical\":").append(check.critical());
			if (check.reason() != null) {
				json.append(",\"reason\":\"").append(escape(check.reason())).append('"');
			}
			json.append('}');
		}
		json.append("]}");
		return new Report(down ? 503 : 200, json.toString());
	}

	static String escape(String value) {
		StringBuilder out = new StringBuilder();
		for (char c : value.toCharArray()) {
			switch (c) {
				case '"' -> out.append("\\\"");
				case '\\' -> out.append("\\\\");
				default -> {
					if (c < 0x20) {
						out.append(String.format("\\u%04x", (int) c));
					} else {
						out.append(c);
					}
				}
			}
		}
		return out.toString();
	}
}
