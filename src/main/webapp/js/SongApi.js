define(["require", "exports"], function (require, exports) {
    /** Song Api class that helps using http song api */
    var SongApi = (function () {
        function SongApi(songUrl, sessionKey) {
            this.songUrl = songUrl;
            this.sessionKey = sessionKey;
        }
        /**
         * Return the url for this song id.
         *
         * @param id
         * @returns {string}
         */
        SongApi.prototype.url = function (id) {
            return this.songUrl + id;
        };
        /**
         * Search songs
         *
         * @param id
         * @param onSuccess
         * @param onError
         */
        SongApi.prototype.search = function (query, contentType, onSuccess, onError) {
            this.request({
                method: "GET",
                headers: { "Accept": contentType },
                url: "/search/" + encodeURIComponent(query),
                onSuccess: onSuccess,
                onError: onError
            });
        };
        /**
         * Get a song
         *
         * @param id
         * @param onSuccess
         * @param onError
         */
        SongApi.prototype.get = function (id, contentType, onSuccess, onError) {
            this.request({
                method: "GET",
                headers: { "Accept": contentType },
                url: this.url(id),
                onSuccess: onSuccess,
                onError: onError
            });
        };
        /**
         * Create a song
         *
         * @param {string} song
         * @param onSuccess
         * @param onError
         */
        SongApi.prototype.create = function (song, onSuccess, onError) {
            this.request({
                method: "POST",
                url: this.songUrl,
                data: song,
                onSuccess: onSuccess,
                onError: onError
            });
        };
        /**
         *  Modify a Song
         *
         * @param {string} id
         * @param {string} song
         * @param onSuccess
         * @param onError
         */
        SongApi.prototype.update = function (id, song, onSuccess, onError) {
            this.request({
                method: "PUT",
                url: this.url(id),
                data: song,
                onSuccess: onSuccess,
                onError: onError
            });
        };
        /**
         * Remove a song
         *
         * @param {string} id
         * @param onSuccess
         * @param onError
         */
        SongApi.prototype.remove = function (id, onSuccess, onError) {
            this.request({
                method: "DELETE",
                url: this.url(id),
                onSuccess: onSuccess,
                onError: onError
            });
        };
        SongApi.prototype.request = function (requestData) {
            var req = new XMLHttpRequest();
            req.open(requestData.method, requestData.url, true);
            if (requestData.headers) {
                Object.keys(requestData.headers).forEach(function (headerName) {
                    req.setRequestHeader(headerName, requestData.headers[headerName]);
                });
            }
            if (this.sessionKey) {
                req.setRequestHeader("Cookie", "SessionKey=" + this.sessionKey);
            }
            req.onreadystatechange = function () {
                if (req.readyState == 4) {
                    if (req.status < 300) {
                        if (requestData.onSuccess) {
                            requestData.onSuccess(req.response, req);
                        }
                    }
                    else {
                        if (requestData.onError) {
                            requestData.onError(req.response, req);
                        }
                    }
                }
            };
            req.send(requestData.data);
        };
        return SongApi;
    })();
    return SongApi;
});
//# sourceMappingURL=SongApi.js.map