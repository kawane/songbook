package songbook.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * Channels utility methods
 */
public class ChannelUtil {


    /** Gets String contents from channel and closes it. */
    public static String getStringContents(ReadableByteChannel channel) throws IOException {
        // TODO Checks if a supplier would be nice
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 8);
            StringBuilder sb = new StringBuilder();
            int bytesRead = channel.read(buffer);
            while (bytesRead != -1) {
                buffer.flip();
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
                sb.append(charBuffer.toString());
                buffer.clear();
                bytesRead = channel.read(buffer);
            }
            return sb.toString();
        } finally {
            channel.close();
        }
    }

    /** Writes String contents to channel and closes it. */
    public static void writeStringContents(String contents, WritableByteChannel channel) throws IOException {
        // TODO Checks if a supplier would be nice
        try {
            ByteBuffer buffer = ByteBuffer.wrap(contents.getBytes(StandardCharsets.UTF_8));
            channel.write(buffer);
        } finally {
            channel.close();
        }
    }
}
