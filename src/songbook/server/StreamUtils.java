package songbook.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by j5r on 30/04/2014.
 */
public class StreamUtils {

    public static String getContents(InputStream stream, String encoding) throws IOException {
        final Charset charset = Charset.forName(encoding);
        final StringBuilder contents = new StringBuilder();
        byte[] buffer = new byte[2048];
        int read = stream.read(buffer);
        while (read >= 0 ) {
            contents.append(new String(buffer, 0, read, charset));
            read = stream.read(buffer);
        }
        return contents.toString();
    }
}
