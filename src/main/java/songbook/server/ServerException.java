/*
 * Copyright 2015 to CloudModelExplorer authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package songbook.server;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

/**
 * ServerException used by exception handler
 */
public class ServerException extends Exception {

    private static final long serialVersionUID = 6619087202310932044L;
    public static final ServerException NOT_FOUND = new ServerException(StatusCodes.NOT_FOUND);
    public static final ServerException BAD_REQUEST = new ServerException(StatusCodes.BAD_REQUEST);
    public static final ServerException METHOD_NOT_ALLOWED = new ServerException(StatusCodes.METHOD_NOT_ALLOWED);

    private final int code;

    public ServerException(int code) {
        super(StatusCodes.getReason(code));
        this.code = code;
    }

    public ServerException(int code, String message) {
        super(StatusCodes.getReason(code) + ":" + message);
        this.code = code;
    }

    public void serveError(String role, HttpServerExchange exchange) {
        exchange.setStatusCode(code);

        StringBuilder out = new StringBuilder();
        Templates.header(out, Integer.toString(code), role);
        errorText(out);
        Templates.footer(out);

        exchange.getResponseSender().send(out.toString());
    }

    public void errorText(StringBuilder out) {
        out.append(getMessage());
    }
}
