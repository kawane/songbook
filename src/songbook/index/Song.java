package songbook.index;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laurent on 14/05/2014.
 */
public class Song {

    static public class PositionnedData<T> {
        public T data;
        public int position;

        public PositionnedData(T data, int position) {
            this.data = data;
            this.position = position;
        }
    }

    public List<PositionnedData<Directive>> directives = new ArrayList<>();

    public List<PositionnedData<Chord>> chords = new ArrayList<>();

    public String lyrics;


}
