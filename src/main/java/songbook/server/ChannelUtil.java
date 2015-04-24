package songbook.server;

import io.undertow.websockets.core.UTF8Output;

import java.io.IOException;
import java.nio.ByteBuffer;
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
            UTF8Output utf8Output = new UTF8Output();
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 8);

            int bytesRead = channel.read(buffer);
            while (bytesRead != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    utf8Output.write(buffer);

                }
                buffer.clear();
                bytesRead = channel.read(buffer);
            }
            return utf8Output.extract();
        } finally {
            channel.close();
        }
    }

    /** Writes String contents to channel and closes it. */
    public static void writeStringContents(String contents, WritableByteChannel channel) throws IOException {
        // TODO Checks if a supplier would be nice
        try {
            ByteBuffer buffer = ByteBuffer.wrap(contents.getBytes(StandardCharsets.UTF_8));
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        } finally {
            channel.close();
        }
    }
}
