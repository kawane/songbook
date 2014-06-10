package songbook.server;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import songbook.index.SongIndex;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by j5r on 26/04/2014.
 */
public class Database {

    public final static String DEFAULT_DATA_ROOT = "data";

    private final static String SONG_EXTENSION = ".html";
    private final static String SONGS_DIRECTORY = "songs";
    public static final Charset UTF8_CHARSET = Charset.forName("utf-8");

    private final Path dataRoot;

    private final Vertx vertx;

    public Database(Vertx vertx) {
        this.vertx = vertx;
        dataRoot = getDataRoot();
    }

    private Path getDataRoot() {
        final String dataRoot = System.getenv("DATA_ROOT");
        return Paths.get(dataRoot == null ? DEFAULT_DATA_ROOT : dataRoot);
    }

    public SongIndex getAllSongIndex() {
        final SongIndex index = new SongIndex();
        final File[] files = dataRoot.resolve(SONGS_DIRECTORY).toFile().listFiles();
        if ( files != null ) {
            for (File file : files) {
                if ( file.getName().endsWith(".html") ) {
                    final String id = removeExtension(file.getName());
                    index.addSong(id, id, null);
                }
            }
        }
        return index;
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
                final Path songPath = dataRoot.resolve(SONGS_DIRECTORY).resolve(id + SONG_EXTENSION).toAbsolutePath();
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

    public String removeExtension(String fileName) {
        final int index = fileName.indexOf(".");
        if ( index <= 0 ) return fileName;
        return fileName.substring(0, index);
    }
}
