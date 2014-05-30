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

    private final static String SONG_EXTENSION = ".cho";
    private final static String SONGS_DIRECTORY = "data/songs";

    public static Stream<Song> getAllSongs() {
        final Builder songs = Stream.builder();
        final File[] files = Paths.get(SONGS_DIRECTORY).toFile().listFiles();
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

    public static Song getSong(String id) throws IOException {
        final Path songPath = Paths.get(SONGS_DIRECTORY, id+SONG_EXTENSION);
        final File file = songPath.toFile();
        if ( file.exists() == false ) return null;

        final SongParser parser = new SongParser();
        return parser.parse(id, new FileReader(file));
    }

    public static boolean isValidId(String id) {
        if (id==null || id.length() == 0) return false;
        if (Character.isJavaIdentifierStart(id.charAt(0)) == false ) return false;
        for (int i=1; i<id.length(); i+=1) {
            if (Character.isJavaIdentifierPart(id.charAt(i)) == false) return false;
        }
        return true;
    }

    public static String removeExtension(String fileName) {
        final int index = fileName.indexOf(".");
        if ( index <= 0 ) return fileName;
        return fileName.substring(0, index);
    }
}
