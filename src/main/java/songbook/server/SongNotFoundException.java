package songbook.server;

import io.undertow.util.StatusCodes;

/**
 * Song not found.
 */
public class SongNotFoundException extends ServerException {

    private final String id;

    public SongNotFoundException(String id) {
        super(StatusCodes.NOT_FOUND);
        this.id = id;
    }

    @Override
    public void errorText(StringBuilder out) {
        Templates.alertSongDoesNotExist(out, id);
    }
}
