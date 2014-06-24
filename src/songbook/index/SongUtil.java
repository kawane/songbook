package songbook.index;

public class SongUtil {

    public static String getId(String fileName) {
        final int index = fileName.lastIndexOf(".");
        if ( index <= 0 ) return fileName;
        return fileName.substring(0, index);
    }
}