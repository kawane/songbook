package songbook.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by laurent on 09/05/2014.
 */
public class SongParser {


    protected ParserInput input;

    public void parse(Reader reader) throws IOException {
        input = new ParserInput(reader);
        char c = input.peek();
        StringBuilder sb = new StringBuilder();
        while (c != ParserInput.EOS) {
            if (c == '{') {
                // directive
                Directive directive = parseDirective();
                System.out.printf("%s at %d\n", directive.toString(), sb.length());
            } else if (c == '[') {
                // chord
                String chord = parseChord();
                System.out.printf("Chord %s at %d\n", chord, sb.length());
            } else {
                sb.append(c);
                input.skip(1);
            }
            c = input.peek();
        }
        System.out.println(sb.toString());
    }

    protected String parseChord() throws IOException {
        input.skip(1);
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = input.peek();
            input.skip(1);
            if (c == ParserInput.EOS) {
                return "";
            } else if (c == ']') {
                return sb.toString();
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

                return new Directive(name, value);
            } else {
                sb.append(c);
            }
        }

    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = Files.newBufferedReader(Paths.get("data/songs/houseoftherisingsun.song"), Charset.forName("UTF-8"));
        try {
            new SongParser().parse(reader);
        } finally {
            reader.close();
        }
    }

}
