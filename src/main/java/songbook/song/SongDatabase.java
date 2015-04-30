package songbook.song;

import songbook.server.ChannelUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SongDatabase {

    public static String SONG_EXTENSION = ".song";

    private final Logger logger = Logger.getLogger("Songbook");

    private Path songDir;

    public SongDatabase(Path songDir) throws IOException {
        this.songDir = songDir;

        if (Files.exists(songDir) == false) {
            Files.createDirectories(songDir);
        }
    }

    public void clearCache() {
        // TODO to implement when a cache will be needed.
    }

    public Stream<String> listSongIds() {
        try {
            return Files.list(songDir).map(SongDatabase::extractId);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't list songs", e);
            return Stream.empty();
        }
    }

    public ReadableByteChannel readChannelForSong(String id) {
        try {
            return Files.newByteChannel(getSongPath(id));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't read song '" + id + "'", e);
            return null;
        }
    }

    public String getSongContents(String id) {
        try {
            ReadableByteChannel channel = readChannelForSong(id);
            return channel == null ? null : ChannelUtil.getStringContents(channel);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't read song '" + id + "'", e);
            return null;
        }
    }

    public WritableByteChannel writeChannelForSong(String id) {
        try {
            Path path = getSongPath(id);
            if (Files.exists(path) == false) {
                Files.createDirectories(path.getParent());
            }
            return Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.SYNC);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't write or create song '" + id + "'", e);
            return null;
        }
    }

    public boolean delete(String id) {
        try {
            Files.delete(getSongPath(id));
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't delete song '" + id + "'", e);
            return false;
        }
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
        String id = encodeId(artist + "-" + title);
        int i = 1;
        while (exists(id)) {
            id = encodeId(artist + "-" + title + "_" + i);
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
     * @param songPath
     * @return
     */
    private static String extractId(Path songPath) {
        String filename = songPath.getFileName().toString();
        return filename.substring(0, filename.length() - SONG_EXTENSION.length());
    }

    private Path getSongPath(String id) {
        return songDir.resolve(id + SONG_EXTENSION);
    }

    private static String encodeId(String id) {
        try {
            id = id.replace("'", " ").replace("\"", " ").trim();
            id = Normalizer.normalize(id, Normalizer.Form.NFD);
            id = id.replaceAll("\\p{M}", "").toLowerCase();
            return URLEncoder.encode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // do nothing
            return id;
        }
    }
}
