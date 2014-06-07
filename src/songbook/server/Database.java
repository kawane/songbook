package songbook.server;

import org.vertx.java.core.Vertx;
import songbook.index.SongIndex;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
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


    public String readHtmlSong(String id) throws IOException {
        final Path songPath = dataRoot.resolve(SONGS_DIRECTORY).resolve(id+SONG_EXTENSION);
        Reader reader = Files.newBufferedReader(songPath);
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[8192];
        int read = reader.read(buf);
        while (read != -1) {
            sb.append(buf, 0, read);
            read = reader.read(buf);
        }
        return sb.toString();
    }

    public String removeExtension(String fileName) {
        final int index = fileName.indexOf(".");
        if ( index <= 0 ) return fileName;
        return fileName.substring(0, index);
    }
}
