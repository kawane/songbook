package songbook.chordpro;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

/**
 * Created by laurent on 14/05/2014.
 */
public class Song {

    static public class PositionedData<T> {
        public T data;
        public int position;

        public PositionedData(T data, int position) {
            this.data = data;
            this.position = position;
        }
    }

    public List<PositionedData<Directive>> directives = new ArrayList<>();

    public List<PositionedData<Chord>> chords = new ArrayList<>();

    public String lyrics;

    public final String id;

    public Song(String id) {
        this.id = id;
    }

    public String findTitle() {
        for (PositionedData<Directive> directive : directives) {
            if ( "title".equals(directive.data.name.toLowerCase()) ) {
                return  directive.data.value;
            }
        }
        return null;
    }


    public Stream<String> findAuthors() {
        final Builder<String> authors = Stream.builder();
        for (PositionedData<Directive> directive : directives) {
            if ( "author".equals(directive.data.name.toLowerCase()) ) {
                authors.add(directive.data.value);
            }
        }
        return authors.build();
    }

}
