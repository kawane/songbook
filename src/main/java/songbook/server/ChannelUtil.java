package songbook.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * Channels utility methods
 */
public class ChannelUtil {


    /** Gets String contents from channel and closes it. */
    public static String getStringContents(ReadableByteChannel channel) throws IOException {
        // Read every byte first, then decode once: decoding chunk by chunk
        // would corrupt any multi-byte UTF-8 character straddling a chunk
        // boundary (e.g. accented letters in a song over 8 KB).
        try (InputStream in = Channels.newInputStream(channel)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Writes String contents to channel and closes it. */
    public static void writeStringContents(String contents, WritableByteChannel channel) throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(contents.getBytes(StandardCharsets.UTF_8));
            channel.write(buffer);
        } finally {
            channel.close();
        }
    }
}
