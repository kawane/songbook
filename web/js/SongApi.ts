/** Song Api class that helps using http song api */
class SongApi {

    songUrl: string;

    sessionKey: string;

    constructor(songUrl: string, sessionKey?: string) {
        this.songUrl = songUrl;
        this.sessionKey = sessionKey;
    }

    /**
     * Return the url for this song id.
     *
     * @param id
     * @returns {string}
     */
    url(id: string): string {
        return this.songUrl + id;
    }

    /**
     * Search songs
     *
     * @param id
     * @param onSuccess
     * @param onError
     */
    search(query: string, contentType: string, onSuccess: (song: string) => void, onError?: (error: string) => void) {
        this.request({
            method: "GET",
            headers: {"Accept": contentType},
            url:  "/search/" + encodeURIComponent(query),
            onSuccess: onSuccess,
            onError: onError
        });
    }

    /**
     * Get a song
     *
     * @param id
     * @param onSuccess
     * @param onError
     */
    get(id: string, contentType: string, onSuccess: (song: string) => void, onError?: (error: string) => void) {
        this.request({
            method: "GET",
            headers: {"Accept": contentType},
            url:  this.url(id),
            onSuccess: onSuccess,
            onError: onError
        });
    }

    /**
     * Create a song
     *
     * @param {string} song
     * @param onSuccess
     * @param onError
     */
    create(song: string, onSuccess: (id: string) => void, onError?: (error: string) => void) {
        this.request({
            method: "POST",
            url:  this.songUrl,
            data: song,
            onSuccess: onSuccess,
            onError: onError
        });
    }

    /**
     *  Modify a Song
     *
     * @param {string} id
     * @param {string} song
     * @param onSuccess
     * @param onError
     */
    update(id: string, song: string, onSuccess?: (result: string) => void, onError?: (error: string) => void) {
        this.request({
            method: "PUT",
            url: this.url(id),
            data: song,
            onSuccess: onSuccess,
            onError: onError
        });
    }

    /**
     * Remove a song
     *
     * @param {string} id
     * @param onSuccess
     * @param onError
     */
    remove(id: string, onSuccess?: (result: string) => void, onError?: (error: string) => void) {
        this.request( {
            method: "DELETE",
            url: this.url(id),
            onSuccess: onSuccess,
            onError: onError
        });
    }


    request(requestData: RequestData) {
        var req = new XMLHttpRequest();
        req.open(requestData.method, requestData.url, true);
        if (requestData.headers) {
            Object.keys(requestData.headers).forEach((headerName) => {
                req.setRequestHeader(headerName, requestData.headers[headerName]);
            });
        }
        if (this.sessionKey) {
            req.setRequestHeader("Cookie", "SessionKey="+this.sessionKey);
        }
        req.onreadystatechange = () => {
            if (req.readyState == 4) {
                if (req.status < 300) {
                    if (requestData.onSuccess) {
                        requestData.onSuccess(req.response, req);
                    }
                } else {
                    if (requestData.onError) {
                        requestData.onError(req.response, req);
                    }
                }
            }
        };
        req.send(requestData.data);
    }

}


interface OnSuccess {
    (data: string, req?: XMLHttpRequest): void;
}

interface OnError {
    (error: string, req?: XMLHttpRequest): void;
}

interface RequestData {
    method: string;
    url: string;
    headers?: any;
    data?: any;
    onSuccess?: OnSuccess;
    onError?: OnError;
}

export = SongApi;
