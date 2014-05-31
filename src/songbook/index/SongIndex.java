package songbook.index;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * The SongIndex stores a set of songs descriptions.
 */
public class SongIndex {

    private final SortedSet<String> songIds = new TreeSet<>();

    private final Map<String, String> songTitles = new HashMap<>();

    private final Map<String, Stream<String>> songAuthors = new HashMap<>();

    public void addSong(String id, String title, Stream<String> authors) {
        songIds.add(id);
        songTitles.put(id, title);
        songAuthors.put(id, authors);
    }

    public Stream<String> getSongs() {
        return songIds.stream();
    }

    public String getTitle(String id) {
        return songTitles.get(id);
    }

    public Stream<String> getAuthors(String id) {
        final Stream<String> stream = songAuthors.get(id);
        return stream == null ? Stream.empty() : stream;
    }

}
