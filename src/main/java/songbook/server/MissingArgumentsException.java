package songbook.server;

import io.undertow.util.StatusCodes;

/**
 * Song not found.
 */
public class MissingArgumentsException extends ServerException {

    private final String[] arguments;

    public MissingArgumentsException(String ... arguments) {
        super(StatusCodes.BAD_REQUEST);
        this.arguments = arguments;
    }

    @Override
    public void errorText(StringBuilder out) {
        Templates.alertMissingArguments(out, String.join(",", arguments));
    }
}
