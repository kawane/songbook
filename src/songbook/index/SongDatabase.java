package songbook.index;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SongDatabase {

    public static String SONG_EXTENSION = ".song";

    private Path songDir;

    private Vertx vertx;

    public SongDatabase(Vertx vertx, Path songDir) {
        this.vertx = vertx;
        this.songDir = songDir;
    }

    public void clearCache() {
        ConcurrentSharedMap<Object, String> songs = vertx.sharedData().getMap("songs");
        songs.clear();
    }

    public void listSongIds(Handler<AsyncResult<String[]>> handler) {
        DefaultFutureResult<String[]> event = new DefaultFutureResult<>();
        event.setHandler(handler);
        vertx.fileSystem().readDir(songDir.toString(), ".*\\.song", (e) -> {
            if (e.succeeded()) {
                String[] files = e.result();
                String[] ids = new String[files.length];
                for (int i = 0; i < ids.length; i++) {
                    ids[i] = extractId(files[i]);
                }
                event.setResult(ids);
            }
        });
    }

    public void readSong(String id, Handler<AsyncResult<String>> handler) {
        DefaultFutureResult<String> event = new DefaultFutureResult<>();
        event.setHandler(handler);
        try {
            ConcurrentSharedMap<Object, String> songs = vertx.sharedData().getMap("songs");
            String song = songs.get(id);
            if (song != null) {
                event.setResult(song);
            } else {
                vertx.fileSystem().readFile(getSongPath(id).toString(), (e) -> {
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

    public void writeSong(String id, String songData, Handler<AsyncResult<Void>> handler) {
        ConcurrentSharedMap<Object, String> songs = vertx.sharedData().getMap("songs");
        songs.put(id, songData);
        vertx.fileSystem().writeFile(getSongPath(id).toString(), new Buffer(songData), handler);
    }

    public void delete(String id) {
        vertx.fileSystem().delete(getSongPath(id).toString(), (ar) -> {/* do nothing */});
        // removes song from vert.x cache (using old title)
        ConcurrentSharedMap<Object, String> songs = vertx.sharedData().getMap("songs");
        songs.remove(id);
    }

    /** Verify if song exists */
    public boolean exists(String id) {
        return Files.exists(getSongPath(id));
    }

    /**
     * Generate a clean id using title and artist information.
     * @param title
     * @param artist
     * @return
     */
    public String generateId(String title, String artist) {
        String id = encodeUrl(artist + "-" + title);
        int i = 1;
        while (exists(id)) {
            id = encodeUrl(artist + "-" + title+ "_" + i);
            i++;
        }
        return id;
    }

    /**
     * Is this filePath correspond to a song file testing
     * @param filePath
     * @return
     */
    private static boolean isSong(Path filePath) {
        return Files.isRegularFile(filePath) && filePath.toString().endsWith(SONG_EXTENSION);
    }

    /**
     * Assuming that file ending with '.song' extension
     * @param songFile
     * @return
     */
    private static String extractId(String songFile) {
        String filename = Paths.get(songFile).getFileName().toString();
        return filename.substring(0, filename.length() - SONG_EXTENSION.length());
    }

    private Path getSongPath(String id) {
        return songDir.resolve(id + SONG_EXTENSION);
    }

    private static String encodeUrl(String id) {
        try {
            return URLEncoder.encode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // do nothing
            return id;
        }
    }

}
