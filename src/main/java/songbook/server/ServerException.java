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

import io.undertow.util.StatusCodes;

/**
 * ServerException used by exception handler
 */
public class ServerException extends Exception {

    public static final ServerException NOT_FOUND = new ServerException(StatusCodes.NOT_FOUND);
    public static final ServerException BAD_REQUEST = new ServerException(StatusCodes.BAD_REQUEST);

    private final int code;

    public ServerException(int code) {
        super(StatusCodes.getReason(code));
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
