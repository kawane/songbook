package songbook.chordpro;

import songbook.chordpro.Song.PositionedData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by laurent on 09/05/2014.
 */
public class SongParser {

    protected ParserInput input;

    public static final Map<String, String> aliases = new HashMap<>();

    static {
        aliases.put("t", "title");
        aliases.put("c", "comment");
    }

    public Song parse(String id, Reader reader) throws IOException {
        input = new ParserInput(reader);
        char c = input.peek();
        StringBuilder sb = new StringBuilder();
        Song song = new Song(id);
        while (c != ParserInput.EOS) {
            if (c == '{') {
                // directive
                song.directives.add(new PositionedData<>(parseDirective(), sb.length()));
            } else if (c == '[') {
                // chord
                song.chords.add(new PositionedData<>(parseChord(), sb.length()));
            } else {
                sb.append(c);
                input.skip(1);
            }
            c = input.peek();
        }
        song.lyrics = sb.toString();
        return song;
    }

    protected Chord parseChord() throws IOException {
        input.skip(1);
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = input.peek();
            input.skip(1);
            if (c == ParserInput.EOS) {
                return new Chord("");
            } else if (c == ']') {
                return new Chord(sb.toString());
            } else {
                sb.append(c);
            }
        }
    }

    protected Directive parseDirective() throws IOException {
        input.skip(1);

        StringBuilder sb = new StringBuilder();
        String name = null;
        String value = null;
        while (true) {
            char c = input.peek();
            input.skip(1);
            if (c == ParserInput.EOS) {
                return new Directive(null, null);
            } else if (c == ':') {
                name = sb.toString();
                sb.setLength(0);
            } else if (c == '}') {
                if (name == null) {
                    name = sb.toString();
                } else {
                    value = sb.toString();
                }
                name = name.toLowerCase();
                String alias = aliases.get(name);
                if (alias != null) {
                    name = alias;
                }
                return new Directive(name, value);
            } else {
                sb.append(c);
            }
        }

    }

    public static void main(String[] args) throws IOException {
        final Path path = Paths.get("data/songs/houseoftherisingsun.song");
        BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"));
        try {
            new SongParser().parse(path.getFileName().toString(), reader);
        } finally {
            reader.close();
        }
    }

}
