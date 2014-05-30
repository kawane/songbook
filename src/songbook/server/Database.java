package songbook.server;

import songbook.index.Song;
import songbook.index.SongParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

/**
 * Created by j5r on 26/04/2014.
 */
public class Database {

    public final static String DEFAULT_DATA_ROOT = "data";

    private final static String SONG_EXTENSION = ".cho";
    private final static String SONGS_DIRECTORY = "songs";

    private final Path dataRoot;

    public Database() {
        dataRoot = getDataRoot();
    }

    private Path getDataRoot() {
        final String dataRoot = System.getenv("DATA_ROOT");
        return Paths.get(dataRoot == null ? DEFAULT_DATA_ROOT : dataRoot);
    }

    public Stream<Song> getAllSongs() {
        final Builder songs = Stream.builder();
        final File[] files = dataRoot.resolve(SONGS_DIRECTORY).toFile().listFiles();
        if ( files != null ) {
            for (File file : files) {
                if ( file.getName().endsWith(".cho") ) {
                    final String id = removeExtension(file.getName());
                    songs.add(new Song(id));
                }
            }
        }
        return songs.build();
    }

    public Song getSong(String id) throws IOException {
        final Path songPath = dataRoot.resolve(SONGS_DIRECTORY).resolve(id+SONG_EXTENSION);
        final File file = songPath.toFile();
        if ( file.exists() == false ) return null;

        final SongParser parser = new SongParser();
        return parser.parse(id, new FileReader(file));
    }

    public String removeExtension(String fileName) {
        final int index = fileName.indexOf(".");
        if ( index <= 0 ) return fileName;
        return fileName.substring(0, index);
    }
}
