package songbook.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Channels utility methods
 */
public class ChannelUtil {

    public static String getStringContents(ReadableByteChannel channel) throws IOException {
        return getStringContents(channel, StandardCharsets.UTF_8);
    }

    public static String getStringContents(ReadableByteChannel channel, Charset charset) throws IOException {
        StringBuilder contents = new StringBuilder();

        ByteBuffer buffer = ByteBuffer.allocate(1024 * 8);
        try (ReadableByteChannel songChannel = channel) {
            int read = songChannel.read(buffer);
            while (read > 0) {
                contents.append(new String(buffer.array(), charset));
                read = songChannel.read(buffer);
            }
        }

        return contents.toString();
    }
}
