package songbook.index;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SongUtil {

    private final static String HEXES = "0123456789abcdef";

    public static String getIdFromTitle(String title) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digest = messageDigest.digest(title.getBytes(StandardCharsets.UTF_8));

            final StringBuilder hex = new StringBuilder( 2 * digest.length );
            for (int i = 0; i < digest.length; i+=1) {
                hex.append(HEXES.charAt((digest[i] & 0xF0) >> 4));
                hex.append(HEXES.charAt((digest[i] & 0x0F)));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // shouldn't happen
            return title;
        }
    }
}
