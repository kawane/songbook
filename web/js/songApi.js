define(["require", "exports", "./utils"], function (require, exports, utils) {
    exports.PathUrl = "/songs/";
    function create(songUrl) {
        return new SongApi(songUrl);
    }
    exports.create = create;
    var SongApi = (function () {
        function SongApi(songUrl) {
            this.songUrl = songUrl;
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
            utils.request({
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
            utils.request({
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
            utils.request({
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
            utils.request({
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
            utils.request({
                method: "DELETE",
                url: this.url(id),
                onSuccess: onSuccess,
                onError: onError
            });
        };
        return SongApi;
    })();
    exports.SongApi = SongApi;
});
//# sourceMappingURL=SongApi.js.map