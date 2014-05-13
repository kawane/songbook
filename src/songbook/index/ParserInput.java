package songbook.index;

import java.io.IOException;
import java.io.Reader;

public class ParserInput {

    public static char EOS = '\uFFFF';

    protected Reader reader;

    protected char buf;

    public ParserInput(Reader reader) throws IOException {
        this.reader = reader;
        buf = (char)reader.read();
    }

    public char peek() {
        return buf;
    }

    public void skip(int count) throws IOException {
        if (buf != EOS) {
            if (count > 1) {
                reader.skip(count - 1);
            }
            buf = (char)reader.read();
        }
    }

}