package songbook.server;

import io.undertow.util.StatusCodes;

/**
 * Song not found.
 */
public class SongNotFoundException extends ServerException {

    private static final long serialVersionUID = 1203265853316984170L;
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
